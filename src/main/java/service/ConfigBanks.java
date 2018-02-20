package service;

import org.apache.log4j.Logger;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.LinkedHashMap;

/**
 * @author: OnlyElena@mail.ru
 * Date: 6/18/16
 * Time: 11:54 PM
 */
public class ConfigBanks implements Serializable {

    private static String configFileBanks = "configBanks.txt";

    private static ConfigBanks ourInstanceBanks = new ConfigBanks();

    public static ConfigBanks getInstance() {
        return ourInstanceBanks;
    }

    private ConfigBanks() {
    }


    public static LinkedHashMap<String, String> getPropertiesBanks(String project) {
        LinkedHashMap<String, String> hashMap = readConfigBanks(configFileBanks, project);

        //запишем параметры по умолчанию если их нет
        if (hashMap.size() == 0) {
//            hashMap = getDefaultProperties();
//            writeConfig(configFile, hashMap);
            MyLoggerBanks.get().logMessage("Config"+project, new Date() + " Default config writed: "+ configFileBanks);
//                System.out.println(new Date() + " Default config writed: " + new File(configFile).getAbsolutePath());
            System.out.println(new Date() + " Default config writed: " + configFileBanks);
            Logger.getLogger(ConfigBanks.class).error("Config"+project + "-" + new Date() + " Default config writed: "+ configFileBanks);
        }
        return hashMap;
    }

    private static LinkedHashMap<String, String> readConfigBanks(String file, String project) {
        LinkedHashMap<String, String> res = new LinkedHashMap<String, String>();

        synchronized (ConfigBanks.class) {
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
                MyLoggerBanks.get().logMessage("ConfigBanks"+project, new Date() + " File not found: " + file+"\n"+e.getMessage());
                Logger.getLogger(ConfigBanks.class).error("ConfigBanks"+project + "-" + new Date() + " File not found: " + file+"\n"+e.getMessage());
            } catch (IOException e) {
                System.err.println(new Date() + " " + e.getMessage());
                MyLoggerBanks.get().logMessage("ConfigBanks"+project, new Date() + " " + e.getMessage());
                Logger.getLogger("ConfigBanks"+project + "-" + new Date() + " " + e.getMessage());
            }
        }

        return res;
    }


}
