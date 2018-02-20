package service;

import beans.RequestBanks;
import exceptions.FlowException;
import jdbc.BeanConnectBanks;
import jdbc.JDBCConnectionBanks;
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
 * Date: 6/18/16
 * Time: 11:18 PM
 */
public class RequestServiceBanks {
    List<BeanConnectBanks> dataSourceBanks;
    LinkedHashMap<String, String> propertiesBanks;
    String ddBanks;
    String depCodeBanks;
    String ospNameBanks;
    String processNameBanks;
    int countBanks;
    String whereBanks;
    String wherePasportBanks;
    Hashtable<String, XmlWriterBanks> xmlWritersBanks;
    Hashtable<String, XmlWriterPasportBanks> xmlWritersPasportBanks;

    public static final SimpleDateFormat ddmmyyyy = new SimpleDateFormat("dd.MM.yyyy");
    public static final SimpleDateFormat yyyymmdd = new SimpleDateFormat("yyyy.MM.dd");


    final static Logger logger = Logger.getLogger(RequestServiceBanks.class);


    public RequestServiceBanks(List<BeanConnectBanks> dataSourceBanks, LinkedHashMap<String, String> propertiesBanks, String ddBanks, String processNameBanks) throws FlowException {
        this.dataSourceBanks = dataSourceBanks;
        this.propertiesBanks = propertiesBanks;
        this.ddBanks = ddBanks;
        this.processNameBanks = processNameBanks;
        initBanks();
    }

    /*В данном методе берем код отдела, его параметры и проверяем на доступность к работе БД*/
    private void initBanks() throws FlowException {
        String smbConnectBanks = dataSourceBanks.get(0).getUrl().substring(17, dataSourceBanks.get(0).getUrl().indexOf("/"));
        String queryBanks = "SELECT * FROM OSP";
//        JDBCConnection jdbcConnectionOld = new JDBCConnection();
        ResultSet resultSetBanks = JDBCConnectionBanks.getInstance().jdbcConnection(dataSourceBanks, processNameBanks, queryBanks);
        if (resultSetBanks == null) {
            System.out.println("Не удалось подключиться к БД, БД не доступна");
            MyLoggerBanks.get().logMessage(processNameBanks, "Не удалось подключиться к БД, БД не доступна");
            logger.error(processNameBanks + " Не удалось подключиться к БД, БД не доступна");
            throw new FlowException(processNameBanks + " Не удалось подключиться к БД, БД не доступна");
        }

        try {
            while (resultSetBanks.next()) {
                ospNameBanks = resultSetBanks.getString("DIV_NAME");
                depCodeBanks = resultSetBanks.getObject("DEPARTMENT").toString();

//        только для Иркутской области Косяк Сбера
//                if (depCodeBanks.equals("50"))depCodeBanks = "80";

                System.out.println("\nУспешное подключение к БД: " + resultSetBanks.getObject("DIV_NAME"));
                MyLoggerBanks.get().logMessage(processNameBanks, "\nУспешное подключение к БД: " + resultSetBanks.getObject("DIV_NAME"));
                logger.info(processNameBanks + " - Успешное подключение к БД: " + resultSetBanks.getObject("DIV_NAME"));
            }

        } catch (SQLException e) {
            System.out.println("БД не доступна Не удалось получить параметры ОСП: " + smbConnectBanks);
            MyLoggerBanks.get().logMessage(processNameBanks, "БД не доступна Не удалось получить параметры ОСП: " + smbConnectBanks + "\n" + e.getMessage());
            logger.error(processNameBanks + " - БД не доступна Не удалось получить параметры ОСП: " + smbConnectBanks + "\n" + e);

            JDBCConnectionBanks.getInstance().jdbcClose(processNameBanks);
            throw new FlowException("Не удалось получить параметры ОСП: " + smbConnectBanks);
        }
        if (depCodeBanks == null) {
            MyLoggerBanks.get().logMessage(processNameBanks, "Не удалось получить код отдела из БД");
            logger.error(processNameBanks + " Не удалось получить код отдела из БД");
            throw new FlowException("Не удалось получить код отдела из БД");
        }
        try {
            resultSetBanks.close();
        } catch (SQLException e) {
            MyLoggerBanks.get().logMessage(processNameBanks, " Не удалось закрыть выборку");
            logger.error(processNameBanks + " Не удалось закрыть выборку");
            throw new FlowException("Не удалось закрыть выборку");
        }
        JDBCConnectionBanks.getInstance().jdbcClose(processNameBanks);
        boolean testConnect = new TestConnect().TestConnect("http://" + smbConnectBanks + ":8080/pksp-server/");
        if (testConnect == false) {
            MyLoggerBanks.get().logMessage(processNameBanks, " Не удалось подключиться по тонкому клиенту, БД на ремонте");
            logger.error(processNameBanks + " Не удалось подключиться по тонкому клиенту, БД на ремонте");
            throw new FlowException("Не удалось подключиться по тонкому клиенту, БД на ремонте");
        }
    }

