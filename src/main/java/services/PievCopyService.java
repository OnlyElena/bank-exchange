package services;

import exceptions.FlowException;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;
import jdbc.BeanConnect;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author: OnlyElena@mail.ru
 * Date: 2/26/15
 * Time: 9:33 PM
 */
public class PievCopyService {
    final static Logger logger = Logger.getLogger(PievCopyService.class);

    public static void main(String[] args) throws IOException {
        String strprog = "STRING CREATED| "; //debug log string
        try {
            strprog += "NTLM| ";
//            NtlmPasswordAuthentication auth =  new NtlmPasswordAuthentication("FSSP38","ChybakovaEL","#$rfVgyH78");
            NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication("FSSP38;ChybakovaEL:#$rfVgyH78");

//            NtlmPasswordAuthentication auth =  new NtlmPasswordAuthentication("FSSP38");

            strprog += "SMB| ";
            SmbFile file = new SmbFile("smb://10.38.63.130/d$/fssp/rezervcopy.txt", auth);

//            smb://Рабочая_группа; Логин: Пароль@Ip/Шара
//            smb://FSSP38; ChybakovaEL: #$rfVgyH78@10.38.63.130/d$/fssp/
            smb:
//
//            \\10.38.63.130\d$\fssp

            strprog += "EXIST| ";
            String there = String.valueOf(file.exists());
            System.out.println("54564");
            System.out.println("1111 " + there);
//            strprog += "View| ";
//            TextView pp;
//            pp = (TextView) findViewById(R.id.tv);
//            pp.setText(there);


        } catch (Exception e) {
            // TODO Auto-generated catch block
            strprog += "ERROR! ";
//            TextView ll;
//            ll = (TextView) findViewById(R.id.tv);


//            ll.setText(strprog + e.getStackTrace().toString() + "    " +  e.getMessage() + "   " + e.getLocalizedMessage() );
        }
    }


