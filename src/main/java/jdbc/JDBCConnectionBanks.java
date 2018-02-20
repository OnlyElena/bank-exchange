
package jdbc;

import exceptions.FlowException;
import org.apache.log4j.Logger;
import service.MyLoggerBanks;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * @author: OnlyElena@mail.ru
 * Date: 6/18/16
 * Time: 11:36 PM
 */
public class JDBCConnectionBanks {
    final static Logger logger = Logger.getLogger(JDBCConnectionBanks.class);
    private static JDBCConnectionBanks ourInstanceBanks = new JDBCConnectionBanks();

    public static JDBCConnectionBanks getInstance() {
        return ourInstanceBanks;
    }

    private JDBCConnectionBanks() {
    }

    private static Connection ConnBanks;
    private static Statement statementBanks;
    private static ResultSet resultSetBanks;


public static ResultSet jdbcConnection(List<BeanConnectBanks> dataSourceBanks, String processNameBanks, String queryBanks) throws FlowException {
//        List<ExPro> dataSource;

        try {
//            System.out.println(dataSource.get(0).getUrl());
            ConnectBanks ConnectBanks = new ConnectBanks();
            ConnBanks = ConnectBanks.ConnectBanks(dataSourceBanks, processNameBanks);
            if (ConnBanks == null) {
                try {
                    //делаем паузу, ждем восстановление связи
                    MyLoggerBanks.get().logMessage(processNameBanks, "ждем восстановление связи " + dataSourceBanks.get(0).getBeanId());
                    logger.info("ждем восстановление связи " + dataSourceBanks.get(0).getBeanId());
                    Thread.sleep(60000);
                } catch (InterruptedException e) {
                    MyLoggerBanks.get().logMessage(processNameBanks, "Ошибка подключения к БД (sleep)" + e.getMessage());
                    logger.error(e);
                    e.printStackTrace();
                }
                ConnBanks = ConnectBanks.ConnectBanks(dataSourceBanks, processNameBanks);
                if (ConnBanks == null) {
                    try {
                        //делаем паузу, ждем восстановление связи
                        MyLoggerBanks.get().logMessage(processNameBanks, "ждем восстановление связи - вторая попытка");
                        logger.info("ждем восстановление связи - вторая попытка");
                        Thread.sleep(60000);
                    } catch (InterruptedException e) {
                        MyLoggerBanks.get().logMessage(processNameBanks, "Ошибка подключения к БД (sleep)" + e.getMessage());
                        logger.error(e);
                        e.printStackTrace();
                    }
                    ConnBanks = ConnectBanks.ConnectBanks(dataSourceBanks, processNameBanks);
                    if (ConnBanks == null) {
                        try {
                            //делаем паузу, ждем восстановление связи
                            MyLoggerBanks.get().logMessage(processNameBanks, "ждем восстановление связи - вторая попытка");
                            logger.info("ждем восстановление связи - вторая попытка");
                            Thread.sleep(60000);
                        } catch (InterruptedException e) {
                            MyLoggerBanks.get().logMessage(processNameBanks, "Ошибка подключения к БД (sleep)" + e.getMessage());
                            logger.error(e);
                            e.printStackTrace();
                        }
                        ConnBanks = ConnectBanks.ConnectBanks(dataSourceBanks, processNameBanks);
                        if (ConnBanks == null) {
                            MyLoggerBanks.get().logMessage(processNameBanks, "Ошибка подключения к БД ");
                            logger.error(processNameBanks + " - Ошибка подключения к БД ");
                            throw new FlowException(processNameBanks + " Ошибка подключения к БД ");

                        }

                    }
                }
            }

        } catch (SQLException e) {
            System.out.println("Ошибка подключения к БД " + e.getMessage());
            MyLoggerBanks.get().logMessage(processNameBanks, "Ошибка подключения к БД " + e.getMessage());
            logger.error(processNameBanks + " - Ошибка подключения к БД " + e.getMessage());
            throw new FlowException(processNameBanks + " Ошибка подключения к БД ");

        }
        if (ConnBanks == null) {
            MyLoggerBanks.get().logMessage(processNameBanks, "Ошибка подключения к БД ");
            logger.error(processNameBanks + " - Ошибка подключения к БД ");
            throw new FlowException(processNameBanks + " Ошибка подключения к БД ");

        }

        try {
            statementBanks = ConnBanks.createStatement();
            resultSetBanks = statementBanks.executeQuery(queryBanks);

            return resultSetBanks;

//            while (resultSet.next()){
//                depCode = resultSetBanks.getObject("DEPARTMENT").toString();
//                System.out.println("\nУспешное подключение к БД: " + resultSetBanks.getObject("DIV_NAME"));
////                MyLoggerBanks.get().logMessage("Request", "\nУспешное подключение к БД: " + map.get("DIV_NAME"));
//            }
        } catch (SQLException e) {
            System.out.println("2 Ошибка подключения к БД " + e.getMessage());
            MyLoggerBanks.get().logMessage(processNameBanks, "2 Ошибка подключения к БД " + e.getMessage());
            logger.error(processNameBanks + " - 2 Ошибка подключения к БД " + e.getMessage());
            jdbcClose(processNameBanks);
//            e.printStackTrace();
//        }finally{
//            try {
//                resultSetBanks.close();
//                statementBanks.close();
//                ConnBanks.close();
//            } catch (SQLException e) {
//                e.printStackTrace();
//            }
        }
        MyLoggerBanks.get().logMessage(processNameBanks, "Ошибка подключения к БД ");
        logger.error(processNameBanks + " - Ошибка подключения к БД ");
        throw new FlowException(processNameBanks + " Ошибка подключения к БД ");

    }


