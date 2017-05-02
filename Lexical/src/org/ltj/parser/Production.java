package org.ltj.parser;

import java.util.ArrayList;

/**
 * �������ʽ�����ݽṹ
 * @author ltaoj
 *
 */
public class Production {
	/**
	 * �󲿷���
	 */
	private String leftSign;
	/**
	 * �Ҳ�
	 */
	private String[] rights;
	/**
	 * �ɽ��ܵĵ������ַ�
	 */
	private ArrayList<String> mAccCharacters;
	/**
	 * ���캯��
	 * @param leftSign ����ʽ�󲿷���
	 * @param rights ����ʽ�Ҳ�
	 */
	public Production(String leftSign, String[] rights){
		this.leftSign = leftSign;
		this.rights = rights;
		mAccCharacters = new ArrayList<>();
	}
	/**
	 * �����󲿷���
	 * @return
	 */
	public String getLeftSign() {
		return leftSign;
	}
	/**
	 * ���ز���ʽ�Ҳ�
	 * @return
	 */
	public String[] getRights() {
		return rights;
	}
	/**
	 * ���ز���ʽ�ɽ��ܵ�������ַ�����
	 * @return
	 */
	public ArrayList<String> getmAccCharacters() {
		return mAccCharacters;
	}


}
