package in.swifiic.plat.helper.hub;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
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
	private static String logDirPath = null; // Set the directory where you want to log files by modifying logging.properties

// We make this syncrhonized as many processes might try running the static block at once
    static {
        synchronized (SwifiicLogger.class) {
            String base = System.getenv("SWIFIIC_HUB_BASE");
            String propertiesPath;
            if (null != base) {
                propertiesPath = base + "/properties/";
                try {
                    FileInputStream fis = new FileInputStream(propertiesPath + "logging.properties");
                    Properties loggingProperties = new Properties();

                    loggingProperties.load(fis);
                    String logFolder = loggingProperties.getProperty("logfolder");
                    logDirPath = logFolder;

                    File directory = new File(logDirPath);
                    if (!directory.exists()) {
                        directory.mkdirs();
                        directory.setExecutable(true);
                        directory.setReadable(true);
                        directory.setWritable(true);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    synchronized public static void logMessage(String className, String message, String filePath) { //make syncronized
       if (logDirPath != null) {
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

}
