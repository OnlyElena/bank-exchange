package services;
import beans.SberbankResponse;
import exceptions.EndDocumentException;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

/**
 * User: Andrey V. Panov
 * Date: 8/10/11
 * Time: 9:22 AM
 */
public class SberbankXmlReader extends DefaultHandler {

    SberbankResponse currentResponse;
    boolean isTextReady = false;
    F field = F.unknown;

//    String depCode;

    String previosReqId = "";

    public SberbankXmlReader() {
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

        if (localName.equals("otvet")) {
            //дошли до нового ответа
            if (currentResponse != null) {
//                importResponse(currentResponse);
                onResponce(currentResponse);
            }
            currentResponse = new SberbankResponse();
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

    Hashtable<String, List<SberbankResponse>> responces = new Hashtable<String, List<SberbankResponse>>();

    public void onResponce(SberbankResponse response) {
        if (response == null) return;

        String id = response.getRequestId();
        List<SberbankResponse> list = responces.get(id);
        if (list == null) {
            list = new LinkedList<SberbankResponse>();
            responces.put(id, list);
        }
        list.add(response);

    }

    public Hashtable<String, List<SberbankResponse>> getResponces() {
        return responces;
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        field = F.unknown;
        if (localName.equals("Response")) {
            onResponce(currentResponse);
//            System.out.println("Все ответы в файле обработаны");
            System.out.println("Все ответы в файле прочитаны");
            MyLogger.get().logMessage("Resp", "Все ответы в файле прочитаны");
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
                    currentResponse.setResult(Integer.parseInt(new String(chars, i, i1)));
                } catch (NumberFormatException e) {
                    throw new SAXParseException("Can't parse RESULT tag", null);
                }
                break;
            case File_Name:
                currentResponse.setRequestFileName(res);
                break;
            case Req_ID:
                currentResponse.setRequestId(res);
                break;
            case User_ID:
                currentResponse.setUserId(res);
                break;
            case Req_Type:
                currentResponse.setRequestType(res);
                break;
            case File_Exp_Name:
                currentResponse.setResponseFileName(res);
                break;
            case Resp_ID:
                currentResponse.setResponseId(res);
                break;
            case Req_Date:
                currentResponse.setRequestDate(res);
                break;
            case Req_Time:
                currentResponse.setRequestTime(res);
                break;
            case INN:
                //пустое не сохраняем
                if (res.equals("-")) break;
                currentResponse.setINN(res);
                break;
            case OSB_Name:
                currentResponse.setOsbName(res);
                break;
            case OSB_Addr:
                currentResponse.setOsbAddress(res);
                break;
            case OSB_Num:
                currentResponse.setOsbNumber(res);
                break;
            case OSB_Tel:
                currentResponse.setOsbPhoneNumber(res);
                break;
            case BIC:
                currentResponse.setOsbBIC(res);
                break;
            case Account:
                currentResponse.setDebtorAccount(res);
                break;
            case Op_Date:
                currentResponse.setAccountCreateDate(res);
                break;
            case Balance:
                currentResponse.setAccountBalance(res);
                break;
            case Vid_vkl:
                currentResponse.setAccountDescr(res);
                break;
            case Val_vkl:
                currentResponse.setAccountCurreny(res);
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
        BIC,
        Account,
        Op_Date,
        Balance,
        Vid_vkl,
        Val_vkl
    }
}