    public void processCreateBanks() throws FlowException {
        initWhereConditionBanks();
//        initWhereConditionPasportBanks();


        List<String> packetsBanks = getPacketsNumbersBanks();
        if (packetsBanks != null) {
            for (String packetIdBanks : packetsBanks) {
                XmlWriterBanks XmlWriterBanks = openXmlWriterBanks(packetIdBanks);
                //прерываем обработку, если достигли лимит файлов для запросов
                if (XmlWriterBanks != null) {
                    fetchPacketsBanks(packetIdBanks);
                    XmlWriterBanks.close();
                    System.out.println("update zapros");
                    MyLoggerBanks.get().logMessage(processNameBanks, "update zapros");
//                  jdbcTemplate.execute("UPDATE EXT_REQUEST SET PROCESSED = 1 WHERE PACK_ID = " + packetId);

                }
            }
        }
//        List<String> packetsPasportBanks = getPacketsNumbersPasportBanks();
//        if (packetsPasportBanks != null) {
//            for (String packetIdPasportBanks : packetsPasportBanks) {
//                XmlWriterPasportBanks XmlWriterPasportBanks = openXmlWriterPasportBanks(packetIdPasportBanks);
//                //прерываем обработку, если достигли лимит файлов для запросов
//                if (XmlWriterPasportBanks != null) {
//                    fetchPacketsPasportBanks(packetIdPasportBanks);
//                    XmlWriterPasportBanks.close();
//                    System.out.println("update specifying zapros");
//                    MyLoggerBanks.get().logMessage(processNameBanks, "update specifying zapros");
////                        jdbcTemplate.execute("UPDATE EXT_REQUEST SET PROCESSED = 1 WHERE PACK_ID = " + packetIdPasport);
//                }
//            }
//        }

    }


    private void initWhereConditionBanks() {
        String whereArr[] = {
                "MVV_AGREEMENT_CODE = '" + propertiesBanks.get("MVV_AGREEMENT_CODE") + "'",
                "MVV_AGENT_CODE = '" + propertiesBanks.get("MVV_AGENT_CODE") + "'",
                "MVV_AGENT_DEPT_CODE = '" + propertiesBanks.get("MVV_AGENT_DEPT_CODE") + "'",
                "PROCESSED = 0",
//                "ENTITY_TYPE IN (2, 71, 95, 96, 97, 666)"
        };
        StringBuilder whereBuilder = new StringBuilder();
        whereBuilder.append(" WHERE ");
        whereBuilder.append(whereArr[0]);
        for (int i = 1; i < whereArr.length; ++i) {
            whereBuilder.append(" AND ").append(whereArr[i]);
        }
        whereBanks = whereBuilder.toString();
//        System.out.println("WHERE: " + whereBanks);
    }

    private void initWhereConditionPasportBanks() {
        String whereArr[] = {
                "MVV_AGREEMENT_CODE = '" + propertiesBanks.get("PASPORT_MVV_AGREEMENT_CODE") + "'",
                "MVV_AGENT_CODE = '" + propertiesBanks.get("PASPORT_MVV_AGENT_CODE") + "'",
                "MVV_AGENT_DEPT_CODE = '" + propertiesBanks.get("PASPORT_MVV_AGENT_DEPT_CODE") + "'",
                "PROCESSED = 0",
                "ENTITY_TYPE IN (2, 71, 95, 96, 97, 666)"
        };
        StringBuilder whereBuilder = new StringBuilder();
        whereBuilder.append(" WHERE ");
        whereBuilder.append(whereArr[0]);
        for (int i = 1; i < whereArr.length; ++i) {
            whereBuilder.append(" AND ").append(whereArr[i]);
        }
        wherePasportBanks = whereBuilder.toString();
//        System.out.println("WHERE PASPORT: " + wherePasportBanks);
    }


    private List<String> getPacketsNumbersBanks() throws FlowException {
        String queryBanks = "SELECT PACK_ID " +
                "FROM EXT_REQUEST "
                + whereBanks +
                "GROUP BY PACK_ID";

//        System.out.println(queryBanks);

        ResultSet resultSet = JDBCConnectionBanks.getInstance().jdbcConnection(dataSourceBanks, processNameBanks, queryBanks);
        if (resultSet == null) {
            System.out.println("Не удалось подключиться к БД, БД не доступна");
            MyLoggerBanks.get().logMessage(processNameBanks, "Не удалось подключиться к БД, БД не доступна");
            logger.error(processNameBanks + " - Не удалось подключиться к БД, БД не доступна");
            JDBCConnectionBanks.getInstance().jdbcClose(processNameBanks);
            return null;
        }
        LinkedList<String> packets = new LinkedList<String>();

        try {
            while (resultSet.next()) {
                packets.add(resultSet.getObject("PACK_ID").toString());
            }
            countBanks = packets.size();
            System.out.println("Пакетов с запросами для отправки: " + packets.size());
            MyLoggerBanks.get().logMessage(processNameBanks, "Пакетов с запросами для отправки: " + packets.size());
            logger.info(processNameBanks + " - Пакетов с запросами для отправки: " + packets.size());

        } catch (SQLException e) {
            System.out.println("Не удалось подключиться и собрать пакеты");
            MyLoggerBanks.get().logMessage(processNameBanks, "Не удалось подключиться и собрать пакеты");
            logger.error(processNameBanks + " - Не удалось подключиться и собрать пакеты");
            JDBCConnectionBanks.getInstance().jdbcClose(processNameBanks);
            return null;
        }
        JDBCConnectionBanks.getInstance().jdbcClose(processNameBanks);
        return packets;
    }

