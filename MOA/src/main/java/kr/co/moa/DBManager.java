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
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.util.JSON;

import kr.co.data.EventData;
import kr.co.data.HtmlData;
import kr.co.data.HtmlParsedData;

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
}
