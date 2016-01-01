package kr.co.moa.keyword.anlyzer.morpheme;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
/* 2015-12-30
 * <body> 위주의  CBT완성.
 * 람다 값을 정한 후 CBT로 부터 noise를 제거한 TT완성
 */
public class HtmlParser {
	class Info{
		String parent,child;		
		public Info(String p, String c){
			parent=p; child =c; 
		}
	}
	private Queue<Element> que = new LinkedList<Element>();
	private Queue<Info> del_list = new LinkedList<Info>();
	private Tree CBT,TT;
	
	// 0 < lamda < 1 테스트를 통해 값 수정 필요
	private static final double lamda = 0.4;	
			
	// sgn(len(ti)/len(t)*1/lamda -1)
	private boolean isTopicNode(Node parent, Node child){
		
		double len_p = parent.getContent().trim().length();
		double len_c = child.getContent().trim().length();
		
		if(len_p == 0 || len_c == 0){
			//System.out.println("0 parent: "+parent.name+" child :"+child.name);
			//System.out.println("p: "+len_p+" c: "+len_c);
			return false;
		}
			
		
		double res = ((len_c/len_p)/lamda)-1;
		if(res <1){
			//System.out.println("parent: "+parent.name+"child :"+child.name);
			//System.out.println("false : "+res);
			return false;
		}
			
		else{
			//System.out.println("true : "+(len_c/len_p));
			return true;
		}
			
	}
	
	public void makeTopicTree(){
		//Tree 초기화
		TT = CBT;
		if(TT == null){
			System.out.println("CBT가 만들어지지 않았습니다.");
			return;
		}
		TopicTree(TT.getRoot());
		System.out.println("size :"+del_list.size());
		//System.out.println(TT.getRoot().getContent());
		for(Info info : del_list){
			//System.out.println(info.parent);
			//System.out.println(info.child+"\n\n");
			TT.deleteNode(info.parent, info.child);
		}
		System.out.println(TT.getRoot().getContent());
		//TT.print();
		
	}	
	private void TopicTree(Node n){
		if(n!= null && n.child_list.size() != 0){
			List<Integer> tmp = new LinkedList<Integer>();
			for(int i=0; i<n.child_list.size(); i++){
				Node child = n.child_list.get(i);
				if(!isTopicNode(n,child)){
					del_list.add(new Info(n.name, child.name));
					tmp.add(i);					
				}
			}
			for(int i=0; i<n.child_list.size(); i++){	
				boolean flag = false;
				for(int j=0; j<tmp.size(); j++){
					if(i == tmp.get(j)){
						flag =true;
						break;
					}
				}
				if(flag)
					continue;
				
				Node child = n.child_list.get(i);
				TopicTree(child);				
			}			
		}
	}
	
	//make ContentBlockTree
	public HtmlParser makeCBT(String html, Map<String,String> uselessTag, Map<String,String> TextTag){
		int tagCnt = 0;
		// 1. 특수문자 처리 일단 &nbsp만 처리
		html = html.replaceAll("&nbsp;","").trim();
		Document doc = Jsoup.parse(html);
		// decription tag 삭제
		for(Element e : doc.getAllElements()){
			if(uselessTag.containsKey(e.tagName())){
				e.remove();
			}
		}
			
		// 2. <body>만 추출
		CBT = new Tree();		
		Elements body_els = doc.getElementsByTag("body");		
		Element body_e = body_els.first();		
		//System.out.println("size :"+body_e.child(1).className()+" id: "+body_e.child(1).id());
		body_e.tagName("body" + tagCnt++);
		que.add( body_e);
		
		// CBT에 add <body>
		Node body_node = new Node(body_e.tagName(), body_e.text());
		CBT.addNode("ROOT", body_node);
		
		// 3. child 추출
		/*
		 * a. BFS순으로 child 추찰 childsize가 0일때까지
		 */
		while(true){
			Element ele = que.poll();
			//System.out.println("size :"+ele.children().size()+"text size:"+ele.textNodes().size());
			for(Element e : ele.children()){
				if(TextTag.containsKey(e.tagName())){
					//printLog("useless",e);					
					continue;
				}
					
				e.tagName(e.tagName()+tagCnt++);
				que.add(e);				
				Node n = new Node(e.tagName(), e.text());				
				CBT.addNode(e.parent().tagName(), n);
				//printLog("use",e);
			}
			
			//탈출조건
			if(que.size() == 0)
				break;
		}
		CBT.print();	
		return this;
	}
	void printLog(String msg, Element e){
		System.out.println(msg);
		System.out.println("tagaName: "+e.tagName());
		System.out.println("parent : "+e.parent().tagName());
		System.out.println("class : "+e.className()+"id :"+e.id()+"\n\n");
	}

}
