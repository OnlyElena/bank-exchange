package services;

import beans.OSP;
import beans.PostMoney;
import exceptions.FlowException;
import jdbc.BeanConnect;
import jdbc.JDBCConnection;
import jdbc.TestConnect;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import javax.xml.transform.TransformerConfigurationException;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author: OnlyElena@mail.ru
 * Date: 3/11/16
 * Time: 5:13 PM
 */
public class PostService {

    List<BeanConnect> dataSource;
    LinkedHashMap<String, String> properties;
    String dd;
    String depCode;
    String processName;
    String where;
    Hashtable<String, SberbankPostWriter> postWriters;
    OSP osp;

    final static Logger logger = Logger.getLogger(PostService.class);


    public PostService(List<BeanConnect> dataSource, LinkedHashMap<String, String> properties, String dd, String processName) throws FlowException {
        this.dataSource = dataSource;
        this.properties = properties;
        this.dd = dd;
        this.processName = processName;
        init();
    }

    /*В данном методе берем код отдела, его параметры и проверяем на доступность к работе БД*/
    private void init() throws FlowException {
        String smbConnect = dataSource.get(0).getUrl().substring(17, dataSource.get(0).getUrl().indexOf("/"));
        String query = "SELECT * FROM OSP";
        osp = new OSP();
//        JDBCConnection jdbcConnectionOld = new JDBCConnection();
        ResultSet resultSet = JDBCConnection.getInstance().jdbcConnection(dataSource, processName, query);
        if (resultSet == null) {
            System.out.println("Не удалось подключиться к БД, БД не доступна");
            MyLogger.get().logMessage(processName, "Не удалось подключиться к БД, БД не доступна");
            logger.error(processName + " Не удалось подключиться к БД, БД не доступна");
            throw new FlowException(processName + " Не удалось подключиться к БД, БД не доступна" + smbConnect);
        }

        try {
            while (resultSet.next()) {
                depCode = resultSet.getObject("TERRITORY").toString() + resultSet.getObject("DEPARTMENT").toString();
                System.out.println("\nУспешное подключение к БД: " + resultSet.getObject("DIV_NAME"));
                MyLogger.get().logMessage(processName, "\nУспешное подключение к БД: " + resultSet.getObject("DIV_NAME"));
                logger.info(processName +" - Успешное подключение к БД: " + resultSet.getObject("DIV_NAME"));

                osp.setName(resultSet.getString("DIV_NAME"));
                if (osp.getName().length() > 50) {
                    System.out.println("Слишком длинное имя ОСП");
                }
                osp.setFullname(resultSet.getString("DIV_FULLNAME"));
                osp.setTerritory(resultSet.getString("TERRITORY"));
                osp.setDepartment(resultSet.getString("DEPARTMENT"));
                osp.setBik(resultSet.getString("DIV_RECV_BIK"));
                osp.setInn(resultSet.getString("DIV_RECV_INN"));
                osp.setKpp(resultSet.getString("DIV_RECV_KPP"));
                osp.setBankname(resultSet.getString("DIV_RECV_BANKNAME"));
                if (osp.getBankname().length() > 200) {
                    System.out.println("Слишком длинные реквизиты по перечислению банком");
                }
                osp.setOkato(resultSet.getString("DIV_RECV_OKATO"));
                osp.setAccount(resultSet.getString("DIV_RECV_CNT"));
                osp.setLs(resultSet.getString("LS"));
                osp.setAddress(resultSet.getString("DIV_ADR"));
                osp.setOrfkKod(resultSet.getString("ORFK_KOD").substring(2));
//                osp.setReceivTitle("УФК по Иркутской области (ОФК " + osp.getOrfkKod() + ", " + osp.getName() + " УФССП России по Ирк.области)");

//                osp.setReceivTitle("УФК по Кемеровской обл. (ОФК " + osp.getOrfkKod() + ", " + osp.getName() + " УФССП России по Кемеровской обл.)");
                String ReceivTitle = ("УФК(ОФК " + osp.getOrfkKod() + ", " + osp.getName() + " УФССП России)");
                if (ReceivTitle.length() < 94) {
                    osp.setReceivTitle(ReceivTitle);
//                    System.out.println("не обрезает поле ReceivTitle");
                } else {
                    osp.setReceivTitle(ReceivTitle.substring(0, 93) + ")");
//                    System.out.println("обрезает поле ReceivTitle");
                }
            }

        } catch (SQLException e) {
            System.out.println("БД не доступна Не удалось получить параметры ОСП: " + smbConnect);
            MyLogger.get().logMessage(processName, "БД не доступна Не удалось получить параметры ОСП: " + smbConnect + "\n" + e.getMessage());
            logger.error(processName +" - БД не доступна Не удалось получить параметры ОСП: " + smbConnect + "\n" + e);

            JDBCConnection.getInstance().jdbcClose(processName);
            throw new FlowException("Не удалось получить параметры ОСП: " + smbConnect);
//            e.printStackTrace();
        }
        if (depCode == null) {
            MyLogger.get().logMessage(processName, "Не удалось получить код отдела из БД "+smbConnect);
            logger.error(processName +" Не удалось получить код отдела из БД "+smbConnect);
            throw new FlowException("Не удалось получить код отдела из БД "+smbConnect);
        }
        try {
            resultSet.close();
        } catch (SQLException e) {
//            e.printStackTrace();
            MyLogger.get().logMessage(processName, " Не удалось закрыть выборку");
            logger.error(processName+" Не удалось закрыть выборку");
            throw new FlowException("Не удалось закрыть выборку");
        }
        JDBCConnection.getInstance().jdbcClose(processName);
        boolean testConnect = new TestConnect().TestConnect("http://" + smbConnect + ":8080/pksp-server/");
        if (testConnect == false) {
            MyLogger.get().logMessage(processName, "Не удалось подключиться по тонкому клиенту, БД на ремонте");
            logger.error(processName +"Не удалось подключиться по тонкому клиенту, БД на ремонте");
            throw new FlowException("Не удалось подключиться по тонкому клиенту, БД на ремонте");
        }
    }

    public void processCreate() throws FlowException {
        initWhereCondition();

        List<String> packets = getPacketsNumbers();
        if (packets != null) {
            for (String packetId : packets) {
                SberbankPostWriter sberbankPostWriter = openPostWriter(packetId);
                //прерываем обработку, если достигли лимит файлов для запросов
                if (sberbankPostWriter != null) {
                    fetchPackets(packetId);
                    fetchPackets_omena(packetId);
                    sberbankPostWriter.close();
//                jdbcTemplate.execute("UPDATE EXT_RESTRICTION SET PROCEED = 1 WHERE PACK_ID = " + packetId);
                }
            }
        }
    }