    public static boolean PievCopyService(String processName, List<BeanConnect> dataSource, LinkedHashMap<String, String> properties, String urlPIEV, String Authentication, String outDir, String authOutDir, String fileCopy) {
        SmbFile smbFileInput = null;
        SmbFile smbFileOutput = null;
        SmbFileInputStream sms = null;
        String strprog = "STRING CREATED| "; //debug log string

//        System.out.println("PARAMETR   "+jcifs.Config.getProperty("jcifs.encoding"));
//        System.out.println("PARAMETR2   "+jcifs.Config.getProperty("jcifs.netbios.hostname"));
//        jcifs.Config.setProperty("jcifs.encoding", "Cp866");
//        jcifs.Config.setProperty("jcifs.netbios.lport", "137");
//        System.setProperty("jcifs.smb.client.responseTimeout", "12000009"); // default: 30000 millisec.
//        System.setProperty("jcifs.smb.client.soTimeout", "14000009"); // default: 35000
//        System.setProperty("jcifs.netbios.soTimeout", "14000009"); // default: 35000
//        System.setProperty("jcifs.netbios.retryTimeout", "14000009"); // default: 35000


//        System.out.println("PARAMETR   "+jcifs.Config.getProperty("jcifs.encoding"));


        try {
//            strprog += "NTLM| ";
            NtlmPasswordAuthentication authInput = new NtlmPasswordAuthentication(Authentication);
            NtlmPasswordAuthentication authOutput = new NtlmPasswordAuthentication(authOutDir);

//            System.out.println(authOutDir);
//            strprog += "SMB| ";
//            SmbFile file = new SmbFile("smb://10.38.63.130/d$/fssp/rezervcopy.txt", authInput);

//            smb://Рабочая_группа; Логин: Пароль@Ip/Шара
//            smb://FSSP38; ChybakovaEL: #$rfVgyH78@10.38.63.130/d$/fssp/
//            \\10.38.63.130\d$\fssp


//            System.setProperty("jcifs.encoding", "Cp866"); // default: 30000 millisec.

//            System.setProperty("jcifs.smb.client.responseTimeout", "1200000"); // default: 30000 millisec.
//            System.setProperty("jcifs.smb.client.soTimeout", "1400000"); // default: 35000
//            try{
//            System.out.println(System.getProperty("jcifs.smb.client.responseTimeout"));
//            System.out.println(System.getProperty("jcifs.smb.client.soTimeout"));
//
//            }catch (Exception e){
//                System.err.println("Err: " + e.getMessage());
//                e.printStackTrace();
//            }
            try {
//                sms = new SmbFileInputStream("smb://oit1053:oit1053"+"@" +urlPIEV+fileCopy);
//                System.out.println(sms.read());
                smbFileInput = new SmbFile("smb://" + urlPIEV + fileCopy, authInput);
//                System.out.println("smbFileInput.canRead()"+smbFileInput.canRead());
//                System.out.println("smbFileInput "+smbFileInput);
            } catch (MalformedURLException ee) {
                System.out.println("не подключился для копирования Input ");
                System.err.println(new Date() + " " + ee.getMessage());
                MyLogger.get().logMessage(processName, "не подключился для копирования Input : " + ee.getMessage());
                logger.error(processName+" - не подключился для копирования Input: " + ee.getMessage());
//                ee.printStackTrace();
                throw new FlowException(processName+" - не подключился для копирования Input: " + ee.getMessage());

            }
//            catch (jcifs.smb.SmbAuthException sae) {
//                System.err.println("Ошибка аутентификации: Input " + sae.getMessage());
//                sae.printStackTrace();
//            } catch (jcifs.smb.SmbException se) {
//                System.err.println("Ошибка доступа: Input " + se.getMessage());
//                se.printStackTrace();
//            } catch (Exception e) {
//                System.err.println("Ошибка: Input " + e.getMessage());
//                e.printStackTrace();
//            }


            //подключить файл получатель
            try {
                smbFileOutput = new SmbFile("smb://" + outDir + fileCopy, authOutput);
            } catch (java.net.MalformedURLException e) {
                System.err.println(e.getMessage());
                MyLogger.get().logMessage(processName, "Err2: " + e.getMessage());
                logger.error(processName +" PievCopyService - Err2: " + e.getMessage());
//                e.printStackTrace();
                throw new FlowException(processName+" PievCopyService - Err2: " + e.getMessage());
            }
//            try{
//                System.out.println("smb://"+outDir+fileCopy);
//            }catch (Exception e){
//                System.err.println("Err2: " + e.getMessage());
//                MyLogger.get().logMessage("PievCopyService", "Err2: " + e.getMessage());
//                logger.error("PievCopyService - Err2: " + e.getMessage());
//                e.printStackTrace();
//            }
            //скопировать файл
            System.out.print(fileCopy + " - ");
            try {
                if (smbFileInput.exists()) {
                    smbFileInput.copyTo(smbFileOutput);
                    System.out.print("файл скопирован - ");
                    MyLogger.get().logMessage(processName, "файл скопирован");

                } else {
                    ImportNotifNotPievService importNotifNotPievService = new ImportNotifNotPievService(dataSource, properties, processName);
                    boolean b = false;
                    try {
                        String statusNotif = "Постановление не направлено в банк в связи с отсутствием легитимной копии (в указанном каталоге отсутствует piev документ)";
                        b = importNotifNotPievService.processResponse(fileCopy, statusNotif);
                    } catch (FlowException e) {
                        System.err.println(new Date() + " " + e.getMessage());
                        MyLogger.get().logMessage(processName, new Date() + " " + e.getMessage());
                        logger.error(new Date() + " " + e.getMessage());
                    }
                    if(b == true){
                        System.out.println("Сделана запись уведомления об отсутствии легитимной копии постановления: " + fileCopy);
                        MyLogger.get().logMessage(processName, "Сделана запись уведомления об отсутствии легитимной копии постановления: " + fileCopy);
                        logger.error("PievCopyService - Сделана запись уведомления об отсутствии легитимной копии постановления: " + fileCopy);
                    } else {
                        MyLogger.get().logMessage(processName, "PievCopyService - не удалось произвести запись об отсутствии легитимной копии постановления: " + fileCopy);
                        logger.error("PievCopyService - не удалось произвести запись об отсутствии легитимной копии постановления: " + fileCopy);
                        throw new FlowException("PievCopyService - не удалось произвести запись об отсутствии легитимной копии постановления");
                    }

                    System.out.println("# файл не существует");
                    MyLogger.get().logMessage(processName, "# файл не существует");
                    return false;
                }
                System.out.println("11111111111111111111111111");
            } catch (jcifs.smb.SmbAuthException sae) {
                System.err.println("Ошибка аутентификации: " + sae.getMessage());
                MyLogger.get().logMessage(processName, "Ошибка аутентификации: " + sae.getMessage());
                logger.error("PievCopyService - Ошибка аутентификации: " + sae.getMessage());
//                sae.printStackTrace();
                throw new FlowException("PievCopyService - Ошибка аутентификации: " + sae.getMessage());
//                return false;
            } catch (jcifs.smb.SmbException se) {
                logger.info("Пауза - вторая попытка скопировать");
                MyLogger.get().logMessage(processName, "Пауза - вторая попытка скопировать");
                try {
                    Thread.sleep(60000);
                } catch (InterruptedException e) {
                    logger.error(e);
                    MyLogger.get().logMessage(processName, e.toString());
//                            e.printStackTrace();
                }
                logger.info("вторая попытка скопировать");
                MyLogger.get().logMessage(processName, "вторая попытка скопировать");
//                -------вторая попытка скопировать-------

                try {
                    if (smbFileInput.exists()) {
                        smbFileInput.copyTo(smbFileOutput);
                        System.out.print("файл скопирован - ");
                        MyLogger.get().logMessage(processName, "файл скопирован");
                    } else {
                        ImportNotifNotPievService importNotifNotPievService = new ImportNotifNotPievService(dataSource, properties, processName);
                        boolean b = false;
                        try {
                            String statusNotif = "Постановление не направлено в банк в связи с отсутствием легитимной копии (в указанном каталоге отсутствует piev документ)";
                            b = importNotifNotPievService.processResponse(fileCopy, fileCopy);
                        } catch (FlowException e) {
                            System.err.println(new Date() + " " + e.getMessage());
                            MyLogger.get().logMessage(processName, new Date() + " " + e.getMessage());
                            logger.error(new Date() + " " + e.getMessage());
                        }
                        if(b == true){
                            System.out.println("Сделана запись уведомления об отсутствии легитимной копии постановления: " + fileCopy);
                            MyLogger.get().logMessage(processName, "Сделана запись уведомления об отсутствии легитимной копии постановления: " + fileCopy);
                            logger.error("PievCopyService - Сделана запись уведомления об отсутствии легитимной копии постановления: " + fileCopy);
                        } else {
                            MyLogger.get().logMessage(processName, "PievCopyService - не удалось произвести запись об отсутствии легитимной копии постановления: " + fileCopy);
                            logger.error("PievCopyService - не удалось произвести запись об отсутствии легитимной копии постановления: " + fileCopy);
                            throw new FlowException("PievCopyService - не удалось произвести запись об отсутствии легитимной копии постановления");
                        }

                        System.out.println("# файл не существует");
                        MyLogger.get().logMessage(processName, "# файл не существует");
                        return false;
                    }

                    System.out.println("2222222222222222222");

                } catch (jcifs.smb.SmbAuthException sae) {
                    System.err.println("Ошибка аутентификации: " + sae.getMessage());
                    MyLogger.get().logMessage(processName, "Ошибка аутентификации: " + sae.getMessage());
                    logger.error("PievCopyService - Ошибка аутентификации: " + sae.getMessage());
//                sae.printStackTrace();
                    throw new FlowException("PievCopyService - Ошибка аутентификации: " + sae.getMessage());
//                return false;
                } catch (jcifs.smb.SmbException sse) {
                    logger.info("третья попытка скопировать");
                    MyLogger.get().logMessage(processName, "третья попытка скопировать");
                    try {
                        Thread.sleep(60000);
                    } catch (InterruptedException e) {
                        logger.error(e);
                        MyLogger.get().logMessage(processName, e.toString());
//                                e.printStackTrace();
                    }

//                    -------третья попытка скопировать-------

                    try {
                        if (smbFileInput.exists()) {
                            smbFileInput.copyTo(smbFileOutput);
                            System.out.print("файл скопирован - ");
                            MyLogger.get().logMessage(processName, "файл скопирован");
                        } else {
                            ImportNotifNotPievService importNotifNotPievService = new ImportNotifNotPievService(dataSource, properties, processName);
                            boolean b = false;
                            try {
                                String statusNotif = "Постановление не направлено в банк в связи с отсутствием легитимной копии (в указанном каталоге отсутствует piev документ)";
                                b = importNotifNotPievService.processResponse(fileCopy,statusNotif);
                            } catch (FlowException e) {
                                System.err.println(new Date() + " " + e.getMessage());
                                MyLogger.get().logMessage(processName, new Date() + " " + e.getMessage());
                                logger.error(new Date() + " " + e.getMessage());
                            }
                            if(b == true){
                                System.out.println("Сделана запись уведомления об отсутствии легитимной копии постановления: " + fileCopy);
                                MyLogger.get().logMessage(processName, "Сделана запись уведомления об отсутствии легитимной копии постановления: " + fileCopy);
                                logger.error("PievCopyService - Сделана запись уведомления об отсутствии легитимной копии постановления: " + fileCopy);
                            }  else {
                                MyLogger.get().logMessage(processName, "PievCopyService - не удалось произвести запись об отсутствии легитимной копии постановления: " + fileCopy);
                                logger.error("PievCopyService - не удалось произвести запись об отсутствии легитимной копии постановления: " + fileCopy);
                                throw new FlowException("PievCopyService - не удалось произвести запись об отсутствии легитимной копии постановления");
                            }

                            System.out.println("# файл не существует");
                            MyLogger.get().logMessage(processName, "# файл не существует");
                            return false;
                        }
                        System.out.println("333333333333333333333");

                    } catch (jcifs.smb.SmbAuthException sae) {
                        System.err.println("Ошибка аутентификации: " + sae.getMessage());
                        MyLogger.get().logMessage(processName, "Ошибка аутентификации: " + sae.getMessage());
                        logger.error("PievCopyService - Ошибка аутентификации: " + sae.getMessage());
//                sae.printStackTrace();
                        throw new FlowException("PievCopyService - Ошибка аутентификации: " + sae.getMessage());
//                return false;
                    } catch (jcifs.smb.SmbException ssse) {
                        System.err.println("Ошибка доступа: " + ssse.getMessage());
                        MyLogger.get().logMessage(processName, "Ошибка доступа: " + ssse.getMessage());
                        logger.error("PievCopyService - Ошибка доступа: " + ssse.getMessage());
                        throw new FlowException("PievCopyService - Ошибка доступа: " + ssse.getMessage());
//                return false;
                    } catch (Exception e) {
                        System.err.println("Ошибка: " + e.getMessage());
                        MyLogger.get().logMessage(processName, "Ошибка: " + e.getMessage());
                        logger.error("PievCopyService - Ошибка: " + e.getMessage());
                        throw new FlowException("PievCopyService - Ошибка: " + e.getMessage());
//                e.printStackTrace();
//                return false;
                    }


//                    -------третья попытка скопировать-------

                    System.err.println("Ошибка доступа: " + sse.getMessage());
                    MyLogger.get().logMessage(processName, "Ошибка доступа: " + sse.getMessage());
                    logger.error("PievCopyService - Ошибка доступа: " + sse.getMessage());
                    throw new FlowException("PievCopyService - Ошибка доступа: " + sse.getMessage());
//                return false;
                } catch (Exception e) {
                    System.err.println("Ошибка: " + e.getMessage());
                    MyLogger.get().logMessage(processName, "Ошибка: " + e.getMessage());
                    logger.error("PievCopyService - Ошибка: " + e.getMessage());
                    throw new FlowException("PievCopyService - Ошибка: " + e.getMessage());
//                e.printStackTrace();
//                return false;
                }


//                -------вторая попытка скопировать-------


                System.err.println("Ошибка доступа: " + se.getMessage());
                MyLogger.get().logMessage(processName, "Ошибка доступа: " + se.getMessage());
                logger.error("PievCopyService - Ошибка доступа: " + se.getMessage());
                throw new FlowException("PievCopyService - Ошибка доступа: " + se.getMessage());
//                return false;
            } catch (Exception e) {
                System.err.println("Ошибка: " + e.getMessage());
                MyLogger.get().logMessage(processName, "Ошибка: " + e.getMessage());
                logger.error("PievCopyService - Ошибка: " + e.getMessage());
                throw new FlowException("PievCopyService - Ошибка: " + e.getMessage());
//                e.printStackTrace();
//                return false;
            }

            System.out.print(" --- ");

            return true;

        } catch (Exception e) {
            System.err.println("Err: " + e.getMessage());
            MyLogger.get().logMessage(processName, "Err: " + e.getMessage());
            logger.error("PievCopyService - Err: " + e.getMessage());
//            throw new FlowException("PievCopyService - Err: " + e.getMessage());
//            e.printStackTrace();
            return false;

        }
    }
}


//            smb://Рабочая_группа; Логин: Пароль@Ip/Шара
//            smb://FSSP38; ChybakovaEL: #$rfVgyH78@10.38.63.130/d$/fssp/
//            smb://38040-serv-db.fssp38.local;oit1053:oit1053@10.38.111.130/regmvv/piev_25401022473849.xml.zip
//            smb://fssp38.local;oit1053;oit1053@10.38.111.130/regmvv/