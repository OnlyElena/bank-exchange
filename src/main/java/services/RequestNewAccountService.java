package services;

import beans.SberbankNewAccountRequest;
import exceptions.FlowException;
import jdbc.BeanConnect;
import jdbc.JDBCConnection;
import jdbc.TestConnect;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import javax.xml.transform.TransformerConfigurationException;
import java.io.File;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * @author: OnlyElena@mail.ru
 * Date: 4/22/16
 * Time: 1:38 PM
 */
public class RequestNewAccountService {
    List<BeanConnect> dataSource;
    LinkedHashMap<String, String> properties;
    String ddmmyy;
    String depCode;
    String ospName;
    String processName;
    String where;
    Hashtable<Integer, SberbankNewAccountXmlWriter> xmlNewAccountWriters;

    public static final SimpleDateFormat ddmmyyyy = new SimpleDateFormat("dd.MM.yyyy");
    public static final SimpleDateFormat yyyymmdd = new SimpleDateFormat("yyyy.MM.dd");


    final static Logger logger = Logger.getLogger(RequestNewAccountService.class);


    public RequestNewAccountService(List<BeanConnect> dataSource, LinkedHashMap<String, String> properties, String ddmmyy, String processName) throws FlowException {
        this.dataSource = dataSource;
        this.properties = properties;
        this.ddmmyy = ddmmyy;
        this.processName = processName;
        init();
    }

    /*В данном методе берем код отдела, его параметры и проверяем на доступность к работе БД*/
    private void init() throws FlowException {
        String smbConnect = dataSource.get(0).getUrl().substring(17, dataSource.get(0).getUrl().indexOf("/"));
        String query = "SELECT * FROM OSP";
//        JDBCConnection jdbcConnectionOld = new JDBCConnection();
        ResultSet resultSet = JDBCConnection.getInstance().jdbcConnection(dataSource, processName, query);
        if (resultSet == null) {
            System.out.println("Не удалось подключиться к БД, БД не доступна");
            MyLogger.get().logMessage(processName, "Не удалось подключиться к БД, БД не доступна");
            logger.error(processName + " Не удалось подключиться к БД, БД не доступна");
            return;
        }

        try {
            while (resultSet.next()) {
                ospName = resultSet.getString("DIV_NAME");
                depCode = resultSet.getObject("DEPARTMENT").toString();

//        только для Иркутской области Косяк Сбера
                if (depCode.equals("50")) depCode = "80";

                System.out.println("\nУспешное подключение к БД: " + resultSet.getObject("DIV_NAME"));
                MyLogger.get().logMessage(processName, "\nУспешное подключение к БД: " + resultSet.getObject("DIV_NAME"));
                logger.info(processName + " - Успешное подключение к БД: " + resultSet.getObject("DIV_NAME"));
            }

        } catch (SQLException e) {
            System.out.println("БД не доступна Не удалось получить параметры ОСП: " + smbConnect);
            MyLogger.get().logMessage(processName, "БД не доступна Не удалось получить параметры ОСП: " + smbConnect + "\n" + e.getMessage());
            logger.error(processName + " - БД не доступна Не удалось получить параметры ОСП: " + smbConnect + "\n" + e);

            JDBCConnection.getInstance().jdbcClose(processName);
            throw new FlowException("Не удалось получить параметры ОСП: " + smbConnect);
//            e.printStackTrace();
        }
        if (depCode == null) {
            MyLogger.get().logMessage(processName, "Не удалось получить код отдела из БД");
            logger.error(processName + " Не удалось получить код отдела из БД");
            throw new FlowException("Не удалось получить код отдела из БД");
        }
        try {
            resultSet.close();
        } catch (SQLException e) {
//            e.printStackTrace();
            MyLogger.get().logMessage(processName, " Не удалось закрыть выборку");
            logger.error(processName + " Не удалось закрыть выборку");
            throw new FlowException("Не удалось закрыть выборку");
        }
        JDBCConnection.getInstance().jdbcClose(processName);
        boolean testConnect = new TestConnect().TestConnect("http://" + smbConnect + ":8080/pksp-server/");
        if (testConnect == false) {
            MyLogger.get().logMessage(processName, " Не удалось подключиться по тонкому клиенту, БД на ремонте");
            logger.error(processName + " Не удалось подключиться по тонкому клиенту, БД на ремонте");
            throw new FlowException("Не удалось подключиться по тонкому клиенту, БД на ремонте");
        }
    }

