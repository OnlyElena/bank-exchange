package services;

import org.apache.log4j.Logger;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.LinkedHashMap;
//import java.util.logging.Logger;

/**
 * @author: OnlyElena@mail.ru
 * Date: 3/11/16
 * Time: 1:50 PM
 */
public class Config implements Serializable {

    private static String configFile = "config.txt";

    private static Config ourInstance = new Config();

    public static Config getInstance() {
        return ourInstance;
    }

    private Config() {
    }


    public static LinkedHashMap<String, String> getProperties(String project) {
            LinkedHashMap<String, String> hashMap = readConfig(configFile, project);

            //запишем параметры по умолчанию если их нет
            if (hashMap.size() == 0) {
//            hashMap = getDefaultProperties();
//            writeConfig(configFile, hashMap);
                MyLogger.get().logMessage("Config"+project, new Date() + " Default config writed: "+ configFile);
//                System.out.println(new Date() + " Default config writed: " + new File(configFile).getAbsolutePath());
                System.out.println(new Date() + " Default config writed: " + configFile);
                Logger.getLogger(Config.class).error("Config"+project + "-" + new Date() + " Default config writed: "+ configFile);
            }
            return hashMap;
        }

    private static LinkedHashMap<String, String> readConfig(String file, String project) {
        LinkedHashMap<String, String> res = new LinkedHashMap<String, String>();

        synchronized (Config.class) {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file), Charset.forName("UTF-8")));
                String line = null;

                while ((line = in.readLine()) != null) {
                    int separator = line.indexOf("=");
                    //+1 смещение, что бы пропустить знак равенства
                    res.put(line.substring(0, separator), line.substring(separator + 1));
                }

            } catch (FileNotFoundException e) {
                System.err.println(new Date() + " File not found: " + file);
                MyLogger.get().logMessage("Config"+project, new Date() + " File not found: " + file+"\n"+e.getMessage());
                Logger.getLogger(Config.class).error("Config"+project + "-" + new Date() + " File not found: " + file+"\n"+e.getMessage());
            } catch (IOException e) {
                System.err.println(new Date() + " " + e.getMessage());
                MyLogger.get().logMessage("Config"+project, new Date() + " " + e.getMessage());
                Logger.getLogger("Config"+project + "-" + new Date() + " " + e.getMessage());
            }
        }

        return res;
    }


}
