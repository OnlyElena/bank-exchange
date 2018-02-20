package exceptions;

import org.xml.sax.SAXException;

/**
 * Нужен исключительно для завершения обработки файла ответа, что бы корректно обрабатывать
 * ЭЦП в конце файла, которая портит весь формат XML
 *
 * @author: Andrey Panov
 * Date: 5/24/12
 * Time: 6:52 PM
 */
public class EndDocumentException extends SAXException {
    public EndDocumentException() {
    }

    public EndDocumentException(String s) {
        super(s);
    }

    public EndDocumentException(Exception e) {
        super(e);
    }

    public EndDocumentException(String s, Exception e) {
        super(s, e);
    }
}
