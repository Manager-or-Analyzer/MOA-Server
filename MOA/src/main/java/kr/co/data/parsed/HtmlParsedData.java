package kr.co.data.parsed;

import java.util.Map;

import kr.co.data.send.Snippet;

public class HtmlParsedData {
	public Snippet snippet;
	public String collectionName = "HtmlParsedData";
	public Map<String,Integer> keywordList;
	
	public HtmlParsedData(){
		snippet = new Snippet();
	}
		
}
