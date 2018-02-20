import beans.ResponseBanks;
import exceptions.EndDocumentException;
import exceptions.FlowException;
import jdbc.BeanConnectBanks;
import jdbc.GetBeansConnectBanks;
import org.apache.log4j.Logger;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;
import service.XmlReaderBanks;
import service.ConfigBanks;
import service.MyLoggerBanks;
import service.ResponceServiceBanks;

import java.io.*;
import java.util.*;
import java.util.LinkedHashMap;

/**
 * @author: OnlyElena@mail.ru
 * Date: 10/30/16
 * Time: 2:04 PM
 */
public class ImportResponsBanksAllDepts {

        public static void main(String[] args) {
            new ImportResponsBanksAllDepts();
        }

        final static Logger logger = Logger.getLogger(ImportResponsBanks.class);

        //параметры Управления
        LinkedHashMap<String, String> propertiesBanks;
        String ProcessNameBanks = "_Response";

        public ImportResponsBanksAllDepts() {
            Thread.currentThread().setName("Banks-exchange_Response");
//        System.out.println(Thread.currentThread());
            processResponseBanks();
        }

        private void processResponseBanks() {
            propertiesBanks = getPropertiesBanks();

//      Читаем список подключений exProd.xml экспортируем список отделов с параметрами подключения в карту Map
            File exprodBanks = new File(propertiesBanks.get("PathExprod"));
            GetBeansConnectBanks getBeansConnectBanks = new GetBeansConnectBanks();
            Map<String, List<BeanConnectBanks>> dataSourcesBanks = GetBeansConnectBanks.GetBeansConnect(exprodBanks);

//        final List<ArrayList<PostStat>> list = Collections.synchronizedList(new ArrayList<ArrayList<PostStat>>());
        List<Thread> threadsBanks = new ArrayList<Thread>();
            int kkBanks = 0;
//      Проводим работу последовательно по отделам по карте
            for (final List<BeanConnectBanks> dataSourceBanks : dataSourcesBanks.values()) {
                Thread t = new Thread("Sber" + kkBanks) {
                    @Override
                    public void run() {
//        Состовляем имя процесса
                ProcessNameBanks = propertiesBanks.get("ProcessName") + "_Response";
//            ProcessNameBanks = dataSourceBanks.get(0).getProcessName() + "_Response";

                logger.info("Обрабатываем БД " + dataSourceBanks.get(0).getBeanId());
                MyLoggerBanks.get().logMessage(ProcessNameBanks, "Обрабатываем БД " + dataSourceBanks.get(0).getBeanId());

//            Thread t = new Thread("Sber" + kk) {
//                @Override
//                public void run() {

                String inputDir = propertiesBanks.get("INPUT_DIRECTORY");
                String kodOSPBanks = dataSourceBanks.get(0).getBeanId();
//            Только для Иркутской области
//            if (dataSource.get(0).getBeanId().equals("50")) kodOSP = "80";
                final String finalKodOSPBanks = kodOSPBanks;
                File[] files = new File(inputDir).listFiles(new FilenameFilter() {
                    @Override
                    public boolean accept(File file, String s) {
//                            return s.toLowerCase().matches("^f[0-9]{2}[0-9a-c]{1}"+properties.get("KOD_OSB")+".[0-9a-z]{3}");
                        if (propertiesBanks.get("MVV_AGENT_CODE").equalsIgnoreCase("БАЙКАЛБАНК")){
                            return s.matches("^R[0-9]{2}_[0-9]{2}_[0-9]{8}_[0-9]{2}.xml");
                        }else {
                            return s.toLowerCase().matches("^f[0-9]{2}[0-9a-c]{1}" + propertiesBanks.get("KOD_OSB") + ".[0-9a-z]{1}" + finalKodOSPBanks);
                        }
                    }
                });
//                try {
                    if (files.length == 0) {
                        System.out.println("Файлов ответов не найдено для отдела " + dataSourceBanks.get(0).getBeanId() + " (" + inputDir + ")");
                        MyLoggerBanks.get().logMessage(ProcessNameBanks, "Файлов ответов не найдено для отдела " + dataSourceBanks.get(0).getBeanId() + " (" + inputDir + ")");
                        logger.error("Файлов ответов не найдено для отдела " + dataSourceBanks.get(0).getBeanId() + " (" + inputDir + ")");
                    }
//                }catch (NullPointerException e){
//                    System.out.println("Файлов ответов не найдено для отдела " + dataSourceBanks.get(0).getBeanId() + " (" + inputDir + ")");
//                    MyLoggerBanks.get().logMessage(ProcessNameBanks, "Файлов ответов не найдено для отдела " + dataSourceBanks.get(0).getBeanId() + " (" + inputDir + ")");
//                    logger.error("Файлов ответов не найдено для отдела " + dataSourceBanks.get(0).getBeanId() + " (" + inputDir + ")");
//                    continue;
//                }

                for (File file : files) {
                    if (file.exists() == false) {
                        System.out.println("Файлов ответов или файл пустой не найдено (" + inputDir + " " + file + ")");
                        MyLoggerBanks.get().logMessage(ProcessNameBanks, "Файлов ответов или файл пустой не найдено (" + inputDir + " " + file + ")");
                        logger.error("Файлов ответов или файл пустой не найдено (" + inputDir + " " + file + ")");
                        return;
                    }
                    MyLoggerBanks.get().logMessage(ProcessNameBanks, "Обрабатываем файл " + file.getName());
                    logger.info("ImportNotification - Обрабатываем файл " + file.getName());
                    boolean deleteFile = true;
//                NotifParser parser = new NotifParser(file, ProcessName);
//                List<NotifBean> res = parser.getRes();
                    Hashtable<String, List<ResponseBanks>> responsesBanks = getResponsesBanks(file);
                    //ответов нет, идем дальше по файлам
                    if (responsesBanks == null) continue;

//                System.out.println(sberbankResponses);

//                NotifService notifService = new NotifService(dataSource, properties, ProcessName);
                    ResponceServiceBanks responceServiceBanks = new ResponceServiceBanks(dataSourceBanks, propertiesBanks, ProcessNameBanks);

                    System.out.println("Обрабатываем файл: " + file.getAbsolutePath());
                    MyLoggerBanks.get().logMessage("Resp", "Обрабатываем файл: " + file.getAbsolutePath());
                    boolean b = false;
                    try {
                        b = responceServiceBanks.processResponseBanks(responsesBanks);
                    } catch (FlowException e) {
                        System.err.println(new Date() + " " + e.getMessage());
                        MyLoggerBanks.get().logMessage(ProcessNameBanks, new Date() + " " + e.getMessage());
                        logger.error(new Date() + " " + e.getMessage());
                    }
                    //если хоть одна строка не обработалась, файл не удаляем
                    if (!b) deleteFile = false;
                    if (deleteFile) {
                        file.delete();
                        System.out.println(new Date() + " Файл обработан и удален: " + file.getAbsolutePath());
                        MyLoggerBanks.get().logMessage(ProcessNameBanks, new Date() + " Файл обработан и удален: " + file.getAbsolutePath());
                        logger.info("ImportNotification" + new Date() + " Файл обработан и удален: " + file.getAbsolutePath());
                    }
                }
                }
            };
            t.start();
            threadsBanks.add(t);
            kkBanks = kkBanks+1;
            }

        for (Thread threadBanks : threadsBanks) {
            try {
//                    System.out.println("Поток ждет="+thread.getName());
                threadBanks.join();
            } catch (InterruptedException e) {
                System.out.println("Ошибкак ожидания потока " + Thread.currentThread().getName());
            }
        }

            System.out.println("Завершение главного потока");

        }


        private Hashtable<String, List<ResponseBanks>> getResponsesBanks(File file) {
            XmlReaderBanks parserBanks = null;
            try {
                InputSource src = new InputSource(new FileInputStream(file));

                XMLReader reader = XMLReaderFactory.createXMLReader();
                parserBanks = new XmlReaderBanks();
                reader.setContentHandler(parserBanks);
                reader.parse(src);


            } catch (EndDocumentException e) {
                //достигли конец документа, все в порядке
                return parserBanks.getResponcesBanks();
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

            String path = getPropertiesBanks().get("INPUT_DIRECTORY");
            final String kod_osb = getPropertiesBanks().get("KOD_OSB");
            File[] files = new File(path).listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File file, String s) {
//                return s.matches("^f[0-9]{2}[0-9a-c]{1}0018.[0-9a-f]{3}");
                    return s.toLowerCase().matches("^f[0-9]{2}[0-9a-c]{1}" + kod_osb + ".[0-9a-z]{3}");
                }
            });

            return files;
        }


        private LinkedHashMap<String, String> getPropertiesBanks() {
            return ConfigBanks.getInstance().getPropertiesBanks(ProcessNameBanks);
        }
    }
