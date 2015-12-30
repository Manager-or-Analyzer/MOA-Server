package kr.co;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.jsoup.Jsoup;

import com.google.gson.Gson;

import kr.co.data.HtmlData;
import kr.co.moa.keyword.anlyzer.morpheme.MorphemeAnalyzer;
import kr.co.moa.keyword.anlyzer.morpheme.Tree;

public class TestModule {
	public static void TestTree(){
		Tree t = new Tree();
//		Node body = new Node("body");
//		Node n1 = new Node("n1");
//		Node n2 = new Node("n2");
//		Node n3 = new Node("n3");
//		Node c1n1 = new Node("c1n1");
//		Node c2n1 = new Node("c2n1");
//		Node c1n2 = new Node("c1n2");
//		Node c2n2 = new Node("c2n2");
//		
//		t.addNode("ROOT", body);
//		t.addNode("body", n1);
//		t.addNode("body", n2);
//		t.addNode("body", n3);
//		t.addNode("n1", c1n1);
//		t.addNode("n1", c2n1);
//		t.addNode("n1", new Node("c3n1"));
//		t.addNode("n1", new Node("c4n1"));
//		t.addNode("n1", new Node("c5n1"));
//		t.addNode("n2", c1n2);
//		t.addNode("n2", c2n2);
//		t.addNode("n3", new Node("c1n3"));
//		t.addNode("n3", new Node("c2n3"));
//		t.addNode("c1n1", new Node("<div>"));
//		t.addNode("c2n1", new Node("<div1>"));
//		t.addNode("<div1>", new Node("<p1>"));
//		t.addNode("<div1>", new Node("<p2>"));
//		t.addNode("<div1>", new Node("<p3>"));
//		t.deleteNode("<div1>", "<p1>");
		t.deleteNode("body", "n2");
		t.deleteNode("body", "n1");
		
		t.print();			
	}
	
	public static void Test_makeCBT(){
		Gson gson = new Gson();
		HtmlData hd = new HtmlData();
		hd.url = "http://addio3305.tistory.com/41";
		try {
			hd.html = Jsoup.connect(hd.url).get().toString();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
				
		MorphemeAnalyzer.getInstance().parsingHTML(hd.html);
		//HtmlParser hp = new HtmlParser();
		//hp.makeCBT(hd.html);
		
	}
	
	
	public static String SendHTML(){
		Gson gson = new Gson();
		HtmlData hd = new HtmlData();
		hd.url = "http://addio3305.tistory.com/41";
		try {
			hd.html = Jsoup.connect(hd.url).get().toString();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		hd.userId = "chemicalatom";
		
		SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat ( "yyyy.MM.dd HH:mm:ss", Locale.KOREA );
		Date currentTime = new Date ( );
		String mTime = mSimpleDateFormat.format ( currentTime );
		hd.time = mTime;
		
		String json = gson.toJson(hd);
		return json;		
	}
}
