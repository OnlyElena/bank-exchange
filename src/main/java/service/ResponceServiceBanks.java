package service;

import beans.ResponseBanks;
import exceptions.FlowException;
import jdbc.BeanConnectBanks;
import jdbc.TestConnect;
import org.apache.log4j.Logger;
import org.firebirdsql.gds.impl.GDSFactory;
import org.firebirdsql.gds.impl.GDSType;
import org.firebirdsql.jca.FBSADataSource;
import org.firebirdsql.jdbc.FBDriverPropertyManager;

import javax.resource.ResourceException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author: OnlyElena@mail.ru
 * Date: 6/19/16
 * Time: 1:43 AM
 */
public class ResponceServiceBanks {
    final static Logger logger = Logger.getLogger(ResponceServiceBanks.class);

    List<BeanConnectBanks> dataSourceBanks;
    LinkedHashMap<String, String> propertiesBanks;
    String processNameBanks;
//    String mvv_agent_codeBanks;
//    String mvv_agent_dept_codeBanks;
//    String mvv_agreement_codeBanks;

    public static final SimpleDateFormat ddmmyyyy = new SimpleDateFormat("dd.MM.yyyy");


    public ResponceServiceBanks(List<BeanConnectBanks> dataSourceBanks, LinkedHashMap<String, String> propertiesBanks, String processNameBanks) {
        this.dataSourceBanks = dataSourceBanks;
        this.propertiesBanks = propertiesBanks;
        this.processNameBanks = processNameBanks;
    }

