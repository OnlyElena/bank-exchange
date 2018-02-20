package service;
// SAX classes.
//JAXP 1.1

import beans.RequestBanks;
import exceptions.FlowException;
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
 * Date: 6/19/16
 * Time: 12:48 AM
 */
public class XmlWriterBanks {
    public String outputDirectoryBanks;
    public String filenameBanks;

    OutputStream outBanks;
    TransformerHandler hdBanks;
    AttributesImpl attsBanks;

    public XmlWriterBanks(String outputDirectoryBanks, String filenameBanks) throws FlowException, TransformerConfigurationException, SAXException, FileNotFoundException {
        this.outputDirectoryBanks = outputDirectoryBanks;
        this.filenameBanks = filenameBanks;
        initBanks();
    }

    /**
     * Данный конструктор нужен только для теста
     *
     * @param outBanks 1
     */

    public XmlWriterBanks(OutputStream outBanks) throws FlowException, TransformerConfigurationException, SAXException, FileNotFoundException {
        this.outBanks = outBanks;
        initBanks();
    }

    private void initBanks() throws FlowException, TransformerConfigurationException, SAXException, FileNotFoundException {
        if (outputDirectoryBanks == null) throw new FlowException("Не указана директория для записи запросов");
        if (!outputDirectoryBanks.endsWith("/")) outputDirectoryBanks = outputDirectoryBanks + "/";
        File file = new File(outputDirectoryBanks);
        if (!file.exists()) {
            if (!file.mkdirs()) throw new FlowException("Не удалось создать директорию: " + file.getAbsolutePath());
        }


        if (outBanks == null)
            outBanks = new BufferedOutputStream(new FileOutputStream(new File(outputDirectoryBanks + filenameBanks)));

        SAXTransformerFactory tf = (SAXTransformerFactory) SAXTransformerFactory.newInstance();
        hdBanks = tf.newTransformerHandler();
        Transformer serializer = hdBanks.getTransformer();
        serializer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

        hdBanks.setResult(new StreamResult(outBanks));

        hdBanks.startDocument();
//            hd.characters(new char[]{13}, 0, 1);
        hdBanks.characters("\n".toCharArray(), 0, "\n".toCharArray().length);

        attsBanks = new AttributesImpl();

        hdBanks.startElement("", "", "Request", attsBanks);

    }

    /**
     * Обязательно нужно делать проверку, что все обязательные поля заполнены.
     * Внутри метода проверка не выполняется.
     */
    public void writeRequestBanks(RequestBanks z) throws FlowException {
        if (z == null) return;

        z.setFileName(filenameBanks);

        validate(z);

        try {

            if (z.getRequestId() == null) {
                throw new FlowException("Не указан уникальный номер запроса");
            }

            hdBanks.startElement("", "", "Zapros", attsBanks);

            writeElement("File_Name", z.getFileName());
            writeElement("Req_ID", z.getRequestId());
            writeElement("User_ID", z.getUserId());
            writeElement("Req_Date", z.getRequestDate());
            writeElement("Req_Time", z.getRequestTime());
            writeElement("Req_Type", z.getRequestType());
            writeElement("OSB_List", z.getOsbList());
            writeElement("Prs_Dep", z.getDepartment());
            writeElement("FIO_SPI", z.getBailiff());
            writeElement("H_PRISTAV", z.getHeadBailiff());
            writeElement("Isp_Num", z.getExecProcNum());
            writeElement("Isp_Sum", z.getSumm());
            writeElement("Isp_Num1", z.getExecActNum());
            writeElement("Req_Date1", z.getExecActDate());
            if (z.getRequestType().equals("2")){
                writeElement("DolgnikOrg", z.getDebtorName());
                writeElement("DolgnikInn", z.getDebtorInn());
                writeElement("DolgnikKpp", z.getDebtorKpp());
                writeElement("Dolg_Addr", z.getDebtorAddres());
            }else {
                writeElement("Dolg_Surname", z.getDebtorLastName());
                writeElement("Dolg_Name", z.getDebtorFirstName());
                writeElement("Dolg_Secondname", z.getDebtorSecondName());
                writeElement("Dolg_Birth_Year", z.getDebtorBirthYear());
                writeElement("Dolg_Addr", z.getDebtorAddres());
                //эти два поля не обязательны, поэтому могут быть нулевыми
                writeElement("Dolg_Birth_Day", z.getDebtorBirthDate());
                writeElement("Dolg_Place_Birth", z.getDebtorBornAddres());
            }


            hdBanks.endElement("", "", "Zapros");

            //перенос строки, что бы симпатичнее было
            hdBanks.characters("\n".toCharArray(), 0, "\n".toCharArray().length);

        } catch (SAXException e) {
            e.printStackTrace();
        }

    }


