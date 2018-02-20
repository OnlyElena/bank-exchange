package jdbc;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

/**
 * @author: OnlyElena@mail.ru
 * Date: 3/11/16
 * Time: 3:15 PM
 */
public class ParserBeanConnect extends DefaultHandler {

    BeanConnect currentBeanConnect;
    int index;

    public ParserBeanConnect() {
    }

    @Override
    public void startDocument() throws SAXException {
        super.startDocument();
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attr) throws SAXException {

        if (localName.equals("bean")){
            index = index+1;
            if (currentBeanConnect != null) {
                onResponce(currentBeanConnect);
            }
            currentBeanConnect = new BeanConnect();
            for (int att = 0; att < attr.getLength(); att++) {
                String attName = attr.getQName(att);
                if (attName.equals("id")){
                    currentBeanConnect.setBeanId(attr.getValue(attName));
                }
            }


        }

        if (localName.equals("property")){
            for (int att = 0; att < attr.getLength(); att++) {
                String attName = attr.getQName(att);
                if (attName.equals("name")){
                    if(attr.getValue(attName).equals("driverClassName")){
                        currentBeanConnect.setDriverClassName(attr.getValue("value"));
                    }
                    if(attr.getValue(attName).equals("url")){
                        currentBeanConnect.setUrl(attr.getValue("value"));
                    }
                    if(attr.getValue(attName).equals("username")){
                        currentBeanConnect.setUsername(attr.getValue("value"));
                    }
                    if(attr.getValue(attName).equals("password")){
                        currentBeanConnect.setPassword(attr.getValue("value"));
                    }
                    if(attr.getValue(attName).equals("ProcessName")){
                        currentBeanConnect.setProcessName(attr.getValue("value"));
                    }
                    if(attr.getValue(attName).equals("urlPIEV")){
                        currentBeanConnect.setUrlPIEV(attr.getValue("value"));
                    }
                    if(attr.getValue(attName).equals("authentication")){
                        currentBeanConnect.setAuthentication(attr.getValue("value"));
                    }
                }
            }
        }

    }

    Hashtable<String, List<BeanConnect>> responces = new Hashtable<String, List<BeanConnect>>();

    public void onResponce(BeanConnect response) {
        if (response == null) return;

        String id = response.getBeanId();
        List<BeanConnect> list = responces.get(id);
        if (list == null) {
            list = new LinkedList<BeanConnect>();
            responces.put(id, list);
        }
//        System.out.println(id + " " +response.getPassword());
//        System.out.println(response);
        list.add(response);
    }


    public Hashtable<String, List<BeanConnect>> getResponces() {
        return responces;
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        long start = System.currentTimeMillis();

        if (qName.equals("beans")){
            onResponce(currentBeanConnect);
//            System.out.println("Всего ответов: "+index);
//            System.out.println("Все ответы в файле прочитаны");
//            throw new EndDocumentException();
        }
    }


}
