package jdbc;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

/**
 * @author: OnlyElena@mail.ru
 * Date: 6/18/16
 * Time: 10:49 PM
 */
public class ParserBeanConnectBanks extends DefaultHandler {

    BeanConnectBanks currentBeanConnectBanks;
    int index;

    public ParserBeanConnectBanks() {
    }

    @Override
    public void startDocument() throws SAXException {
        super.startDocument();
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attr) throws SAXException {

        if (localName.equals("bean")){
            index = index+1;
            if (currentBeanConnectBanks != null) {
                onResponce(currentBeanConnectBanks);
            }
            currentBeanConnectBanks = new BeanConnectBanks();
            for (int att = 0; att < attr.getLength(); att++) {
                String attName = attr.getQName(att);
                if (attName.equals("id")){
                    currentBeanConnectBanks.setBeanId(attr.getValue(attName));
                }
            }


        }

        if (localName.equals("property")){
            for (int att = 0; att < attr.getLength(); att++) {
                String attName = attr.getQName(att);
                if (attName.equals("name")){
                    if(attr.getValue(attName).equals("driverClassName")){
                        currentBeanConnectBanks.setDriverClassName(attr.getValue("value"));
                    }
                    if(attr.getValue(attName).equals("url")){
                        currentBeanConnectBanks.setUrl(attr.getValue("value"));
                    }
                    if(attr.getValue(attName).equals("username")){
                        currentBeanConnectBanks.setUsername(attr.getValue("value"));
                    }
                    if(attr.getValue(attName).equals("password")){
                        currentBeanConnectBanks.setPassword(attr.getValue("value"));
                    }
                    if(attr.getValue(attName).equals("ProcessName")){
                        currentBeanConnectBanks.setProcessName(attr.getValue("value"));
                    }
                    if(attr.getValue(attName).equals("urlPIEV")){
                        currentBeanConnectBanks.setUrlPIEV(attr.getValue("value"));
                    }
                    if(attr.getValue(attName).equals("authentication")){
                        currentBeanConnectBanks.setAuthentication(attr.getValue("value"));
                    }
                }
            }
        }

    }

    Hashtable<String, List<BeanConnectBanks>> responcesBanks = new Hashtable<String, List<BeanConnectBanks>>();

    public void onResponce(BeanConnectBanks responseBanks) {
        if (responseBanks == null) return;

        String id = responseBanks.getBeanId();
        List<BeanConnectBanks> list = responcesBanks.get(id);
        if (list == null) {
            list = new LinkedList<BeanConnectBanks>();
            responcesBanks.put(id, list);
        }
//        System.out.println(id + " " +response.getPassword());
//        System.out.println(response);
        list.add(responseBanks);
    }


    public Hashtable<String, List<BeanConnectBanks>> getResponcesBanks() {
        return responcesBanks;
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        long start = System.currentTimeMillis();

        if (qName.equals("beans")){
            onResponce(currentBeanConnectBanks);
//            System.out.println("Всего ответов: "+index);
//            System.out.println("Все ответы в файле прочитаны");
//            throw new EndDocumentException();
        }
    }


}
