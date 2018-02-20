package service;

import beans.OSPBanks;
import beans.PostMoneyBanks;
import exceptions.FlowException;
import jdbc.BeanConnectBanks;
import jdbc.JDBCConnectionBanks;
import jdbc.TestConnect;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;
import services.MyLogger;

import javax.xml.transform.TransformerConfigurationException;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author: OnlyElena@mail.ru
 * Date: 6/20/16
 * Time: 4:46 PM
 */
public class PostServiceBanks {

    List<BeanConnectBanks> dataSourceBanks;
    LinkedHashMap<String, String> propertiesBanks;
    String ddBanks;
    String depCodeBanks;
    String processNameBanks;
    String whereBanks;
    Hashtable<String, BanksPostWriter> banksPostWriters;
    OSPBanks ospBanks;

    final static Logger logger = Logger.getLogger(PostServiceBanks.class);


    public PostServiceBanks(List<BeanConnectBanks> dataSourceBanks, LinkedHashMap<String, String> propertiesBanks, String ddBanks, String processNameBanks) throws FlowException {
        this.dataSourceBanks = dataSourceBanks;
        this.propertiesBanks = propertiesBanks;
        this.ddBanks = ddBanks;
        this.processNameBanks = processNameBanks;
        init();
    }

    /*В данном методе берем код отдела, его параметры и проверяем на доступность к работе БД*/
    private void init() throws FlowException {
        String smbConnect = dataSourceBanks.get(0).getUrl().substring(17, dataSourceBanks.get(0).getUrl().indexOf("/"));
        String queryBanks = "SELECT * FROM OSP";
        ospBanks = new OSPBanks();
//        JDBCConnection jdbcConnectionOld = new JDBCConnection();
        ResultSet resultSetBanks = JDBCConnectionBanks.getInstance().jdbcConnection(dataSourceBanks, processNameBanks, queryBanks);
        if (resultSetBanks == null) {
            System.out.println("Не удалось подключиться к БД, БД не доступна");
            MyLoggerBanks.get().logMessage(processNameBanks, "Не удалось подключиться к БД, БД не доступна");
            logger.error(processNameBanks + " Не удалось подключиться к БД, БД не доступна");
            throw new FlowException(processNameBanks + " Не удалось подключиться к БД, БД не доступна");

        }

        try {
            while (resultSetBanks.next()) {
                depCodeBanks = resultSetBanks.getObject("TERRITORY").toString() + resultSetBanks.getObject("DEPARTMENT").toString();
                System.out.println("\nУспешное подключение к БД: " + resultSetBanks.getObject("DIV_NAME"));
                MyLoggerBanks.get().logMessage(processNameBanks, "\nУспешное подключение к БД: " + resultSetBanks.getObject("DIV_NAME"));
                logger.info(processNameBanks + " - Успешное подключение к БД: " + resultSetBanks.getObject("DIV_NAME"));

                ospBanks.setName(resultSetBanks.getString("DIV_NAME"));
                if (ospBanks.getName().length() > 50) {
                    System.out.println("Слишком длинное имя ОСП");
                }
                ospBanks.setFullname(resultSetBanks.getString("DIV_FULLNAME"));
                ospBanks.setTerritory(resultSetBanks.getString("TERRITORY"));
                ospBanks.setDepartment(resultSetBanks.getString("DEPARTMENT"));
                ospBanks.setBik(resultSetBanks.getString("DIV_RECV_BIK"));
                ospBanks.setInn(resultSetBanks.getString("DIV_RECV_INN"));
                ospBanks.setKpp(resultSetBanks.getString("DIV_RECV_KPP"));
                ospBanks.setBankname(resultSetBanks.getString("DIV_RECV_BANKNAME"));
                if (ospBanks.getBankname().length() > 200) {
                    System.out.println("Слишком длинные реквизиты по перечислению банком");
                }
                ospBanks.setOkato(resultSetBanks.getString("DIV_RECV_OKATO"));
                ospBanks.setAccount(resultSetBanks.getString("DIV_RECV_CNT"));
                ospBanks.setLs(resultSetBanks.getString("LS"));
                ospBanks.setAddress(resultSetBanks.getString("DIV_ADR"));
                ospBanks.setOrfkKod(resultSetBanks.getString("ORFK_KOD").substring(2));
//                osp.setReceivTitle("УФК по Иркутской области (ОФК " + osp.getOrfkKod() + ", " + osp.getName() + " УФССП России по Ирк.области)");

//                osp.setReceivTitle("УФК по Кемеровской обл. (ОФК " + osp.getOrfkKod() + ", " + osp.getName() + " УФССП России по Кемеровской обл.)");
                String ReceivTitle = ("УФК(ОФК " + ospBanks.getOrfkKod() + ", " + ospBanks.getName() + " УФССП России)");
                if (ReceivTitle.length() < 94) {
                    ospBanks.setReceivTitle(ReceivTitle);
//                    System.out.println("не обрезает поле ReceivTitle");
                } else {
                    ospBanks.setReceivTitle(ReceivTitle.substring(0, 93) + ")");
//                    System.out.println("обрезает поле ReceivTitle");
                }
            }

        } catch (SQLException e) {
            System.out.println("БД не доступна Не удалось получить параметры ОСП: " + smbConnect);
            MyLoggerBanks.get().logMessage(processNameBanks, "БД не доступна Не удалось получить параметры ОСП: " + smbConnect + "\n" + e.getMessage());
            logger.error(processNameBanks + " - БД не доступна Не удалось получить параметры ОСП: " + smbConnect + "\n" + e);

            JDBCConnectionBanks.getInstance().jdbcClose(processNameBanks);
            throw new FlowException("Не удалось получить параметры ОСП: " + smbConnect);
        }
        if (depCodeBanks == null) {
            MyLoggerBanks.get().logMessage(processNameBanks, "Не удалось получить код отдела из БД " + smbConnect);
            logger.error(processNameBanks + " Не удалось получить код отдела из БД " + smbConnect);
            throw new FlowException("Не удалось получить код отдела из БД " + smbConnect);
        }
        try {
            resultSetBanks.close();
        } catch (SQLException e) {
            MyLoggerBanks.get().logMessage(processNameBanks, " Не удалось закрыть выборку");
            logger.error(processNameBanks + " Не удалось закрыть выборку");
            throw new FlowException("Не удалось закрыть выборку");
        }
        JDBCConnectionBanks.getInstance().jdbcClose(processNameBanks);
        boolean testConnect = new TestConnect().TestConnect("http://" + smbConnect + ":8080/pksp-server/");
        if (testConnect == false) {
            MyLoggerBanks.get().logMessage(processNameBanks, "Не удалось подключиться по тонкому клиенту, БД на ремонте");
            logger.error(processNameBanks + "Не удалось подключиться по тонкому клиенту, БД на ремонте");
            throw new FlowException("Не удалось подключиться по тонкому клиенту, БД на ремонте");
        }
    }

