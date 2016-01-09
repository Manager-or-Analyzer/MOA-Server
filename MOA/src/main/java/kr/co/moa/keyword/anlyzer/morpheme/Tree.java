package kr.co.moa.keyword.anlyzer.morpheme;
import java.util.ArrayList;
import java.util.List;

import kr.co.DebuggingLog;
/*
+ * 2015-12-26
+ * Content Block Tree, Topic Tree를 만들기 위한 tree
+ * Remove noise를 하기 위한 과정.
+ * 
+ * 무조건 BFS 순 으로 노드가 들어가야 한다. 아니면 에러.
+ * Author by dongyoung  
+ * */
public class Tree {
	private class Cash{
		int index;
		String name;
		
		public Cash(int i, String n){
			index = i;
			name = n;
		}
	};
	
	private Node root;
	private List<Cash> cashing;
	
	public Tree(){
		root = new Node("ROOT"," ");
		cashing = new ArrayList<Cash>();
	}
	public Node getRoot(){
		return root;
	}
	public void addNode(String parent, Node child){
		//parent가 root일때
		if(parent.equals("ROOT")){
			root.setChild(child);
			root.setContent(child.getContent());
		}else{
			//parent를 찾는다.
			Node p = findNode(root, parent);			
			p.setChild(child);
		}	
	}
	private Node findNode(Node node, String parent){
		if(parent.equals(node.name)){
			return node;
		}
		Node tmp;
		for(Node child : node.child_list){
			tmp = findNode(child,parent);
			if(tmp != null) return tmp;
		}
		return null;
	}
//	private Node findNode(Node root, String parent){
//		//cashing 있을떄
//		if(cashing.size() > 0 && parent.equals(cashing.get(cashing.size()-1).name)){
//			return this.getNode(root, 0);
//		}else{			
//			//cashing 마지막 인자 확인
//			if(cashing.size() == 0){
//				makeCash(root,parent);
//			}else{
//				//다음 level이 생기는 경우
//				
//				Node node = getNode(root, 0);			
//				for(Node n : node.child_list){
//					if(parent.equals(n.name)){
//						makeCash(node,parent);
//						//test_print();
//						return this.getNode(root, 0);
//					}
//				}
//				//cashFault	발생.
//				//body+ 1 만 남아 있을때.
//				if(cashing.size() == 2){
//					Cash c= cashing.remove(cashing.size()-1);
//					
//					
//					
//				}
//				cashing.remove(cashing.size()-1);				
//				node = getNode(root, 0);
//				for(Node n : node.child_list){
//					if(parent.equals(n.name)){
//						makeCash(node,parent);
//						//test_print();
//						return this.getNode(root, 0);
//					}
//				}
//				//
//				//부모가 빠져 있는 경우
//				if(subCashing.size() == 0){
//					System.err.println("subcashing err");
//					return null;
//				}
//				cashing.add(subCashing.poll());
//				return findNode(root, parent);
//			}	
//			return this.getNode(root, 0);
//		}							
//	}
	private void makeCash(Node node, String parent){
		for(int i=0; i<node.child_list.size(); i++){
			if(node.child_list.get(i).name.equals(parent)){
				Cash c = new Cash(i,parent);
				cashing.add(c);
				return;
			}
		}
	}
	private Node getNode(Node node, int idx){
		try{
			if(idx == cashing.size()-1){
				return node.child_list.get(cashing.get(idx).index);
			}
			node = node.child_list.get(cashing.get(idx).index);
			return getNode(node, idx+1);
		}catch(Exception e) {
			System.out.println(node.name+" idx: "+idx);
			print();
			test_print();
			return null;
		}
		
	}	
	public void deleteNode(String parent, String target){
		//parent가 root일때
		if(parent.equals("ROOT")){			
			root.delChild(target,"");
			root.setContent("");
		}else{
			//parent를 찾는다.
			Node p = findDelNode(root, parent);
			if(p == null){
				System.err.println("findDelNode err: " + parent);
				return;
			}
			String tmp = p.delChild(target,root.getContent());
			root.setContent(tmp);
		}	
	}	
	private Node findDelNode(Node node, String parent){
		if(parent.equals(node.name)){
			return node;
		}
		Node tmp;
		for(Node child : node.child_list){
			tmp = findDelNode(child,parent);
			if(tmp != null) return tmp;
		}
		return null;
	}
	private void test_print(){
		System.out.println("size :"+cashing.size());
		for(Cash c: cashing){
			System.out.println(c.name+ " " + c.index);
		}					
	}
	public void print(){
		this.print_tree(root, 0);
		//this.print_tree_data(root,0);
		
	}
	public void print_debug(DebuggingLog debug){
		this.print_tree_debug(root, 0, debug);
	}
	public void print_tree_debug(Node node, int level, DebuggingLog debug){
		if(node.child_list.size() == 0)
			return;
		if(level == 0){
			debug.write(node.name+" level:" +level+++" ");
			debug.writeln();
		}
		for(Node n : node.child_list){
			debug.write(n.name+" ");
		}
		debug.write(" level:"+level);
		debug.writeln();
		for(Node n : node.child_list){
			if(n.child_list.size() !=0 ){
				for(int i=0; i<level; i++)
					debug.write("\t");
				debug.write(n.name+" child: ");
			}
				
			print_tree_debug(n,level+1, debug);
		}
	}
	public void print_tree(Node node, int level){
		if(node.child_list.size() == 0)
			return;
		if(level == 0)System.out.println(node.name+" level:" +level+++" ");
		for(Node n : node.child_list){
			System.out.print(n.name+" ");
		}
		System.out.print(" level:"+level);
		System.out.println("");
		for(Node n : node.child_list){
			if(n.child_list.size() !=0 ){
				for(int i=0; i<level; i++)
					System.out.print("\t");
				System.out.print(n.name+" child: ");
			}
				
			print_tree(n,level+1);
		}
	}
	public void print_tree_data(Node node, int level){
		if(node.child_list.size() == 0)
			return;
		if(level == 0)System.out.println(node.getContent()+" ");
		for(Node n : node.child_list){
			System.out.print(node.getContent()+" ");
		}
		System.out.println("");
		for(Node n : node.child_list){
			if(n.child_list.size() !=0 ){
				for(int i=0; i<level; i++)
					System.out.print("\t");
				System.out.print(node.getContent()+" ");
			}
				
			print_tree_data(n,level+1);
		}
	}

}