    public void processCreate() throws FlowException {

        //прерываем обработку, если достигли лимит файлов для запросов
//                if (sberbankNewAccountXmlWriter != null) {
        fetchPackets();
        System.out.println("update zapros");
        MyLogger.get().logMessage(processName, "update zapros");
//                  jdbcTemplate.execute("UPDATE EXT_REQUEST SET PROCESSED = 1 WHERE PACK_ID = " + packetId);

//                }
//        }
    }


    private void fetchPackets() throws FlowException {
        int count = 0;
        int packetId = 0;
        SberbankNewAccountXmlWriter sberbankNewAccountXmlWriter = null;
        boolean pievCopyService;
//        Этот запрос для Запросов
        String query = "select DISTINCT \n" +
                "mda.acc as \"Счет должника\",\n" +
                "mres.ip_no  as \"Номер ИП\",\n" +
                "--d.doc_number,\n" +
                "d.id,\n" +
                "did.id_docno,\n" +
                "did.ip_rest_debtsum as \"Остаток долга\",\n" +
                "vh.i_idate as \"Дата ответа\",\n" +
                "did.id_dbtr_name as \"ФИО должника\",\n" +
                "did.id_dbtr_born as \"Дата рождения должника\",\n" +
                "did.id_dbtr_adr as \"Адрес должника\",\n" +
                "d.OSP_DEP_NAME,\n" +
                "did.ID_CRDR_NAME,\n" +
                "mdi.ser_doc,\n" +
                "mdi.num_doc,\n" +
                "mdi.issued_doc,\n" +
                "mdi.date_doc,\n" +
                "mdi.code_dep,\n" +
                "mdi.type_doc_code\n" +
                "from MVV_DATUM_ACCOUNT mda\n" +
                "join MVV_DATUM md on mda.id=md.id\n" +
                "join Mvv_External_Response mr on mr.id=md.origin_document_id\n" +
                "join MVV_RESPONSE mres on mres.id=mr.id\n" +
                "join i vh on vh.id=mres.id\n" +
                "join doc_ip_doc did on did.id=mres.ip_id\n" +
                "join document d on d.id=did.id\n" +
                "JOIN DOC_IP ON did.ID=DOC_IP.ID\n" +
                " INNER JOIN ENTITY ON DOC_IP.ID_DBTR = ENTITY.ENTT_ID\n" +
                " INNER JOIN MVV_DATUM mdd ON ENTITY.ENTT_ID = mdd.contragent_id\n" +
                " INNER JOIN MVV_DATUM_IDENTIFICATOR mdi on mdd.id = mdi.id\n" +
                "where\n" +
                "mres.query_type='O_IP_REQ_BANK_REG'\n" +
                "and d.docstatusid=9\n" +
                "and vh.agent_code = 'СБЕРБАНК'\n" +
                "and vh.agent_dept_code = 'СБЕРБАНКИРК'\n" +
                "and vh.agreement_code = 'СБЕРБАНКСОГЛ'\n" +
                "--and vh.i_idate >'01.01.2016'\n" +
                "--and vh.i_idate <'01.01.2017'\n" +
                "--and mt.telephone_no not containing '.'\n" +
                "AND (did.ID_CRDR_NAME like '%сбер%' or did.ID_CRDR_NAME like '% осб %')\n" +
                "AND (lower(did.ID_CRDR_NAME) not containing '%энергосб%' or lower(did.ID_CRDR_NAME) not containing '%росбанк%')\n" +
                "--group by mda.acc, mres.ip_no, d.id, did.id_docno, did.ip_rest_debtsum, vh.i_idate,did.id_dbtr_name,\n" +
                "--did.id_dbtr_born, did.id_dbtr_adr, d.OSP_DEP_NAME, did.ID_CRDR_NAME\n" +
                " AND mdi.type_doc_code in (01, 21)" +
                " AND mdi.ser_doc is not null" +
                " AND char_length(mdi.ser_doc)>3" +
                " AND mdi.num_doc is not null" +
                " AND mdi.date_doc is not null" +
                " AND mdi.code_dep is not null" +
                " AND (/*mdi.ser_doc>100 " +
                " OR*/ mdi.ser_doc not containing 'I'" +
                " AND mdi.ser_doc not containing 'V'" +
                " AND mdi.ser_doc not containing 'X'" +
                " AND char_length(mdi.ser_doc)>3" +
                " AND mdi.ser_doc not containing 'У'" +
                " AND mdi.ser_doc not containing 'В'" +
                " AND mdi.ser_doc not containing 'Х'" +
                " )" +
                "\n" +
                "\n";

//
//        "SELECT " +
//                "REQ_ID," +
//                "REQ_DATE," +
//                "FIO_SPI," +
//                "H_SPI," +
////                "SUBSTRING(H_SPI FROM 1 FOR POSITION(',' IN H_SPI)-1) AS H_PRISTAV," +
//                "IP_NUM," +
//                "IP_SUM," +
//                "ID_NUMBER," +
//                "EXT_REQUEST.ID_DATE," +
//                "ENTT_SURNAME," +
//                "ENTT_FIRSTNAME," +
//                "ENTT_PATRONYMIC," +
//                "DBTR_BORN_YEAR," +
//                "DEBTOR_ADDRESS," +
//                "DEBTOR_BIRTHDATE," +
//                "DEBTOR_BIRTHPLACE," +
//                "mdi.ser_doc," +
//                "mdi.num_doc," +
//                "mdi.issued_doc," +
//                "mdi.date_doc," +
//                "mdi.code_dep," +
//                "mdi.type_doc_code" +
//                " FROM" +
//                " DOCUMENT INNER JOIN EXT_REQUEST ON DOCUMENT.ID = EXT_REQUEST.IP_ID" +
//                " INNER JOIN DOC_IP ON DOCUMENT.ID = DOC_IP.ID" +
//                " INNER JOIN DOC_IP_DOC ON DOCUMENT.ID = DOC_IP_DOC.ID" +
//                " INNER JOIN ENTITY ON DOC_IP.ID_DBTR = ENTITY.ENTT_ID" +
//                " INNER JOIN MVV_DATUM md ON md.contragent_id = ENTITY.ENTT_ID" +
//                " INNER JOIN MVV_DATUM_IDENTIFICATOR mdi on mdi.id = md.id" +
//                wherePasport +
//                " AND PACK_ID = " + packetIdPasport + " " +
//                " AND mdi.type_doc_code in (01, 21)" +
//                " AND mdi.ser_doc is not null" +
//                " AND char_length(mdi.ser_doc)>3" +
//                " AND mdi.num_doc is not null" +
//                " AND mdi.date_doc is not null" +
//                " AND mdi.code_dep is not null" +
//                " AND (/*mdi.ser_doc>100 " +
//                " OR*/ mdi.ser_doc not containing 'I'" +
//                " AND mdi.ser_doc not containing 'V'" +
//                " AND mdi.ser_doc not containing 'X'" +
//                " AND char_length(mdi.ser_doc)>3" +
//                " AND mdi.ser_doc not containing 'У'" +
//                " AND mdi.ser_doc not containing 'В'" +
//                " AND mdi.ser_doc not containing 'Х'" +
//                " )" +
//                " GROUP BY" +
//                " REQ_ID, REQ_DATE, FIO_SPI, H_SPI, IP_NUM, IP_SUM, ID_NUMBER, EXT_REQUEST.ID_DATE," +
//                " ENTT_SURNAME, ENTT_FIRSTNAME, ENTT_PATRONYMIC, DBTR_BORN_YEAR, DEBTOR_ADDRESS," +
//                " DEBTOR_BIRTHDATE, DEBTOR_BIRTHPLACE, mdi.ser_doc, mdi.num_doc, mdi.issued_doc," +
//                " mdi.date_doc, mdi.code_dep, mdi.type_doc_code";
//



//        System.out.println("QUERY: " + query);
        List<SberbankNewAccountRequest> requests = new LinkedList<SberbankNewAccountRequest>();

        ResultSet resultSet = JDBCConnection.getInstance().jdbcConnection(dataSource, processName, query);
        if (resultSet == null) {
            System.out.println("Не удалось подключиться к БД, БД не доступна");
            MyLogger.get().logMessage(processName, "Не удалось подключиться к БД, БД не доступна");
            logger.error(processName + " - Не удалось подключиться к БД, БД не доступна");
            return;
        }
        try {
            while (resultSet.next()) {
                SberbankNewAccountRequest r = new SberbankNewAccountRequest();
//                r.setId(getLong(resultSet, "ID"));
//                r.setBarcode(getString(resultSet, "BARCODE"));
//                r.setExecProcDate(parseDate(getSqlDate(resultSet, "ip_risedate")));
//                r.setPriority(getInteger(resultSet, "priority_penalties"));
//                r.setSumm(getBigDecimal(resultSet, "total_dept_sum"));
//                r.setRequestDate(getDateDDMMYYYY(new Date(System.currentTimeMillis())));
                r.setRequestId(getLong(resultSet, "id"));
                r.setAccount(getString(resultSet, "acc"));
                r.setExecProcNum(getString(resultSet, "ip_no"));
                r.setExecActNum(getString(resultSet, "id_docno"));
                r.setFioDolg(getString(resultSet, "id_dbtr_name"));
                r.setDebtorBirthDate(parseDate(getSqlDate(resultSet, "id_dbtr_born")));
                r.setDebtorAddres(getString(resultSet, "id_dbtr_adr"));
                r.setCreditorName(getString(resultSet, "ID_CRDR_NAME"));
                r.setMdi_ser_doc(getString(resultSet, "ser_doc"));
                r.setMdi_num_doc(getString(resultSet, "num_doc"));
                r.setMdi_issued_doc(getString(resultSet, "issued_doc"));
                r.setMdi_date_doc(parseDate(getSqlDate(resultSet, "date_doc")));
                r.setMdi_code_dep(getString(resultSet, "code_dep"));
                r.setMdi_type_doc_code(getString(resultSet, "type_doc_code"));

//
//                r.setSumm(getBigDecimal(resultSet, "IP_SUM"));
//                r.setExecActDate(parseDate(getSqlDate(resultSet, "ID_DATE")));
//
//                r.setDebtorLastName(getString(resultSet, "ENTT_SURNAME"));
//                r.setDebtorFirstName(getString(resultSet, "ENTT_FIRSTNAME"));
//                r.setDebtorSecondName(getString(resultSet, "ENTT_PATRONYMIC"));
//                r.setDebtorBirthYear(getInteger(resultSet, "DBTR_BORN_YEAR"));
//                r.setDebtorAddres(getString(resultSet, "DEBTOR_ADDRESS"));
////            r.setDebtorBirthDate(parseDate(row.get("DEBTOR_BIRTHDATE").toString()));
//                r.setDebtorBirthDate(parseDate(getSqlDate(resultSet, "DEBTOR_BIRTHDATE")));
//                r.setDebtorBornAddres(getNN(resultSet.getObject("DEBTOR_BIRTHPLACE")));

                if (count == 0) sberbankNewAccountXmlWriter = openNewAccountXmlWriter(packetId);
                count = count + 1;
                requests.add(r);

                writeNewAccountRequest(packetId, r);

                if (count == 3000) {
                    count = 0;
                    packetId = packetId + 1;
                    sberbankNewAccountXmlWriter.close();

                }
            }

        } catch (SQLException e) {
            System.out.println("БД не доступна: " + ospName);
//            в случае возникновения исключительной ситуации, удаляем сформированные файлы
            System.err.println(e.getMessage() + " ОШИБКА в fetchPackets: ");
            MyLogger.get().logMessage(processName, e.getMessage() + " ОШИБКА в fetchPackets: ");
            logger.error(processName + " " + e.getMessage() + " ОШИБКА в fetchPackets: ");

            JDBCConnection.getInstance().jdbcClose(processName);
            throw new FlowException(processName + " " + e.getMessage() + " ОШИБКА в fetchPackets: ");
        }
    }