    private void initWhereCondition() {
        String whereArr[] = {
                "ER.AGENT_AGREEMENT_CODE = '" + properties.get("MVV_AGREEMENT_CODE") + "'",
                "ER.AGENT_CODE = '" + properties.get("MVV_AGENT_CODE") + "'",
                "ER.AGENT_DEPT_CODE = '" + properties.get("MVV_AGENT_DEPT_CODE") + "'",
                "PROCEED = 0",
                "PIEV_ATTACH = 1",
//                "ER.pack_id = 25501008845225",
                "ENTITY.ENTT_TYPEID IN (2, 71, 95, 96, 97, 666)",
                "ER.doc_code in ('O_IP_ACT_GACCOUNT_MONEY', 'O_IP_ACT_GETCURRENCY', 'O_IP_ACT_ENDGACCOUNT_MONEY', 'O_IP_ACT_ARREST_ACCMONEY', 'O_IP_ACT_ENDARREST')"
//                , 'O_IP_ACT_CURRENCY_ROUB'
//                "MD.usage is not null"
        };

        StringBuilder whereBuilder = new StringBuilder();

        whereBuilder.append(" WHERE ");
        whereBuilder.append(whereArr[0]);
        for (int i = 1; i < whereArr.length; ++i) {
            whereBuilder.append(" AND ").append(whereArr[i]);
        }

        where = whereBuilder.toString();

//        System.out.println("WHERE: " + where);
    }

    private List<String> getPacketsNumbers() throws FlowException {
        String query = "SELECT PACK_ID " +
                " FROM EXT_RESTRICTION ER\n" +
                "        LEFT JOIN DOCUMENT D ON ER.IP_ID = D.ID\n" +
                "        LEFT JOIN DOC_IP ON D.ID = DOC_IP.ID\n" +
                "        LEFT JOIN DOC_IP_DOC DID ON D.ID = DID.ID\n" +
                "        LEFT JOIN ENTITY ON DOC_IP.ID_DBTR = ENTITY.ENTT_ID\n" +

//                "        JOIN SENDLIST S ON S.SENDLIST_O_ID = ER.ACT_ID\n" +
//                "        JOIN O_IP ON O_IP.ID = S.SENDLIST_O_ID\n" +
//                "        INNER JOIN MVV_DATUM MD ON MD.DOCUMENT_ID = O_IP.ID" +

                where +
                " GROUP BY PACK_ID";

//        System.out.println(query);

        ResultSet resultSet = JDBCConnection.getInstance().jdbcConnection(dataSource, processName, query);
        if (resultSet == null) {
            System.out.println("Не удалось подключиться к БД, БД не доступна");
            MyLogger.get().logMessage(processName, "Не удалось подключиться к БД, БД не доступна");
            logger.error(processName +" - Не удалось подключиться к БД, БД не доступна");
            JDBCConnection.getInstance().jdbcClose(processName);
            return null;
        }
        LinkedList<String> packets = new LinkedList<String>();

        try {
            while (resultSet.next()) {
                packets.add(resultSet.getObject("PACK_ID").toString());
            }
            System.out.println("Пакетов для отправки: " + packets.size());
            MyLogger.get().logMessage(processName, "Пакетов для отправки: " + packets.size());
            logger.info(processName +" - Пакетов для отправки: " + packets.size());

        } catch (SQLException e) {
            System.out.println("Не удалось подключиться и собрать пакеты");
            MyLogger.get().logMessage(processName, "Не удалось подключиться и собрать пакеты");
            logger.error(processName +" - Не удалось подключиться и собрать пакеты");
            JDBCConnection.getInstance().jdbcClose(processName);
            return null;
//            e.printStackTrace();
        }
        try {
            resultSet.close();
        } catch (SQLException e) {
//            e.printStackTrace();
            MyLogger.get().logMessage(processName, " Не удалось закрыть выборку2");
            logger.error(processName+" Не удалось закрыть выборку2");
            throw new FlowException("Не удалось закрыть выборку2");
        }
        JDBCConnection.getInstance().jdbcClose(processName);
        return packets;
    }

