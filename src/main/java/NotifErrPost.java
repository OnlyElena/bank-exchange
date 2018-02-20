import jdbc.BeanConnect;
import jdbc.GetBeansConnect;
import org.apache.log4j.Logger;
import services.Config;
import services.MyLogger;
import services.NotifErrPostService;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: OnlyElena@mail.ru
 * Date: 5/13/16
 * Time: 2:17 PM
 */
public class NotifErrPost {
    public static void main(String[] args) {
        new NotifErrPost();
    }

//параметры Управления
LinkedHashMap<String, String> properties;
String ProcessName = "_PostanovlenieNotNotiff";

    public NotifErrPost() {
        Thread.currentThread().setName("Bank-exchange");
        System.out.println(Thread.currentThread());
        process();
    }

    private void process() {
        Date date = new Date(System.currentTimeMillis());
        String ddmmyy = new SimpleDateFormat("ddMMyy").format(date);
        properties = getProperties();


//        Состовляем имя процесса
//        String ProcessName = properties.get("ProcessName") + "_Postanovlenie";

//        Читаем список подключений exProd.xml экспортируем список отделов с параметрами подключения в карту Map
        File file = new File(properties.get("PathExprod"));
        GetBeansConnect getBeansConnect = new GetBeansConnect();
        Map<String, List<BeanConnect>> dataSources = GetBeansConnect.GetBeansConnect(file);
//        Проводим работу последовательно по отделам по карте
        for (List<BeanConnect> dataSource : dataSources.values()) {
//        Состовляем имя процесса
//            String ProcessName = properties.get("ProcessName") + "_Postanovlenie";
            ProcessName = dataSource.get(0).getProcessName() + "_PostanovlenieNotNotiff";

            System.out.println(ProcessName+" Обрабатываем БД "+dataSource.get(0).getBeanId());
            MyLogger.get().logMessage(ProcessName, " Обрабатываем БД " + dataSource.get(0).getBeanId());
            Logger.getLogger(NotifErrPost.class).info(" Обрабатываем БД " + dataSource.get(0).getBeanId());

            NotifErrPostService service = null;
            try {
                service = new NotifErrPostService(dataSource, properties, ddmmyy, ProcessName);
//            создание запросов
                service.processCreate();


            } catch (Exception e) {
//                e.printStackTrace();
                System.out.println(ProcessName+" Исключение в главном классе "+e.getMessage()+" "+e);
                MyLogger.get().logMessage(ProcessName, "Исключение в главном классе "+e.getMessage());
                Logger.getLogger(NotifErrPost.class).error("Исключение в главном классе "+e.getMessage());
            }
        }

    }


    private LinkedHashMap<String, String> getProperties() {
        return Config.getInstance().getProperties(ProcessName);
    }
}
