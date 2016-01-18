
package kr.co.moa.keyword;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.google.gson.Gson;

import kr.co.MapUtil;
import kr.co.data.HtmlData;
import kr.co.data.IDf;
import kr.co.data.TF_IDF;
import kr.co.data.parsed.HtmlParsedData;
import kr.co.moa.DBManager;
import kr.co.moa.keyword.anlyzer.morpheme.MorphemeAnalyzer;

public class KeywordManager {
	
	private static final double W_BODY = 0.5;
	private static final double W_TITLE = 0.3;
	private static final double W_EVNET = 0.2;
	private static final int MAX_KEYWORDS = 50;
	
	private static KeywordManager instance;
	public static KeywordManager getInstance(){
		if(instance == null){
			instance = new KeywordManager();
		}
		return instance;
	}
	
	private KeywordManager(){
		
	}
	
	public void parsingHTML(String html){
		
	}
	
	public void calTF_IDF(HtmlData hd){
		HtmlParsedData hpd = MorphemeAnalyzer.getInstance().parsingHTML(hd);
		
		try {
			// 이미 방문했던 사이트인진 체크
			
			
			Map<String, Double> idfList = cal_IDF(hpd.keywordList);
		 
//			System.out.println("idfList size"+idfList.size());
//			for(String key : idfList.keySet()){
//				System.out.println(key+" "+idfList.get(key));
//			}
			//본문 가중치 계산
			Map<String, Double> Tf_Body = cal_TF(hpd.keywordList);			
			//title 가중치 계산
			Map<String, Double> Tf_Title = cal_TF(MorphemeAnalyzer.getInstance().doMecabProcess(hpd.snippet.title, "html"));	
			//event 가중치 계산.
			Map<String, Double> Tf_Event = cal_TF_Event(DBManager.getInstnace().getParsedEvents(hd.userid, hd.url));
			//Map<String, Double> Tf_Event = new HashMap<String, Double>();
			
			Map<String, Double> TF_IDF_list = new HashMap<String, Double>();
			
			for(String key : Tf_Body.keySet()){
				Double tf = Tf_Body.get(key)*W_BODY;
				if(Tf_Title.size() !=0 && Tf_Title.containsKey(key)){
					tf += Tf_Title.get(key)*W_TITLE;
				}
				if(Tf_Event.size()!=0 && Tf_Event.containsKey(key)){
					tf += Tf_Event.get(key)*W_EVNET;
				}
				Double idf;
				if((idf =idfList.get(key)) == null){
					idf = 0.0;
				}
				//System.out.println("tf :"+tf+" idf: "+idf);
				TF_IDF_list.put(key, tf*idf);
			}
			TF_IDF tfid = new TF_IDF();
			tfid.snippet = hpd.snippet;
			tfid.userid = hd.userid;
			tfid.keywordList = MapUtil.Map_sortByValue(TF_IDF_list);
						
			int count = 10;
			System.out.println("key\t count\t");
			for(String key : tfid.keywordList.keySet()){
				System.out.println(key + "\t " + tfid.keywordList.get(key));
			}
					
			DBManager.getInstnace().insertData("KeywordCollection", new Gson().toJson(tfid));
//			}
		}catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private Map cal_TF(Map<String,Integer> wordsByMecab) throws Exception{
		Map<String,Double> Tf = new HashMap<String, Double>();
		
		int cnt = 0;
		for(Map.Entry<String, Integer> me : wordsByMecab.entrySet()){
			if(cnt++<MAX_KEYWORDS && wordsByMecab.size()>0){
				Tf.put(me.getKey(), ((double)me.getValue())/wordsByMecab.size());
			}else
				break;
		}
//		System.out.println("Tf size: "+Tf.size());
//		if(Tf.size() !=0){
//			for(String key : Tf.keySet()){
//				System.out.println(key+"\t"+Tf.get(key));
//			}	
//		}
		
		return Tf;
		
	}
	private Map cal_TF_Event(Map<String,Double> wordsByMecab) throws Exception{
		Map<String,Double> Tf = new HashMap<String, Double>();
		
		int cnt = 0;
		for(Map.Entry<String, Double> me : wordsByMecab.entrySet()){
			if(cnt++<MAX_KEYWORDS && wordsByMecab.size()>0){
				Tf.put(me.getKey(), ((double)me.getValue())/wordsByMecab.size());
			}else
				break;
		}
//		System.out.println("Tf size: "+Tf.size());
//		if(Tf.size() !=0){
//			for(String key : Tf.keySet()){
//				System.out.println(key+"\t"+Tf.get(key));
//			}	
//		}
		return Tf;
		
	}
	private Map cal_IDF(Map<String,Integer> keywordList) throws Exception{
		/*
		 *  DB 연산을 최소화 하는 알고리즘 개선 필요.
		 *  
		 *  MongoDB는 wirte 연산이 우선. read 연산 중에도  write 연산이 들어오면 block;
		 *  해결 필요.
		 */
		DBManager.getInstnace().makeData_IDF();
		Map<String, Double> idfList = DBManager.getInstnace().getIDFList(keywordList);
		return idfList;
	}
	
	
}
