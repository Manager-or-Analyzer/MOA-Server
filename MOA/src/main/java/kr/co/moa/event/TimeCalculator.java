package kr.co.moa.event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;

import kr.co.data.DomTimeData;
import kr.co.data.origin.EventData;
import kr.co.moa.DBManager;

public class TimeCalculator {
	private static TimeCalculator instance;
	private static final long minCalcTime = (long) 15; //second
	private static final long maxWaitTime = (long) 5;  //minute
	
	
	public static TimeCalculator getInstance(){
		if(instance == null)	instance = new TimeCalculator();
		return					instance;
	}
	
	private TimeCalculator(){
		
	}

	@SuppressWarnings("unchecked")
	public void calcTime(EventData evt){
		ArrayList<EventData> events = new ArrayList<EventData>();
		ArrayList<Long> eventTimes  = new ArrayList<Long>();

		events = (ArrayList<EventData>) DBManager.getInstnace().getTimeEvent(evt);
		if(events.size() <= 0){
			System.out.println("TimeCalculator : number of event is 0");
			return;
		}
		//Sorting
		Collections.sort(events, new Comparator<EventData>() {
			@Override
			public int compare(EventData o1, EventData o2) {
				return o1.time.compareTo(o2.time);
			}
		});
		
		boolean start = false;
		for(EventData e : events){
			if(!start && !e.type.equals("pagein")) continue;
			start = true;
			eventTimes.add(e.time.getTime());
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
		dd.userid = evt.userid;
		dd.url = evt.url;
		dd.time = events.get(0).time;
		dd.duration = (double) total;

//		System.out.println("userid : " + dd.userid);
//		System.out.println("url : " + dd.url);
//		System.out.println("time : " + dd.time);
//		System.out.println("duration : " + dd.duration);
		
		try {
			DBManager.getInstnace().updateDurationData(dd);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		//total/1000
	}
	
}
