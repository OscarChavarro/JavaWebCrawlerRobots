//===========================================================================

package webcrawler;

// Java classes
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.URI;
import java.util.List;

//
        
//
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpVersion;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;

import org.json.JSONObject;

import vsdk.toolkit.common.VSDK;

/**
This class is meant to keep an on-memory copy of a HTML or similar file.
The stored data structure keeps some structure to give hints over the
original HTML type data:
  - HTML bytes are partitioned in different areas: inside tag, or between
    tags areas
  - For each tag, tag name and tag parameters can be retrieved
  - Some tag search/query operations are provided over the structure
  - Original data can be reconstructed from data structure's copy
*/
public class TaggedHtml
{
    private static final int OUTSIDE_TAG = 1;
    private static final int INSIDE_TAG = 2;
    private int currentState = OUTSIDE_TAG;
    public ArrayList<TagSegment> segmentList;
    private TagSegment currentSegment;
    private final CookieManager cookieManager;

    public TaggedHtml()
    {
        segmentList = null;
        currentSegment = null;
        cookieManager = new CookieManager();
        CookieHandler.setDefault(cookieManager);
    }

    public static boolean justSpaces(String in)
    {
        if ( in == null || in.length() < 1 ) {
            return true;
        }
        int i;
        char c;
        for ( i = 0; i < in.length(); i++ ) {
            c = in.charAt(i);
                if ( c != ' ' && c != '\t' && c != '\n' && c != '\r' ) {
            return false;
            }
        }
        return true;
    }

    public static String trimQuotes(String in)
    {
        int l;
        String out;
        out = in;
        l = out.length();
        if ( out.charAt(l-1) == '\"' ) {
            out = out.substring(0, l-1);
        }
        l = out.length();
        if ( out.charAt(0) == '\"' ) {
            out = out.substring(1, l);
        }
        return out;
    }

    public static String trimSpaces(String in)
    {
        int start, end;
        String out;
        out = in;
        char c;

        if ( in == null ) {
            return null;
        }

        if ( in.length() < 1 ) {
            return in;
        }

        if ( justSpaces(in) ) {
            return "";
        }

        for ( start = 0; start < out.length(); start++ ) {
            c = out.charAt(start);
            if ( c != ' ' && c != '\t' && c != '\n' && c != '\r' ) {
                break;
            }
        }

        for ( end = out.length() - 1; end >= 0; end-- ) {
            c = out.charAt(end);
            if ( c != ' ' && c != '\t' && c != '\n' && c != '\r' ) {
                break;
            }
        }

        out = out.substring(start, end + 1);
        return out;
    }

    /**

    @param is
    @return true if this is last page
    */
    private String importDataFromJson(
        InputStream is)
    {
        segmentList = new ArrayList<TagSegment>();

        try {
            //-----------------------------------------------------------------
            byte []  buffer = new byte [4096];

            String msg = "";

            int bytes;
            
            while ( true )  {
                bytes = is.read(buffer);
                if ( bytes <= 0 ) {
                    break;
                }
                msg += new String(buffer, 0, bytes, "UTF8");                
            }
            
            JSONObject jsonObject;
            
            try {
                jsonObject = new JSONObject(msg);                    
            }
            catch ( Exception e ) {
                jsonObject = null;
            }

            if ( jsonObject == null ) {
                return null;
            }
            
            //-----------------------------------------------------------------
            Set<String> keys;
            keys = jsonObject.keySet();

            for ( Object o : keys ) {
                String s = o.toString();
                Object v = jsonObject.get(s);

                if ( s.contains("url") ) {
                    if ( v instanceof String ) {
                        String ss = (String)v;
                        return ss;
                    }
                }
            }
            //-----------------------------------------------------------------
        }
        catch ( IOException | NumberFormatException e ) {
            System.err.println("Error reading processing HTML");
        }
        return null;
    }

