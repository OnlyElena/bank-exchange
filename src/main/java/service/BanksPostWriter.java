package service;

import beans.OSPBanks;
import beans.PostMoneyBanks;
import exceptions.FlowException;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import java.io.*;

/**
 * @author: OnlyElena@mail.ru
 * Date: 6/20/16
 * Time: 5:10 PM
 */
public class BanksPostWriter {
    String outputDirectoryBanks;
    String filenameBanks;
    String processNameBanks;

//    OutputStreamWriter outBanks;
    OutputStream outBanks;
    TransformerHandler hdBanks;
    AttributesImpl attsBanks;

    final static Logger logger = Logger.getLogger(BanksPostWriter.class);

    public BanksPostWriter(String outputDirectoryBanks, String filenameBanks, String processNameBanks) throws FlowException, SAXException, TransformerConfigurationException {
        this.outputDirectoryBanks = outputDirectoryBanks;
        this.filenameBanks = filenameBanks;
        this.processNameBanks = processNameBanks;
        initBanks();
    }

    /**
     * Данный конструктор нужен только для теста
     *
     * @param outBanks 1
     */
    public BanksPostWriter(OutputStream outBanks) throws FlowException, SAXException, TransformerConfigurationException {
        this.outBanks = outBanks;
        initBanks();
    }

    private void initBanks() throws FlowException, TransformerConfigurationException, SAXException {
        if (outputDirectoryBanks == null) throw new FlowException("Не указана директория для записи запросов");
        if (!outputDirectoryBanks.endsWith("/")) outputDirectoryBanks = outputDirectoryBanks + "/";
        File file = new File(outputDirectoryBanks);
        if (!file.exists()) {
            if (!file.mkdirs()) throw new FlowException("Не удалось создать директорию: " + file.getAbsolutePath());
        }


        if (outBanks == null)
            try {
                outBanks = new BufferedOutputStream(new FileOutputStream(new File(outputDirectoryBanks + filenameBanks)));
//                outBanks = new OutputStreamWriter(new FileOutputStream(outputDirectoryBanks + filenameBanks), Charset.forName("UTF-8"));
            } catch (FileNotFoundException e) {
                System.out.println("Не удалось записать фйал в директорию: " + file.getAbsolutePath()+"\n"+e.getMessage());
                MyLoggerBanks.get().logMessage(processNameBanks, "Не удалось записать фйал в директорию: " + file.getAbsolutePath()+"\n"+e.getMessage());
                logger.error("PostWriter - Не удалось записать фйал в директорию: " + file.getAbsolutePath()+"\n"+e.getMessage());
                new FlowException("Не удалось записать фйал в директорию: " + file.getAbsolutePath()+"\n"+e.getMessage());
//                e.printStackTrace();
            }
        SAXTransformerFactory tf = (SAXTransformerFactory) SAXTransformerFactory.newInstance();
        hdBanks = tf.newTransformerHandler();
        Transformer serializer = hdBanks.getTransformer();
        serializer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        serializer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        serializer.setOutputProperty(OutputKeys.INDENT, "yes");
        serializer.setOutputProperty(OutputKeys.METHOD, "xml");

        hdBanks.setResult(new StreamResult(outBanks));

        hdBanks.startDocument();
//            hd.characters(new char[]{13}, 0, 1);
        hdBanks.characters("\n".toCharArray(), 0, "\n".toCharArray().length);

        attsBanks = new AttributesImpl();

        hdBanks.startElement("", "", "Decree", attsBanks);

    }