    public boolean processResponseBanks(Hashtable<String, List<ResponseBanks>> Banksresponses) throws FlowException {
        String agent_codeBanks = propertiesBanks.get("MVV_AGENT_CODE");
        String agent_dept_codeBanks = propertiesBanks.get("MVV_AGENT_DEPT_CODE");
        String agreement_codeBanks = propertiesBanks.get("MVV_AGREEMENT_CODE");
//        String pasport_agent_codeBanks = propertiesBanks.get("PASPORT_MVV_AGENT_CODE");
//        String pasport_agent_dept_codeBanks = propertiesBanks.get("PASPORT_MVV_AGENT_DEPT_CODE");
//        String pasport_agreement_codeBanks = propertiesBanks.get("PASPORT_MVV_AGREEMENT_CODE");
//        String territoryBanks = propertiesBanks.get("TERRITORY");
//        String depCodeBanks = dataSourceBanks.get(0).getBeanId();
        final String databaseURLBanks = dataSourceBanks.get(0).getUrl().substring(17, dataSourceBanks.get(0).getUrl().indexOf("?"));
        final String jdbcUrlBanks = dataSourceBanks.get(0).getUrl();
        final GDSType typeBanks = GDSFactory.getTypeForProtocol(jdbcUrlBanks);
        final Properties propsBanks = new Properties();
        propsBanks.setProperty("user", dataSourceBanks.get(0).getUsername());
        propsBanks.setProperty("password", dataSourceBanks.get(0).getPassword());

//        long start = System.currentTimeMillis();

        try {
            final Map<String, String> normalizedInfo = FBDriverPropertyManager.normalize(jdbcUrlBanks, propsBanks);
            FBSADataSource fbDataSourceBanks = new FBSADataSource(typeBanks);
            fbDataSourceBanks.setDatabase(databaseURLBanks);
            for (Map.Entry<String, String> entry : normalizedInfo.entrySet())
                fbDataSourceBanks.setNonStandardProperty(entry.getKey(), entry.getValue());

            fbDataSourceBanks.setNonStandardProperty("isc_dpb_process_name", processNameBanks);
            final Connection con = fbDataSourceBanks.getConnection();
//            con.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

            String smbConnect = dataSourceBanks.get(0).getUrl().substring(17, dataSourceBanks.get(0).getUrl().indexOf("/"));

            boolean testConnect = new TestConnect().TestConnect("http://" + smbConnect + ":8080/pksp-server/");
            if (testConnect == false) {
                MyLoggerBanks.get().logMessage(processNameBanks, "Не удалось подключиться по тонкому клиенту, БД" + smbConnect + " на ремонте");
                logger.error(processNameBanks + "Не удалось подключиться по тонкому клиенту, БД" + smbConnect + " на ремонте");
                throw new FlowException("Не удалось подключиться по тонкому клиенту, БД" + smbConnect + " на ремонте");
            }

            final PreparedStatement genidPSt = con.prepareStatement("SELECT NEXT VALUE FOR SEQ_DOCUMENT FROM RDB$DATABASE");

            String queryMap =
//                    "SELECT REQ_ID,\n" +
                    "SELECT DISTINCT REQ_ID,\n" +
//                      "SELECT FIRST(1) REQ_ID,\n" +
                            "REQ_DATE,\n" +
                            "FIO_SPI,\n" +
                            "IP_NUM,\n" +
                            "IP_SUM,\n" +
                            "ID_NUMBER,\n" +
                            "ID_DATE,\n" +
                            "DEBTOR_NAME,\n" +
                            "--DBTR_BORN_YEAR,\n" +
                            "extract(year from DEBTOR_BIRTHDATE) as DBTR_BORN_YEAR,\n" +
                            "DEBTOR_ADDRESS,\n" +
                            "DEBTOR_BIRTHDATE,\n" +
                            "DEBTOR_BIRTHPLACE,\n" +
                            "PACK_NUMBER,\n" +
                            "DEBTOR_INN,\n" +
                            "REQ_NUMBER\n" +
                            "from ext_request \n" +
                            "where req_id = ?\n" +
                            "AND MVV_AGREEMENT_CODE = ?\n" +
                            " AND MVV_AGENT_CODE = ?\n" +
                            " AND MVV_AGENT_DEPT_CODE = ?\n" +
                            " AND (REQ_NUMBER not containing '/' )";

//        System.out.println("query: " + queryMap);

            final PreparedStatement MapPSt = con.prepareStatement(queryMap);
//            Установили количество выдаваемых результатов = 1 строке
//            MapPSt.setMaxRows(1);

            String sql1 = "INSERT INTO " +
                    "EXT_INPUT_HEADER " +
                    "(ID," +
                    "PACK_NUMBER," +
                    "PROCEED," +
                    "AGENT_CODE," +
                    "AGENT_DEPT_CODE," +
                    "AGENT_AGREEMENT_CODE," +
                    "EXTERNAL_KEY," +
                    "METAOBJECTNAME," +
                    "DATE_IMPORT," +
                    "SOURCE_BARCODE" +
                    ") VALUES (" +
//                        "" + genid + "," +
                    "?, " +
//                        "" + map.get("PACK_NUMBER") + "," +
                    "?, " +
                    "0," +
//                        "'" + mvv_agent_code + "'," +
                    "?, " +
//                        "'" + mvv_agent_dept_code + "'," +
                    "?, " +
//                        "'" + mvv_agreement_code + "'," +
                    "?, " +
//                        "'" + genuuid + "'," +
                    "?, " +
                    "'EXT_RESPONSE'," +
                    "CAST('NOW' AS DATE)," +
                    "''" +
                    ")";

            final PreparedStatement insert1 = con.prepareStatement(sql1);

            String sql2 = "INSERT INTO " +
                    "EXT_RESPONSE (" +
                    "ID," +
                    "RESPONSE_DATE," +
                    "ENTITY_NAME," +
                    "ENTITY_BIRTHYEAR," +
                    "ENTITY_BIRTHDATE," +
                    "ENTITY_INN," +
                    "ID_NUM," +
                    "IP_NUM," +
                    "REQUEST_NUM," +
                    "REQUEST_ID," +
                    "DATA_STR," +
                    "ANSWER_TYPE" +
                    ") VALUES (" +
//                        "" + genid + "," +
                    "?, " +
//                        "'" + responses.get(0).getRequestDate() + "'," +
                    "?, " +
//                        "'" + map.get("DEBTOR_NAME") + "'," +
                    "?, " +
//                        "'" + map.get("DBTR_BORN_YEAR") + "'," +
                    "?, " +
//                        "'" + map.get("DEBTOR_BIRTHDATE") + "'," +
                    "?, " +
//                        "'" + map.get("DEBTOR_INN") + "'," +
                    "?, " +
//                        "'" + map.get("ID_NUMBER") + "'," +
                    "?, " +
//                        "'" + map.get("IP_NUM") + "'," +
                    "?, " +
//                        "'" + map.get("REQ_NUMBER") + "'," +
                    "?, " +
//                        "" + id + "," +
                    "?, " +
//                        "'" + result_str + "'," +
                    "?, " +
//                        "'" + ANSWER_TYPE + "'" +
                    "? " +
                    ")";

            final PreparedStatement insert2 = con.prepareStatement(sql2);

            String sql3 = "INSERT INTO " +
                    "EXT_INFORMATION (" +
                    "ID," +
                    "ACT_DATE," +
                    "KIND_DATA_TYPE," +
                    "ENTITY_NAME," +
                    "EXTERNAL_KEY," +
                    "ENTITY_BIRTHDATE," +
                    "ENTITY_BIRTHYEAR," +
                    "PROCEED," +
                    "DOCUMENT_KEY," +
                    "ENTITY_INN" +
                    ") VALUES (" +
//                        "" + extInfoId + "," +
                    "?, " +
//                        "'" + response.getRequestDate() + "'," +
                    "?, " +
                    "'09'," +
//                        "'" + map.get("DEBTOR_NAME") + "'," +
                    "?, " +
//                        "'" + genuuid + "'," +
                    "?, " +
//                        "'" + map.get("DEBTOR_BIRTHDATE") + "'," +
                    "?, " +
//                        "'" + map.get("DBTR_BORN_YEAR") + "'," +
                    "?, " +
                    "0," +
//                        "'" + genuuid + "'," +
                    "?, " +
//                        "'" + map.get("DEBTOR_INN") + "'" +
                    "? " +
                    ")";
            final PreparedStatement insert3 = con.prepareStatement(sql3);

            String sql4 = "INSERT INTO " +
                    "EXT_AVAILABILITY_ACC_DATA (" +
                    "ID," +
                    "BIC_BANK," +
                    "CURRENCY_CODE, " +
                    "CURRENCY_CODE_CHR, " +
                    "ACC," +
                    "BANK_NAME," +
                    "SUMMA," +
                    "DEPT_CODE," +
                    "SUMMA_INFO" +
                    ") VALUES (" +
//                        "" + extInfoId + "," +
                    "?, " +
//                        "'" + response.getOsbBIC() + "'," +
                    "?, " +
//                        "'" + currCode + "'," +
                    "?, " +
//                        "'" + currCode_CH + "'," +
                    "?, " +
//                        "'" + response.getDebtorAccount().replaceAll("\\.", "") + "'," +
                    "?, " +
//                        "'" + response.getOsbName() + "'," +
                    "?, " +
//                        "" + response.getAccountBalance() + "," +
                    "?, " +
//                        "'" + response.getOsbNumber() + "'," +
                    "?, " +
//                        "'Остаток на счету " + response.getAccountBalance() + "'" + //длинна должна быть не более 99 символов
                    "? " +
                    ")";

            final PreparedStatement insert4 = con.prepareStatement(sql4);

            final PreparedStatement infoIdPSt = con.prepareStatement("SELECT NEXT VALUE FOR EXT_INFORMATION FROM RDB$DATABASE");
//            System.out.println("PST " + (System.currentTimeMillis() - start) / 1000 /*/ 60*/ + " сек.");

//            final Statement st = con.createStatement();
//            final ResultSet rs = st.executeQuery("select * from mon$attachments");
//            while (rs.next()) {
//                System.out.println(rs.getString("mon$remote_process"));
//            }
            try {
                for (List<ResponseBanks> responsesBanks : Banksresponses.values()) {
                    if (responsesBanks.get(0).getRequestId().equals("null")) {
                        System.out.println("В файле ID ответа = null, если данных больше нет, то файл удалить вручную!");
                        MyLoggerBanks.get().logMessage(processNameBanks, "В файле ID ответа = null, если данных больше нет, то файл удалить вручную!");
                        logger.error("Notiff - В файле ID ответа = null, если данных больше нет, то файл удалить вручную!");
                        continue;
                    }

                    Long genid = null;
                    Map<String, Object> map = null;

                    ResultSet genidRSt = genidPSt.executeQuery();
                    while (genidRSt.next()) genid = genidRSt.getLong(1);
                    if (genidRSt != null) genidRSt.close();
//                System.out.println(genid);

                    String genuuid = UUID.randomUUID().toString();

                    String id = responsesBanks.get(0).getRequestId();
                    System.out.println(id);
//                    System.out.println("id " + (System.currentTimeMillis() - start) / 1000 /*/ 60*/ + " сек.");
//                    mvv_agent_codeBanks = agent_codeBanks;
//                    mvv_agreement_codeBanks = agreement_codeBanks;
//                    mvv_agent_dept_codeBanks = agent_dept_codeBanks;
                    MapPSt.setString(1, id);
                    MapPSt.setString(2, agreement_codeBanks);
                    MapPSt.setString(3, agent_codeBanks);
                    MapPSt.setString(4, agent_dept_codeBanks);
                    ResultSet MapRSt = MapPSt.executeQuery();

//                    if (!MapRSt.next()) {
//                        mvv_agent_codeBanks = pasport_agent_codeBanks;
//                        mvv_agent_dept_codeBanks = pasport_agent_dept_codeBanks;
//                        mvv_agreement_codeBanks = pasport_agreement_codeBanks;
//
//                        MapPSt.setString(1, id);
//                        MapPSt.setString(2, mvv_agreement_codeBanks);
//                        MapPSt.setString(3, mvv_agent_codeBanks);
//                        MapPSt.setString(4, mvv_agent_dept_codeBanks);
//                        MapRSt = MapPSt.executeQuery();
//                    } else {
                    MapRSt = MapPSt.executeQuery();
//                    }

                    map = new HashMap<String, Object>();

                    if (MapRSt == null) {
                        System.out.println("не найден запрос для ответа " + id);
                        MyLoggerBanks.get().logMessage(processNameBanks, "не найден запрос для ответа " + id);
                        continue;
                    }
                    while (MapRSt.next()) {
                        map.put("REQ_ID", MapRSt.getObject("REQ_ID"));
                        map.put("REQ_DATE", MapRSt.getObject("REQ_DATE"));
                        map.put("FIO_SPI", MapRSt.getObject("FIO_SPI"));
                        map.put("IP_NUM", MapRSt.getObject("IP_NUM"));
                        map.put("IP_SUM", MapRSt.getObject("IP_SUM"));
                        map.put("ID_NUMBER", MapRSt.getObject("ID_NUMBER"));
                        map.put("ID_DATE", MapRSt.getObject("ID_DATE"));
                        map.put("DEBTOR_NAME", MapRSt.getObject("DEBTOR_NAME"));
                        map.put("DBTR_BORN_YEAR", MapRSt.getObject("DBTR_BORN_YEAR"));

//                            if (map.get("DBTR_BORN_YEAR") == null) {
//                                map.put("DBTR_BORN_YEAR", "0000");
//                            }
                        map.put("DEBTOR_ADDRESS", MapRSt.getObject("DEBTOR_ADDRESS"));
                        map.put("DEBTOR_BIRTHDATE", MapRSt.getObject("DEBTOR_BIRTHDATE"));
                        map.put("DEBTOR_BIRTHPLACE", MapRSt.getObject("DEBTOR_BIRTHPLACE"));
                        map.put("PACK_NUMBER", MapRSt.getObject("PACK_NUMBER"));
                        map.put("DEBTOR_INN", MapRSt.getObject("DEBTOR_INN"));
                        map.put("REQ_NUMBER", MapRSt.getObject("REQ_NUMBER"));
//                            map.put("ip_id", MapRSt.getLong("ip_id"));

                    }
                    if (MapRSt != null) MapRSt.close();
//                    System.out.println(map);
                    if (map.isEmpty()) {
                        System.out.println("не найден запрос для ответа " + id);
                        MyLoggerBanks.get().logMessage(processNameBanks, "не найден запрос для ответа " + id);
                        continue;
                    }

                    insert1.setLong(1, genid);
                    insert1.setObject(2, map.get("PACK_NUMBER"));
                    insert1.setString(3, agent_codeBanks);
                    insert1.setString(4, agent_dept_codeBanks);
                    insert1.setString(5, agreement_codeBanks);
                    insert1.setString(6, genuuid);
                    insert1.executeUpdate();

                    String result_str;
                    String ANSWER_TYPE;
                    int result = responsesBanks.get(0).getResult();
                    if (result == 0) {
                        result_str = "Счетов не найдено";
                        ANSWER_TYPE = "2";
//                    } else if (result == 3) {
//                        result_str = "По должнику требуется дополнительная информация (найдены несколько совпадении ФИО и даты рождения)";
//                        ANSWER_TYPE = "3";
                    } else {
                        result_str = "Имеются счета, информация прилагается";
                        if (responsesBanks.get(0).getAccountDescr() != null) {
                            result_str = result_str + "\n\nВид вклада: " + responsesBanks.get(0).getAccountDescr() +
                                    "\nВалюта вклада: " + responsesBanks.get(0).getAccountCurreny();
                        }
                        ANSWER_TYPE = "1";
                    }

                    if (agent_codeBanks.equalsIgnoreCase("БАЙКАЛБАНК") && result == 2) {
                        result_str = "Счетов не найдено\n" + responsesBanks.get(0).getErrors();
                        ANSWER_TYPE = "2";
                    }

                    insert2.setLong(1, genid);
                    insert2.setDate(2, getDateDDMMYYYY(responsesBanks.get(0).getRequestDate()));
                    insert2.setObject(3, map.get("DEBTOR_NAME"));
                    insert2.setObject(4, map.get("DBTR_BORN_YEAR"));
                    insert2.setObject(5, map.get("DEBTOR_BIRTHDATE"));
                    insert2.setObject(6, map.get("DEBTOR_INN"));
                    insert2.setObject(7, map.get("ID_NUMBER"));
                    insert2.setObject(8, map.get("IP_NUM"));
                    insert2.setObject(9, map.get("REQ_NUMBER"));
                    insert2.setString(10, id);
                    insert2.setString(11, result_str);
                    insert2.setString(12, ANSWER_TYPE);
                    insert2.executeUpdate();

                    for (ResponseBanks responseBanks : responsesBanks) {

                        Long extInfoId = null;

                        ResultSet infoIdRSt = infoIdPSt.executeQuery();
                        while (infoIdRSt.next()) extInfoId = infoIdRSt.getLong(1);
                        if (infoIdRSt != null) infoIdRSt.close();

//                        String currCode = "???";
                        String currCode = null;
                        String currCodeCH = null;

                        if (responseBanks.getAccountCurreny() != null) {
                            if (responseBanks.getAccountCurreny().equals("Российский рубль") || responseBanks.getAccountCurreny().equalsIgnoreCase("руб") || responseBanks.getAccountCurreny().equalsIgnoreCase("810")) {
                                currCode = "810";
//                                currCodeCH = "РУБ";
                                currCodeCH = "RUR";
                            } else {
                                if (responseBanks.getAccountCurreny().length() > 3) {
                                    currCode = responseBanks.getAccountCurreny().substring(0, 3);
                                } else {
                                    currCode = responseBanks.getAccountCurreny();
                                }
//                    System.out.println(currCode + " " + response.getAccountCurreny());
                            }

                            if (currCode.equals("643")) currCodeCH = "RUB";
                            else if (currCode.equals("978")) currCodeCH = "EUR";
                            else if (currCode.equals("840")) currCodeCH = "USD";

                        }

                        if (responseBanks.getResult() == 0) {
                            //счетов не найдено
//                System.out.println("счетов не найдено:  "+id);
                            System.out.print("*");
                        } else if (agent_codeBanks.equalsIgnoreCase("БАЙКАЛБАНК") && result == 2) {
                            System.out.print("*");
//                        } else if (response.getResult() == 3) {
//                            // result_str = "По должнику требуется дополнительная информация (найдены несколько совпадении ФИО и даты рождения)";
////                System.out.println("По должнику требуется дополнительная информация:  " + id);
//                            System.out.print("*");
                        } else if (responseBanks.getDebtorAccount() == null) {
                            System.out.println("нет номера счета, вероятно банковская ячейка");
                        } else {
//                System.out.println("имеется информация:  "+extInfoId);
                            System.out.print("*");

                            insert3.setLong(1, extInfoId);
                            insert3.setDate(2, getDateDDMMYYYY(responseBanks.getRequestDate()));
                            insert3.setObject(3, map.get("DEBTOR_NAME"));
                            insert3.setString(4, genuuid);
                            insert3.setDate(5, getDateDDMMYYYYinString(map.get("DEBTOR_BIRTHDATE")));
                            insert3.setObject(6, map.get("DBTR_BORN_YEAR"));
                            insert3.setString(7, genuuid);
                            insert3.setObject(8, map.get("DEBTOR_INN"));
                            insert3.executeUpdate();

                            insert4.setLong(1, extInfoId);
                            insert4.setString(2, responseBanks.getOsbBIC());
                            insert4.setString(3, currCode);
                            insert4.setString(4, currCodeCH);
                            insert4.setString(5, responseBanks.getDebtorAccount().replaceAll("\\.", ""));
                            insert4.setString(6, responseBanks.getOsbName());
                            insert4.setObject(7, getBigDecimal(responseBanks.getAccountBalance()));
//                            insert4.setObject(6, new BigDecimal(response.getAccountBalance().replace("\n", "")));
                            insert4.setString(8, responseBanks.getOsbNumber());
                            insert4.setString(9, "Остаток на счету " + responseBanks.getAccountBalance());                            insert4.executeUpdate();
                        }
                    }
//                    System.out.println("end " + (System.currentTimeMillis() - start) / 1000 /*/ 60*/ + " сек.");
//                    long end = System.currentTimeMillis();
//                    start = end;
                }
//            try {
//                restriction_int_keyPSt.close();
//                if (insert2 != null) insert2.close();
//                if (insert1 != null) insert1.close();
//                if (bar_codePSt != null) bar_codePSt.close();
//                if (ip_idPSt != null) ip_idPSt.close();
//                if (new_idPSt != null) new_idPSt.close();
//                if (con != null) con.close();
//                if (fbDataSource != null) fbDataSource.close();
//            } catch (ResourceException e) {
//                MyLogger.get().logMessage("Notif", "не закрылся fbDataSource " + e);
//                System.out.println("не закрылся fbDataSource " + e);
//                logger.error("не закрылся fbDataSource " + e);
            } finally {
                try {
                    if (infoIdPSt != null) infoIdPSt.close();
                    if (insert4 != null) insert4.close();
                    if (insert3 != null) insert3.close();
                    if (insert2 != null) insert2.close();
                    if (insert1 != null) insert1.close();
                    if (MapPSt != null) MapPSt.close();
                    if (genidPSt != null) genidPSt.close();
                    if (con != null) con.close();
                    if (fbDataSourceBanks != null) fbDataSourceBanks.close();
                } catch (ResourceException e) {
                    MyLoggerBanks.get().logMessage(processNameBanks, "не закрылся fbDataSource " + e);
                    System.out.println("не закрылся fbDataSource " + e);
                    logger.error("не закрылся fbDataSource " + e);
                } catch (SQLException e) {
                    MyLoggerBanks.get().logMessage(processNameBanks, "Ошибка подключения к БД " + e.getMessage());
                    logger.error("Notif - Ошибка подключения к БД " + e.getMessage());
                    return false;
//                    throw new FlowException("Ошибка подключения к БД " + e.getMessage());
//                e.printStackTrace();
                }
            }
        } catch (SQLException e) {
            System.out.println("\nОшибка подключения к БД " + jdbcUrlBanks + "\n" + e.getMessage());
            MyLoggerBanks.get().logMessage(processNameBanks, "Ошибка подключения к БД " + jdbcUrlBanks + "\n" + e.getMessage());
            logger.error("Notif - Ошибка подключения к БД " + jdbcUrlBanks + "\n" + e.getMessage());
//            e.printStackTrace();
            return false;
        }

        return true;
    }

