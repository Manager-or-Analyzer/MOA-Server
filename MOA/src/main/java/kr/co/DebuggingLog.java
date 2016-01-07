package kr.co;

import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DebuggingLog {
//	private final static Logger LOGGER = Logger.getLogger("------MOA_server_debugging-------");
//	
//	private Handler debuggingHandler;
//	private Handler consoleHandler;
//	
//	private static DebuggingLog instance;
//	
//	public static DebuggingLog getInstance(){
//		if(instance == null)
//			instance = new DebuggingLog();
//		return instance;
//	}
//	
//	private DebuggingLog(){
//		try {
//			debuggingHandler = new FileHandler("C:\\moa_debugging.log");
//			consoleHandler = new ConsoleHandler();
//			
//			//LOGGER.addHandler(debuggingHandler);
//			LOGGER.addHandler(consoleHandler);
//			
//			debuggingHandler.setLevel(Level.ALL);
//			consoleHandler.setLevel(Level.ALL);
//			LOGGER.setLevel(Level.ALL);
//			
//			LOGGER.info("DebuggungRecord Log start");
//		} catch (IOException e) {
//			this.severe(DebuggingLog.class.getName(), "Error occur in FileHandler"+" :"+e);
//		}
//	}
//	
//	public void severe(String className, String log){
//		LOGGER.severe(className+" :"+log);
//	}
//	public void warning(String className, String log){
//		LOGGER.warning(className+" :"+log);
//	}
//	public void info(String className, String log){
//		LOGGER.info(className+" :"+log);
//	}
//	

}
