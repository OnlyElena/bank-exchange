import beans.NotifBean;
import exceptions.FlowException;
import jdbc.BeanConnect;
import jdbc.GetBeansConnect;
import org.apache.log4j.Logger;
import services.Config;
import services.MyLogger;
import services.NotifParser;
import services.NotifService;

import java.io.File;
import java.io.FilenameFilter;
import java.util.*;

/**
 * @author: OnlyElena@mail.ru
 * Date: 5/23/16
 * Time: 1:10 PM
 */
public class ImportNotificationAllDepts {

    public static void main(String[] args) {
        new ImportNotificationAllDepts();
    }

    final static Logger logger = Logger.getLogger(NotifService.class);

    //параметры Управления
    LinkedHashMap<String, String> properties;
    String ProcessNam = "_Notification";

    public ImportNotificationAllDepts() {
        Thread.currentThread().setName("Bank-exchange_Notification");
//        System.out.println(Thread.currentThread());
        processNotif();
    }

    private void processNotif() {
        properties = getProperties();

//      Читаем список подключений exProd.xml экспортируем список отделов с параметрами подключения в карту Map
        File exprod = new File(properties.get("PathExprod"));
        GetBeansConnect getBeansConnect = new GetBeansConnect();
        Map<String, List<BeanConnect>> dataSources = GetBeansConnect.GetBeansConnect(exprod);

//        final List<ArrayList<PostStat>> list = Collections.synchronizedList(new ArrayList<ArrayList<PostStat>>());
        List<Thread> threads = new ArrayList<Thread>();
        int kk = 0;
//      Проводим работу последовательно по отделам по карте
        for (final List<BeanConnect> dataSource : dataSources.values()) {

            Thread t = new Thread("Sber" + kk) {
                @Override
                public void run() {
//        Состовляем имя процесса
//            ProcessName = dataSource.get(0).getProcessName() + "_Notification";
                    String ProcessName = dataSource.get(0).getProcessName() + "_Notification_" + dataSource.get(0).getBeanId();

                    logger.info("Обрабатываем БД " + dataSource.get(0).getBeanId());
                    MyLogger.get().logMessage(ProcessName, "Обрабатываем БД " + dataSource.get(0).getBeanId());
//
//            Thread t = new Thread("Sber" + kk) {
//                @Override
//                public void run() {

//                    ProcessName = dataSource.get(0).getProcessName() + "_Notification_"+dataSource.get(0).getBeanId();
                    String inputDir = properties.get("INPUT_DIRECTORY");
                    File[] files = new File(inputDir).listFiles(new FilenameFilter() {
                        @Override
                        public boolean accept(File file, String s) {
                /*u3802068.1ss*/
//                    return s.toLowerCase().matches("^u[0-9]{6}[0-9a-c]{1}.[0-9a-z]{1}ss");
                            return s.toLowerCase().matches("^u" + properties.get("KOD_OSP") + dataSource.get(0).getBeanId() + "[0-9]{2}[0-9a-c]{1}.[0-9a-z]{1}ss");
                        }
                    });
                    if (files.length == 0) {
                        System.out.println("Файлов уведомлений (например: u3802068.1ss) не найдено для отдела " + dataSource.get(0).getBeanId() + " (" + inputDir + ")");
                        MyLogger.get().logMessage(ProcessName, "Файлов уведомлений (например: u3802068.1ss) не найдено для отдела " + dataSource.get(0).getBeanId() + " (" + inputDir + ")");
                        logger.error("Файлов уведомлений (например: u3802068.1ss) не найдено для отдела " + dataSource.get(0).getBeanId() + " (" + inputDir + ")");
                    }
                    for (File file : files) {
                        if (file.exists() == false) {
                            System.out.println("Файлов уведомлений или файл пустой не найдено (" + inputDir + " " + file + ")");
                            MyLogger.get().logMessage(ProcessName, "Файлов уведомлений или файл пустой не найдено (" + inputDir + " " + file + ")");
                            logger.error("Файлов уведомлений или файл пустой не найдено (" + inputDir + " " + file + ")");
                            return;
                        }
                        MyLogger.get().logMessage(ProcessName, "Обрабатываем файл " + file.getName());
                        logger.info("ImportNotification - Обрабатываем файл " + file.getName());
                        boolean deleteFile = true;
                        NotifParser parser = new NotifParser(file, ProcessName);
                        List<NotifBean> res = parser.getRes();
                        NotifService notifService = new NotifService(dataSource, properties, ProcessName);
                        boolean b = false;
                        try {
                            b = notifService.processNotif(res);
                        } catch (FlowException e) {
                            System.err.println(new Date() + " " + e.getMessage());
                            MyLogger.get().logMessage(ProcessName, new Date() + " " + e.getMessage());
                            logger.error(new Date() + " " + e.getMessage());
                        }
                        //если хоть одна строка не обработалась, файл не удаляем
                        if (!b) deleteFile = false;
                        if (deleteFile) {
                            file.delete();
                            System.out.println(new Date() + " Файл обработан и удален: " + file.getAbsolutePath());
                            MyLogger.get().logMessage(ProcessName, new Date() + " Файл обработан и удален: " + file.getAbsolutePath());
                            logger.info("ImportNotification" + new Date() + " Файл обработан и удален: " + file.getAbsolutePath());
                        }
                    }
                }
            };
            t.start();
            threads.add(t);
            kk = kk + 1;
        }

        for (Thread thread : threads) {
            try {
//                    System.out.println("Поток ждет="+thread.getName());
                thread.join();
            } catch (InterruptedException e) {
                System.out.println("Ошибкак ожидания потока " + Thread.currentThread().getName());
            }
        }

        System.out.println("Завершение главного потока");

    }

    private LinkedHashMap<String, String> getProperties() {
        return Config.getInstance().getProperties(ProcessNam);
    }
}


//new Thread(){
//@Override
//public void run() {
//        //здесь все что нужно
//        }
//        }.start();


