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
import java.util.HashMap;
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
public class ComputrabajoTaggedHtml extends TaggedHtml
{
    private static final int OUTSIDE_TAG = 1;
    private static final int INSIDE_TAG = 2;
    private int currentState = OUTSIDE_TAG;
    public ArrayList<ComputrabajoTagSegment> segmentList2;
    private ComputrabajoTagSegment currentSegment;
    private final CookieManager cookieManager;

    public ComputrabajoTaggedHtml()
    {
        segmentList2 = null;
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
        segmentList2 = new ArrayList<ComputrabajoTagSegment>();

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
        String pageUrl, 
        ArrayList<String> cookies, 
        boolean withRedirect)
    {
        //----------------------------------------------------------------- 
        CloseableHttpClient httpclient;

        HttpGet connection;
        connection = new HttpGet(pageUrl);

        connection.setProtocolVersion(HttpVersion.HTTP_1_1);
        connection.setHeader("Host", getHostFromURL(pageUrl));
        connection.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        connection.setHeader("Upgrade-Insecure-Requests", "1");
        connection.setHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.80 Safari/537.36");
        connection.setHeader("DNT", "1");
        connection.setHeader("Accept-Encoding", "identity" /*"gzip, deflate, sdch"*/);
        connection.setHeader("Accept-Language", "en-US,en;q=0.8,es;q=0.6");
        //connection.setHeader("Connection", "keep-alive");
        //connection.setHeader("Cache-Control", "max-age=0");
        //connection.setHeader("Referer", "http://empresa.computrabajo.com.co/");
        prepareExistingCookies(cookies, connection);

        //-----------------------------------------------------------------
        if ( !withRedirect ) {
            httpclient = HttpClients.createDefault();
        }
        else {
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
        }
        catch ( ClientProtocolException e ) {
            System.out.println("HTTP redirect error");
        }
        catch ( IOException e ) {
            System.out.println("ERROR BAJANDO ARCHIVO!");
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
                is.close();
                response.close();
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
        try {
            httpclient.close();
        }
        catch ( Exception e ) {
	}
    }

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
        String password,
        HashMap<String, String> initialIdentifiers)
    {
        try {
            HttpPost connection = new HttpPost(pageUrl);

            prepareExistingCookies(cookies, connection);

            connection.setHeader("Host", "empresa.computrabajo.com.co");
            //connection.setHeader("Connection", "keep-alive");
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
            String loginString = "";
            String separator = "";
            
            if ( initialIdentifiers.containsKey("__VIEWSTATE") ) {
                loginString += "__VIEWSTATE" + "=";
                loginString += normalizeStringForWeb(
                    initialIdentifiers.get("__VIEWSTATE"));
                separator = "&";
            }
            if ( initialIdentifiers.containsKey("__VIEWSTATEGENERATOR") ) {
                loginString += separator + "__VIEWSTATEGENERATOR" + "=";
                loginString += normalizeStringForWeb(
                    initialIdentifiers.get("__VIEWSTATEGENERATOR"));
                separator = "&";
            }
            if ( initialIdentifiers.containsKey("__EVENTVALIDATION") ) {
                loginString += separator + "__EVENTVALIDATION" + "=";
                loginString += normalizeStringForWeb(
                    initialIdentifiers.get("__EVENTVALIDATION"));
                separator = "&";
            }
            loginString += separator + "txEmail=" + login + "&txPwd=" + password;
            if ( initialIdentifiers.containsKey("bbR") ) {
                loginString += separator + "bbR" + "=";
                loginString += normalizeStringForWeb(
                    initialIdentifiers.get("bbR"));
            }
            loginString += "&tn=";
            
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
            System.out.println("3. Activating page");
            
            //ComputrabajoCoDownloader.listTagsFromPage(this);
            
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
                segmentList2.add(currentSegment);
            }

            currentState = INSIDE_TAG;
            currentSegment = new ComputrabajoTagSegment();
            currentSegment.insideTag = true;
        }
        currentSegment.append(b);
        if ( b == '>' ) {
            if ( currentState == OUTSIDE_TAG ) {
                //System.err.println("Warning: re-exiting tag");
                //System.err.print("!]");
            }
            segmentList2.add(currentSegment);
            currentState = OUTSIDE_TAG;
            currentSegment = new ComputrabajoTagSegment();
            currentSegment.insideTag = false;
        }
    }

    /**
    Always erase previous html data, but remember cookies...
    @param is
    */
    @Override
    public void importDataFromHtml(InputStream is)
    {
        int i;

        currentState = OUTSIDE_TAG;
        if ( segmentList2 == null ) {
            segmentList2 = new ArrayList<ComputrabajoTagSegment>();
        }
        currentSegment = new ComputrabajoTagSegment();
        currentSegment.insideTag = false;
        segmentList2.add(currentSegment);

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

    @Override
    public void exportHtml(OutputStream os)
    {
        ComputrabajoTagSegment elem;
        byte []  buffer;
        int i;
        

        for ( i = 0; i < segmentList2.size(); i++ ) {
            elem = segmentList2.get(i);
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

    @Override
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

    @Override
    public String getUrlFromAHrefContaining(String contentKey)
    {
        int i;
        String tagName;
        String lastUrl = null;

        for ( i = 0; i < segmentList2.size(); i++ ) {
            tagName = segmentList2.get(i).getTagName();
            if ( tagName != null && tagName.equals("A") ) {
                lastUrl = segmentList2.get(i).getTagParameterValue("HREF");
            }
            if ( tagName == null && lastUrl != null ) {
                if ( segmentList2.get(i).getContent().contains(contentKey) ) {
                    return trimQuotes(lastUrl);
                }
            }
        }
        return null;
    }

    public ComputrabajoHtmlForm getHtmlForm2(int index)
    {
        int i;
        String tagName;
        String lastUrl = null;
        ComputrabajoHtmlForm form = new ComputrabajoHtmlForm();
        int formNumber = -1;
        boolean insideForm = false;

        for ( i = 0; i < segmentList2.size(); i++ ) {
            tagName = segmentList2.get(i).getTagName();

            if ( tagName != null && tagName.equals("FORM") ) {
                formNumber++;
                insideForm = true;
                ArrayList<ComputrabajoTagParameter> tag;
                tag = segmentList2.get(i).getTagParameters();
                form.configure(tag);
            }
            if ( tagName != null && tagName.equals("INPUT") &&
		 insideForm && formNumber == index) {
                ArrayList<ComputrabajoTagParameter> tag;
                tag = segmentList2.get(i).getTagParameters();
                form.addInputFromTag(tag);
            }
            if ( tagName != null && tagName.equals("/FORM") ) {
                insideForm = false;
            }
        }
        return form;
    }

    /**
    Extracts a subpage from `this` ComputrabajoTaggedHtml, and returns it
    in the `trimmed` ComputrabajoTaggedHtml.  The trimmed page contains
    the `tableIndex` first level table from inside the original
    page.
    @param tableIndex
    @return
    */
    @Override
    public ComputrabajoTaggedHtml extractTrimmedByTable(int tableIndex)
    {
        ComputrabajoTaggedHtml trimmed = new ComputrabajoTaggedHtml();
        trimmed.segmentList2 = new ArrayList<ComputrabajoTagSegment>();

        int i;
        ComputrabajoTagSegment segi;
        String tagName;
        int level = 0;
        int count = -1;
        boolean tableTag = false;

        for ( i = 0; i < segmentList2.size(); i++ ) {
            segi = segmentList2.get(i);
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
                trimmed.segmentList2.add(new ComputrabajoTagSegment(segi));
            }
            tableTag = false;
        }

        return trimmed;
    }

    public ArrayList<ComputrabajoTaggedHtml> extractTableCells2()
    {
        ArrayList<ComputrabajoTaggedHtml> cells;
        cells = new ArrayList<ComputrabajoTaggedHtml>();

        int i;
        ComputrabajoTagSegment segi;
        String tagName;
        int level = 0;
        ComputrabajoTaggedHtml trimmed = null;

        for ( i = 0; i < segmentList2.size(); i++ ) {
            segi = segmentList2.get(i);
            tagName = segi.getTagName();
            if ( tagName!= null && tagName.equals("TD") ) {
                level++;
                if ( level == 1 ) {
                    trimmed = new ComputrabajoTaggedHtml();
                    trimmed.segmentList2 = new ArrayList<ComputrabajoTagSegment>();
                    cells.add(trimmed);
                }
            }
            if ( tagName!= null && tagName.equals("/TD") ) {
                level--;
            }

            if ( level > 0 && trimmed != null ) {
                trimmed.segmentList2.add(new ComputrabajoTagSegment(segi));
            }
        }

        return cells;
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
