package kr.co.moa.controller.collector;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Locale;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.mongodb.BasicDBObject;

import kr.co.Log;
import kr.co.Util;
import kr.co.data.origin.EventData;
import kr.co.data.origin.EventData_deprecated;
import kr.co.moa.DBManager;
import kr.co.moa.event.TimeCalculator;
import kr.co.moa.keyword.KeywordManager;

/**
 * Servlet implementation class EventReceiverController
 */
public class EventReceiverController extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String CLASS = "EventReceiverController";
	
	// / moa/send/Event
	protected void doPost(HttpServletRequest request,HttpServletResponse response) throws ServletException, IOException {
		
		PrintWriter out = response.getWriter();

		String eventData = request.getParameter("data");
		if (eventData == null || eventData.equals("")) {
			Log.getInstance().severe(CLASS, "fail");
			out.println("fail");
			return;
		}
		//Log.getInstance().info(CLASS, "eventData: " + eventData);
		
		EventData_deprecated edd = new Gson().fromJson(eventData, EventData_deprecated.class);
		EventData ed = new EventData(edd);
		
		try {
			if (ed.type.equals("drag")) {
				KeywordManager.getInstance().applyEvent(ed);
			} else if (ed.type.equals("tabout" )) {
				TimeCalculator.getInstance().calcTime(ed);
			} else if (ed.type.equals("pageout")) {
				TimeCalculator.getInstance().calcTime(ed);
				ed.isUsed = true;
			} else if(ed.type.equals("tabin")||ed.type.equals("pagein")){
				DBManager.getInstnace().updateTime(edd.url, edd.userid, edd.time);
			}
			BasicDBObject query = new BasicDBObject();
			//BasicDBObject date = new BasicDBObject(
			//		ed.time.toString(), new BasicDBObject("date", true));
			query.put("userid", ed.userid);
			query.put("url", ed.url);
			query.put("type", ed.type);
			query.put("data", ed.data);
			query.put("time", ed.time);
			query.put("x", ed.x);
			query.put("y", ed.y);
			query.put("isUsed", ed.isUsed);
			
			DBManager.getInstnace().insertEventData(query);
			// Log.getInstance().info(CLASS, "DB :insertData success");
		} catch (Exception e) {
			 //Log.getInstance().severe(CLASS, "DB :insertData fail: "+e);
			 e.printStackTrace();
		}
		out.println("success");
	}

}
