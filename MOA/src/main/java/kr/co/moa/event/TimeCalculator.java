package kr.co.moa.event;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

import com.google.gson.Gson;

import kr.co.data.DomTimeData;
import kr.co.data.origin.EventData;
import kr.co.moa.DBManager;

public class TimeCalculator {
	private static TimeCalculator instance;
	private static final long minCalcTime = (long) 30; //second
	private static final long maxWaitTime = (long) 5;  //minute
	
	
	public static TimeCalculator getInstance(){
		if(instance == null)	instance = new TimeCalculator();
		return					instance;
	}
	
	private TimeCalculator(){
		
	}

	public void calcTime(EventData pageout) {
		ArrayList<EventData> events = 
				(ArrayList<EventData>) DBManager.getInstnace().getTimeEvent(pageout);
		ArrayList<Long> eventTimes = new ArrayList<Long>();
		final SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd yyyy HH:mm:ss", Locale.UK);
		
		//Sorting
		Collections.sort(events, new Comparator<EventData>() {
			@Override
			public int compare(EventData o1, EventData o2) {
				Date d1 = null;
				Date d2 = null;
				try {
					d1 = sdf.parse(o1.time);
					d2 = sdf.parse(o2.time);
				} catch (ParseException e1) {
					e1.printStackTrace();
				}
				return d1.compareTo(d2);
			}
		});
		
		for(EventData e : events){
			Date d = null;
			try {
				d = sdf.parse(e.time);
			} catch (ParseException e1) {
				e1.printStackTrace();
			}
			eventTimes.add(d.getTime());
		}
		
		//start calc stay time
		Long total = (eventTimes.get(events.size()-1) - eventTimes.get(0));
		if( (total/1000) < minCalcTime ){
			//계산끝 그냥 total을 머문시간으로 가정 (큰 의미없는 페이지들)
		}else{
			long gap;
			for(int i = 0; i<eventTimes.size()-1; i++){
				if(events.get(i).type.equals("tabout")){
					total = total - (eventTimes.get(i+1) - eventTimes.get(i));
					i++;
					continue;
				}
				gap = eventTimes.get(i+1) - eventTimes.get(i);
				//gap is more than maxWaitTime minute
				if(gap > maxWaitTime * 60 * 1000) total -= gap;	
			}
		}
		
		total /= 1000;
		DomTimeData dd = new DomTimeData();
		dd.userid = pageout.userid;
		dd.url = pageout.url;
		dd.time = events.get(0).time;
		dd.duration = total;
		try {
			DBManager.getInstnace().insertData("DurationData", new Gson().toJson(dd));
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		//total/1000
	}
}
