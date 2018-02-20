import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;
import services.Config;

import java.io.*;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.LinkedHashMap;

//import org.junit.Test;
/**
 * @author: OnlyElena@mail.ru
 * Date: 5/23/16
 * Time: 1:26 PM
 */
public class TestCopyPiev {
    //    @Test
    public static void main(String[] args) throws IOException {
        TestCopyPiev();
    }
    public static void  TestCopyPiev() throws IOException {
        String DIR_IN = null, AUTH_IN = null, file = null;

        try {

            BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream("exProd.xml"), Charset.forName("UTF-8")));
            String line = null;
            boolean met = false;
            while ((line = in.readLine()) != null) {
                int separator = line.indexOf("=");
                if (line.contains("<!--tetst")) {
                    System.out.println(line);
                    AUTH_IN = line.substring(separator + 1, line.length()-3);
                    DIR_IN = line.substring(line.indexOf("<!--")+9, separator);
                }
                if (line.contains("<!--tesFileCopy")){
                    file = line.substring(separator + 1, line.length()-3);
                }
            }
        } catch (FileNotFoundException e) {
            System.err.println(new Date() + " File not found: " + new File("exProd").getAbsolutePath());
        } catch (IOException e) {
            System.err.println(new Date() + " " + e.getMessage());
        }
        System.out.println("AUTH_IN =" + AUTH_IN);
        System.out.println("DIR_IN =" + DIR_IN);
        System.out.println("file =" + file);
        //параметры Управления
        LinkedHashMap<String, String> properties;
        properties = Config.getProperties("testConnect");
        smbConnect(AUTH_IN, DIR_IN, properties, file);
    }

    private static void smbConnect(String AUTH_IN, String DIR_IN, LinkedHashMap<String, String> prop, String fileCopy) {
//parameters.get("OUTPUT_IP")+parameters.get("OUTPUT_DIRECTORY_SHARE"), parameters.get("AUTH_OUTPUT_DIRECTORY")

        SmbFile smbFileInput = null;
        SmbFile smbFileOutput = null;
        SmbFileInputStream sms =null;
        String strprog = "STRING CREATED| "; //debug log string
//        String authInDir = smbParam.keySet();

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
            NtlmPasswordAuthentication authInput = new NtlmPasswordAuthentication(AUTH_IN);
            NtlmPasswordAuthentication authOutput = new NtlmPasswordAuthentication(prop.get("AUTH_OUTPUT_DIRECTORY"));

            System.out.println("authOutput =" + authOutput);
            System.out.println("autDir ="+ prop.get("OUTPUT_IP")+prop.get("OUTPUT_DIRECTORY_SHARE"));

//            System.out.println(smbParam.get("AUTH_IN"));
//            strprog += "SMB| ";
//            SmbFile file = new SmbFile("smb://10.38.63.130/d$/fssp/rezervcopy.txt", authInput);

//            smb://Рабочая_группа; Логин: Пароль@Ip/Шара
//            smb://FSSP38; ChybakovaEL: #$rfVgyH78@10.38.63.130/d$/fssp/
//            smb://

//            \\10.38.63.130\d$\fssp


//            System.setProperty("jcifs.encoding", "Cp866"); // default: 30000 millisec.

//            System.setProperty("jcifs.smb.client.responseTimeout", "1200000"); // default: 30000 millisec.
//            System.setProperty("jcifs.smb.client.soTimeout", "1400000"); // default: 35000
//            try{
//            System.out.println(System.getProperty("jcifs.smb.client.responseTimeout"));
//            System.out.println(System.getProperty("jcifs.smb.client.soTimeout"));
//
//            System.out.println("smb://"+smbParam.get("DIR_IN")+fileCopy);
//            }catch (Exception e){
//                System.err.println("Err: " + e.getMessage());
//                e.printStackTrace();
//            }
            try {
//                sms = new SmbFileInputStream("smb://oit1053:oit1053"+"@" +smbParam.get("DIR_IN")+fileCopy);
//                System.out.println(sms.read());
                smbFileInput = new SmbFile("smb://"+DIR_IN+fileCopy, authInput);
//                System.out.println("smbFileInput.canRead()"+smbFileInput.canRead());
                System.out.println("smbFileInput ="+smbFileInput);
                System.out.println("smbFileInput файл существует: "+smbFileInput.exists()+", Доступен для чтения:"+smbFileInput.canRead());
            } catch (MalformedURLException ee) {
                ee.printStackTrace();
                System.out.println("не подключился для копирования Input ");
                System.err.println(new Date() + " " + ee.getMessage());
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
                smbFileOutput = new SmbFile("smb://"+prop.get("OUTPUT_IP")+prop.get("OUTPUT_DIRECTORY_SHARE")+fileCopy, authOutput);
            } catch (java.net.MalformedURLException e) {
                e.printStackTrace();
                System.err.println(e.getMessage());
            }
            try{
//            System.out.println(smbParam.get("AUTH_IN"));
//            System.out.println("smb://"+smbParam.get("DIR_IN")+fileCopy);
                System.out.println("smbFileOutput =" +"smb://"+prop.get("OUTPUT_IP")+prop.get("OUTPUT_DIRECTORY_SHARE")+fileCopy);
//                System.out.println("smbFileOutput файл существует: " + smbFileOutput.exists() + ", Доступен для чтения:" + smbFileOutput.canRead());
            }catch (Exception e){
                System.err.println("Err2: " + e.getMessage());
                e.printStackTrace();
            }
            //скопировать файл
            try {
                if(smbFileInput.exists()){
                    smbFileInput.copyTo(smbFileOutput);
                    System.out.println("файл скопирован");
                    System.out.println("smbFileOutput файл существует: " + smbFileOutput.exists() + ", Доступен для чтения:" + smbFileOutput.canRead());
                }else {
                }
            } catch (jcifs.smb.SmbAuthException sae) {
                System.err.println("Ошибка аутентификации: " + sae.getMessage());
                sae.printStackTrace();
            } catch (jcifs.smb.SmbException se) {
                System.err.println("Ошибка доступа: " + se.getMessage());
                se.printStackTrace();
            } catch (Exception e) {
                System.err.println("Ошибка: " + e.getMessage());
                e.printStackTrace();
            }


        } catch (Exception e) {
            System.err.println("Err: " + e.getMessage());
            e.printStackTrace();
        }
    }


}
