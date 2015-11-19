package kr.co.moa;

import java.net.UnknownHostException;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.util.JSON;

//singleton으로 구현 
public class DBManager {	
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
    
    public void insertData(String dbname, String data_json) throws Exception{
    	 db = mongoClient.getDB(dbname);
    	 
    	 DBCollection collection = db.getCollection(dbname);
    	 DBObject dbObject = (DBObject)JSON.parse(data_json);
    	 
    	 collection.insert(dbObject);
    	
    }
//    public void mongoTest(String ip,int port,String dbname) throws Exception{
//           
//           mongoClient = new MongoClient(new ServerAddress(ip,port));
//           db = mongoClient.getDB(dbname);
//          
//           DBCollection userTable = db.getCollection("userTable");
//           BasicDBObject doc = new BasicDBObject("name", "MongoDB").
//            append("type", "database").
//            append("count", 1).
//            append("info", new BasicDBObject("x", 203).append("y", 102));
//
//           userTable.insert(doc);
//    }
}
