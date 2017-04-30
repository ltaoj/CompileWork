package org.ltj.lexical;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class TResult {

	private StringProperty word;
	private StringProperty type;

	public TResult(String word, String type){
		this.word = new SimpleStringProperty(word);
		this.type = new SimpleStringProperty(type);
	}

	public StringProperty getWord() {
		return word;
	}

	public void setWord(StringProperty word) {
		this.word = word;
	}

	public StringProperty getType() {
		return type;
	}

	public void setType(StringProperty type) {
		this.type = type;
	}

	public StringProperty wordProperty(){
		if(word == null){
			word = new SimpleStringProperty(this,"word");
		}
		return word;
	}
	public StringProperty typeProperty(){
		if(type == null){
			type = new SimpleStringProperty(this, "type");
		}
		return type;
	}

}
