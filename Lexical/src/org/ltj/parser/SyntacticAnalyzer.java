package org.ltj.parser;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * 语法分析器
 * 分析Java源程序语法
 * 分析语法通过3种算法实现
 * 1.预测分析法
 * 2.算符优先法
 * 3.LR(1)分析法
 *
 * 该类主要是为了构造预测分析表
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
	private String inputFileName = "production";
	private String outputFileName;
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
		mFollow.get("E").add("#");
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
								System.out.println(rights[j] +" 1 " + mFirst.get(rights[k]) + " " + mFollow.get(rights[j]));
								if(!addToFollow(rights[j], mFirst.get(rights[k]).get(m))){
									continue;
								}
								flag = false;
							}
							if(mFirst.get(rights[k]).contains("~")){
								if(k == rights.length-1){
									for(int m = 0;m < mFollow.get(left).size();m++){
										System.out.println(rights[j] +" 2 " + mFollow.get(left) + " " + mFollow.get(rights[j]));
										if(!addToFollow(rights[j], mFollow.get(left).get(m)))
											continue;
										flag = false;
									}
								}
							}
						}
						if(j == rights.length - 1){
							for(int m = 0;m < mFollow.get(left).size();m++){
								System.out.println(rights[j] +" 3 " + mFollow.get(left) + " " + mFollow.get(rights[j]));
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
	 * 产生预测分析表
	 */
	private void generatePredict() {

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

	private boolean isNullChar(String sign){
		return sign.equals("~");
	}
	void printFo(){
		System.out.println("keyset:" + mFollow.keySet());
		System.out.println("valueset:" + mFollow.values());
	}
	void printF(){
		System.out.println("keyset:" + mFirst.keySet());

		System.out.println("valueset:" + mFirst.values());
	}
	void printT(){
		System.out.println("terminals:" + mTerminals);
	}
	void printN(){
		System.out.println("non:" + mNonTerminals);
	}
	public static void main(String[] args){
		SyntacticAnalyzer sa = new SyntacticAnalyzer();
		sa.printN();
		sa.printT();
		sa.printF();
		sa.printFo();
	}
}