    private List<String> getPacketsNumbersPasportBanks() throws FlowException {
        String queryBanks = "SELECT PACK_ID " +
                "FROM EXT_REQUEST "
                + wherePasportBanks +
                "GROUP BY PACK_ID";
//        System.out.println(query);

        ResultSet resultSet = JDBCConnectionBanks.getInstance().jdbcConnection(dataSourceBanks, processNameBanks, queryBanks);
        if (resultSet == null) {
            System.out.println("Не удалось подключиться к БД, БД не доступна");
            MyLoggerBanks.get().logMessage(processNameBanks, "Не удалось подключиться к БД, БД не доступна");
            logger.error(processNameBanks + " - Не удалось подключиться к БД, БД не доступна");
            JDBCConnectionBanks.getInstance().jdbcClose(processNameBanks);
            return null;
        }
        LinkedList<String> packetsBanks = new LinkedList<String>();

        try {
            while (resultSet.next()) {
                packetsBanks.add(resultSet.getObject("PACK_ID").toString());
            }
            System.out.println("Пакетов с уточняющими запросами для отправки: " + packetsBanks.size());
            MyLoggerBanks.get().logMessage(processNameBanks, "Пакетов с уточняющими запросами для отправки: " + packetsBanks.size());
            logger.info(processNameBanks + " - Пакетов с уточняющими запросами для отправки: " + packetsBanks.size());

        } catch (SQLException e) {
            System.out.println("Не удалось подключиться и собрать пакеты");
            MyLoggerBanks.get().logMessage(processNameBanks, "Не удалось подключиться и собрать пакеты");
            logger.error(processNameBanks + " - Не удалось подключиться и собрать пакеты");
            JDBCConnectionBanks.getInstance().jdbcClose(processNameBanks);
            return null;
        }
        JDBCConnectionBanks.getInstance().jdbcClose(processNameBanks);
        return packetsBanks;
    }