    private void fetchPackets(String packetId) throws FlowException {
        boolean pievCopyService;
//        Этот запрос для постановлений об обращении взыскания и наложении ареста
//        и снятия ареста с ДС и об отмене постановления об обращении взыскания на ДС
        String query = "SELECT DISTINCT " +
                " DOC.ID,  /*PK ДОКУМЕНТА*/" +
                " ER.ACT_ID, " +
                " ER.DOC_CODE,  S.SENDLIST_PARENT_METANAME, /*КОД ДОКУМЕНТА*/" +
                " D.PARENT_ID," +
                " D.PARENT_INFO," +
                " DOC.BARCODE,  /*BAR_CODE*/" +
                " DOC.DOC_NUMBER,  /*--НОМЕР ПОСТАНОВЛЕНИЯ ОБ ОБРАЩЕНИИ ВЗЫСКАНИЯ*/" +
                " DOC.DOC_DATE,  /*--ДАТА ПОСТАНОВЛЕНИЯ ОБ ОБРАЩЕНИИ ВЗЫСКАНИЯ*/" +
                " ER.DOC_DATE," +
                " O_IP.IPNO,  /*--НОМЕР ИП*/" +
                " DOC_IP.ID_NO /*as \\\"Регистрационный номер ИД - старый номер ИП\\\"*/," +
                " O_IP.IP_RISEDATE,  /*--ДАТА ВОЗБУЖДЕНИЯ ИП*/" +
                " DID.PRIORITY_PENALTIES,  /*--ОЧЕРЕДНОСТЬ ВЗЫСКАНИЯ*/" +
                " O_IP.ID_DOCNO,  /*--НОМЕР ИД*/" +
                " O_IP.ID_DOCDATE,  /*--ДАТА ИД*/" +
                " O_IP.ID_COURT_NAME,  /*--НАИМЕНОВАНИЕ ОРГАНА ВЫДАВШЕГО ИД*/" +
                " O_IP.ID_COURT_ADR,  /*--АДРЕС ОРГАНА ВЫДАВШЕГО ИД*/" +
                " O_IP.IP_EXEC_PRIST_NAME,  /*--ФИО СПИ*/" +
//                " /*//             O_IP.IP_REST_DEBTSUM,  /*--ОСТАТОК ДОЛГА ПО ИП*/" +
                " O_IP.TOTAL_DEPT_SUM,/*-- ОБЩАЯ СУММА ЗАДОЛЖЕННОСТИ ПО ПОСТАНОВЛЕНИЮ ОБ ОБРАЩЕНИИ НА ДС */" +
//                " /*//              DID.ID_CRDR_ENTID,  /*--ПРИЗНАК ВЗЫСКАТЕЛЯ, ЗАЯВИТЕЛЯ (ТИП??)*/" +
                " O_IP.ID_CRDR_NAME," +
                " O_IP.ID_CRDR_ADR," +
                " MDA.ACC,  /*--СЧЕТ С КОТОРОГО СПИСЫВАЮТСЯ ДЕНЬГИ*/" +
                " ENTITY.ENTT_SURNAME,   /*--ФИО ДОЛЖНИКА*/" +
                " ENTITY.ENTT_FIRSTNAME," +
                " ENTITY.ENTT_PATRONYMIC," +
                " DID.ID_DBTR_BORNADR," +
                " DID.DBTR_BORN_YEAR," +
                " DID.ID_DBTR_BORN, PACK_ID," +
                " O_IP.ID_DBTR_ADR,  /*ПРОПИСКА ДОЛЖНИКА*/" +
                " MDA.BIC_BANK, " +
//                " ACC.ARREST_AMOUNT, " +
//                "  --MDA.CURRENCY_TYPE_ID," +
//                " ACC.CURRENCY_TYPE_ID, ACC.CURRENCY_TYPE /*-- ВАЛЮТА*/" +
                " MDA.CURRENCY_TYPE /*-- ВАЛЮТА*/" +
//                " MD.USAGE" +
                " FROM" +
                " EXT_RESTRICTION ER" +
                " INNER JOIN DOCUMENT D ON ER.IP_ID = D.ID" +
                " INNER JOIN DOC_IP ON D.ID = DOC_IP.ID" +
                " INNER JOIN DOC_IP_DOC DID ON D.ID = DID.ID" +
                " INNER JOIN ENTITY ON DOC_IP.ID_DBTR = ENTITY.ENTT_ID" +
//                " JOIN MVV_SENDLIST MS ON MS.ID = ER.ACT_ID" +
                " JOIN SENDLIST S ON S.SENDLIST_O_ID = ER.ACT_ID" +
//                " /* JOIN O_IP_ACT_GACCOUNT_MONEY A1 ON A1.ID = S.SENDLIST_O_ID*/" +
                " JOIN O_IP ON O_IP.ID = S.SENDLIST_O_ID" +
                " JOIN DOCUMENT DOC ON DOC.ID = O_IP.ID" +
                "    join DATUM_LINK_OIP DLO ON DLO.DOC_ID=ER.ACT_ID\n" +
//                " JOIN MVV_DATUM MD ON MD.DOCUMENT_ID = O_IP.ID" +
//                "--   JOIN MVV_DATUM MD ON MD.DOCUMENT_ID = O_IP.ID\n" +
                "   JOIN MVV_DATUM MD ON MD.ID = DLO.DATUM_ID" +
                " JOIN MVV_DATUM_AVAILABILITY_ACC ACC ON ACC.ID = MD.ID" +
                " join MVV_DATUM_ACCOUNT MDA on MDA.ID = ACC.ID" +
                where +
                " AND DOC_CODE IN ('O_IP_ACT_GETCURRENCY', 'O_IP_ACT_ARREST_ACCMONEY', " +
                "'O_IP_ACT_GACCOUNT_MONEY','O_IP_ACT_ENDARREST', 'O_IP_ACT_CURRENCY_ROUB') " +
//                " AND ((MD.USAGE CONTAINING 'АРЕСТОВАТЬ' " +
//                "       AND ER.DOC_CODE IN ('O_IP_ACT_GETCURRENCY', 'O_IP_ACT_ARREST_ACCMONEY', 'O_IP_ACT_GACCOUNT_MONEY'))" +
//                "  OR ((MD.USAGE CONTAINING 'СНЯТЬ АРЕСТ' " +
//                "       AND ER.DOC_CODE IN (/*'O_IP_ACT_ENDGACCOUNT_MONEY',*/ 'O_IP_ACT_ENDARREST'))))" +
                " AND PACK_ID = " + packetId + " " +
                " order by ER.id";

//        System.out.println("QUERY: " + query);

        List<PostMoney> post = new LinkedList<PostMoney>();
//        PostMoney r = new PostMoney();


        ResultSet resultSet = JDBCConnection.getInstance().jdbcConnection(dataSource, processName, query);
        if (resultSet == null) {
            System.out.println("Не удалось подключиться к БД, БД не доступна");
            MyLogger.get().logMessage(processName, "Не удалось подключиться к БД, БД не доступна");
            logger.error(processName +" - Не удалось подключиться к БД, БД не доступна");
            throw new FlowException(processName +" - Не удалось подключиться к БД, БД не доступна");
        }


        try {
            while (resultSet.next()) {
//                MyLogger.get().logMessage(processName, "------------" + resultSet.getString("ID"));
                PostMoney r = new PostMoney();
                System.out.print(resultSet.getString("ID")+"==");
                r.setId(getLong(resultSet, "ID"));
                r.setBarcode(getString(resultSet, "BARCODE"));
                r.setActtype(getString(resultSet, "DOC_CODE"));
                r.setExecProcNumber(getString(resultSet, "ipno"));
//                System.out.println("**********************************");
                if ((resultSet.getObject("ID_NO") != null) && (!((getString(resultSet, "ipno")).contains((getString(resultSet, "ID_NO")))) && ((getString(resultSet, "DOC_CODE")).equals("O_IP_ACT_ENDARREST")))) {
                    r.setExecOldProcNumber(getString(resultSet, "ID_NO"));
//                    System.out.println(getString(resultSet, "ID_NO"));
                }
//                    r.setExecProcDate(getDateDDMMYYYY(row.get("ip_risedate")));
                r.setExecProcDate(parseDate(getSqlDate(resultSet, "ip_risedate")));
                r.setActNumber(getString(resultSet, "doc_number"));
                r.setActDate(parseDate(getSqlDate(resultSet, "doc_date")));
                r.setPriority(getInteger(resultSet, "priority_penalties"));
                r.setExecActNum(getString(resultSet, "id_docno"));
                r.setExecActDate(parseDate(getSqlDate(resultSet, "id_docdate")));
                r.setExecActInitial(getString(resultSet, "id_court_name"));
                r.setExecActInitialAddr(getString(resultSet, "id_court_adr"));
                r.setBailiff(getString(resultSet, "ip_exec_prist_name"));
//                act.setSumm(rs.getString("ip_rest_debtsum"));
                r.setSumm(getBigDecimal(resultSet, "total_dept_sum"));
                r.setCreditorName(getString(resultSet, "id_crdr_name"));
                r.setCreditorAddress(getString(resultSet, "id_crdr_adr"));
                r.setAccountNumber(getString(resultSet, "ACC"));
                r.setDebtorFirstName(getString(resultSet, "entt_firstname"));
                r.setDebtorLastName(getString(resultSet, "entt_surname"));
                r.setDebtorSecondName(getString(resultSet, "entt_patronymic"));
                r.setDebtorBornAddres(getString(resultSet, "id_dbtr_bornadr"));
                r.setDebtorAddres(getString(resultSet, "id_dbtr_adr"));
                r.setDebtorBirthYear(getInteger(resultSet, "dbtr_born_year"));
                r.setDebtorBirth(parseDate(getSqlDate(resultSet, "id_dbtr_born")));
                r.setAccountCurreny(getString(resultSet, "CURRENCY_TYPE"));


                post.add(r);
            }

        } catch (SQLException e) {
            System.out.println("БД не доступна: " + osp.getName());
//            в случае возникновения исключительной ситуации, удаляем сформированные файлы
            System.err.println(e.getMessage() + " ОШИБКА в fetchPackets: ");
            MyLogger.get().logMessage(processName, e.getMessage() + " ОШИБКА в fetchPackets: ");
            logger.error(processName +" " + e.getMessage() + " ОШИБКА в fetchPackets: ");

            JDBCConnection.getInstance().jdbcClose(processName);
            throw new FlowException("Post " + e.getMessage() + " ОШИБКА в fetchPackets: ");
        }

        try {
            resultSet.close();
        } catch (SQLException e) {
//            e.printStackTrace();
            MyLogger.get().logMessage(processName, " Не удалось закрыть выборку3");
            logger.error(processName+" Не удалось закрыть выборку3");
            throw new FlowException("Не удалось закрыть выборку3");
        }
        JDBCConnection.getInstance().jdbcClose(processName);

        if (post != null){
//            System.out.println("////////////////////"+post.size()+"//////////////////");
            for (PostMoney pos:post) {
//                MyLogger.get().logMessage(processName,pos.getActtype());
//                if (pos.getActtype().contains("O_IP_ACT_GETCURRENCY") || pos.getActtype().contains("O_IP_ACT_ARREST_ACCMONEY")
//                        || pos.getActtype().contains("O_IP_ACT_GACCOUNT_MONEY") || pos.getActtype().contains("O_IP_ACT_CURRENCY_ROUB"))
//                {
//                    MyLogger.get().logMessage(processName, "CONTAINS!!!!");
//                }
//
//                if (pos.getActtype().equals("O_IP_ACT_GETCURRENCY") || pos.getActtype().equals("O_IP_ACT_ARREST_ACCMONEY")
//                        || pos.getActtype().equals("O_IP_ACT_GACCOUNT_MONEY") || pos.getActtype().equals("O_IP_ACT_CURRENCY_ROUB"))
//
//                {
//                    MyLogger.get().logMessage(processName, "EQUALS!!!!");
//                    ImportNotifNotPievService importNotifNotPievService = new ImportNotifNotPievService(dataSource, properties, processName);
//                    boolean b = false;
//                    try {
//                        String statusNotif = "Постановление не обрабатывается на региональном уровне";
//                        b = importNotifNotPievService.processResponse("piev_" + pos.getId() + ".xml.zip" , statusNotif);
//                    } catch (FlowException e) {
//                        System.err.println(new Date() + " " + e.getMessage());
//                        MyLogger.get().logMessage(processName, new Date() + " " + e.getMessage());
//                        logger.error(new Date() + " " + e.getMessage());
//                    }
//                    if(b == true){
//                        System.out.println("Сделана запись уведомления об \"Постановление не обрабатывается на региональном уровне\" " + "piev_" + pos.getId() + ".xml.zip");
//                        MyLogger.get().logMessage(processName, "Сделана запись уведомления об \"Постановление не обрабатывается на региональном уровне\" " + "piev_" + pos.getId() + ".xml.zip");
//                        logger.error("Сделана запись уведомления об \"Постановление не обрабатывается на региональном уровне\" " + "piev_" + pos.getId() + ".xml.zip");
//                    } else {
//                        MyLogger.get().logMessage(processName, "не удалось произвести запись уведомления об \"Постановление не обрабатывается на региональном уровне\" " + "piev_" + pos.getId() + ".xml.zip");
//                        logger.error("не удалось произвести запись уведомления об \"Постановление не обрабатывается на региональном уровне\" " + "piev_" + pos.getId() + ".xml.zip");
//                        throw new FlowException("не удалось произвести запись уведомления об \"Постановление не обрабатывается на региональном уровне\" " + "piev_" + pos.getId() + ".xml.zip");
//                    }
//
//                }
//                else {


//                piev_25161054884546.xml
//               pievCopyService = new PievCopyService("smb://10.38.63.130/d$/fssp/rezervcopy.txt");
//                MyLogger.get().logMessage(processName, getLong(resultSet, "ID").toString()+" ****");
//                MyLogger.get().logMessage(processName, processName + " " + dataSource + " " + properties + " " + dataSource.get(0).getUrlPIEV() + " " + dataSource.get(0).getAuthentication() + " " + properties.get("OUTPUT_IP") + " " + properties.get("OUTPUT_DIRECTORY_SHARE") + " " + properties.get("AUTH_OUTPUT_DIRECTORY") + " " + "piev_" + pos.getId() + ".xml.zip");
//                System.out.println(processName + " " + dataSource + " " + properties + " " + dataSource.get(0).getUrlPIEV() + " " + dataSource.get(0).getAuthentication() + " " + properties.get("OUTPUT_IP") + " " + properties.get("OUTPUT_DIRECTORY_SHARE") + " " + properties.get("AUTH_OUTPUT_DIRECTORY") + " " + "piev_" + pos.getId() + ".xml.zip");
                    pievCopyService = PievCopyService.PievCopyService(processName, dataSource, properties, dataSource.get(0).getUrlPIEV(), dataSource.get(0).getAuthentication(), properties.get("OUTPUT_IP") + properties.get("OUTPUT_DIRECTORY_SHARE"), properties.get("AUTH_OUTPUT_DIRECTORY"), "piev_" + pos.getId() + ".xml.zip");
                    System.out.println("pievCopyService" + pievCopyService);

                    if (pievCopyService == true) {
//                    System.out.println("writePost");
//                    System.out.println(packetId+ "  "+ pos+ "  "+osp);
                        writePost(packetId, pos, osp);
                        System.out.println("piev_" + pos.getId() + ".xml.zip " + "Файл успешно скопирован и записан в реестр");
                        MyLogger.get().logMessage(processName, "piev_" + pos.getId() + ".xml.zip " + "Файл успешно скопирован и записан в реестр");
                    } else {
                        System.out.println("ИП " + pos.getExecProcNumber() + " !!!!!!!!!!!!!!!! файл не скопирован!!!!!!!!!!!!");
                        MyLogger.get().logMessage(processName, "ИП " + pos.getExecProcNumber() + " !!!!!!!!!!!!!!!! файл не скопирован!!!!!!!!!!!!");
                        logger.error(processName + " - ИП " + pos.getExecProcNumber() + " !!!!!!!!!!!!!!!! файл не скопирован!!!!!!!!!!!!");
                    }
                }
//            }

        System.out.println("*--Запрос для постановлений об обращении взыскания и наложении/снятии ареста--*");
        MyLogger.get().logMessage(processName, "*--Запрос для постановлений об обращении взыскания и наложении/снятии ареста--*");
            System.out.println("*********" + post.size());
            MyLogger.get().logMessage(processName, "*********" + post.size());
    }

        String query_up = "UPDATE EXT_RESTRICTION SET PROCEED = 1 " +
                "WHERE DOC_CODE IN ('O_IP_ACT_GETCURRENCY', 'O_IP_ACT_ARREST_ACCMONEY', " +
                "'O_IP_ACT_GACCOUNT_MONEY','O_IP_ACT_ENDARREST', 'O_IP_ACT_CURRENCY_ROUB') AND PACK_ID = " + packetId;

//        if (!post.isEmpty()) {

            JDBCConnection.getInstance().jdbcUpdate(dataSource, processName, query_up);
        System.out.println("update_restriction");
//        } else {
//            JDBCConnection.getInstance().jdbcClose();
//        }
    }

