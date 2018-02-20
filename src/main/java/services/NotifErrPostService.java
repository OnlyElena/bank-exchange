package services;

import beans.OSP;
import exceptions.FlowException;
import jdbc.BeanConnect;
import jdbc.JDBCConnection;
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
import java.util.*;

/**
 * @author: OnlyElena@mail.ru
 * Date: 5/13/16
 * Time: 2:19 PM
 */
public class NotifErrPostService {
    List<BeanConnect> dataSource;
    LinkedHashMap<String, String> properties;
    String ddmmyy;
    String depCode;
    String processName;
    String where;
    Hashtable<String, SberbankPostWriter> postWriters;
    OSP osp;

    final static Logger logger = Logger.getLogger(PostService.class);

    public NotifErrPostService(List<BeanConnect> dataSource, LinkedHashMap<String, String> properties, String ddmmyy, String processName) throws FlowException {
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
        osp = new OSP();
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
                depCode = resultSet.getObject("TERRITORY").toString() + resultSet.getObject("DEPARTMENT").toString();
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
            MyLogger.get().logMessage(processName, "Не удалось получить код отдела из БД " + smbConnect);
            logger.error(processName + " Не удалось получить код отдела из БД " + smbConnect);
            throw new FlowException("Не удалось получить код отдела из БД " + smbConnect);
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
            MyLogger.get().logMessage(processName, "Не удалось подключиться по тонкому клиенту, БД на ремонте");
            logger.error(processName + "Не удалось подключиться по тонкому клиенту, БД на ремонте");
            throw new FlowException("Не удалось подключиться по тонкому клиенту, БД на ремонте");
        }
    }

    public void processCreate() throws FlowException {
        initWhereCondition();

        List<String> packets = getPacketsNumbers();
        if (packets != null) {
            fetchPackets(packets, "Постановление не направлено в банк в связи с отсутствием легитимной копии");
        }
//        List<String> packetsImptyAccount =  getPacketsNumbersImptyAccount();
//        if (packetsImptyAccount != null) {
//            fetchPackets(packetsImptyAccount, "Постановление не направлено в банк в связи с незаполненными сведениями по счетам");
//        }
//        List<String> packetsDiscrepancyAccount =  getPacketsNumbersDiscrepancyAccount();
//        if (packetsDiscrepancyAccount != null) {
////            fetchPackets(packetsDiscrepancyAccount, "Постановление не направлено в банк в связи с не соответсвующим статусам ареста по счетам");
//            fetchPackets(packetsDiscrepancyAccount, "Постановление не направлено в банк в связи с не соответствующим статусом ареста по счетам");
//
//        }
//            for (String packetId : packets) {
//                SberbankPostWriter sberbankPostWriter = openPostWriter(packetId);
//                //прерываем обработку, если достигли лимит файлов для запросов
//                if (sberbankPostWriter != null) {
//                    fetchPackets(packetId);
//                    fetchPackets_omena(packetId);
//                    sberbankPostWriter.close();
////                jdbcTemplate.execute("UPDATE EXT_RESTRICTION SET PROCEED = 1 WHERE PACK_ID = " + packetId);
//                }
//            }
//        }
    }


    private void initWhereCondition() {
        String whereArr[] = {
                "ER.AGENT_AGREEMENT_CODE = '" + properties.get("MVV_AGREEMENT_CODE") + "'",
                "ER.AGENT_CODE = '" + properties.get("MVV_AGENT_CODE") + "'",
                "ER.AGENT_DEPT_CODE = '" + properties.get("MVV_AGENT_DEPT_CODE") + "'",
                "PROCEED = 1",
//                "PIEV_ATTACH = 0",
                "ER.act_id not in (SELECT erp.restrictn_internal_key FROM ext_report erp group by erp.restrictn_internal_key)",
//                "ER.pack_id = 25501008845225",
//                "ENTITY.ENTT_TYPEID IN (2, 71, 95, 96, 97, 666)",
                "ER.doc_date >= '" + properties.get("StartDatePost") + "'"
//                "ER.doc_date <= '" + properties.get("EndDatePost") + "'",
//                "MD.usage is not null"
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

    private List<String> getPacketsNumbers() throws FlowException {
        String query = "SELECT DISTINCT ER.ACT_ID " +
                " FROM EXT_RESTRICTION ER\n" +
//                "        INNER JOIN DOCUMENT D ON ER.IP_ID = D.ID\n" +
//                "        INNER JOIN DOC_IP ON D.ID = DOC_IP.ID\n" +
//                "        INNER JOIN DOC_IP_DOC DID ON D.ID = DID.ID\n" +
//                "        INNER JOIN ENTITY ON DOC_IP.ID_DBTR = ENTITY.ENTT_ID\n" +
//
//                "        JOIN SENDLIST S ON S.SENDLIST_O_ID = ER.ACT_ID\n" +
//                "        JOIN O_IP ON O_IP.ID = S.SENDLIST_O_ID\n" +
//                "        INNER JOIN MVV_DATUM MD ON MD.DOCUMENT_ID = O_IP.ID" +

                where +
                " AND PIEV_ATTACH = 0" +
                " GROUP BY ER.ACT_ID";

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
                packets.add(resultSet.getObject("ACT_ID").toString());
            }
            System.out.println("Постановлений без легитимной копии: " + packets.size());
            MyLogger.get().logMessage(processName, "Постановлений без легитимной копии: " + packets.size());
            logger.info(processName + " - Постановлений без легитимной копии: " + packets.size());

        } catch (SQLException e) {
            System.out.println("Не удалось подключиться и собрать постановления без легитимной копии");
            MyLogger.get().logMessage(processName, "Не удалось подключиться и собрать постановления без легитимной копии");
            logger.error(processName + " - Не удалось подключиться и собрать постановления без легитимной копии");
            JDBCConnection.getInstance().jdbcClose(processName);
            return null;
//            e.printStackTrace();
        }
        JDBCConnection.getInstance().jdbcClose(processName);
        return packets;
    }


    private List<String> getPacketsNumbersImptyAccount() throws FlowException {
        String query = "SELECT DISTINCT ER.ACT_ID " +
                " FROM EXT_RESTRICTION ER\n" +
                " INNER JOIN DOCUMENT D ON ER.IP_ID = D.ID\n" +
                " INNER JOIN DOC_IP ON D.ID = DOC_IP.ID\n" +
                " INNER JOIN DOC_IP_DOC DID ON D.ID = DID.ID\n" +
                " INNER JOIN ENTITY ON DOC_IP.ID_DBTR = ENTITY.ENTT_ID\n" +
                " JOIN SENDLIST S ON S.SENDLIST_O_ID = ER.ACT_ID\n" +
                " JOIN O_IP ON O_IP.ID = S.SENDLIST_O_ID\n" +
                " JOIN DOCUMENT DOC ON DOC.ID = O_IP.ID\n" +
//                " JOIN O_IP_ACT_ENDGACCOUNT_MONEY GACC  ON GACC.ID = DOC.ID\n" +
                " left JOIN MVV_DATUM MD ON MD.DOCUMENT_ID = O_IP.ID\n" +
                " left JOIN MVV_DATUM_AVAILABILITY_ACC ACC ON ACC.ID = MD.ID\n" +
                " left join MVV_DATUM_ACCOUNT MDA on MDA.ID = ACC.ID" +
//                "        INNER JOIN DOCUMENT D ON ER.IP_ID = D.ID\n" +
//                "        INNER JOIN DOC_IP ON D.ID = DOC_IP.ID\n" +
//                "        INNER JOIN DOC_IP_DOC DID ON D.ID = DID.ID\n" +
//                "        INNER JOIN ENTITY ON DOC_IP.ID_DBTR = ENTITY.ENTT_ID\n" +
//
//                "        JOIN SENDLIST S ON S.SENDLIST_O_ID = ER.ACT_ID\n" +
//                "        JOIN O_IP ON O_IP.ID = S.SENDLIST_O_ID\n" +
//                "        INNER JOIN MVV_DATUM MD ON MD.DOCUMENT_ID = O_IP.ID" +

                where +
                " AND (MD.usage is null" +
                " OR MDA.ACC is null)"+
                " GROUP BY ER.ACT_ID";

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
                packets.add(resultSet.getObject("ACT_ID").toString());
            }
            System.out.println("Постановлений с незаполненными сведениями по счетам: " + packets.size());
            MyLogger.get().logMessage(processName, "Постановлений с незаполненными сведениями по счетам: " + packets.size());
            logger.info(processName + " - Постановлений с незаполненными сведениями по счетам: " + packets.size());

        } catch (SQLException e) {
            System.out.println("Не удалось подключиться и собрать постановления с незаполненными сведениями по счетам");
            MyLogger.get().logMessage(processName, "Не удалось подключиться и собрать постановления с незаполненными сведениями по счетам");
            logger.error(processName + " - Не удалось подключиться и собрать постановления с незаполненными сведениями по счетам");
            JDBCConnection.getInstance().jdbcClose(processName);
            return null;
//            e.printStackTrace();
        }
        JDBCConnection.getInstance().jdbcClose(processName);
        return packets;
    }

    private List<String> getPacketsNumbersDiscrepancyAccount() throws FlowException {
        String query = "SELECT DISTINCT ER.ACT_ID " +
                " FROM EXT_RESTRICTION ER\n" +
                " INNER JOIN DOCUMENT D ON ER.IP_ID = D.ID\n" +
                " INNER JOIN DOC_IP ON D.ID = DOC_IP.ID\n" +
                " INNER JOIN DOC_IP_DOC DID ON D.ID = DID.ID\n" +
                " INNER JOIN ENTITY ON DOC_IP.ID_DBTR = ENTITY.ENTT_ID\n" +
                " JOIN SENDLIST S ON S.SENDLIST_O_ID = ER.ACT_ID\n" +
                " JOIN O_IP ON O_IP.ID = S.SENDLIST_O_ID\n" +
                " JOIN DOCUMENT DOC ON DOC.ID = O_IP.ID\n" +
//                " JOIN O_IP_ACT_ENDGACCOUNT_MONEY GACC  ON GACC.ID = DOC.ID\n" +
                " left JOIN MVV_DATUM MD ON MD.DOCUMENT_ID = O_IP.ID\n" +
                " left JOIN MVV_DATUM_AVAILABILITY_ACC ACC ON ACC.ID = MD.ID\n" +
                " left join MVV_DATUM_ACCOUNT MDA on MDA.ID = ACC.ID" +

                where +
                " AND MD.usage is not null\n" +
                " and MDA.ACC is not null\n" +
                " AND DOC.docstatusid = 2\n" +
                " AND PIEV_ATTACH = 1" +
                " AND (\n" +
                "  (MD.USAGE CONTAINING 'СНЯТЬ АРЕСТ'\n" +
                "  AND ER.DOC_CODE IN ('O_IP_ACT_GETCURRENCY', 'O_IP_ACT_ARREST_ACCMONEY', 'O_IP_ACT_GACCOUNT_MONEY'))\n" +
                " OR\n" +
                " (MD.USAGE CONTAINING 'АРЕСТОВАТЬ' AND\n" +
                "  ER.DOC_CODE IN ('O_IP_ACT_ENDGACCOUNT_MONEY', 'O_IP_ACT_ENDARREST')))"+
                " GROUP BY ER.ACT_ID";

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
                packets.add(resultSet.getObject("ACT_ID").toString());
            }
            System.out.println("Постановлений с не соответсвующим статусам ареста по счетам: " + packets.size());
            MyLogger.get().logMessage(processName, "Постановлений с не соответсвующим статусам ареста по счетам: " + packets.size());
            logger.info(processName + " - Постановлений с не соответсвующим статусам ареста по счетам: " + packets.size());

        } catch (SQLException e) {
            System.out.println("Не удалось подключиться и собрать постановления с не соответствующим статусом ареста по счетам");
            MyLogger.get().logMessage(processName, "Не удалось подключиться и собрать постановления с не соответствующим статусом ареста по счетам");
            logger.error(processName + " - Не удалось подключиться и собрать постановления с не соответствующим статусом ареста по счетам");
            JDBCConnection.getInstance().jdbcClose(processName);
            return null;
//            e.printStackTrace();
        }
        JDBCConnection.getInstance().jdbcClose(processName);
        return packets;
    }


    private void fetchPackets(List<String> packets, String statusNotif) throws FlowException {

        final String databaseURL = dataSource.get(0).getUrl().substring(17, dataSource.get(0).getUrl().indexOf("?"));
        final String jdbcUrl = dataSource.get(0).getUrl();
        final GDSType type = GDSFactory.getTypeForProtocol(jdbcUrl);
        final Properties props = new Properties();
        props.setProperty("user", dataSource.get(0).getUsername());
        props.setProperty("password", dataSource.get(0).getPassword());

        try {
            final Map<String, String> normalizedInfo = FBDriverPropertyManager.normalize(jdbcUrl, props);
            FBSADataSource fbDataSource = new FBSADataSource(type);
            fbDataSource.setDatabase(databaseURL);
            for (Map.Entry<String, String> entry : normalizedInfo.entrySet())
                fbDataSource.setNonStandardProperty(entry.getKey(), entry.getValue());

            fbDataSource.setNonStandardProperty("isc_dpb_process_name", processName);
            final Connection con = fbDataSource.getConnection();

            String smbConnect = dataSource.get(0).getUrl().substring(17, dataSource.get(0).getUrl().indexOf("/"));
            boolean testConnect = new TestConnect().TestConnect("http://" + smbConnect + ":8080/pksp-server/");
            if (testConnect == false) {
                MyLogger.get().logMessage(processName, "Не удалось подключиться по тонкому клиенту, БД" + smbConnect + " на ремонте");
                logger.error(processName + "Не удалось подключиться по тонкому клиенту, БД" + smbConnect + " на ремонте");
                throw new FlowException("Не удалось подключиться по тонкому клиенту, БД" + smbConnect + " на ремонте");
            }

            final PreparedStatement genidPSt = con.prepareStatement("SELECT NEXT VALUE FOR SEQ_DOCUMENT FROM RDB$DATABASE");
            final PreparedStatement new_idPSt = con.prepareStatement("SELECT NEXT VALUE FOR SEQ_DOCUMENT FROM RDB$DATABASE");
            final PreparedStatement new_pakumberPSt = con.prepareStatement("SELECT NEXT VALUE FOR seq_ext_input_header FROM RDB$DATABASE");
            final PreparedStatement ip_idPSt = con.prepareStatement("select ip_id from O_IP where id = ?");
            final PreparedStatement bar_codePSt = con.prepareStatement("select barcode, DOC_NUMBER from DOCUMENT where id = ?");

            String sql1 = "INSERT INTO EXT_INPUT_HEADER (" +
                    "ID, " +
                    "PACK_NUMBER, " +
                    "PROCEED, " +
                    "AGENT_CODE, " +
                    "AGENT_DEPT_CODE, " +
                    "AGENT_AGREEMENT_CODE, " +
                    "EXTERNAL_KEY, " +
                    "METAOBJECTNAME, " +
                    "DATE_IMPORT, " +
                    "SOURCE_BARCODE" +
                    ") " +
                    "VALUES ( " +
//                    new_id + ", " +
                    "?," +
//                    "0, " +
                    "?, " +
                    "0, " +
                    "'" + properties.get("MVV_AGENT_CODE") + "', " +
                    "'" + properties.get("MVV_AGENT_DEPT_CODE") + "', " +
                    "'" + properties.get("MVV_AGREEMENT_CODE") + "', " +
//                "'СБЕРБАНК', " +
//                "'СБЕРБАНКИРК', " +
//                "'СБЕРБАНКСОГЛ', " +
//                    "'" + uuid + "', " +
                    " ?," +
                    "'EXT_REPORT', " +
                    "CAST('NOW' AS DATE), " +
//                    "'" + bar_code + "'" +
                    "?" +
                    ")";

            final PreparedStatement insert1 = con.prepareStatement(sql1);

            String sql2 = "insert into EXT_REPORT\n" +
                    "(" +
                    "ID, " +
                    "IP_INTERNAL_KEY, " +
                    "RESTRICTN_INTERNAL_KEY, " +
                    "DOC_DATE, " +
                    "RESTRICTION_ANSWER_TYPE, " +
                    "DESCRIPTION, " +
                    "LEGAL_IMPOSSIBILITY, " +
                    "INSIDE_INFORMATION " +
                    ") values ( " +
//                    +new_id + ", " +
                    " ?, " +
//                    +ip_id + ", " +
                    " ?, " +
//                    +restriction_int_key + ", " +
                    " ?, " +
                    " cast('NOW' as date), " +
//                    status + ", " +
                    " 5, " +
                    "'Этот текст никуда не попадает'," +
//                    " ?, " +
                    " ?, " +
                    " ? " +
                    ")";

            final PreparedStatement insert2 = con.prepareStatement(sql2);


            try {
                Long new_paknumber = null;

                ResultSet  new_pakumberRSt = new_pakumberPSt.executeQuery();
                while (new_pakumberRSt.next()) new_paknumber = new_pakumberRSt.getLong(1);
                if (new_pakumberRSt != null)new_pakumberRSt.close();
//                System.out.println(new_id);

                for (String packetId : packets) {
//                    System.out.println(packetId);
//                    if (notification.getId().equals("null")) {
//                        System.out.println("В файле ID Постановления = null, если данных больше нет, то файл удалить вручную!");
//                        MyLogger.get().logMessage(processName, "В файле ID Постановления = null, если данных больше нет, то файл удалить вручную!");
//                        logger.error("Notiff - В файле ID Постановления = null, если данных больше нет, то файл удалить вручную!");
////            return false;
//                        continue;
//                    }
                    Long new_id = null;
                    Long ip_id = null;
                    String bar_code = null;
                    String ActNumber = null;
//                String restriction_int_key = null;

                    ResultSet new_idRSt = new_idPSt.executeQuery();
                    while (new_idRSt.next()) new_id = new_idRSt.getLong(1);
                    if (new_idRSt != null) new_idRSt.close();
//                System.out.println(new_id);

                    ip_idPSt.setString(1, packetId);
                    ResultSet ip_idRSt = ip_idPSt.executeQuery();
                    while (ip_idRSt.next()) ip_id = ip_idRSt.getLong("ip_id");
                    if (ip_idRSt != null) ip_idRSt.close();
//                System.out.println(ip_id);

                    bar_codePSt.setString(1, packetId);
                    ResultSet bar_codeRSt = bar_codePSt.executeQuery();
                    while (bar_codeRSt.next()) {
                        bar_code = bar_codeRSt.getString("barcode");
                        ActNumber = bar_codeRSt.getString("DOC_NUMBER");
                    }
                    if (bar_codeRSt != null) bar_codeRSt.close();

//                System.out.println(notification.getId() + " " + ip_id + " " + bar_code);


//                restriction_int_keyPSt.setString(1, "Банк");
//                restriction_int_keyPSt.setString(2, notification.getId());
//                ResultSet restriction_int_keyRSt = restriction_int_keyPSt.executeQuery();
//                while (restriction_int_keyRSt.next())restriction_int_keys = restriction_int_keyRSt.getLong(1);
//                restriction_int_keyRSt.close();

                    String uuid = UUID.randomUUID().toString();
                    if (ip_id == null) {
                        System.out.println("ИП не найдено, наверно окончено" + packetId);
                        MyLogger.get().logMessage(processName, "ИП не найдено, наверно окончено" + packetId);
//                    System.out.println(notification.getId());
                        continue;
                    }

                    insert1.setLong(1, new_id);
                    insert1.setLong(2, new_paknumber);
                    insert1.setString(3, uuid);
                    insert1.setString(4, bar_code);
                    insert1.executeUpdate();

                    insert2.setLong(1, new_id);
                    insert2.setLong(2, ip_id);
//                insert2.setLong(3, restriction_int_keys);
                    insert2.setString(3, packetId);
//                    insert2.setString(4, notification.toString());
//                    insert2.setString(5, notification.getStatus() + " - " + notification.getProcNumberState());
                    insert2.setString(4, statusNotif);
                    insert2.setString(5, statusNotif);
                    insert2.executeUpdate();

                    logger.info(packetId + " " + ip_id + " " + bar_code + " " + ActNumber);
                    MyLogger.get().logMessage(processName, packetId + " " + ip_id + " " + bar_code + " " + ActNumber);
                    System.out.print("*");
//                System.out.println("***********");
//                25301053246407
                }

            } finally {
                try {
//                    if (insert2.isClosed()) System.out.println("66666666");
                    if (insert2 != null) insert2.close();
//                    System.out.println("+++++");
                    if (insert1 != null) insert1.close();
                    if (bar_codePSt != null) bar_codePSt.close();
                    if (ip_idPSt != null) ip_idPSt.close();
                    if (new_pakumberPSt != null) new_pakumberPSt.close();
                    if (new_idPSt != null) new_idPSt.close();
                    if (con != null) con.close();
                    if (fbDataSource != null) fbDataSource.close();
                } catch (ResourceException e) {
                    MyLogger.get().logMessage(processName, "не закрылся fbDataSource " + e);
                    System.out.println("не закрылся fbDataSource " + e);
                    logger.error("не закрылся fbDataSource " + e);
                } catch (SQLException e) {
                    MyLogger.get().logMessage(processName, "Ошибка подключения к БД " + e.getMessage());
                    logger.error("Notif - Ошибка подключения к БД " + e.getMessage());
//                    return false;
//                    throw new FlowException("Ошибка подключения к БД " + e.getMessage());
//                e.printStackTrace();
                }
            }
        } catch (SQLException e) {
            System.out.println("\nОшибка подключения к БД " + jdbcUrl + "\n" + e.getMessage());
            MyLogger.get().logMessage(processName, "Ошибка подключения к БД " + jdbcUrl + "\n" + e.getMessage());
            logger.error("Notif - Ошибка подключения к БД " + jdbcUrl + "\n" + e.getMessage());
//            e.printStackTrace();
//            return false;
        }

//        return true;
    }

}
