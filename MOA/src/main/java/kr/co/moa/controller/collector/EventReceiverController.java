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
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		final SimpleDateFormat sdf = new SimpleDateFormat(
				"EEE MMM dd yyyy HH:mm:ss", Locale.UK);
		// Log.getInstance().info(CLASS, "start");
		PrintWriter out = response.getWriter();

		String eventData = request.getParameter("data");
		if (eventData == null || eventData.equals("")) {
			out.println("fail");
			return;
		}

		System.out.println("eventData: " + eventData);
		EventData_deprecated edd = new Gson().fromJson(eventData, EventData_deprecated.class);
		//EventData ed = new Gson().fromJson(eventData, EventData.class);
		EventData ed = new EventData();
		ed.userid = edd.userid;
		ed.url = edd.url;
		ed.type = edd.type;
		ed.time = Util.strToDate(edd.time);
		ed.isUsed = false;
		if(edd.data != null) ed.data = edd.data;
		if(edd.x != null) ed.x = edd.x;
		if(edd.y != null) ed.y = edd.y;

		try {
			if (ed.type.equals("drag")) {
				KeywordManager.getInstance().applyEvent(ed);
			} else if (ed.type.equals("tabout" )) {
				TimeCalculator.getInstance().calcTime(ed);
			} else if (ed.type.equals("pageout")) {
				TimeCalculator.getInstance().calcTime(ed);
				ed.isUsed = true;
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
			// Log.getInstance().severe(CLASS, "DB :insertData fail");
		}
		out.println("success");
	}

}
