package kr.co.moa.event;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import kr.co.data.EventData;
import kr.co.moa.DBManager;

public class TimeCalculator {
	private static TimeCalculator instance;
	
	public static TimeCalculator getInstance(){
		if(instance == null)	instance = new TimeCalculator();
		return					instance;
	}
	
	private TimeCalculator(){
		
	}

	public void calcTime(EventData pageout) {
		ArrayList<EventData> events = 
				(ArrayList<EventData>) DBManager.getInstnace().getTimeEvent(pageout);
		
		//Sorting
		Collections.sort(events, new Comparator<EventData>() {
			@Override
			public int compare(EventData o1, EventData o2) {
				SimpleDateFormat sdf = new SimpleDateFormat("E MM dd yyyy HH:mm:ss");
				Date d1 = null; 
				Date d2 = null;
				try {
					d1 = sdf.parse(o1.time);
					d2 = sdf.parse(o2.time);
				} catch (ParseException e) {
					e.printStackTrace();
				}
				return d1.compareTo(d2);
			}
		});

	}
}
