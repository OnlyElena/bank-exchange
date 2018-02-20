package services;

import beans.SberbankRequest;
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
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author: OnlyElena@mail.ru
 * Date: 4/15/16
 * Time: 8:00 PM
 */
public class RequestService {

    List<BeanConnect> dataSource;
    LinkedHashMap<String, String> properties;
    String dd;
    String depCode;
    String ospName;
    String processName;
    int count;
    String where;
    String wherePasport;
    Hashtable<String, SberbankXmlWriter> xmlWriters;
    Hashtable<String, SberbankXmlWriterPasport> xmlWritersPasport;

    public static final SimpleDateFormat ddmmyyyy = new SimpleDateFormat("dd.MM.yyyy");
    public static final SimpleDateFormat yyyymmdd = new SimpleDateFormat("yyyy.MM.dd");


    final static Logger logger = Logger.getLogger(RequestService.class);


    public RequestService(List<BeanConnect> dataSource, LinkedHashMap<String, String> properties, String dd, String processName) throws FlowException {
        this.dataSource = dataSource;
        this.properties = properties;
        this.dd = dd;
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
            throw new FlowException(processName + " Не удалось подключиться к БД, БД не доступна");

        }

        try {
            while (resultSet.next()) {
                ospName = resultSet.getString("DIV_NAME");
                depCode = resultSet.getObject("DEPARTMENT").toString();

//        только для Иркутской области Косяк Сбера
                if (depCode.equals("50"))depCode = "80";

                System.out.println("\nУспешное подключение к БД: " + resultSet.getObject("DIV_NAME"));
                MyLogger.get().logMessage(processName, "\nУспешное подключение к БД: " + resultSet.getObject("DIV_NAME"));
                logger.info(processName+" - Успешное подключение к БД: " + resultSet.getObject("DIV_NAME"));
            }

        } catch (SQLException e) {
            System.out.println("БД не доступна Не удалось получить параметры ОСП: " + smbConnect);
            MyLogger.get().logMessage(processName, "БД не доступна Не удалось получить параметры ОСП: " + smbConnect + "\n" + e.getMessage());
            logger.error(processName+" - БД не доступна Не удалось получить параметры ОСП: " + smbConnect + "\n" + e);

            JDBCConnection.getInstance().jdbcClose(processName);
            throw new FlowException("Не удалось получить параметры ОСП: " + smbConnect);
        }
        if (depCode == null) {
            MyLogger.get().logMessage(processName, "Не удалось получить код отдела из БД");
            logger.error(processName+" Не удалось получить код отдела из БД");
            throw new FlowException("Не удалось получить код отдела из БД");
        }
        try {
            resultSet.close();
        } catch (SQLException e) {
            MyLogger.get().logMessage(processName, " Не удалось закрыть выборку");
            logger.error(processName+" Не удалось закрыть выборку");
            throw new FlowException("Не удалось закрыть выборку");
        }
        JDBCConnection.getInstance().jdbcClose(processName);
        boolean testConnect = new TestConnect().TestConnect("http://" + smbConnect + ":8080/pksp-server/");
        if (testConnect == false) {
            MyLogger.get().logMessage(processName, " Не удалось подключиться по тонкому клиенту, БД на ремонте");
            logger.error(processName+" Не удалось подключиться по тонкому клиенту, БД на ремонте");
            throw new FlowException("Не удалось подключиться по тонкому клиенту, БД на ремонте");
        }
    }

    public void processCreate() throws FlowException {
        initWhereCondition();
        initWhereConditionPasport();


        List<String> packets = getPacketsNumbers();
        if (packets != null) {
            for (String packetId : packets) {
                SberbankXmlWriter sberbankXmlWriter = openXmlWriter(packetId);
                //прерываем обработку, если достигли лимит файлов для запросов
                if (sberbankXmlWriter != null) {
                    fetchPackets(packetId);
                    sberbankXmlWriter.close();
                    System.out.println("update zapros");
                    MyLogger.get().logMessage(processName, "update zapros");
//                  jdbcTemplate.execute("UPDATE EXT_REQUEST SET PROCESSED = 1 WHERE PACK_ID = " + packetId);

                }
            }
        }
        List<String> packetsPasport = getPacketsNumbersPasport();
        if (packetsPasport != null) {
            for (String packetIdPasport : packetsPasport) {
                SberbankXmlWriterPasport sberbankXmlWriterPasport = openXmlWriterPasport(packetIdPasport);
                //прерываем обработку, если достигли лимит файлов для запросов
                if (sberbankXmlWriterPasport != null) {
                    fetchPacketsPasport(packetIdPasport);
                    sberbankXmlWriterPasport.close();
                    System.out.println("update specifying zapros");
                    MyLogger.get().logMessage(processName, "update specifying zapros");
//                        jdbcTemplate.execute("UPDATE EXT_REQUEST SET PROCESSED = 1 WHERE PACK_ID = " + packetIdPasport);
                }
            }
        }

    }


    private void initWhereCondition() {
        String whereArr[] = {
                "MVV_AGREEMENT_CODE = '" + properties.get("MVV_AGREEMENT_CODE") + "'",
                "MVV_AGENT_CODE = '" + properties.get("MVV_AGENT_CODE") + "'",
                "MVV_AGENT_DEPT_CODE = '" + properties.get("MVV_AGENT_DEPT_CODE") + "'",
                "PROCESSED = 0",
                "ENTITY_TYPE IN (2, 71, 95, 96, 97, 666)"
        };
        StringBuilder whereBuilder = new StringBuilder();
        whereBuilder.append(" WHERE ");
        whereBuilder.append(whereArr[0]);
        for (int i = 1; i < whereArr.length; ++i) {
            whereBuilder.append(" AND ").append(whereArr[i]);
        }
        where = whereBuilder.toString();
//        System.out.println("WHERE: " + where);
    }

    private void initWhereConditionPasport() {
        String whereArr[] = {
                "MVV_AGREEMENT_CODE = '" + properties.get("PASPORT_MVV_AGREEMENT_CODE") + "'",
                "MVV_AGENT_CODE = '" + properties.get("PASPORT_MVV_AGENT_CODE") + "'",
                "MVV_AGENT_DEPT_CODE = '" + properties.get("PASPORT_MVV_AGENT_DEPT_CODE") + "'",
                "PROCESSED = 0",
                "ENTITY_TYPE IN (2, 71, 95, 96, 97, 666)"
        };
        StringBuilder whereBuilder = new StringBuilder();
        whereBuilder.append(" WHERE ");
        whereBuilder.append(whereArr[0]);
        for (int i = 1; i < whereArr.length; ++i) {
            whereBuilder.append(" AND ").append(whereArr[i]);
        }
        wherePasport = whereBuilder.toString();
//        System.out.println("WHERE PASPORT: " + wherePasport);
    }


    private List<String> getPacketsNumbers() throws FlowException {
        String query = "SELECT PACK_ID " +
                "FROM EXT_REQUEST "
                + where +
                "GROUP BY PACK_ID";

//        System.out.println(query);

        ResultSet resultSet = JDBCConnection.getInstance().jdbcConnection(dataSource, processName, query);
        if (resultSet == null) {
            System.out.println("Не удалось подключиться к БД, БД не доступна");
            MyLogger.get().logMessage(processName, "Не удалось подключиться к БД, БД не доступна");
            logger.error(processName + " - Не удалось подключиться к БД, БД не доступна");
            JDBCConnection.getInstance().jdbcClose(processName);
            return null;
        }
        LinkedList<String> packets = new LinkedList<String>();

        try {
            while (resultSet.next()) {
                packets.add(resultSet.getObject("PACK_ID").toString());
            }
            count = packets.size();
            System.out.println("Пакетов с запросами для отправки: " + packets.size());
            MyLogger.get().logMessage(processName, "Пакетов с запросами для отправки: " + packets.size());
            logger.info(processName + " - Пакетов с запросами для отправки: " + packets.size());

        } catch (SQLException e) {
            System.out.println("Не удалось подключиться и собрать пакеты");
            MyLogger.get().logMessage(processName, "Не удалось подключиться и собрать пакеты");
            logger.error(processName + " - Не удалось подключиться и собрать пакеты");
            JDBCConnection.getInstance().jdbcClose(processName);
            return null;
        }
        JDBCConnection.getInstance().jdbcClose(processName);
        return packets;
    }

    private List<String> getPacketsNumbersPasport() throws FlowException {
        String query = "SELECT PACK_ID " +
                "FROM EXT_REQUEST "
                + wherePasport +
                "GROUP BY PACK_ID";
//        System.out.println(query);

        ResultSet resultSet = JDBCConnection.getInstance().jdbcConnection(dataSource, processName, query);
        if (resultSet == null) {
            System.out.println("Не удалось подключиться к БД, БД не доступна");
            MyLogger.get().logMessage(processName, "Не удалось подключиться к БД, БД не доступна");
            logger.error(processName+" - Не удалось подключиться к БД, БД не доступна");
            JDBCConnection.getInstance().jdbcClose(processName);
            return null;
        }
        LinkedList<String> packets = new LinkedList<String>();

        try {
            while (resultSet.next()) {
                packets.add(resultSet.getObject("PACK_ID").toString());
            }
            System.out.println("Пакетов с уточняющими запросами для отправки: " + packets.size());
            MyLogger.get().logMessage(processName, "Пакетов с уточняющими запросами для отправки: " + packets.size());
            logger.info(processName + " - Пакетов с уточняющими запросами для отправки: " + packets.size());

        } catch (SQLException e) {
            System.out.println("Не удалось подключиться и собрать пакеты");
            MyLogger.get().logMessage(processName, "Не удалось подключиться и собрать пакеты");
            logger.error(processName + " - Не удалось подключиться и собрать пакеты");
            JDBCConnection.getInstance().jdbcClose(processName);
            return null;
        }
        JDBCConnection.getInstance().jdbcClose(processName);
        return packets;
    }


    private void fetchPackets(String packetId) throws FlowException {
        boolean pievCopyService;
//        Этот запрос для Запросов
        String query = "SELECT " +
                "REQ_ID," +
                "REQ_DATE," +
                "FIO_SPI," +
                "H_SPI," +
//                "SUBSTRING(H_SPI FROM 1 FOR POSITION(',' IN H_SPI)-1) AS H_PRISTAV," +
                "IP_NUM," +
                "IP_SUM," +
                "ID_NUMBER," +
                "EXT_REQUEST.ID_DATE," +
                "ENTT_SURNAME," +
                "ENTT_FIRSTNAME," +
                "ENTT_PATRONYMIC," +
                "DBTR_BORN_YEAR," +
                "DEBTOR_ADDRESS," +
                "DEBTOR_BIRTHDATE," +
                "DEBTOR_BIRTHPLACE" +
                " FROM" +
                " DOCUMENT INNER JOIN EXT_REQUEST ON DOCUMENT.ID = EXT_REQUEST.IP_ID" +
                " INNER JOIN DOC_IP ON DOCUMENT.ID = DOC_IP.ID" +
                " INNER JOIN DOC_IP_DOC ON DOCUMENT.ID = DOC_IP_DOC.ID" +
                " INNER JOIN ENTITY ON DOC_IP.ID_DBTR = ENTITY.ENTT_ID" +
                where +
                " AND PACK_ID = " + packetId + "";

//        System.out.println("QUERY: " + query);
        List<SberbankRequest> requests = new LinkedList<SberbankRequest>();

        ResultSet resultSet = JDBCConnection.getInstance().jdbcConnection(dataSource, processName, query);
        if (resultSet == null) {
            System.out.println("Не удалось подключиться к БД, БД не доступна");
            MyLogger.get().logMessage(processName, "Не удалось подключиться к БД, БД не доступна");
            logger.error(processName + " - Не удалось подключиться к БД, БД не доступна");
            throw new FlowException(processName + " - Не удалось подключиться к БД, БД не доступна");

        }
        try {
            while (resultSet.next()) {
                SberbankRequest r = new SberbankRequest();
//                r.setId(getLong(resultSet, "ID"));
//                r.setBarcode(getString(resultSet, "BARCODE"));
//                r.setExecProcDate(parseDate(getSqlDate(resultSet, "ip_risedate")));
//                r.setPriority(getInteger(resultSet, "priority_penalties"));
//                r.setSumm(getBigDecimal(resultSet, "total_dept_sum"));

                if (properties.get("COUNT_ID").equals("14")) {
//                System.out.println(row.get("REQ_ID").toString());
                    r.setDepartment(resultSet.getString("REQ_ID").substring(2, 4));
                } else if (properties.get("COUNT_ID").equals("13")) {
//                System.out.println(row.get("REQ_ID").toString());
                    r.setDepartment(resultSet.getString("REQ_ID").substring(1, 3));
                } else if (properties.get("COUNT_ID").equals("15")) {
//                System.out.println(row.get("REQ_ID").toString());
                    r.setDepartment(resultSet.getString("REQ_ID").substring(3, 5));
                } else if (properties.get("COUNT_ID").equals("16")) {
//                System.out.println(row.get("REQ_ID").toString());
                    r.setDepartment(resultSet.getString("REQ_ID").substring(4, 6));
                } else {
                    throw new FlowException("1.Не верно указан параметр COUNT_ID в config.txt");
                }
                r.setUserId("9998");
                r.setRequestTime("12:01");
                r.setRequestType("1");
                if (properties.get("KOD_OSB") == null) {
                    throw new FlowException("Не указан код ОСБ в config.txt");
                }
                r.setOsbList(properties.get("KOD_OSB"));
//            r.setOsbList("0018");
//            r.setDepartment(dep_code);
//            r.setExecutoryProcessId(Long.parseLong(row.get("REQ_ID").toString())); //это не ИД производства
//            r.setRequestId(row.get("REQ_ID").toString().substring(4)); //сокращаем до 10 знаков
                if (properties.get("COUNT_ID").equals("14")) {
                    r.setRequestId(resultSet.getString("REQ_ID").substring(4)); //сокращаем до 10 знаков
                } else if (properties.get("COUNT_ID").equals("13")) {
                    r.setRequestId(resultSet.getString("REQ_ID").substring(3)); //сокращаем до 10 знаков
                } else if (properties.get("COUNT_ID").equals("15")) {
                    r.setRequestId(resultSet.getString("REQ_ID").substring(5)); //сокращаем до 10 знаков
                } else if (properties.get("COUNT_ID").equals("16")) {
                    r.setRequestId(resultSet.getString("REQ_ID").substring(6)); //сокращаем до 10 знаков
                } else {
                    throw new FlowException("2.Не верно указан параметр COUNT_ID в config.txt");
                }
                r.setRequestDate(getDateDDMMYYYY(new Date(System.currentTimeMillis())));
                r.setBailiff(getString(resultSet, "FIO_SPI"));
//            r.setHeadBailiff(row.get("H_PRISTAV").toString());
                r.setHeadBailiff(getString(resultSet, "H_SPI"));
                r.setExecProcNum(getString(resultSet, "IP_NUM"));
                r.setSumm(getBigDecimal(resultSet, "IP_SUM"));
                r.setExecActNum(getString(resultSet, "ID_NUMBER"));
                r.setExecActDate(parseDate(getSqlDate(resultSet, "ID_DATE")));

                r.setDebtorLastName(getString(resultSet, "ENTT_SURNAME"));
                r.setDebtorFirstName(getString(resultSet, "ENTT_FIRSTNAME"));
                r.setDebtorSecondName(getString(resultSet, "ENTT_PATRONYMIC"));
                r.setDebtorBirthYear(getInteger(resultSet, "DBTR_BORN_YEAR"));
                r.setDebtorAddres(getString(resultSet, "DEBTOR_ADDRESS"));
//            r.setDebtorBirthDate(parseDate(row.get("DEBTOR_BIRTHDATE").toString()));
                r.setDebtorBirthDate(parseDate(getSqlDate(resultSet, "DEBTOR_BIRTHDATE")));
                r.setDebtorBornAddres(getNN(resultSet.getObject("DEBTOR_BIRTHPLACE")));

                requests.add(r);

                writeRequest(packetId, r);
            }

        } catch (SQLException e) {
            System.out.println("БД не доступна: " + ospName);
//            в случае возникновения исключительной ситуации, удаляем сформированные файлы
            System.err.println(e.getMessage() + "ОШИБКА в fetchPackets: ");
            MyLogger.get().logMessage(processName, e.getMessage() + "ОШИБКА в fetchPackets: ");
            logger.error(processName+" " + e.getMessage() + "ОШИБКА в fetchPackets: ");

            JDBCConnection.getInstance().jdbcClose(processName);
            throw new FlowException(processName+" " + e.getMessage() + "ОШИБКА в fetchPackets: ");
        }
        String query_up = "UPDATE EXT_REQUEST SET PROCESSED = 1 WHERE PACK_ID = " + packetId;

//        if (!requests.isEmpty()) {
            JDBCConnection.getInstance().jdbcUpdate(dataSource, processName, query_up);
//        } else {
//            JDBCConnection.getInstance().jdbcClose(processName);
//        }
    }


    private void fetchPacketsPasport(String packetIdPasport) throws FlowException {
        boolean pievCopyService;
//        Этот запрос для Запросов
        String query = "SELECT " +
                "REQ_ID," +
                "REQ_DATE," +
                "FIO_SPI," +
                "H_SPI," +
//                "SUBSTRING(H_SPI FROM 1 FOR POSITION(',' IN H_SPI)-1) AS H_PRISTAV," +
                "IP_NUM," +
                "IP_SUM," +
                "ID_NUMBER," +
                "EXT_REQUEST.ID_DATE," +
                "ENTT_SURNAME," +
                "ENTT_FIRSTNAME," +
                "ENTT_PATRONYMIC," +
                "DBTR_BORN_YEAR," +
                "DEBTOR_ADDRESS," +
                "DEBTOR_BIRTHDATE," +
                "DEBTOR_BIRTHPLACE," +
                "mdi.ser_doc," +
                "mdi.num_doc," +
                "mdi.issued_doc," +
                "mdi.date_doc," +
                "mdi.code_dep," +
                "mdi.type_doc_code" +
                " FROM" +
                " DOCUMENT INNER JOIN EXT_REQUEST ON DOCUMENT.ID = EXT_REQUEST.IP_ID" +
                " INNER JOIN DOC_IP ON DOCUMENT.ID = DOC_IP.ID" +
                " INNER JOIN DOC_IP_DOC ON DOCUMENT.ID = DOC_IP_DOC.ID" +
                " INNER JOIN ENTITY ON DOC_IP.ID_DBTR = ENTITY.ENTT_ID" +
                " INNER JOIN MVV_DATUM md ON md.contragent_id = ENTITY.ENTT_ID" +
                " INNER JOIN MVV_DATUM_IDENTIFICATOR mdi on mdi.id = md.id" +
                wherePasport +
                " AND PACK_ID = " + packetIdPasport + " " +
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
                " GROUP BY" +
                " REQ_ID, REQ_DATE, FIO_SPI, H_SPI, IP_NUM, IP_SUM, ID_NUMBER, EXT_REQUEST.ID_DATE," +
                " ENTT_SURNAME, ENTT_FIRSTNAME, ENTT_PATRONYMIC, DBTR_BORN_YEAR, DEBTOR_ADDRESS," +
                " DEBTOR_BIRTHDATE, DEBTOR_BIRTHPLACE, mdi.ser_doc, mdi.num_doc, mdi.issued_doc," +
                " mdi.date_doc, mdi.code_dep, mdi.type_doc_code";


//        System.out.println("QUERY: " + query);
        List<SberbankRequest> requests = new LinkedList<SberbankRequest>();

        ResultSet resultSet = JDBCConnection.getInstance().jdbcConnection(dataSource, processName, query);
        if (resultSet == null) {
            System.out.println("Не удалось подключиться к БД, БД не доступна");
            MyLogger.get().logMessage(processName, "Не удалось подключиться к БД, БД не доступна");
            logger.error(processName + " - Не удалось подключиться к БД, БД не доступна");
            throw new FlowException(processName + " - Не удалось подключиться к БД, БД не доступна");

        }

        try {
            while (resultSet.next()) {
                SberbankRequest r = new SberbankRequest();
//                r.setId(getLong(resultSet, "ID"));
//                r.setBarcode(getString(resultSet, "BARCODE"));
//                r.setExecProcDate(parseDate(getSqlDate(resultSet, "ip_risedate")));
//                r.setPriority(getInteger(resultSet, "priority_penalties"));
//                r.setSumm(getBigDecimal(resultSet, "total_dept_sum"));

                if (properties.get("COUNT_ID").equals("14")) {
//                System.out.println(row.get("REQ_ID").toString());
                    r.setDepartment(resultSet.getString("REQ_ID").substring(2, 4));
                } else if (properties.get("COUNT_ID").equals("13")) {
//                System.out.println(row.get("REQ_ID").toString());
                    r.setDepartment(resultSet.getString("REQ_ID").substring(1, 3));
                } else if (properties.get("COUNT_ID").equals("15")) {
//                System.out.println(row.get("REQ_ID").toString());
                    r.setDepartment(resultSet.getString("REQ_ID").substring(3, 5));
                } else if (properties.get("COUNT_ID").equals("16")) {
//                System.out.println(row.get("REQ_ID").toString());
                    r.setDepartment(resultSet.getString("REQ_ID").substring(4, 6));
                } else {
                    throw new FlowException("1.Не верно указан параметр COUNT_ID в config.txt");
                }
                r.setUserId("9998");
                r.setRequestTime("12:01");
                r.setRequestType("1");
                if (properties.get("KOD_OSB") == null) {
                    throw new FlowException("Не указан код ОСБ в config.txt");
                }
                r.setOsbList(properties.get("KOD_OSB"));
//            r.setOsbList("0018");
//            r.setDepartment(dep_code);
//            r.setExecutoryProcessId(Long.parseLong(row.get("REQ_ID").toString())); //это не ИД производства
//            r.setRequestId(row.get("REQ_ID").toString().substring(4)); //сокращаем до 10 знаков
                if (properties.get("COUNT_ID").equals("14")) {
                    r.setRequestId(resultSet.getString("REQ_ID").substring(4)); //сокращаем до 10 знаков
                } else if (properties.get("COUNT_ID").equals("13")) {
                    r.setRequestId(resultSet.getString("REQ_ID").substring(3)); //сокращаем до 10 знаков
                } else if (properties.get("COUNT_ID").equals("15")) {
                    r.setRequestId(resultSet.getString("REQ_ID").substring(5)); //сокращаем до 10 знаков
                } else if (properties.get("COUNT_ID").equals("16")) {
                    r.setRequestId(resultSet.getString("REQ_ID").substring(6)); //сокращаем до 10 знаков
                } else {
                    throw new FlowException("2.Не верно указан параметр COUNT_ID в config.txt");
                }
                r.setRequestDate(getDateDDMMYYYY(new Date(System.currentTimeMillis())));
                r.setBailiff(getString(resultSet, "FIO_SPI"));
//            r.setHeadBailiff(row.get("H_PRISTAV").toString());
                r.setHeadBailiff(getString(resultSet, "H_SPI"));
                r.setExecProcNum(getString(resultSet, "IP_NUM"));
                r.setSumm(getBigDecimal(resultSet, "IP_SUM"));
                r.setExecActNum(getString(resultSet, "ID_NUMBER"));
                r.setExecActDate(parseDate(getSqlDate(resultSet, "ID_DATE")));

                r.setDebtorLastName(getString(resultSet, "ENTT_SURNAME"));
                r.setDebtorFirstName(getString(resultSet, "ENTT_FIRSTNAME"));
                r.setDebtorSecondName(getString(resultSet, "ENTT_PATRONYMIC"));
                r.setDebtorBirthYear(getInteger(resultSet, "DBTR_BORN_YEAR"));
                r.setDebtorAddres(getString(resultSet, "DEBTOR_ADDRESS"));
//            r.setDebtorBirthDate(parseDate(row.get("DEBTOR_BIRTHDATE").toString()));
                r.setDebtorBirthDate(parseDate(getSqlDate(resultSet, "DEBTOR_BIRTHDATE")));
                r.setDebtorBornAddres(getNN(resultSet.getObject("DEBTOR_BIRTHPLACE")));
                String ss = getString(resultSet, "ser_doc").trim().replaceAll(" ", "");
//                System.out.println(ss);
                r.setPasportSer(ss.substring(0, 2) + " " + ss.substring(2, ss.length()));
//                System.out.println(ss.substring(0,2)+" "+ss.substring(2,ss.length()));
                r.setPasportNum(getString(resultSet, "num_doc").replaceAll(" ", "").trim());
                r.setPasportIssued(getString(resultSet, "issued_doc"));
                r.setPasportType(getString(resultSet, "type_doc_code"));
                r.setPasportCod(getString(resultSet, "code_dep"));
                r.setPasportDate(parseDate(getSqlDate(resultSet, "date_doc")));

                requests.add(r);

                writeRequestPasport(packetIdPasport, r);
            }

        } catch (SQLException e) {
            System.out.println("БД не доступна: " + ospName);
//            в случае возникновения исключительной ситуации, удаляем сформированные файлы
            System.err.println(e.getMessage() + "ОШИБКА в fetchPackets: ");
            MyLogger.get().logMessage(processName, e.getMessage() + "ОШИБКА в fetchPackets: ");
            logger.error(processName+" " + e.getMessage() + "ОШИБКА в fetchPackets: ");

            JDBCConnection.getInstance().jdbcClose(processName);
            throw new FlowException(processName+" " + e.getMessage() + "ОШИБКА в fetchPackets: ");
        }
        String query_upPasport = "UPDATE EXT_REQUEST SET PROCESSED = 1 WHERE PACK_ID = " + packetIdPasport;

//        if (!requests.isEmpty()) {
            JDBCConnection.getInstance().jdbcUpdate(dataSource, processName, query_upPasport);
//        } else {
//            JDBCConnection.getInstance().jdbcClose(processName);
//        }
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
    private SberbankXmlWriter openXmlWriter(String packetId) {
        if (xmlWriters == null) xmlWriters = new Hashtable<String, SberbankXmlWriter>();
        SberbankXmlWriter xmlWriter = xmlWriters.get(packetId);
        String kod_osb = properties.get("KOD_OSB");

        if (xmlWriter == null) {
            String nextSberbankFileName = null;
            try {
                nextSberbankFileName = getNextSberbankFileName(xmlWriters.size() + 1, depCode, kod_osb);
            } catch (FlowException e) {
                System.out.println("Ошибка в имени файла");
                MyLogger.get().logMessage(processName, "Ошибка в имени файла");
                logger.error(processName + "Ошибка в имени файла");
            }
            if (nextSberbankFileName == null) return null;

            try {
                xmlWriter = new SberbankXmlWriter(
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
            xmlWriters.put(packetId, xmlWriter);
            System.out.println("Имя файла: " + nextSberbankFileName);
            MyLogger.get().logMessage(processName, "Имя файла: " + nextSberbankFileName);
            logger.info(processName+" - Имя файла: " + nextSberbankFileName);
        }

        System.out.println("Подготовка к обработке пакета: " + packetId);
        MyLogger.get().logMessage(processName, "Подготовка к обработке пакета: " + packetId);
        logger.info(processName+" - Подготовка к обработке пакета: " + packetId);

        return xmlWriter;
    }

    private SberbankXmlWriterPasport openXmlWriterPasport(String packetIdPasport) {
        if (xmlWritersPasport == null) xmlWritersPasport = new Hashtable<String, SberbankXmlWriterPasport>();
        SberbankXmlWriterPasport xmlWriterPasport = xmlWritersPasport.get(packetIdPasport);
        String kod_osb = properties.get("KOD_OSB");
        if (xmlWriterPasport == null) {
            String nextSberbankFileName = null;
            try {
                nextSberbankFileName = getNextSberbankFileName(count + xmlWritersPasport.size() + 1, depCode, kod_osb);
            } catch (FlowException e) {
                System.out.println("Ошибка в имени файла");
                MyLogger.get().logMessage(processName, "Ошибка в имени файла");
                logger.error(processName + "Ошибка в имени файла");
            }
            if (nextSberbankFileName == null) return null;

            try {
                xmlWriterPasport = new SberbankXmlWriterPasport(
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
            xmlWritersPasport.put(packetIdPasport, xmlWriterPasport);
            System.out.println("Имя файла: " + nextSberbankFileName);
            MyLogger.get().logMessage(processName, "Имя файла: " + nextSberbankFileName);
            logger.info(processName + " - Имя файла: " + nextSberbankFileName);
        }

        System.out.println("Подготовка к обработке пакета: " + packetIdPasport);
        MyLogger.get().logMessage(processName, "Подготовка к обработке пакета: " + packetIdPasport);
        logger.info(processName + " - Подготовка к обработке пакета: " + packetIdPasport);

        return xmlWriterPasport;
    }


    private void writeRequest(String packetId, SberbankRequest r) {
        try {
            getRequestWriter(packetId).writeRequest(r);
        } catch (FlowException e) {
            System.err.println("ОШИБКА: " + e.getMessage());
            MyLogger.get().logMessage(processName, "ОШИБКА: " + e.getMessage());
            logger.error(processName+" - ОШИБКА: " + e.getMessage());
        }
    }

    private void writeRequestPasport(String packetIdPasport, SberbankRequest r) {
        try {
            getRequestWriterPasport(packetIdPasport).writeRequestPasport(r);
        } catch (FlowException e) {
            System.err.println("ОШИБКА: " + e.getMessage());
            MyLogger.get().logMessage(processName, "ОШИБКА: " + e.getMessage());
            logger.error(processName+" - ОШИБКА: " + e.getMessage());
        }
    }


    private SberbankXmlWriter getRequestWriter(String packetId) {
        return xmlWriters.get(packetId);
    }

    private SberbankXmlWriterPasport getRequestWriterPasport(String packetIdPasport) {
        return xmlWritersPasport.get(packetIdPasport);
    }


    /**
     * @param fileCount //порядковый номер файла запроса за текущий день (кажется от 1 до Z)
     * @param depCode   //код отдела
     * @return имя файла запроса
     */
    private String getNextSberbankFileName(int fileCount, String depCode, String kod_osb) throws FlowException {
        if (fileCount >= 35) return null; //достигнут лимит файлов для отправки

        Calendar inst = Calendar.getInstance();
//        String day = new DecimalFormat("00").format(inst.get(Calendar.DAY_OF_MONTH));
        String day = dd;
        String month = Integer.toHexString(inst.get(Calendar.MONTH) + 1);
        System.out.println("порядковый номер файла = " + fileCount);
        MyLogger.get().logMessage(processName, "порядковый номер файла = " + fileCount);
        logger.info("порядковый номер файла = " + fileCount);
//        return "r" + day + month + "0018." + Integer.toHexString(fileCount) + depCode;
//        return "r" + day + month + kod_osb +"." + Integer.toHexString(fileCount) + depCode;
        if (fileCount <= 9) return "r" + day + month + kod_osb + "." + fileCount + depCode;
        return "r" + day + month + kod_osb + "." + Character.toString((char) (fileCount - 10 + 'a')) + depCode;
//        if (fileCount <= 9) return "r" + "204" + kod_osb + "." + fileCount + depCode;
//        return "r" + "204" + kod_osb + "." + Character.toString((char) (fileCount - 10 + 'a')) + depCode;

    }

    public void deleteCreatedFiles() {
        if (!xmlWriters.isEmpty()){
        for (SberbankXmlWriter writer : xmlWriters.values()) {
            new File(writer.outputDirectory + writer.filename).delete();
        }}
        if (!xmlWritersPasport.isEmpty()){
        for (SberbankXmlWriterPasport writerPasport : xmlWritersPasport.values()) {
            new File(writerPasport.outputDirectory + writerPasport.filename).delete();
        }}
    }
}

