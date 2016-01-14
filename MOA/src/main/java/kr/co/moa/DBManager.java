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

import kr.co.data.EventData;
import kr.co.data.HtmlData;
import kr.co.data.HtmlParsedData;
import kr.co.data.IDf;

//singleton���� ���� 
public class DBManager {	
	
	private static final String DB_NAME = "test";
    private static final String IP = "127.0.0.1";
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
    
    public int getDocSize() throws Exception{
    	db = mongoClient.getDB(DB_NAME);
    	
    	DBCollection collection = db.getCollection("ParsedHtmlCollection");
    	return collection.distinct("url").size();    	    	
    }
    
    public int getDocCnt(String key) throws Exception{
    	db = mongoClient.getDB(DB_NAME);
    	
    	DBCollection collection = db.getCollection("ParsedHtmlCollection");
    	List urlList = collection.distinct("url");
    	DBCursor cursor = collection.find();
    	
    	int cnt = 0;
    	while(cursor.hasNext()){
    		BasicDBObject obj = (BasicDBObject)cursor.next();
    		BasicDBObject list = (BasicDBObject)obj.get("keywordList");
    		String url = obj.getString("url");
    		
    		if(list != null && urlList.contains(url)){
    			urlList.remove(url);
    			if(list.containsKey(key)){
    				cnt++;
    			}
    		}
    	}
    	return cnt;
    }
    
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
   	 	Map keywordlist = new HashMap();
   	 	int cnt = 1;
   	 	for(DBObject res :  out.results()){
   	 		System.out.println(res.get("value").toString());
   	 		Object obj = res.get("value");
   	 		DBObject tmp = (DBObject)obj;
   	 		keywordlist =  (Map)tmp.get("eventwordsList");
   	 	}
   	 	
   	 	return keywordlist;
   	 	
    }
    public void updateData_IDF(IDf updateList) throws Exception{
    	db = mongoClient.getDB(DB_NAME);
   	 
   	 	DBCollection collection = db.getCollection("IdfCollection");
   	 	
   	 	// 1. collection 안에 값을 가져온다.
   	 	// collection에서 나온 값이 updateList에 없으면 새로 계산.
   	 	// 있으면 업데이트
   	 	// 없으면 추가.
   	 
   	 	DBCursor cursor = collection.find();
   	 	if(!cursor.hasNext()){
   	 		insertData("IdfCollection", new Gson().toJson(updateList));
   	 		return;
   	 	}
   	 	
   	 	BasicDBObject obj = (BasicDBObject)cursor.next();
 		BasicDBObject list = (BasicDBObject)obj.get("idfList");
 		Set keyset = updateList.idfList.keySet();
 		System.out.println("updateList Size: "+updateList.idfList.size());
 		if(list != null){
 			
 			for(String key : list.keySet()){
 				//System.out.println("start");
 				if( keyset.contains(key)){
 					//System.out.println("val : "+updateList.idfList.get(key));
 					BasicDBObject searchQuery = new BasicDBObject().append("name", "idfCollection");
 					BasicDBObject updateQuery = new BasicDBObject()
 							.append("$set", new BasicDBObject().append("idfList."+key, updateList.idfList.get(key)));
 					collection.update(searchQuery, updateQuery);
 					updateList.idfList.remove(key);
 				}else{
 					
 					//idf 계산
 					int totalDoc = getDocSize();
 					int cnt = getDocCnt(key);
 					double val = java.lang.Math.log(totalDoc/(1+cnt));
 					if(val <0) continue;
 					
 					BasicDBObject searchQuery = new BasicDBObject().append("name", "idfCollection");
 					BasicDBObject updateQuery = new BasicDBObject()
 							.append("$set", new BasicDBObject().append("idfList."+key, val));
 					collection.update(searchQuery, updateQuery); 					 				
 				}
 	 		}
 			
 			//제거되고 남은  updateList.idfList 추가.
 			for(String key : updateList.idfList.keySet()){
 					BasicDBObject searchQuery = new BasicDBObject().append("name", "idfCollection");
					BasicDBObject updateQuery = new BasicDBObject()
							.append("$set", new BasicDBObject().append("idfList."+key, updateList.idfList.get(key)));
					collection.update(searchQuery, updateQuery);
 			}
 			
 		}else{
 			insertData("IdfCollection", new Gson().toJson(updateList));
 		} 		   	 
    }
}
