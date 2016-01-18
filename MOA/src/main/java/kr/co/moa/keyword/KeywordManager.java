
package kr.co.moa.keyword;

import java.util.Collection;
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
import kr.co.data.TF_IDF;
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
			Map<String, Double> Tf_Title = cal_TF(MorphemeAnalyzer.getInstance().doMecabProcess(hpd.title, "html"));	
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
			ValueComparator bvc = new ValueComparator(TF_IDF_list);
			TreeMap sorted_tfidf = new TreeMap(bvc);
			sorted_tfidf.putAll(TF_IDF_list);
			
			Collection<String>  keys 	= sorted_tfidf.keySet();
			Collection<Integer> values	= sorted_tfidf.values();
			Iterator key_iter = keys.iterator();
			Iterator val_iter = values.iterator();
//			int count = 10;
//			System.out.println("key\t count\t");
//			while(key_iter.hasNext()){//count-- > 0){
//				String ikey = (String)  key_iter.next();
//				double ival 	= (Double) val_iter.next();
//				System.out.println(ikey + "\t " + ival);
			TF_IDF tfid = new TF_IDF();
			tfid.snippet.url = hpd.url;
			tfid.userid = hpd.userid;
			tfid.keywordList = TF_IDF_list;
			DBManager.getInstnace().insertData("KeywordCollection", new Gson().toJson(tfid));
//			}
		}catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	class ValueComparator implements Comparator {
	    Map<String, Double> base;

	    public ValueComparator(Map<String, Double> tF_IDF_list) {
	        this.base = tF_IDF_list;
	    }

		@Override
		public int compare(Object o1, Object o2) {
			if (((Double)base.get(o1)).intValue() >= ((Double)base.get(o2)).intValue()) {
	            return -1;
	        } else {
	            return 1;
	        } // returning 0 would merge keys
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
		Set keyLists = keywordList.keySet();
		Map<String, Double> countingMap = new HashMap<String, Double>();
					
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
		
		//DB저장
		IDf idf = new IDf();
		idf.name = "idfCollection";
		idf.idfList = new HashMap<String, Double>(); 
		idf.idfList.putAll(countingMap);
		//DBManager.getInstnace().updateData_IDF(idf);
		return countingMap;
	}
	
	
}