    private void fetchPacketsBanks(String packetIdBanks) throws FlowException {
//        Этот запрос для Запросов
        String queryBanks = "SELECT " +
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
                "DEBTOR_NAME, " +
                "ENTT_INN, " +
                "ENTT_KPP, " +
                "ENTITY_TYPE, " +
                "DBTR_BORN_YEAR," +
                "DEBTOR_ADDRESS," +
                "DEBTOR_BIRTHDATE," +
                "DEBTOR_BIRTHPLACE, " +
//                "EXT_REQUEST.ID_SUBJECT_TYPE, " +
                "EXT_REQUEST.IP_REST_DEBTSUM " +
                "IP_REST_DEBTSUM " +
                " FROM" +
                " DOCUMENT INNER JOIN EXT_REQUEST ON DOCUMENT.ID = EXT_REQUEST.IP_ID" +
                " INNER JOIN DOC_IP ON DOCUMENT.ID = DOC_IP.ID" +
                " INNER JOIN DOC_IP_DOC ON DOCUMENT.ID = DOC_IP_DOC.ID" +
                " INNER JOIN ENTITY ON DOC_IP.ID_DBTR = ENTITY.ENTT_ID" +
                whereBanks +
                " AND PACK_ID = " + packetIdBanks + "";

//        System.out.println("QUERY: " + queryBanks);
        List<RequestBanks> requestsBanks = new LinkedList<RequestBanks>();

        ResultSet resultSetBanks = JDBCConnectionBanks.getInstance().jdbcConnection(dataSourceBanks, processNameBanks, queryBanks);
        if (resultSetBanks == null) {
            System.out.println("Не удалось подключиться к БД, БД не доступна");
            MyLoggerBanks.get().logMessage(processNameBanks, "Не удалось подключиться к БД, БД не доступна");
            logger.error(processNameBanks + " - Не удалось подключиться к БД, БД не доступна");
            throw new FlowException(processNameBanks + " - Не удалось подключиться к БД, БД не доступна");
        }
        try {
            while (resultSetBanks.next()) {
                RequestBanks rr = new RequestBanks();
//                r.setId(getLong(resultSet, "ID"));
//                r.setBarcode(getString(resultSet, "BARCODE"));
//                r.setExecProcDate(parseDate(getSqlDate(resultSet, "ip_risedate")));
//                r.setPriority(getInteger(resultSet, "priority_penalties"));
//                r.setSumm(getBigDecimal(resultSet, "total_dept_sum"));

                if (propertiesBanks.get("COUNT_ID").equals("14")) {
//                System.out.println(row.get("REQ_ID").toString());
                    rr.setDepartment(resultSetBanks.getString("REQ_ID").substring(2, 4));
                } else if (propertiesBanks.get("COUNT_ID").equals("13")) {
//                System.out.println(row.get("REQ_ID").toString());
                    rr.setDepartment(resultSetBanks.getString("REQ_ID").substring(1, 3));
                } else if (propertiesBanks.get("COUNT_ID").equals("15")) {
//                System.out.println(row.get("REQ_ID").toString());
                    rr.setDepartment(resultSetBanks.getString("REQ_ID").substring(3, 5));
                } else if (propertiesBanks.get("COUNT_ID").equals("16")) {
//                System.out.println(row.get("REQ_ID").toString());
                    rr.setDepartment(resultSetBanks.getString("REQ_ID").substring(4, 6));
                } else {
                    throw new FlowException("1.Не верно указан параметр COUNT_ID в config.txt");
                }
                rr.setUserId("9998");
                rr.setRequestTime("12:01");
                //            2, 71, 95, 96, 97, 666
//                rr.setRequestType("1");
//            System.out.println(row.get("ENTITY_TYPE"));
                if (resultSetBanks.getString("ENTITY_TYPE").toString().equals("2")
                        |resultSetBanks.getString("ENTITY_TYPE").toString().equals("71")
                        |resultSetBanks.getString("ENTITY_TYPE").toString().equals("95")
                        |resultSetBanks.getString("ENTITY_TYPE").toString().equals("96")
                        |resultSetBanks.getString("ENTITY_TYPE").toString().equals("97")
                        |resultSetBanks.getString("ENTITY_TYPE").toString().equals("666"))  {
//                System.out.println("1111111111111111111111111111111111");
                    rr.setRequestType("1");
                    System.out.println("1");
                } else {
                    rr.setRequestType("2");
                    System.out.println();
                }

                if (propertiesBanks.get("KOD_OSB") == null) {
                    throw new FlowException("Не указан код ОСБ в config.txt");
                }
                rr.setOsbList(propertiesBanks.get("KOD_OSB"));
//            r.setOsbList("0018");
//            r.setDepartment(dep_code);
//            r.setExecutoryProcessId(Long.parseLong(row.get("REQ_ID").toString())); //это не ИД производства
//            r.setRequestId(row.get("REQ_ID").toString().substring(4)); //сокращаем до 10 знаков
//                if (propertiesBanks.get("COUNT_ID").equals("14")) {
//                    rr.setRequestId(resultSetBanks.getString("REQ_ID").substring(4)); //сокращаем до 10 знаков
//                } else if (propertiesBanks.get("COUNT_ID").equals("13")) {
//                    rr.setRequestId(resultSetBanks.getString("REQ_ID").substring(3)); //сокращаем до 10 знаков
//                } else if (propertiesBanks.get("COUNT_ID").equals("15")) {
//                    rr.setRequestId(resultSetBanks.getString("REQ_ID").substring(5)); //сокращаем до 10 знаков
//                } else if (propertiesBanks.get("COUNT_ID").equals("16")) {
//                    rr.setRequestId(resultSetBanks.getString("REQ_ID").substring(6)); //сокращаем до 10 знаков
//                } else {
//                    throw new FlowException("2.Не верно указан параметр COUNT_ID в config.txt");
//                }
                rr.setRequestId(resultSetBanks.getString("REQ_ID"));
                rr.setRequestDate(getDateDDMMYYYY(new Date(System.currentTimeMillis())));
                rr.setBailiff(getString(resultSetBanks, "FIO_SPI"));
//            r.setHeadBailiff(row.get("H_PRISTAV").toString());
                rr.setHeadBailiff(getString(resultSetBanks, "H_SPI"));
                rr.setExecProcNum(getString(resultSetBanks, "IP_NUM"));
                if (resultSetBanks.getBigDecimal("IP_SUM") == null){
//                    ID_SUBJECT_TYPE containing Аллименты
                    rr.setSumm(getBigDecimal(resultSetBanks, "IP_REST_DEBTSUM"));
                }else {
                    rr.setSumm(getBigDecimal(resultSetBanks, "IP_SUM"));
                }
                rr.setExecActNum(getString(resultSetBanks, "ID_NUMBER"));
                rr.setExecActDate(parseDate(getSqlDate(resultSetBanks, "ID_DATE")));

                rr.setDebtorLastName(getString(resultSetBanks, "ENTT_SURNAME"));
                rr.setDebtorFirstName(getString(resultSetBanks, "ENTT_FIRSTNAME"));
                rr.setDebtorSecondName(getString(resultSetBanks, "ENTT_PATRONYMIC"));

                rr.setDebtorName(getString(resultSetBanks, "DEBTOR_NAME"));
                rr.setDebtorInn(getString(resultSetBanks, "ENTT_INN"));
                rr.setDebtorKpp(getString(resultSetBanks, "ENTT_KPP"));
                rr.setDebtorBirthYear(getInteger(resultSetBanks, "DBTR_BORN_YEAR"));
                rr.setDebtorAddres(getString(resultSetBanks, "DEBTOR_ADDRESS"));
                System.out.println("**********************************************");
//            r.setDebtorBirthDate(parseDate(row.get("DEBTOR_BIRTHDATE").toString()));
                rr.setDebtorBirthDate(parseDate(getSqlDate(resultSetBanks, "DEBTOR_BIRTHDATE")));
                rr.setDebtorBornAddres(getNN(resultSetBanks.getObject("DEBTOR_BIRTHPLACE")));

                requestsBanks.add(rr);

                if (propertiesBanks.get("MVV_AGENT_CODE").equalsIgnoreCase("БАЙКАЛБАНК")) {
                    writeRequestBaykalBank(packetIdBanks, rr);
                }  else {
                    writeRequestBanks(packetIdBanks, rr);
                }
            }

        } catch (SQLException e) {
            System.out.println("БД не доступна: " + ospNameBanks);
//            в случае возникновения исключительной ситуации, удаляем сформированные файлы
            System.err.println(e.getMessage() + "ОШИБКА в fetchPackets: ");
            MyLoggerBanks.get().logMessage(processNameBanks, e.getMessage() + "ОШИБКА в fetchPackets: ");
            logger.error(processNameBanks + " " + e.getMessage() + "ОШИБКА в fetchPackets: ");

            JDBCConnectionBanks.getInstance().jdbcClose(processNameBanks);
            throw new FlowException(processNameBanks + " " + e.getMessage() + "ОШИБКА в fetchPackets: ");
        }
        String query_upBanks = "UPDATE EXT_REQUEST SET PROCESSED = 1 WHERE PACK_ID = " + packetIdBanks;

//        if (!requests.isEmpty()) {
        JDBCConnectionBanks.getInstance().jdbcUpdateBanks(dataSourceBanks, processNameBanks, query_upBanks);
//        } else {
//            JDBCConnection.getInstance().jdbcClose(processName);
//        }
    }