    private Double getBigDecimal(String row) {
        if (row == null) return null;
        if (row.isEmpty()) return null;
//        System.out.println(row);
//        row.replaceAll(".",",");
        Double dd = Double.parseDouble(row.replace("\n", ""));
//        BigDecimal bigDecimal = new BigDecimal(dd);
//        System.out.println(bigDecimal);
        return dd;
    }

    private java.sql.Date getDateDDMMYYYYinString(Object date) {
        if (date == null) return null;
        SimpleDateFormat format = new SimpleDateFormat();
        format.applyPattern("yyyy-MM-dd");
        Date date1 = null;
        java.sql.Date sqlDate = null;
        try {
            date1 = format.parse(date.toString());
            sqlDate = new java.sql.Date(date1.getTime());
        } catch (ParseException e) {
            System.out.println("err format date " + e);
            MyLoggerBanks.get().logMessage(processNameBanks, "err format date " + e);
        }
        return sqlDate;
    }

//    public java.sql.Date getDateDDMMYYYY(String date) {
//        if (date == null) return null;
//        SimpleDateFormat format = new SimpleDateFormat();
//        format.applyPattern("yyyy.MM.dd");
//        Date date1 = null;
//        java.sql.Date sqlDate = null;
//        try {
//            date1 = format.parse(date);
//            sqlDate = new java.sql.Date(date1.getTime());
//        } catch (ParseException e) {
//            System.out.println("err format date " + e);
//            MyLoggerBanks.get().logMessage(processNameBanks, "err format date " + e);
//        }
//        return sqlDate;
//    }


