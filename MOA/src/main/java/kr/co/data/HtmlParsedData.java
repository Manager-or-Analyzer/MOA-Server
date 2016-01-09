package kr.co.data;

import java.util.Map;

public class HtmlParsedData {
	public String userid;
	public String url;
	public String time;
		
	public String title;
	public String imrsrc;
	public Map<String,String> keywordList;
	
	public HtmlParsedData(String id, String url, String time){
		this.userid = id;
		this.url = url;
		this.time = time;		
	}
}