    /**
    Creates a new html based web transaction from the specified pageUrl,
    without taking into account any input cookies.
    TODO: Change this to only call another method, one with cookies handling!
    @param pageUrl
    @param cookies
    @param withRedirect true if used in the login phase with redirects
    */
    public void getInternetPage(
        String pageUrl, ArrayList<String> cookies, boolean withRedirect)
    {
        //----------------------------------------------------------------- 
        CloseableHttpClient httpclient;

        HttpGet connection;
        connection = new HttpGet(pageUrl);

        connection.setProtocolVersion(HttpVersion.HTTP_1_1);
        connection.setHeader("Host", getHostFromURL(pageUrl));
        connection.setHeader("Connection", "keep-alive");
        //connection.setHeader("Cache-Control", "max-age=0");
        connection.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        connection.setHeader("Upgrade-Insecure-Requests", "1");
        connection.setHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.80 Safari/537.36");
        connection.setHeader("DNT", "1");
        connection.setHeader("Accept-Encoding", "identity" /*"gzip, deflate, sdch"*/);
        connection.setHeader("Accept-Language", "en-US,en;q=0.8,es;q=0.6");
        //connection.setHeader("Referer", "http://empresa.computrabajo.com.co/");
        prepareExistingCookies(cookies, connection);

        //-----------------------------------------------------------------
        if ( !withRedirect ) {
            //System.out.println("  - Creating HTTP connection without redirection to URL: " + pageUrl);
            httpclient = HttpClients.createDefault();
        }
        else {
            //System.out.println("  - Creating HTTP connection with redirection to URL: " + pageUrl);
            DefaultHttpClient dhttpclient;
            dhttpclient = new DefaultHttpClient();
            MyRedirectStrategy rs;
            rs = new MyRedirectStrategy(connection, cookies);
            dhttpclient.setRedirectStrategy(rs);
            httpclient = dhttpclient;
        }
        
        CloseableHttpResponse response = null;
        try {
            response = httpclient.execute(connection);

            //System.out.println("  - Response code: " + response.getStatusLine());
        }
        catch ( ClientProtocolException e ) {
            //e.printStackTrace();
            //e.getCause().printStackTrace();
            System.out.println("HTTP redirect error");
        }
        catch ( IOException e ) {
            try {
                Thread.sleep(5000);
            } 
            catch (InterruptedException ex) {
            
            }
            System.out.println("*** Fallo en descarga html, reintento 2");
            try {
                response = httpclient.execute(connection);
            }
            catch ( Exception e2 ) {
                VSDK.reportMessageWithException(this,
                    VSDK.WARNING,
                    "getInternetPage",
                    "HTTP ERROR",
                    e2);                        
            }
        }

        //-----------------------------------------------------------------
        try {
            if ( response != null ) {
                InputStream is;
                is = response.getEntity().getContent();
                importDataFromHtml(is);

                //-----------------------------------------------------------------
                addRecievedCookies(response, cookies);        
            }
        }
        catch ( IOException e ) {
            VSDK.reportMessageWithException(this,
                VSDK.WARNING,
                "getInternetPage",
                "HTTP ERROR",
                e);        
        }

        //-----------------------------------------------------------------
    }

