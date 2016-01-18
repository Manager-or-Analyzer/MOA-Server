package kr.co.moa;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

import kr.co.data.origin.EventData;
import kr.co.data.origin.HtmlData;
import kr.co.data.parsed.HtmlParsedData;

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
    
    public void insertData(String collection_name, String data_json) throws Exception{
    	 db = mongoClient.getDB(DB_NAME);
    	 
    	 DBCollection collection = db.getCollection(collection_name);
    	 DBObject dbObject = (DBObject)JSON.parse(data_json);
    	 System.out.println("insertData log :"+data_json);
    	 collection.insert(dbObject);    	    
    }
    
    public void updateData(String collection_name, String data_json) throws Exception{
   	 db = mongoClient.getDB(DB_NAME);
   	 
   	 DBCollection collection = db.getCollection(collection_name);
   	 DBObject dbObject = (DBObject)JSON.parse(data_json);
   	 System.out.println("updateData log :"+data_json);
   	 collection.insert(dbObject);    	    
   }
    
    
    public List<HtmlParsedData> getHtmlParsedDataList(String userid, String keyword) throws Exception{
    	db = mongoClient.getDB(DB_NAME);
    	
    	DBCollection collection = db.getCollection("ParsedHtmlCollection");
    	BasicDBObject whereQuery = new BasicDBObject();
    	whereQuery.put("userid", userid);
    	DBCursor cursor = collection.find(whereQuery);
    	
    	Gson gson = new Gson();
    	List<HtmlParsedData> list = new ArrayList<HtmlParsedData>();
    	while(cursor.hasNext()){
    		HtmlParsedData hpd = gson.fromJson(cursor.next().toString(), HtmlParsedData.class);
    		list.add(hpd);    		
    	}
    	
    	return list;
    }
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
    
//    public int getDocSize() throws Exception{
//    	db = mongoClient.getDB(DB_NAME);
//    	
//    	DBCollection collection = db.getCollection("ParsedHtmlCollection");
//    	return collection.distinct("url").size();    	    	
//    }
//    
//    public int getDocCnt(String key) throws Exception{
//    	db = mongoClient.getDB(DB_NAME);
//    	
//    	DBCollection collection = db.getCollection("ParsedHtmlCollection");
//    	List urlList = collection.distinct("url");
//    	DBCursor cursor = collection.find();
//    	
//    	int cnt = 0;
//    	while(cursor.hasNext()){
//    		BasicDBObject obj = (BasicDBObject)cursor.next();
//    		BasicDBObject list = (BasicDBObject)obj.get("keywordList");
//    		String url = obj.getString("url");
//    		
//    		if(list != null && urlList.contains(url)){
//    			urlList.remove(url);
//    			if(list.containsKey(key)){
//    				cnt++;
//    			}
//    		}
//    	}
//    	return cnt;
//    }
    
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
   	 			+							"map[k] = map[k]+doc['eventwordsList'][k];"
   	 			+							"check = 1;"
   	 			+						"}else{"
   	 			+							"map[k] = doc['eventwordsList'][k];"
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
   	 		keywordlist =  (Map)tmp.get("eventwordsList");
   	 	}
   	 	
   	 	return keywordlist;
   	 	
    }
