package kr.co.moa.controller.analyzer;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import kr.co.MapUtil;
import kr.co.Util;
import kr.co.data.receive.DateData;
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
		PrintWriter out = response.getWriter();	
		
		String receiveData = request.getParameter("data");	
		System.out.println("/receive/keyword receive: "+ receiveData);
		
		DateData datedata = new Gson().fromJson(receiveData, DateData.class);
		List<String> urls = Util.urlFromTo(datedata);
		System.out.println("urls size:"+urls.size());
		List<Snippet> list_snippet = new ArrayList<Snippet>();
		
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
		
		try {
			SendData sd = new SendData();
			
			Map<String,Double> keywordList = DBManager.getInstnace().getKeywordList(urls, datedata.userid);			
			if(keywordList == null){
				System.out.println("getKeywordList fail. at userid :"+datedata.userid);
				return;
			}
			keywordList = MapUtil.Map_sortByValue(keywordList);
			sd.bChart = new BubbleChart();
			sd.bChart.children = new ArrayList<MainKeywordData>();
			
			int cnt = 20;
			for(String key : keywordList.keySet()){
				if(cnt-->0 )
					sd.bChart.children.add(new MainKeywordData(key, keywordList.get(key)));
				else
					break;
			}
			sd.snippetList = list_snippet;
			String res = new Gson().toJson(sd);
			out.println(res);
			System.out.println("result :"+ res);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