    private void fetchPacketsPasportBanks(String packetIdPasportBanks) throws FlowException {
//        Этот запрос для Запросов
        String queryBanks = "SELECT " +
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
                wherePasportBanks +
                " AND PACK_ID = " + packetIdPasportBanks + " " +
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
        List<RequestBanks> requestsBanks = new LinkedList<RequestBanks>();

        ResultSet resultSetBanks = JDBCConnectionBanks.getInstance().jdbcConnection(dataSourceBanks, processNameBanks, queryBanks);
        if (resultSetBanks == null) {
            System.out.println("Не удалось подключиться к БД, БД не доступна");
            MyLoggerBanks.get().logMessage(processNameBanks, "Не удалось подключиться к БД, БД не доступна");
            logger.error(processNameBanks + " - Не удалось подключиться к БД, БД не доступна");
            throw new FlowException(processNameBanks + " - Не удалось подключиться к БД, БД не доступна");

        }

        try {
            while (resultSetBanks.next()) {
                RequestBanks rr = new RequestBanks();
//                r.setId(getLong(resultSet, "ID"));
//                r.setBarcode(getString(resultSet, "BARCODE"));
//                r.setExecProcDate(parseDate(getSqlDate(resultSet, "ip_risedate")));
//                r.setPriority(getInteger(resultSet, "priority_penalties"));
//                r.setSumm(getBigDecimal(resultSet, "total_dept_sum"));

                if (propertiesBanks.get("COUNT_ID").equals("14")) {
//                System.out.println(row.get("REQ_ID").toString());
                    rr.setDepartment(resultSetBanks.getString("REQ_ID").substring(2, 4));
                } else if (propertiesBanks.get("COUNT_ID").equals("13")) {
//                System.out.println(row.get("REQ_ID").toString());
                    rr.setDepartment(resultSetBanks.getString("REQ_ID").substring(1, 3));
                } else if (propertiesBanks.get("COUNT_ID").equals("15")) {
//                System.out.println(row.get("REQ_ID").toString());
                    rr.setDepartment(resultSetBanks.getString("REQ_ID").substring(3, 5));
                } else if (propertiesBanks.get("COUNT_ID").equals("16")) {
//                System.out.println(row.get("REQ_ID").toString());
                    rr.setDepartment(resultSetBanks.getString("REQ_ID").substring(4, 6));
                } else {
                    throw new FlowException("1.Не верно указан параметр COUNT_ID в config.txt");
                }
                rr.setUserId("9998");
                rr.setRequestTime("12:01");
                rr.setRequestType("1");
                if (propertiesBanks.get("KOD_OSB") == null) {
                    throw new FlowException("Не указан код ОСБ в config.txt");
                }
                rr.setOsbList(propertiesBanks.get("KOD_OSB"));
//            r.setOsbList("0018");
//            r.setDepartment(dep_code);
//            r.setExecutoryProcessId(Long.parseLong(row.get("REQ_ID").toString())); //это не ИД производства
//            r.setRequestId(row.get("REQ_ID").toString().substring(4)); //сокращаем до 10 знаков
                if (propertiesBanks.get("COUNT_ID").equals("14")) {
                    rr.setRequestId(resultSetBanks.getString("REQ_ID").substring(4)); //сокращаем до 10 знаков
                } else if (propertiesBanks.get("COUNT_ID").equals("13")) {
                    rr.setRequestId(resultSetBanks.getString("REQ_ID").substring(3)); //сокращаем до 10 знаков
                } else if (propertiesBanks.get("COUNT_ID").equals("15")) {
                    rr.setRequestId(resultSetBanks.getString("REQ_ID").substring(5)); //сокращаем до 10 знаков
                } else if (propertiesBanks.get("COUNT_ID").equals("16")) {
                    rr.setRequestId(resultSetBanks.getString("REQ_ID").substring(6)); //сокращаем до 10 знаков
                } else {
                    throw new FlowException("2.Не верно указан параметр COUNT_ID в config.txt");
                }
                rr.setRequestDate(getDateDDMMYYYY(new Date(System.currentTimeMillis())));
                rr.setBailiff(getString(resultSetBanks, "FIO_SPI"));
//            r.setHeadBailiff(row.get("H_PRISTAV").toString());
                rr.setHeadBailiff(getString(resultSetBanks, "H_SPI"));
                rr.setExecProcNum(getString(resultSetBanks, "IP_NUM"));
                rr.setSumm(getBigDecimal(resultSetBanks, "IP_SUM"));
                rr.setExecActNum(getString(resultSetBanks, "ID_NUMBER"));
                rr.setExecActDate(parseDate(getSqlDate(resultSetBanks, "ID_DATE")));

                rr.setDebtorLastName(getString(resultSetBanks, "ENTT_SURNAME"));
                rr.setDebtorFirstName(getString(resultSetBanks, "ENTT_FIRSTNAME"));
                rr.setDebtorSecondName(getString(resultSetBanks, "ENTT_PATRONYMIC"));
                rr.setDebtorBirthYear(getInteger(resultSetBanks, "DBTR_BORN_YEAR"));
                rr.setDebtorAddres(getString(resultSetBanks, "DEBTOR_ADDRESS"));
//            r.setDebtorBirthDate(parseDate(row.get("DEBTOR_BIRTHDATE").toString()));
                rr.setDebtorBirthDate(parseDate(getSqlDate(resultSetBanks, "DEBTOR_BIRTHDATE")));
                rr.setDebtorBornAddres(getNN(resultSetBanks.getObject("DEBTOR_BIRTHPLACE")));
                String ss = getString(resultSetBanks, "ser_doc").trim().replaceAll(" ", "");
//                System.out.println(ss);
                rr.setPasportSer(ss.substring(0, 2) + " " + ss.substring(2, ss.length()));
//                System.out.println(ss.substring(0,2)+" "+ss.substring(2,ss.length()));
                rr.setPasportNum(getString(resultSetBanks, "num_doc").replaceAll(" ", "").trim());
                rr.setPasportIssued(getString(resultSetBanks, "issued_doc"));
                rr.setPasportType(getString(resultSetBanks, "type_doc_code"));
                rr.setPasportCod(getString(resultSetBanks, "code_dep"));
                rr.setPasportDate(parseDate(getSqlDate(resultSetBanks, "date_doc")));

                requestsBanks.add(rr);

                writeRequestPasportBanks(packetIdPasportBanks, rr);
            }

        } catch (SQLException e) {
            System.out.println("БД не доступна: " + ospNameBanks);
//            в случае возникновения исключительной ситуации, удаляем сформированные файлы
            System.err.println(e.getMessage() + "ОШИБКА в fetchPackets: ");
            MyLoggerBanks.get().logMessage(processNameBanks, e.getMessage() + "ОШИБКА в fetchPackets: ");
            logger.error(processNameBanks + " " + e.getMessage() + "ОШИБКА в fetchPackets: ");

            JDBCConnectionBanks.getInstance().jdbcClose(processNameBanks);
            throw new FlowException(processNameBanks + " " + e.getMessage() + "ОШИБКА в fetchPackets: ");
        }
        String query_upPasportBanks = "UPDATE EXT_REQUEST SET PROCESSED = 1 WHERE PACK_ID = " + packetIdPasportBanks;

//        if (!requests.isEmpty()) {
        JDBCConnectionBanks.getInstance().jdbcUpdateBanks(dataSourceBanks, processNameBanks, query_upPasportBanks);
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
            MyLoggerBanks.get().logMessage(processNameBanks, "Field: " + filedName + ", are not Integer type. Actual type: " + o.getClass());
            logger.error("Field: " + filedName + ", are not Integer type. Actual type: " + o.getClass());
            return o.toString();
        }
        if (o == null) return null;

