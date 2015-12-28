package kr.co.moa.collector;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.google.gson.Gson;

import kr.co.TestModule;
import kr.co.data.HtmlData;
import kr.co.moa.DBManager;

/**
 * Servlet implementation class SendHTML
 */
public class SendHTML extends HttpServlet {
	private static final long serialVersionUID = 1L;
          
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		System.out.println("the sendHtml");		
		//PrintWriter out = response.getWriter();	
		
		String hd = request.getParameter("data");
				
		//String test = TestModule.SendHTML();
		
		//System.out.println(test);
		
		/*		 
		  client로부터 데이터를 잘받아오는지에 대한 체크		 			  
		  2015 12-14
	     */
		System.out.println("hd0");
		if(!hd.equals("") && hd != null){
			System.out.println("hd1");
			try {
				DBManager.getInstnace().insertData("HtmlData", hd);
				
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