    /**
    Needs to previously been called a get page (using GET method) in order to
    have a cookie set to send.
    @param pageUrl
    @param cookies
    @param login
    @param password
    @return index page
    */
    public TaggedHtml postInternetPageForLogin(
        String pageUrl,
        ArrayList<String> cookies,
        String login,
        String password)
    {
        try {
            HttpPost connection = new HttpPost(pageUrl);

            prepareExistingCookies(cookies, connection);

            connection.setHeader("Host", "empresa.computrabajo.com.co");
            connection.setHeader("Connection", "keep-alive");
            connection.setHeader("Cache-Control", "max-age=0");
            connection.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
            connection.setHeader("Origin", "http://empresa.computrabajo.com.co");
            //connection.setHeader("X-Requested-With", "XMLHttpRequest");
            connection.setHeader("Upgrade-Insecure-Requests", "1");
            connection.setHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.106 Safari/537.36");
            connection.setHeader("Content-Type", "application/x-www-form-urlencoded");
            connection.setHeader("DNT", "1");
            connection.setHeader("Referer", "http://empresa.computrabajo.com.co/");
            connection.setHeader("Accept-Encoding", "identity" /*"gzip, deflate, sdch"*/);
            connection.setHeader("Accept-Language", "es-US,en;q=0.8,es;q=0.6");
            connection.setProtocolVersion(HttpVersion.HTTP_1_1);

            //-----------------------------------------------------------------
            // Prepare POST contents request
            String loginString;
	    // = "txEmailLogin=" + login + s + "txPwdLogin=" + password;

            loginString = "__VIEWSTATE=Esw6OrNrqrp2DwTYphaZq5GCP%2Fjx4MmPyex0i%2FUliQdYmlJT9QD9JqMwkXqcZsahsl5kkhLYL6DyJNiRDMg3%2FE5AztxA9hsEhOrzgn4zQP2AZwG8QOhI4mcJKR1xOvWUc4ZyVWcrepaEwevdWd3Ci6qQidsupaqkNdQO7ACMTaDgiUolOMf2sKtn0zHlPvPk1cbwS9ExOeYVhGoWCn6BuLE2kzQfYIKwH1DhKighLxLIEyEimmGK%2BrQLL%2Bmc13GrmYsT3N9%2BAfhSeK07YBgHCS6NpmGQhl0%2FoQGehzFB9TgH%2FXv0cczGidi4k1%2BA2zyFT3Gc89JJNQTWKJRnkVLdWPPNnlC4ffq4gWhTlr8mTSmnKTcuZSF3sR8uKRnv63kq4xYPTBrz1FQ0yVPreqmwR1UXSWCbc5GBNHc6jFGpnNnhuDTGviRe8FOSZ6XZ7Qk0yqfF531bO3QNarhikVENEQ7fBMNEgcU%2BGsTI9l%2BTKiSN3gaugu4Of3LkJY92XxP%2FmWdjAalDikEr8KeJv1xLATsDQzIK3INdv6Qbed24HV0FYfZZTBYFP7P4PHcNEo6s1B2VO57mQpj4fIm0ce4K8ECbAIkNcjyYgAES19F0nI1YrVPq3HDqz4%2FtEl%2Fb4UT5ZwoxwhPl5cgFNkwD%2FPAOVW2GDr%2BMfH2XYXfQMmoFd4SatCj2YbK2VCi9Z7qWRLacobd4%2FMVkC35xbOP9LBzqxo%2BjaQUtlX7ONryKaHMzKpMU1fI7NIxX5tn1nXhlbf6MANR13bcyc%2BCjv1g%2FGoknsho%2Fr7SKsC0AYwdXs%2F5gKFnHlyE5sdWY%2F7rPHM%2F%2B0GufZivKhZyrFCdw88QegKheO7B6Sqj7QZUqaNeijh2HWsIIWzcH8JN6N%2BkO28PkD6yXYwjaKpAkx4cWU3SWexuZsAdmYvJlT2%2FVB5frjIeQMx55jTB4SryTsFfyI1MiIBztNfPaS5dO4xmeJIbwhdfGRljLYOviqtc%2FefZarSccvCox6y%2FFgSKuBC5Krr4rqfG71jefqbGDun5Z5FMFlMygf%2FVeu6nYgYnUeNtaEWJXkmPIvRZMnn1LJvNY58ujgfaZm9s8C9GUrIqWORVAMo2UJW7%2BQtH7pAIm7bwLtc4ud33GdX52GFOHzh%2FrGffuAUQ1dDrZsVzQy2h6kGT6I%2FJ4b%2B5pJQHC%2FVgsvgmAorItmnyaTi490tEpBXSrG7bdBpdL5LEWMeDXNE1nmW7%2F%2FQ47bY%2Bek7QdHT%2Blvd3DHBJeAlQaA4W08%2Bxnf5KDYD65dPuD2dNmEord5cgJYAtYMFX76OCStEk4EVvH4OW8sYX1uo0v45zspkDAc5Xe4PKpo64ObGLdx4kMjNfdJp2wzEbb%2FBngjyEo%2BW%2FtyB8AjoALq%2BqwHVWOm%2FwhiJd4yM%2FvF0nMm1zscNjuXpczVsrsjb5jhkGgriSxmcNBFvEt3TEmLir%2BYv9zQCEUp2MW9XFyWOL1gUeoUrHrN4Ub58IyMQ6WIkce2F%2B4W2rnLDNYNaqjCbMSt03hLGPKXytEJAMd8bG2upVfs1fw6ZeDk6DbvClC67NwqMtHt1ddAZphLt9oxcpJ2r7D3I%2BEiWmMr%2Bmkmn0IkpVPiDyBD%2BmC1XhRFfCYcFjbfXyCsxq%2FSMQfaYNbpdsXwoF%2FJ5ppRjBtGTrkTnKu2vevuofXwN5%2Fk9N2einENbOVpiK57ThzG%2FtK9MbSuW3YRWSBcXrDw9T1Xp3APOL3RPr9FpsfDj2GbwLVMzIUYPeZG5RJUCqXDM1fjIySHK%2B%2F7LSN654PWi9mkaf5r5s0Sw0nQo%2BcLQHN4adLQALr9s8l9MtLtLNMo7GADNsMAAeXj9sa0u%2FZnQTBijDqqsyL4FPsnUZBXb2LV2xNrUc%2F4QV%2F%2B2JzrsV3n5KDW5hhnpxHSG2Y9yWJMXpLKHdH7q3xd16cr%2F%2F%2FhAZYXvhbe%2BzEU5X9LQ4j%2BiAeUlP%2Fa4xkICqlQUfHXIA09wDWCIP9YlHUobjVBOTQ%2FxEwneBo3Zmt07V1BdsliSxTWJ6jcHMUOJ6T%2BjtdE5NFDyWm%2FEG3OWWRJ0YjVXFcQ2dXSiNWZEbzoe%2FX%2B1WdQTFM048KKSgc0eTrVkbknm0GTiU6CUfLUl9T6lozAvw6jiHapAVPG1Kw68RN0H7If7PAOOazel9KiE49Iqi9fy4ZeEtthhfpOpBc48eE7VYcD1q1ZmFZH6VsKyvsnW97krcDYL2uyTv9i18RlkhvMhfHSSo1DrFZT9QQuAANPS61OK0hzLkAxRXvF2R6zFGVUcSeZAGPfcUB7tq5G7B3hrc8G9IJqPkwHGW2Ptw9rMq4UaIoP9nS5M1KaqleO4MyuOfdahZ%2Bes1XKX91GYLiNw24GjvVMv%2FphSPMWTaDEjfsM3cba1U2Ji%2Fzlaadb5G1f2d291O5YGFzd2MvUVojXytsvurtQbrhfNBOWOxJmgfiRZzDDXokuICtwY7US6fw2YKnT8ya2CFO1GBDk3J4%2FlLTYq0wtzMg9w9QPEM%2BYzQi7bfxIGrEV1UyOIDAO9Mk0IrKSJYG1GJ6C9s5U4hRS5mn6%2BsuYLZ7%2FY%2FrLO7THfan%2B19QTa4w7Z1GwfIQySkJAy6Qfx4fXxthMqLdaCrgsvkINcezsCRqGv1sH%2FA0NQzOG2DyYgnui%2BdVITlegMUOowJf3c930c443OnP%2BmoonmCWpRutODFMpqL%2FQOYgUpLAWWv1rcSmK0p8G6Y4ptXHtTObozakF66F090KHAWVYiWQuOPu3QykDhXWk8je2%2BDSuWL0%2F79C%2FQ9icqooW8H6X7Z7sBh0sejTPMboTydiA1cGpZRgP0DYEicOQ6zVaDgspJJV%2B5QH4nTvwEujNggoh00dC%2BUMAnoS%2FImDD7Oo3ojsW5JZa4vK%2FPkJKNPggn9lUbd49t1nnbm2O5Sf67EVInkP6tOCIpZW8JSmy9umyzs%2Bc41vtopifD3%2BddDwANUbr%2BoLzGhmGWQMONIXY7Zjgc2Hptq2wHficaGNfI%2FjkmWcgNHg9HBXZHKsCoONXHZf0iZ67B7k4SDvuY9QzUSLt8LmPsbZehNGv9SN6OyS94SrD4vhV%2BpJx6GL3J8cYedb9%2FZaYu2xY08BiOFM3UnsJEFie3t5QljpSV%2FTROedxwPMFXO2kobchNbDsDW6wbFSpWDEKyEpuqMayJCuO7QcNm3zUkW8xZScjIdBz1V7xcEFTKt0IpCXxsH0EXyOPeBB0yjMo2a1T%2BXFuqA5GIrRRmULjab2ug357PTSiaO8Va%2FnGod1LFiGi32n5Qq5r93xt7%2BkaMfbexkq86JKOZj29He%2FyQvOoxo%2FenSMcBBbnBDHqb3X5pJeQTptRT9OnBQQ58KjCEik800NrPx9eZEJ9pbGxQ9vUmzRkJqomqXnAI7z%2FsXCZfFS3YvRY9vM%2FBrBXGo8qYLjgmqMBOlUJhKo%2FKMXCcO1c1s%2Bht9BuyzRZ5%2BGPru6fLm9P%2FQfJKsjogqNqsM8TG35svtYjr9x0w4hSbLOkkS1CtfG%2FLaTUvFX2z2tOXoQVczxj6MaXkUGxZKf64coBMS7WriYixa6Bsa6e51h2jJS783MCiflfBS%2F9ZqURh%2B2hVDmo5njmTcI9Kbbsc9m0WUeAyiisjqn4yEa1L3aaplM%2F8o2IRg%2BiZqaxeiz2rbFjcOecpw3j3wZGdlO0UuoPFmb9z9QmrBHNh3VF0xShcUJVHaaSIV%2B5CIIFO%2BryKmHnBsx7sGbf1zfUot3qMKh6wXdRQTmjWsZTlOxwbq2wjoP4HLIDzERrQv4dZuIieZKNUnVZMmxRlwWsgMcaQetHckWccdu%2B5Ohs4KbTyCmg2sCzcqqubYVnR1huWAq8Ww6pUvW6M8IICEcM0tLx5%2Fg0n7yeqD86VE7e6r3jYStnRviatXwu5MQTjGvb1UvNDEi1xO7sdHo1l048KNAhDGikOJev51DOkgns7iJPV0vL8noNm651x7RT3LO4HRD%2BkVtVzmMlVH%2BKFPeQ6mxit4IXfDnivRzUdhtHmLv1jhHKgUnED%2ByU%2BxS4mvgg3Efg%2B6%2FU05Fy9Ps66DP5IeiyzMRRX3xOKPmt90sVkcxdb86d7HoVZQDOuZPuBETws0sxFOhXlibMv92KkswelcL2aayLBcd3dLlIrehB40uqpWtieNj36yOjiegNWPO7J6hLxi0JteX4wWxO64gB%2Fe4bbH2c52cQA4oCch5LMlu%2BaRsDIUUCTPlUPicrlQTqAAv4528OyLECmeXJ5uVTOvudoh7hW54f2wiX0js%2B9tlsUM9V0DdKzyMfBjh%2BXZWAAa627lb%2FjftrnWFwu5zknBLm8qXTc2zdXsVaaljPgW%2BCd35gVufsRr%2BjYA9NgioFMGxuNVTWSQkwdfXDtpqX%2FIdNFFc%2B%2Fh9Oc4ApTDtXa4y%2Fb1uWwhFend12FMj16p1BnnXWvpqAM%2F%2Burtw5N88CFQcfm307LbjQNyyhgCbvGR3LiPDHHm%2Fgg4uKqeXnWitcMKyxaa8Xw6mxT%2BlEby%2BiNFUAmEbwcQS0yMFPRcNBJd0Zj7xZR2rZp0o089ecRHdN38mCqOROQq0XIT8mmR9eyoC%2BjBHoA8pFQ%2FFmNx8kDwLci3feZ8P%2F%2FYhp3KDFsusZePialjWkJ9zp71nO4hsLXh75WlBlGKwhq6gbOAD5O%2FaI3628iuOoH3FIpRm0rbjYLiojA3ZYKERApOXyR9K1%2FsIQIJ6O%2FBljTkysIQWvTqVEhHHdXofoCCbGwi0%2FNrHlFLJCM6QVkufZM8owqomPvvnZXfGviSXVnOH9EbanPEyT5Us7ikjJyL1aJzlrVnIym9HVi2H6u3uZgdxb4bzh1pGhQJJU%2Fz7CszclGP8VU694IXnxFq0ZogAJswBpjo%2BhJXCJaQktvV4f4A8LejMf02V5rnZPVthqO53%2B9PmuggHX9JcySPZ3g0GGe2BvjPo3iDYNe04Il5%2BtpiPlEaCkh2xfNX3t8EtYhgHzW6nkk1DU7wyFBr36jzsx2FgUf1yvVkHqtnvHgZGS9kYWDiIFo%2Fs4U75IMx8XOzDLFLDDKCXMGt%2FBTPCCJl%2FsgAX1Ok60OQsdmU2WElMiw6IwLEmDEC3iu0abajyJAqBRjZRV%2B9p5RZjkCw%2Bew6ZbIQn5dxP319OVnkZYRYq8WwtYs8l3I%2BM%2FpCloQFiWm38PJIcXD1B83E2%2Brqg%2Bz7uvmw0cadk7nsgzlH88KV5ypFl8KIT%2F%2BPK2wdgDlkdk%2FgmMQ4pDpSGDe%2B0YqRILTC1MtC3nT1CVK0k2Ckr4e8wb3QadOAXlg%3D%3D&amp;" +

"__VIEWSTATEGENERATOR=CA0B0334&amp;" +

"__EVENTVALIDATION=ow6jjL8%2FZvn7Vlvaf6CE7EmYZnHYftlyj5F0RNKaCu6x478YhAXJK8fN0Ow3%2FAJLmfdoy7jFG3wedfYr8uMMzzY2kPb7AxM%2F09RkmjD5Y%2F1h%2Fbcnq5HHWsltv%2FwHCbpcA4PH91rego0UtYvx2jWPmWvdGx0ex3hAzxhGITBijV2hUGGMnyG1IjiH9AMFq6w3qZoNoGrFJYpnTk8bE7Joj94%2FnP3FuoWGkaBvZZ8K4HkjOYO0J1VX%2BCTz1tSobWVnh72ysMyXWp5zMwR08fCijv5G%2BBYS9WpVaQWO7GNO4%2BGqT9FdunBdXHz0PiOCXTaPa8jMcoqtDZRtijHUmN9xWnQ5jP9hKQdHXEjen1Pphy5jP4o%2BpBXWYI0zKOLvShAako2V5axC1m1C7l%2FH%2BRIqRmDvuS8pChCaGbJ8hGn0Q9J2NukOKbxYeD44uY3%2Bru88%2FOCdD3SDjHeFVjGEqI5KC537G24U1KG4ZDHc1I4bd8f5nJwDb6FpgM%2Btm5pzXPIq01SqZ7HEtnUWgHXrmE%2FWsoAsh7DMa8wdsFuCukNN6a%2BJB7ZDUF6lvp1s9ruJtqmympKMkjegI266woFIfT0Nf81gsbgDQRkQmgyn%2FtWDODVbr%2BjG4QyC5A56xVuSmPWHClUKwkZ0Wp5YlfOSNPLpzR2PsbqwPwnCVj6Erb5YuG9dnTyLiFgcgELU3nRwUCuagV59A0%2FmeIPkVRJx0n3wRn2TfB%2Bc9qmvUFGxEzYj64eQtmmv%2F0nqOXoON6ONh2Hz9tSzk066JoWm1cFVf90rO0muOWMoY%2FMnjdWVyP017cM%2FbIce6OojSKZ5m9uOUqmAIQdHlncA0sLViqpJ9%2FbRRI1pW9Oco0yABsasHmHytcSND3NLW5G30%2Bv4AXeKDhbWJlVJTLDqJJezQW1oU%2BvJrkDXMSBdK1AJRHdFEuJxpJuRYfasJn4%2FUrUxlHEi7Ji38zMdDjfShFJ1BXre5E3bFi630gLAx50xEX5ZIy7H%2F5piU6uVvmc6ewLswftqw7VaItLBIyu8LaIEWw%2FDAsX0Gf88tcOVmm94RkpDt6x0UeJ0UQc6LIdUCeDDnW3KdkYhdEJ0tIp4YAAj5KRj7gzegKxSjYZV7Z81hIb2iMWzD63dyq2GCAlM73nFLaDex2vq8GxtSBUZjGOD65cOcux77Opp8w4vpVIibvb6%2Bu6SDqqHpgi3JEuPD6yTh4ayX3bySg8ufEzpkw8J5%2FUp9xlMc%2Bw6svbpWqHQddD6pnIq6TBIms%2BJf6N%2BOk3Dl4MsptHqlk0CDiPRkl7KCxsBPblMx1NlPiITq%2F6r2dXOz4gH2ylupBGHnmYSJVfZ%2FAVEVpCWX79gOpKiDF4rxlYXoCexd4M0OY%2FE%2B%2F%2BcyoBzZAhrRlPL1hJ4hkEU3a14ctGvspVv3WNDcFXv%2BJ2TVHz5JFvd3VQhr%2BVYASHBys4pv6xpukfmLahm95SKheq9ESUPVCoSvzxbNA%3D%3D&amp;" +

"ctl00%24txEmailLogin=talent%40abakoventures.com&amp;" +
"ctl00%24txPwdLogin=Qwerty77&amp;" +
"ctl00%24btnLogin=Entrar&amp;" +
"ctl00%24ContentPlaceHolder1%24txQuery=&amp;" +
"ctl00%24ContentPlaceHolder1%24ddProvince=0&amp;" +
"ctl00%24ContentPlaceHolder1%24ddCat=0";


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

            //-----------------------------------------------------------------
            InputStream is;
            is = response.getEntity().getContent();
            String responseUrl;
            responseUrl = importDataFromJson(is);
            if ( responseUrl != null ) {
                System.out.println("3. Activating authentication tokens");
                TaggedHtml pageProcessor = new TaggedHtml();
                pageProcessor.getInternetPage(responseUrl, cookies, true);
                return pageProcessor;
            }
            response.close();
        }
        catch ( IOException e ) {
            VSDK.reportMessageWithException(this,
                VSDK.WARNING,
                "getInternetPage",
                "HTTP ERROR",
                e);
        }
        return null;
    }

