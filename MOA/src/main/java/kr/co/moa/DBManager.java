package kr.co.moa;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bson.BasicBSONObject;

import com.google.gson.Gson;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MapReduceCommand;
import com.mongodb.MapReduceOutput;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.util.JSON;

import kr.co.data.DomTimeData;
import kr.co.Util;
import kr.co.data.TF_IDF;
import kr.co.data.origin.EventData;
import kr.co.data.origin.HtmlData;
import kr.co.data.parsed.HtmlParsedData;
import kr.co.data.receive.DateData;
import kr.co.data.send.Snippet;
import kr.co.moa.controller.analyzer.KeywordsSenderController;
import kr.co.moa.controller.analyzer.KeywordsSenderController.Dictionary_custom;

//singleton���� ���� 
public class DBManager {	
	
	private static final String DB_NAME = "MOA";
    private static final String IP = "210.118.74.183";
    private static final int PORT = 27017;
    private static DBManager instance;
    private MongoClient mongoClient;
    private DB db=null;
	
	public static DBManager getInstnace() {
		if (instance == null) {
			instance = new DBManager();
		}
		return instance;
	}		
    private DBManager(){
    	 try {
			mongoClient = new MongoClient(new ServerAddress(IP,PORT));
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}    	
    }
    
    //insert
    public void insertData(String collection_name, String data_json) throws Exception{
    	 db = mongoClient.getDB(DB_NAME);
    	 
    	 DBCollection collection = db.getCollection(collection_name);
    	 DBObject dbObject = (DBObject)JSON.parse(data_json);
    	 System.out.println("insertData log :"+data_json);
    	 collection.insert(dbObject);    	    
    }
	public void insertEventData(BasicDBObject query) {
		db = mongoClient.getDB(DB_NAME);
		db.getCollection("EventData").insert(query);
	}
	
	public void insertDurationData(BasicDBObject query) {
		db = mongoClient.getDB(DB_NAME);
		db.getCollection("DurationData").insert(query);
	}
            