    private String getInteger(ResultSet row, String filedName) {
        Object o = null;
        try {
            o = row.getObject(filedName);
        } catch (SQLException e) {
            System.err.println("Field: " + filedName + ", are not Integer type. Actual type: " + o.getClass());
            MyLogger.get().logMessage(processName, "Field: " + filedName + ", are not Integer type. Actual type: " + o.getClass());
            logger.error("Field: " + filedName + ", are not Integer type. Actual type: " + o.getClass());
            return o.toString();
        }
        if (o == null) return null;

        if (o instanceof Integer) {
            return o.toString();
        } else {
            System.err.println("Field: " + filedName + ", are not Integer type. Actual type: " + o.getClass());
            MyLogger.get().logMessage(processName, "Field: " + filedName + ", are not Integer type. Actual type: " + o.getClass());
            logger.error("Field: " + filedName + ", are not Integer type. Actual type: " + o.getClass());
            return o.toString();
        }
    }

    private String getString(ResultSet row, String filedName) {
        Object o = null;
        try {
            o = row.getObject(filedName);
        } catch (SQLException e) {
            System.err.println("Field: " + filedName + ", are not String type. Actual type: " + o.getClass());
            MyLogger.get().logMessage(processName, "Field: " + filedName + ", are not String type. Actual type: " + o.getClass());
            logger.error("Field: " + filedName + ", are not String type. Actual type: " + o.getClass());
            return o.toString();
        }
        if (o == null) return null;

        if (o instanceof String) {
            return (String) o;
        } else {
            System.err.println("Field: " + filedName + ", are not String type. Actual type: " + o.getClass());
            MyLogger.get().logMessage(processName, "Field: " + filedName + ", are not String type. Actual type: " + o.getClass());
            logger.error("Field: " + filedName + ", are not String type. Actual type: " + o.getClass());
            return o.toString();
        }
    }

