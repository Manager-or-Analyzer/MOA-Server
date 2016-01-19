package kr.co;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;

import kr.co.data.receive.DateData;
import kr.co.moa.DBManager;

public class Util {
	public static Date strToDate(String str){
		final SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd yyyy HH:mm:ss", Locale.UK);
		if(str == null) return new Date();
		try {
			return sdf.parse(str);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static String dateToStr(Date date){
		final SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd yyyy HH:mm:ss", Locale.UK);
		if(date == null) return new Date().toString();
		return sdf.format(date);
	}
	
	public static List<String> urlFromTo (DateData raw){
		Date from = strToDate(raw.start);
		Date to = strToDate(raw.end);
		System.out.println("from :"+raw.start+" end:"+raw.end);

		BasicDBObject query = new BasicDBObject();
		BasicDBObject date_query = new BasicDBObject();
		date_query.put("$gte", from);
		date_query.put("$lte", to);
		query.put("userid", raw.userid);
		query.put("time", date_query);
		
		DBCursor cursor = DBManager.getInstnace().getFromToUrl(query);
		
		List<String> result = new ArrayList<String>();
		while(cursor.hasNext()){
			result.add((String) cursor.next().get("url"));
		}
		
		return result;
		
	}
}
