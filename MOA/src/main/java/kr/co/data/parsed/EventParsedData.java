package kr.co.data.parsed;

import java.util.Map;

public class EventParsedData {
	public String userid;
	public String url;
	public String time;
	public int totalCnt;	
	public Map<String,Integer> keywordList;
	
	public EventParsedData(String id, String url, String time, Map<String,Integer> words){
		this.userid = id;
		this.url = url;
		this.time = time;
		this.totalCnt = 0;
		for(String key : words.keySet()){
			totalCnt += words.get(key);
		}
		keywordList = words;
	}
}
