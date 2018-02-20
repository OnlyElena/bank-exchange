package services;

import org.apache.log4j.Logger;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;

/**
 * @author: OnlyElena@mail.ru
 * Date: 3/14/16
 * Time: 3:24 PM
 */
public class MyLogger implements Serializable {
    private static MyLogger instance = new MyLogger();

    private MyLogger() {
        lastLogMessage = System.currentTimeMillis();
    }

    public static MyLogger get() {
        return instance;
    }

    private long lastLogMessage = 0;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy\\MM\\dd kk:mm");
    private SimpleDateFormat datFormat = new SimpleDateFormat("yyyy-MM-dd");
//    private String Path = Config.getProperties().get("OUTPUT_DIRECTORY");

    public synchronized void logMessage(String name, String text) {
        try {
            long current = System.currentTimeMillis();
            long pause = (current - lastLogMessage) / 1000;
            lastLogMessage = current;
            String s = dateFormat.format(current);
            String date = datFormat.format(current);


//            FileWriter out = new FileWriter(Path+datFormat.format(current)+name+"Log.txt", true);
            FileWriter out = new FileWriter(Config.getProperties(name).get("PathLog") + date + "_" + name + "_Log.log", true);


            out
                    .append(s)
                    .append("\t")
                    .append(pause + "\t")
                    .append(text)
                    .append((char) 10)
                    .append((char) 13);
            out.flush();
            out.close();
        } catch (IOException e) {
            Logger.getLogger(MyLogger.class).error(e);
            e.printStackTrace();
        }
    }
}