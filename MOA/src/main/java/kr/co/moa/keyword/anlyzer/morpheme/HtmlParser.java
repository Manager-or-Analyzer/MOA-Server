package kr.co.moa.keyword.anlyzer.morpheme;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import kr.co.DebuggingLog;
import kr.co.data.origin.HtmlData;
import kr.co.data.parsed.HtmlParsedData;
/* 2015-12-30
 * <body> 위주의  CBT완성.
 * 람다 값을 정한 후 CBT로 부터 noise를 제거한 TT완성
 * 
 * Author by dongyoung  
 */
public class HtmlParser {
	class Info{
		String parent,child;		
		public Info(String p, String c){
			parent=p; child =c; 
		}
	}
	DebuggingLog debug;
	private Queue<Element> que = new LinkedList<Element>();
	private Queue<Info> del_list = new LinkedList<Info>();
	private Tree CBT,TT;
	
	// 0 < lamda < 1 테스트를 통해 값 수정 필요
	//private static final double lamda = 0.05;
	public double lamda = 0.35;
			
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
//			System.out.println("parent: "+parent.name+" child :"+child.name);
//			System.out.println("false : "+res+"\n");
			return false;
		}
			
		else{
//			System.out.println("parent: "+parent.name+"child :"+child.name);
//			System.out.println("true : "+(len_c/len_p)+"\n");
			return true;
		}
			
	}
	
	public String makeTopicTree(){
		//Tree 초기화
		TT = CBT;
		if(TT == null){
			System.out.println("CBT가 만들어지지 않았습니다.");
			return null;
		}
		TopicTree(TT.getRoot());
		//System.out.println("size :"+del_list.size());
		//System.out.println(TT.getRoot().getContent());
		for(Info info : del_list){
			//System.out.println(info.parent);
			//System.out.println(info.child+"\n\n");
			TT.deleteNode(info.parent, info.child);
		}
		//debug용
//		debug.write("TT----------------------");
//		debug.writeln();
//		TT.print_debug(debug);
//		debug.close();
//		TT.print();
		return TT.getRoot().getContent();
		//System.out.println(TT.getRoot().getContent());
			
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
	
	// 일부 일치를 찾아 주는 함수
	private String findPart(String src, HtmlData hd){
		String str = src.substring(0,(src.length()-1)/2);
		for(HtmlData tmp : hd.children){
			if(tmp.url.contains(str)){
				return tmp.url;
			}
		}
		return findPart(str,hd);
	}
	private void makeChild(Element e, HtmlData hd, Map<String,String> uselessTag, List<Boolean> isUsedList ){
		
		String src = e.attr("src");
		
		// 상대경로 인지 파악하기.
		if(src.startsWith("/")){
			String[] tokens = src.split("redirect");
			boolean flag = false;
			for(HtmlData chd : hd.children){
				flag = false;
				for(String token : tokens){
					//System.out.println("token " + token);
					if(chd.url.contains(token)){
						src = chd.url;
						flag = true;
						break;
					}
				}
				if(flag)
					break;										
			}
			//찾지 못한 경우
			if(!flag){
				src = findPart(src, hd);
			}
			System.out.println("src " + src);
		}		
		int i;
		//System.out.println("src :"+src);
		//System.out.println("size :"+hd.children.size());
		for(i=0; i<hd.children.size(); i++){
			if(hd.children.get(i).url.equals(src) ){
				isUsedList.set(i, true);
				String html = hd.children.get(i).doc;
				html = html.replaceAll("&nbsp;","").trim();
				Document doc = Jsoup.parse(html);
				
				for(Element ee : doc.getAllElements()){
					if(uselessTag.containsKey(ee.tagName())){
						ee.remove();
					}
				}		
				Element body = doc.body();
				// body가 없고 frameset이 있을경우 -> html 엤날 방식.
				if(body == null){
					body = doc.select("frameset").first();
				}
				
				body.tagName(body.tagName()+"child"+i);				
				e.appendChild(body);
				
				///System.out.println(e.outerHtml());
				for(Element ee : body.getAllElements()){
					if(ee.tagName() == "frame" || ee.tagName() == "iframe"){
						//System.out.println("makechild "+ ee.tagName());
						if(ee.ownText().equals("")){
							if(hd.children.size() != 0)
								makeChild(ee,hd,uselessTag,isUsedList);
							else
								System.out.println("makeChild chid size 0");
						}else
							System.out.println("makeChild own text != 0");
					}
				}				
				break;					
			}
			else if(i == hd.children.size()-1){
				//System.out.println("size :"+hd.children.size());
				for(int j=0; j<isUsedList.size(); j++){
					if(!isUsedList.get(j)){
						//System.out.println("isUsedList :"+hd.children.size());
						isUsedList.set(j, true);
						String html = hd.children.get(j).doc;
						html = html.replaceAll("&nbsp;","").trim();
						Document doc = Jsoup.parse(html);
						
						for(Element ee : doc.getAllElements()){
							if(uselessTag.containsKey(ee.tagName())){
								ee.remove();
							}
						}		
						Element body = doc.body();
						// body가 없고 frameset이 있을경우 -> html 엤날 방식.
						if(body == null){
							body = doc.select("frameset").first();
						}
						
						body.tagName(body.tagName()+"child"+i+j);				
						e.appendChild(body);
						
						///System.out.println(e.outerHtml());
						for(Element ee : body.getAllElements()){
							if(ee.tagName() == "frame" || ee.tagName() == "iframe"){
								//System.out.println("makechild "+ ee.tagName());
								if(ee.ownText().equals("")){
									if(hd.children.size() != 0)
										makeChild(ee,hd,uselessTag,isUsedList);
									else
										System.out.println("makeChild chid size 0");
								}else
									System.out.println("makeChild own text != 0");
							}
						}				
					}
				}
			}
		}	
	}
	private String getImagesrc(Document doc){
		
		Elements metaTags = doc.head().select("link");
		if(metaTags.size() != 0){
			for(Element e :metaTags){
				String name = e.attr("rel").toLowerCase();
				if(name.equals("shortcut icon") || name.equals("fluid-icon")){
					return e.attr("href");
				}
			}
		}else
			System.out.println("meta size : 0");
		return null;
	}
	//make ContentBlockTree
	public HtmlParser makeCBT(HtmlData hd, Map<String,String> uselessTag, Map<String,String> TextTag, HtmlParsedData hdp){
		int tagCnt = 0;
		// 1. 특수문자 처리 일단 &nbsp만 처리
		String html = hd.doc;
		html = html.replaceAll("&nbsp;","").trim();
		Document doc = Jsoup.parse(html);
		
		//title 가져오기
		hdp.snippet.title = doc.title();
		//img 가져오기
		String img = getImagesrc(doc);
		if(img != null && img.contains("//")){
			hdp.snippet.img = img;
		}else if(img != null){
			String[] tokens = hd.url.split("/");
			if(img.startsWith("/"))
				img = tokens[0]+"//"+tokens[2]+img;
			else
				img = tokens[0]+"//"+tokens[2]+"/"+img;
			
			hdp.snippet.img = img;
		}
			
		// decription tag 삭제 및 iframe 추가
		for(Element e : doc.getAllElements()){
			if(uselessTag.containsKey(e.tagName())){
				e.remove();
			}
		}
				
		//boolean List 만들기
		List<Boolean> isUsedList = new ArrayList<Boolean>();
		for(int i=0; i<hd.children.size(); i++)
			isUsedList.add(false);
		
		for(Element e : doc.getAllElements()){
			if(e.tagName() == "frame" || e.tagName() == "iframe"){
				if(hd.children.size() != 0)
					makeChild(e,hd,uselessTag, isUsedList);
				else
					System.out.println("chid size 0");
			}
		}
			
		// 2. <body>만 추출
		CBT = new Tree();		
		Elements body_els = doc.getElementsByTag("body");
		// body가 없고 frameset이 있을경우 -> html 엤날 방식.
		if(body_els.isEmpty()){
			body_els = doc.getElementsByTag("frameset");
		}
		Element body_e = body_els.first();		
		//System.out.println("size :"+body_e.child(1).className()+" id: "+body_e.child(1).id());
		body_e.tagName("body" + tagCnt++);
		que.add( body_e);
		
		// CBT에 add <body>
		Node body_node = new Node(body_e.tagName(), body_e.text());
		CBT.addNode("ROOT", body_node);
		//System.out.println("전체------------------------");
		//System.out.println(body_e.text());
		
		// 3. child 추출
		/*
		 * a. BFS순으로 child 추찰 childsize가 0일때까지
		 */
		while(true){
			Element ele = que.poll();
			//System.out.println("size :"+ele.children().size()+"text size:"+ele.textNodes().size());
			for(Element e : ele.children()){
				if(TextTag.containsKey(e.tagName())){
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
		//Debug용 파일 출력
//		debug = new DebuggingLog("TREE");
//		debug.write("CBT----------------------");
//		debug.writeln();
//		CBT.print_debug(debug);
		//CBT.print();	
		return this;
	}
	
			
	void printLog(String msg, Element e){
		System.out.println(msg);
		System.out.println("tagaName: "+e.tagName());
		System.out.println("parent : "+e.parent().tagName());
		System.out.println("class : "+e.className()+"id :"+e.id()+"\n\n");
	}

}
