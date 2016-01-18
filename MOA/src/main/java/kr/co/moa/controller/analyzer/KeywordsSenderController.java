package kr.co.moa.controller.analyzer;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import kr.co.data.parsed.HtmlParsedData;
import kr.co.data.send.BubbleChart;
import kr.co.data.send.MainKeywordData;
import kr.co.data.send.SendData;
import kr.co.data.send.Snippet;
import kr.co.moa.DBManager;

///receive/keyword
public class KeywordsSenderController extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
    
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("application/json");
		response.setCharacterEncoding("utf-8");
		
		String userid = request.getParameter("data");	
		System.out.println("/receive/keyword receive: "+ request.getParameter("data"));
		String keyword = "";
		PrintWriter out = response.getWriter();	
		
		SendData res = new SendData();
		List<Snippet> list_snippet = new ArrayList<Snippet>();
		
//		List<HtmlParsedData> list_hpd = null;
//		try {
//			list_hpd = DBManager.getInstnace().getHtmlParsedDataList(userid, keyword);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		for(HtmlParsedData h : list_hpd){
//			Snippet s = new Snippet();
//			s.title = h.title;
//			s.url = h.url;
//			s.keyword = "세미";
//			s.time = h.time;
//			
//			//System.out.println("img : "+ h.imrsrc);
//			if(h.imrsrc != null && h.imrsrc.equals("/favicon.ico")){
//				String[] tokens = h.url.split("/");
//				s.img="http://";
//				for(int i=2; i<tokens.length-1; i++)
//					s.img += tokens[i];
//				s.img += h.imrsrc;
//				//System.out.println("simg : "+ s.img);
//			}else{
//				s.img = h.imrsrc;
//				//System.out.println("simg : "+ s.img);
//			}
//			list_snippet.add(s);
//		}
//		
		
		for(int i=0; i<10 ;++i){
			Snippet sn = new Snippet();
			sn.keyword = new ArrayList<String>();
			sn.title = "김세미다"+i;
			sn.keyword.add("세미"+i);
			sn.url ="http://www.facebook.com";
			sn.time = "Fri Jan 08 2016 03:31:59 GMT+0900 (대한민국 표준시)";
			if(i==0)
				sn.img = "";
			else if(i%2 == 0)
				sn.img = "http://blog.naver.com/favicon.ico";
			else
				sn.img = "https://fbstatic-a.akamaihd.net/rsrc.php/yl/r/H3nktOa7ZMg.ico";
			
			list_snippet.add(sn);
		}
		
		res.snippetList = list_snippet;
		//dummy
		BubbleChart bchart = new BubbleChart();
		bchart.children = new ArrayList<MainKeywordData>();
		//bchart.
		
		double d = 0.05;
		for(int i=0; i<50; i++){
			MainKeywordData md = new MainKeywordData();
			md.name = "세미"+i;
			md.size = 0.3+d*i;			
			bchart.children.add(md);
		}
//		res.bChart = "{"+
//				   "'name': '모아',"+
//				   "'children': ["+
//				    
//
//				      "{'name': '기린', 'size': 3938},{'name': '사자', 'size': 3812},{'name': '고양이', 'size': 6714},"+
//				      "{'name': '얼룩말', 'size': 743},{'name': 'BetweennessCentrality', 'size': 3534}]"+
//				"}";
//		
//								
		res.bChart = bchart;
		Gson gson = new Gson();
		String ress = gson.toJson(res);
		out.print(ress);
		System.out.println("result :"+ ress);
		System.out.println(keyword);
	}

}
