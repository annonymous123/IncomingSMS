package org.raxa.module.handlesms;

public enum Language {
	ENGLISH("English","en"),
	HINDI("Hindi","hi");
	
	private final String translateNotation;
	private final String langaugeName;
	
	Language(String language,String notation){
		langaugeName=language;
		translateNotation=notation;
	}
	
	public String getLanguage(){
		return langaugeName;
	}
	
	public String getNotation(){
		return translateNotation;
	}

}
