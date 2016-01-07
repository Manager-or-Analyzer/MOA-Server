package kr.co.moa.keyword.anlyzer.morpheme;

import java.util.HashMap;
import java.util.Map;

import kr.co.data.HtmlData;

public class MorphemeAnalyzer {

	/*2015-12-30
	 * 1. Remove noise
	 * 2. 형태소 분석 by 메캅
	 * 3. indexing to DB
	 * 
	 * Author by dongyoung  
	 */
	private static MorphemeAnalyzer instance;
	private Map<String,String> TagsMap;
	private Map<String,String> TexttagMap;
	
	 private static final String[] uselessTags = {
	            "script", "noscript", "style", "meta", "link",
	            "noframes", "section", "nav", "aside", "hgroup", "header", "footer", "math",
	            "button", "fieldset", "input", "keygen", "object", "output", "select", "textarea",
	            "img", "br", "wbr", "embed", "hr","col", "colgroup", "command",
	            "device", "area", "basefont", "bgsound", "menuitem", "param", "track","a",
	            "i","aside","embed"
	 };
	 //표는 버린다.ㅋ
	 private static final String[] textTags = {
			 "title", "p", "h1", "h2", "h3", "h4", "h5", "h6", "pre", "address",
	         "ins", "textarea","blockquote", "dt","dd","span","b","font","strong"
	 };
	
	public static MorphemeAnalyzer getInstance(){
		if(instance == null)
			instance = new MorphemeAnalyzer();
		return instance;
	}
	
	private MorphemeAnalyzer(){
		TagsMap = new HashMap<String,String>();
		for(String tag : uselessTags){
			TagsMap.put(tag, null);
		}
		TexttagMap = new HashMap<String,String>();
		for(String tag : textTags){
			TexttagMap.put(tag, null);
		}
	}
	
	public void parsingHTML(HtmlData html){
		HtmlParser hp = new HtmlParser();
		String content = hp.makeCBT(html, TagsMap, TexttagMap).makeTopicTree();
		
		 System.out.println(content);
//		List<LNode> result = Analyzer.parseJava(content);
//		for (LNode term: result) {
//		    System.out.println(term);
//		}
		
		
	}
	
	
	
	
	
	//int i=0;
//	List<TermNode> result = Analyzer.parseJava("�ƹ������濡���Ŵ�.");
//	
//	public void doAnalyze(){
//		for (TermNode term: result) {
//		    System.out.println(term);
//		}
//	}
		
}
