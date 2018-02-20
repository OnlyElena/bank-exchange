import jdbc.BeanConnect;
import jdbc.GetBeansConnect;
import org.apache.log4j.Logger;
import services.RequestService;
import services.Config;
import services.MyLogger;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: OnlyElena@mail.ru
 * Date: 4/15/16
 * Time: 7:52 PM
 */
public class CreateRequests {

    public static void main(String[] args) {
        new CreateRequests();
    }

    //параметры Управления
    LinkedHashMap<String, String> properties;
    String ProcessName = "_Requests";

    public CreateRequests() {
        Thread.currentThread().setName("Bank-exchange");
        System.out.println(Thread.currentThread());
        process();
    }

    private void process() {
        Date date = new Date(System.currentTimeMillis());
        String dd = new SimpleDateFormat("dd").format(date);
        properties = getProperties();

//        Читаем список подключений exProd.xml экспортируем список отделов с параметрами подключения в карту Map
        File file = new File(properties.get("PathExprod"));
        GetBeansConnect getBeansConnect = new GetBeansConnect();
        Map<String, List<BeanConnect>> dataSources = getBeansConnect.GetBeansConnect(file);
//        Проводим работу последовательно по отделам по карте
        for (List<BeanConnect> dataSource : dataSources.values()) {
//        Состовляем имя процесса
//            String ProcessName = properties.get("ProcessName") + "_Postanovlenie";
            ProcessName = dataSource.get(0).getProcessName() + "_Requests";
            RequestService service = null;

            try {
                service = new RequestService(dataSource, properties, dd, ProcessName);
//            создание запросов
                service.processCreate();


            } catch (Exception e) {
//                e.printStackTrace();
                //в случае возникновения исключительной ситуации, удаляем сформированные файлы
                System.err.println(e.getMessage());
                MyLogger.get().logMessage(ProcessName , e.getMessage());
                if (service != null)
                    service.deleteCreatedFiles();
                System.out.println("Исключение в главном классе "+e.getMessage());
                MyLogger.get().logMessage(ProcessName, "Исключение в главном классе "+e.getMessage());
                Logger.getLogger(CreateRequests.class).error("Исключение в главном классе "+e.getMessage());

            }
        }
    }


    private LinkedHashMap<String, String> getProperties() {
        return Config.getInstance().getProperties(ProcessName);
    }
}
