package org.ltj.parser;
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
	 * ���캯��
	 * @param leftSign ����ʽ�󲿷���
	 * @param rights ����ʽ�Ҳ�
	 */
	public Production(String leftSign, String[] rights){
		this.leftSign = leftSign;
		this.rights = rights;
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


}