    private Long getLong(ResultSet row, String filedName) throws FlowException {
        Object o = null;
        try {
            o = row.getObject(filedName);
        } catch (SQLException e) {
            System.err.println("Field: " + filedName + ", are not String type. Actual type: " + o.getClass());
            MyLogger.get().logMessage(processName, "Field: " + filedName + ", are not String type. Actual type: " + o.getClass());
            logger.error("Field: " + filedName + ", are not String type. Actual type: " + o.getClass());
            throw new FlowException("Can't convert Long");
        }
        if (o == null) return null;

        if (o instanceof Long) {
            return (Long) o;
        } else {
            System.err.println("Field: " + filedName + ", are not String type. Actual type: " + o.getClass());
            MyLogger.get().logMessage(processName, "Field: " + filedName + ", are not String type. Actual type: " + o.getClass());
            logger.error("Field: " + filedName + ", are not String type. Actual type: " + o.getClass());
            throw new FlowException("Can't convert Long");
        }
    }

    private java.sql.Date getSqlDate(ResultSet row, String filedName) throws FlowException {
        Object o = null;
        try {
            o = row.getObject(filedName);
        } catch (SQLException e) {
            System.err.println("Field: " + filedName + ", are not java.sql.Date type. Actual type: " + o.getClass());
            MyLogger.get().logMessage(processName, "Field: " + filedName + ", are not java.sql.Date type. Actual type: " + o.getClass());
            logger.error("Field: " + filedName + ", are not java.sql.Date type. Actual type: " + o.getClass());
            throw new FlowException("Can't convert data");
        }
        if (o == null) return null;

        if (o instanceof java.sql.Date) {
            return (java.sql.Date) o;
        } else {
            System.err.println("Field: " + filedName + ", are not java.sql.Date type. Actual type: " + o.getClass());
            MyLogger.get().logMessage(processName, "Field: " + filedName + ", are not java.sql.Date type. Actual type: " + o.getClass());
            logger.error("Field: " + filedName + ", are not java.sql.Date type. Actual type: " + o.getClass());
            throw new FlowException("Can't convert data");
        }
    }

