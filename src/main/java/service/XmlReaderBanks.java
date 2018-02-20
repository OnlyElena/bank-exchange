package service;

import beans.ResponseBanks;
import exceptions.EndDocumentException;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

/**
 * @author: OnlyElena@mail.ru
 * Date: 6/19/16
 * Time: 1:54 AM
 */
public class XmlReaderBanks extends DefaultHandler {

    ResponseBanks currentResponseBanks;
    boolean isTextReady = false;
    F field = F.unknown;

//    String depCode;

    String previosReqId = "";

    public XmlReaderBanks() {
//        this.depCode = depCode;
    }

    @Override
    public void startDocument() throws SAXException {
        super.startDocument();
//        System.out.println("Start document " + depCode);
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attr) throws SAXException {

        //сбрасываем поле
        field = F.unknown;

//        if (localName.equals("otvet")|| localName.equals("Otvet")) {
        if (localName.equalsIgnoreCase("otvet")) {

                //дошли до нового ответа
            if (currentResponseBanks != null) {
//                importResponse(currentResponse);
                onResponce(currentResponseBanks);
            }
            currentResponseBanks = new ResponseBanks();
        } else {
            //выставляем соответствие тэга к полю
            F[] values = F.values();
            for (F value : values) {
                if (localName.equals(value.toString())) {
                    field = value;
                    //выходим из цикла, соответствие найдено
                    break;
                }
            }
        }
    }

    Hashtable<String, List<ResponseBanks>> responcesBanks = new Hashtable<String, List<ResponseBanks>>();

    public void onResponce(ResponseBanks response) {
        if (response == null) return;

        String id = response.getRequestId();
        List<ResponseBanks> list = responcesBanks.get(id);
        if (list == null) {
            list = new LinkedList<ResponseBanks>();
            responcesBanks.put(id, list);
        }
        list.add(response);

    }

    public Hashtable<String, List<ResponseBanks>> getResponcesBanks() {
        return responcesBanks;
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        field = F.unknown;
        if (localName.equalsIgnoreCase("Response")) {
            onResponce(currentResponseBanks);
//            System.out.println("Все ответы в файле обработаны");
            System.out.println("Все ответы в файле прочитаны");
            MyLoggerBanks.get().logMessage("Resp", "Все ответы в файле прочитаны");
            throw new EndDocumentException();
        }
    }