    private void fetchPackets_omena(String packetId) throws FlowException {
        boolean pievCopyService;
//      Запрос об отмене постановления об обращении взыскания на ДС
        String query_sn = "SELECT DISTINCT " +
                " DOC.ID,  /*PK ДОКУМЕНТА*/" +
                " ER.ACT_ID, " +
                " ER.DOC_CODE,  S.SENDLIST_PARENT_METANAME, /*КОД ДОКУМЕНТА*/" +
                " D.PARENT_ID," +
                " D.PARENT_INFO," +
                " DOC.BARCODE,  /*BAR_CODE*/" +
                " DOC.DOC_NUMBER,  /*--НОМЕР ПОСТАНОВЛЕНИЯ ОБ ОБРАЩЕНИИ ВЗЫСКАНИЯ*/" +
                " DOC.DOC_DATE,  /*--ДАТА ПОСТАНОВЛЕНИЯ ОБ ОБРАЩЕНИИ ВЗЫСКАНИЯ*/" +
                " ER.DOC_DATE," +
                " O_IP.IPNO,  /*--НОМЕР ИП*/" +
                " DOC_IP.ID_NO /*as \\\"Регистрационный номер ИД - старый номер ИП\\\"*/," +
                " O_IP.IP_RISEDATE,  /*--ДАТА ВОЗБУЖДЕНИЯ ИП*/" +
                " DID.PRIORITY_PENALTIES,  /*--ОЧЕРЕДНОСТЬ ВЗЫСКАНИЯ*/" +
                " O_IP.ID_DOCNO,  /*--НОМЕР ИД*/" +
                " O_IP.ID_DOCDATE,  /*--ДАТА ИД*/" +
                " O_IP.ID_COURT_NAME,  /*--НАИМЕНОВАНИЕ ОРГАНА ВЫДАВШЕГО ИД*/" +
                " O_IP.ID_COURT_ADR,  /*--АДРЕС ОРГАНА ВЫДАВШЕГО ИД*/" +
                " O_IP.IP_EXEC_PRIST_NAME,  /*--ФИО СПИ*/" +
//                " /*//             O_IP.IP_REST_DEBTSUM,  /*--ОСТАТОК ДОЛГА ПО ИП*/" +
                " O_IP.TOTAL_DEPT_SUM,/*-- ОБЩАЯ СУММА ЗАДОЛЖЕННОСТИ ПО ПОСТАНОВЛЕНИЮ ОБ ОБРАЩЕНИИ НА ДС */" +
                " GACC.GACCOUNT_MONEY_TOTAL_DEPT_SUM, /*-- СУММА ПО ПОСТАНОВЛЕНИЮ ОБ ОБРАЩЕНИИ*/" +
//                " /*//              DID.ID_CRDR_ENTID,  /*--ПРИЗНАК ВЗЫСКАТЕЛЯ, ЗАЯВИТЕЛЯ (ТИП??)*/" +
                " O_IP.ID_CRDR_NAME," +
                " O_IP.ID_CRDR_ADR," +
                " MDA.ACC,  /*--СЧЕТ С КОТОРОГО СПИСЫВАЮТСЯ ДЕНЬГИ*/" +
                " ENTITY.ENTT_SURNAME,   /*--ФИО ДОЛЖНИКА*/" +
                " ENTITY.ENTT_FIRSTNAME," +
                " ENTITY.ENTT_PATRONYMIC," +
                " DID.ID_DBTR_BORNADR," +
                " DID.DBTR_BORN_YEAR," +
                " DID.ID_DBTR_BORN, PACK_ID," +
                " O_IP.ID_DBTR_ADR,  /*ПРОПИСКА ДОЛЖНИКА*/" +
                " MDA.BIC_BANK, " +
//                " ACC.ARREST_AMOUNT, " +
//                "  --MDA.CURRENCY_TYPE_ID," +
                " MDA.CURRENCY_TYPE /*-- ВАЛЮТА*/" +
//                " MD.USAGE" +
                " FROM" +
                " EXT_RESTRICTION ER" +
                " INNER JOIN DOCUMENT D ON ER.IP_ID = D.ID" +
                " INNER JOIN DOC_IP ON D.ID = DOC_IP.ID" +
                " INNER JOIN DOC_IP_DOC DID ON D.ID = DID.ID" +
                " INNER JOIN ENTITY ON DOC_IP.ID_DBTR = ENTITY.ENTT_ID" +
//                " JOIN MVV_SENDLIST MS ON MS.ID = ER.ACT_ID" +
                " JOIN SENDLIST S ON S.SENDLIST_O_ID = ER.ACT_ID" +
//                " /* JOIN O_IP_ACT_GACCOUNT_MONEY A1 ON A1.ID = S.SENDLIST_O_ID*/" +
                " JOIN O_IP ON O_IP.ID = S.SENDLIST_O_ID" +
                " JOIN DOCUMENT DOC ON DOC.ID = O_IP.ID" +
                " JOIN O_IP_ACT_ENDGACCOUNT_MONEY GACC  ON GACC.ID = DOC.ID" +
                "   join DATUM_LINK_OIP DLO ON DLO.DOC_ID=ER.ACT_ID\n" +
//                " JOIN MVV_DATUM MD ON MD.DOCUMENT_ID = O_IP.ID" +
//                "--   JOIN MVV_DATUM MD ON MD.DOCUMENT_ID = O_IP.ID\n" +
                "   JOIN MVV_DATUM MD ON MD.ID = DLO.DATUM_ID" +
                " JOIN MVV_DATUM_AVAILABILITY_ACC ACC ON ACC.ID = MD.ID" +
                " join MVV_DATUM_ACCOUNT MDA on MDA.ID = ACC.ID" +
                where +
                " AND DOC_CODE IN ('O_IP_ACT_ENDGACCOUNT_MONEY', 'O_IP_ACT_CURRENCY_ROUB') " +
//                " AND ((MD.USAGE CONTAINING 'СНЯТЬ АРЕСТ' " +
//                "       AND ER.DOC_CODE IN ('O_IP_ACT_ENDGACCOUNT_MONEY')))" +
                " AND PACK_ID = " + packetId + " " +
                " order by ER.id";
//        System.out.println("QUERY_SN: " + query_sn);


        List<PostMoney> post = new LinkedList<PostMoney>();
//        PostMoney r = new PostMoney();

        ResultSet resultSet = JDBCConnection.getInstance().jdbcConnection(dataSource, processName, query_sn);
        if (resultSet == null) {
            System.out.println("Не удалось подключиться к БД, БД не доступна(otmena)");
            MyLogger.get().logMessage(processName, "Не удалось подключиться к БД, БД не доступна(otmena)");
            logger.error(processName +" - Не удалось подключиться к БД, БД не доступна(otmena)");
            throw new FlowException(processName +" - Не удалось подключиться к БД, БД не доступна(otmena)");

        }

        try {
            while (resultSet.next()) {

                PostMoney r = new PostMoney();
                r.setId(getLong(resultSet, "ID"));
                r.setBarcode(getString(resultSet, "BARCODE"));
                r.setActtype(getString(resultSet, "DOC_CODE"));
                r.setExecProcNumber(getString(resultSet, "ipno"));
//                MyLogger.get().logMessage(processName, "------------"+resultSet.getString("ipno")+"  "+resultSet.getString("ID_NO") +" "+ getString(resultSet, "DOC_CODE"));
//                if (!resultSet.getObject("ipno").toString().contains(resultSet.getObject("ID_NO").toString()) && resultSet.getObject("DOC_CODE").equals("O_IP_ACT_ENDGACCOUNT_MONEY")) {
//                    r.setExecOldProcNumber(getString(resultSet, "ID_NO"));
//                }
                if (!((getString(resultSet, "ipno")).contains(getString(resultSet, "ID_NO"))) && ((getString(resultSet, "DOC_CODE")).equals("O_IP_ACT_ENDGACCOUNT_MONEY"))) {
                    r.setExecOldProcNumber(getString(resultSet, "ID_NO"));
//                    System.out.println(getString(resultSet, "ID_NO"));
                }
//                    r.setExecProcDate(getDateDDMMYYYY(row.get("ip_risedate")));
                r.setExecProcDate(parseDate(getSqlDate(resultSet, "ip_risedate")));
                r.setActNumber(getString(resultSet, "doc_number"));
                r.setActDate(parseDate(getSqlDate(resultSet, "doc_date")));
                r.setPriority(getInteger(resultSet, "priority_penalties"));
                r.setExecActNum(getString(resultSet, "id_docno"));
                r.setExecActDate(parseDate(getSqlDate(resultSet, "id_docdate")));
                r.setExecActInitial(getString(resultSet, "id_court_name"));
                r.setExecActInitialAddr(getString(resultSet, "id_court_adr"));
                r.setBailiff(getString(resultSet, "ip_exec_prist_name"));
//                act.setSumm(rs.getString("ip_rest_debtsum"));
                r.setSumm(getBigDecimal(resultSet, "GACCOUNT_MONEY_TOTAL_DEPT_SUM"));
                r.setCreditorName(getString(resultSet, "id_crdr_name"));
                r.setCreditorAddress(getString(resultSet, "id_crdr_adr"));
                r.setAccountNumber(getString(resultSet, "ACC"));
                r.setDebtorFirstName(getString(resultSet, "entt_firstname"));
                r.setDebtorLastName(getString(resultSet, "entt_surname"));
                r.setDebtorSecondName(getString(resultSet, "entt_patronymic"));
                r.setDebtorBornAddres(getString(resultSet, "id_dbtr_bornadr"));
                r.setDebtorAddres(getString(resultSet, "id_dbtr_adr"));
                r.setDebtorBirthYear(getInteger(resultSet, "dbtr_born_year"));
                r.setDebtorBirth(parseDate(getSqlDate(resultSet, "id_dbtr_born")));
                r.setAccountCurreny(getString(resultSet, "CURRENCY_TYPE"));

//                System.out.println(r);

                post.add(r);

            }

        } catch (SQLException e) {
            System.out.println("БД не доступна: " + osp.getName());
//            в случае возникновения исключительной ситуации, удаляем сформированные файлы
            System.err.println(e.getMessage() + "ОШИБКА в fetchPackets_otmena: ");
            MyLogger.get().logMessage(processName, e.getMessage() + "ОШИБКА в fetchPackets_otmena: ");
            logger.error(processName +" - " + e.getMessage() + "ОШИБКА в fetchPackets_otmena: ");

            JDBCConnection.getInstance().jdbcClose(processName);
            throw new FlowException(processName +" " + e.getMessage() + "ОШИБКА в fetchPackets_otmena: ");
        }

        try {
            resultSet.close();
        } catch (SQLException e) {
//            e.printStackTrace();
            MyLogger.get().logMessage(processName, " Не удалось закрыть выборку4");
            logger.error(processName+" Не удалось закрыть выборку4");
            throw new FlowException("Не удалось закрыть выборку4");
        }
        JDBCConnection.getInstance().jdbcClose(processName);

        if (post != null){
            for (PostMoney pos:post){
//                MyLogger.get().logMessage(processName, processName + " " + dataSource + " " + properties + " " + dataSource.get(0).getUrlPIEV() + " " + dataSource.get(0).getAuthentication() + " " + properties.get("OUTPUT_IP") + " " + properties.get("OUTPUT_DIRECTORY_SHARE") + " " + properties.get("AUTH_OUTPUT_DIRECTORY") + " " + "piev_" + pos.getId() + ".xml.zip");
//                System.out.println(processName+" "+dataSource+" "+properties+" "+dataSource.get(0).getUrlPIEV()+" "+dataSource.get(0).getAuthentication()+" "+properties.get("OUTPUT_IP")+" "+properties.get("OUTPUT_DIRECTORY_SHARE")+" "+properties.get("AUTH_OUTPUT_DIRECTORY")+" "+"piev_" + pos.getId() + ".xml.zip");
                pievCopyService = PievCopyService.PievCopyService(processName, dataSource, properties, dataSource.get(0).getUrlPIEV(), dataSource.get(0).getAuthentication(), properties.get("OUTPUT_IP") + properties.get("OUTPUT_DIRECTORY_SHARE"), properties.get("AUTH_OUTPUT_DIRECTORY"), "piev_" + pos.getId() + ".xml.zip");
//                System.out.println("pievCopyService"+pievCopyService);
                if (pievCopyService == true) {
//                    System.out.println("writePost");
//                    System.out.println(packetId+ "  "+ pos+ "  "+osp);
                    writePost(packetId, pos, osp);
                    System.out.println("piev_" + pos.getId() + ".xml.zip " + "Файл успешно скопирован и записан в реестр");
                    MyLogger.get().logMessage(processName, "piev_" + pos.getId() + ".xml.zip " + "Файл успешно скопирован и записан в реестр");
                } else {
                    System.out.println("ИП " + pos.getExecProcNumber() + " !!!!!!!!!!!!!!!! файл не скопирован!!!!!!!!!!!!");
                    MyLogger.get().logMessage(processName, "ИП " + pos.getExecProcNumber() + " !!!!!!!!!!!!!!!! файл не скопирован!!!!!!!!!!!!");
                    logger.error(processName +" - ИП " + pos.getExecProcNumber() + " !!!!!!!!!!!!!!!! файл не скопирован!!!!!!!!!!!!");
                }
            }
            System.out.println("*--Запрос об отмене постановления об обращении взыскания на ДС --*");
            MyLogger.get().logMessage(processName, "*--Запрос об отмене постановления об обращении взыскания на ДС --*");
            System.out.println("*************" + post.size());
            MyLogger.get().logMessage(processName, "*************" + post.size());
        }


        String query_up_otmena = "UPDATE EXT_RESTRICTION SET PROCEED = 1 " +
                "WHERE DOC_CODE IN ('O_IP_ACT_ENDGACCOUNT_MONEY', 'O_IP_ACT_CURRENCY_ROUB') AND PACK_ID = " + packetId;
//        System.out.println(query_up_otmena);
//        if (!post.isEmpty()) {
            JDBCConnection.getInstance().jdbcUpdate(dataSource, processName, query_up_otmena);
        System.out.println("update_otmena");
//        } else {
//            JDBCConnection.getInstance().jdbcClose();
//        }

    }


