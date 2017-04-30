package org.ltj.lexical;

import javafx.collections.ObservableList;

/**
 * 词法分析器
 * 分析Java语言源程序
 * @author ltaoj
 *
 */
public class LexicalAnalyzer {
	/**
	 * 当前读取的字符
	 */
	private char ch = ' ';
	/**
	 * 当前行号
	 */
	private int rowNum = 1;
	/**
	 *
	 */
	private int pointer = 0;
	/**
	 * 单词种别编码
	 */
	private int code;
	/**
	 * 当前单词
	 */
	private String strToken = "";
	/**
	 * 要分析的源代码
	 */
	private String sourceStr = "";
	/**
	 * 源代码的长度
	 */
	private long sourceLength = 0;
	/**
	 * 结果列表
	 */
	private ObservableList ob_result;
	/**
	 * 错误列表
	 */
	private ObservableList ob_error;

	private static int $KEY_WORD = 1;// 关键字
	private static int $FLAG_CH = 2;// 标识符
	private static int $UNSIGNED_INT = 3;// 无符号常整形数
	private static int $OPERATOR = 4;// 运算符
	private static int $SINGLE_SEPERATOR = 5;// 单界分隔符
	private static int $DOUBLE_SEPERATOR = 6;// 双界分隔符

	private static String[] KEY_WORDS = {"if", "for", "while", "do",
			"return","break","continue", "void","main","int","double","float",
			"char","boolean","long","String","if","else","class", "public",
			"private","protected","static"};// 保留字
	private static char[] OPERATORS = {'+','-','*','/','=','<','%'};// 运算符
	private static char[] SINGLE_SEPERATORS = {',',';','.'};// 单界分隔符
	private static char[] DOUBLE_SEPERATORS = {'[',']','{','}','(',')','\'','\"'};// 双界分隔符
	public void scanAll(){
		// 开始扫描
		do{
			scanner();
		}while(pointer < sourceLength);
	}
	/**
	 * 扫描
	 * @throws AnalyzeError
	 */
	private void scanner(){
		// 重置strToken
		strToken = "";
		// 单个过程
		getChar();
		getBC();
		if(isLetter()){// 代表这是个标识符
			while(isLetter()||isDigit()){
				concat();// 将当前字符添加到单词末尾
				getChar();
			}
			retract();
			code = reserve();
			_return(strToken, code);

		}else if(isDigit()){// 代表这是数字
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
				throw new AnalyzeError("Errors strToken:"+strToken +" ch:"+ch + "无法识别");
			} catch (AnalyzeError e) {
				ob_error.add(new TError(e.getErrorCode(), e.getMessage() , ""+rowNum));
			}
		}
	}
	/**
	 * 得到一个字符
	 * @return
	 */
	public void getChar(){
		if(pointer < sourceStr.length()){
			ch = sourceStr.charAt(pointer);
			pointer++;
		}
	}
	/**
	 * 跳过空格
	 */
	public void getBC(){
		while(ch == ' '||ch == '\t'){
			getChar();
		}
	}
	/**
	 * 将当前字符加到单词末尾
	 */
	public void concat(){
		StringBuilder sb = new StringBuilder(strToken);
		sb.append(ch);
		strToken = sb.toString();
	}
	/**
	 * 判断当前字符是否为字母
	 * @return
	 */
	public boolean isLetter(){
		return Character.isLetter(ch);
	}
	/**
	 * 判断当前字符是否为数字
	 * @return
	 */
	public boolean isDigit(){
		return Character.isDigit(ch);
	}
	/**
	 * 判断是否为单个字符的运算符
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
	 * 判断是否为单界分隔符
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
	 * 判断是否为双界分隔符
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
	 * 搜索器回调一个位置
	 */
	public void retract(){
		ch = ' ';
		pointer--;
	}
	/**
	 * 如果字符串是保留字，返回保留字编码，否则返回标识符编码
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
	 * 打印code-value
	 * @param code
	 * @param value
	 */
	public void _return(String value, int code){
		ob_result.add(new TResult(""+code, value));
		System.out.println("("+code + "," + "\""+value+"\")");
	}
//	/**
//	 * 判断是否进入了新的一行
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
//		System.out.println("请输入源程序,(结束行标志为over):");
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
