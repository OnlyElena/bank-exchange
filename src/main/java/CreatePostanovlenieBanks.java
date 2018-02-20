import jdbc.BeanConnectBanks;
import jdbc.GetBeansConnectBanks;
import org.apache.log4j.Logger;
import service.ConfigBanks;
import service.MyLoggerBanks;
import service.PostServiceBanks;

import java.io.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author: OnlyElena@mail.ru
 * Date: 6/20/16
 * Time: 1:35 PM
 */
public class CreatePostanovlenieBanks {

    public static void main(String[] args) {
        new CreatePostanovlenieBanks();
    }

    //параметры Управления
    LinkedHashMap<String, String> propertiesBanks;
    String ProcessNameBanks = "_Postanovlenie";

    public CreatePostanovlenieBanks() {
        Thread.currentThread().setName("Banks-exchange");
        System.out.println(Thread.currentThread());
        process();
    }

    private void process() {
        Date dateBanks = new Date(System.currentTimeMillis());
        String ddBanks = new SimpleDateFormat("dd").format(dateBanks);
        propertiesBanks = getProperties();


//        Состовляем имя процесса
//        String ProcessName = properties.get("ProcessName") + "_Postanovlenie";
//        Читаем список подключений exProd.xml экспортируем список отделов с параметрами подключения в карту Map
        File file = new File(propertiesBanks.get("PathExprod"));
        GetBeansConnectBanks getBeansConnectBanks = new GetBeansConnectBanks();
        Map<String, List<BeanConnectBanks>> dataSourcesBanks = getBeansConnectBanks.GetBeansConnect(file);
//        Проводим работу последовательно по отделам по карте
        for (List<BeanConnectBanks> dataSourceBanks : dataSourcesBanks.values()) {
            //        Состовляем имя процесса
            ProcessNameBanks = propertiesBanks.get("ProcessName") + "_Postanovlenie";
//            ProcessNameBanks = dataSourceBanks.get(0).getProcessName() + "_Postanovlenie";

            System.out.println(ProcessNameBanks+" Обрабатываем БД "+dataSourceBanks.get(0).getBeanId());
            MyLoggerBanks.get().logMessage(ProcessNameBanks, " Обрабатываем БД " + dataSourceBanks.get(0).getBeanId());
            Logger.getLogger(CreatePostanovlenieBanks.class).info(" Обрабатываем БД " + dataSourceBanks.get(0).getBeanId());

            PostServiceBanks serviceBanks = null;


            try {
                serviceBanks = new PostServiceBanks(dataSourceBanks, propertiesBanks, ddBanks, ProcessNameBanks);
//            создание постановлений
                serviceBanks.processCreate();


            } catch (Exception e) {
//                e.printStackTrace();
                System.out.println(ProcessNameBanks+" Исключение в главном классе "+e.getMessage()+" "+e);
                MyLoggerBanks.get().logMessage(ProcessNameBanks, "Исключение в главном классе "+e.getMessage());
                Logger.getLogger(CreatePostanovlenie.class).error("Исключение в главном классе "+e.getMessage());
            }
        }

        boolean zipFiles = false;
        File[] files = new File(propertiesBanks.get("OUTPUT_DIRECTORY")).listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
//                System.out.println(s);
                return s.toLowerCase().matches("^piev_[0-9]{14}.xml.zip");
            }
        });
        Logger.getLogger(CreatePostanovlenie.class).info("Количество файлов для архивации: " + files.length);
        MyLoggerBanks.get().logMessage(ProcessNameBanks, "Количество файлов для архивации: " + files.length);
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
            String outFilename = "p" + propertiesBanks.get("KOD_OSP") + "00" + day + month + "." + "1ss";
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(propertiesBanks.get("OUTPUT_DIRECTORY")+outFilename));

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
                    Logger.getLogger(CreatePostanovlenieBanks.class).error("Не удалось запаковать файл: " + filename.getAbsolutePath());
                    MyLoggerBanks.get().logMessage(ProcessNameBanks, "Не удалось запаковать файл: " + filename.getAbsolutePath());
                    return false;
                }
            }

            // Complete the ZIP file
            out.close();
            Logger.getLogger(CreatePostanovlenieBanks.class).info("Подготовлен архив: " + outFilename);
            MyLoggerBanks.get().logMessage(ProcessNameBanks, "Подготовлен архив: " + outFilename);
        } catch (IOException e) {
            Logger.getLogger(CreatePostanovlenieBanks.class).error("Ошибка при формировании архива: " + e.getMessage());
            MyLoggerBanks.get().logMessage(ProcessNameBanks, "Ошибка при формировании архива: " + e.getMessage());
            return false;
        }
        return true;
    }

    private LinkedHashMap<String, String> getProperties() {
        return ConfigBanks.getInstance().getPropertiesBanks(ProcessNameBanks);
    }
}
