package kr.co.moa.keyword.anlyzer.morpheme;

import java.util.LinkedList;
import java.util.List;

import kr.co.moa.HomeController;
import scala.collection.concurrent.Debug;
/*
 * 2015-12-25
 * Content Block Tree, Topic Tree를 만들기 위한 노드
 * Remove noise를 하기 위한 과정.
 * 
 * Author by dongyoung  
 * */
public class Node {
	String name;
	private String content;
	List<Node> child_list = new LinkedList<Node>();
	
	public Node(String n, String data){
		name= n;
		content = data;
	}
	public String getContent(){
		return content;
	}	
	public void setChild(Node child){
		if(!child_list.add(child)){
			HomeController.logger.debug("addNode fail, size : "+ child_list.size());
		}		
	}
	public void delChild(String target){
		for(int i=0; i<child_list.size(); i++){
			if(child_list.get(i).name.equals(target)){
				child_list.remove(i);
				return;
			}
		}
	}

}
