package kr.co;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class DebuggingLog {
	private	 BufferedWriter out;
	
	public DebuggingLog(String fileName){
		try {
			out = new BufferedWriter(new FileWriter("C:\\"+fileName+".txt"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void write(String content){
		try {
			out.write(content);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	public void writeln(){
		try {
			out.newLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	public void close(){
		try {
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