    private String getInteger(ResultSet row, String filedName) {
        Object o = null;
        try {
            o = row.getObject(filedName);
        } catch (SQLException e) {
            System.err.println("Field: " + filedName + ", are not Integer type. Actual type: " + o.getClass());
            MyLogger.get().logMessage(processName, "Field: " + filedName + ", are not Integer type. Actual type: " + o.getClass());
            logger.error("Field: " + filedName + ", are not Integer type. Actual type: " + o.getClass());
            return o.toString();
        }
        if (o == null) return null;

        if (o instanceof Integer) {
            return o.toString();
        } else {
            System.err.println("Field: " + filedName + ", are not Integer type. Actual type: " + o.getClass());
            MyLogger.get().logMessage(processName, "Field: " + filedName + ", are not Integer type. Actual type: " + o.getClass());
            logger.error("Field: " + filedName + ", are not Integer type. Actual type: " + o.getClass());
            return o.toString();
        }
    }

    private String getString(ResultSet row, String filedName) {
        Object o = null;
        try {
            o = row.getObject(filedName);
        } catch (SQLException e) {
            System.err.println("Field: " + filedName + ", are not String type. Actual type: " + o.getClass());
            MyLogger.get().logMessage(processName, "Field: " + filedName + ", are not String type. Actual type: " + o.getClass());
            logger.error("Field: " + filedName + ", are not String type. Actual type: " + o.getClass());
            return o.toString();
        }
        if (o == null) return null;

        if (o instanceof String) {
            return (String) o;
        } else {
            System.err.println("Field: " + filedName + ", are not String type. Actual type: " + o.getClass());
            MyLogger.get().logMessage(processName, "Field: " + filedName + ", are not String type. Actual type: " + o.getClass());
            logger.error("Field: " + filedName + ", are not String type. Actual type: " + o.getClass());
            return o.toString();
        }
    }

