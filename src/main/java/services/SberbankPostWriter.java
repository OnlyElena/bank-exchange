package services;

import beans.OSP;
import beans.PostMoney;
import exceptions.FlowException;
import org.apache.log4j.Logger;

import java.io.*;
import java.nio.charset.Charset;

/**
 * @author: OnlyElena@mail.ru
 * Date: 3/14/16
 * Time: 3:12 PM
 */
public class SberbankPostWriter {
    String outputDirectory;
    String filename;
    String processName;

    OutputStreamWriter out;

    final static Logger logger = Logger.getLogger(SberbankPostWriter.class);

    public SberbankPostWriter(String outputDirectory, String filename, String processName) throws FlowException {
        this.outputDirectory = outputDirectory;
        this.filename = filename;
        this.processName = processName;
        init();
    }

    /**
     * Данный конструктор нужен только для теста
     *
     * @param out 1
     */
    public SberbankPostWriter(OutputStreamWriter out) throws FlowException {
        this.out = out;
        init();
    }

    private void init() throws FlowException {
        if (outputDirectory == null) throw new FlowException("Не указана директория для записи запросов");
        if (!outputDirectory.endsWith("/")) outputDirectory = outputDirectory + "/";
        File file = new File(outputDirectory);
        if (!file.exists()) {
            if (!file.mkdirs()) throw new FlowException("Не удалось создать директорию: " + file.getAbsolutePath());
        }


        if (out == null)
//            out = new BufferedOutputStream(new FileOutputStream(new File(outputDirectory + filename)));
            try {
                out = new OutputStreamWriter(new FileOutputStream(outputDirectory + filename), Charset.forName("UTF-8"));
            } catch (FileNotFoundException e) {
                System.out.println("Не удалось записать фйал в директорию: " + file.getAbsolutePath()+"\n"+e.getMessage());
                MyLogger.get().logMessage(processName, "Не удалось записать фйал в директорию: " + file.getAbsolutePath()+"\n"+e.getMessage());
                logger.error("PostWriter - Не удалось записать фйал в директорию: " + file.getAbsolutePath()+"\n"+e.getMessage());
                new FlowException("Не удалось записать фйал в директорию: " + file.getAbsolutePath()+"\n"+e.getMessage());
//                e.printStackTrace();
            }
    }

    /**
     * Обязательно нужно делать проверку, что все обязательные поля заполнены.
     * Внутри метода проверка не выполняется.
     */
    public void writePost(PostMoney z, OSP osp) throws FlowException {

        try {

            if (z.getId() == null) {
                throw new FlowException("Не указан уникальный номер запроса");
            }


            out.write(z.toString());
            System.out.println(z.toString());
            out.write(" ");
            out.write(osp.toString());
            System.out.println(osp.toString());
            out.write(" ");
            out.write(z.toStringSber());
            System.out.println(z.toStringSber());
            out.write("\n");

//            writeElement("File_Name", z.getFileName());
//            writeElement("Req_ID", z.getRequestId());

        } catch (IOException e) {
            System.err.println("writePost: " + e.getMessage());
            MyLogger.get().logMessage(processName, "writePost: " + e.getMessage());
            logger.error("PostWriter - writePost: " + e.getMessage());
            e.printStackTrace();
        }

    }

    private void nonNull(String obj) throws FlowException {
        if (obj == null) throw new FlowException("Нулевое значение параметра");

        if (obj.replaceAll("[\\s]+", "").length() == 0) {
            throw new FlowException("Нулевое значение параметра");
        }
    }

    int reqCounter = 0;


    public void close() {
        try {
            out.flush();
            out.close();
//            System.out.println("close");
        } catch (IOException e) {
            System.out.println(e);
            MyLogger.get().logMessage(processName, e.toString());
                    e.printStackTrace();
        }
    }

    private static boolean isNull(String obj) {
        if (obj == null) return true;

        if (obj.replaceAll("[\\s]+", "").length() == 0) {
            return true;
        }
        return false;
    }

}
