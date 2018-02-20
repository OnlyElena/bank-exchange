package jdbc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * @author: OnlyElena@mail.ru
 * Date: 3/11/16
 * Time: 1:42 PM
 */
public class jdbcConnectionOld {
        Connection Conn = null;
        Statement statement = null;
        ResultSet resultSet = null;

        public ResultSet jdbcConnection(List<BeanConnect> dataSource,String processName, String query){
//        List<ExPro> dataSource;

            try {
                System.out.println(dataSource.get(0).getUrl());
                Conn = Connect.Connect(dataSource, processName);
            } catch (SQLException e) {
                e.printStackTrace();
            }

            try {
                statement = Conn.createStatement();
                resultSet = statement.executeQuery(query);

                return resultSet;

//            while (resultSet.next()){
//                depCode = resultSet.getObject("DEPARTMENT").toString();
//                System.out.println("\nУспешное подключение к БД: " + resultSet.getObject("DIV_NAME"));
////                MyLogger.get().logMessage("Request", "\nУспешное подключение к БД: " + map.get("DIV_NAME"));
//            }
            } catch (SQLException e) {
                System.out.println("Ошибка подключения к БД "+e.getMessage());
                e.printStackTrace();
//        }finally{
//            try {
//                resultSet.close();
//                statement.close();
//                Conn.close();
//            } catch (SQLException e) {
//                e.printStackTrace();
//            }
            }
            return null;
        }

        public void jdbcClose(){
            try {
                resultSet.close();
                statement.close();
                Conn.close();
            } catch (SQLException e) {
                System.out.println("Ошибка подключения к БД "+e.getMessage());
                e.printStackTrace();
            }

        }

    }