    private void prepareExistingCookies(
        List<String> cookies, 
        HttpRequestBase connection) {
        //-----------------------------------------------------------------
        int i;
        String val = "";
        for ( i = cookies.size() - 1; i >= 0; i-- ) {
            String c;
            c = cookies.get(i);
            if ( c.contains(";") ) {
                int x;
                x = c.indexOf(';');
                c = c.substring(0, x+1);
            }
            val += c + " ";
        }
        val += " _gat=1; _ga=GA1.3.575338493.1446664730";
        if ( cookies.size() > 0 ) {
            connection.setHeader("Cookie", val);
        }
    }

    private void addRecievedCookies(
        CloseableHttpResponse response, ArrayList<String> cookies) 
            throws ParseException {
        //-----------------------------------------------------------------
        // Append new cookies
        Header hs[] = response.getAllHeaders();

        if ( hs != null ) {
            addHeadersToCookies(hs, cookies);
        }
    }

    public static void addHeadersToCookies(
        Header[] hs, ArrayList<String> cookies) throws ParseException {
        //System.out.println("  - Processing headers in HTTP response: " + hs.length);
        int i;
        for ( i = 0; i < hs.length; i++ ) {
            //System.out.println("    . Header: " + hs[i].getName());
            if ( hs[i].getName().equals("Set-Cookie") ) {
                HeaderElement he[] = hs[i].getElements();
                int j;
                for ( j = 0; j < he.length; j++ ) {
                    String cc;
                    cc = he[j].getName() + "=" + he[j].getValue() + ";";
                    if ( !containsCookie(cookies, he[j].getName()) ) {
                        //System.out.println("      -> (*NEW*) " + cc);
                        cookies.add(cc);
                    }
                    else {
                        //System.out.println("      -> " + cc);
                    }
                }
            }
        }
    }

