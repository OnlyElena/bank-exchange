package service;

import exceptions.FlowException;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;
import jdbc.BeanConnectBanks;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author: OnlyElena@mail.ru
 * Date: 6/20/16
 * Time: 5:25 PM
 */
public class PievCopyServiceBanks {
    final static Logger logger = Logger.getLogger(PievCopyServiceBanks.class);

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


    public static boolean PievCopyServiceBanks(String processNameBanks, List<BeanConnectBanks> dataSourceBanks, LinkedHashMap<String, String> propertiesBanks, String urlPIEVBanks, String AuthenticationBanks, String outDirBanks, String authOutDirBanks, String fileCopyBanks) throws FlowException {
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
            NtlmPasswordAuthentication authInput = new NtlmPasswordAuthentication(AuthenticationBanks);
            NtlmPasswordAuthentication authOutput = new NtlmPasswordAuthentication(authOutDirBanks);

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
                smbFileInput = new SmbFile("smb://" + urlPIEVBanks + fileCopyBanks, authInput);
                System.out.println("smbFileInput.canRead()"+smbFileInput.canRead());
                System.out.println("smbFileInput "+smbFileInput);
            } catch (MalformedURLException ee) {
                System.out.println("не подключился для копирования Input ");
                System.err.println(new Date() + " " + ee.getMessage());
                MyLoggerBanks.get().logMessage(processNameBanks, "не подключился для копирования Input : " + ee.getMessage());
                logger.error(processNameBanks+" - не подключился для копирования Input: " + ee.getMessage());
//                ee.printStackTrace();
                throw new FlowException(processNameBanks+" - не подключился для копирования Input: " + ee.getMessage());

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
                smbFileOutput = new SmbFile("smb://" + outDirBanks + fileCopyBanks, authOutput);
                System.out.println("smb://" + outDirBanks + fileCopyBanks + "---"+ authOutput);
            } catch (java.net.MalformedURLException e) {
                System.err.println(e.getMessage());
                MyLoggerBanks.get().logMessage(processNameBanks, "Err2: " + e.getMessage());
                logger.error(processNameBanks +" PievCopyService - Err2: " + e.getMessage());
//                e.printStackTrace();
                throw new FlowException(processNameBanks+" PievCopyService - Err2: " + e.getMessage());
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
            System.out.print(fileCopyBanks + " - ");
            try {
                if (smbFileInput.exists()) {
                    smbFileInput.copyTo(smbFileOutput);
                    System.out.print("файл скопирован - ");
                    MyLoggerBanks.get().logMessage(processNameBanks, "файл скопирован");

                } else {
                    ImportNotifNotPievServiceBanks importNotifNotPievServiceBanks = new ImportNotifNotPievServiceBanks(dataSourceBanks, propertiesBanks, processNameBanks);
                    boolean b = false;
                    try {
                        b = importNotifNotPievServiceBanks.processResponseBanks(fileCopyBanks);
                    } catch (FlowException e) {
                        System.err.println(new Date() + " " + e.getMessage());
                        MyLoggerBanks.get().logMessage(processNameBanks, new Date() + " " + e.getMessage());
                        logger.error(new Date() + " " + e.getMessage());
                    }
                    if(b == true){
                        System.out.println("Сделана запись уведомления об отсутствии легитимной копии постановления: " + fileCopyBanks);
                        MyLoggerBanks.get().logMessage(processNameBanks, "Сделана запись уведомления об отсутствии легитимной копии постановления: " + fileCopyBanks);
                        logger.error("PievCopyService - Сделана запись уведомления об отсутствии легитимной копии постановления: " + fileCopyBanks);
                    } else {
                        MyLoggerBanks.get().logMessage(processNameBanks, "PievCopyService - не удалось произвести запись об отсутствии легитимной копии постановления: " + fileCopyBanks);
                        logger.error("PievCopyService - не удалось произвести запись об отсутствии легитимной копии постановления: " + fileCopyBanks);
                        throw new FlowException("PievCopyService - не удалось произвести запись об отсутствии легитимной копии постановления");
                    }

                    System.out.println("# файл не существует");
                    MyLoggerBanks.get().logMessage(processNameBanks, "# файл не существует");
                    return false;
                }
            } catch (jcifs.smb.SmbAuthException sae) {
                System.err.println("Ошибка аутентификации: " + sae.getMessage());
                MyLoggerBanks.get().logMessage(processNameBanks, "Ошибка аутентификации: " + sae.getMessage());
                logger.error("PievCopyService - Ошибка аутентификации: " + sae.getMessage());
//                sae.printStackTrace();
                throw new FlowException("PievCopyService - Ошибка аутентификации: " + sae.getMessage());
//                return false;
            } catch (jcifs.smb.SmbException se) {
                logger.info("Пауза - вторая попытка скопировать");
                MyLoggerBanks.get().logMessage(processNameBanks, "Пауза - вторая попытка скопировать");
                try {
                    Thread.sleep(60000);
                } catch (InterruptedException e) {
                    logger.error(e);
                    MyLoggerBanks.get().logMessage(processNameBanks, e.toString());
//                            e.printStackTrace();
                }
                logger.info("вторая попытка скопировать");
                MyLoggerBanks.get().logMessage(processNameBanks, "вторая попытка скопировать");
//                -------вторая попытка скопировать-------

                try {
                    if (smbFileInput.exists()) {
                        smbFileInput.copyTo(smbFileOutput);
                        System.out.print("файл скопирован - ");
                        MyLoggerBanks.get().logMessage(processNameBanks, "файл скопирован");
                    } else {
                        ImportNotifNotPievServiceBanks importNotifNotPievServiceBanks = new ImportNotifNotPievServiceBanks(dataSourceBanks, propertiesBanks, processNameBanks);
                        boolean b = false;
                        try {
                            b = importNotifNotPievServiceBanks.processResponseBanks(fileCopyBanks);
                        } catch (FlowException e) {
                            System.err.println(new Date() + " " + e.getMessage());
                            MyLoggerBanks.get().logMessage(processNameBanks, new Date() + " " + e.getMessage());
                            logger.error(new Date() + " " + e.getMessage());
                        }
                        if(b == true){
                            System.out.println("Сделана запись уведомления об отсутствии легитимной копии постановления: " + fileCopyBanks);
                            MyLoggerBanks.get().logMessage(processNameBanks, "Сделана запись уведомления об отсутствии легитимной копии постановления: " + fileCopyBanks);
                            logger.error("PievCopyService - Сделана запись уведомления об отсутствии легитимной копии постановления: " + fileCopyBanks);
                        } else {
                            MyLoggerBanks.get().logMessage(processNameBanks, "PievCopyService - не удалось произвести запись об отсутствии легитимной копии постановления: " + fileCopyBanks);
                            logger.error("PievCopyService - не удалось произвести запись об отсутствии легитимной копии постановления: " + fileCopyBanks);
                            throw new FlowException("PievCopyService - не удалось произвести запись об отсутствии легитимной копии постановления");
                        }

                        System.out.println("# файл не существует");
                        MyLoggerBanks.get().logMessage(processNameBanks, "# файл не существует");
                        return false;
                    }
                } catch (jcifs.smb.SmbAuthException sae) {
                    System.err.println("Ошибка аутентификации: " + sae.getMessage());
                    MyLoggerBanks.get().logMessage(processNameBanks, "Ошибка аутентификации: " + sae.getMessage());
                    logger.error("PievCopyService - Ошибка аутентификации: " + sae.getMessage());
//                sae.printStackTrace();
                    throw new FlowException("PievCopyService - Ошибка аутентификации: " + sae.getMessage());
//                return false;
                } catch (jcifs.smb.SmbException sse) {
                    logger.info("третья попытка скопировать");
                    MyLoggerBanks.get().logMessage(processNameBanks, "третья попытка скопировать");
                    try {
                        Thread.sleep(60000);
                    } catch (InterruptedException e) {
                        logger.error(e);
                        MyLoggerBanks.get().logMessage(processNameBanks, e.toString());
//                                e.printStackTrace();
                    }

//                    -------третья попытка скопировать-------

                    try {
                        if (smbFileInput.exists()) {
                            smbFileInput.copyTo(smbFileOutput);
                            System.out.print("файл скопирован - ");
                            MyLoggerBanks.get().logMessage(processNameBanks, "файл скопирован");
                        } else {
                            ImportNotifNotPievServiceBanks importNotifNotPievServiceBanks = new ImportNotifNotPievServiceBanks(dataSourceBanks, propertiesBanks, processNameBanks);
                            boolean b = false;
                            try {
                                b = importNotifNotPievServiceBanks.processResponseBanks(fileCopyBanks);
                            } catch (FlowException e) {
                                System.err.println(new Date() + " " + e.getMessage());
                                MyLoggerBanks.get().logMessage(processNameBanks, new Date() + " " + e.getMessage());
                                logger.error(new Date() + " " + e.getMessage());
                            }
                            if(b == true){
                                System.out.println("Сделана запись уведомления об отсутствии легитимной копии постановления: " + fileCopyBanks);
                                MyLoggerBanks.get().logMessage(processNameBanks, "Сделана запись уведомления об отсутствии легитимной копии постановления: " + fileCopyBanks);
                                logger.error("PievCopyService - Сделана запись уведомления об отсутствии легитимной копии постановления: " + fileCopyBanks);
                            }  else {
                                MyLoggerBanks.get().logMessage(processNameBanks, "PievCopyService - не удалось произвести запись об отсутствии легитимной копии постановления: " + fileCopyBanks);
                                logger.error("PievCopyService - не удалось произвести запись об отсутствии легитимной копии постановления: " + fileCopyBanks);
                                throw new FlowException("PievCopyService - не удалось произвести запись об отсутствии легитимной копии постановления");
                            }

                            System.out.println("# файл не существует");
                            MyLoggerBanks.get().logMessage(processNameBanks, "# файл не существует");
                            return false;
                        }
                    } catch (jcifs.smb.SmbAuthException sae) {
                        System.err.println("Ошибка аутентификации: " + sae.getMessage());
                        MyLoggerBanks.get().logMessage(processNameBanks, "Ошибка аутентификации: " + sae.getMessage());
                        logger.error("PievCopyService - Ошибка аутентификации: " + sae.getMessage());
//                sae.printStackTrace();
                        throw new FlowException("PievCopyService - Ошибка аутентификации: " + sae.getMessage());
//                return false;
                    } catch (jcifs.smb.SmbException ssse) {

//                        try {
//                            Thread.sleep(1000);
//                        } catch (InterruptedException e) {
//                            logger.error(e);
//                            e.printStackTrace();
//                        }


                        System.err.println("Ошибка доступа: " + ssse.getMessage());
                        MyLoggerBanks.get().logMessage(processNameBanks, "Ошибка доступа: " + ssse.getMessage());
                        logger.error("PievCopyService - Ошибка доступа: " + ssse.getMessage());
                        throw new FlowException("PievCopyService - Ошибка доступа: " + ssse.getMessage());
//                return false;
                    } catch (Exception e) {
                        System.err.println("Ошибка: " + e.getMessage());
                        MyLoggerBanks.get().logMessage(processNameBanks, "Ошибка: " + e.getMessage());
                        logger.error("PievCopyService - Ошибка: " + e.getMessage());
                        throw new FlowException("PievCopyService - Ошибка: " + e.getMessage());
//                e.printStackTrace();
//                return false;
                    }


//                    -------третья попытка скопировать-------

                    System.err.println("Ошибка доступа: " + sse.getMessage());
                    MyLoggerBanks.get().logMessage(processNameBanks, "Ошибка доступа: " + sse.getMessage());
                    logger.error("PievCopyService - Ошибка доступа: " + sse.getMessage());
//                    throw new FlowException("PievCopyService - Ошибка доступа: " + sse.getMessage());
//                return false;
                } catch (Exception e) {
                    System.err.println("Ошибка: " + e.getMessage());
                    MyLoggerBanks.get().logMessage(processNameBanks, "Ошибка: " + e.getMessage());
                    logger.error("PievCopyService - Ошибка: " + e.getMessage());
                    throw new FlowException("PievCopyService - Ошибка: " + e.getMessage());
//                e.printStackTrace();
//                return false;
                }


//                -------вторая попытка скопировать-------


                System.err.println("Ошибка доступа: " + se.getMessage());
                MyLoggerBanks.get().logMessage(processNameBanks, "Ошибка доступа: " + se.getMessage());
                logger.error("PievCopyService - Ошибка доступа: " + se.getMessage());
//                throw new FlowException("PievCopyService - Ошибка доступа: " + se.getMessage());
//                return false;
            } catch (Exception e) {
                System.err.println("Ошибка: " + e.getMessage());
                MyLoggerBanks.get().logMessage(processNameBanks, "Ошибка: " + e.getMessage());
                logger.error("PievCopyService - Ошибка: " + e.getMessage());
                throw new FlowException("PievCopyService - Ошибка: " + e.getMessage());
//                e.printStackTrace();
//                return false;
            }

            return true;

        } catch (Exception e) {
            System.err.println("Err: " + e.getMessage());
            MyLoggerBanks.get().logMessage(processNameBanks, "Err: " + e.getMessage());
            logger.error("PievCopyService - Err: " + e.getMessage());
            throw new FlowException("PievCopyService - Err: " + e.getMessage());
//            e.printStackTrace();
//            return false;

        }
    }
}


//            smb://Рабочая_группа; Логин: Пароль@Ip/Шара
//            smb://FSSP38; ChybakovaEL: #$rfVgyH78@10.38.63.130/d$/fssp/
//            smb://38040-serv-db.fssp38.local;oit1053:oit1053@10.38.111.130/regmvv/piev_25401022473849.xml.zip
//            smb://fssp38.local;oit1053;oit1053@10.38.111.130/regmvv/