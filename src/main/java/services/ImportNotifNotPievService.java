package services;

import exceptions.FlowException;
import jdbc.BeanConnect;
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
 * Date: 5/23/16
 * Time: 12:04 PM
 */
public class ImportNotifNotPievService {

    List<BeanConnect> dataSource;
    LinkedHashMap<String, String> properties;
    String ddmmyy;
    String processName;

    final static Logger logger = Logger.getLogger(ImportNotifNotPievService.class);

    public ImportNotifNotPievService(List<BeanConnect> dataSource, LinkedHashMap<String, String> properties, String processName) throws FlowException {
        this.dataSource = dataSource;
        this.properties = properties;
        this.processName = processName;
    }


    public boolean processResponse(String fileCopy,  String statusNotif) throws FlowException {
//        String statusNotif = "Постановление не направлено в банк в связи с отсутствием легитимной копии (в указанном каталоге отсутствует piev документ)";
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
                    "0, " +
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
//                "piev_" + getLong(resultSet, "ID") + ""
                String packetId = fileCopy.replace("piev_", "").replace(".xml.zip", "");
                MyLogger.get().logMessage(processName, "uv "+packetId);
//                for (String packetId : packets) {
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


                    String uuid = UUID.randomUUID().toString();
                    if (ip_id == null) {
                        System.out.println("ИП не найдено, наверно окончено" + packetId);
                        MyLogger.get().logMessage(processName, "ИП не найдено, наверно окончено" + packetId);
//                    System.out.println(notification.getId());
                        return false;
                    }

                    insert1.setLong(1, new_id);
                    insert1.setString(2, uuid);
                    insert1.setString(3, bar_code);
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
//                }
            } finally {
                try {
                    if (insert2 != null) insert2.close();
                    if (insert1 != null) insert1.close();
                    if (bar_codePSt != null) bar_codePSt.close();
                    if (ip_idPSt != null) ip_idPSt.close();
                    if (new_idPSt != null) new_idPSt.close();
                    if (con != null) con.close();
                    if (fbDataSource != null) fbDataSource.close();
                } catch (ResourceException e) {
                    MyLogger.get().logMessage(processName, "не закрылся fbDataSource " + e);
                    System.out.println("не закрылся fbDataSource " + e);
                    logger.error("не закрылся fbDataSource " + e);
                    return false;
                } catch (SQLException e) {
                    MyLogger.get().logMessage(processName, "Ошибка подключения к БД " + e.getMessage());
                    logger.error("Notif - Ошибка подключения к БД " + e.getMessage());
                    return false;
//                    throw new FlowException("Ошибка подключения к БД " + e.getMessage());
                }
            }
        } catch (SQLException e) {
            System.out.println("\nОшибка подключения к БД " + jdbcUrl + "\n" + e.getMessage());
            MyLogger.get().logMessage(processName, "Ошибка подключения к БД " + jdbcUrl + "\n" + e.getMessage());
            logger.error("Notif - Ошибка подключения к БД " + jdbcUrl + "\n" + e.getMessage());
            return false;
        }

        return true;
    }


}
