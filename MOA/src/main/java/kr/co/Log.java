package kr.co;

import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Log {
//	private final static Logger LOGGER = Logger.getLogger("------MOA_server-------");
//	
//	private Handler fileHandler;
//	private Handler consoleHandler;
//	
//	private static Log instance;
//	
//	public static Log getInstance(){
//		if(instance == null)
//			instance = new Log();
//		return instance;
//	}
//	
//	private Log(){
//		try {
//			fileHandler = new FileHandler("C:\\moa.log");
//			consoleHandler = new ConsoleHandler();
//			
//			//LOGGER.addHandler(fileHandler);
//			LOGGER.addHandler(consoleHandler);
//			
//			fileHandler.setLevel(Level.ALL);
//			consoleHandler.setLevel(Level.SEVERE);
//			LOGGER.setLevel(Level.ALL);
//			
//			LOGGER.info("FileRecord Log start");
//		} catch (IOException e) {
//			this.severe(Log.class.getName(), "Error occur in FileHandler"+" :"+e);
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
