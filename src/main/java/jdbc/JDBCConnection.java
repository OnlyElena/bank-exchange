package jdbc;

import exceptions.FlowException;
import org.apache.log4j.Logger;
import services.MyLogger;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * @author: OnlyElena@mail.ru
 * Date: 3/11/16
 * Time: 5:30 PM
 */
public class JDBCConnection {
    final static Logger logger = Logger.getLogger(JDBCConnection.class);
    private static JDBCConnection ourInstance = new JDBCConnection();

    public static JDBCConnection getInstance() {
        return ourInstance;
    }

    private JDBCConnection() {
    }

    private static Connection Conn;
    private static Statement statement;
    private static ResultSet resultSet;

    public static ResultSet jdbcConnection(List<BeanConnect> dataSource, String processName, String query) throws FlowException {
//        List<ExPro> dataSource;

        try {
//            System.out.println(dataSource.get(0).getUrl());
            Connect connect = new Connect();
            Conn = connect.Connect(dataSource, processName);
            if (Conn == null) {
                try {
                    //делаем паузу, ждем восстановление связи
                    MyLogger.get().logMessage(processName, "ждем восстановление связи " + dataSource.get(0).getBeanId());
                    logger.info("ждем восстановление связи " + dataSource.get(0).getBeanId());
                    Thread.sleep(60000);
                } catch (InterruptedException e) {
                    MyLogger.get().logMessage(processName, "Ошибка подключения к БД (sleep)" + e.getMessage());
                    logger.error(e);
                    e.printStackTrace();
                }
                Conn = connect.Connect(dataSource, processName);
                if (Conn == null) {
                    try {
                        //делаем паузу, ждем восстановление связи
                        MyLogger.get().logMessage(processName, "ждем восстановление связи - вторая попытка");
                        logger.info("ждем восстановление связи - вторая попытка");
                        Thread.sleep(60000);
                    } catch (InterruptedException e) {
                        MyLogger.get().logMessage(processName, "Ошибка подключения к БД (sleep)" + e.getMessage());
                        logger.error(e);
                        e.printStackTrace();
                    }
                    Conn = connect.Connect(dataSource, processName);
                    if (Conn == null) {
                        try {
                            //делаем паузу, ждем восстановление связи
                            MyLogger.get().logMessage(processName, "ждем восстановление связи - вторая попытка");
                            logger.info("ждем восстановление связи - вторая попытка");
                            Thread.sleep(60000);
                        } catch (InterruptedException e) {
                            MyLogger.get().logMessage(processName, "Ошибка подключения к БД (sleep)" + e.getMessage());
                            logger.error(e);
                            e.printStackTrace();
                        }
                        Conn = connect.Connect(dataSource, processName);
                        if (Conn == null) {
                            MyLogger.get().logMessage(processName, "Ошибка подключения к БД ");
                            logger.error(processName + " - Ошибка подключения к БД ");
                            throw new FlowException(processName + " Ошибка подключения к БД ");

                        }

                    }
                }
            }

        } catch (SQLException e) {
            System.out.println("Ошибка подключения к БД " + e.getMessage());
            MyLogger.get().logMessage(processName, "Ошибка подключения к БД " + e.getMessage());
            logger.error(processName + " - Ошибка подключения к БД " + e.getMessage());
            throw new FlowException(processName + " Ошибка подключения к БД ");

        }
        if (Conn == null) {
            MyLogger.get().logMessage(processName, "Ошибка подключения к БД ");
            logger.error(processName + " - Ошибка подключения к БД ");
            throw new FlowException(processName + " Ошибка подключения к БД ");

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
            System.out.println("2 Ошибка подключения к БД " + e.getMessage());
            MyLogger.get().logMessage(processName, "2 Ошибка подключения к БД " + e.getMessage());
            logger.error(processName + " - 2 Ошибка подключения к БД " + e.getMessage());
            jdbcClose(processName);
//            e.printStackTrace();
//        }finally{
//            try {
//                resultSet.close();
//                statement.close();
//                Conn.close();
//            } catch (SQLException e) {
//                e.printStackTrace();
//            }
        }
        MyLogger.get().logMessage(processName, "Ошибка подключения к БД ");
        logger.error(processName + " - Ошибка подключения к БД ");
        throw new FlowException(processName + " Ошибка подключения к БД ");

    }