    private Long getLong(ResultSet row, String filedName) throws FlowException {
        Object o = null;
        try {
            o = row.getObject(filedName);
        } catch (SQLException e) {
            System.err.println("Field: " + filedName + ", are not String type. Actual type: " + o.getClass());
            MyLogger.get().logMessage(processName, "Field: " + filedName + ", are not String type. Actual type: " + o.getClass());
            logger.error("Field: " + filedName + ", are not String type. Actual type: " + o.getClass());
            throw new FlowException("Can't convert Long");
        }
        if (o == null) return null;

        if (o instanceof Long) {
            return (Long) o;
        } else {
            System.err.println("Field: " + filedName + ", are not String type. Actual type: " + o.getClass());
            MyLogger.get().logMessage(processName, "Field: " + filedName + ", are not String type. Actual type: " + o.getClass());
            logger.error("Field: " + filedName + ", are not String type. Actual type: " + o.getClass());
            throw new FlowException("Can't convert Long");
        }
    }

    private java.sql.Date getSqlDate(ResultSet row, String filedName) throws FlowException {
        Object o = null;
        try {
            o = row.getObject(filedName);
        } catch (SQLException e) {
            System.err.println("Field: " + filedName + ", are not java.sql.Date type. Actual type: " + o.getClass());
            MyLogger.get().logMessage(processName, "Field: " + filedName + ", are not java.sql.Date type. Actual type: " + o.getClass());
            logger.error("Field: " + filedName + ", are not java.sql.Date type. Actual type: " + o.getClass());
            throw new FlowException("Can't convert data");
        }
        if (o == null) return null;

        if (o instanceof java.sql.Date) {
            return (java.sql.Date) o;
        } else {
            System.err.println("Field: " + filedName + ", are not java.sql.Date type. Actual type: " + o.getClass());
            MyLogger.get().logMessage(processName, "Field: " + filedName + ", are not java.sql.Date type. Actual type: " + o.getClass());
            logger.error("Field: " + filedName + ", are not java.sql.Date type. Actual type: " + o.getClass());
            throw new FlowException("Can't convert data");
        }
    }

