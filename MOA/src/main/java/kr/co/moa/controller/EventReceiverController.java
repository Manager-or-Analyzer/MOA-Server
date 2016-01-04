package kr.co.moa.controller;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import kr.co.moa.DBManager;

/**
 * Servlet implementation class EventReceiverController
 */
public class EventReceiverController extends HttpServlet {
	private static final long serialVersionUID = 1L;       	
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	///send/Event
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		System.out.println("Event collector");				
		String hd = request.getParameter("data");
						
		if(!hd.equals("") && hd != null){
			System.out.println("hd1");
			try {
				DBManager.getInstnace().insertData("EventData", hd);				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("hd2");
			try {
				//DBManager.getInstnace().getData("HtmlData");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println(hd);
			System.out.println("success");
		}else
			System.out.println("fail");				
	}	
}
