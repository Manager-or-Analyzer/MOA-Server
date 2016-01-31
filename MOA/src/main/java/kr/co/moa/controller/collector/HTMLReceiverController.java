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
import kr.co.data.origin.HtmlData;
import kr.co.moa.DBManager;
import kr.co.moa.keyword.KeywordManager;
import kr.co.moa.keyword.anlyzer.morpheme.MorphemeAnalyzer;

/**
 * Servlet implementation class SendHTML
 */
// url; /moa/send/HTML
public class HTMLReceiverController extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String CLASS = "HTMLReceiverController";
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("utf-8");
		PrintWriter out = response.getWriter();	
		String htmlData = request.getParameter("data");
		
		HtmlData hd = new Gson().fromJson(htmlData, HtmlData.class);
		
		if(htmlData == null || htmlData.equals("") || hd.userid.equals("")){
			Log.getInstance().severe(CLASS, "client fails to send htmlData");
			System.out.println("fail");
			out.println("fail");
			return;
		}

		System.out.println("htmlreceivercontroller : " + htmlData);
		System.out.println("htmlreceivercontroller : " + hd.userid);
		System.out.println("htmlreceivercontroller : " + hd.url);
		
		try {
			DBManager.getInstnace().insertData("HtmlData", htmlData);				
			KeywordManager.getInstance().calTF_IDF(hd);
			//KeywordManager.getInstance().calTest(hd);
		} catch (Exception e) {
			Log.getInstance().severe(CLASS, "DB :insertData fail : "+e);
		}			
		out.println("success");
	}
}
