import beans.NotifBeanBanks;
import exceptions.EndDocumentException;
import exceptions.FlowException;
import jdbc.BeanConnectBanks;
import jdbc.GetBeansConnectBanks;
import org.apache.log4j.Logger;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;
import service.ConfigBanks;
import service.MyLoggerBanks;
import service.NotifServiceBanks;
import service.XmlReaderNotifBanks;

import java.io.*;
import java.util.*;

/**
 * @author: OnlyElena@mail.ru
 * Date: 7/9/16
 * Time: 12:06 AM
 */
public class ImportNotificationBanks {

    public static void main(String[] args) {
        new ImportNotificationBanks();
    }

    final static Logger loggerBanks = Logger.getLogger(ImportNotificationBanks.class);

    //параметры Управления
    LinkedHashMap<String, String> propertiesBanks;
    String ProcessNameBanks = "_NotificationBanks";

    public ImportNotificationBanks() {
        Thread.currentThread().setName("Bank-exchange_Notification");
//        System.out.println(Thread.currentThread());
        processNotifBanks();
    }

    private void processNotifBanks() {
        propertiesBanks = getPropertiesBanks();

//      Читаем список подключений exProd.xml экспортируем список отделов с параметрами подключения в карту Map
        File exprodBanks = new File(propertiesBanks.get("PathExprod"));
        GetBeansConnectBanks getBeansConnectBanks = new GetBeansConnectBanks();
        Map<String, List<BeanConnectBanks>> dataSourcesBanks = GetBeansConnectBanks.GetBeansConnect(exprodBanks);

//        final List<ArrayList<PostStat>> list = Collections.synchronizedList(new ArrayList<ArrayList<PostStat>>());
        List<Thread> threads = new ArrayList<Thread>();
        int kk = 0;
//      Проводим работу последовательно по отделам по карте
        for (final List<BeanConnectBanks> dataSourceBanks : dataSourcesBanks.values()) {
//        Состовляем имя процесса
            ProcessNameBanks = propertiesBanks.get("ProcessName") + "_Notification";

            loggerBanks.info("Обрабатываем БД " + dataSourceBanks.get(0).getBeanId());
            MyLoggerBanks.get().logMessage(ProcessNameBanks, "Обрабатываем БД " + dataSourceBanks.get(0).getBeanId());

//            Thread t = new Thread("Sber" + kk) {
//                @Override
//                public void run() {

//                }
//            };

            String inputDir = propertiesBanks.get("INPUT_DIRECTORY");
            File[] files = new File(inputDir).listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File file, String s) {
                /*u3802068.1ss*/
//                    return s.toLowerCase().matches("^u[0-9]{6}[0-9a-c]{1}.[0-9a-z]{1}ss");
                    return s.toLowerCase().matches("^u" + propertiesBanks.get("KOD_OSP") + dataSourceBanks.get(0).getBeanId() + "[0-9]{2}[0-9a-c]{1}.[0-9a-z]{1,2}ss");
                }
            });
            if (files.length == 0) {
                System.out.println("Файлов уведомлений (например: u3802068.1ss) не найдено для отдела " + dataSourceBanks.get(0).getBeanId() + " (" + inputDir + ")");
                MyLoggerBanks.get().logMessage(ProcessNameBanks, "Файлов уведомлений (например: u3802068.1ss) не найдено для отдела " + dataSourceBanks.get(0).getBeanId() + " (" + inputDir + ")");
                loggerBanks.error("Файлов уведомлений (например: u3802068.1ss) не найдено для отдела " + dataSourceBanks.get(0).getBeanId() + " (" + inputDir + ")");
            }
            for (File file : files) {
                if (file.exists() == false) {
                    System.out.println("Файлов уведомлений или файл пустой не найдено (" + inputDir + " " + file + ")");
                    MyLoggerBanks.get().logMessage(ProcessNameBanks, "Файлов уведомлений или файл пустой не найдено (" + inputDir + " " + file + ")");
                    loggerBanks.error("Файлов уведомлений или файл пустой не найдено (" + inputDir + " " + file + ")");
                    return;
                }
                MyLoggerBanks.get().logMessage(ProcessNameBanks, "Обрабатываем файл " + file.getName());
                loggerBanks.info("ImportNotification - Обрабатываем файл " + file.getName());
                boolean deleteFile = true;
                Hashtable<String, List<NotifBeanBanks>> notifBanks = getNotifBanks(file);
                //ответов нет, идем дальше по файлам
                if (notifBanks == null) continue;
//                NotifParserBanks parserBanks = new NotifParserBanks(file, ProcessNameBanks);
                NotifServiceBanks notifServiceBanks = new NotifServiceBanks(dataSourceBanks, propertiesBanks, ProcessNameBanks);
                boolean b = false;
                try {
                    b = notifServiceBanks.processNotifBanks(notifBanks);
                } catch (FlowException e) {
                    System.err.println(new Date() + " " + e.getMessage());
                    MyLoggerBanks.get().logMessage(ProcessNameBanks, new Date() + " " + e.getMessage());
                    loggerBanks.error(new Date() + " " + e.getMessage());
                }
                //если хоть одна строка не обработалась, файл не удаляем
//                if (!b) deleteFile = false;
//                if (deleteFile) {
//                    file.delete();
//                    System.out.println(new Date() + " Файл обработан и удален: " + file.getAbsolutePath());
//                    MyLoggerBanks.get().logMessage(ProcessNameBanks, new Date() + " Файл обработан и удален: " + file.getAbsolutePath());
//                    loggerBanks.info("ImportNotification" + new Date() + " Файл обработан и удален: " + file.getAbsolutePath());
//                }
            }
//                }
//            };
//            t.start();
//            threads.add(t);
//            kk = kk+1;
        }

//        for (Thread thread : threads) {
//            try {
////                    System.out.println("Поток ждет="+thread.getName());
//                thread.join();
//            } catch (InterruptedException e) {
//                System.out.println("Ошибкак ожидания потока " + Thread.currentThread().getName());
//            }
//        }

        System.out.println("Завершение главного потока");

    }

    private Hashtable<String, List<NotifBeanBanks>> getNotifBanks(File file) {
        XmlReaderNotifBanks parserNotifBanks = null;
        try {
            InputSource src = new InputSource(new FileInputStream(file));

            XMLReader reader = XMLReaderFactory.createXMLReader();
            parserNotifBanks = new XmlReaderNotifBanks();
            reader.setContentHandler(parserNotifBanks);
            reader.parse(src);


        } catch (EndDocumentException e) {
            //достигли конец документа, все в порядке
            return parserNotifBanks.getNotifBanks();
        } catch (SAXException e) {
            e.printStackTrace();
            //пропускаем цикл, т.е. не смогли разобрать файл

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;

    }



    private LinkedHashMap<String, String> getPropertiesBanks() {
        return ConfigBanks.getInstance().getPropertiesBanks(ProcessNameBanks);
    }
}


