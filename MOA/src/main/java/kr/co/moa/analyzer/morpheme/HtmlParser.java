package kr.co.moa.analyzer.morpheme;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class HtmlParser {
	
	//make ContentBlockTree
	public static void makeCBT(String html){
		Document doc = Jsoup.parse(html);
		System.out.println(doc.title());
		System.out.println(doc.childNodeSize());
		
		for(Element e : doc.getAllElements()){
			System.out.println(e.tagName());
			if(e.parent() != null){
				System.out.println("parent :" + e.parent().tagName());
			}
		}
		
		//Element ele = doc.child(1);
		
		//System.out.println(ele.tagName());
		//System.out.println(ele.html());
		//System.out.println(ele.outerHtml());
			
		
	}

}
