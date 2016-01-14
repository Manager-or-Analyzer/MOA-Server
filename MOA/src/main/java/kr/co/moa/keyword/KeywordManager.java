
package kr.co.moa.keyword;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.google.gson.Gson;

import kr.co.data.HtmlData;
import kr.co.data.HtmlParsedData;
import kr.co.data.IDf;
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
		Map idfList;
		try {
			// 이미 방문했던 사이트인진 체크
			idfList = cal_IDF(hpd.keywordList);
		 
			//본문 가중치 계산
			Map<String, Double> Tf_Body = new HashMap<String, Double>();
			int cnt = 0;
						
			for(Map.Entry<String, Integer> me : hpd.keywordList.entrySet()){
				if(cnt++<MAX_KEYWORDS && hpd.keywordList.size() >0){
					Tf_Body.put(me.getKey(), (double)me.getValue()/hpd.keywordList.size());
				}else
					break;			
			}
			
//			System.out.println("Tf-Body size: "+Tf_Body.size());
//			for(String key : Tf_Body.keySet()){
//				System.out.println(key+"\t"+Tf_Body.get(key));
//			}			
			//title 가중치 계산
			Map<String, Double> Tf_Title = new HashMap<String, Double>();
			Map<String, Integer> mapByMecab = MorphemeAnalyzer.getInstance().doMecabProcess(hpd.title, "html");
			cnt = 0;
			
			for(Map.Entry<String, Integer> me : mapByMecab.entrySet()){
				if(cnt++<MAX_KEYWORDS && mapByMecab.size()>0){
					Tf_Title.put(me.getKey(), (double)me.getValue()/mapByMecab.size());
				}else
					break;			
			}
//			System.out.println("Tf-Title size: "+Tf_Title.size());
//			for(String key : Tf_Title.keySet()){
//				System.out.println(key+"\t"+Tf_Title.get(key));
//			}
			//event 가중치 계산.
			System.out.println("parse start");
			Map<String, Double> Tf_Event= new HashMap<String, Double>();
			Map<String, Integer> eventKeylist = DBManager.getInstnace().getParsedEvents(hd.userid, hd.url);
			cnt = 0;
			
			for(Map.Entry<String, Integer> me : eventKeylist.entrySet()){
				if(cnt++<MAX_KEYWORDS && eventKeylist.size()>0){
					Tf_Event.put(me.getKey(), (double)me.getValue()/eventKeylist.size());
				}else
					break;			
			}
			// null일때 처리
			System.out.println("Tf-Event size: "+eventKeylist.size());
			if(eventKeylist.size() !=0){
			for(String key : eventKeylist.keySet()){
				System.out.println(key+"\t"+eventKeylist.get(key));
			}
			}
			System.out.println("parse end");
		
		}catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private Map cal_IDF(Map<String,Integer> keywordList) throws Exception{
		/*
		 *  DB 연산을 최소화 하는 알고리즘 개선 필요.
		 *  
		 *  MongoDB는 wirte 연산이 우선. read 연산 중에도  write 연산이 들어오면 block;
		 *  해결 필요.
		 */
		Set keyLists = keywordList.keySet();
		Map<String, Double> countingMap = new HashMap<String, Double>();
		Map<String, Double> idfList = new TreeMap<String, Double>(new ValueComparator(countingMap));
					
		Iterator<String> it = keyLists.iterator();
		
		//DB 연산
		int totalDoc = DBManager.getInstnace().getDocSize();
		System.out.println("totalDoc :"+totalDoc);
		while(it.hasNext()){
			String key = it.next();
			
			//DB 연산
			int cnt = DBManager.getInstnace().getDocCnt(key);
			
			double val = java.lang.Math.log(totalDoc/(1+cnt));
			if(val <0){
				countingMap.put(key, (double) 0);
			}else{
				countingMap.put(key, val);
			}
			//DB연산
			
			
			
			//System.out.println("cnt ;"+cnt+" val:"+val);
										
		}
		idfList.putAll(countingMap);
		
		//DB저장
		IDf idf = new IDf();
		idf.name = "idfCollection";
		idf.idfList = countingMap;
		DBManager.getInstnace().updateData_IDF(idf);
		
		return idfList;
	}
	
	class ValueComparator implements Comparator {
	    Map<String, Double> base;

	    public ValueComparator(Map base) {
	        this.base = base;
	    }

		@Override
		public int compare(Object o1, Object o2) {
			if (base.get(o1) >= base.get(o2)) {
	            return -1;
	        } else {
	            return 1;
	        } // returning 0 would merge keys
		}
	}
}