    public static void jdbcClose(String processNameBanks) throws FlowException {
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
                if (resultSetBanks != null) resultSetBanks.close();
                if (statementBanks != null) statementBanks.close();
                if (ConnBanks != null) ConnBanks.close();
                Connect.CloseFBConnect(processNameBanks);
            } catch (SQLException e) {
                MyLoggerBanks.get().logMessage(processNameBanks, "Ошибка подключения к БД " + e.getMessage());
                logger.error(processNameBanks + " Close - Ошибка подключения к БД " + e.getMessage());
                throw new FlowException("Ошибка подключения к БД " + e.getMessage());
            }
        }
//        System.out.println("БД закрыта! ");
    }


    public static void jdbcUpdateBanks(List<BeanConnectBanks> dataSourceBanks, String processNameBanks, String queryBanks) throws FlowException {
        try {
            ConnectBanks ConnecttBanks = new ConnectBanks();
            MyLoggerBanks.get().logMessage(processNameBanks, "Update_start");
            Connection ConnBanksn = ConnecttBanks.ConnectBanks(dataSourceBanks, processNameBanks);
            if (ConnBanksn == null) {
                try {
                    //делаем паузу, ждем восстановление связи
                    MyLoggerBanks.get().logMessage(processNameBanks, "ждем восстановление связи " + dataSourceBanks.get(0).getBeanId());
                    logger.info("ждем восстановление связи " + dataSourceBanks.get(0).getBeanId());
                    Thread.sleep(60000);
                } catch (InterruptedException e) {
                    MyLoggerBanks.get().logMessage(processNameBanks, "Ошибка подключения к БД (sleep)" + e.getMessage());
                    logger.error(e);
                    e.printStackTrace();
                }
                ConnBanksn = ConnecttBanks.ConnectBanks(dataSourceBanks, processNameBanks);
                if (ConnBanksn == null) {
                    try {
                        //делаем паузу, ждем восстановление связи
                        MyLoggerBanks.get().logMessage(processNameBanks, "ждем восстановление связи - вторая попытка");
                        logger.info("ждем восстановление связи - вторая попытка");
                        Thread.sleep(60000);
                    } catch (InterruptedException e) {
                        MyLoggerBanks.get().logMessage(processNameBanks, "Ошибка подключения к БД (sleep)" + e.getMessage());
                        logger.error(e);
                        e.printStackTrace();
                    }
                    ConnBanksn = ConnecttBanks.ConnectBanks(dataSourceBanks, processNameBanks);
                    if (ConnBanksn == null) {
                        try {
                            //делаем паузу, ждем восстановление связи
                            MyLoggerBanks.get().logMessage(processNameBanks, "ждем восстановление связи - вторая попытка");
                            logger.info("ждем восстановление связи - вторая попытка");
                            Thread.sleep(60000);
                        } catch (InterruptedException e) {
                            MyLoggerBanks.get().logMessage(processNameBanks, "Ошибка подключения к БД (sleep)" + e.getMessage());
                            logger.error(e);
                            e.printStackTrace();
                        }
                        ConnBanksn = ConnecttBanks.ConnectBanks(dataSourceBanks, processNameBanks);
                        if (ConnBanksn == null) {
                            MyLoggerBanks.get().logMessage(processNameBanks, "Ошибка подключения к БД ");
                            logger.error(processNameBanks + " - Ошибка подключения к БД ");
                            throw new FlowException(processNameBanks + " Ошибка подключения к БД ");

                        }

                    }
                }
            }


            if (ConnBanksn == null) {
                MyLoggerBanks.get().logMessage(processNameBanks, "Ошибка подключения к БД ");
                logger.error(processNameBanks + " - Ошибка подключения к БД ");
                throw new FlowException(processNameBanks + " Ошибка подключения к БД ");

            }

            System.out.println("Connect установелен!");

            Statement statemen = null;
            try {
                statemen = ConnBanksn.createStatement();
                System.out.println("statement is TRU!");
                if (statemen == null) statemen = ConnBanksn.createStatement();
                System.out.println(queryBanks);
                statemen.executeUpdate(queryBanks);
                System.out.println("Update successful");

                System.out.println("Update");
                MyLoggerBanks.get().logMessage(processNameBanks, "Update_end");
            } catch (SQLException e) {
                System.out.println("Ошибка подключения к БД 2" + e.getMessage());
                MyLoggerBanks.get().logMessage(processNameBanks, "Ошибка подключения к БД 2" + e.getMessage());
                logger.error(processNameBanks + " Update - Ошибка подключения к БД 2" + e.getMessage());
                throw new FlowException(processNameBanks + " Ошибка подключения к БД 2" + e.getMessage());
            } finally {
                try {
//                    if (resultSet != null) resultSetBanks.close();
                    if (statemen != null) statemen.close();
                    if (ConnBanksn != null) ConnBanksn.close();
                    Connect.CloseFBConnect(processNameBanks);
                } catch (SQLException e) {
                    System.out.println("Ошибка подключения к БД 3" + e.getMessage());
                    MyLoggerBanks.get().logMessage(processNameBanks, "Ошибка подключения к БД 3" + e.getMessage());
                    logger.error(processNameBanks + " Update - Ошибка подключения к БД 3" + e.getMessage());
                    throw new FlowException(processNameBanks + " Ошибка подключения к БД 3" + e.getMessage());
                }
            }



        } catch (SQLException e) {
            System.out.println("Ошибка подключения к БД " + e.getMessage());
            MyLoggerBanks.get().logMessage(processNameBanks, "Ошибка подключения к БД " + e.getMessage());
            logger.error(processNameBanks + " - Ошибка подключения к БД " + e.getMessage());
            throw new FlowException(processNameBanks + " Ошибка подключения к БД ");

        }

    }



}




