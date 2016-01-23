package kr.co.moa.controller.analyzer;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import kr.co.MapUtil;
import kr.co.Util;
import kr.co.data.TF_IDF;
import kr.co.data.receive.DateData;
import kr.co.data.send.BubbleChart;
import kr.co.data.send.MainKeywordData;
import kr.co.data.send.SendData;
import kr.co.data.send.Snippet;
import kr.co.moa.DBManager;

// url : moa/receive/keyword
public class KeywordsSenderController extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
	public static class Dictionary_custom{
		public Map<String,Double> keywordList;
		public List<TF_IDF> keyCollections;
	}
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("application/json");
		response.setCharacterEncoding("utf-8");
		request.setCharacterEncoding("utf-8");

		PrintWriter out = response.getWriter();	
		
		String receiveData = request.getParameter("data");	
		System.out.println("/receive/keyword receive: "+ receiveData);
		
		DateData datedata = new Gson().fromJson(receiveData, DateData.class);
		List<String> urls = DBManager.getInstnace().getUrls(datedata);
		System.out.println("urls size:"+urls.size());

		
		try {
			SendData sd = new SendData();
			
			Dictionary_custom dc = DBManager.getInstnace().getKeywordList(urls, datedata.userid, datedata);
			//Map<String,Double> keywordList = DBManager.getInstnace().getKeywordList(urls, datedata.userid);			
			if(dc.keywordList == null || dc.keyCollections == null){
				System.out.println("getKeywordList fail. at userid :"+datedata.userid);
				return;
			}
			dc.keywordList = MapUtil.Map_sortByValue(dc.keywordList);
			sd.bChart = new BubbleChart();
			sd.bChart.children = new ArrayList<MainKeywordData>();
			
			for(String key : dc.keywordList.keySet()){
				System.out.println("key :"+ key);
				sd.bChart.children.add(new MainKeywordData(key, dc.keywordList.get(key)));
			}
			sd.snippetList = dc.keyCollections;
			//sd.snippetList = list_snippet;
			String res = new Gson().toJson(sd);
			out.println(res);
			System.out.println("result :"+ res);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
