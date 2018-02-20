import jdbc.BeanConnect;
import jdbc.GetBeansConnect;
import org.apache.log4j.Logger;
import services.Config;
import services.MyLogger;
import services.PostService;

import java.io.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author: OnlyElena@mail.ru
 * Date: 3/11/16
 * Time: 1:24 PM
 */
public class CreatePostanovlenie {

    public static void main(String[] args) {
        new CreatePostanovlenie();
    }

    //параметры Управления
    LinkedHashMap<String, String> properties;
    String ProcessName = "_Postanovlenie";

    public CreatePostanovlenie() {
        Thread.currentThread().setName("Bank-exchange");
        System.out.println(Thread.currentThread());
        process();
    }

    private void process() {
        Date date = new Date(System.currentTimeMillis());
        String dd = new SimpleDateFormat("dd").format(date);
        properties = getProperties();


//        Состовляем имя процесса
//        String ProcessName = properties.get("ProcessName") + "_Postanovlenie";
//        Читаем список подключений exProd.xml экспортируем список отделов с параметрами подключения в карту Map
        File file = new File(properties.get("PathExprod"));
        GetBeansConnect getBeansConnect = new GetBeansConnect();
        Map<String, List<BeanConnect>> dataSources = getBeansConnect.GetBeansConnect(file);
//        Проводим работу последовательно по отделам по карте
        for (List<BeanConnect> dataSource : dataSources.values()) {
            //        Состовляем имя процесса
//            String ProcessName = properties.get("ProcessName") + "_Postanovlenie";
            ProcessName = dataSource.get(0).getProcessName() + "_Postanovlenie";

            System.out.println(ProcessName+" Обрабатываем БД "+dataSource.get(0).getBeanId());
            MyLogger.get().logMessage(ProcessName, " Обрабатываем БД " + dataSource.get(0).getBeanId());
            Logger.getLogger(CreatePostanovlenie.class).info(" Обрабатываем БД " + dataSource.get(0).getBeanId());

            PostService service = null;


            try {
                service = new PostService(dataSource, properties, dd, ProcessName);
//            создание постановлений
                service.processCreate();


            } catch (Exception e) {
//                e.printStackTrace();
                System.out.println(ProcessName+" Исключение в главном классе "+e.getMessage()+" "+e);
                MyLogger.get().logMessage(ProcessName, "Исключение в главном классе "+e.getMessage());
                Logger.getLogger(CreatePostanovlenie.class).error("Исключение в главном классе "+e.getMessage());
            }
        }

        boolean zipFiles = false;
        File[] files = new File(properties.get("OUTPUT_DIRECTORY")).listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
//                System.out.println(s);
//                return s.toLowerCase().matches("^piev_[0-9]{14}.xml.zip");
                return s.toLowerCase().matches("^piev_[0-9]{14}.xml.zip");

            }
        });
        Logger.getLogger(CreatePostanovlenie.class).info("Количество файлов для архивации: " + files.length);
        MyLogger.get().logMessage(ProcessName, "Количество файлов для архивации: " + files.length);
                zipFiles = zipAddAll(files);
        if (zipFiles != false){
            for (File filenam : files){
                filenam.delete();
            }
        }

    }

    //    Пакуем пиев документы в один архив.
    private boolean zipAddAll(File[] files) {

//        AddZip(String path, String outFilename) {
//            File[] filenames = new File(path).listFiles();
        Calendar inst = Calendar.getInstance();
        String day = new DecimalFormat("00").format(inst.get(Calendar.DAY_OF_MONTH));
        String month = Integer.toHexString(inst.get(Calendar.MONTH) + 1);

        byte[] buf = new byte[1024];

        try {
//            String outFilename = "outfile.zip";
            String outFilename = "p" + properties.get("KOD_OSP") + "00" + day + month + "." + "1ss";
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(properties.get("OUTPUT_DIRECTORY")+outFilename));

            // Compress the files
//                for (File filename : filenames) {
            for (File filename : files) {

                try {
                    if (!filename.isFile()) continue;
                    FileInputStream in = new FileInputStream(filename);

                    //здесь ТОЛЬКО ИМЯ!
                    out.putNextEntry(new ZipEntry(filename.getName()));

                    // Transfer bytes from the file to the ZIP file
                    int len;
                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }

                    // Complete the entry
                    out.closeEntry();
                    in.close();
                } catch (IOException e) {
                    System.err.println("Не удалось запаковать файл: " + filename.getAbsolutePath());
                    Logger.getLogger(CreatePostanovlenie.class).error("Не удалось запаковать файл: " + filename.getAbsolutePath());
                    MyLogger.get().logMessage(ProcessName, "Не удалось запаковать файл: " + filename.getAbsolutePath());
                    return false;
                }
            }

            // Complete the ZIP file
            out.close();
            Logger.getLogger(CreatePostanovlenie.class).info("Подготовлен архив: " + outFilename);
            MyLogger.get().logMessage(ProcessName, "Подготовлен архив: " + outFilename);
        } catch (IOException e) {
            Logger.getLogger(CreatePostanovlenie.class).error("Ошибка при формировании архива: " + e.getMessage());
            MyLogger.get().logMessage(ProcessName, "Ошибка при формировании архива: " + e.getMessage());
            return false;
        }
        return true;
    }

    private LinkedHashMap<String, String> getProperties() {
        return Config.getInstance().getProperties(ProcessName);
    }
}