    public void processCreate() throws FlowException {
        initWhereCondition();

        List<String> packets = getPacketsNumbers();
        if (packets != null) {
            for (String packetId : packets) {
                BanksPostWriter banksPostWriter = openPostWriterBanks(packetId);
                //прерываем обработку, если достигли лимит файлов для запросов
                if (banksPostWriter != null) {
                    fetchPackets(packetId);
                    fetchPackets_omena(packetId);
                    banksPostWriter.close();
//                jdbcTemplate.execute("UPDATE EXT_RESTRICTION SET PROCEED = 1 WHERE PACK_ID = " + packetId);
                }
            }
        }
    }


    private void initWhereCondition() {
        String whereArr[] = {
                "ER.AGENT_AGREEMENT_CODE = '" + propertiesBanks.get("MVV_AGREEMENT_CODE") + "'",
                "ER.AGENT_CODE = '" + propertiesBanks.get("MVV_AGENT_CODE") + "'",
                "ER.AGENT_DEPT_CODE = '" + propertiesBanks.get("MVV_AGENT_DEPT_CODE") + "'",
                "PROCEED = 0",
                "PIEV_ATTACH = 1",
//                "ER.pack_id = 25501008845225",
//                "ENTITY.ENTT_TYPEID IN (2, 71, 95, 96, 97, 666)",
                "ER.doc_code in ('O_IP_ACT_GACCOUNT_MONEY', 'O_IP_ACT_GETCURRENCY', 'O_IP_ACT_ENDGACCOUNT_MONEY', 'O_IP_ACT_ARREST_ACCMONEY', 'O_IP_ACT_ENDARREST')"
//                "MD.usage is not null"
        };

        StringBuilder whereBuilder = new StringBuilder();

        whereBuilder.append(" WHERE ");
        whereBuilder.append(whereArr[0]);
        for (int i = 1; i < whereArr.length; ++i) {
            whereBuilder.append(" AND ").append(whereArr[i]);
        }

        whereBanks = whereBuilder.toString();

//        System.out.println("WHERE: " + where);
    }