    public static void jdbcClose(String processName) throws FlowException {
        try {
            System.out.print(" *");
//            System.out.println("Подключение = "+Conn.isClosed()+", закрываем подключение");
//            if (Conn != null) System.out.println();
//            if (resultSet != null) {
//                System.out.println("Производим закрытие подключения");
//            }
//        } catch (SQLException e) {
//            System.out.println("Ошибка подключения к БД " + e.getMessage());
//            throw new FlowException("Ошибка подключения к БД " + e.getMessage());
        } finally {
            try {
                if (resultSet != null) resultSet.close();
                if (statement != null) statement.close();
                if (Conn != null) Conn.close();
                Connect.CloseFBConnect(processName);
            } catch (SQLException e) {
                MyLogger.get().logMessage(processName, "Ошибка подключения к БД " + e.getMessage());
                logger.error(processName + " Close - Ошибка подключения к БД " + e.getMessage());
                throw new FlowException("Ошибка подключения к БД " + e.getMessage());
            }
        }
//        System.out.println("БД закрыта! ");
    }


    public static void jdbcUpdate(List<BeanConnect> dataSource, String processName, String query) throws FlowException {
        try {
            Connect connectt = new Connect();
            MyLogger.get().logMessage(processName, "Update_start");
            Connection Connn = connectt.Connect(dataSource, processName);
            if (Connn == null) {
                try {
                    //делаем паузу, ждем восстановление связи
                    MyLogger.get().logMessage(processName, "ждем восстановление связи " + dataSource.get(0).getBeanId());
                    logger.info("ждем восстановление связи " + dataSource.get(0).getBeanId());
                    Thread.sleep(60000);
                } catch (InterruptedException e) {
                    MyLogger.get().logMessage(processName, "Ошибка подключения к БД (sleep)" + e.getMessage());
                    logger.error(e);
                    e.printStackTrace();
                }
                Connn = connectt.Connect(dataSource, processName);
                if (Connn == null) {
                    try {
                        //делаем паузу, ждем восстановление связи
                        MyLogger.get().logMessage(processName, "ждем восстановление связи - вторая попытка");
                        logger.info("ждем восстановление связи - вторая попытка");
                        Thread.sleep(60000);
                    } catch (InterruptedException e) {
                        MyLogger.get().logMessage(processName, "Ошибка подключения к БД (sleep)" + e.getMessage());
                        logger.error(e);
                        e.printStackTrace();
                    }
                    Connn = connectt.Connect(dataSource, processName);
                    if (Connn == null) {
                        try {
                            //делаем паузу, ждем восстановление связи
                            MyLogger.get().logMessage(processName, "ждем восстановление связи - вторая попытка");
                            logger.info("ждем восстановление связи - вторая попытка");
                            Thread.sleep(60000);
                        } catch (InterruptedException e) {
                            MyLogger.get().logMessage(processName, "Ошибка подключения к БД (sleep)" + e.getMessage());
                            logger.error(e);
                            e.printStackTrace();
                        }
                        Connn = connectt.Connect(dataSource, processName);
                        if (Connn == null) {
                            MyLogger.get().logMessage(processName, "Ошибка подключения к БД ");
                            logger.error(processName + " - Ошибка подключения к БД ");
                            throw new FlowException(processName + " Ошибка подключения к БД ");

                        }

                    }
                }
            }


            if (Connn == null) {
                MyLogger.get().logMessage(processName, "Ошибка подключения к БД ");
                logger.error(processName + " - Ошибка подключения к БД ");
                throw new FlowException(processName + " Ошибка подключения к БД ");

            }

            System.out.println("Connect установелен!");

            Statement statemen = null;
            try {
                statemen = Connn.createStatement();
                System.out.println("statement is TRU!");
                if (statemen == null) statemen = Connn.createStatement();
                System.out.println(query);
                statemen.executeUpdate(query);
                System.out.println("Update successful");

                System.out.println("Update");
                MyLogger.get().logMessage(processName, "Update_end");
            } catch (SQLException e) {
                System.out.println("Ошибка подключения к БД 2" + e.getMessage());
                MyLogger.get().logMessage(processName, "Ошибка подключения к БД 2" + e.getMessage());
                logger.error(processName + " Update - Ошибка подключения к БД 2" + e.getMessage());
                throw new FlowException(processName + " Ошибка подключения к БД 2" + e.getMessage());
            } finally {
                try {
//                    if (resultSet != null) resultSet.close();
                    if (statemen != null) statemen.close();
                    if (Connn != null) Connn.close();
                    Connect.CloseFBConnect(processName);
                } catch (SQLException e) {
                    System.out.println("Ошибка подключения к БД 3" + e.getMessage());
                    MyLogger.get().logMessage(processName, "Ошибка подключения к БД 3" + e.getMessage());
                    logger.error(processName + " Update - Ошибка подключения к БД 3" + e.getMessage());
                    throw new FlowException(processName + " Ошибка подключения к БД 3" + e.getMessage());
                }
            }



        } catch (SQLException e) {
            System.out.println("Ошибка подключения к БД " + e.getMessage());
            MyLogger.get().logMessage(processName, "Ошибка подключения к БД " + e.getMessage());
            logger.error(processName + " - Ошибка подключения к БД " + e.getMessage());
            throw new FlowException(processName + " Ошибка подключения к БД ");

        }

    }



}
