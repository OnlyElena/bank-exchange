package jdbc;

import org.apache.log4j.Logger;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.*;
import java.util.List;
import java.util.Map;

/**
 * @author: OnlyElena@mail.ru
 * Date: 3/11/16
 * Time: 3:11 PM
 */
public class GetBeansConnect implements Serializable {

    public static Map<String, List<BeanConnect>> GetBeansConnect(File file) {

        ParserBeanConnect parser = null;
        try {
            InputSource src = new InputSource(new FileInputStream(file));

            XMLReader reader = XMLReaderFactory.createXMLReader();
            parser = new ParserBeanConnect();
            reader.setContentHandler(parser);
            reader.parse(src);


//        } catch (EndDocumentException e) {
            //достигли конец документа, все в порядке
            return parser.getResponces();
        } catch (SAXException e) {
            Logger.getLogger(GetBeansConnect.class).error(e);
            e.printStackTrace();
            //пропускаем цикл, т.е. не смогли разобрать файл

        } catch (FileNotFoundException e) {
            Logger.getLogger(GetBeansConnect.class).error(e);
            e.printStackTrace();
        } catch (IOException e) {
            Logger.getLogger(GetBeansConnect.class).error(e);
            e.printStackTrace();
        }

        return null;
    }
}
