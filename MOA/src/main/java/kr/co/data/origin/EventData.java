package kr.co.data.origin;

import java.util.Date;

import kr.co.Util;

public class EventData {
	public String userid;
	public String url;
	public String type;
	public String data;
	public Date time;
	public String x;
	public String y;
	public boolean isUsed;
	
	public EventData(){
		
	}
	public EventData(EventData_deprecated edd){
		this.userid = edd.userid;
		this.url = edd.url;
		this.type = edd.type;
		this.time = Util.strToDate(edd.time);
		this.isUsed = false;
		if(edd.data != null) this.data = edd.data;
		if(edd.x != null) this.x = edd.x;
		if(edd.y != null) this.y = edd.y;
	}
}