    private String getBigDecimal(ResultSet row, String filedName) throws FlowException {
        Object o = null;
        try {
            o = row.getObject(filedName);
        } catch (SQLException e) {
            System.err.println("Field: " + filedName + ", are not bigDecimal type. Actual type: " + o.getClass());
            MyLogger.get().logMessage(processName, "Field: " + filedName + ", are not bigDecimal type. Actual type: " + o.getClass());
            logger.error("Field: " + filedName + ", are not bigDecimal type. Actual type: " + o.getClass());
            return "0";
        }
        if (o == null) return "0";

        if (o instanceof BigDecimal) {
            return o.toString();
        } else {
            System.err.println("Field: " + filedName + ", are not bigDecimal type. Actual type: " + o.getClass());
            MyLogger.get().logMessage(processName, "Field: " + filedName + ", are not bigDecimal type. Actual type: " + o.getClass());
            logger.error("Field: " + filedName + ", are not bigDecimal type. Actual type: " + o.getClass());
            return "0";
        }
    }

    private String getNN(Object obj) {
        if (obj == null) return "";
        return obj.toString();
    }

    public static String getDateDDMMYYYY(Date date) {
        if (date == null) return null;
        return ddmmyyyy.format(date);
    }

    private String parseDate(java.sql.Date wrongDate) {
        SimpleDateFormat ddmmyyyy = new SimpleDateFormat("dd.MM.yyyy");
        if (wrongDate == null) return null;
        return ddmmyyyy.format(wrongDate);
    }


