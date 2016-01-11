package kr.co.moa.controller.analyzer;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import kr.co.data.HtmlData;
import kr.co.moa.DBManager;
import kr.co.moa.keyword.anlyzer.morpheme.MorphemeAnalyzer;

import com.google.gson.Gson;

/**
 * Servlet implementation class SearchingDocumentController
 */
///searching
public class SearchingDocumentController extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SearchingDocumentController() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PrintWriter out = response.getWriter();	
		String searches = request.getParameter("data");		
		
		if(searches.equals("") && searches == null) out.println("fail");			
		else{
			try {
				DBManager.getInstnace().getBaseDocumentKeywords(searches);
				
			} catch (Exception e) {
				//DB exception
			}			
			out.println("success");
		}
	}

}
