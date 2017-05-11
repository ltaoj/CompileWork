package org.ltj.parser;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * �﷨������
 * ����C++Դ�����﷨
 * �����﷨ͨ��3���㷨ʵ��
 * 1.Ԥ�������
 * 2.������ȷ�
 * 3.LR(1)������
 *
 * ������Ҫ��Ϊ�˹���Ԥ������������㷨1
 * ���룺����ʽ productions.txt
 * �����Ԥ������� predict.txt
 * @author ltaoj
 *
 */
public class SyntacticAnalyzer {
	/**
	 * ���в���ʽ����
	 */
	private ArrayList<Production> mProductions;
	/**
	 * �����ս������
	 */
	private ArrayList<String> mTerminals;
	/**
	 * ���з��ս������
	 */
	private ArrayList<String> mNonTerminals;
	/**
	 * first��
	 */
	private HashMap<String, ArrayList<String>> mFirst;
	/**
	 * follow��
	 */
	private HashMap<String, ArrayList<String>> mFollow;
	/**
	 * Ԥ�������
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
		// ���ļ���ȡ����ʽ
		loadProductions();
		// �Ӳ���ʽ�õ����ս��
		getNonTerminals();
		// �Ӳ���ʽ�õ��ս��
		getTerminals();
		// �����First��
		computeFirst();
		// �����follow��
		computeFollow();
		// ������ʽ���Խ��ܵ������ַ�
		fillAccCharactor();
		// ����Ԥ��������
		generatePredict();

	}
	/**
	 * ���ļ���ȡ����ʽ
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
	 * ���Ѷ�ȡ�Ĳ���ʽ�еõ����ս��
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
	 * ���Ѷ�ȡ�Ĳ���ʽ�еõ��ս��
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
	 * ����first����
	 */
	private void computeFirst() {
		ArrayList<String> firsts;
		// ���Ƚ��ս���ŵ��Լ���first����
		for(int i = 0;i < mTerminals.size();i++){
			firsts = new ArrayList<>();
			firsts.add(mTerminals.get(i));
			mFirst.put(mTerminals.get(i), firsts);
		}
		// �����ս����ʼ��һ���յ�first��
		for(int i = 0;i < mNonTerminals.size();i++){
			firsts = new ArrayList<>();
			mFirst.put(mNonTerminals.get(i), firsts);
		}

		boolean flag;
		while(true){
			flag = true;// ѭ�������ı�־,ֻҪ�иı�ͽ�flagֵfalse
			String left;
			String right0;
			String[] rights;
			for(int i = 0;i < mProductions.size();i++){
				Production production = mProductions.get(i);
				left = production.getLeftSign();
				right0 = production.getRights()[0];
				rights = production.getRights();
				if(isTerminal((right0))||isNullChar(right0)){// ����ұߵ�һ�����ս��������뵽��߷��ս��first����
					if(!addToFirst(left, right0)) continue;
					flag = false;
				}else if(isNonTerminal(right0)){// ����ұߵ�һ���Ƿ��ս��
					for(int j = 0;j < mFirst.get(right0).size();j++){// ���ұߵ�һ�����ս��first���ϲ�����߷��ս��first����
						if(!addToFirst(left, mFirst.get(right0).get(j))) continue;
						flag = false;
					}

					for(int k = 0;k < rights.length;k++){// ѭ���õ��ұߴӵ�һ����ʼ������k��first���ж����п��ַ��ķ��ս��
						if(isNonTerminal(rights[k])&&mFirst.get(rights[k]).contains("~")){
							if(k == rights.length -1){
								if(!addToFirst(left, "~")) continue;
							}
							continue;
						}
						for(int m = 0;m < mFirst.get(rights[k]).size();m++){// ����k + 1�����ս����first���ӵ���߷��ս����first����
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
	 * ��item��ӵ�left��first����
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
	 * ����follow��
	 */
	private void computeFollow() {
		ArrayList<String> follows;
		// ��ʼ���������ŵ�follow����
		for(int i = 0;i < mNonTerminals.size();i++){
			follows = new ArrayList<>();
			mFollow.put(mNonTerminals.get(i), follows);
		}
		// ��#��ӵ��ķ���ʼ���ŵ�folow��
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
	 * ������ʽ���Խ��ܵ������ַ�
	 */
	private void fillAccCharactor() {
		boolean flag = true;
		String left;
		String[] rights;
		for(int i = 0;i < mProductions.size();i++){
			left = mProductions.get(i).getLeftSign();
			rights = mProductions.get(i).getRights();
			// ����ʽ�����Ҳ����ŵ�first�����ǿɽ��ܵ������ַ�
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
	 * ��������ӵ���i������ʽ�Ŀ������ַ�
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
	 * ����Ԥ�������
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
	 * �жϷ����Ƿ����ս��
	 * @param sign
	 * @return
	 */
	private boolean isTerminal(String sign){
		return mTerminals.contains(sign);
	}
	/**
	 * �жϷ����Ƿ��Ƿ��ս��
	 * @param sign
	 * @return
	 */
	private boolean isNonTerminal(String sign){
		return mNonTerminals.contains(sign);
	}
	/**
	 * �ж�һ���ַ��Ƿ�Ϊ���Ʒ�s
	 * @param sign
	 * @return
	 */
	private boolean isNullChar(String sign){
		return sign.equals("~");
	}
	/**
	 * ��Ԥ��������ļ��м���
	 * �����HashMap�У�key��String(left + "-" + symbol), value: String(rights)
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
	 * ����Դ�����Ƿ���ȷ
	 * @param inputStack ����ķ���ջ
	 */
	public void analyze(ArrayList<String> inputStack){
		loadTable();
		// ����ջ
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
				// ��ջ�������Ƴ����������ָ����һ��
				inferStack.remove(0);
				X = inferStack.get(0);
				System.out.println("2" + inferStack + "symbol" + inputStack.get(i));
				continue;
			}else if(isNonTerminal(X)){
				if(predictTable.containsKey(X + "-" + inputStack.get(i))){
					System.out.println("symbol:" +inputStack.get(i) +"���ò���ʽ:" + X + "->" + predictTable.get(X + "-" + inputStack.get(i)));
					// �õ�����ʽ���Ҳ�
					String[] rights = predictTable.get(X + "-" + inputStack.get(i)).trim().split(" ");
					// ��X�Ƴ�
					inferStack.remove(0);
					X = inferStack.get(0);
					// ������ʽ�Ҳ�����ѹջ
					for(int j = rights.length - 1;j >= 0;j--){
						if(rights[j].equals("~"))
							continue;
						inferStack.add(0, rights[j]);
					}
					System.out.println("3" + inferStack);
					i--;
				}else{
					System.out.println("4" + "������" + X + "-" + inputStack.get(i));
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