    private List<String> getPacketsNumbers() throws FlowException {
        String queryBanks = "SELECT PACK_ID " +
                " FROM EXT_RESTRICTION ER\n" +
                "        INNER JOIN DOCUMENT D ON ER.IP_ID = D.ID\n" +
                "        INNER JOIN DOC_IP ON D.ID = DOC_IP.ID\n" +
                "        INNER JOIN DOC_IP_DOC DID ON D.ID = DID.ID\n" +
                "        INNER JOIN ENTITY ON DOC_IP.ID_DBTR = ENTITY.ENTT_ID\n" +

//                "        JOIN SENDLIST S ON S.SENDLIST_O_ID = ER.ACT_ID\n" +
//                "        JOIN O_IP ON O_IP.ID = S.SENDLIST_O_ID\n" +
//                "        INNER JOIN MVV_DATUM MD ON MD.DOCUMENT_ID = O_IP.ID" +

                whereBanks +
                " GROUP BY PACK_ID";

//        System.out.println(query);

        ResultSet resultSet = JDBCConnectionBanks.getInstance().jdbcConnection(dataSourceBanks, processNameBanks, queryBanks);
        if (resultSet == null) {
            System.out.println("Не удалось подключиться к БД, БД не доступна");
            MyLoggerBanks.get().logMessage(processNameBanks, "Не удалось подключиться к БД, БД не доступна");
            logger.error(processNameBanks + " - Не удалось подключиться к БД, БД не доступна");
            JDBCConnectionBanks.getInstance().jdbcClose(processNameBanks);
            return null;
        }
        LinkedList<String> packetsBanks = new LinkedList<String>();

        try {
            while (resultSet.next()) {
                packetsBanks.add(resultSet.getObject("PACK_ID").toString());
            }
            System.out.println("Пакетов для отправки: " + packetsBanks.size());
            MyLoggerBanks.get().logMessage(processNameBanks, "Пакетов для отправки: " + packetsBanks.size());
            logger.info(processNameBanks + " - Пакетов для отправки: " + packetsBanks.size());

        } catch (SQLException e) {
            System.out.println("Не удалось подключиться и собрать пакеты");
            MyLoggerBanks.get().logMessage(processNameBanks, "Не удалось подключиться и собрать пакеты");
            logger.error(processNameBanks + " - Не удалось подключиться и собрать пакеты");
            JDBCConnectionBanks.getInstance().jdbcClose(processNameBanks);
            return null;
        }

        try {
            resultSet.close();
        } catch (SQLException e) {
//            e.printStackTrace();
            MyLoggerBanks.get().logMessage(processNameBanks, " Не удалось закрыть выборку2");
            logger.error(processNameBanks+" Не удалось закрыть выборку2");
            throw new FlowException("Не удалось закрыть выборку2");
        }

        JDBCConnectionBanks.getInstance().jdbcClose(processNameBanks);
        return packetsBanks;
    }

