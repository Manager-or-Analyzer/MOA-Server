package kr.co.moa.controller.collector;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.google.gson.Gson;

import kr.co.DebuggingLog;
import kr.co.Log;
import kr.co.TestModule;
import kr.co.data.HtmlData;
import kr.co.moa.DBManager;
import kr.co.moa.keyword.KeywordManager;
import kr.co.moa.keyword.anlyzer.morpheme.MorphemeAnalyzer;

/**
 * Servlet implementation class SendHTML
 */
// url; moa/send/HTML
public class HTMLReceiverController extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String CLASS = "HTMLReceiverController";
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		//Log.getInstance().info(CLASS, "start");
		PrintWriter out = response.getWriter();	
		
		String htmlData = request.getParameter("data");
		//System.out.println(htmlData);
		
		//DebuggingLog.getInstance().info(CLASS, htmlData);		
		HtmlData hd = new Gson().fromJson(htmlData, HtmlData.class);
		
		//System.out.println(hd);
		
		if(!htmlData.equals("") && htmlData != null){
			try {
				DBManager.getInstnace().insertData("HtmlData", htmlData);
				//Log.getInstance().info(CLASS, "DB :insertData success");
				KeywordManager.getInstance().calTF_IDF(hd);
				
			} catch (Exception e) {
				//Log.getInstance().severe(CLASS, "DB :insertData fail");
			}			
			out.println("success");
		}else
			//Log.getInstance().severe(CLASS, "client fails to send htmlData");
			out.println("fail");
	}
}
