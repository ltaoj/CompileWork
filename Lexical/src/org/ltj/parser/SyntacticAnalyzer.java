package org.ltj.parser;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * �﷨������
 * ����JavaԴ�����﷨
 * �����﷨ͨ��3���㷨ʵ��
 * 1.Ԥ�������
 * 2.������ȷ�
 * 3.LR(1)������
 *
 * ������Ҫ��Ϊ�˹���Ԥ�������
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
	private String inputFileName = "production";
	private String outputFileName;
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
	 * ����Ԥ�������
	 */
	private void generatePredict() {

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
