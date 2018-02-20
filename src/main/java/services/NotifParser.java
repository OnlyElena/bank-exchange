package services;

//import fssp38.sberbank.dao.MyLogger;

import beans.NotifBean;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * User: Andrey V. Panov
 * Date: 8/13/12
 * Time: 11:07 AM
 */
/**
 * @author: OnlyElena@mail.ru
 * Date: 4/7/16
 * Time: 10:52 AM
 */
public class NotifParser {
    File file;
    List<NotifBean> res;
    String processName;

    public NotifParser(File file, String processName) {
        this.file = file;
        this.processName = processName;
        res = new LinkedList<NotifBean>();

        parse();
    }

    private void parse() {

        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file), Charset.forName("UTF-8")));
            String line = null;

            while ((line = in.readLine()) != null) {
                NotifBean bean = parseLine(line);
//                System.out.println(file);
//                System.out.println(bean);
                if (bean != null) {
                    res.add(bean);
                }
            }

        } catch (FileNotFoundException e) {
            System.err.println(new Date() + " File not found: " + file.getAbsolutePath());
            MyLogger.get().logMessage(processName, new Date() + " File not found: " + file.getAbsolutePath());
        } catch (Exception e) {
            System.err.println(new Date() + " " + e.getMessage());
            MyLogger.get().logMessage(processName, new Date() + " " + e.getMessage());
        }
    }

    private NotifBean parseLine(String line) {
        if (!line.startsWith("| ")) return null;

        NotifBean bean = new NotifBean();

        //первый разделитель пропускаем, он лишний
        String[] strings = line.substring(2).split("\\|");

        //дробим по знаку равно и укладываем в бин
        for (String string : strings) {
            string = string.trim();
            int separator = string.indexOf("=");

            if (separator == -1) {
                System.err.println("Не удалось обработать строку: " + line);
                MyLogger.get().logMessage(processName, "Не удалось обработать строку: " + line);
                return null;
            }

            //+1 смещение, что бы пропустить знак равенства
            String arg = string.substring(0, separator);
            String value = string.substring(separator + 1);

            if (arg.equals("id")) {
                bean.setId(value);
            } else if (arg.equals("execProcNumber")) {
                bean.setExecProcNumber(value);
            } else if (arg.equals("debtorFirstName")) {
                bean.setDebtorFirstName(value);
            } else if (arg.equals("debtorLastName")) {
                bean.setDebtorLastName(value);
            } else if (arg.equals("debtorSecondName")) {
                bean.setDebtorSecondName(value);
            } else if (arg.equals("accountNumber")) {
                bean.setAccountNumber(value);
            } else if (arg.equals("summ")) {
                bean.setSumm(value);
            } else if (arg.equals("ProcNumberState")) {
                bean.setProcNumberState(value);
            } else if (arg.equals("Status")) {
                bean.setStatus(value);
            }
        }

        return bean;

    }

    public List<NotifBean> getRes() {
        return res;
    }
}