    /**
     * В этом методе создается ассоциативный массив, где ключ это номер пакета
     *
     * @param packetId
     * @throws FlowException
     * @throws TransformerConfigurationException
     * @throws SAXException
     * @throws FileNotFoundException
     */
    private SberbankNewAccountXmlWriter openNewAccountXmlWriter(int packetId) {
        if (xmlNewAccountWriters == null) xmlNewAccountWriters = new Hashtable<Integer, SberbankNewAccountXmlWriter>();
        SberbankNewAccountXmlWriter xmlNewAccountWriter = xmlNewAccountWriters.get(packetId);
        String kod_osb = properties.get("KOD_OSB");

        if (xmlNewAccountWriter == null) {
            String nextSberbankFileName = null;
            try {
                nextSberbankFileName = getNextSberbankFileName(xmlNewAccountWriters.size() + 1, depCode, kod_osb);
            } catch (FlowException e) {
                System.out.println("Ошибка в имени файла");
                MyLogger.get().logMessage(processName, "Ошибка в имени файла");
                logger.error(processName + "Ошибка в имени файла");
            }
            if (nextSberbankFileName == null) return null;

            try {
                xmlNewAccountWriter = new SberbankNewAccountXmlWriter(
                        properties.get("OUTPUT_DIRECTORY"),
                        nextSberbankFileName);
            } catch (FlowException e) {
                MyLogger.get().logMessage(processName, "Ошибка при записи файла " + e);
                logger.error(processName + " Ошибка при записи файла " + e);
            } catch (TransformerConfigurationException e) {
                MyLogger.get().logMessage(processName, e.toString());
                logger.error(processName + e.toString());
            } catch (SAXException e) {
                MyLogger.get().logMessage(processName, e.toString());
                logger.error(processName + e);
            } catch (FileNotFoundException e) {
                MyLogger.get().logMessage(processName, e.toString());
                logger.error(processName + e);
            }
            xmlNewAccountWriters.put(packetId, xmlNewAccountWriter);
            System.out.println("Имя файла: " + nextSberbankFileName);
            MyLogger.get().logMessage(processName, "Имя файла: " + nextSberbankFileName);
            logger.info(processName + " - Имя файла: " + nextSberbankFileName);
        }

        System.out.println("Подготовка к обработке пакета: " + packetId);
        MyLogger.get().logMessage(processName, "Подготовка к обработке пакета: " + packetId);
        logger.info(processName + " - Подготовка к обработке пакета: " + packetId);

        return xmlNewAccountWriter;
    }