    private void fetchPackets(String packetId) throws FlowException {
        boolean pievCopyServiceBanks;
//        Этот запрос для постановлений об обращении взыскания и наложении ареста
//        и снятия ареста с ДС и об отмене постановления об обращении взыскания на ДС
        String queryBanks = "SELECT DISTINCT " +
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
                " ENTITY.ENTT_TYPEID," +
                " ENTITY.ENTT_FULL_NAME, " +
                " ENTITY.entt_inn," +
                " entity.entt_kpp," +
                " ENTITY.ENTT_SURNAME,   /*--ФИО ДОЛЖНИКА*/" +
                " ENTITY.ENTT_FIRSTNAME," +
                " ENTITY.ENTT_PATRONYMIC," +
                " DID.ID_DBTR_BORNADR," +
                " DID.DBTR_BORN_YEAR," +
                " DID.ID_DBTR_BORN, PACK_ID," +
                " O_IP.ID_DBTR_ADR,  /*ПРОПИСКА ДОЛЖНИКА*/" +
                " MDA.BIC_BANK, " +
//                " ACC.ARREST_AMOUNT, " +
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
                "  join DATUM_LINK_OIP DLO ON DLO.DOC_ID=ER.ACT_ID\n" +
//                " JOIN MVV_DATUM MD ON MD.DOCUMENT_ID = O_IP.ID" +
//                "--   JOIN MVV_DATUM MD ON MD.DOCUMENT_ID = O_IP.ID\n" +
                "   JOIN MVV_DATUM MD ON MD.ID = DLO.DATUM_ID" +
                " JOIN MVV_DATUM_AVAILABILITY_ACC ACC ON ACC.ID = MD.ID" +
                " join MVV_DATUM_ACCOUNT MDA on MDA.ID = ACC.ID" +
                whereBanks +
                " AND DOC_CODE IN ('O_IP_ACT_GETCURRENCY', 'O_IP_ACT_ARREST_ACCMONEY', " +
                "'O_IP_ACT_GACCOUNT_MONEY','O_IP_ACT_ENDARREST', 'O_IP_ACT_CURRENCY_ROUB') " +
//                " AND ((MD.USAGE CONTAINING 'АРЕСТОВАТЬ' " +
//                "       AND ER.DOC_CODE IN ('O_IP_ACT_GETCURRENCY', 'O_IP_ACT_ARREST_ACCMONEY', 'O_IP_ACT_GACCOUNT_MONEY'))" +
//                "  OR ((MD.USAGE CONTAINING 'СНЯТЬ АРЕСТ' " +
//                "       AND ER.DOC_CODE IN (/*'O_IP_ACT_ENDGACCOUNT_MONEY',*/ 'O_IP_ACT_ENDARREST'))))" +
                " AND PACK_ID = " + packetId + " " +
                " order by ER.id";

        System.out.println("QUERY: " + queryBanks);

        List<PostMoneyBanks> post = new LinkedList<PostMoneyBanks>();
//        PostMoney r = new PostMoney();


        ResultSet resultSet = JDBCConnectionBanks.getInstance().jdbcConnection(dataSourceBanks, processNameBanks, queryBanks);
        if (resultSet == null) {
            System.out.println("Не удалось подключиться к БД, БД не доступна");
            MyLoggerBanks.get().logMessage(processNameBanks, "Не удалось подключиться к БД, БД не доступна");
            logger.error(processNameBanks + " - Не удалось подключиться к БД, БД не доступна");
            throw new FlowException(processNameBanks + " Не удалось подключиться к БД, БД не доступна");

        }

        try {
            while (resultSet.next()) {
                PostMoneyBanks rr = new PostMoneyBanks();
                rr.setId(getLong(resultSet, "ID"));
                rr.setBarcode(getString(resultSet, "BARCODE"));
                rr.setActtype(getString(resultSet, "DOC_CODE"));
                rr.setExecProcNumber(getString(resultSet, "ipno"));
                if (!((getString(resultSet, "ipno")).contains(getString(resultSet, "ID_NO"))) && ((getString(resultSet, "DOC_CODE")).equals("O_IP_ACT_ENDARREST"))) {
                    rr.setExecOldProcNumber(getString(resultSet, "ID_NO"));
//                    System.out.println(getString(resultSet, "ID_NO"));
                }
//                    r.setExecProcDate(getDateDDMMYYYY(row.get("ip_risedate")));
                rr.setExecProcDate(parseDate(getSqlDate(resultSet, "ip_risedate")));
                rr.setActNumber(getString(resultSet, "doc_number"));
                rr.setActDate(parseDate(getSqlDate(resultSet, "doc_date")));
                rr.setPriority(getInteger(resultSet, "priority_penalties"));
                rr.setExecActNum(getString(resultSet, "id_docno"));
                rr.setExecActDate(parseDate(getSqlDate(resultSet, "id_docdate")));
                rr.setExecActInitial(getString(resultSet, "id_court_name"));
                rr.setExecActInitialAddr(getString(resultSet, "id_court_adr"));
                rr.setBailiff(getString(resultSet, "ip_exec_prist_name"));
//                act.setSumm(rs.getString("ip_rest_debtsum"));
                rr.setSumm(getBigDecimal(resultSet, "total_dept_sum"));
                rr.setCreditorName(getString(resultSet, "id_crdr_name"));
                rr.setCreditorAddress(getString(resultSet, "id_crdr_adr"));
                rr.setAccountNumber(getString(resultSet, "ACC"));
                rr.setENTITY_TYPE(getLong(resultSet, "ENTT_TYPEID"));
                rr.setDolgnikOrg(getString(resultSet, "ENTT_FULL_NAME"));
                rr.setDolgnikInn(getString(resultSet,"entt_inn"));
                rr.setDolgnikKpp(getString(resultSet, "entt_kpp"));

                rr.setDebtorFirstName(getString(resultSet, "entt_firstname"));
                rr.setDebtorLastName(getString(resultSet, "entt_surname"));
                rr.setDebtorSecondName(getString(resultSet, "entt_patronymic"));
                System.out.println(rr.getDebtorFirstName()+" "+rr.getDebtorLastName()+" "+rr.getDebtorSecondName());
                rr.setDebtorBornAddres(getString(resultSet, "id_dbtr_bornadr"));
                rr.setDebtorAddres(getString(resultSet, "id_dbtr_adr"));
                rr.setDebtorBirthYear(getInteger(resultSet, "dbtr_born_year"));
                rr.setDebtorBirth(parseDate(getSqlDate(resultSet, "id_dbtr_born")));
                rr.setAccountCurreny(getString(resultSet, "CURRENCY_TYPE"));


                post.add(rr);

            }

        } catch (SQLException e) {
            System.out.println("БД не доступна: " + ospBanks.getName());
//            в случае возникновения исключительной ситуации, удаляем сформированные файлы
            System.err.println(e.getMessage() + " ОШИБКА в fetchPackets: ");
            MyLoggerBanks.get().logMessage(processNameBanks, e.getMessage() + " ОШИБКА в fetchPackets: ");
            logger.error(processNameBanks + " " + e.getMessage() + " ОШИБКА в fetchPackets: ");

            JDBCConnectionBanks.getInstance().jdbcClose(processNameBanks);
            throw new FlowException("Post " + e.getMessage() + " ОШИБКА в fetchPackets: ");
        }

        try {
            resultSet.close();
        } catch (SQLException e) {
//            e.printStackTrace();
            MyLoggerBanks.get().logMessage(processNameBanks, " Не удалось закрыть выборку3");
            logger.error(processNameBanks+" Не удалось закрыть выборку3");
            throw new FlowException("Не удалось закрыть выборку3");
        }
        JDBCConnectionBanks.getInstance().jdbcClose(processNameBanks);


            if (post != null) {
//            System.out.println("////////////////////"+post.size()+"//////////////////");
                for (PostMoneyBanks posBanks : post) {

                    //                piev_25161054884546.xml
//               pievCopyService = new PievCopyService("smb://10.38.63.130/d$/fssp/rezervcopy.txt");
                    pievCopyServiceBanks = PievCopyServiceBanks.PievCopyServiceBanks(processNameBanks, dataSourceBanks, propertiesBanks, dataSourceBanks.get(0).getUrlPIEV(), dataSourceBanks.get(0).getAuthentication(), propertiesBanks.get("OUTPUT_IP") + propertiesBanks.get("OUTPUT_DIRECTORY_SHARE"), propertiesBanks.get("AUTH_OUTPUT_DIRECTORY"), "piev_" + posBanks.getId() + ".xml.zip");
                    if (pievCopyServiceBanks == true) {
                        writePost(packetId, posBanks, ospBanks);
//                System.out.println("packetId " + packetId);
//                System.out.println("****" + r);
//                System.out.println("-----"+row+"----------");
//                    if (r == null) System.out.println("/////////");
//                    if (resultSet == null) System.out.println("+++++++");
//                getString(row, "asdfs");
//                System.out.println("----------------------------------------");
                        System.out.println("piev_" + getLong(resultSet, "ID") + ".xml.zip " + "Файл успешно скопирован и записан в реестр");
                        MyLoggerBanks.get().logMessage(processNameBanks, "piev_" + getLong(resultSet, "ID") + ".xml.zip " + "Файл успешно скопирован и записан в реестр");
                    } else {
                        System.out.println("ИП " + posBanks.getExecProcNumber() + " !!!!!!!!!!!!!!!! файл не скопирован!!!!!!!!!!!!");
                        MyLoggerBanks.get().logMessage(processNameBanks, "ИП " + posBanks.getExecProcNumber() + " !!!!!!!!!!!!!!!! файл не скопирован!!!!!!!!!!!!");
                        logger.error(processNameBanks + " - ИП " + posBanks.getExecProcNumber() + " !!!!!!!!!!!!!!!! файл не скопирован!!!!!!!!!!!!");
                    }
                }

                System.out.println("*--Запрос для постановлений об обращении взыскания и наложении/снятии ареста--*");
                MyLoggerBanks.get().logMessage(processNameBanks, "*--Запрос для постановлений об обращении взыскания и наложении/снятии ареста--*");
                System.out.println("*********" + post.size());
                MyLoggerBanks.get().logMessage(processNameBanks, "*********" + post.size());

            }


        String query_up = "UPDATE EXT_RESTRICTION SET PROCEED = 1 " +
                "WHERE DOC_CODE IN ('O_IP_ACT_GETCURRENCY', 'O_IP_ACT_ARREST_ACCMONEY', " +
                "'O_IP_ACT_GACCOUNT_MONEY','O_IP_ACT_ENDARREST') AND PACK_ID = " + packetId;

//        if (!post.isEmpty()) {
        JDBCConnectionBanks.getInstance().jdbcUpdateBanks(dataSourceBanks, processNameBanks, query_up);
        System.out.println("update_restriction");
//        } else {
//            JDBCConnection.getInstance().jdbcClose();
//        }
    }

