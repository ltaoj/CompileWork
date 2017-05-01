package org.ltj.parser;
/**
 * 定义产生式的数据结构
 * @author ltaoj
 *
 */
public class Production {
	/**
	 * 左部符号
	 */
	private String leftSign;
	/**
	 * 右部
	 */
	private String[] rights;
	/**
	 * 构造函数
	 * @param leftSign 产生式左部符号
	 * @param rights 产生式右部
	 */
	public Production(String leftSign, String[] rights){
		this.leftSign = leftSign;
		this.rights = rights;
	}
	/**
	 * 返回左部符号
	 * @return
	 */
	public String getLeftSign() {
		return leftSign;
	}
	/**
	 * 返回产生式右部
	 * @return
	 */
	public String[] getRights() {
		return rights;
	}


}
