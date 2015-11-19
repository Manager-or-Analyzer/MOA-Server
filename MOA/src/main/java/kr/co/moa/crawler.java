package kr.co.moa;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import kr.co.moa.morpheme.MorphemeAnalyzer;

/**
 * Servlet implementation class crawler
 */
public class crawler extends HttpServlet {
	private static final long serialVersionUID = 1L;
   
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		System.out.println("the cralser1");		
		PrintWriter out = response.getWriter();
		//Document doc = Jsoup.connect("http://www.nextree.co.kr/p11205/").get();
		Document doc = Jsoup.connect("http://addio3305.tistory.com/41").get();
		System.out.println("the cralser2");		
		out.println(doc.html());
		System.out.println("the cralser3");		
		
		
		//response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
