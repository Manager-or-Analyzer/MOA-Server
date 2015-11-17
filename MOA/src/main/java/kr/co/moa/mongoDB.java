package kr.co.moa;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;

public class mongoDB {
	MongoClient mongoClient = null;
    DB db=null;
   
    public void mongoTest(String ip,int port,String dbname) throws Exception{
          
           mongoClient = new MongoClient(new ServerAddress(ip,port));
           db = mongoClient.getDB(dbname);
          
           DBCollection userTable = db.getCollection("userTable");
           BasicDBObject doc = new BasicDBObject("name", "MongoDB").
            append("type", "database").
            append("count", 1).
            append("info", new BasicDBObject("x", 203).append("y", 102));

           userTable.insert(doc);
    }
}