    private String getBigDecimal(ResultSet row, String filedName) throws FlowException {
        Object o = null;
        try {
            o = row.getObject(filedName);
        } catch (SQLException e) {
            System.err.println("Field: " + filedName + ", are not bigDecimal type. Actual type: " + o.getClass());
            MyLogger.get().logMessage(processName, "Field: " + filedName + ", are not bigDecimal type. Actual type: " + o.getClass());
            logger.error("Field: " + filedName + ", are not bigDecimal type. Actual type: " + o.getClass());
            return "0";
        }
        if (o == null) return "0";

        if (o instanceof BigDecimal) {
            return o.toString();
        } else {
            System.err.println("Field: " + filedName + ", are not bigDecimal type. Actual type: " + o.getClass());
            MyLogger.get().logMessage(processName, "Field: " + filedName + ", are not bigDecimal type. Actual type: " + o.getClass());
            logger.error("Field: " + filedName + ", are not bigDecimal type. Actual type: " + o.getClass());
            return "0";
        }
    }

    private String parseDate(java.sql.Date wrongDate) {
        SimpleDateFormat ddmmyyyy = new SimpleDateFormat("dd.MM.yyyy");
        if (wrongDate == null) return null;
        return ddmmyyyy.format(wrongDate);
    }


    /**
     * В этом методе создается ассоциативный массив, где ключ это номер пакета
     *
     * @param packetId
     * @throws FlowException
     * @throws TransformerConfigurationException
     * @throws SAXException
     * @throws FileNotFoundException
     */
//    private SberbankPostWriter openPostWriter(String packetId) throws FlowException, TransformerConfigurationException, SAXException, FileNotFoundException {
    private SberbankPostWriter openPostWriter(String packetId) {
        if (postWriters == null) postWriters = new Hashtable<String, SberbankPostWriter>();
        SberbankPostWriter PostWriter = postWriters.get(packetId);

        if (PostWriter == null) {
            String nextSberbankFileName = null;
            try {
                nextSberbankFileName = getNextSberbankFileName(postWriters.size() + 1, depCode);
            } catch (FlowException e) {
                System.out.println("Ошибка в имени файла");
                MyLogger.get().logMessage(processName, "Ошибка в имени файла"+e.toString());
                logger.info(processName +"Ошибка в имени файла"+e);
//                e.printStackTrace();
            }
            if (nextSberbankFileName == null) return null;

            try {
                PostWriter = new SberbankPostWriter(
                        properties.get("OUTPUT_DIRECTORY"),
                        nextSberbankFileName, processName);
            } catch (FlowException e) {
                System.out.println("Ошибка не можем записать в файл");
                MyLogger.get().logMessage(processName, "Ошибка не можем записать в файл"+e.toString());
                logger.info(processName +"Ошибка не можем записать в файл"+e);
//                e.printStackTrace();
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
            }
            postWriters.put(packetId, PostWriter);
            System.out.println("Имя файла: " + nextSberbankFileName);
            MyLogger.get().logMessage(processName, "Имя файла: " + nextSberbankFileName);
            logger.info(processName +" - Имя файла: " + nextSberbankFileName);
        }

        System.out.println("Подготовка к обработке пакета: " + packetId);
        MyLogger.get().logMessage(processName, "Подготовка к обработке пакета: " + packetId);
        logger.info(processName +" - Подготовка к обработке пакета: " + packetId);

//        System.out.println(PostWriter+"--------------------");

        return PostWriter;
    }