    //getData
    //Get Data
    public EventData getEventData() throws Exception{
    	db = mongoClient.getDB(DB_NAME);
   	 
    	DBCollection collection = db.getCollection("EventData");
    	String res = collection.findOne().toString();    	    	 
    	Gson gson = new Gson();
    	EventData ed = gson.fromJson(res, EventData.class);    	

    	return ed;
    }
    public HtmlData getHtmlData() throws Exception{
    	db = mongoClient.getDB(DB_NAME);
   	 
    	DBCollection collection = db.getCollection("HtmlData");
    	String res = collection.findOne().toString();
    	    	 
    	Gson gson = new Gson();
    	HtmlData hd = gson.fromJson(res, HtmlData.class);    	

    	return hd;
    	
    }   
    public Map getParsedEvents(String userId, String url) throws Exception{
    	db = mongoClient.getDB(DB_NAME);
      	 
   	 	DBCollection collection = db.getCollection("ParsedEventCollection");
   	 	
   	 	String map = "function() { "
   	 			+		"var key = {userId: this.userid, url:this.url};"
   	 			+		"emit(key, {eventwordsList : this.keywordList});"
   	 			+	"};";
   	 	
   	 	String reduce = "function (key, values) {"
   	 			+			"var map = {};"
   	 			+			"values.forEach(function (doc){"
   	 			+					"for(var k in doc['eventwordsList']){ "
   	 			+						"if(k in map){"
   	 			+							"map[k] = map[k]+doc['eventwordsList'][k]+0.0;"
   	 			+							"check = 1;"
   	 			+						"}else{"
   	 			+							"map[k] = doc['eventwordsList'][k]+0.0;"
   	 			+						"}}});"
   	 			+		"return {eventwordsList :map};};";
   	 	
   	 	BasicDBObject query = new BasicDBObject();
   	 	query.append("url", url);
   	 	query.append("userid", userId);
   	 	MapReduceCommand cmd = new MapReduceCommand(collection, map, reduce, 
   	 			null, MapReduceCommand.OutputType.INLINE, query);
   	 	
   	 	MapReduceOutput out = collection.mapReduce(cmd);
   	 	Map<String, Double> keywordlist = new HashMap<String, Double>();
   	 	int cnt = 1;
   	 	for(DBObject res :  out.results()){
   	 		System.out.println(res.get("value").toString());
   	 		Object obj = res.get("value");
   	 		DBObject tmp = (DBObject)obj;
   	 		keywordlist.putAll((Map)tmp.get("eventwordsList"));
   	 	}
   	 	
   	 	return keywordlist;
   	 	
    }    
    public KeywordsSenderController.Dictionary_custom getKeywordList(List<String> urls, String userid, DateData datedata) throws Exception{
    	db = mongoClient.getDB(DB_NAME);
//      	for(String s: urls){
//      		System.out.println("url :"+s+" "+userid);
//      	}
   	 	DBCollection collection = db.getCollection("KeywordCollection");
   	 	String map = "function() { "
	 			+		"var key = {name: this.collectionName};"
	 			+		"emit(key, {keywordsList : this.keywordList});"
	 			+	"};";
	 	
	 	String reduce = "function (key, values) {"
	 			+		"var totalDoc = values.length;"
	 			+		"return {total : totalDoc, result : values};};";
	 	
	 	String reduce2 = 
	 						"values = tmp; var map = {}; var cnt = 0; var totalDoc = values.length;"
	 			+			"values.forEach(function (doc){"
	 			+				"cnt =0;"
	 			+					"for(var k in doc['keywordsList']){ "
	 			+					  "if(cnt++ <3){"
	 			+						"if(k in map){"
	 			+							"cnt--;"
   	 			+						"}else{"
	 			+							"map[k]=0.0;"
	 			+							"for(i=0; i<totalDoc; i++){"
	 			+								"var maplist = values[i]['keywordsList'];"
	 			+								"if(k in maplist)"
	 			+									"map[k] = map[k]+maplist[k];"
	 			+							"}"
   	 			+						"}}"
   	 			+ 					  "else{"
   	 			+ 						"break;}}});"
	 			+		"return {tmp : tmp, total : totalDoc, val : values[0]['keywordsList']  , keywordsList :map};";
	 	
	 	String finalize = "function (key, values) {"
	 			+			"var map = {}; var tmp = values['result'];"
	 			+			"if('result' in values['result'][0]){"
	 			+			"var totalDoc = 0; var i;"
	 			+			"for(i=0; i<values['result'].length; i++){"
	 			+				"totalDoc += values['result'][i]['total'];}"	 	
	 			+			"for(i=0; i<values['result'].length; i++){"
	 			+				"var j; var obj = values['result'][i]['result']; "
	 			+				"for(j=0; j< obj.length; j++){"
	 			+					"var cnt = 0;"
	 			+					"for(var k in obj[j]['keywordsList']){"
	 			+						"if(cnt++ <3){"
	 			+							"if(k in map){"
	 			+								"map[k] += obj[j]['keywordsList'][k];"
	 			+								"cnt--;"
	 			+							"}else{"
	 			+								"map[k] = 0.0;"
	 			+								"map[k] = obj[j]['keywordsList'][k];"
	 			+							"}"
	 			+						"}else break;"
	 			+					"}}};"
	 			+		"return {tmp : tmp, total :totalDoc,keywordsList: map};}"
	 			+		"else {"+reduce2+"}}";	
	 	
	 	

	 	BasicDBObject query = new BasicDBObject();
	 		query.append("userid", userid);
	 		query.append("snippet.url", new BasicDBObject("$in", urls));
	 		
	 	MapReduceCommand cmd = new MapReduceCommand(collection, map, reduce, 
	 			"kTest", MapReduceCommand.OutputType.REPLACE, query);
	 	cmd.setFinalize(finalize);
	 		
	 	MapReduceOutput out = collection.mapReduce(cmd);
	 	Map<String, Double> keywordlist = new HashMap<String, Double>();
	 	for(DBObject res :  out.results()){
	 		System.out.println("bubble "+res.get("value").toString());
	 		Object obj = res.get("value");
	 		DBObject tmp = (DBObject)obj;
	 		keywordlist.putAll((Map)tmp.get("keywordsList"));
	 		//return keywordlist;
	 	}
	 	System.out.println("bubble "+keywordlist.size());
	 	BasicDBObject timeQuery = new BasicDBObject();
	 	
	 	Date from = Util.strToDate(datedata.start);
    	Date to = Util.strToDate(datedata.end);
	 	BasicDBObject date_query = new BasicDBObject();
	 			date_query.put("$gte", from);
	 			date_query.put("$lte", to);
			query.put("userid", datedata.userid);
			query.put("snippet.time", date_query);
	
		List<TF_IDF> keyCollections = new ArrayList<TF_IDF>();	
		DBCursor cursor = collection.find(query);
		
		while(cursor.hasNext()){
			BasicDBObject obj = (BasicDBObject) cursor.next();
			TF_IDF tfidf = new TF_IDF();
			tfidf.userid = obj.getString("userid");
			tfidf.keywordList = (Map<String, Double>) obj.get("keywordList");
			tfidf.snippet = new Snippet();
			BasicBSONObject obj2 = (BasicBSONObject) obj.get("snippet");
			
			tfidf.snippet.title = obj2.getString("title");			
			tfidf.snippet.url = obj2.getString("url");
			tfidf.snippet.img = obj2.getString("img");
			
			Date time = obj2.getDate("time");
			tfidf.snippet.time = Util.dateToStr(time);
			keyCollections.add(tfidf);
		}
	 	
		KeywordsSenderController.Dictionary_custom res = new Dictionary_custom();
		res.keywordList = keywordlist;
		res.keyCollections = keyCollections;
	 	return res;
    	
    }    
    public <K> Map getIDFList(Map<String, K> idfList){
	  	db = mongoClient.getDB(DB_NAME);
  	 
	 	DBCollection collection = db.getCollection("IdfCollection");
	 	BasicDBObject query = new BasicDBObject();
	 	DBCursor cursor = collection.find(query);	
	 	
	 	Map<String, Double> keywordlist = new HashMap<String, Double>();
	 	while(cursor.hasNext()){
	 		DBObject data = (DBObject)cursor.next();
	 		Object obj = data.get("value");
	 		DBObject tmp = (DBObject)obj;
	 		keywordlist =  (Map)tmp.get("idfwordsList");
	 	}
	 	
	 	Map<String, Double> res= new HashMap<String, Double>();
	 	for(String key : idfList.keySet()){
	 		res.put(key, keywordlist.get(key));
	 	}
	 	return res;
	 	
    }
    public List getUrls(DateData datedata){
    	db = mongoClient.getDB(DB_NAME);
    	
    	DBCollection collection = db.getCollection("EventData");
    	
    	Date from = Util.strToDate(datedata.start);
    	Date to = Util.strToDate(datedata.end);
    	System.out.println("getUrls : "+ "from :"+datedata.start+" end:"+datedata.end);
    	
    	BasicDBObject query = new BasicDBObject();
		BasicDBObject date_query = new BasicDBObject();
			date_query.put("$gte", from);
			date_query.put("$lte", to);
			query.put("userid", datedata.userid);
			query.put("time", date_query);
			query.put("type", "pagein");
		
		DBCursor cursor = collection.find(query);
		
		Set<String> set = new HashSet<String>();
		while(cursor.hasNext()){
			set.add((String) cursor.next().get("url"));
		}
		
		List<String> res = new ArrayList<String>();
		Iterator<String> it = set.iterator();
		while(it.hasNext()){
			res.add(it.next());
		}
		return res;    	   
    }
    public HtmlParsedData getHtmlParsedData(String url){
    	db = mongoClient.getDB(DB_NAME);

    	DBCollection collection = db.getCollection("ParsedHtmlCollection");
    	
    	BasicDBObject query = new BasicDBObject();
    	query.put("snippet.url", url);
    	
    	DBCursor cursor = collection.find(query);
    	if(cursor.hasNext()){
    		Gson gson = new Gson();
    		HtmlParsedData hpd = gson.fromJson(cursor.next().toString(), HtmlParsedData.class);
    		return hpd;
    	}    		
    	else{
    		return null;
    	}    	
    }
	public DBCursor getTargetDocuments(String userid, String keyword) {
		db = mongoClient.getDB(DB_NAME);

    	DBCollection collection = db.getCollection("KeywordCollection");
    	
    	BasicDBObject query = new BasicDBObject();
    	query.put("userid", userid);
    	query.put("keywordList." + keyword, new BasicDBObject("$exists", true));
    	
    	return collection.find(query);
	}
	public List getTimeEvent(EventData evt) {
		db = mongoClient.getDB(DB_NAME);
    	
    	DBCollection collection = db.getCollection("EventData");
    	BasicDBObject whereQuery = new BasicDBObject();
    	whereQuery.put("userid", evt.userid);
    	whereQuery.put("url", evt.url);
    	whereQuery.put("isUsed", false);
    	
    	//get data from MongoDB
		DBCursor cursor = collection.find(whereQuery);
		
		ArrayList<EventData> events = new ArrayList<EventData>();
		while(cursor.hasNext()){
			System.out.println("asdfsadfasdfsadf");
    		BasicDBObject obj = (BasicDBObject) cursor.next();
    		EventData ed = new EventData();
    		
    		ed.userid = obj.getString("userid");
    		ed.url 	  = obj.getString("url");
    		ed.type   = obj.getString("type");
    		ed.data   = obj.getString("data");
    		ed.time   = obj.getDate("time");
    		ed.x 	  = obj.getString("x");
    		ed.y 	  = obj.getString("y");
    		ed.isUsed = obj.getBoolean("isUsed");

    		//System.out.println(ed.userid);
    		//System.out.println(ed.url);
    		//System.out.println(ed.type);
    		
       		events.add(ed);
    	}
    	if(evt.type.equals("pageout")){	//pageout일때만 eventdata업데이트  
	    	//update data in MongoDB
	    	BasicDBObject updateData = new BasicDBObject();
	    	updateData.append("$set", new BasicDBObject().append("isUsed", true));
	    	BasicDBObject updateQuery = new BasicDBObject();
	    	updateQuery.
	    			append("userid", evt.userid).
	    			append("url", 	 evt.url).
	    			append("isUsed", false);
	    	
	    	collection.update(updateQuery, updateData, false, true);
	
    	}
		return events;
	}	
	public List getSimilar(BasicDBObject minhashInput){
		db = mongoClient.getDB(DB_NAME);
    	DBCollection collection = db.getCollection("idfCollection");
    	
    	//collection.insert(arr)
		BasicDBObject minhashQuery = new BasicDBObject();
		////minhashQuery.append("userid", )
    	
    	String firstMap = 
				"function (){" + 
					"db.loadServerScripts();" + 
					"var shingle = \"\";" +
					"for(var i = 0; i<5; i++){" +
						"shingle += this.keyword[i%5] + \" \" + " +
							"this.keyword[(i+1)%5] + \" \" + " +
							"this.keyword[(i+2)%5] + \" \" + " +
							"this.keyword[(i+3)%5] + \" \" + " +
							"this.keyword[(i+4)%5];" +
							
						"shingle = \"\""+
					"}" +
				
				
				"}";
		//가중치가 부여되어있는 상황인데 
		//minhash값을 찾는게 맞는것인가???
					
		/*
		 *   mongoDB nomalHash function
		 *   function (str) {
    			var hash = 0, i, chr, len;
    			if (str.length === 0) {
        			return hash;
    			}
    			for (i = 0, len = str.length; i < len; i++) {
        			chr = str.charCodeAt(i);
        			hash = (hash << 5) - hash + chr;
        			hash |= 0;
    			}
    			return hash;
			}
		 *   
		 *   
		 *   
		 */
		String firstReduce = "";
		String secondMap = "";
		String secondReduce = "";
		
		
					
		MapReduceCommand cmd = new MapReduceCommand(collection, 
				firstMap, firstReduce, "outputMinhash", 
				MapReduceCommand.OutputType.REDUCE, null);
		return null;
	}
	public DBCursor getDurationData(String userid, Set<String> set) {
		db = mongoClient.getDB(DB_NAME);
    	
    	BasicDBObject query = new BasicDBObject();
    	query.put("userid", userid);
    	query.put("url", new BasicDBObject("$in", set));
    	
    	return db.getCollection("DurationData").find(query);
	}	

