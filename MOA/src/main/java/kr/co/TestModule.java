package kr.co;

import java.io.IOException;

import scala.Enumeration.Val;
import scala.io.Codec;
import java.io.UnsupportedEncodingException;
import java.nio.charset.CodingErrorAction;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.bitbucket.eunjeon.seunjeon.Analyzer;
import org.bitbucket.eunjeon.seunjeon.LNode;
import org.jsoup.Jsoup;

import com.google.gson.Gson;

import kr.co.data.HtmlData;
import kr.co.moa.DBManager;
import kr.co.moa.keyword.anlyzer.morpheme.MorphemeAnalyzer;
import kr.co.moa.keyword.anlyzer.morpheme.Tree;
import scala.io.Source;

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
		HtmlData hd = null;
		try {
			hd = DBManager.getInstnace().getData("HtmlData");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//hd.url = "http://addio3305.tistory.com/41";
		//hd.url = "http://news.naver.com/main/read.nhn?oid=008&sid1=103&aid=0003606076&mid=shm&cid=428288&mode=LSD&nh=20151231163858";
		//hd.url ="http://gogorchg.tistory.com/entry/Android-javautilconcurrentmodificationexception";
		//hd.url="http://osen.mt.co.kr/article/G1110323676";
		//hd.url="http://www.reversecore.com/38";
//		try {
//			//hd.html = Jsoup.connect(hd.url).get().toString();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		
		MorphemeAnalyzer.getInstance().parsingHTML(hd);

		
	}
	
//	
//	public static String SendHTML(){
//		Gson gson = new Gson();
//		HtmlData hd = new HtmlData();
//		hd.url = "http://addio3305.tistory.com/41";
//		try {
//			hd.html = Jsoup.connect(hd.url).get().toString();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		hd.userId = "chemicalatom";
//		
//		SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat ( "yyyy.MM.dd HH:mm:ss", Locale.KOREA );
//		Date currentTime = new Date ( );
//		String mTime = mSimpleDateFormat.format ( currentTime );
//		hd.time = mTime;
//		
//		String json = gson.toJson(hd);
//		return json;		
//	}
}