    private void processByteHtml(byte b)
    {
        String content = currentSegment.getContent();
        if ( b == '<' ) {
            if ( currentState == INSIDE_TAG ) {
                //System.err.println("Warning: re-entering tag");
                //System.err.print("[!");
            }
            
            if ( content != null &&
                 content.length() > 0 &&
                 !justSpaces(content) ) {
                //System.out.println("REMANENTE: " + currentSegment.content);
                currentSegment.insideTag = false;
                segmentList.add(currentSegment);
            }

            currentState = INSIDE_TAG;
            currentSegment = new TagSegment();
            currentSegment.insideTag = true;
        }
        currentSegment.append(b);
        if ( b == '>' ) {
            if ( currentState == OUTSIDE_TAG ) {
                //System.err.println("Warning: re-exiting tag");
                //System.err.print("!]");
            }
            segmentList.add(currentSegment);
            currentState = OUTSIDE_TAG;
            currentSegment = new TagSegment();
            currentSegment.insideTag = false;
        }
    }

    /**
    Always erase previous html data, but remember cookies...
    @param is
    */
    public void importDataFromHtml(InputStream is)
    {
        int i;

        currentState = OUTSIDE_TAG;
        if ( segmentList == null ) {
            segmentList = new ArrayList<TagSegment>();
        }
        currentSegment = new TagSegment();
        currentSegment.insideTag = false;
        segmentList.add(currentSegment);

        try {
            //-----------------------------------------------------------------
            byte buffer[] = new byte[4096];
            int bytes;
            while  ( true )  {
                bytes = is.read (buffer);
                if ( bytes <= 0 ) {
                    break;
                }

                for ( i = 0; i < bytes; i++ ) {
                    processByteHtml(buffer[i]);
                }
            }

            //-----------------------------------------------------------------
        }
        catch ( Exception e ) {
            System.err.println("Error reading processing HTML");
        }
    }

