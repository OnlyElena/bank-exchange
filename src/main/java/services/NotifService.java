package services;

import beans.NotifBean;
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
 * Date: 4/7/16
 * Time: 11:11 AM
 */
public class NotifService {
    final static Logger logger = Logger.getLogger(NotifService.class);

    List<BeanConnect> dataSource;
    LinkedHashMap<String, String> properties;
    String processName;

    public NotifService(List<BeanConnect> dataSource, LinkedHashMap<String, String> properties, String processName) {
        this.dataSource = dataSource;
        this.properties = properties;
        this.processName = processName;
//        processNotif();
    }

    public boolean processNotif(List<NotifBean> res) throws FlowException {
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
                MyLogger.get().logMessage(processName, "Не удалось подключиться по тонкому клиенту, БД"+smbConnect+" на ремонте");
                logger.error(processName + "Не удалось подключиться по тонкому клиенту, БД"+smbConnect+" на ремонте");
                throw new FlowException("Не удалось подключиться по тонкому клиенту, БД"+smbConnect+" на ремонте");
            }

            final PreparedStatement genidPSt = con.prepareStatement("SELECT NEXT VALUE FOR SEQ_DOCUMENT FROM RDB$DATABASE");

//            con.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

//            Long new_id = jdbcTemplate.queryForLong("SELECT NEXT VALUE FOR SEQ_DOCUMENT FROM RDB$DATABASE");
//            final PreparedStatement new_idPSt = con.prepareStatement("SELECT NEXT VALUE FOR SEQ_DOCUMENT FROM RDB$DATABASE", PreparedStatement.RETURN_GENERATED_KEYS);
            final PreparedStatement new_idPSt = con.prepareStatement("SELECT NEXT VALUE FOR SEQ_DOCUMENT FROM RDB$DATABASE");
            final PreparedStatement new_pakumberPSt = con.prepareStatement("SELECT NEXT VALUE FOR seq_ext_input_header FROM RDB$DATABASE");

//            Long ip_id = jdbcTemplate.queryForLong("select ip_id from O_IP where id = " + doc_id);
            final PreparedStatement ip_idPSt = con.prepareStatement("select ip_id from O_IP where id = ?");
//            Object[] parameters = new Object[]{doc_id};
//            String bar_code = jdbcTemplate.queryForObject("select barcode from DOCUMENT where id = ?", parameters, String.class);
            final PreparedStatement bar_codePSt = con.prepareStatement("select barcode, DOC_NUMBER from DOCUMENT where id = ?");

//            По данному запросу будет получен id из списка рассылки, а я оставлю id самого постановления-act_id, т.к. все равно идет привязка.
//            Long restriction_int_key = jdbcTemplate.queryForLong("select FIRST 1 id from sendlist where sendlist_o_id = " + doc_id + " and sendlist_contr_type containing 'Банк'");
//            final PreparedStatement restriction_int_keyPSt = con.prepareStatement("select FIRST 1 id from sendlist where sendlist_contr_type containing \'Банк\' and sendlist_o_id = ?");
//            final PreparedStatement restriction_int_keyPSt = con.prepareStatement("select FIRST 1 id from sendlist where sendlist_contr_type LIKE ? and sendlist_o_id = ?");

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
                    "?," +
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
//                    "RESTRICTION_ANSWER_TYPE, " +
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
//                    " ?, " +
//                    "'Этот текст никуда не попадает'," +
                    " ?, " +
                    " ?, " +
                    " ? " +
                    ")";

            final PreparedStatement insert2 = con.prepareStatement(sql2);


//            final Statement st = con.createStatement();
//            final ResultSet rs = st.executeQuery("select * from mon$attachments");
//            while (rs.next()) {
//                System.out.println(rs.getString("mon$remote_process"));
//            }

            try {
                Long new_paknumber = null;

                ResultSet  new_pakumberRSt = new_pakumberPSt.executeQuery();
                while (new_pakumberRSt.next()) new_paknumber = new_pakumberRSt.getLong(1);
                if (new_pakumberRSt != null)new_pakumberRSt.close();
//                System.out.println(new_id);

            for (NotifBean notification : res) {
                if (notification.getId().equals("null")) {
                    System.out.println("В файле ID Постановления = null!");
                    MyLogger.get().logMessage(processName, "В файле ID Постановления = null!");
                    logger.error("Notiff - В файле ID Постановления = null!");
//            return false;
                    continue;
                }
                Long new_id = null;
                Long ip_id = null;
                String bar_code = null;
//                String restriction_int_key = null;

                ResultSet new_idRSt = new_idPSt.executeQuery();
                while (new_idRSt.next()) new_id = new_idRSt.getLong(1);
                if (new_idRSt != null)new_idRSt.close();
//                System.out.println(new_id);



                ip_idPSt.setString(1, notification.getId());
                ResultSet ip_idRSt = ip_idPSt.executeQuery();
                while (ip_idRSt.next()) ip_id = ip_idRSt.getLong("ip_id");
                if (ip_idRSt != null) ip_idRSt.close();
//                System.out.println(ip_id);

                bar_codePSt.setString(1, notification.getId());
                ResultSet bar_codeRSt = bar_codePSt.executeQuery();
                while (bar_codeRSt.next()) {
                    bar_code = bar_codeRSt.getString("barcode");
                    notification.setActNumber(bar_codeRSt.getString("DOC_NUMBER"));
                }
                if (bar_codeRSt != null)bar_codeRSt.close();

//                System.out.println(notification.getId() + " " + ip_id + " " + bar_code);


//                restriction_int_keyPSt.setString(1, "Банк");
//                restriction_int_keyPSt.setString(2, notification.getId());
//                ResultSet restriction_int_keyRSt = restriction_int_keyPSt.executeQuery();
//                while (restriction_int_keyRSt.next())restriction_int_keys = restriction_int_keyRSt.getLong(1);
//                restriction_int_keyRSt.close();

                String uuid = UUID.randomUUID().toString();
                if (ip_id == null){
                    System.out.println("ИП не найдено, наверно окончено"+notification.getId());
                    MyLogger.get().logMessage(processName, "ИП не найдено, наверно окончено"+notification.getId());
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
                insert2.setString(3, notification.getId());
                insert2.setString(4, notification.toString());
                insert2.setString(5, notification.getStatus() + " - " + notification.getProcNumberState());
                insert2.setString(6, notification.toString());
                insert2.executeUpdate();

//                con.commit();
//                logger.info(notification.getId() + " " + ip_id + " " + bar_code + " " + notification.getProcNumberState());
                MyLogger.get().logMessage(processName, notification.getId() + " " + ip_id + " " + bar_code + " " + notification.getProcNumberState());
                System.out.print("*");
//                System.out.println("***********");
//                25301053246407
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
                    return false;
//                    throw new FlowException("Ошибка подключения к БД " + e.getMessage());
//                e.printStackTrace();
                }
            }
        } catch (SQLException e) {
            System.out.println("\nОшибка подключения к БД " + jdbcUrl + "\n" + e.getMessage());
            MyLogger.get().logMessage(processName, "Ошибка подключения к БД " + jdbcUrl + "\n" + e.getMessage());
            logger.error("Notif - Ошибка подключения к БД " + jdbcUrl + "\n" + e.getMessage());
//            e.printStackTrace();
            return false;
        }

        return true;
    }
}
