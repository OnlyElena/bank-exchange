import beans.SberbankResponse;
import exceptions.EndDocumentException;
import exceptions.FlowException;
import jdbc.BeanConnect;
import jdbc.GetBeansConnect;
import org.apache.log4j.Logger;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;
import services.Config;
import services.MyLogger;
import services.ResponceService;
import services.SberbankXmlReader;

import java.io.*;
import java.util.*;

/**
 * @author: OnlyElena@mail.ru
 * Date: 4/18/16
 * Time: 5:57 PM
 */
public class ImportResponse {
    public static void main(String[] args) {
        new ImportResponse();
    }

    final static Logger logger = Logger.getLogger(ImportResponse.class);

    //параметры Управления
    LinkedHashMap<String, String> properties;
    String ProcessName = "_Response";

    public ImportResponse() {
        Thread.currentThread().setName("Bank-exchange_Response");
//        System.out.println(Thread.currentThread());
        processResponse();
    }

    private void processResponse() {
        properties = getProperties();

//      Читаем список подключений exProd.xml экспортируем список отделов с параметрами подключения в карту Map
        File exprod = new File(properties.get("PathExprod"));
        GetBeansConnect getBeansConnect = new GetBeansConnect();
        Map<String, List<BeanConnect>> dataSources = GetBeansConnect.GetBeansConnect(exprod);

//        final List<ArrayList<PostStat>> list = Collections.synchronizedList(new ArrayList<ArrayList<PostStat>>());
//        List<Thread> threads = new ArrayList<Thread>();
        int kk = 0;
//      Проводим работу последовательно по отделам по карте
        for (final List<BeanConnect> dataSource : dataSources.values()) {
//        Состовляем имя процесса
            ProcessName = dataSource.get(0).getProcessName() + "_Response";

            logger.info("Обрабатываем БД " + dataSource.get(0).getBeanId());
            MyLogger.get().logMessage(ProcessName, "Обрабатываем БД " + dataSource.get(0).getBeanId());

//            Thread t = new Thread("Sber" + kk) {
//                @Override
//                public void run() {

            String inputDir = properties.get("INPUT_DIRECTORY");
            String kodOSP = dataSource.get(0).getBeanId();
//            Только для Иркутской области
            if (dataSource.get(0).getBeanId().equals("50")) kodOSP = "80";
            final String finalKodOSP = kodOSP;
            File[] files = new File(inputDir).listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File file, String s) {
//                            return s.toLowerCase().matches("^f[0-9]{2}[0-9a-c]{1}"+properties.get("KOD_OSB")+".[0-9a-z]{3}");
                    return s.toLowerCase().matches("^f[0-9]{2}[0-9a-c]{1}" + properties.get("KOD_OSB") + ".[0-9a-z]{1}" + finalKodOSP);
                }
            });
            if (files.length == 0) {
                System.out.println("Файлов ответов не найдено для отдела " + dataSource.get(0).getBeanId() + " (" + inputDir + ")");
                MyLogger.get().logMessage(ProcessName, "Файлов ответов не найдено для отдела " + dataSource.get(0).getBeanId() + " (" + inputDir + ")");
                logger.error("Файлов ответов не найдено для отдела " + dataSource.get(0).getBeanId() + " (" + inputDir + ")");
            }
            for (File file : files) {
                if (file.exists() == false) {
                    System.out.println("Файлов ответов или файл пустой не найдено (" + inputDir + " " + file + ")");
                    MyLogger.get().logMessage(ProcessName, "Файлов ответов или файл пустой не найдено (" + inputDir + " " + file + ")");
                    logger.error("Файлов ответов или файл пустой не найдено (" + inputDir + " " + file + ")");
                    return;
                }
                MyLogger.get().logMessage(ProcessName, "Обрабатываем файл " + file.getName());
                logger.info("ImportNotification - Обрабатываем файл " + file.getName());
                boolean deleteFile = true;
//                NotifParser parser = new NotifParser(file, ProcessName);
//                List<NotifBean> res = parser.getRes();
                Hashtable<String, List<SberbankResponse>> sberbankResponses = getSberbankResponses(file);
                //ответов нет, идем дальше по файлам
                if (sberbankResponses == null) continue;

//                System.out.println(sberbankResponses);

//                NotifService notifService = new NotifService(dataSource, properties, ProcessName);
                ResponceService responceService = new ResponceService(dataSource, properties, ProcessName);

                System.out.println("Обрабатываем файл: " + file.getAbsolutePath());
                MyLogger.get().logMessage("Resp", "Обрабатываем файл: " + file.getAbsolutePath());
                boolean b = false;
                try {
                    b = responceService.processResponse(sberbankResponses);
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


    private Hashtable<String, List<SberbankResponse>> getSberbankResponses(File file) {
        SberbankXmlReader parser = null;
        try {
            InputSource src = new InputSource(new FileInputStream(file));

            XMLReader reader = XMLReaderFactory.createXMLReader();
            parser = new SberbankXmlReader();
            reader.setContentHandler(parser);
            reader.parse(src);


        } catch (EndDocumentException e) {
            //достигли конец документа, все в порядке
            return parser.getResponces();
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

    private File[] getResponseFiles() {

        String path = getProperties().get("INPUT_DIRECTORY");
        final String kod_osb = getProperties().get("KOD_OSB");
        File[] files = new File(path).listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
//                return s.matches("^f[0-9]{2}[0-9a-c]{1}0018.[0-9a-f]{3}");
                return s.toLowerCase().matches("^f[0-9]{2}[0-9a-c]{1}" + kod_osb + ".[0-9a-z]{3}");
            }
        });

        return files;
    }


    private LinkedHashMap<String, String> getProperties() {
        return Config.getInstance().getProperties(ProcessName);
    }
}