    public void exportHtml(OutputStream os)
    {
        TagSegment elem;
        byte []  buffer;
        int i;
        

        for ( i = 0; i < segmentList.size(); i++ ) {
            elem = segmentList.get(i);
            String content = elem.getContent();
            buffer = content.getBytes();
            try {
                os.write(buffer);
            }
            catch ( Exception e ) {
                System.err.println("Error writing to html...");
            }
        }
    }

    public void exportHtml(String filename)
    {
        //-----------------------------------------------------------------
        try {
            File output2 = new File (filename) ;
            FileOutputStream outputStream2;
            outputStream2 = new FileOutputStream(output2);
            exportHtml(outputStream2);
            outputStream2.close();
        }
        catch ( Exception e ) {
            System.err.println("Error exportando tags...");
        }
        //-----------------------------------------------------------------
    }

    public String getUrlFromAHrefContaining(String contentKey)
    {
        int i;
        String tagName;
        String lastUrl = null;

        for ( i = 0; i < segmentList.size(); i++ ) {
            tagName = segmentList.get(i).getTagName();
            if ( tagName != null && tagName.equals("A") ) {
                lastUrl = segmentList.get(i).getTagParameterValue("HREF");
            }
            if ( tagName == null && lastUrl != null ) {
                if ( segmentList.get(i).getContent().contains(contentKey) ) {
                    return trimQuotes(lastUrl);
                }
            }
        }
        return null;
    }

