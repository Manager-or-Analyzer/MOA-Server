package kr.co.moa;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MapReduceCommand;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.util.JSON;

import kr.co.data.EventData;
import kr.co.data.HtmlData;
import kr.co.data.HtmlParsedData;
import kr.co.data.SearchData;

//singleton���� ���� 
public class DBManager {	
	
	private static final String DB_NAME = "test";
    private static final String IP = "localhost";//"210.118.74.183";
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
    	 System.out.println(data_json);
    	 collection.insert(dbObject);    	    
    }
    public List<HtmlParsedData> getHtmlParsedDataList(String userid, String keyword){
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
    	
//    	String everything;
//    	BufferedReader br = new BufferedReader(new FileReader("C:\\test.txt"));
//    	try {
//    	    StringBuilder sb = new StringBuilder();
//    	    String line = br.readLine();
//
//    	    while (line != null) {
//    	        sb.append(line);
//    	        sb.append(System.lineSeparator());
//    	        line = br.readLine();
//    	    }
//    	    everything = sb.toString();
//    	} finally {
//    	    try {
//				br.close();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//    	}
    	//String ress = new String(everything.getBytes("8859_1"), "UTF-8");
    	//Document doc = Jsoup.parse(everything);
    	//System.out.println(doc.select("html"));
    	
    	//HtmlParser.makeCBT(everything);
   	    //return res;	 
       	   
    }
    
    
	public DBCursor getTargetDocuments(SearchData sd) {
		db = mongoClient.getDB(DB_NAME);

    	DBCollection collection = db.getCollection("KeywordCollection");
    	
    	BasicDBObject query = new BasicDBObject();
    	query.put("userid", sd.userid);
    	query.put("keywordList."+sd.searches, new BasicDBObject("$exists", true));
    	
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
	
	
	
	
	
	
	
	
}