        /**
     * Обязательно нужно делать проверку, что все обязательные поля заполнены.
     * Внутри метода проверка не выполняется.
     */
    public void writePostBanks(PostMoneyBanks z, OSPBanks ospBanks) throws FlowException {

        try {

            if (z.getId() == null) {
                throw new FlowException("Не указан уникальный номер запроса");
            }


            hdBanks.startElement("", "", "Act", attsBanks);

            writeElement("id", z.getId().toString());
            writeElement("barcode", z.getBarcode());
            writeElement("actype", z.getActtype());
            writeElement("actNumber", z.getActNumber());
            writeElement("actDate", z.getActDate());
            writeElement("execProcNumber", z.getExecProcNumber());
            writeElement("execProcDate", z.getExecProcDate());
            writeElement("priority", z.getPriority());
            writeElement("execActNum", z.getExecActNum());
            writeElement("execActDate", z.getExecActDate());
            writeElement("execActInitial", z.getExecActInitial());
            writeElement("execActInitialAddr", z.getExecActInitialAddr());
            writeElement("bailiff", z.getBailiff());
            writeElement("summ", z.getSumm());
            writeElement("creditorName", z.getCreditorName());
            writeElement("creditorAddress", z.getCreditorAddress());
            writeElement("accountNumber", z.getAccountNumber());
            if (z.getENTITY_TYPE().toString().equals("2")
                        |z.getENTITY_TYPE().toString().equals("71")
                        |z.getENTITY_TYPE().toString().equals("95")
                        |z.getENTITY_TYPE().toString().equals("96")
                        |z.getENTITY_TYPE().toString().equals("97")
                        |z.getENTITY_TYPE().toString().equals("666"))  {
                    System.out.println("ФЛ");
                writeElement("debtorFirstName", z.getDebtorFirstName());
                writeElement("debtorLastName", z.getDebtorLastName());
                writeElement("debtorSecondName", z.getDebtorSecondName());
                writeElement("debtorBirthYear", z.getDebtorBirthYear());
                writeElement("debtorBirth", z.getDebtorBirth());
                writeElement("debtorBornAddres", z.getDebtorBornAddres());
            } else {
                    System.out.println("ЮЛ");
                writeElement("DolgnikOrg", z.getDolgnikOrg());
                writeElement("DolgnikInn", z.getDolgnikInn());
                writeElement("DolgnikKpp", z.getDolgnikKpp());
                }

            writeElement("debtorAddres", z.getDebtorAddres());

            writeElement("territory", ospBanks.getTerritory());
            writeElement("department", ospBanks.getDepartment());
            writeElement("name", ospBanks.getName());
            writeElement("bankname", ospBanks.getBankname());
            writeElement("kpp", ospBanks.getKpp());
            writeElement("inn", ospBanks.getInn());
            writeElement("okato", ospBanks.getOkato());
            writeElement("bik", ospBanks.getBik());
            writeElement("ls", ospBanks.getLs());
            writeElement("account", ospBanks.getAccount());
            writeElement("receivTitle", ospBanks.getReceivTitle());
            writeElement("address", ospBanks.getAddress());
            writeElement("kbk", "");
            writeElement("bankBranch", "");
            writeElement("bankAgency", "");

            writeElement("osbnumber", z.getOsbNumber());
            writeElement("accountCurreny", z.getAccountCurreny());
            writeElement("oldnumber", z.getExecOldProcNumber());

            hdBanks.endElement("", "", "Act");

            //перенос строки, что бы симпатичнее было
            hdBanks.characters("\n".toCharArray(), 0, "\n".toCharArray().length);


        } catch (SAXException e) {
            System.err.println("writePost: " + e.getMessage());
            MyLoggerBanks.get().logMessage(processNameBanks, "writePost: " + e.getMessage());
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

    private void writeElement(String elName, String value) throws SAXException, FlowException {
        hdBanks.startElement("", "", elName, attsBanks);

        if (value == null || value.replaceAll("\\s+", "").length() == 0) {
            hdBanks.characters(" ".toCharArray(), 0, 1);
        } else {
            hdBanks.characters(value.toCharArray(), 0, value.length());
        }

        hdBanks.endElement("", "", elName);
    }

    public void close() {
        try {
            hdBanks.endElement("", "", "Decree");
            hdBanks.endDocument();
//            System.out.println("close");
        } catch (SAXException e) {
            System.out.println(e);
            MyLoggerBanks.get().logMessage(processNameBanks, e.toString());
            e.printStackTrace();
        }
    try {
        outBanks.flush();
        outBanks.close();
//            System.out.println("close");
    } catch (IOException e) {
        System.out.println(e);
        MyLoggerBanks.get().logMessage(processNameBanks, e.toString());
        e.printStackTrace();
    }
    }

    public void setFilename(String filenameBanks) {
        this.filenameBanks = filenameBanks;
    }

//    public static void validate(SberbankRequest z) throws FlowException {
//        StringBuffer buff = new StringBuffer();
//
//        if (isNull(z.getFileName())) {
//            buff.append("Не укано имя файла для запроса\n");
//        }
//        if (isNull(z.getRequestId())) {
//            buff.append("Не указан ID запроса\n");
//        }
//        if (isNull(z.getUserId())) {
//            buff.append("Не указан ID пользователя\n");
//        }
//        if (isNull(z.getRequestDate())) {
//            buff.append("Не указана дата запроса\n");
//        }
//        if (isNull(z.getRequestTime())) {
//            buff.append("Не указано время запроса\n");
//        }
//        if (isNull(z.getRequestType())) {
//            buff.append("Не указан тип запроса\n");
//        }
//        if (isNull(z.getOsbList())) {
//            buff.append("Не указано отделение Сбербанка\n");
//        }
//        if (isNull(z.getDepartment())) {
//            buff.append("Не указано структурное подразделенеи ФССП\n");
//        }
//        if (isNull(z.getBailiff())) {
//            buff.append("Не указан пристав\n");
//        }
//        if (isNull(z.getHeadBailiff())) {
//            buff.append("Не указан начальник структурного подразделение ФССП\n");
//        }
//        if (isNull(z.getExecProcNum())) {
//            buff.append("Не указан номер исполнительного производства\n");
//        }
//        if (isNull(z.getSumm())) {
//            buff.append("Не указана сумма\n");
//        }
//        if (isNull(z.getExecActNum())) {
//            buff.append("Не указан номер исполнительного документа\n");
//        }
//        if (isNull(z.getExecActDate())) {
//            buff.append("Не указана дата исполнительного документа\n");
//        }
//        if (isNull(z.getDebtorLastName())) {
//            buff.append("Не указана Фамилия должника\n");
//        }
//        if (isNull(z.getDebtorFirstName())) {
//            buff.append("Не указано Имя должника\n");
//        }
//        if (isNull(z.getDebtorSecondName())) {
//            buff.append("Не указано отчество должника\n");
//        }
//        if (isNull(z.getDebtorBirthYear())) {
//            buff.append("Не указан год рождения должника\n");
//        }
//        if (isNull(z.getDebtorAddres())) {
//            buff.append("Не указан адрес должника\n");
//        }
//
//        //необязательные параметры
////        if (isNull(z.getDebtorBirthDate())) {
////            buff.append("Не указана дата рождения должника\n");
////        }
////        if (isNull(z.getDebtorBornAddres())) {
////            buff.append("Не указано место рождения должника\n");
////        }
//
//        if (buff.length() > 0) {
//            buff.append("Номер ИП: "+z.getExecProcNum());
//            throw new FlowException(buff.toString());
//        }
//    }


    private static boolean isNull(String obj) {
        if (obj == null) return true;

        if (obj.replaceAll("[\\s]+", "").length() == 0) {
            return true;
        }
        return false;
    }

}