	public void updateDurationData(DomTimeData dd) {
		db = mongoClient.getDB(DB_NAME);
		
		BasicDBObject domkey   = new BasicDBObject();
		domkey.put("userid", 	 dd.userid);
		domkey.put("url", 	  	 dd.url);
		domkey.put("time", 	     dd.time);
		
		BasicDBObject newValue = new BasicDBObject();
		newValue.put("userid",   dd.userid);
		newValue.put("url", 	 dd.url);
		newValue.put("time", 	 dd.time);
		newValue.put("duration", dd.duration);
		
		db.getCollection("DurationData").update(domkey, newValue, true, false);	//upsert : true
	}
	
	public DBCursor getFromToUrl(BasicDBObject query) {
		db = mongoClient.getDB(DB_NAME);
		return db.getCollection("DurationData").find(query);
	}
	//boolean isExist
    
    @SuppressWarnings("deprecation")
	public boolean isExist_in_idfCollection(String key) throws Exception{
    	db = mongoClient.getDB(DB_NAME);
    	
    	DBCollection collection = db.getCollection("IdfCollection");
    	DBCursor cursor = collection.find();
    	
    	while(cursor.hasNext()){
    		BasicDBObject obj = (BasicDBObject)cursor.next();
    		BasicDBObject list = (BasicDBObject)obj.get("idfList");
    		
    		if(list != null){
    			//Map idfList = list.
    			if(list.containsKey(key))
    				return true;
    		}    		    
    	}
    	return false;
    }
    public boolean isDocExist(String url,String userid){
    	userid = userid.replace(".", "\uff0E");
    	db = mongoClient.getDB(DB_NAME);

    	DBCollection collection = db.getCollection("ParsedHtmlCollection");
    	
    	BasicDBObject query = new BasicDBObject();
    	query.put("snippet.url", url);
    	query.put("userList."+userid, new BasicDBObject("$exists", true));
    	
    	DBCursor cursor = collection.find(query);
    	
    	if(cursor.hasNext()){
    		return true;
    	}    		
    	else{
    		return false;
    	}
    		
    }
    public boolean isParsedDataExist(String url){
    	db = mongoClient.getDB(DB_NAME);

    	DBCollection collection = db.getCollection("ParsedHtmlCollection");
    	
    	BasicDBObject query = new BasicDBObject();
    	query.put("snippet.url", url);
    	
    	DBCursor cursor = collection.find(query);
    	
    	if(cursor.hasNext()){
    		System.out.println("isParsedDataExist true");
    		return true;
    	}    		
    	else{
    		System.out.println("isParsedDataExist false");
    		return false;
    	}
    		
    }
    
 