    private void fetchPackets_omena(String packetId) throws FlowException {
        boolean pievCopyServiceBanks;
//      Запрос об отмене постановления об обращении взыскания на ДС
        String queryBanks_sn = "SELECT DISTINCT " +
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
                " ENTITY.ENTT_TYPEID," +
                " ENTITY.ENTT_FULL_NAME, " +
                " ENTITY.entt_inn," +
                " entity.entt_kpp," +
                " ENTITY.ENTT_SURNAME,   /*--ФИО ДОЛЖНИКА*/" +
                " ENTITY.ENTT_FIRSTNAME," +
                " ENTITY.ENTT_PATRONYMIC," +
                " DID.ID_DBTR_BORNADR," +
                " DID.DBTR_BORN_YEAR," +
                " DID.ID_DBTR_BORN, PACK_ID," +
                " O_IP.ID_DBTR_ADR,  /*ПРОПИСКА ДОЛЖНИКА*/" +
                " MDA.BIC_BANK, " +
//                " ACC.ARREST_AMOUNT, " +
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
                " JOIN O_IP_ACT_ENDGACCOUNT_MONEY GACC  ON GACC.ID = DOC.ID" +
                "  join DATUM_LINK_OIP DLO ON DLO.DOC_ID=ER.ACT_ID\n" +
//                " JOIN MVV_DATUM MD ON MD.DOCUMENT_ID = O_IP.ID" +
//                "--   JOIN MVV_DATUM MD ON MD.DOCUMENT_ID = O_IP.ID\n" +
                "   JOIN MVV_DATUM MD ON MD.ID = DLO.DATUM_ID" +
                " JOIN MVV_DATUM_AVAILABILITY_ACC ACC ON ACC.ID = MD.ID" +
                " join MVV_DATUM_ACCOUNT MDA on MDA.ID = ACC.ID" +
                whereBanks +
                " AND DOC_CODE IN ('O_IP_ACT_ENDGACCOUNT_MONEY', 'O_IP_ACT_CURRENCY_ROUB') " +
//                " AND ((MD.USAGE CONTAINING 'СНЯТЬ АРЕСТ' " +
//                "       AND ER.DOC_CODE IN ('O_IP_ACT_ENDGACCOUNT_MONEY')))" +
                " AND PACK_ID = " + packetId + " " +
                " order by ER.id";
//        System.out.println("QUERY_SN: " + query_sn);


        List<PostMoneyBanks> post = new LinkedList<PostMoneyBanks>();
//        PostMoney r = new PostMoney();

        ResultSet resultSet = JDBCConnectionBanks.getInstance().jdbcConnection(dataSourceBanks, processNameBanks, queryBanks_sn);
        if (resultSet == null) {
            System.out.println("Не удалось подключиться к БД, БД не доступна(otmena)");
            MyLoggerBanks.get().logMessage(processNameBanks, "Не удалось подключиться к БД, БД не доступна(otmena)");
            logger.error(processNameBanks + " - Не удалось подключиться к БД, БД не доступна(otmena)");
            throw new FlowException(processNameBanks + " Не удалось подключиться к БД, БД не доступна");
        }

        try {
            while (resultSet.next()) {
                PostMoneyBanks rr = new PostMoneyBanks();
                rr.setId(getLong(resultSet, "ID"));
                rr.setBarcode(getString(resultSet, "BARCODE"));
                rr.setActtype(getString(resultSet, "DOC_CODE"));
                rr.setExecProcNumber(getString(resultSet, "ipno"));
//                if (!resultSet.getObject("ipno").toString().contains(resultSet.getObject("ID_NO").toString()) && resultSet.getObject("DOC_CODE").equals("O_IP_ACT_ENDGACCOUNT_MONEY")) {
//                    r.setExecOldProcNumber(getString(resultSet, "ID_NO"));
//                }
                if (!((getString(resultSet, "ipno")).contains(getString(resultSet, "ID_NO"))) && ((getString(resultSet, "DOC_CODE")).equals("O_IP_ACT_ENDGACCOUNT_MONEY"))) {
                    rr.setExecOldProcNumber(getString(resultSet, "ID_NO"));
//                    System.out.println(getString(resultSet, "ID_NO"));
                }
//                    r.setExecProcDate(getDateDDMMYYYY(row.get("ip_risedate")));
                rr.setExecProcDate(parseDate(getSqlDate(resultSet, "ip_risedate")));
                rr.setActNumber(getString(resultSet, "doc_number"));
                rr.setActDate(parseDate(getSqlDate(resultSet, "doc_date")));
                rr.setPriority(getInteger(resultSet, "priority_penalties"));
                rr.setExecActNum(getString(resultSet, "id_docno"));
                rr.setExecActDate(parseDate(getSqlDate(resultSet, "id_docdate")));
                rr.setExecActInitial(getString(resultSet, "id_court_name"));
                rr.setExecActInitialAddr(getString(resultSet, "id_court_adr"));
                rr.setBailiff(getString(resultSet, "ip_exec_prist_name"));
//                act.setSumm(rs.getString("ip_rest_debtsum"));
                rr.setSumm(getBigDecimal(resultSet, "GACCOUNT_MONEY_TOTAL_DEPT_SUM"));
                rr.setCreditorName(getString(resultSet, "id_crdr_name"));
                rr.setCreditorAddress(getString(resultSet, "id_crdr_adr"));
                rr.setAccountNumber(getString(resultSet, "ACC"));

                rr.setENTITY_TYPE(getLong(resultSet, "ENTT_TYPEID"));
                rr.setDolgnikOrg(getString(resultSet, "ENTT_FULL_NAME"));
                rr.setDolgnikInn(getString(resultSet,"entt_inn"));
                rr.setDolgnikKpp(getString(resultSet, "entt_kpp"));

                rr.setDebtorFirstName(getString(resultSet, "entt_firstname"));
                rr.setDebtorLastName(getString(resultSet, "entt_surname"));
                rr.setDebtorSecondName(getString(resultSet, "entt_patronymic"));
                rr.setDebtorBornAddres(getString(resultSet, "id_dbtr_bornadr"));
                rr.setDebtorAddres(getString(resultSet, "id_dbtr_adr"));
                rr.setDebtorBirthYear(getInteger(resultSet, "dbtr_born_year"));
                rr.setDebtorBirth(parseDate(getSqlDate(resultSet, "id_dbtr_born")));
                rr.setAccountCurreny(getString(resultSet, "CURRENCY_TYPE"));

//                System.out.println(r);

                post.add(rr);
            }
        } catch (SQLException e) {
            System.out.println("БД не доступна: " + ospBanks.getName());
//            в случае возникновения исключительной ситуации, удаляем сформированные файлы
            System.err.println(e.getMessage() + "ОШИБКА в fetchPackets_otmena: ");
            MyLoggerBanks.get().logMessage(processNameBanks, e.getMessage() + "ОШИБКА в fetchPackets_otmena: ");
            logger.error(processNameBanks + " - " + e.getMessage() + "ОШИБКА в fetchPackets_otmena: ");

            JDBCConnectionBanks.getInstance().jdbcClose(processNameBanks);
            throw new FlowException(processNameBanks + " " + e.getMessage() + "ОШИБКА в fetchPackets_otmena: ");
        }

        try {
            resultSet.close();
        } catch (SQLException e) {
//            e.printStackTrace();
            MyLogger.get().logMessage(processNameBanks, " Не удалось закрыть выборку4");
            logger.error(processNameBanks+" Не удалось закрыть выборку4");
            throw new FlowException("Не удалось закрыть выборку4");
        }
        JDBCConnectionBanks.getInstance().jdbcClose(processNameBanks);

        if (post != null) {
            for (PostMoneyBanks posBanks : post) {


                pievCopyServiceBanks = PievCopyServiceBanks.PievCopyServiceBanks(processNameBanks, dataSourceBanks, propertiesBanks, dataSourceBanks.get(0).getUrlPIEV(), dataSourceBanks.get(0).getAuthentication(), propertiesBanks.get("OUTPUT_IP") + propertiesBanks.get("OUTPUT_DIRECTORY_SHARE"), propertiesBanks.get("AUTH_OUTPUT_DIRECTORY"), "piev_" + posBanks.getId() + ".xml.zip");
                if (pievCopyServiceBanks == true) {
                    writePost(packetId, posBanks, ospBanks);
                    System.out.println("piev_" + getLong(resultSet, "ID") + ".xml.zip " + "Файл успешно скопирован и записан в реестр");
                    MyLoggerBanks.get().logMessage(processNameBanks, "piev_" + getLong(resultSet, "ID") + ".xml.zip " + "Файл успешно скопирован и записан в реестр");
                } else {
                    System.out.println("ИП " + posBanks.getExecProcNumber() + " !!!!!!!!!!!!!!!! файл не скопирован!!!!!!!!!!!!");
                    MyLoggerBanks.get().logMessage(processNameBanks, "ИП " + posBanks.getExecProcNumber() + " !!!!!!!!!!!!!!!! файл не скопирован!!!!!!!!!!!!");
                    logger.error(processNameBanks + " - ИП " + posBanks.getExecProcNumber() + " !!!!!!!!!!!!!!!! файл не скопирован!!!!!!!!!!!!");
                }
            }
            System.out.println("*--Запрос об отмене постановления об обращении взыскания на ДС --*");
            MyLoggerBanks.get().logMessage(processNameBanks, "*--Запрос об отмене постановления об обращении взыскания на ДС --*");
            System.out.println("*************" + post.size());
            MyLoggerBanks.get().logMessage(processNameBanks, "*************" + post.size());
            System.out.println("---------------------------------------------------------");
            MyLoggerBanks.get().logMessage(processNameBanks, "---------------------------------------------------------");
        }



    String query_up_otmena = "UPDATE EXT_RESTRICTION SET PROCEED = 1 " +
                "WHERE DOC_CODE IN ('O_IP_ACT_ENDGACCOUNT_MONEY') AND PACK_ID = " + packetId;
//        if (!post.isEmpty()) {
        JDBCConnectionBanks.getInstance().jdbcUpdateBanks(dataSourceBanks, processNameBanks, query_up_otmena);
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
            MyLoggerBanks.get().logMessage(processNameBanks, "Field: " + filedName + ", are not Integer type. Actual type: " + o.getClass());
            logger.error("Field: " + filedName + ", are not Integer type. Actual type: " + o.getClass());
            return o.toString();
        }
        if (o == null) return null;