    public HtmlForm getHtmlForm(int index)
    {
        int i;
        String tagName;
        String lastUrl = null;
        HtmlForm form = new HtmlForm();
        int formNumber = -1;
        boolean insideForm = false;

        for ( i = 0; i < segmentList.size(); i++ ) {
            tagName = segmentList.get(i).getTagName();

            if ( tagName != null && tagName.equals("FORM") ) {
                formNumber++;
                insideForm = true;
                ArrayList<TagParameter> tag;
                tag = segmentList.get(i).getTagParameters();
                form.configure(tag);
            }
            if ( tagName != null && tagName.equals("INPUT") &&
		 insideForm && formNumber == index) {
                ArrayList<TagParameter> tag;
                tag = segmentList.get(i).getTagParameters();
                form.addInputFromTag(tag);
            }
            if ( tagName != null && tagName.equals("/FORM") ) {
                insideForm = false;
            }
        }
        return form;
    }

    /**
    Extracts a subpage from `this` TaggedHtml, and returns it
    in the `trimmed` TaggedHtml.  The trimmed page contains
    the `tableIndex` first level table from inside the original
    page.
    @param tableIndex
    @return
    */
    public TaggedHtml extractTrimmedByTable(int tableIndex)
    {
        TaggedHtml trimmed = new TaggedHtml();
        trimmed.segmentList = new ArrayList<TagSegment>();

        int i;
        TagSegment segi;
        String tagName;
        int level = 0;
        int count = -1;
        boolean tableTag = false;

        for ( i = 0; i < segmentList.size(); i++ ) {
            segi = segmentList.get(i);
            tagName = segi.getTagName();
            if ( tagName!= null && tagName.equals("TABLE") ) {
                level++;
                if ( level == 1 ) {
                    count++;
                    tableTag = true;
                }
            }
            if ( tagName!= null && tagName.equals("/TABLE") ) {
                level--;
            }

            if ( level > 0 && count == tableIndex && tableTag == false ) {
                trimmed.segmentList.add(new TagSegment(segi));
            }
            tableTag = false;
        }

        return trimmed;
    }

