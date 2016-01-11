package kr.co.moa.controller.collector;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import kr.co.DebuggingLog;
import kr.co.Log;
import kr.co.data.EventData;
import kr.co.data.HtmlData;
import kr.co.moa.DBManager;
import kr.co.moa.event.TimeCalculator;
import kr.co.moa.keyword.anlyzer.morpheme.MorphemeAnalyzer;

/**
 * Servlet implementation class EventReceiverController
 */
public class EventReceiverController extends HttpServlet {
	private static final long serialVersionUID = 1L;       	
	private static final String CLASS = "EventReceiverController";
	
	/// moa/send/Event
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		//Log.getInstance().info(CLASS, "start");
		PrintWriter out = response.getWriter();	
		
		String eventData = request.getParameter("data");
		
		EventData ed = new Gson().fromJson(eventData, EventData.class);
		ed.isUsed = false;
		//DebuggingLog.getInstance().info(CLASS, eventData);				
		if(!eventData.equals("") && eventData != null){
			try {
				if(ed.type.equals("drag"))
					MorphemeAnalyzer.getInstance().parsingEvent(ed);
				else if(ed.type.equals("pageout"))
					TimeCalculator.getInstance().calcTime(ed);
				DBManager.getInstnace().insertData("EventData", new Gson().toJson(ed));
				//Log.getInstance().info(CLASS, "DB :insertData success");
			} catch (Exception e) {
				//Log.getInstance().severe(CLASS, "DB :insertData fail");
			}
			out.println("success");
		}else{
			//Log.getInstance().severe(CLASS, "client fails to send eventData");
			out.println("fail");		
		}
	}	
		
}
