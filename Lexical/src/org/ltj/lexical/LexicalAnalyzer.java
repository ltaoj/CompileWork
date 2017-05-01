package org.ltj.lexical;

import javafx.collections.ObservableList;

/**
 * �ʷ�������
 * ����Java����Դ����
 * @author ltaoj
 *
 */
public class LexicalAnalyzer {
	/**
	 * ��ǰ��ȡ���ַ�
	 */
	private char ch = ' ';
	/**
	 * ��ǰ�к�
	 */
	private int rowNum = 1;
	/**
	 *
	 */
	private int pointer = 0;
	/**
	 * �����ֱ����
	 */
	private int code;
	/**
	 * ��ǰ����
	 */
	private String strToken = "";
	/**
	 * Ҫ������Դ����
	 */
	private String sourceStr = "";
	/**
	 * Դ����ĳ���
	 */
	private long sourceLength = 0;
	/**
	 * ����б�
	 */
	private ObservableList ob_result;
	/**
	 * �����б�
	 */
	private ObservableList ob_error;

	private static int $KEY_WORD = 1;// �ؼ���
	private static int $FLAG_CH = 2;// ��ʶ��
	private static int $UNSIGNED_INT = 3;// �޷��ų�������
	private static int $OPERATOR = 4;// �����
	private static int $SINGLE_SEPERATOR = 5;// ����ָ���
	private static int $DOUBLE_SEPERATOR = 6;// ˫��ָ���

	private static String[] KEY_WORDS = {"if", "for", "while", "do",
			"return","break","continue", "void","main","int","double","float",
			"char","boolean","long","String","if","else","class", "public",
			"private","protected","static"};// ������
	private static char[] OPERATORS = {'+','-','*','/','=','<','%'};// �����
	private static char[] SINGLE_SEPERATORS = {',',';','.'};// ����ָ���
	private static char[] DOUBLE_SEPERATORS = {'[',']','{','}','(',')','\'','\"'};// ˫��ָ���
	public void scanAll(){
		// ��ʼɨ��
		do{
			scanner();
		}while(pointer < sourceLength);
	}
	/**
	 * ɨ��
	 * @throws AnalyzeError
	 */
	private void scanner(){
		// ����strToken
		strToken = "";
		// ��������
		getChar();
		getBC();
		if(isLetter()){// �������Ǹ���ʶ��
			while(isLetter()||isDigit()){
				concat();// ����ǰ�ַ���ӵ�����ĩβ
				getChar();
			}
			retract();
			code = reserve();
			_return(strToken, code);

		}else if(isDigit()){// ������������
			while(isDigit()){
				concat();
				getChar();
			}
			retract();
			_return(strToken,$UNSIGNED_INT);
		}else if(ch == '!'||ch == '<'||ch == '>'){
			concat();
			getChar();
			getBC();
			if(ch == '='){
				concat();
				_return(strToken, $OPERATOR);
			}else{
				retract();
			}
		}else if(isSingleLetterOperator()){
			concat();
			_return(strToken, $OPERATOR);
		}else if(isSingleSeperator()){
			concat();
			_return(strToken,$SINGLE_SEPERATOR);
		}else if(isDoubleSeperator()){
			concat();
			_return(strToken, $DOUBLE_SEPERATOR);
		}else if(ch == '\n'){
			rowNum++;
			return;
		}else{
			try {
				throw new AnalyzeError("Errors strToken:"+strToken +" ch:"+ch + "�޷�ʶ��");
			} catch (AnalyzeError e) {
				ob_error.add(new TError(e.getErrorCode(), e.getMessage() , ""+rowNum));
			}
		}
	}
	/**
	 * �õ�һ���ַ�
	 * @return
	 */
	public void getChar(){
		if(pointer < sourceStr.length()){
			ch = sourceStr.charAt(pointer);
			pointer++;
		}
	}
	/**
	 * �����ո�
	 */
	public void getBC(){
		while(ch == ' '||ch == '\t'){
			getChar();
		}
	}
	/**
	 * ����ǰ�ַ��ӵ�����ĩβ
	 */
	public void concat(){
		StringBuilder sb = new StringBuilder(strToken);
		sb.append(ch);
		strToken = sb.toString();
	}
	/**
	 * �жϵ�ǰ�ַ��Ƿ�Ϊ��ĸ
	 * @return
	 */
	public boolean isLetter(){
		return Character.isLetter(ch);
	}
	/**
	 * �жϵ�ǰ�ַ��Ƿ�Ϊ����
	 * @return
	 */
	public boolean isDigit(){
		return Character.isDigit(ch);
	}
	/**
	 * �ж��Ƿ�Ϊ�����ַ��������
	 * @return
	 */
	private boolean isSingleLetterOperator(){
		for(int i = 0;i < OPERATORS.length;i++){
			if(ch == OPERATORS[i])
				return true;
		}
		return false;
	}
	/**
	 * �ж��Ƿ�Ϊ����ָ���
	 * @return
	 */
	private boolean isSingleSeperator(){
		for(int i = 0;i < SINGLE_SEPERATORS.length;i++){
			if(ch == SINGLE_SEPERATORS[i])
				return true;
		}
		return false;
	}
	/**
	 * �ж��Ƿ�Ϊ˫��ָ���
	 * @return
	 */
	private boolean isDoubleSeperator(){
		for(int i = 0;i < DOUBLE_SEPERATORS.length;i++){
			if(ch == DOUBLE_SEPERATORS[i]){
				return true;
			}
		}
		return false;
	}
	/**
	 * �������ص�һ��λ��
	 */
	public void retract(){
		ch = ' ';
		pointer--;
	}
	/**
	 * ����ַ����Ǳ����֣����ر����ֱ��룬���򷵻ر�ʶ������
	 * @return
	 */
	public int reserve(){
		for(int i = 0;i < KEY_WORDS.length;i++){
			if(strToken.equals(KEY_WORDS[i])){
				return $KEY_WORD;
			}
		}
		return $FLAG_CH;
	}
	/**
	 * ��ӡcode-value
	 * @param code
	 * @param value
	 */
	public void _return(String value, int code){
		ob_result.add(new TResult(""+code, value));
	}
//	/**
//	 * �ж��Ƿ�������µ�һ��
//	 * @return
//	 */
//	private boolean checkNewRow(){
//		if(ch == '\n'){
//			rowNum ++;
//			return true;
//		}
//		return false;
//	}

	class AnalyzeError extends Exception {
		private String errorCode;
//		public AnalyzeError
		public AnalyzeError(String arg0) {
			this(arg0, "E0000");

		}
		public AnalyzeError(String arg0, String errorCode){
			super(arg0);
			this.errorCode = errorCode;
		}
		public String getErrorCode() {
			return errorCode;
		}
	}
	public static void main(String[] args) {
//		StringBuffer sb = new StringBuffer();
//		Scanner input = new Scanner(System.in);
//		System.out.println("������Դ����,(�����б�־Ϊover):");
//		while(true){
//			String line = input.nextLine();
//			if(line.equals("over")) break;
//			sb.append(line);
//		}
//		System.out.println(sb.toString());
//		LexicalAnalyzer analyzer = new LexicalAnalyzer();
//		analyzer.startAnalyze(sb.toString());
	}
	public LexicalAnalyzer(String sourceStr, ObservableList ob_result, ObservableList ob_error) {
		this.sourceStr = sourceStr + "\0";
		this.ob_result = ob_result;
		this.ob_error = ob_error;
		sourceLength = sourceStr.length();
	}



}