        if (o instanceof Integer) {
            return o.toString();
        } else {
            System.err.println("Field: " + filedName + ", are not Integer type. Actual type: " + o.getClass());
            MyLoggerBanks.get().logMessage(processNameBanks, "Field: " + filedName + ", are not Integer type. Actual type: " + o.getClass());
            logger.error("Field: " + filedName + ", are not Integer type. Actual type: " + o.getClass());
            return o.toString();
        }
    }

    private String getString(ResultSet row, String filedName) {
        try {
            System.out.println("-----------------" + row.getObject(filedName));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        Object o = null;
        try {
            o = row.getObject(filedName);
        } catch (SQLException e) {
            System.err.println("Field: " + filedName + ", are not String type. Actual type: " + o.getClass());
            MyLoggerBanks.get().logMessage(processNameBanks, "Field: " + filedName + ", are not String type. Actual type: " + o.getClass());
            logger.error("Field: " + filedName + ", are not String type. Actual type: " + o.getClass());
            return o.toString();
        }
        if (o == null) return null;

        if (o instanceof String) {
            return (String) o;
        } else {
            System.err.println("Field: " + filedName + ", are not String type. Actual type: " + o.getClass());
            MyLoggerBanks.get().logMessage(processNameBanks, "Field: " + filedName + ", are not String type. Actual type: " + o.getClass());
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
            MyLoggerBanks.get().logMessage(processNameBanks, "Field: " + filedName + ", are not String type. Actual type: " + o.getClass());
            logger.error("Field: " + filedName + ", are not String type. Actual type: " + o.getClass());
            throw new FlowException("Can't convert Long");
        }
        if (o == null) return null;

        if (o instanceof Long) {
            return (Long) o;
        } else {
            System.err.println("Field: " + filedName + ", are not String type. Actual type: " + o.getClass());
            MyLoggerBanks.get().logMessage(processNameBanks, "Field: " + filedName + ", are not String type. Actual type: " + o.getClass());
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
            MyLoggerBanks.get().logMessage(processNameBanks, "Field: " + filedName + ", are not java.sql.Date type. Actual type: " + o.getClass());
            logger.error("Field: " + filedName + ", are not java.sql.Date type. Actual type: " + o.getClass());
            throw new FlowException("Can't convert data");
        }
        if (o == null) return null;

        if (o instanceof java.sql.Date) {
            return (java.sql.Date) o;
        } else {
            System.err.println("Field: " + filedName + ", are not java.sql.Date type. Actual type: " + o.getClass());
            MyLoggerBanks.get().logMessage(processNameBanks, "Field: " + filedName + ", are not java.sql.Date type. Actual type: " + o.getClass());
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
            MyLoggerBanks.get().logMessage(processNameBanks, "Field: " + filedName + ", are not bigDecimal type. Actual type: " + o.getClass());
            logger.error("Field: " + filedName + ", are not bigDecimal type. Actual type: " + o.getClass());
            return "0";
        }
        if (o == null) return "0";

        if (o instanceof BigDecimal) {
            return o.toString();
        } else {
            System.err.println("Field: " + filedName + ", are not bigDecimal type. Actual type: " + o.getClass());
            MyLoggerBanks.get().logMessage(processNameBanks, "Field: " + filedName + ", are not bigDecimal type. Actual type: " + o.getClass());
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
    private XmlWriterBanks openXmlWriterBanks(String packetId) {
        if (xmlWritersBanks == null) xmlWritersBanks = new Hashtable<String, XmlWriterBanks>();
        XmlWriterBanks xmlWriterBanks = xmlWritersBanks.get(packetId);
        String kod_osb = propertiesBanks.get("KOD_OSB");

        if (xmlWriterBanks == null) {
            String nextFileNameBanks = null;
            try {
                nextFileNameBanks = getNextFileNameBanks(xmlWritersBanks.size() + 1, depCodeBanks, kod_osb);
            } catch (FlowException e) {
                System.out.println("Ошибка в имени файла");
                MyLoggerBanks.get().logMessage(processNameBanks, "Ошибка в имени файла");
                logger.error(processNameBanks + "Ошибка в имени файла");
            }
            if (nextFileNameBanks == null) return null;

            try {
                xmlWriterBanks = new XmlWriterBanks(
                        propertiesBanks.get("OUTPUT_DIRECTORY"),
                        nextFileNameBanks);
            } catch (FlowException e) {
                MyLoggerBanks.get().logMessage(processNameBanks, "Ошибка при записи файла " + e);
                logger.error(processNameBanks + " Ошибка при записи файла " + e);
            } catch (TransformerConfigurationException e) {
                MyLoggerBanks.get().logMessage(processNameBanks, e.toString());
                logger.error(processNameBanks + e.toString());
            } catch (SAXException e) {
                MyLoggerBanks.get().logMessage(processNameBanks, e.toString());
                logger.error(processNameBanks + e);
            } catch (FileNotFoundException e) {
                MyLoggerBanks.get().logMessage(processNameBanks, e.toString());
                logger.error(processNameBanks + e);
            }
            xmlWritersBanks.put(packetId, xmlWriterBanks);
            System.out.println("Имя файла: " + nextFileNameBanks);
            MyLoggerBanks.get().logMessage(processNameBanks, "Имя файла: " + nextFileNameBanks);
            logger.info(processNameBanks + " - Имя файла: " + nextFileNameBanks);
        }

        System.out.println("Подготовка к обработке пакета: " + packetId);
        MyLoggerBanks.get().logMessage(processNameBanks, "Подготовка к обработке пакета: " + packetId);
        logger.info(processNameBanks + " - Подготовка к обработке пакета: " + packetId);

        return xmlWriterBanks;
    }

    private XmlWriterPasportBanks openXmlWriterPasportBanks(String packetIdPasportBanks) {
        if (xmlWritersPasportBanks == null) xmlWritersPasportBanks = new Hashtable<String, XmlWriterPasportBanks>();
        XmlWriterPasportBanks xmlWriterPasportBanks = xmlWritersPasportBanks.get(packetIdPasportBanks);
        String kod_osb = propertiesBanks.get("KOD_OSB");
        if (xmlWriterPasportBanks == null) {
            String nextFileNameBanks = null;
            try {
                nextFileNameBanks = getNextFileNameBanks(countBanks + xmlWritersPasportBanks.size() + 1, depCodeBanks, kod_osb);
            } catch (FlowException e) {
                System.out.println("Ошибка в имени файла");
                MyLoggerBanks.get().logMessage(processNameBanks, "Ошибка в имени файла");
                logger.error(processNameBanks + "Ошибка в имени файла");
            }
            if (nextFileNameBanks == null) return null;

            try {
                xmlWriterPasportBanks = new XmlWriterPasportBanks(
                        propertiesBanks.get("OUTPUT_DIRECTORY"),
                        nextFileNameBanks);
            } catch (FlowException e) {
                MyLoggerBanks.get().logMessage(processNameBanks, "Ошибка при записи файла " + e);
                logger.error(processNameBanks + " Ошибка при записи файла " + e);
            } catch (TransformerConfigurationException e) {
                MyLoggerBanks.get().logMessage(processNameBanks, e.toString());
                logger.error(processNameBanks + e.toString());
            } catch (SAXException e) {
                MyLoggerBanks.get().logMessage(processNameBanks, e.toString());
                logger.error(processNameBanks + e);
            } catch (FileNotFoundException e) {
                MyLoggerBanks.get().logMessage(processNameBanks, e.toString());
                logger.error(processNameBanks + e);
            }
            xmlWritersPasportBanks.put(packetIdPasportBanks, xmlWriterPasportBanks);
            System.out.println("Имя файла: " + nextFileNameBanks);
            MyLoggerBanks.get().logMessage(processNameBanks, "Имя файла: " + nextFileNameBanks);
            logger.info(processNameBanks + " - Имя файла: " + nextFileNameBanks);
        }

        System.out.println("Подготовка к обработке пакета: " + packetIdPasportBanks);
        MyLoggerBanks.get().logMessage(processNameBanks, "Подготовка к обработке пакета: " + packetIdPasportBanks);
        logger.info(processNameBanks + " - Подготовка к обработке пакета: " + packetIdPasportBanks);

        return xmlWriterPasportBanks;
    }


    private void writeRequestBanks(String packetIdBanks, RequestBanks r) {
        try {
            getRequestWriterBanks(packetIdBanks).writeRequestBanks(r);
        } catch (FlowException e) {
            System.err.println("ОШИБКА: " + e.getMessage());
            MyLoggerBanks.get().logMessage(processNameBanks, "ОШИБКА: " + e.getMessage());
            logger.error(processNameBanks + " - ОШИБКА: " + e.getMessage());
        }
    }

    private void writeRequestBaykalBank(String packetIdBanks, RequestBanks r) {
        try {
            getRequestWriterBanks(packetIdBanks).writeRequestBaykalBank(r);
        } catch (FlowException e) {
            System.err.println("ОШИБКА: " + e.getMessage());
            MyLoggerBanks.get().logMessage(processNameBanks, "ОШИБКА: " + e.getMessage());
            logger.error(processNameBanks + " - ОШИБКА: " + e.getMessage());
        }
    }

    private void writeRequestPasportBanks(String packetIdPasportBanks, RequestBanks r) {
        try {
            getRequestWriterPasportBanks(packetIdPasportBanks).writeRequestPasportBanks(r);
        } catch (FlowException e) {
            System.err.println("ОШИБКА: " + e.getMessage());
            MyLoggerBanks.get().logMessage(processNameBanks, "ОШИБКА: " + e.getMessage());
            logger.error(processNameBanks + " - ОШИБКА: " + e.getMessage());
        }
    }


    private XmlWriterBanks getRequestWriterBanks(String packetIdBanks) {
        return xmlWritersBanks.get(packetIdBanks);
    }

    private XmlWriterPasportBanks getRequestWriterPasportBanks(String packetIdPasportBanks) {
        return xmlWritersPasportBanks.get(packetIdPasportBanks);
    }


    /**
     * @param fileCountBanks //порядковый номер файла запроса за текущий день (кажется от 1 до Z)
     * @param depCodeBanks   //код отдела
     * @return имя файла запроса
     */
    private String getNextFileNameBanks(int fileCountBanks, String depCodeBanks, String kod_osb) throws FlowException {
        if (propertiesBanks.get("MVV_AGENT_CODE").equalsIgnoreCase("БАЙКАЛБАНК")) {
            if (fileCountBanks >= 99) return null; //достигнут лимит файлов для отправки
            String kod_osp = propertiesBanks.get("KOD_OSP");
            Calendar inst = Calendar.getInstance();
            String day = new DecimalFormat("00").format(inst.get(Calendar.DAY_OF_MONTH));
            String month = new DecimalFormat("00").format(inst.get(Calendar.MONTH)+1);
            String year = new DecimalFormat("0000").format(inst.get(Calendar.YEAR));
            System.out.println("порядковый номер файла = "+fileCountBanks);
            return "Z" + kod_osp+ "_" + depCodeBanks + "_" + day + month + year + "_" + new DecimalFormat("00").format(fileCountBanks) +".xml";
//        Z20_10_030314_27.xml
        }else {
            if (fileCountBanks >= 35) return null; //достигнут лимит файлов для отправки

            Calendar inst = Calendar.getInstance();
//        String day = new DecimalFormat("00").format(inst.get(Calendar.DAY_OF_MONTH));
            String day = ddBanks;
            String month = Integer.toHexString(inst.get(Calendar.MONTH) + 1);
            System.out.println("порядковый номер файла = " + fileCountBanks);
            MyLoggerBanks.get().logMessage(processNameBanks, "порядковый номер файла = " + fileCountBanks);
            logger.info("порядковый номер файла = " + fileCountBanks);
//        return "r" + day + month + "0018." + Integer.toHexString(fileCount) + depCode;
//        return "r" + day + month + kod_osb +"." + Integer.toHexString(fileCount) + depCode;
            if (fileCountBanks <= 9) return "r" + day + month + kod_osb + "." + fileCountBanks + depCodeBanks;
            return "r" + day + month + kod_osb + "." + Character.toString((char) (fileCountBanks - 10 + 'a')) + depCodeBanks;
//        if (fileCount <= 9) return "r" + "204" + kod_osb + "." + fileCount + depCode;
//        return "r" + "204" + kod_osb + "." + Character.toString((char) (fileCount - 10 + 'a')) + depCode;
        }
    }

    public void deleteCreatedFiles() {
        for (XmlWriterBanks writer : xmlWritersBanks.values()) {
            new File(writer.outputDirectoryBanks + writer.filenameBanks).delete();
        }
//        for (SberbankXmlWriterPasport writerPasport : xmlWritersPasport.values()) {
//            new File(writerPasport.outputDirectory + writerPasport.filename).delete();
//        }
    }
}

