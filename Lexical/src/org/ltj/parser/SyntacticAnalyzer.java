package org.ltj.parser;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * 语法分析器
 * 分析C++源程序语法
 * 分析语法通过3种算法实现
 * 1.预测分析法
 * 2.算符优先法
 * 3.LR(1)分析法
 *
 * 该类主要是为了构造预测分析表，采用算法1
 * 输入：产生式 productions.txt
 * 输出：预测分析表 predict.txt
 * @author ltaoj
 *
 */
public class SyntacticAnalyzer {
	/**
	 * 所有产生式集合
	 */
	private ArrayList<Production> mProductions;
	/**
	 * 所有终结符集合
	 */
	private ArrayList<String> mTerminals;
	/**
	 * 所有非终结符集合
	 */
	private ArrayList<String> mNonTerminals;
	/**
	 * first集
	 */
	private HashMap<String, ArrayList<String>> mFirst;
	/**
	 * follow集
	 */
	private HashMap<String, ArrayList<String>> mFollow;
	/**
	 * 预测分析表
	 */
	private HashMap<String, String> predictTable;
	private String inputFileName = "production";
	private String outputFileName = "predict";
	public SyntacticAnalyzer() {
		mProductions = new ArrayList<>();
		mTerminals = new ArrayList<>();
		mNonTerminals = new ArrayList<>();
		mFirst = new HashMap<>();
		mFollow = new HashMap<>();
		// 从文件读取产生式
		loadProductions();
		// 从产生式得到非终结符
		getNonTerminals();
		// 从产生式得到终结符
		getTerminals();
		// 计算出First集
		computeFirst();
		// 计算出follow集
		computeFollow();
		// 填充产生式可以接受的输入字符
		fillAccCharactor();
		// 产生预测分析结果
		generatePredict();

	}
	/**
	 * 从文件读取产生式
	 */
	private void loadProductions() {
		File file = new File(inputFileName);
		try {
			RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");
			String line;
			String left;
			String[] rights;
			while((line = randomAccessFile.readLine())!=null){
				left = line.split("->")[0].trim();
				rights = line.split("->")[1].trim().split(" ");
				mProductions.add(new Production(left, rights));
			}
			randomAccessFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	/**
	 * 从已读取的产生式中得到非终结符
	 */
	private void getNonTerminals() {
		for(int i = 0;i < mProductions.size();i++){
			if (mNonTerminals.contains(mProductions.get(i).getLeftSign())) {
				continue;
			}
			mNonTerminals.add(mProductions.get(i).getLeftSign());
		}
	}
	/**
	 * 从已读取的产生式中得到终结符
	 */
	private void getTerminals() {
		for(int i = 0;i < mProductions.size();i++){
			String[] rights = mProductions.get(i).getRights();
			for(int j = 0;j < rights.length;j++){
				if(mNonTerminals.contains(rights[j])||
						mTerminals.contains(rights[j])||rights[j].equals("~"))
					continue;
				mTerminals.add(rights[j]);
			}
		}
	}
	/**
	 * 计算first集合
	 */
	private void computeFirst() {
		ArrayList<String> firsts;
		// 首先将终结符放到自己的first集中
		for(int i = 0;i < mTerminals.size();i++){
			firsts = new ArrayList<>();
			firsts.add(mTerminals.get(i));
			mFirst.put(mTerminals.get(i), firsts);
		}
		// 给非终结符初始化一个空的first集
		for(int i = 0;i < mNonTerminals.size();i++){
			firsts = new ArrayList<>();
			mFirst.put(mNonTerminals.get(i), firsts);
		}

		boolean flag;
		while(true){
			flag = true;// 循环结束的标志,只要有改变就将flag值false
			String left;
			String right0;
			String[] rights;
			for(int i = 0;i < mProductions.size();i++){
				Production production = mProductions.get(i);
				left = production.getLeftSign();
				right0 = production.getRights()[0];
				rights = production.getRights();
				if(isTerminal((right0))||isNullChar(right0)){// 如果右边第一个是终结符，则加入到左边非终结符first集中
					if(!addToFirst(left, right0)) continue;
					flag = false;
				}else if(isNonTerminal(right0)){// 如果右边第一个是非终结符
					for(int j = 0;j < mFirst.get(right0).size();j++){// 把右边第一个非终结符first集合并到左边非终结符first集合
						if(!addToFirst(left, mFirst.get(right0).get(j))) continue;
						flag = false;
					}

					for(int k = 0;k < rights.length;k++){// 循环得到右边从第一个开始连续的k个first集中都含有空字符的非终结符
						if(isNonTerminal(rights[k])&&mFirst.get(rights[k]).contains("~")){
							if(k == rights.length -1){
								if(!addToFirst(left, "~")) continue;
							}
							continue;
						}
						for(int m = 0;m < mFirst.get(rights[k]).size();m++){// 将第k + 1个非终结符的first集加到左边非终结符的first集合
							if(isNullChar(mFirst.get(rights[k]).get(m))){
								continue;
							}
							if(!addToFirst(left, mFirst.get(rights[k]).get(m))) continue;
							flag = false;
						}
						break;
					}
				}
			}
			if(flag){
				break;
			}
		}
	}
	/**
	 * 将item添加到left的first集合
	 * @param left
	 * @param item
	 * @return
	 */
	private boolean addToFirst(String left, String item){
		if(mFirst.get(left).contains(item)){
			return false;
		}
		mFirst.get(left).add(item);
		return true;
	}
	/**
	 * 计算follow集
	 */
	private void computeFollow() {
		ArrayList<String> follows;
		// 初始化各个符号的follow集合
		for(int i = 0;i < mNonTerminals.size();i++){
			follows = new ArrayList<>();
			mFollow.put(mNonTerminals.get(i), follows);
		}
		// 将#添加到文法开始符号的folow集
		mFollow.get("S").add("#");
		boolean flag;
		while(true){
			flag = true;
			String left;
			String[] rights;

			for(int i = 0;i < mProductions.size();i++){
				left = mProductions.get(i).getLeftSign();
				rights = mProductions.get(i).getRights();

				for(int j = 0;j < rights.length;j++){
					if(mNonTerminals.contains(rights[j])){
						for(int k = j + 1;k < rights.length;k++){
							for(int m = 0;m < mFirst.get(rights[k]).size();m++){
								if(!addToFollow(rights[j], mFirst.get(rights[k]).get(m))){
									continue;
								}
								flag = false;
							}
							if(mFirst.get(rights[k]).contains("~")){
								if(k == rights.length-1){
									for(int m = 0;m < mFollow.get(left).size();m++){
										if(!addToFollow(rights[j], mFollow.get(left).get(m)))
											continue;
										flag = false;
									}
								}
							}
						}
						if(j == rights.length - 1){
							for(int m = 0;m < mFollow.get(left).size();m++){
								if(!addToFollow(rights[j], mFollow.get(left).get(m)))
									continue;
								flag = false;
							}
						}
					}
				}
			}
			if(flag){
				break;
			}

		}
	}

	private boolean addToFollow(String nonTerminal, String sign){
		if(sign.equals("~") || mFollow.get(nonTerminal).contains(sign)){
			return false;
		}
		mFollow.get(nonTerminal).add(sign);
		return true;
	}
	/**
	 * 填充产生式可以接受的输入字符
	 */
	private void fillAccCharactor() {
		boolean flag = true;
		String left;
		String[] rights;
		for(int i = 0;i < mProductions.size();i++){
			left = mProductions.get(i).getLeftSign();
			rights = mProductions.get(i).getRights();
			// 产生式所有右部符号的first集都是可接受的输入字符
			for(int j = 0;j < rights.length;j++){
				if(isNullChar(rights[j])){
					for(int k = 0;k < mFollow.get(left).size();k++){
						addToAccCharactor(i, mFollow.get(left).get(k));
					}
				}else{
					for(int k = 0;k < mFirst.get(rights[j]).size();k++){
						addToAccCharactor(i, mFirst.get(rights[j]).get(k));
					}
					if(!mFirst.get(rights[j]).contains("~")){
						flag = false;
					}
				}
				if(!flag){
					break;
				}
			}
		}
	}
	/**
	 * 将符号添加到第i个产生式的可输入字符
	 * @param index
	 * @param sign
	 * @return
	 */
	private boolean addToAccCharactor(int index, String sign){
		if(isNullChar(sign) || mProductions.get(index).getmAccCharacters().contains(sign)){
			return false;
		}
		mProductions.get(index).getmAccCharacters().add(sign);
		return true;
	}
	/**
	 * 产生预测分析表
	 */
	private void generatePredict() {
		try {
			File file = new File(outputFileName);
			if(!file.exists()){
				file.createNewFile();
			}
			RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
			String left;
			String[] rights;
			String line="";
			for(int i = 0;i < mProductions.size();i++){
				left = mProductions.get(i).getLeftSign();
				rights = mProductions.get(i).getRights();
				line = left + " -> ";
				for(int j = 0;j < rights.length;j++){
					line += rights[j] + " ";
				}
				String prefix = line;
				for(int k = 0;k < mProductions.get(i).getmAccCharacters().size();k++){
					line = prefix + "<- " + mProductions.get(i).getmAccCharacters().get(k) + "\n";
					randomAccessFile.writeBytes(line);
//					System.out.println(line);
				}
			}
			randomAccessFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/**
	 * 判断符号是否是终结符
	 * @param sign
	 * @return
	 */
	private boolean isTerminal(String sign){
		return mTerminals.contains(sign);
	}
	/**
	 * 判断符号是否是非终结符
	 * @param sign
	 * @return
	 */
	private boolean isNonTerminal(String sign){
		return mNonTerminals.contains(sign);
	}
	/**
	 * 判断一个字符是否为控制符s
	 * @param sign
	 * @return
	 */
	private boolean isNullChar(String sign){
		return sign.equals("~");
	}
	/**
	 * 从预测分析表文件中加载
	 * 存放在HashMap中，key：String(left + "-" + symbol), value: String(rights)
	 */
	private void loadTable(){
		String left;
		String symbol;
		String rights;
		String line;
		predictTable = new HashMap<>();
		try {
			File file = new File(outputFileName);
			RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");
			while((line = randomAccessFile.readLine())!=null){
				left = line.split("->")[0].trim();
				rights = line.split("->")[1].split("<-")[0].trim();
				symbol = line.split("->")[1].split("<-")[1].trim();
				predictTable.put(left + "-" + symbol, rights);
			}
			randomAccessFile.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * 分析源程序是否正确
	 * @param inputStack 输入的符号栈
	 */
	public void analyze(ArrayList<String> inputStack){
		loadTable();
		// 分析栈
		ArrayList<String> inferStack = new ArrayList<>();
		System.out.println(inputStack);
		inferStack.add(0, "#");
		inferStack.add(0, "S");
		boolean flag = false;
		for(int i = 0;i < inputStack.size();i++){
			String X = inferStack.get(0);
			if(X.equals(inputStack.get(i))){
				if(inputStack.get(i).equals("#")){
					flag = true;
					System.out.println("1" + inferStack);
					break;
				}
				// 将栈顶符号移除，输入符号指向下一个
				inferStack.remove(0);
				X = inferStack.get(0);
				System.out.println("2" + inferStack + "symbol" + inputStack.get(i));
				continue;
			}else if(isNonTerminal(X)){
				if(predictTable.containsKey(X + "-" + inputStack.get(i))){
					System.out.println("symbol:" +inputStack.get(i) +"运用产生式:" + X + "->" + predictTable.get(X + "-" + inputStack.get(i)));
					// 得到产生式的右部
					String[] rights = predictTable.get(X + "-" + inputStack.get(i)).trim().split(" ");
					// 将X移除
					inferStack.remove(0);
					X = inferStack.get(0);
					// 将产生式右部反序压栈
					for(int j = rights.length - 1;j >= 0;j--){
						if(rights[j].equals("~"))
							continue;
						inferStack.add(0, rights[j]);
					}
					System.out.println("3" + inferStack);
					i--;
				}else{
					System.out.println("4" + "出错了" + X + "-" + inputStack.get(i));
				}
			}
		}
	}
//	void printFo(){
//		System.out.println("keyset:" + mFollow.keySet());
//		System.out.println("valueset:" + mFollow.values());
//	}
//	void printF(){
//		System.out.println("keyset:" + mFirst.keySet());
//
//		System.out.println("valueset:" + mFirst.values());
//	}
//	void printT(){
//		System.out.println("terminals:" + mTerminals);
//	}
//	void printN(){
//		System.out.println("non:" + mNonTerminals);
//	}
//	void printA(){
//		int count = 0;
//		for(int i =0;i < mProductions.size();i++){
//			count+=mProductions.get(i).getmAccCharacters().size();
//			System.out.println(mProductions.get(i).getmAccCharacters());
//		}
//		System.out.println(count);
//	}
//	public static void main(String[] args){
//		SyntacticAnalyzer sa = new SyntacticAnalyzer();
//		sa.printN();
//		sa.printT();
//		sa.printF();
//		sa.printFo();
//		sa.printA();
//	}
}
