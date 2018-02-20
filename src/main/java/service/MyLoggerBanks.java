package service;

import org.apache.log4j.Logger;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;

/**
 * @author: OnlyElena@mail.ru
 * Date: 6/18/16
 * Time: 11:51 PM
 */
public class MyLoggerBanks implements Serializable {
    private static MyLoggerBanks instanceBanks = new MyLoggerBanks();

    private MyLoggerBanks() {
        lastLogMessageBanks = System.currentTimeMillis();
    }

    public static MyLoggerBanks get() {
        return instanceBanks;
    }

    private long lastLogMessageBanks = 0;

    private SimpleDateFormat dateFormatBanks = new SimpleDateFormat("yyyy\\MM\\dd kk:mm");
    private SimpleDateFormat datFormatBanks = new SimpleDateFormat("yyyy-MM-dd");
//    private String Path = Config.getProperties().get("OUTPUT_DIRECTORY");

    public synchronized void logMessage(String name, String text) {
        try {
            long current = System.currentTimeMillis();
            long pause = (current - lastLogMessageBanks) / 1000;
            lastLogMessageBanks = current;
            String s = dateFormatBanks.format(current);
            String date = datFormatBanks.format(current);


//            FileWriter out = new FileWriter(Path+datFormat.format(current)+name+"Log.txt", true);
            FileWriter out = new FileWriter(ConfigBanks.getPropertiesBanks(name).get("PathLog") + date + "_" + name + "_Log.log", true);


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
            Logger.getLogger(MyLoggerBanks.class).error(e);
            e.printStackTrace();
        }
    }
}