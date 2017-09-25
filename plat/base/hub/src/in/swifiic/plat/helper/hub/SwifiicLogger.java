package in.swifiic.plat.helper.hub;

import java.io.IOException;

import java.util.logging.Formatter;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.LogManager;
import java.util.logging.SimpleFormatter;

public final class SwifiicLogger {
    private static final Logger LOGGER = Logger.getLogger(Base.class.getName());
    private static Formatter simpleFormatter = null;
    private static FileHandler fileHandler = null;
	private static final String logDirPath = "/home/nic/logfolder/";

	private SwifiicLogger() {

	}
	public static void logMessage(String className, String message, String filePath) { //make syncronized
		String fullPath = logDirPath + filePath;
	   try {
           fileHandler = new FileHandler(fullPath, true);
           fileHandler.setFormatter(new SimpleFormatter());
           fileHandler.setLevel(Level.ALL);
           LOGGER.addHandler(fileHandler);
       } catch (IOException e) {
           LOGGER.log(Level.SEVERE, "FileHandler Exception", e);
       }
       LOGGER.setLevel(Level.ALL); //take this from a file (or load at runtime);
       LOGGER.info(className + ": " + message);

       try {
           fileHandler.close();
       } catch (SecurityException e) {
           LOGGER.log(Level.SEVERE, "Unable to close file handler!");
       }
   }

}