//package jdbc;
//
//import exceptions.FlowException;
//import org.apache.log4j.Logger;
//import service.MyLoggerBanks;
//
//import java.sql.Connection;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.sql.Statement;
//import java.util.List;
//
///**
// * @author: OnlyElena@mail.ru
// * Date: 6/18/16
// * Time: 11:36 PM
// */
//public class JDBCConnectionBanks {
//    final static Logger logger = Logger.getLogger(JDBCConnectionBanks.class);
//    private static JDBCConnectionBanks ourInstanceBanks = new JDBCConnectionBanks();
//
//    public static JDBCConnectionBanks getInstance() {
//        return ourInstanceBanks;
//    }
//
//    private JDBCConnectionBanks() {
//    }
//
//    private static Connection ConnBanks;
//    private static Statement statementBanks;
//    private static ResultSet resultSetBanks;
//
//    public static ResultSet jdbcConnection(List<BeanConnectBanks> dataSourceBanks, String processNameBanks, String queryBanks) throws FlowException {
////        List<ExPro> dataSource;
//
//        try {
//            ConnectBanks connectBanks = new ConnectBanks();
////            System.out.println(dataSource.get(0).getUrl());
//            ConnBanks = connectBanks.ConnectBanks(dataSourceBanks, processNameBanks);
//            if (ConnBanks == null) {
//                try {
//                    //делаем паузу, ждем восстановление связи
//                    MyLoggerBanks.get().logMessage(processNameBanks, "ждем восстановление связи " +dataSourceBanks.get(0).getBeanId());
//                    logger.info("ждем восстановление связи " +dataSourceBanks.get(0).getBeanId());
//                    Thread.sleep(60000);
//                } catch (InterruptedException e) {
//                    MyLoggerBanks.get().logMessage(processNameBanks, "Ошибка подключения к БД (sleep)" + e.getMessage());
//                    logger.error(e);
//                    e.printStackTrace();
//                }
//                ConnBanks = connectBanks.ConnectBanks(dataSourceBanks, processNameBanks);
//                if (ConnBanks == null) {
//                    try {
//                        //делаем паузу, ждем восстановление связи
//                        MyLoggerBanks.get().logMessage(processNameBanks, "ждем восстановление связи - вторая попытка");
//                        logger.info("ждем восстановление связи - вторая попытка");
//                        Thread.sleep(60000);
//                    } catch (InterruptedException e) {
//                        MyLoggerBanks.get().logMessage(processNameBanks, "Ошибка подключения к БД (sleep)" + e.getMessage());
//                        logger.error(e);
//                        e.printStackTrace();
//                    }
//                    ConnBanks = connectBanks.ConnectBanks(dataSourceBanks, processNameBanks);
//                    if (ConnBanks == null) return null;
//                }
//            }
//
//        } catch (SQLException e) {
//            System.out.println("Ошибка подключения к БД " + e.getMessage());
//            MyLoggerBanks.get().logMessage(processNameBanks, "Ошибка подключения к БД " + e.getMessage());
//            logger.error(processNameBanks+" - Ошибка подключения к БД " + e.getMessage());
//            return null;
////            e.printStackTrace();
//        }
//        if (ConnBanks == null) return null;
//
//        try {
//            statementBanks = ConnBanks.createStatement();
//            resultSetBanks = statementBanks.executeQuery(queryBanks);
//
//            return resultSetBanks;
//
////            while (resultSet.next()){
////                depCode = resultSet.getObject("DEPARTMENT").toString();
////                System.out.println("\nУспешное подключение к БД: " + resultSet.getObject("DIV_NAME"));
//////                MyLogger.get().logMessage("Request", "\nУспешное подключение к БД: " + map.get("DIV_NAME"));
////            }
//        } catch (SQLException e) {
//            System.out.println("2 Ошибка подключения к БД " + e.getMessage());
//            MyLoggerBanks.get().logMessage(processNameBanks, "2 Ошибка подключения к БД " + e.getMessage());
//            logger.error(processNameBanks+" - 2 Ошибка подключения к БД " + e.getMessage());
//            jdbcClose(processNameBanks);
////            e.printStackTrace();
////        }finally{
////            try {
////                resultSet.close();
////                statement.close();
////                Conn.close();
////            } catch (SQLException e) {
////                e.printStackTrace();
////            }
//        }
//        return null;
//    }
//
//    public static void jdbcUpdateBanks(List<BeanConnectBanks> dataSourceBanks, String processNameBanks, String queryBanks) throws FlowException {
//        try {
//            if (ConnBanks == null) ConnBanks = ConnectBanks.ConnectBanks(dataSourceBanks, processNameBanks);
//            if (statementBanks == null) statementBanks = ConnBanks.createStatement();
//            statementBanks.executeUpdate(queryBanks);
//
//            System.out.println("Update");
//            MyLoggerBanks.get().logMessage(processNameBanks, "Update");
//        } catch (SQLException e) {
//            System.out.println("Ошибка подключения к БД " + e.getMessage());
//            MyLoggerBanks.get().logMessage(processNameBanks, "Ошибка подключения к БД " + e.getMessage());
//            logger.error(processNameBanks+" Update - Ошибка подключения к БД " + e.getMessage());
//            throw new FlowException(processNameBanks+" Ошибка подключения к БД " + e.getMessage());
////            e.printStackTrace();
//        } finally {
//            try {
//                if (resultSetBanks != null) resultSetBanks.close();
//                if (statementBanks != null) statementBanks.close();
//                if (ConnBanks != null) ConnBanks.close();
//                Connect.CloseFBConnect(processNameBanks);
//            } catch (SQLException e) {
//                System.out.println("Ошибка подключения к БД " + e.getMessage());
//                MyLoggerBanks.get().logMessage(processNameBanks, "Ошибка подключения к БД " + e.getMessage());
//                logger.error(processNameBanks+" Update - Ошибка подключения к БД " + e.getMessage());
//                throw new FlowException(processNameBanks+" Ошибка подключения к БД " + e.getMessage());
////                        e.printStackTrace();
//            }
//        }
//    }
//
//
//    public static void jdbcClose(String processName) throws FlowException {
//        try {
//            System.out.print(" *");
////            System.out.println("Подключение = "+Conn.isClosed()+", закрываем подключение");
////            if (Conn != null) System.out.println();
////            if (resultSet != null) {
////                System.out.println("Производим закрытие подключения");
////            }
////        } catch (SQLException e) {
////            System.out.println("Ошибка подключения к БД " + e.getMessage());
////            throw new FlowException("Ошибка подключения к БД " + e.getMessage());
//////                e.printStackTrace();
//        } finally {
//            try {
//                if (resultSetBanks != null) resultSetBanks.close();
//                if (statementBanks != null) statementBanks.close();
//                if (ConnBanks != null) ConnBanks.close();
//                Connect.CloseFBConnect(processName);
//            } catch (SQLException e) {
//                MyLoggerBanks.get().logMessage(processName, "Ошибка подключения к БД " + e.getMessage());
//                logger.error(processName+" Close - Ошибка подключения к БД " + e.getMessage());
//                throw new FlowException("Ошибка подключения к БД " + e.getMessage());
////                e.printStackTrace();
//            }
//        }
////        System.out.println("БД закрыта! ");
//    }
//
//}