    @Override
    public void characters(char[] chars, int i, int i1) throws SAXException {

        //выделяем текст
        String res = new String(chars, i, i1);

        switch (field) {
            case unknown:
                break;
            case RESULT:
                try {
                    currentResponseBanks.setResult(Integer.parseInt(new String(chars, i, i1)));
                } catch (NumberFormatException e) {
                    throw new SAXParseException("Can't parse RESULT tag", null);
                }
                break;
            case File_Name:
                currentResponseBanks.setRequestFileName(res);
                break;
            case Req_ID:
                currentResponseBanks.setRequestId(res);
                break;
            case Isp_Num:
                currentResponseBanks.setIsp_Num(res);
                break;
            case User_ID:
                currentResponseBanks.setUserId(res);
                break;
            case Req_Type:
                currentResponseBanks.setRequestType(res);
                break;
            case File_Exp_Name:
                currentResponseBanks.setResponseFileName(res);
                break;
            case Resp_ID:
                currentResponseBanks.setResponseId(res);
                break;
            case Req_Date:
                currentResponseBanks.setRequestDate(res);
                break;
            case Req_Time:
                currentResponseBanks.setRequestTime(res);
                break;
            case INN:
                //пустое не сохраняем
                if (res.isEmpty()) break;
                if (res.equals("-")) break;
                currentResponseBanks.setINN(res);
                break;
            case OSB_Name:
                currentResponseBanks.setOsbName(res);
                break;
            case OSB_Addr:
                currentResponseBanks.setOsbAddress(res);
                break;
            case OSB_Num:
                currentResponseBanks.setOsbNumber(res);
                break;
            case OSB_Tel:
                currentResponseBanks.setOsbPhoneNumber(res);
                break;

            case Organization_Name:
                currentResponseBanks.setOsbName(res);
                break;
            case Organization_Addr:
                currentResponseBanks.setOsbAddress(res);
                break;
//            case OSB_Num:
//                currentResponse.setOsbNumber(res);
//                break;
            case Organization_Tel:
                currentResponseBanks.setOsbPhoneNumber(res);
                break;
            case BIC:
//                if (res.isEmpty()) break;
//                if (res.equals("-")) break;
                currentResponseBanks.setOsbBIC(res);
                break;
            case Account:
                if (res.isEmpty()) break;
                if (res.equals("-")) break;
                currentResponseBanks.setDebtorAccount(res);
                break;
            case Op_Date:
                if (res.isEmpty()) break;
                if (res.equals("-")) break;
                currentResponseBanks.setAccountCreateDate(res);
                break;
            case Balance:
                currentResponseBanks.setAccountBalance(res);
                break;
            case Vid_vkl:
                if (res.isEmpty()) break;
                if (res.equals("-")) break;
                currentResponseBanks.setAccountDescr(res);
                break;
            case Val_vkl:
                if (res.isEmpty()) break;
                if (res.equals("-")) break;
                currentResponseBanks.setAccountCurreny(res);
                break;
            case Errors:
                currentResponseBanks.setErrors(res);
                break;
        }

//        StringBuffer buf = new StringBuffer();
//        buf.append(chars, i, i1);
//        System.out.println(buf.toString());
    }


//    public void importResponse(SberbankResponse resp) {
//        ColumnQuery<Long, Long, String> q = HFactory.createColumnQuery(D.ks, D.ls, D.ls, D.ss);
//        q.setColumnFamily(S.cfDocs);
//        q.setKey(Long.parseLong("25" + depCode + resp.getRequestId()));
//        q.setName(Long.parseLong("25" + depCode + resp.getRequestId()));
//        HColumn<Long, String> column = q.execute().get();
//
//        if (column == null) {
//            System.err.println("ERROR: can't find for " + "25" + depCode + resp.getRequestId());
//            return;
//        }
//
//        SberbankRequest request = StrUtil.fromJson(column.getValue(), SberbankRequest.class);
//        request.setResponse(null);
//
//        if (request.getResponses() == null) {
//            request.setResponses(new LinkedList<SberbankResponse>());
//        }
//
//        //это дополнительный счет должника
//        if (previosReqId.equals(resp.getRequestId())) {
//            request.getResponses().add(resp);
//        } else {
//            //затираем все ответы полученые ранее
//            request.setResponses(new LinkedList<SberbankResponse>());
//            request.getResponses().add(resp);
//        }
//
//        previosReqId = request.getRequestId();
//
//        Mutator<Long> m = HFactory.createMutator(D.ks, D.ls);
//
//        m.insert(request.getExecutoryProcessId(), S.cfDocs, HFactory.createColumn(request.getExecutoryProcessId(), StrUtil.toJson(request), D.ls, D.ss));
//
//        //Сбор статистики
//        String accountDescr = resp.getAccountDescr();
//        Stat.get().update(request.getDepartment() + " " + accountDescr, 1);
//
////        System.out.println("update " + request.getExecutoryProcessId());
//    }

    private enum F {
        unknown,
        RESULT,
        File_Name,
        Req_ID,
        Isp_Num,
        User_ID,
        Req_Type,
        File_Exp_Name,
        Resp_ID,
        Req_Date,
        Req_Time,
        INN,
        OSB_Name,
        OSB_Addr,
        OSB_Num,
        OSB_Tel,

        Organization_Name,
        Organization_Addr,
        //        OSB_Num,
        Organization_Tel,

        BIC,
        Account,
        Op_Date,
        Balance,
        Vid_vkl,
        Val_vkl,
        Errors
    }
}
