package org.ltj.lexical;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class TError {

	private StringProperty errorCode;
	private StringProperty describe;
	private StringProperty rowNum;



	public TError(String errorCode, String describe, String rowNum) {
		super();
		this.errorCode = new SimpleStringProperty(errorCode);
		this.describe = new SimpleStringProperty(describe);
		this.rowNum = new SimpleStringProperty(rowNum);
	}

	public StringProperty getErrorCode() {
		return errorCode;
	}
	public void setErrorCode(StringProperty errorCode) {
		this.errorCode = errorCode;
	}
	public StringProperty getDescribe() {
		return describe;
	}
	public void setDescribe(StringProperty describe) {
		this.describe = describe;
	}
	public StringProperty getRowNum() {
		return rowNum;
	}
	public void setRowNum(StringProperty rowNum) {
		this.rowNum = rowNum;
	}

	public StringProperty errorCodeProperty(){
		if(errorCode == null){
			errorCode = new SimpleStringProperty(this, "errorCode");
		}
		return errorCode;
	}

	public StringProperty describeProperty(){
		if(describe == null){
			describe = new SimpleStringProperty(this, "describe");
		}
		return describe;
	}

	public StringProperty rowNumProperty(){
		if(rowNum == null){
			rowNum = new SimpleStringProperty(this, "rowNum");
		}
		return rowNum;
	}

}