        if (o instanceof Integer) {
            return o.toString();
        } else {
            System.err.println("Field: " + filedName + ", are not Integer type. Actual type: " + o.getClass());
            MyLoggerBanks.get().logMessage(processNameBanks, "Field: " + filedName + ", are not Integer type. Actual type: " + o.getClass());
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
            MyLoggerBanks.get().logMessage(processNameBanks, "Field: " + filedName + ", are not String type. Actual type: " + o.getClass());
            logger.error("Field: " + filedName + ", are not String type. Actual type: " + o.getClass());
            return o.toString();
        }
        if (o == null) return null;

        if (o instanceof String) {
            return (String) o;
        } else {
            System.err.println("Field: " + filedName + ", are not String type. Actual type: " + o.getClass());
            MyLoggerBanks.get().logMessage(processNameBanks, "Field: " + filedName + ", are not String type. Actual type: " + o.getClass());
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
            MyLoggerBanks.get().logMessage(processNameBanks, "Field: " + filedName + ", are not String type. Actual type: " + o.getClass());
            logger.error("Field: " + filedName + ", are not String type. Actual type: " + o.getClass());
            throw new FlowException("Can't convert Long");
        }
        if (o == null) return null;

        if (o instanceof Long) {
            return (Long) o;
        } else {
            System.err.println("Field: " + filedName + ", are not String type. Actual type: " + o.getClass());
            MyLoggerBanks.get().logMessage(processNameBanks, "Field: " + filedName + ", are not String type. Actual type: " + o.getClass());
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
            MyLoggerBanks.get().logMessage(processNameBanks, "Field: " + filedName + ", are not java.sql.Date type. Actual type: " + o.getClass());
            logger.error("Field: " + filedName + ", are not java.sql.Date type. Actual type: " + o.getClass());
            throw new FlowException("Can't convert data");
        }
        if (o == null) return null;

