package kr.co.moa.controller.analyzer;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import kr.co.data.Snippet;

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
		
		String keyword = request.getParameter("data");
		
		PrintWriter out = response.getWriter();	
		Snippet sn = new Snippet();
		sn.title = "김세미다";
		sn.keyword = "세미";
		sn.url ="http://www.facebook.com";
		sn.decription = "나는 김세미다 으하하하"+ keyword;
		sn.time = "2016-01-08 11:05";
		sn.img = "https://fbstatic-a.akamaihd.net/rsrc.php/yl/r/H3nktOa7ZMg.ico";
		
		Gson gson = new Gson();
		String res = gson.toJson(sn);
		out.print(res);
		System.out.println(res);
		System.out.println(keyword);
	}

}
