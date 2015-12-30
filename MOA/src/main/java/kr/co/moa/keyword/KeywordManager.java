package kr.co.moa.keyword;

public class KeywordManager {

	/*2015-12-30
	 * singleton으로 구현. 
	 * 
	 *  일정한 주기로 키워드 분석 가중치 매기기.
	 * 
	 */
	private static KeywordManager instance;
	
	public KeywordManager getInstance(){
		if(instance == null){
			instance = new KeywordManager();
		}
		return instance;
	}
	
	private KeywordManager(){
		
	}
	
	public void parsingHTML(String html){
		
	}
}
