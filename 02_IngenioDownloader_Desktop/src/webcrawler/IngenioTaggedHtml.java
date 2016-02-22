package webcrawler;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.http.HttpVersion;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import vsdk.toolkit.common.VSDK;

/**
*/
public class IngenioTaggedHtml extends TaggedHtml {
    /**
    Needs to previously been called a get page (using GET method) in order to
    have a cookie set to send.
    @param pageUrl
    @param cookies
    @param login
    @param password
    @param initialIdentifiers
    @return index page
    */
    public boolean postInternetPageForLogin(
        String pageUrl,
        ArrayList<String> cookies,
        String login,
        String password)
    {
        String host = "www.mppromocionales.com";
        try {
            HttpPost connection = new HttpPost(pageUrl);

            prepareExistingCookies(cookies, connection);

            connection.setHeader("Host", host);
            //connection.setHeader("Connection", "keep-alive");
            connection.setHeader("Cache-Control", "max-age=0");
            connection.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
            connection.setHeader("Origin", "http://" + host);
            //connection.setHeader("X-Requested-With", "XMLHttpRequest");
            connection.setHeader("Upgrade-Insecure-Requests", "1");
            connection.setHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.106 Safari/537.36");
            connection.setHeader("Content-Type", "application/x-www-form-urlencoded");
            //connection.setHeader("DNT", "1");
            connection.setHeader("Referer", "http://" + host + "/index.php");
            connection.setHeader("Accept-Encoding", "identity" /*"gzip, deflate"*/);
            connection.setHeader("Accept-Language", "es-US,en;q=0.8,es;q=0.8");
            connection.setProtocolVersion(HttpVersion.HTTP_1_1);

            //-----------------------------------------------------------------
            // Prepare POST contents request
            String loginString = "user=" + login + "&pass=" + password;
                        
            //-----------------------------------------------------------------
            ByteArrayInputStream bais;
            bais = new ByteArrayInputStream(loginString.getBytes());
            byte arr[];
            arr = new byte[bais.available()];
            bais.read(arr);
            ByteArrayEntity reqEntity = new ByteArrayEntity(arr);
            connection.setEntity(reqEntity);
            
            //-----------------------------------------------------------------
            CloseableHttpClient httpclient = HttpClients.createDefault();
            CloseableHttpResponse response;
            response = httpclient.execute(connection);

            addRecievedCookies(response, cookies);

            int status = response.getStatusLine().getStatusCode();
            System.out.println("  - HTTP Post result status: " + status);
            
            if ( status != 302 ) {
                return false;
            }
            
            //-----------------------------------------------------------------
            InputStream is;
            is = response.getEntity().getContent();
            
            importDataFromHtml(is);
            
            response.close();
            return true;
        }
        catch ( IOException e ) {
            VSDK.reportMessageWithException(this,
                VSDK.WARNING,
                "getInternetPage",
                "HTTP ERROR",
                e);
        }
        return false;
    }

}