    //update
    public void updateTime(String url, String userid, String time){
    	db = mongoClient.getDB(DB_NAME);

    	DBCollection collection = db.getCollection("KeywordCollection");
    	
    	BasicDBObject searchQuery = new BasicDBObject();
    		searchQuery.put("snippet.url", url);
    		searchQuery.put("userid", userid);
    	BasicDBObject updateQuery = new BasicDBObject();
    		updateQuery.append("$set", new BasicDBObject().append("snippet.time", Util.strToDate(time)));
    	collection.update(searchQuery, updateQuery);	
    }  
    public void updateParsedData(String url, String userid){
    	userid = userid.replace(".", "\uff0E");
    	db = mongoClient.getDB(DB_NAME);

    	DBCollection collection = db.getCollection("ParsedHtmlCollection");
    	
    	BasicDBObject searchQuery = new BasicDBObject();
    		searchQuery.put("snippet.url", url);
    	BasicDBObject updateQuery = new BasicDBObject();
    		updateQuery.append("$set", new BasicDBObject().append("userList."+userid, true));
    	collection.update(searchQuery, updateQuery);	
    }  
    public void updateTF_IDFByEvent(String url, String userid, Map<String, Double> keywordList){
    	db = mongoClient.getDB(DB_NAME);
    	
    	DBCollection collection = db.getCollection("KeywordCollection");
    	
    	BasicDBObject kl = new BasicDBObject();
    	BasicDBObject searchQuery = new BasicDBObject();
    		searchQuery.put("snippet.url", url);
    		searchQuery.put("userid", userid);
    	BasicDBObject updateQuery = new BasicDBObject();
    		updateQuery.append("$set", new BasicDBObject().append("keywordList", keywordList));
    	collection.update(searchQuery, updateQuery);	
    }
  
    
    public void makeData_IDF() throws Exception{
    	db = mongoClient.getDB(DB_NAME);
      	 
   	 	DBCollection collection = db.getCollection("ParsedHtmlCollection");
   	 	String map = "function() { "
	 			+		"var key = {name: this.collectionName};"
	 			+		"emit(key, {idfwordsList : this.keywordList});"
	 			+	"};";
	 	
   	 	String reduce = "function (key, values) {"
	 			+			"var map = {};"
	 			+			"var totalDoc = values.length;"
	 			+		"return {total :totalDoc, result: values};};";
   	 	String finalize = "function (key, values) {"
	 			+			"var map = {}; var tmp = {};"
	 			+			"var totalDoc = 0; var i;"
	 			+			"for(i=0; i<values['result'].length; i++){"
	 			+				"totalDoc += values['result'][i]['total'];}"	 	
	 			+			"for(i=0; i<values['result'].length; i++){"
	 			+				"var j; var obj = values['result'][i]['result'];"
	 			+				"for(j=0; j< obj.length; j++){"
	 			+					"for(var k in obj[j]['idfwordsList']){"
	 			+						"if(k in tmp) tmp[k] += 1;"
	 			+						"else tmp[k] =1;"
	 			+					"}}};"
	 			+			"for(var k in tmp){"
	 			+				"var val = Math.log(totalDoc/tmp[k]);"
	 			+				"if(val<0)val=0; map[k]=val;"
	 			+			"}"
	 			+		"return {total :totalDoc,idfwordsList: map};};";
   	 	
//	 	String reduce = "function (key, values) {"
//	 			+			"var map = {};"
//	 			+			"var totalDoc = values.length;var j=values;"
//	 			+			"values.forEach(function (doc){"
//	 			+					"for(var k in doc['idfwordsList']){ "
//	 			+						"if(k in map){"
//	 			+							"continue;"
//	 			+						"}"
//	 			+						"var i; var cnt=0; var val=0;"
//	 			+						"for(i=0; i<totalDoc; i++){"
//	 			+						"	if(k in values[i]['idfwordsList'])cnt++;"
//	 			+						"}"
//	 			+						"if(cnt != 0)"
//	 			+							"val = Math.log(totalDoc/cnt);"
//	 			+						"if(val <0)val=0; map[k] = val;"
//	 			+					"}});"
//	 			+		"return {total :totalDoc, idfwordsList :map};};";
	 	
	 	MapReduceCommand cmd = new MapReduceCommand(collection, map, reduce, 
	 			"IdfCollection", MapReduceCommand.OutputType.REPLACE, null);
	 	cmd.setFinalize(finalize);
	 	
	 	MapReduceOutput out = collection.mapReduce(cmd);
	 	Map<String, Double> keywordlist = new HashMap<String, Double>();
	 	int cnt = 1;
	 	for(DBObject res :  out.results()){
	 		System.out.println("makeIDF "+res.get("value").toString());
	 		Object obj = res.get("value");
	 		DBObject tmp = (DBObject)obj;
	 		//keywordlist =  (Map)tmp.get("eventwordsList");
	 	}
    	
    }
    






	

	
	

//  public int getDocSize() throws Exception{
//	db = mongoClient.getDB(DB_NAME);
//	
//	DBCollection collection = db.getCollection("ParsedHtmlCollection");
//	return collection.distinct("url").size();    	    	
//}
//
//public int getDocCnt(String key) throws Exception{
//	db = mongoClient.getDB(DB_NAME);
//	
//	DBCollection collection = db.getCollection("ParsedHtmlCollection");
//	List urlList = collection.distinct("url");
//	DBCursor cursor = collection.find();
//	
//	int cnt = 0;
//	while(cursor.hasNext()){
//		BasicDBObject obj = (BasicDBObject)cursor.next();
//		BasicDBObject list = (BasicDBObject)obj.get("keywordList");
//		String url = obj.getString("url");
//		
//		if(list != null && urlList.contains(url)){
//			urlList.remove(url);
//			if(list.containsKey(key)){
//				cnt++;
//			}
//		}
//	}
//	return cnt;
//}
	
//  public Map getTfCollection(String userid, String url){
//	db = mongoClient.getDB(DB_NAME);
//
//	DBCollection collection = db.getCollection("KeywordCollection");
//	
//	BasicDBObject query = new BasicDBObject();
//		query.put("snippet.url", url);
//		query.put("userid", userid);
//	DBCursor cursor = collection.find(query);
//	if(cursor.hasNext()){
//		Map res = (Map) cursor.next().get("keywordList");
//		return res;
//	}    		
//	else{
//		return null;
//	}    	
//}

//  public void updateData_IDF(IDf updateList) throws Exception{
//	db = mongoClient.getDB(DB_NAME);
//	 
//	 	DBCollection collection = db.getCollection("IdfCollection");
//	 	
//	 	// 1. collection 안에 값을 가져온다.
//	 	// collection에서 나온 값이 updateList에 없으면 새로 계산.
//	 	// 있으면 업데이트
//	 	// 없으면 추가.
//	 
//	 	DBCursor cursor = collection.find();
//	 	if(!cursor.hasNext()){
//	 		insertData("IdfCollection", new Gson().toJson(updateList));
//	 		return;
//	 	}
//	 	
//	 	BasicDBObject obj = (BasicDBObject)cursor.next();
//		BasicDBObject list = (BasicDBObject)obj.get("idfList");
//		Set keyset = updateList.idfList.keySet();
//		System.out.println("updateList Size: "+updateList.idfList.size());
//		if(list != null){
//			
//			for(String key : list.keySet()){
//				//System.out.println("start");
//				if( keyset.contains(key)){
//					//System.out.println("val : "+updateList.idfList.get(key));
//					BasicDBObject searchQuery = new BasicDBObject().append("name", "idfCollection");
//					BasicDBObject updateQuery = new BasicDBObject()
//							.append("$set", new BasicDBObject().append("idfList."+key, updateList.idfList.get(key)));
//					collection.update(searchQuery, updateQuery);
//					updateList.idfList.remove(key);
//				}else{
//					
//					//idf 계산
//					int totalDoc = getDocSize();
//					int cnt = getDocCnt(key);
//					double val = java.lang.Math.log(totalDoc/(1+cnt));
//					if(val <0) continue;
//					
//					BasicDBObject searchQuery = new BasicDBObject().append("name", "idfCollection");
//					BasicDBObject updateQuery = new BasicDBObject()
//							.append("$set", new BasicDBObject().append("idfList."+key, val));
//					collection.update(searchQuery, updateQuery); 					 				
//				}
//	 		}
//			
//			//제거되고 남은  updateList.idfList 추가.
//			for(String key : updateList.idfList.keySet()){
//					BasicDBObject searchQuery = new BasicDBObject().append("name", "idfCollection");
//				BasicDBObject updateQuery = new BasicDBObject()
//						.append("$set", new BasicDBObject().append("idfList."+key, updateList.idfList.get(key)));
//				collection.update(searchQuery, updateQuery);
//			}
//			
//		}else{
//			insertData("IdfCollection", new Gson().toJson(updateList));
//		} 		   	 
//}
	
//  public List<HtmlParsedData> getHtmlParsedDataList(String userid, String keyword) throws Exception{
//	db = mongoClient.getDB(DB_NAME);
//	
//	DBCollection collection = db.getCollection("ParsedHtmlCollection");
//	BasicDBObject whereQuery = new BasicDBObject();
//	whereQuery.put("userid", userid);
//	DBCursor cursor = collection.find(whereQuery);
//	
//	Gson gson = new Gson();
//	List<HtmlParsedData> list = new ArrayList<HtmlParsedData>();
//	while(cursor.hasNext()){
//		HtmlParsedData hpd = gson.fromJson(cursor.next().toString(), HtmlParsedData.class);
//		list.add(hpd);    		
//	}
//	
//	return list;
//}	
//	public DBCursor getFromToUrl(BasicDBObject query) {
//	db = mongoClient.getDB(DB_NAME);
//	return db.getCollection("DurationData").find(query);
//}
	
}