    public java.sql.Date getDateDDMMYYYY(String date) {
//        System.out.println("date "+date);
        if (date == null) return null;
        SimpleDateFormat format = new SimpleDateFormat();
        format.applyPattern("dd.MM.yyyy");
        Date date1 = null;
        java.sql.Date sqlDate = null;
        try {
            date1 = format.parse(date);
            sqlDate = new java.sql.Date(date1.getTime());
        } catch (ParseException e) {
            System.out.println("err format date " + e);
            MyLoggerBanks.get().logMessage(processNameBanks, "err format date " + e);
        }
//        System.out.println("date --"+sqlDate);

        return sqlDate;
    }


}


//
//        String query =
//        "SELECT DISTINCT " +
//        " REQ_ID," +
//        " REQ_DATE," +
//        " FIO_SPI," +
////                        " SUBSTRING(H_SPI FROM 1 FOR POSITION(',' IN H_SPI)-1) AS H_PRISTAV," +
//        " IP_NUM," +
//        " IP_SUM," +
//        " ID_NUMBER," +
//        " EXT_REQUEST.ID_DATE," +
//        " DEBTOR_NAME," +
//        " DBTR_BORN_YEAR," +
//        " DEBTOR_ADDRESS," +
//        " DEBTOR_BIRTHDATE," +
//        " DEBTOR_BIRTHPLACE," +
//        " PACK_NUMBER," +
//        " DEBTOR_INN," +
//        " REQ_NUMBER" +
//        " FROM " +
//        " DOCUMENT INNER JOIN EXT_REQUEST ON DOCUMENT.ID = EXT_REQUEST.IP_ID " +
//        " INNER JOIN DOC_IP ON DOCUMENT.ID = DOC_IP.ID " +
//        " INNER JOIN DOC_IP_DOC ON DOCUMENT.ID = DOC_IP_DOC.ID " +
//        " INNER JOIN NSI_COUNTERPARTY_CLASS ON DOC_IP_DOC.ID_DBTR_ENTID = NSI_COUNTERPARTY_CLASS.NCC_ID " +
//        " WHERE REQ_ID = ? " +
//        " AND MVV_AGREEMENT_CODE = '" + agreement_code + "'" +
//        " AND MVV_AGENT_CODE = '" + agent_code + "'" +
//        " AND MVV_AGENT_DEPT_CODE = '" + agent_dept_code + "'" +
//        " AND (REQ_NUMBER not containing '/' )" +
//        " GROUP BY REQ_ID, REQ_DATE, FIO_SPI," +
////                        " SUBSTRING(H_SPI FROM 1 FOR POSITION (',' IN H_SPI)-1), " +
//        " IP_NUM, IP_SUM, ID_NUMBER, document.id," +
//        " EXT_REQUEST.ID_DATE, DEBTOR_NAME, DBTR_BORN_YEAR, DEBTOR_ADDRESS," +
//        " DEBTOR_BIRTHDATE, DEBTOR_BIRTHPLACE, PACK_NUMBER, DEBTOR_INN, REQ_NUMBER";
//
////        System.out.println("query: " + query);