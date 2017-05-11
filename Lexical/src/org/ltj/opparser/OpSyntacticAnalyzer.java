package org.ltj.opparser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;

import javax.annotation.Generated;

import org.ltj.parser.Production;

/**
 * 语法分析器
 * 分析C++源程序语法
 * 分析语法通过3种算法实现
 * 1.预测分析法
 * 2.算符优先法
 * 3.LR(1)分析法
 *
 * 该类主要是为了构造预测分析表，采用算法2
 * 算法的核心是优先表的构造
 * 为了构造优先表，需要根据产生式将FIRSTVT和LASTVT求出
 * 之后根据算法构造优先表
 * 有了优先表就可以分析源程序
 * @author ltaoj
 *
 */
public class OpSyntacticAnalyzer {
	/**
	 * 产生式集合
	 */
	private ArrayList<Production> mProductions;
	/**
	 * 终结符
	 */
	private ArrayList<String> mTerminals;
	/**
	 * 非终结符
	 */
	private ArrayList<String> mNonTerminals;
	/**
	 * FIRSTVT集
	 */
	private HashMap<String, ArrayList<String>> mFirstvt;
	/**
	 * LASTVT集
	 */
	private HashMap<String, ArrayList<String>> mLastvt;
	/**
	 * 优先表
	 */
	private ArrayList<String> priorityTable;

	private String inputFileName = "production";
	private String outputFileName = "priority";

	public OpSyntacticAnalyzer(){
		mProductions = new ArrayList<>();
		mTerminals = new ArrayList<>();
		mNonTerminals = new ArrayList<>();
		mFirstvt = new HashMap<>();
		mLastvt = new HashMap<>();
		priorityTable = new ArrayList<>();
		// 从文件读取产生式
		loadProductions();
		// 从产生式得到非终结符
		getNonTerminals();
		// 从产生式得到终结符
		getTerminals();
		// 计算出First集
		computeFirstvt();
		// 计算出follow集
		computeFollowvt();
		// 产生预测分析结果
		generatedPriority();
	}


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

