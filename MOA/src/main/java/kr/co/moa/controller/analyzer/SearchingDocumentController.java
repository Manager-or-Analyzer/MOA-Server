package kr.co.moa.controller.analyzer;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import kr.co.MapUtil;
import kr.co.data.SearchData;
import kr.co.data.TF_IDF;
import kr.co.moa.DBManager;

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
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PrintWriter out = response.getWriter();	
		String str = request.getParameter("data");		
		DBCursor cursor = null;
		SearchData sd = new Gson().fromJson(str, SearchData.class);
		HashMap<String, TF_IDF> rawdata = new HashMap<String, TF_IDF>();
		
		if(sd.searches.equals("") && sd.searches == null){ 
			out.println("fail");
			return;
		}
		
		try {
			cursor = DBManager.getInstnace().getTargetDocuments(sd);
		} catch (Exception e) {
			//DB exception
		}		
		
		double maxWeight = 0;		
		String standard_url = null;		//기준문서 
		if(!cursor.hasNext()){			//userid에 해당하는 일치하는 키워드 없음 
			System.out.println("No match data with keyword : " + sd.searches + " / " + sd.userid);
		}
		
		while(cursor.hasNext()){
			//parsing
			BasicDBObject obj = (BasicDBObject) cursor.next();
			TF_IDF temp = new TF_IDF();
			//gson.fromJson(cursor.next().toString(), KeywordData.class);    
			temp.userid = (String) obj.get("userid");
			temp.snippet.url = (String) obj.get("url");
			temp.keywordList = (Map<String, Double>) obj.get("keywordList");
			
			//sorting
	        temp.keywordList = MapUtil.Map_sortByValue(temp.keywordList);
	        
	        //최대값 구함 
	        if(temp.keywordList.get(sd.searches) > maxWeight){
	        	maxWeight = temp.keywordList.get(sd.searches);
	        	standard_url = temp.snippet.url;
	        }
			rawdata.put(temp.snippet.url, temp);
		}
		System.out.println(standard_url + " / " + maxWeight);
		
		//3. url군집에 속하는 상위 keyword_num개의 키워드 가져옴
		ArrayList<TF_IDF> arr = new ArrayList<TF_IDF>(rawdata.values());
 		HashMap<Integer, ArrayList<String>> reduced = new HashMap<Integer, ArrayList<String>>();
		int[] standard_hashcodes = new int[DIFF_NUM];
		String[] standard_keywords = null;
		
		for(TF_IDF entry : arr){
			String[] keyword = getFirstEntries(KEYWORD_NUM, entry.keywordList);
			int[] hashcodes = new int[KEYWORD_NUM];
			
			for(int i = 0; i < DIFF_NUM; i++){
				int minHashcode = 0x00010000;
				for(int k = 0; k < KEYWORD_NUM; k++){
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
		Map<String, Double> similar_doc = 
				cosineSimilarity(clustedGroup, standard_url, rawdata, standard_keywords);
		
		System.out.println(similar_doc);
		
		out.println("success");
		
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Map<String, Double> cosineSimilarity(
			ArrayList<String> clustedGroup, String standard_url, HashMap<String, TF_IDF> rawdata, String[] standard_keywords) {
		/*
		 * 문서간의 유사도를 코사인 유사도를 이용하여 계산한 후
		 * 코사인유사도 값이 높은 문서 순으로 소팅하여 map에 넣어 반환해주는 함수 
		 */
		HashMap<String, Double> result = new HashMap<String, Double>();
		double standard_doc_div = 0;
		double[] standard_wight = new double[KEYWORD_NUM];
		for(int i = 0; i<KEYWORD_NUM; i++){
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
				}else{
					//System.out.print("0 ");
				}
			}
			//System.out.println();
			target_doc_div = Math.sqrt(target_doc_div);
			
			result.put(target_url, numerator/(standard_doc_div * target_doc_div));
		}
		
		//sorting
		result = (HashMap<String, Double>) MapUtil.Map_sortByValue(result);
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

}
