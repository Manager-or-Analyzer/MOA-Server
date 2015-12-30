package kr.co.moa.keyword.anlyzer.morpheme;

import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
/* 2015-12-30
 * <body> 위주의  CBT완성.
 * 
 */
public class HtmlParser {
	private Queue<Element> que = new LinkedList<Element>();
	//make ContentBlockTree
	
	public void makeCBT(String html, Map<String,String> uselessTag){
		int tagCnt = 0;
		// 1. 특수문자 처리 일단 &nbsp만 처리
		html = html.replaceAll("&nbsp;","").trim();
		Document doc = Jsoup.parse(html);
		System.out.println("title: " +doc.title());
		System.out.println("child size: "+doc.childNodeSize());
		
			
		// 2. <body>만 추출
		Tree CBT = new Tree();		
		Elements body_els = doc.getElementsByTag("body");		
		Element body_e = body_els.first();		
		body_e.tagName("body" + tagCnt++);
		que.add( body_e);
		
		// CBT에 add <body>
		Node body_node = new Node(body_e.tagName(), body_e.text());
		CBT.addNode("ROOT", body_node);
		
		// 3. child 추출
		/*
		 * a. BFS순으로 child 추찰 childsize가 0일때까지
		 * b. description tag제거
		 */
		while(true){
			Element ele = que.poll();
			
			for(Element e : ele.children()){
				if(uselessTag.containsKey(e.tagName()))
					continue;
				e.tagName(e.tagName()+tagCnt++);
				que.add(e);
				//System.out.println("tagaName: "+e.tagName());
				Node n = new Node(e.tagName(), e.text());
				//System.out.println("parent : "+e.parent().tagName());
				CBT.addNode(e.parent().tagName(), n);
			}
			
			//탈출조건
			if(que.size() == 0)
				break;
		}
		CBT.print();
		
		
	}

}
