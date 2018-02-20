package jdbc;

import exceptions.FlowException;
import org.apache.log4j.Logger;
import org.firebirdsql.gds.impl.GDSFactory;
import org.firebirdsql.gds.impl.GDSType;
import org.firebirdsql.jca.FBSADataSource;
import org.firebirdsql.jdbc.FBDriverPropertyManager;
import service.MyLoggerBanks;

import javax.resource.ResourceException;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @author: OnlyElena@mail.ru
 * Date: 6/18/16
 * Time: 11:42 PM
 */
public class ConnectBanks implements Serializable {
    final static Logger logger = Logger.getLogger(ConnectBanks.class);

    private static ConnectBanks ourInstanceBanks = new ConnectBanks();

    public static ConnectBanks getInstance() {
//        Thread.currentThread().setName("Bank-exchange");
//        System.out.println(Thread.currentThread());
        return ourInstanceBanks;
    }

    public ConnectBanks() {
    }

    private static FBSADataSource fbsaDataSourceBanks = null;

    public static Connection ConnectBanks(List<BeanConnectBanks> dataSourceBanks, String processNameBanks) throws SQLException {
        final String databaseURLBanks = dataSourceBanks.get(0).getUrl().substring(17, dataSourceBanks.get(0).getUrl().indexOf("?"));
        final String jdbcUrlBanks = dataSourceBanks.get(0).getUrl();

        final GDSType typeBanks = GDSFactory.getTypeForProtocol(jdbcUrlBanks);
        final Properties propsBanks = new Properties();
        propsBanks.setProperty("user", dataSourceBanks.get(0).getUsername());
        propsBanks.setProperty("password", dataSourceBanks.get(0).getPassword());

        try {
            final Map<String, String> normalizedInfo = FBDriverPropertyManager.normalize(jdbcUrlBanks, propsBanks);
            fbsaDataSourceBanks = new FBSADataSource(typeBanks);

            fbsaDataSourceBanks.setDatabase(databaseURLBanks);
            for (Map.Entry<String, String> entry : normalizedInfo.entrySet())
                fbsaDataSourceBanks.setNonStandardProperty(entry.getKey(), entry.getValue());

            fbsaDataSourceBanks.setNonStandardProperty("isc_dpb_process_name", processNameBanks);
            final Connection conBanks = fbsaDataSourceBanks.getConnection();
//            con.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

//            final Statement st = con.createStatement();
//            final ResultSet rs = st.executeQuery("select * from mon$attachments");
//            while (rs.next()) {
//                System.out.println(rs.getString("mon$remote_process"));
//            }
            return conBanks;
        } catch (SQLException e) {
            System.out.println("\nОшибка подключения к БД " + jdbcUrlBanks + "\n" + e.getMessage());
            MyLoggerBanks.get().logMessage("Connect", "Ошибка подключения к БД " + jdbcUrlBanks + "\n" + e.getMessage());
            logger.error("Connect - Ошибка подключения к БД " + jdbcUrlBanks + "\n" + e.getMessage());
            return null;
        }
    }

    public static void CloseFBConnectBanks(String processNameBanks) throws FlowException {
        try {
            if (fbsaDataSourceBanks != null)
                fbsaDataSourceBanks.close();
        } catch (ResourceException e) {
            System.out.println("Connect - Не удалось закрыть подключение"+e.getMessage());
            MyLoggerBanks.get().logMessage(processNameBanks, "Не удалось закрыть подключение"+e.getMessage());
            logger.error(processNameBanks+" Connect - Не удалось закрыть подключение" + e.getMessage());
            throw new FlowException("Connect - Не удалось закрыть подключение" + e.getMessage());
        }
    }


    public static void main(String[] args) throws SQLException {

        final String databaseURL = "10.38.53.133/3052:ncore-fssp";
        System.out.println(databaseURL);
        final String jdbcUrl = "jdbc:firebirdsql:" + databaseURL + "?lc_ctype=WIN1251";
        System.out.println(jdbcUrl);
//        final String jdbcUrl = "jdbc:firebirdsql:192.168.0.104/3052:ncore-fssp?lc_ctype=WIN1251";
        final GDSType type = GDSFactory.getTypeForProtocol(jdbcUrl);
        final Properties props = new Properties();
        props.setProperty("user", "SYSDBA");
        props.setProperty("password", "qs1x1wdc");

        final Map<String, String> normalizedInfo = FBDriverPropertyManager.normalize(jdbcUrl, props);
        System.out.println(type);
        final FBSADataSource fbDataSource = new FBSADataSource(type);
        System.out.println(fbDataSource);
        fbDataSource.setDatabase(databaseURL);
        for (Map.Entry<String, String> entry : normalizedInfo.entrySet())
            fbDataSource.setNonStandardProperty(entry.getKey(), entry.getValue());

        final String processName = "NAME PROCESS";
        fbDataSource.setNonStandardProperty("isc_dpb_process_name", processName);

        final Connection c = fbDataSource.getConnection();
        final Statement st = c.createStatement();
        final ResultSet rs = st.executeQuery("select * from mon$attachments");
        try {

            while (rs.next()) {
                System.out.println(rs.getString("mon$remote_process"));
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } finally {
            if (st != null) {
                st.close();
                System.out.println("*****");
            }
            if (rs != null) {
                c.close();
                System.out.println("*****");
            }
            if (c != null) {
                c.close();
                System.out.println("*****");
            }
            try {
                //делаем паузу, ждем восстановление связи
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (fbDataSource != null) {
                try {
                    fbDataSource.close();
                } catch (ResourceException e) {
//                    e.printStackTrace();
                    System.out.println("-------------------");
                }
                System.out.println("*****");
            }
        }

        System.out.println(c.isClosed());

        try {
            //делаем паузу, ждем восстановление связи
            Thread.sleep(1000000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

}
