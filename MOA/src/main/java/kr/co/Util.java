package kr.co;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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
}
