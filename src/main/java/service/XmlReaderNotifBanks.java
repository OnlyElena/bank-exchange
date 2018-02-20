package service;

import beans.NotifBeanBanks;
import exceptions.EndDocumentException;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

/**
 * @author: OnlyElena@mail.ru
 * Date: 7/9/16
 * Time: 11:43 PM
 */
public class XmlReaderNotifBanks extends DefaultHandler {
    NotifBeanBanks currentNotifBanks;
    F field = F.unknown;

    public XmlReaderNotifBanks() {
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
        if (localName.equalsIgnoreCase("notification")) {

            //дошли до нового ответа
            if (currentNotifBanks != null) {
//                importResponse(currentResponse);
                onResponce(currentNotifBanks);
            }
            currentNotifBanks = new NotifBeanBanks();
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

    Hashtable<String, List<NotifBeanBanks>> notifBanks = new Hashtable<String, List<NotifBeanBanks>>();

    public void onResponce(NotifBeanBanks notif) {
        if (notif == null) return;

        String id = notif.getId();
        List<NotifBeanBanks> list = notifBanks.get(id);
        if (list == null) {
            list = new LinkedList<NotifBeanBanks>();
            notifBanks.put(id, list);
        }
        list.add(notif);

    }

    public Hashtable<String, List<NotifBeanBanks>> getNotifBanks() {
        return notifBanks;
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        field = F.unknown;
        if (localName.equalsIgnoreCase("Response")) {
            onResponce(currentNotifBanks);
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
            case id:
                currentNotifBanks.setId(res);
                break;
            case execProcNumber:
                currentNotifBanks.setExecProcNumber(res);
                break;
            case debtorFirstName:
                currentNotifBanks.setDebtorFirstName(res);
                break;
            case debtorLastName:
                currentNotifBanks.setDebtorLastName(res);
                break;
            case debtorSecondName:
                currentNotifBanks.setDebtorSecondName(res);
                break;
            case accountNumber:
                currentNotifBanks.setAccountNumber(res);
                break;
            case summ:
                currentNotifBanks.setSumm(res);
                break;
            case ProcNumberState:
                currentNotifBanks.setProcNumberState(res);
                break;
            case Status:
                currentNotifBanks.setStatus(res);
                break;
            case ActNumber:
                currentNotifBanks.setActNumber(res);
                break;
        }

//        StringBuffer buf = new StringBuffer();
//        buf.append(chars, i, i1);
//        System.out.println(buf.toString());
    }



    private enum F {
        unknown,
        id,
        execProcNumber,
        debtorFirstName,
        debtorLastName,
        debtorSecondName,
        accountNumber,
        summ,
        ProcNumberState,
        Status,
        ActNumber

    }

}

