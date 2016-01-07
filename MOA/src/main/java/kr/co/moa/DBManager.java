package kr.co.moa;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.UnknownHostException;

import com.google.gson.Gson;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.util.JSON;

import kr.co.data.HtmlData;

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
    
    public HtmlData getData(String collection_name) throws Exception{
    	db = mongoClient.getDB(DB_NAME);
   	 
    	DBCollection collection = db.getCollection(collection_name);
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