    private void writePost(String packetId, PostMoney r, OSP osp) {
        try {
//            System.out.println("8888888888888888");
            getPostWriter(packetId).writePost(r, osp);
//            System.out.println("---8888888888");
        } catch (FlowException e) {
            System.err.println("ОШИБКА: " + e.getMessage());
            MyLogger.get().logMessage(processName, "ОШИБКА: " + e.getMessage());
            logger.error(processName +" - ОШИБКА: " + e.getMessage());
        }
    }

    private SberbankPostWriter getPostWriter(String packetId) {
        return postWriters.get(packetId);
    }


    /**
     * @param fileCount //порядковый номер файла запроса за текущий день (кажется от 1 до Z)
     * @param depCode   //код отдела
     * @return имя файла запроса
     */
    private String getNextSberbankFileName(int fileCount, String depCode) throws FlowException {
        if (fileCount >= 35) return null; //достигнут лимит файлов для отправки

        Calendar inst = Calendar.getInstance();
//        String day = new DecimalFormat("00").format(inst.get(Calendar.DAY_OF_MONTH));
        String day = dd;
        String month = Integer.toHexString(inst.get(Calendar.MONTH) + 1);
        System.out.println("порядковый номер файла = " + fileCount);
        MyLogger.get().logMessage(processName, "порядковый номер файла = " + fileCount);
        logger.info(processName +" порядковый номер файла = " + fileCount);
        if (fileCount <= 9)
            return "p" + depCode + day + month + "." + fileCount + "ss";
//        return "p"+ kodOSP + depCode + "033" + "." + fileCount + "ss";
//        else if(fileCount<= 35 & fileCount >9){
//        return "p"+ kodOSP + depCode + "171" + "." + Character.toString((char) (fileCount-10 + 'a')) + "ss";
//        }else return "p"+ kodOSP + depCode + "033" + "." + fileCount + "ss";
//            return "p"+ kodOSP + depCode + "033" + "." + Character.toString((char) (fileCount-10 + 'a')) + "ss";
        return "p" + depCode + day + month + "." + Character.toString((char) (fileCount - 10 + 'a')) + "ss";

    }


}
