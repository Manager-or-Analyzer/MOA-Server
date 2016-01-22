package kr.co.moa.controller.analyzer;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import kr.co.MapUtil;
import kr.co.Util;
import kr.co.data.DomTimeData;
import kr.co.data.SearchData;
import kr.co.data.TF_IDF;
import kr.co.data.receive.DateData;
import kr.co.data.send.Snippet;
import kr.co.moa.DBManager;
import kr.co.moa.keyword.anlyzer.morpheme.MorphemeAnalyzer;

import com.google.gson.Gson;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;

/**
 * Servlet implementation class SearchingDocumentController
 */
///searching
public class SearchingDocumentController extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final int KEYWORD_NUM = 5;			//p		shrot doc : 5 / long doc : 10
	private static final int DIFF_NUM = 100;			//q 
	   
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SearchingDocumentController() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String userid;
		HashMap<String, TF_IDF> rawdata    = new HashMap<String, TF_IDF>();;
		HashMap<String, Double> similarDoc = new HashMap<String, Double>();
	    PrintWriter out = response.getWriter();	
		String str      = request.getParameter("data");		
		String str_date = request.getParameter("date");
		SearchData sd = new Gson().fromJson(str,      SearchData.class);
		DateData dd   = new Gson().fromJson(str_date, DateData.class  );
		System.out.println(str);
		System.out.println(str_date);
		
		if(sd.searches == null || sd.searches.equals("")){ 
			out.println("fail");
			return;
		}
		userid = sd.userid;
		String[] keywords = getKeywords(sd.searches);
		DBCursor[] cursor = new DBCursor[keywords.length];
		
		for(int i = 0; i < keywords.length; i++){
			try {
				cursor[i] = DBManager.getInstnace().getTargetDocuments(userid, keywords[i]);
			} catch (Exception e) {
				//DB exception
			}
			calcSimilarity(userid, rawdata, similarDoc, cursor[i], keywords[i]);
		}
		//sorting
		Date startDay = null; 	Date endDay = null;
		if(dd.start == null || dd.start.equals("")) startDay = new Date(0);
		else 										startDay = Util.strToDate(dd.start);
		if(dd.end == null   || dd.end.equals("")) 	endDay   = new Date();
		else 										endDay   = Util.strToDate(dd.end);
		

		reflectDuration(userid, startDay, endDay, similarDoc);
		//sorting
		similarDoc = (HashMap<String, Double>) MapUtil.Map_sortByValue(similarDoc);
		rawdata    = (HashMap<String, TF_IDF>) MapUtil.sortRawdataAsSimilarity(rawdata, similarDoc);
		//ArrayList<Snippet> sni_list = makeSniArray(rawdata, similarDoc);
		
		//System.out.println(similarDoc);
		//System.out.println(sni_list);		
		//System.out.println(new Gson().toJson(sni_list));
		
		String res = new Gson().toJson(rawdata);
		//out.print("snippet : " +new Gson().toJson(sni_list));
		//out.println("success");
		out.println(res);
	}
	
	
	private void reflectDuration(String userid, Date startDay, Date endDay, 
			HashMap<String, Double> similarDoc) {
		HashMap<String, Double> map = new HashMap<String, Double>();
		DBCursor cursor = DBManager.getInstnace().getDurationData(userid, similarDoc.keySet());
		while(cursor.hasNext()){
			//parsing
			BasicDBObject raw = (BasicDBObject) cursor.next();
			DomTimeData obj = new DomTimeData();
			obj.userid = (String) raw.getString("userid");
			obj.url = (String) raw.getString("url");
			obj.time = raw.getDate("time");
			obj.duration = Double.parseDouble(raw.getString("duration"));//(double) raw.getString("duration");
			
			
			if(startDay.compareTo(obj.time) > 0 && endDay.compareTo(obj.time) < 0) continue;		//기간 내 데이터만 고려 
			if(map.containsKey(obj.url)) map.put(obj.url, map.get(obj.url) + obj.duration);
			else 						 map.put(obj.url, obj.duration);
		}
		
		Iterator<String> iter = similarDoc.keySet().iterator();
		while(iter.hasNext()){
			String url = iter.next();
			if(!map.containsKey(url)) continue;
			similarDoc.put( url, (similarDoc.get(url) * map.get(url)) );
		}
	}

	@SuppressWarnings({ "unchecked" })
	private void calcSimilarity(String userid, HashMap<String, TF_IDF> rawdata, 
			HashMap<String, Double> similarDoc, DBCursor cursor, String searches) {
		if(!cursor.hasNext()){			//userid에 해당하는 일치하는 키워드 없음 
			System.out.println("No match data with keyword : " + searches + " / " + userid);
			return;
		}

		double maxWeight = 0;		
		String standard_url = null;		//기준문서 

		ArrayList<TF_IDF> arr = new ArrayList<TF_IDF>();

		System.out.println("searches" + searches);
		while(cursor.hasNext()){
			//parsing
			BasicDBObject obj = (BasicDBObject) cursor.next();
			BasicDBObject sni_obj = (BasicDBObject) obj.get("snippet");
			Snippet sni = new Snippet();
			TF_IDF temp = new TF_IDF();

			sni.title = sni_obj.getString("title");
			sni.url   = sni_obj.getString("url");
			sni.time  = sni_obj.getString("time");
			sni.img   = sni_obj.getString("img");
			temp.userid = (String) obj.get("userid");
			temp.snippet = sni;
			temp.keywordList = (Map<String, Double>) obj.get("keywordList");
			
			//sorting
	        //temp.keywordList = MapUtil.Map_sortByValue(temp.keywordList);

			//최대값 구함 
	        if(temp.keywordList.get(searches) > maxWeight){
	        	maxWeight = temp.keywordList.get(searches);

	        	standard_url = temp.snippet.url;
	        }
			rawdata.put(temp.snippet.url, temp);

			arr.add(temp);
		}
		System.out.println(standard_url + " / " + maxWeight);
		
		//3. url군집에 속하는 상위 keyword_num개의 키워드 가져옴
		HashMap<Integer, ArrayList<String>> reduced = new HashMap<Integer, ArrayList<String>>();
		int[] standard_hashcodes = new int[DIFF_NUM];
		String[] standard_keywords = null;
		
		for(TF_IDF entry : arr){
			String[] keyword = getFirstEntries(KEYWORD_NUM, entry.keywordList);
			int[] hashcodes = new int[KEYWORD_NUM];
			
			for(int i = 0; i < DIFF_NUM; i++){
				int minHashcode = 0x00010000;
				for(int k = 0; k < KEYWORD_NUM; k++){
					if(keyword[k] == null || keyword[k].equals(""))	break;
					hashcodes[k] = getHashCode(keyword[k], i);
					if(minHashcode > hashcodes[k]) minHashcode = hashcodes[k];
				}
				emit(reduced, minHashcode, entry.snippet.url);	//minhashcode를 key로하여 emit
				if(entry.snippet.url.equals(standard_url)) standard_hashcodes[i] = minHashcode;
			}
			if(entry.snippet.url.equals(standard_url))	   standard_keywords     = keyword;
		}
		System.out.println(reduced);
		
		Set<String> set = new HashSet<String>();			//standard_url과 같은 그룹에 있는 모든 url 모음 
		for(int i = 0; i < DIFF_NUM; i++)			
			set.addAll(reduced.get(standard_hashcodes[i]));
		
		ArrayList<String> clustedGroup = new ArrayList<String>(set);
		/*url-keyword 행렬 생성 
			행렬생성하지 않고, 코사인 유사도를 구하면서 한꺼번에 계산한
		*/
		cosineSimilarity(rawdata, similarDoc, clustedGroup, standard_url, standard_keywords);
		//System.out.println(similar_doc);
	}

	private void cosineSimilarity(
			HashMap<String, TF_IDF> rawdata, HashMap<String, Double> similarDoc, 
			ArrayList<String> clustedGroup, String standard_url, String[] standard_keywords) {
		/*
		 * 문서간의 유사도를 코사인 유사도를 이용하여 계산한 후
		 * 코사인유사도 값이 높은 문서 순으로 소팅하여 map에 넣어 반환해주는 함수 
		 */
		double standard_doc_div = 0;
		double[] standard_wight = new double[KEYWORD_NUM];
		for(int i = 0; i<KEYWORD_NUM; i++){
			if(standard_keywords[i] == null || standard_keywords[i].equals(""))	break;
			standard_wight[i] = rawdata.get(standard_url).keywordList.get(standard_keywords[i]);
			standard_doc_div += (standard_wight[i] * standard_wight[i]);
		}
		standard_doc_div = Math.sqrt(standard_doc_div);
		
		for(int i = 0; i<clustedGroup.size(); i++){
			String target_url = clustedGroup.get(i);
			TF_IDF data = rawdata.get(target_url);
			double target_doc_div = 0;
			double numerator = 0;
			
			for(int k = 0; k<KEYWORD_NUM; k++){
				if(data.keywordList.containsKey(standard_keywords[k])){
					double weight = data.keywordList.get(standard_keywords[k]);
					target_doc_div += (weight * weight);
					numerator 	   += (weight * standard_wight[k]);
					//System.out.print(weight + " ");
				}//else{
					//System.out.print("0 ");
				//}
			}
			//System.out.println();
			target_doc_div = Math.sqrt(target_doc_div);
			
			double cos_sim;
			if(numerator == 0) 	cos_sim = 0;
			else 				cos_sim = numerator/(standard_doc_div * target_doc_div);
			
			if(similarDoc.containsKey(target_url)){
				//target_url이 이미 존재한다면 두 키워드에 중첩되는 문서 이므로 가중치 값을 더한다 
				similarDoc.put(target_url, similarDoc.get(target_url) + cos_sim);
			}else{
				similarDoc.put(target_url, cos_sim);
			}
		}
	}
	
	@SuppressWarnings("rawtypes")
	private String[] getKeywords(String searches) {
		
		searches = searches.replace('+', ' ');
		Map temp = MorphemeAnalyzer.getInstance().doMecabProcess(searches, null);	
		Iterator iter = temp.keySet().iterator();
		String[] result = new String[temp.size()];
		int cnt = 0;

		//System.out.println("keywords~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
		while(iter.hasNext()){
			result[cnt++] = (String) iter.next();
			//System.out.println(result[cnt-1]);
		}
		
		return result;
	}
	
	private String[] getFirstEntries(int max, Map<String, Double> keywords) {
		int count = 0;
		String[] target = new String[max];
		for (Map.Entry<String,Double> entry:keywords.entrySet()) {
			if (count >= max) break;
		    target[count] = entry.getKey();
		    count++;
		}
		return target;
	}
	
	private void emit(HashMap<Integer, ArrayList<String>> map, int key, String url){
		if(map.containsKey(key)){
			map.get(key).add(url);
		}else{
			ArrayList<String> val = new ArrayList<String>();
			val.add(url);
			map.put(key, val);
		}
	}
	
	private int getHashCode(String s, int index){
		final int[] primes = {
				127,	131,	137,	139,	149,	151,	157,	163,	167,	173,
				179,	181,	191,	193,	197,	199,	211,	223,	227,	229,
				233,	239,	241,	251,	257,	263,	269,	271,	277,	281,
				283,	293,	307,	311,	313,	317,	331,	337,	347,	349,
				353,	359,	367,	373,	379,	383,	389,	397,	401,	409,
				419,	421,	431,	433,	439,	443,	449,	457,	461,	463,
				467,	479,	487,	491,	499,	503,	509,	521,	523,	541,
				547,	557,	563,	569,	571,	577,	587,	593,	599,	601,
				607,	613,	617,	619,	631,	641,	643,	647,	653,	659,
				661,	673,	677,	683,	691,	701,	709,	719,	727,	733,
				739,	743,	751,	757,	761,	769,	773,	787,	797,	809,
				811,	821,	823,	827,	829,	839,	853,	857,	859,	863,
				877,	881,	883,	887,	907,	911,	919,	929,	937,	941,
				947,	953,	967,	971,	977,	983,	991,	997,	1009,	1013,
				1019,	1021,	1031,	1033,	1039,	1049,	1051,	1061,	1063,	1069,
				1087,	1091,	1093,	1097,	1103,	1109,	1117,	1123,	1129,	1151,
				1153,	1163,	1171,	1181,	1187,	1193,	1201,	1213,	1217,	1223,
				1229,	1231,	1237,	1249,	1259,	1277,	1279,	1283,	1289,	1291,
				1297,	1301,	1303,	1307,	1319,	1321,	1327,	1361,	1367,	1373,
				1381,	1399,	1409,	1423,	1427,	1429,	1433,	1439,	1447,	1451,
				1453,	1459,	1471,	1481,	1483,	1487,	1489,	1493,	1499,	1511};
	    final int bmax = 0x0000FFFF; 
	    return (((s.hashCode() * (index*2 + 1)) + Math.min(primes[index], bmax)) >> (Integer.SIZE / 2));
	}
	
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static ArrayList<Snippet> makeSniArray(Map rawdata, Map similarDoc){
		ArrayList<Snippet> sni = new ArrayList<Snippet>();
		
		List<Map.Entry<String, Double>> list = new LinkedList<Map.Entry<String, Double>>( similarDoc.entrySet());
		for(Map.Entry<String, Double> entry : list){
			TF_IDF temp = (TF_IDF) rawdata.get(entry.getKey());
			sni.add(temp.snippet);
		}
		return sni;
	}

}
