package model.logger;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Log {
    // Instance of Java logger, used for logging
    private Logger javaLogger;

    // Singleton instance of log.
    private static Log instance = null;

    /**
     * Private logger constructor, which is construct the moment get() is called.
     */
    private Log() {
        // -------------
        // Logging tools
        // -------------
        try {
            // Get the current date time. This is used to create unique filename
            Date date = new Date() ;
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");

            // Create tmp folders, in case the folder does not yet exist
            File dir = new File("tmp/");
            if (!dir.exists()) dir.mkdirs();

            // This block configure the logger with handler and formatter
            FileHandler fileHandler = new FileHandler("tmp/MainSimulation" + dateFormat.format(date) + ".log");
            javaLogger = Logger.getLogger("MainSimulationLog");
            javaLogger.addHandler(fileHandler);
            SimpleFormatter formatter = new SimpleFormatter();
            fileHandler.setFormatter(formatter);
            javaLogger.info("Initializing MainSimulation logger.");

            // the following statement is used to log any messages
            javaLogger.setUseParentHandlers(false);
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Log method
     */
    public static void info(String s) {
        if (instance == null) {
            instance = new Log();
        }
        instance.javaLogger.info(s);
    }
}