	private void getNonTerminals() {
		for(int i = 0;i < mProductions.size();i++){
			if (mNonTerminals.contains(mProductions.get(i).getLeftSign())) {
				continue;
			}
			mNonTerminals.add(mProductions.get(i).getLeftSign());
		}
	}

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
	 * 计算firstvt
	 */
	private void computeFirstvt() {
		// 初始化firstvt
		String left;
		String[] rights;
		for(int i = 0;i < mNonTerminals.size();i++){
			mFirstvt.put(mNonTerminals.get(i), new ArrayList<String>());
		}

		for(int i = 0;i < mProductions.size();i++){
			left = mProductions.get(i).getLeftSign();
			rights = mProductions.get(i).getRights();
			if(mTerminals.contains(rights[0])){
				addToFirstvt(left, rights[0]);
			}else if(rights.length > 1 && mTerminals.contains(rights[1])){
				addToFirstvt(left, rights[1]);
			}

		}
		boolean flag;
		while(true){
			flag = true;
			for(int i = 0;i < mProductions.size();i++){
				left = mProductions.get(i).getLeftSign();
				rights = mProductions.get(i).getRights();
				if(mNonTerminals.contains(rights[0])){
					for(int j = 0;j < mFirstvt.get(rights[0]).size();j++){
						if(!addToFirstvt(left, mFirstvt.get(rights[0]).get(j)))
							continue;
						flag = false;
					}
				}
			}
			if(flag){
				break;
			}
		}

	}
	/**
	 * 将终结符a加到非终结符p的firstvt
	 * @param p
	 * @param a
	 * @return
	 */
	private boolean addToFirstvt(String p, String a){
		if(mFirstvt.get(p).contains(a)){
			return false;
		}
		mFirstvt.get(p).add(a);
		return true;
	}
	/**
	 * 计算lastvt
	 */
	private void computeFollowvt() {
		String left;
		String[] rights;
		for(int i = 0;i < mNonTerminals.size();i++){
			mLastvt.put(mNonTerminals.get(i), new ArrayList<String>());
		}
		for(int i = 0;i < mProductions.size();i++){
			left = mProductions.get(i).getLeftSign();
			rights = mProductions.get(i).getRights();
			if(mTerminals.contains(rights[rights.length - 1])){
				addToLast(left, rights[rights.length - 1]);
			}else if(rights.length > 1 && mTerminals.contains(rights[rights.length - 2])){
				addToLast(left, rights[rights.length - 2]);
			}
		}
		boolean flag;
		while(true){
			flag = true;
			for(int i = 0;i < mProductions.size();i++){
				left = mProductions.get(i).getLeftSign();
				rights = mProductions.get(i).getRights();
				if(mNonTerminals.contains(rights[rights.length - 1])){
					for(int j = 0;j < mLastvt.get(rights[rights.length - 1]).size();j++){
						if(!addToLast(left, mLastvt.get(rights[rights.length - 1]).get(j)))
							continue;
						flag = false;
					}
				}
			}
			if(flag)
				break;
		}
	}
	/**
	 * 将终结符a加到非终结符p的lastvt中
	 * @param p
	 * @param a
	 * @return
	 */
	private boolean addToLast(String p, String a){
		if(mLastvt.get(p).contains(a)){
			return false;
		}
		mLastvt.get(p).add(a);
		return true;
	}
	private void generatedPriority() {

		try {
			File file = new File(outputFileName);
			if(!file.exists()){
				file.createNewFile();
			}
			RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
			String left;
			String[] rights;
			String line;
			for(int i = 0;i < mProductions.size();i++){
				left = mProductions.get(i).getLeftSign();
				rights = mProductions.get(i).getRights();
				if(rights.length > 1){
					for(int j = 0;j < rights.length - 1;j++){
						if(mTerminals.contains(rights[j]) && mTerminals.contains(rights[j+1])){
							line = rights[j] + " =.= " + rights[j + 1];
							if(!priorityTable.contains(line)){
								priorityTable.add(line);
								randomAccessFile.writeBytes(line + "\n");
							}
						}else if(j < rights.length - 2 && mTerminals.contains(rights[j]) && mTerminals.contains(rights[j + 2])){
							line = rights[j] + " =.= " + rights[j + 2];
							if(!priorityTable.contains(line)){
								priorityTable.add(line);
								randomAccessFile.writeBytes(line + "\n");
							}
						}else if(mTerminals.contains(rights[j]) && mNonTerminals.contains(rights[j + 1])){
							for(int k = 0;k < mFirstvt.get(rights[j + 1]).size();k++){
								line = rights[j] + " <. " + mFirstvt.get(rights[j + 1]).get(k);
								if(!priorityTable.contains(line)){
									priorityTable.add(line);
									randomAccessFile.writeBytes(line + "\n");
								}
							}
						}else if(mNonTerminals.contains(rights[j]) && mTerminals.contains(rights[j + 1])){
							for(int k = 0;k < mLastvt.get(rights[j]).size();k++){
								line = mLastvt.get(rights[j]).get(k) + " .> " + rights[j + 1];
								if(!priorityTable.contains(line)){
									priorityTable.add(line);
									randomAccessFile.writeBytes(line + "\n");
								}
							}
						}
					}
				}
			}
			randomAccessFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void printFirstvt(){
		for(String key : mFirstvt.keySet()){
			System.out.println("key:" + key + " value:" + mFirstvt.get(key));
		}

	}

	public void printLastvt(){
		for(String key : mLastvt.keySet()){
			System.out.println("key:" + key + " value:" + mLastvt.get(key));
		}
	}
	public static void main(String[] args){
		OpSyntacticAnalyzer opAnalyzer = new OpSyntacticAnalyzer();
		System.out.println(opAnalyzer.mTerminals.size());
		opAnalyzer.printFirstvt();
		System.out.println("*****************************");
		opAnalyzer.printLastvt();
	}
}