//    public Map getKeyword(String userid){
//    	db = mongoClient.getDB(DB_NAME);
//      	 
//   	 	DBCollection collection = db.getCollection("KeywordCollection");
//   	 	BasicDBObject query = new BasicDBObject();
//   	 		query.append("userid", userid);
//   	 	   	 	
//   	 	DBCursor cursor = collection.find(query);	
//   	 	
//   	 	Map<String, Double> topKeyList = new HashMap<String, Double>();
//   	 	while(cursor.hasNext()){
//   	 		DBObject tmp = (DBObject)obj;
//	 		keywordlist =  (Map)tmp.get("eventwordsList");
//   	 	}
//   	 	
//    }
    public void makeData_IDF() throws Exception{
    	db = mongoClient.getDB(DB_NAME);
      	 
   	 	DBCollection collection = db.getCollection("ParsedHtmlCollection");
   	 	String map = "function() { "
	 			+		"var key = {name: this.name};"
	 			+		"emit(key, {idfwordsList : this.keywordList});"
	 			+	"};";
	 	
	 	String reduce = "function (key, values) {"
	 			+			"var map = {};"
	 			+			"var totalDoc = values.length;"
	 			+			"values.forEach(function (doc){"
	 			+					"for(var k in doc['idfwordsList']){ "
	 			+						"if(k in map){"
	 			+							"continue;"
	 			+						"}"
	 			+						"var i; var cnt=0; var val=0;"
	 			+						"for(i=0; i<totalDoc; i++){"
	 			+						"	if(k in values[i]['idfwordsList'])cnt++;"
	 			+						"}"
	 			+						"if(cnt != 0)"
	 			+							"val = Math.log(totalDoc/cnt);"
	 			+						"if(val <0)val=0; map[k] = val;"
	 			+					"}});"
	 			+		"return {idfwordsList :map};};";
	 	
	 	MapReduceCommand cmd = new MapReduceCommand(collection, map, reduce, 
	 			"IdfCollection", MapReduceCommand.OutputType.REPLACE, null);
	 	
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
    
  public Map getIDFList(Map<String, Integer> idfList){
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
//    public void updateData_IDF(IDf updateList) throws Exception{
//    	db = mongoClient.getDB(DB_NAME);
//   	 
//   	 	DBCollection collection = db.getCollection("IdfCollection");
//   	 	
//   	 	// 1. collection 안에 값을 가져온다.
//   	 	// collection에서 나온 값이 updateList에 없으면 새로 계산.
//   	 	// 있으면 업데이트
//   	 	// 없으면 추가.
//   	 
//   	 	DBCursor cursor = collection.find();
//   	 	if(!cursor.hasNext()){
//   	 		insertData("IdfCollection", new Gson().toJson(updateList));
//   	 		return;
//   	 	}
//   	 	
//   	 	BasicDBObject obj = (BasicDBObject)cursor.next();
// 		BasicDBObject list = (BasicDBObject)obj.get("idfList");
// 		Set keyset = updateList.idfList.keySet();
// 		System.out.println("updateList Size: "+updateList.idfList.size());
// 		if(list != null){
// 			
// 			for(String key : list.keySet()){
// 				//System.out.println("start");
// 				if( keyset.contains(key)){
// 					//System.out.println("val : "+updateList.idfList.get(key));
// 					BasicDBObject searchQuery = new BasicDBObject().append("name", "idfCollection");
// 					BasicDBObject updateQuery = new BasicDBObject()
// 							.append("$set", new BasicDBObject().append("idfList."+key, updateList.idfList.get(key)));
// 					collection.update(searchQuery, updateQuery);
// 					updateList.idfList.remove(key);
// 				}else{
// 					
// 					//idf 계산
// 					int totalDoc = getDocSize();
// 					int cnt = getDocCnt(key);
// 					double val = java.lang.Math.log(totalDoc/(1+cnt));
// 					if(val <0) continue;
// 					
// 					BasicDBObject searchQuery = new BasicDBObject().append("name", "idfCollection");
// 					BasicDBObject updateQuery = new BasicDBObject()
// 							.append("$set", new BasicDBObject().append("idfList."+key, val));
// 					collection.update(searchQuery, updateQuery); 					 				
// 				}
// 	 		}
// 			
// 			//제거되고 남은  updateList.idfList 추가.
// 			for(String key : updateList.idfList.keySet()){
// 					BasicDBObject searchQuery = new BasicDBObject().append("name", "idfCollection");
//					BasicDBObject updateQuery = new BasicDBObject()
//							.append("$set", new BasicDBObject().append("idfList."+key, updateList.idfList.get(key)));
//					collection.update(searchQuery, updateQuery);
// 			}
// 			
// 		}else{
// 			insertData("IdfCollection", new Gson().toJson(updateList));
// 		} 		   	 
//    }
    public boolean isDocExist(String url){
    	db = mongoClient.getDB(DB_NAME);

    	DBCollection collection = db.getCollection("ParsedHtmlCollection");
    	
    	BasicDBObject query = new BasicDBObject();
    	query.put("snippet.url", url);
    	
    	DBCursor cursor = collection.find(query);
    	
    	if(cursor.hasNext()){
    		return true;
    	}    		
    	else{
    		return false;
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
	public List<EventData> getTimeEvent(EventData pageout) {
		db = mongoClient.getDB(DB_NAME);
    	
    	DBCollection collection = db.getCollection("EventData");
    	BasicDBObject whereQuery = new BasicDBObject();
    	whereQuery.put("userid", pageout.userid);
    	whereQuery.put("url", pageout.url);
    	whereQuery.put("isUsed", false);
    	
    	//get data from MongoDB
		DBCursor cursor = collection.find(whereQuery);
    	Gson gson = new Gson();
    	List<EventData> list = new ArrayList<EventData>();
    	while(cursor.hasNext()){
    		String jsondata = cursor.next().toString();
    		EventData ed = gson.fromJson(jsondata, EventData.class);
    		list.add(ed);
    	}
    	
    	//update data in MongoDB
    	BasicDBObject updateData = new BasicDBObject();
    	updateData.append("$set", new BasicDBObject().append("isUsed", true));
    	BasicDBObject updateQuery = new BasicDBObject();
    	updateQuery.
    			append("userid", pageout.userid).
    			append("url", pageout.url).
    			append("isUsed", false);
    	collection.update(updateQuery, updateData, false, true);
    	
    	return list;
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
	
	
	

	
	
	
	
	
	
}
