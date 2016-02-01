
package kr.co.moa.keyword;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
	private static double W_TITLE = 0.5;
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
						
			int eventCnt = DBManager.getInstnace().getParsedEventsCnt(ed.userid, ed.url);
			if(eventCnt < 10) eventCnt = 10;
			System.out.println("evecnt: "+eventCnt);
			Map<String, Double> Tf_Event = cal_TF_Event(epd.keywordList,eventCnt);
			if(Tf_map == null){
				System.out.println("There is no TFList at url :"+ed.url);
				return;
			}
			DBManager.getInstnace().updateEventCalData(Tf_Event, ed.url, ed.userid);
			Map<String, Double> idfList = DBManager.getInstnace().getIDFList(Tf_map);
						
			//event 가중치 계산.
								
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
			
			int count = 10;
//			System.out.println("key\t count\t");
//			for(String key : Tf_map.keySet()){
//				if(count-->0)
//					System.out.println(key + "\t " + Tf_map.get(key));
//				else break;
//			}
		
			DBManager.getInstnace().updateTF_IDFByEvent(ed.url, ed.userid, Tf_map);
			//updateAll();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	public void updateAll() throws Exception{
		Map garbage = new HashMap<String,Double>();
		Map<String,Double> idfList = DBManager.getInstnace().getIDFList(garbage);
		Set<HtmlData> set = DBManager.getInstnace().updateAll();
		
		Iterator<HtmlData> it = set.iterator();
		
		while(it.hasNext()){
			calTF_IDF(it.next());
		}
			
	}
	public void calTF_IDF(HtmlData hd){
			HtmlParsedData hpd = MorphemeAnalyzer.getInstance().parsingHTML(hd);
				
			try {
			// 이미 방문했던 사이트인진 체크			
				Map<String, Double> idfList = cal_IDF(hpd.keywordList, hpd.snippet.url, hd.userid);		 	
				if(idfList == null){
					System.out.println("idfList null");
				}
	//			for(String key : idfList.keySet()){
	//				System.out.println("key: "+key+" val:"+idfList.get(key));
	//			}
				//본문 가중치 계산
				int totalCnt = getTotalCnt(hpd.keywordList);
				Map<String, Double> Tf_Body = cal_TF(hpd.keywordList,totalCnt);			
				//title 가중치 계산
				Map<String, Double> Tf_Title = cal_TF_Title(MorphemeAnalyzer.getInstance().doMecabTitleProcess(hpd.snippet.title, hd.userid,hd.url),totalCnt);	
				
				Map<String, Double> TF_IDF_list = new HashMap<String, Double>();
				
				if(hd.url.contains("stackoverflow.com")){
					W_TITLE = 0.7;
					System.out.println("stack");
				}
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
				if(hd.url.contains("stackoverflow.com"))
					W_TITLE = 0.5;
				
				Map<String,Double> Tf_Event = DBManager.getInstnace().getEventCollect(hd.url, hd.userid);
				if(Tf_Event != null){					
					for(String key : Tf_Event.keySet()){
						Double idf;
						Double tf = Tf_Event.get(key)*W_EVNET;
						if((idf =idfList.get(key)) == null){
							idf = 0.0;
						}
						
						if(TF_IDF_list.containsKey(key)){
							System.out.println(key+" orgin key :"+TF_IDF_list.get(key)+" eKey"+Tf_Event.get(key));
							TF_IDF_list.replace(key, TF_IDF_list.get(key)+tf*idf);
						}else{
							TF_IDF_list.put(key, tf*idf);
						}						
					}
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
	public void calTest(HtmlData hd){
		HtmlParsedData hpd = MorphemeAnalyzer.getInstance().parsingRawHTML(hd);
			
		try {
		// 이미 방문했던 사이트인진 체크			
			Map<String, Double> idfList = test_cal_IDF(hpd.keywordList, hd.url, hd.userid);		 	
			if(idfList == null){
				System.out.println("idfList null");
			}
			int totalCnt = getTotalCnt(hpd.keywordList);
			Map<String, Double> Tf_Body = cal_TF(hpd.keywordList,totalCnt);			
			
			Map<String, Double> TF_IDF_list = new HashMap<String, Double>();
			
			for(String key : Tf_Body.keySet()){
				Double tf = Tf_Body.get(key);					
				Double idf;
				if((idf =idfList.get(key)) == null){
					idf = 0.0;
				}
				TF_IDF_list.put(key, tf*idf);
			}
												
			TF_IDF tfid = new TF_IDF();

			tfid.userid = hd.userid;
			tfid.keywordList = MapUtil.Map_sortByValue(TF_IDF_list);
						
			int count = 10;
			System.out.println("key\t count\t");
			for(String key : tfid.keywordList.keySet()){
				if(count-->0)
					System.out.println(key + "\t " + tfid.keywordList.get(key));
				else break;
			}
					
			}catch (Exception e) {
				e.printStackTrace();
			}
	

}
	
	private int getTotalCnt(Map<String, Integer> keywordList) {
		
		int totalSize = 0;
		for(String key: keywordList.keySet()){			
			totalSize += keywordList.get(key);
		}
		return totalSize;
	}

	private Map cal_TF(Map<String,Integer> wordsByMecab,int totalSize) throws Exception{
		Map<String,Double> Tf = new HashMap<String, Double>();	
				
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
	private Map cal_TF_Title(Map<String,Integer> titleMap ,int totalSize) throws Exception{
		Map<String,Double> Tf = new HashMap<String, Double>();	
		
		int titleSize=0;
		for(String key : titleMap.keySet()){
			titleSize += titleMap.get(key);
		}
		titleSize *= 5;
		if(totalSize == 0 || totalSize < titleSize){	//content(본문 내용)이 없는 경우
			totalSize = titleSize;
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
	private Map cal_TF_Event(Map<String,Integer> wordsByMecab, int totalSize) throws Exception{
		Map<String,Double> Tf = new HashMap<String, Double>();
					
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
	private Map cal_IDF(Map<String,Integer> keywordList, String url,String userid) throws Exception{		
		if(!DBManager.getInstnace().isKeywordDocExist(url, userid))
			DBManager.getInstnace().updateIDFData(keywordList);
		Map<String, Double> idfList = DBManager.getInstnace().getIDFList(keywordList);
		return idfList;
	}
	private Map test_cal_IDF(Map<String,Integer> keywordList, String url,String userid) throws Exception{		
		DBManager.getInstnace().updateIDFData(keywordList);
		Map<String, Double> idfList = DBManager.getInstnace().getIDFList(keywordList);
		return idfList;
	}
	
	
}