        if (o instanceof java.sql.Date) {
            return (java.sql.Date) o;
        } else {
            System.err.println("Field: " + filedName + ", are not java.sql.Date type. Actual type: " + o.getClass());
            MyLoggerBanks.get().logMessage(processNameBanks, "Field: " + filedName + ", are not java.sql.Date type. Actual type: " + o.getClass());
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
            MyLoggerBanks.get().logMessage(processNameBanks, "Field: " + filedName + ", are not bigDecimal type. Actual type: " + o.getClass());
            logger.error("Field: " + filedName + ", are not bigDecimal type. Actual type: " + o.getClass());
            return "0";
        }
        if (o == null) return "0";

        if (o instanceof BigDecimal) {
            return o.toString();
        } else {
            System.err.println("Field: " + filedName + ", are not bigDecimal type. Actual type: " + o.getClass());
            MyLoggerBanks.get().logMessage(processNameBanks, "Field: " + filedName + ", are not bigDecimal type. Actual type: " + o.getClass());
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
     * @param packetIdBanks
     * @throws FlowException
     * @throws TransformerConfigurationException
     * @throws SAXException
     * @throws FileNotFoundException
     */
//    private SberbankPostWriter openPostWriter(String packetId) throws FlowException, TransformerConfigurationException, SAXException, FileNotFoundException {
    private BanksPostWriter openPostWriterBanks(String packetIdBanks) {
        if (banksPostWriters == null) banksPostWriters = new Hashtable<String, BanksPostWriter>();
        BanksPostWriter PostWriterBanks = banksPostWriters.get(packetIdBanks);

        if (PostWriterBanks == null) {
            String nextBanksFileName = null;
            try {
                nextBanksFileName = getNextBanksFileName(banksPostWriters.size() + 1, depCodeBanks);
            } catch (FlowException e) {
                System.out.println("Ошибка в имени файла");
                MyLoggerBanks.get().logMessage(processNameBanks, "Ошибка в имени файла" + e.toString());
                logger.info(processNameBanks + "Ошибка в имени файла" + e);
            }
            if (nextBanksFileName == null) return null;

            try {
                PostWriterBanks = new BanksPostWriter(
                        propertiesBanks.get("OUTPUT_DIRECTORY"),
                        nextBanksFileName, processNameBanks);
            } catch (FlowException e) {
                System.out.println("Ошибка не можем записать в файл");
                MyLoggerBanks.get().logMessage(processNameBanks, "Ошибка не можем записать в файл" + e.toString());
                logger.info(processNameBanks + "Ошибка не можем записать в файл" + e);
            } catch (TransformerConfigurationException e) {
                MyLoggerBanks.get().logMessage(processNameBanks, e.toString());
                logger.error(processNameBanks + e.toString());
            } catch (SAXException e) {
                MyLoggerBanks.get().logMessage(processNameBanks, e.toString());
                logger.error(processNameBanks + e);
            }
            banksPostWriters.put(packetIdBanks, PostWriterBanks);
            System.out.println("Имя файла: " + nextBanksFileName);
            MyLoggerBanks.get().logMessage(processNameBanks, "Имя файла: " + nextBanksFileName);
            logger.info(processNameBanks + " - Имя файла: " + nextBanksFileName);
        }

        System.out.println("Подготовка к обработке пакета: " + packetIdBanks);
        MyLoggerBanks.get().logMessage(processNameBanks, "Подготовка к обработке пакета: " + packetIdBanks);
        logger.info(processNameBanks + " - Подготовка к обработке пакета: " + packetIdBanks);

        return PostWriterBanks;
    }

    private void writePost(String packetId, PostMoneyBanks r, OSPBanks osp) {
        try {
            getPostWriterBanks(packetId).writePostBanks(r, osp);
        } catch (FlowException e) {
            System.err.println("ОШИБКА: " + e.getMessage());
            MyLoggerBanks.get().logMessage(processNameBanks, "ОШИБКА: " + e.getMessage());
            logger.error(processNameBanks + " - ОШИБКА: " + e.getMessage());
        }
    }

    private BanksPostWriter getPostWriterBanks(String packetId) {
        return banksPostWriters.get(packetId);
    }


    /**
     * @param fileCount //порядковый номер файла запроса за текущий день (кажется от 1 до Z)
     * @param depCode   //код отдела
     * @return имя файла запроса
     */
    private String getNextBanksFileName(int fileCount, String depCode) throws FlowException {
        if (fileCount >= 999) return null; //достигнут лимит файлов для отправки

        Calendar inst = Calendar.getInstance();
//        String day = new DecimalFormat("00").format(inst.get(Calendar.DAY_OF_MONTH));
        String day = ddBanks;
        String month = Integer.toHexString(inst.get(Calendar.MONTH) + 1);
        System.out.println("порядковый номер файла = " + fileCount);
        MyLoggerBanks.get().logMessage(processNameBanks, "порядковый номер файла = " + fileCount);
        logger.info(processNameBanks + " порядковый номер файла = " + fileCount);
//        if (fileCount <= 9)
            return "p" + depCode + day + month + "." + fileCount + "ss";
//            PKKKKDDM.NNNSS,
//        return "p"+ kodOSP + depCode + "033" + "." + fileCount + "ss";
//        else if(fileCount<= 35 & fileCount >9){
//        return "p"+ kodOSP + depCode + "171" + "." + Character.toString((char) (fileCount-10 + 'a')) + "ss";
//        }else return "p"+ kodOSP + depCode + "033" + "." + fileCount + "ss";
//            return "p"+ kodOSP + depCode + "033" + "." + Character.toString((char) (fileCount-10 + 'a')) + "ss";
//        return "p" + depCode + day + month + "." + Character.toString((char) (fileCount - 10 + 'a')) + "ss";

    }


}
