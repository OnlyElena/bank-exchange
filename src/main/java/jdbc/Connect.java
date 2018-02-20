package jdbc;

import exceptions.FlowException;
import org.apache.log4j.Logger;
import org.firebirdsql.gds.impl.GDSFactory;
import org.firebirdsql.gds.impl.GDSType;
import org.firebirdsql.jca.FBSADataSource;
import org.firebirdsql.jdbc.FBDriverPropertyManager;
import services.MyLogger;


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
 * Date: 3/11/16
 * Time: 1:31 PM
 */
public class Connect implements Serializable {
    final static Logger logger = Logger.getLogger(Connect.class);

    private static Connect ourInstance = new Connect();

    public static Connect getInstance() {
//        Thread.currentThread().setName("Bank-exchange");
//        System.out.println(Thread.currentThread());
        return ourInstance;
    }

    public Connect() {
    }

    private static FBSADataSource fbDataSource = null;

    public static Connection Connect(List<BeanConnect> dataSource, String processName) throws SQLException {

//        final String databaseURL = dataSource.iterator().next().getUrl().replace("jdbc:firebirdsql:","").replace("?lc_ctype=WIN1251","");

//        final String databaseURL = dataSource.get(0).getUrl().replace("jdbc:firebirdsql:","").replace("?lc_ctype=WIN1251","");
        final String databaseURL = dataSource.get(0).getUrl().substring(17, dataSource.get(0).getUrl().indexOf("?"));
//        10.38.99.241/3052:ncore-fssp
//        System.out.println(databaseURL);


//        System.out.println(databaseURL);
//        final String databaseURL = "192.168.0.104/3052:ncore-fssp";
//        final String jdbcUrl = "jdbc:firebirdsql:" + databaseURL + "?lc_ctype=WIN1251";
//        final String jdbcUrl = databaseURL;
//        final String jdbcUrl = dataSource.iterator().next().getUrl();
        final String jdbcUrl = dataSource.get(0).getUrl();

//        System.out.println(jdbcUrl);

//        final String jdbcUrl = "jdbc:firebirdsql:192.168.0.104/3052:ncore-fssp?lc_ctype=WIN1251";
        final GDSType type = GDSFactory.getTypeForProtocol(jdbcUrl);
        final Properties props = new Properties();
//        props.setProperty("user", dataSource.iterator().next().getUsername());
        props.setProperty("user", dataSource.get(0).getUsername());
//        System.out.println(dataSource.get(0).getUsername());
//            props.setProperty("password", dataSource.iterator().next().getPassword());
        props.setProperty("password", dataSource.get(0).getPassword());
//        System.out.println(dataSource.get(0).getPassword());
//        System.out.println(dataSource.iterator().next().getUsername()+" "+dataSource.iterator().next().getPassword());

        try {
            final Map<String, String> normalizedInfo = FBDriverPropertyManager.normalize(jdbcUrl, props);
//            System.out.println(type);
//        final FBSADataSource fbDataSource = new FBSADataSource(type);
            fbDataSource = new FBSADataSource(type);

//            System.out.println(fbDataSource);
            fbDataSource.setDatabase(databaseURL);
            for (Map.Entry<String, String> entry : normalizedInfo.entrySet())
                fbDataSource.setNonStandardProperty(entry.getKey(), entry.getValue());

//        final String processName = "Stat RegMVV sberpost";
            fbDataSource.setNonStandardProperty("isc_dpb_process_name", processName);
            final Connection con = fbDataSource.getConnection();
//            con.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

//            final Statement st = con.createStatement();
//            final ResultSet rs = st.executeQuery("select * from mon$attachments");
//            while (rs.next()) {
//                System.out.println(rs.getString("mon$remote_process"));
//            }
            return con;
        } catch (SQLException e) {
            System.out.println("\nОшибка подключения к БД " + jdbcUrl + "\n" + e.getMessage());
            MyLogger.get().logMessage("Connect", "Ошибка подключения к БД " + jdbcUrl + "\n" + e.getMessage());
            logger.error("Connect - Ошибка подключения к БД " + jdbcUrl + "\n" + e.getMessage());
//            e.printStackTrace();
            return null;
        }
    }

    public static void CloseFBConnect(String processName) throws FlowException {
        try {
            if (fbDataSource != null)
                fbDataSource.close();
        } catch (ResourceException e) {
            System.out.println("Connect - Не удалось закрыть подключение"+e.getMessage());
            MyLogger.get().logMessage(processName, "Не удалось закрыть подключение"+e.getMessage());
            logger.error(processName+" Connect - Не удалось закрыть подключение" + e.getMessage());
            throw new FlowException("Connect - Не удалось закрыть подключение" + e.getMessage());
//            e.printStackTrace();
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
