package kr.co.moa;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class test1
 */

public class test1 extends HttpServlet {
	private static final long serialVersionUID = 1L;
        
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		PrintWriter out = response.getWriter();
		mongoDB md = new mongoDB();
		
		try {
			out.print("mongo start");
			md.mongoTest("localhost", 27017, "test");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			out.print(e);
		}
		
		out.print("heheheheeh");
		
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
