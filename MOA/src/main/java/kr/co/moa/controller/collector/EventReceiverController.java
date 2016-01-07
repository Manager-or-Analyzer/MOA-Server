package kr.co.moa.controller.collector;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import kr.co.DebuggingLog;
import kr.co.Log;
import kr.co.moa.DBManager;

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
		//DebuggingLog.getInstance().info(CLASS, eventData);				
		if(!eventData.equals("") && eventData != null){
			try {
				//DBManager.getInstnace().insertData("EventData", eventData);
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
