package jdbc;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @author: OnlyElena@mail.ru
 * Date: 3/11/16
 * Time: 1:47 PM
 */
public class TestConnect {

    public boolean TestConnect(String url) {
        boolean st;
        st = checkInternetConnection(url);
        if (st == false) {
            try {
                //делаем паузу, ждем восстановление связи
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Logger.getLogger(TestConnect.class).error(e);
                e.printStackTrace();
            }
            st = checkInternetConnection(url);
            if (st == false) {
                try {
                    //делаем паузу, ждем восстановление связи
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Logger.getLogger(TestConnect.class).error(e);
                    e.printStackTrace();
                }
                st = checkInternetConnection(url);
                if (st == false) {
                    return st;
                }
            }
        }
        return st;
    }

    public static void main(String[] args) throws IOException {
        String url = "http://10.38.85.130:8080/pksp-server/";
        System.out.println(checkInternetConnection(url));


//    DefaultHttpClient httpClient = new DefaultHttpClient();
//    HttpGet request = new HttpGet("http://url.com");
//    HttpResponse response = (HttpResponse) httpClient.execute(request);
////    int responseCode = response.getStatusLine().getStatusCode();
//    int responseCode = response.getStatusCode();
//        if(responseCode==200){ // url найден
//    }

    }

    private static boolean checkInternetConnection(String url) {
        Boolean result = false;
        HttpURLConnection con = null;
        try {
            // HttpURLConnection.setFollowRedirects(false);
            // HttpURLConnection.setInstanceFollowRedirects(false)
//            con = (HttpURLConnection) new URL("http://www.ya.ru").openConnection();
//            con = (HttpURLConnection) new URL("http://10.38.4.11:8080/pksp-server/").openConnection();
            con = (HttpURLConnection) new URL(url).openConnection();

            con.setRequestMethod("HEAD");
            result = (con.getResponseCode() == HttpURLConnection.HTTP_OK);
        } catch (Exception e) {
            Logger.getLogger(TestConnect.class).error(e);
            e.printStackTrace();
        } finally {
            if (con != null) {
                try {
                    con.disconnect();
                } catch (Exception e) {
                    Logger.getLogger(TestConnect.class).error(e);
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

}