    public void writeRequestBaykalBank(RequestBanks z) throws FlowException {
        if (z == null) return;

        z.setFileName(filenameBanks);

        validate(z);

        try {

            if (z.getRequestId() == null) {
                throw new FlowException("Не указан уникальный номер запроса");
            }

            hdBanks.startElement("", "", "Zapros", attsBanks);

            writeElement("File_Name", z.getFileName());
            writeElement("Req_ID", z.getRequestId());
            writeElement("User_ID", z.getUserId());
            writeElement("Req_Date", z.getRequestDate());
            writeElement("Req_Time", z.getRequestTime());
            writeElement("Req_Type", z.getRequestType());
            writeElement("Organization_Name", "ОАО АК Байкалбанк");
            writeElement("Prs_Dep", z.getDepartment());
            writeElement("FIO_SPI", z.getBailiff());
            writeElement("H_PRISTAV", z.getHeadBailiff());
            writeElement("Isp_Num", z.getExecProcNum());
            writeElement("Isp_Sum", z.getSumm());
            writeElement("Isp_Doc", z.getExecActNum());
            writeElement("Req_Doc_Date", z.getExecActDate());
//            writeElement("Dolg_Surname", z.getDebtorLastName());
//            writeElement("Dolg_Name", z.getDebtorFirstName());
//            writeElement("Dolg_Secondname", z.getDebtorSecondName());
            writeElement("Dolg_Name", z.getDebtorName());
            writeElement("Dolg_Inn", z.getDebtorInn());
//            writeElement("Dolg_Birth_Year", z.getDebtorBirthYear());
            writeElement("Dolg_Addr", z.getDebtorAddres());
            //эти два поля не обязательны, поэтому могут быть нулевыми
            writeElement("Dolg_Birth_Day", z.getDebtorBirthDate());
            writeElement("Dolg_Place_Birth", z.getDebtorBornAddres());

            hdBanks.endElement("", "", "Zapros");

            //перенос строки, что бы симпатичнее было
            hdBanks.characters("\n".toCharArray(), 0, "\n".toCharArray().length);

        } catch (SAXException e) {
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
            hdBanks.endElement("", "", "Request");
            hdBanks.endDocument();
//            System.out.println("request");
        } catch (SAXException e) {
            System.out.println(e);
            e.printStackTrace();
        }


        try {
            outBanks.flush();
            outBanks.close();
//            System.out.println("close");
        } catch (IOException e) {
            System.out.println(e);
            e.printStackTrace();
        }
    }

    public void setFilename(String filenameBanks) {
        this.filenameBanks = filenameBanks;
    }

    public static void validate(RequestBanks z) throws FlowException {
        StringBuffer buff = new StringBuffer();

        if (isNull(z.getFileName())) {
            buff.append("Не укано имя файла для запроса\n");
        }
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

        //необязательные параметры
//        if (isNull(z.getDebtorBirthDate())) {
//            buff.append("Не указана дата рождения должника\n");
//        }
//        if (isNull(z.getDebtorBornAddres())) {
//            buff.append("Не указано место рождения должника\n");
//        }

        if (buff.length() > 0) {
            buff.append("Номер ИП: "+z.getExecProcNum());
            throw new FlowException(buff.toString());
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