    private void writeNewAccountRequest(int packetId, SberbankNewAccountRequest r) {
        try {
            getRequestNewAccountWriter(packetId).writeNewAccountRequest(r);
        } catch (FlowException e) {
            System.err.println("ОШИБКА: " + e.getMessage());
            MyLogger.get().logMessage(processName, "ОШИБКА: " + e.getMessage());
            logger.error(processName + " - ОШИБКА: " + e.getMessage());
        }
    }

    private SberbankNewAccountXmlWriter getRequestNewAccountWriter(int packetId) {
        return xmlNewAccountWriters.get(packetId);
    }


    /**
     * @param fileCount //порядковый номер файла запроса за текущий день (кажется от 1 до Z)
     * @param depCode   //код отдела
     * @return имя файла запроса
     */
    private String getNextSberbankFileName(int fileCount, String depCode, String kod_osb) throws FlowException {
        if (fileCount >= 35) return null; //достигнут лимит файлов для отправки

        Calendar inst = Calendar.getInstance();
        String day = new DecimalFormat("00").format(inst.get(Calendar.DAY_OF_MONTH));
        String month = Integer.toHexString(inst.get(Calendar.MONTH) + 1);
        System.out.println("порядковый номер файла = " + fileCount);
        MyLogger.get().logMessage(processName, "порядковый номер файла = " + fileCount);
        logger.info("порядковый номер файла = " + fileCount);
//        return "r" + day + month + "0018." + Integer.toHexString(fileCount) + depCode;
//        return "r" + day + month + kod_osb +"." + Integer.toHexString(fileCount) + depCode;
//        if (fileCount <= 9) return "r" + day + month + kod_osb + "." + fileCount + depCode;
//        return "r" + day + month + kod_osb + "." + Character.toString((char) (fileCount - 10 + 'a')) + depCode;
//        if (fileCount <= 9) return "r" + "204" + kod_osb + "." + fileCount + depCode;
//        return "r" + "204" + kod_osb + "." + Character.toString((char) (fileCount - 10 + 'a')) + depCode;
        if (fileCount <= 9) return "sber_" + "33"+fileCount+"_"+day+month+"8586."+ "xls";
        return "sber_" + "333_"+day+month+"8586." + Character.toString((char) (fileCount - 10 + 'a')) + "xls";

    }

    public void deleteCreatedFiles() {
        for (SberbankNewAccountXmlWriter writer : xmlNewAccountWriters.values()) {
            new File(writer.outputDirectory + writer.filename).delete();
        }
    }
}

