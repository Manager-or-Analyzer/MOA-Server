package kr.co.data.parsed;

import java.util.Map;

public class EventParsedData {
	public String userid;
	public String url;
	public String time;
	public boolean isUsed;
	
	public Map<String,Integer> keywordList;
	
	public EventParsedData(String id, String url, String time, Map words){
		userid = id;
		this.url = url;
		this.time = time;
		keywordList = words;
		isUsed = false;
	}
}
