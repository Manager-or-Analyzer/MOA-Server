
package kr.co.moa.keyword;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;

import kr.co.MapUtil;
import kr.co.data.TF_IDF;
import kr.co.data.origin.EventData;
import kr.co.data.origin.HtmlData;
import kr.co.data.parsed.EventParsedData;
import kr.co.data.parsed.HtmlParsedData;
import kr.co.moa.DBManager;
import kr.co.moa.keyword.anlyzer.morpheme.MorphemeAnalyzer;

public class KeywordManager {
	
	private static final double W_BODY = 0.5;
	private static final double W_TITLE = 0.5;
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
		//timer로 쓰레드 시작.
	}
	
	public void applyEvent(EventData ed){
		EventParsedData epd = MorphemeAnalyzer.getInstance().parsingEvent(ed,ed.userid,ed.url);
//		HtmlParsedData hpd = DBManager.getInstnace().getHtmlParsedData(ed.url);
//		if(hpd == null){
//			System.out.println("There is no HtmlParsedData at url :"+ed.url);
//			return;
//		}
		try {			
			Map<String, Double> Tf_map = DBManager.getInstnace().getTfCollection(ed.userid, ed.url);
			Map<String, Double> tmp = DBManager.getInstnace().getParsedEvents(ed.userid, ed.url);
			Map<String, Double> Tf_Event = cal_TF_Event(epd.keywordList,tmp);
			if(Tf_map == null){
				System.out.println("There is no TFList at url :"+ed.url);
				return;
			}
			Map<String, Double> idfList = DBManager.getInstnace().getIDFList(Tf_map);
						
			//event 가중치 계산.
			
//			for(String key : Tf_map.keySet()){
//				Double tf = Tf_map.get(key)*W_BODY;			
//				if(Tf_Event.size() !=0 && Tf_Event.containsKey(key)){
//					
//					tf += Tf_Event.get(key)*W_EVNET;
//					Tf_Event.remove(key);
//				}
//				Double idf;
//				if((idf =idfList.get(key)) == null){
//					idf = 0.0;
//				}				
//				Tf_map.replace(key, tf*idf );
//				
//			}
			for(String key : Tf_Event.keySet()){
				Double tf = Tf_Event.get(key)*W_EVNET;
				Double idf;
				if((idf =idfList.get(key)) == null){
					idf = 0.0;
				}
				if(Tf_map.containsKey(key)){
					Tf_map.replace(key, Tf_map.get(key)+tf*idf);
				}else{
					Tf_map.put(key, tf*idf);
				}
			}

			Tf_map = MapUtil.Map_sortByValue(Tf_map);
			System.out.println("start");
			
//			for(String key: Tf_map.keySet()){
//				System.out.println("key : "+key+" val:"+Tf_map.get(key));
//			}
			
			DBManager.getInstnace().updateTF_IDFByEvent(ed.url, ed.userid, Tf_map);
			//updateAll();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	private void updateAll() throws Exception{
		Map garbage = new HashMap<String,Double>();
		Map<String,Double> idfList = DBManager.getInstnace().getIDFList(garbage);
		List<HtmlParsedData> list = DBManager.getInstnace().updateAll();
		for(int i=0; i<list.size(); i++){
			String url = list.get(i).snippet.url;
			Map<String,Boolean> map = list.get(i).userList;
			
			for(String key1: map.keySet()){
				String userid = key1.replace("\uff0E", ".");
						Map<String, Double> Tf_Body = cal_TF(list.get(i).keywordList);			
						//title 가중치 계산
						Map<String, Double> Tf_Title = cal_TF_Title(MorphemeAnalyzer.getInstance().doMecabTitleProcess(list.get(i).snippet.title,userid,url),list.get(i).keywordList);	
						
						
						Map<String, Double> TF_IDF_list = new HashMap<String, Double>();
						
						for(String key : Tf_Body.keySet()){
							Double tf = Tf_Body.get(key)*W_BODY;
							if(Tf_Title.size() !=0 && Tf_Title.containsKey(key)){
								
								tf += Tf_Title.get(key)*W_TITLE;
								Tf_Title.remove(key);
							}

							Double idf;
							if((idf =idfList.get(key)) == null){
								idf = 0.0;
							}
							//System.out.println("tf :"+tf+" idf: "+idf);
							TF_IDF_list.put(key, tf*idf);
						}
						for(String key : Tf_Title.keySet()){
							Double tf = Tf_Title.get(key)*W_TITLE;
							Double idf;
							if((idf =idfList.get(key)) == null){
								idf = 0.0;
							}
							TF_IDF_list.put(key, tf*idf);
						}								
						TF_IDF tfid = new TF_IDF();

						tfid.snippet = list.get(i).snippet;
						tfid.userid = userid;
						tfid.keywordList = MapUtil.Map_sortByValue(TF_IDF_list);
						DBManager.getInstnace().updateTF_IDFByEvent(tfid);
			}			
		}				
	}
	public void calTF_IDF(HtmlData hd){
		HtmlParsedData hpd = MorphemeAnalyzer.getInstance().parsingHTML(hd);
		
		try {
			// 이미 방문했던 사이트인진 체크			
			Map<String, Double> idfList = cal_IDF(hpd.keywordList);		 	
//			for(String key : idfList.keySet()){
//				System.out.println("key: "+key+" val:"+idfList.get(key));
//			}
			//본문 가중치 계산
			Map<String, Double> Tf_Body = cal_TF(hpd.keywordList);			
			//title 가중치 계산
			Map<String, Double> Tf_Title = cal_TF_Title(MorphemeAnalyzer.getInstance().doMecabTitleProcess(hpd.snippet.title, hd.userid,hd.url),hpd.keywordList);	
			
			
			Map<String, Double> TF_IDF_list = new HashMap<String, Double>();
			
			for(String key : Tf_Body.keySet()){
				Double tf = Tf_Body.get(key)*W_BODY;
				if(Tf_Title.size() !=0 && Tf_Title.containsKey(key)){
					
					tf += Tf_Title.get(key)*W_TITLE;
					Tf_Title.remove(key);
				}

				Double idf;
				if((idf =idfList.get(key)) == null){
					idf = 0.0;
				}
				//System.out.println("tf :"+tf+" idf: "+idf+" "+key+" "+tf*idf);
				TF_IDF_list.put(key, tf*idf);
			}
			for(String key : Tf_Title.keySet()){
				Double tf = Tf_Title.get(key)*W_TITLE;
				Double idf;
				if((idf =idfList.get(key)) == null){
					idf = 0.0;
				}
				TF_IDF_list.put(key, tf*idf);
				//System.out.println("title "+"tf :"+tf+" idf: "+idf+" "+key+" "+tf*idf);
			}
			
			TF_IDF tfid = new TF_IDF();

			tfid.snippet = hpd.snippet;
			tfid.userid = hd.userid;
			tfid.keywordList = MapUtil.Map_sortByValue(TF_IDF_list);
						
			int count = 10;
			System.out.println("key\t count\t");
			for(String key : tfid.keywordList.keySet()){
				if(count-->0)
					System.out.println(key + "\t " + tfid.keywordList.get(key));
				else break;
			}
			DBManager.getInstnace().updateTF_IDFByEvent(tfid);
			
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private Map cal_TF(Map<String,Integer> wordsByMecab) throws Exception{
		Map<String,Double> Tf = new HashMap<String, Double>();	
		
		int totalSize = 0;
		for(String key: wordsByMecab.keySet()){			
			totalSize += wordsByMecab.get(key);
		}
				
		System.out.println("size :"+totalSize);
		int cnt = 0;
		for(Map.Entry<String, Integer> me : wordsByMecab.entrySet()){
			if(cnt++<MAX_KEYWORDS && wordsByMecab.size()>0 && totalSize >0){
				Tf.put(me.getKey(), ((double)me.getValue())/totalSize);
			}else
				break;
		}
		return Tf;
		
	}
	private Map cal_TF_Title(Map<String,Integer> titleMap ,Map<String,Integer> wordsByMecab) throws Exception{
		Map<String,Double> Tf = new HashMap<String, Double>();	
		
		int totalSize = 0;
		for(String key: wordsByMecab.keySet()){			
			totalSize += wordsByMecab.get(key);
		}
				
		System.out.println("size :"+totalSize);
		int cnt = 0;
		for(Map.Entry<String, Integer> me : titleMap.entrySet()){
			if(cnt++<MAX_KEYWORDS && titleMap.size()>0 && totalSize >0){
				Tf.put(me.getKey(), ((double)me.getValue())/totalSize);
			}else
				break;
		}
		return Tf;
		
	}
	private Map cal_TF_Event(Map<String,Integer> wordsByMecab, Map<String,Double> tmp) throws Exception{
		Map<String,Double> Tf = new HashMap<String, Double>();
		
		double totalSize = 0.0;
		for(String key: tmp.keySet()){
			totalSize += tmp.get(key);
		}
		totalSize *= 10;
		
		System.out.println("size :"+totalSize);
		int cnt = 0;
		for(Map.Entry<String, Integer> me : wordsByMecab.entrySet()){
			if(cnt++<MAX_KEYWORDS && wordsByMecab.size()>0 && totalSize >0){
				Tf.put(me.getKey(), ((double)me.getValue())/totalSize);
			}else
				break;
		}

		return Tf;
		
	}
	private Map cal_IDF(Map<String,Integer> keywordList) throws Exception{		
		DBManager.getInstnace().makeData_IDF();
		Map<String, Double> idfList = DBManager.getInstnace().getIDFList(keywordList);
		return idfList;
	}
	
	
}
