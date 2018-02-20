import jdbc.BeanConnectBanks;
import jdbc.GetBeansConnectBanks;
import org.apache.log4j.Logger;
import service.ConfigBanks;
import service.MyLoggerBanks;
import service.RequestServiceBanks;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: OnlyElena@mail.ru
 * Date: 6/18/16
 * Time: 10:17 PM
 */
public class CreateRequestBanks {
    public static void main(String[] args) {
        new CreateRequestBanks();
    }

    //параметры Управления
    LinkedHashMap<String, String> propertiesBanks;
    String ProcessNameBanks = "_Requests";

    public CreateRequestBanks() {
        Thread.currentThread().setName("Banks-exchange");
        System.out.println(Thread.currentThread());
        proces();
    }

    private void proces() {
        Date date = new Date(System.currentTimeMillis());
        String dd = new SimpleDateFormat("dd").format(date);
        propertiesBanks = getProperties();

//        Читаем список подключений exProd.xml экспортируем список отделов с параметрами подключения в карту Map
        File file = new File(propertiesBanks.get("PathExprod"));
        GetBeansConnectBanks getBeansConnectBanks = new GetBeansConnectBanks();
        Map<String, List<BeanConnectBanks>> dataSourcesBanks = getBeansConnectBanks.GetBeansConnect(file);
//        Проводим работу последовательно по отделам по карте
        for (List<BeanConnectBanks> dataSourceBanks : dataSourcesBanks.values()) {
//        Состовляем имя процесса
          ProcessNameBanks = propertiesBanks.get("ProcessName") + "_Requests";
//            ProcessNameBanks = dataSourceBanks.get(0).getProcessName() + "_Requests";
            RequestServiceBanks serviceBanks = null;

            try {
                serviceBanks = new RequestServiceBanks(dataSourceBanks, propertiesBanks, dd, ProcessNameBanks);
//            создание запросов
                serviceBanks.processCreateBanks();


            } catch (Exception e) {
//                e.printStackTrace();
                //в случае возникновения исключительной ситуации, удаляем сформированные файлы
                System.err.println(e.getMessage());
                MyLoggerBanks.get().logMessage(ProcessNameBanks , e.getMessage());
                if (serviceBanks != null)
                    serviceBanks.deleteCreatedFiles();
                System.out.println("Исключение в главном классе "+e.getMessage());
                MyLoggerBanks.get().logMessage(ProcessNameBanks, "Исключение в главном классе "+e.getMessage());
                Logger.getLogger(CreateRequestBanks.class).error("Исключение в главном классе "+e.getMessage());

            }
        }
    }


    private LinkedHashMap<String, String> getProperties() {
        return ConfigBanks.getInstance().getPropertiesBanks(ProcessNameBanks);
    }
}