    public ArrayList<TaggedHtml> extractTableCells()
    {
        ArrayList<TaggedHtml> cells;
        cells = new ArrayList<TaggedHtml>();

        int i;
        TagSegment segi;
        String tagName;
        int level = 0;
        TaggedHtml trimmed = null;

        for ( i = 0; i < segmentList.size(); i++ ) {
            segi = segmentList.get(i);
            tagName = segi.getTagName();
            if ( tagName!= null && tagName.equals("TD") ) {
                level++;
                if ( level == 1 ) {
                    trimmed = new TaggedHtml();
                    trimmed.segmentList = new ArrayList<TagSegment>();
                    cells.add(trimmed);
                }
            }
            if ( tagName!= null && tagName.equals("/TD") ) {
                level--;
            }

            if ( level > 0 && trimmed != null ) {
                trimmed.segmentList.add(new TagSegment(segi));
            }
        }

        return cells;
    }

    private static boolean containsCookie(List<String> cookies, String name) {
        int i;
                
        for ( i = 0; i < cookies.size(); i++ ) {
            if ( cookies.get(i).contains(name) ) {
                return true;
            }
        }
        return false;
    }

    public static String getHostFromURL(String pageUrl) {
        //System.out.println("  * URL: " + pageUrl);
        try {
            String hostname;
            URI u = new URI(pageUrl);
            hostname = u.getHost();
            //System.out.println("  * HOSTNAME: " + hostname);
            return hostname;
        }
        catch ( Exception e ) {
            System.out.println("Error: malformed URL");
            System.exit(9);
        }
        return null;
    }
}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
