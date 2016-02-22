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
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.URL;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.ParseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.json.JSONArray;
import org.json.JSONObject;

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

    protected String normalizeStringForWeb(String a) {
        char keys[] = {
            ' ',
            '!',
            '"',
            '#',
            '$',
            '%',
            '&',
            '\'',
            '(',
            ')',
            '*',
            '+',
            ',',
            '-',
            '.',
            '/',
            ':',
            ';',
            '<',
            '=',
            '>',
            '?',
            '@',
            '[',
            '\\',
            ']',
            '^',
            '_',
            '`',
            '{',
            '|',
            '}',
            '~',
	     ' '};
        String values[] = {
            "%20",
            "%21",
            "%22",
            "%23",
            "%24",
            "%25",
            "%26",
            "%27",
            "%28",
            "%29",
            "%2A",
            "%2B",
            "%2C",
            "%2D",
            "%2E",
            "%2F",
            "%3A",
            "%3B",
            "%3C",
            "%3D",
            "%3E",
            "%3F",
            "%40",
            "%5B",
            "%5C",
            "%5D",
            "%5E",
            "%5F",
            "%60",
            "%7B",
            "%7C",
            "%7D",
            "%7E",
	    "%7F"};
        int i;
        int j;
        
        String b = "";
        for ( i = 0; i < a.length(); i++ ) {
            char c = a.charAt(i);
            String code = "" + c;
            for ( j = 0; j < keys.length; j++ ) {
                if ( c == keys[j] ) {
                    code = values[j];
                    break;
                }
            }
            b += code;
        }

        return b;
    }

        protected void addRecievedCookies(
        CloseableHttpResponse response, ArrayList<String> cookies) 
            throws ParseException {
        //-----------------------------------------------------------------
        // Append new cookies
        Header hs[] = response.getAllHeaders();

        if ( hs != null ) {
            addHeadersToCookies(hs, cookies);
        }
    }
        
    protected static void addHeadersToCookies(
        Header[] hs, ArrayList<String> cookies) throws ParseException 
    {
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

    protected void prepareExistingCookies(
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
        //val += " _ga=GA1.3.58098705.1450971763; _gat=1";
        if ( cookies.size() > 0 ) {
            connection.setHeader("Cookie", val);
        }
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
    private boolean importDataFromJson(
        InputStream is, 
        AtomicInteger pageNumber,
        AtomicInteger pages)
    {
        int i;
        boolean isLast = true;

        segmentList = new ArrayList<TagSegment>();

        try {
            //-----------------------------------------------------------------
            byte []  buffer = new byte [4096];

            String msg = "";

            int bytes;
            HashMap<String, String> jsonVariables;
            jsonVariables = new HashMap<String, String>();

            
            while ( true )  {
                bytes = is.read(buffer);
                if ( bytes <= 0 ) {
                    break;
                }

                //for ( i = 0; i < bytes; i++ ) {
                //    processByteJson(jsonVariables, buffer[i]);
                //}

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
                return false;
            }
            
            //-----------------------------------------------------------------
            Set<String> keys;
            keys = jsonObject.keySet();

            for ( Object o : keys ) {
                String s = o.toString();
                Object v = jsonObject.get(s);

                //System.out.println("JSON: " + s);

                if ( s.contains("inmueblesHTML") ) {
                    if ( v instanceof JSONArray ) {
                        JSONArray arr = (JSONArray)v;
                        //System.out.println("  - Segmentos HTML: " + arr.length());
                        for ( i = 0; i < arr.length(); i++ ) {
                            currentState = OUTSIDE_TAG;
                            InputStream iss = new ByteArrayInputStream(arr.get(i).toString().getBytes());
                            importDataFromHtml(iss);
                        }
                    }
                }
                else if ( s.contains("esUltimaPagina") ) {
                    if ( v instanceof Boolean ) {
                        Boolean b = (Boolean)v;
                        
                        isLast = b;  
                    }
                }
                else if ( s.contains("numeroPaginaActual") ) {
                    if ( v instanceof String ) {
                        String ss = (String)v;
                        pageNumber.set(Integer.parseInt(ss));                        
                    }
                }
                else if ( s.contains("numeroPaginas") ) {
                    if ( v instanceof String ) {
                        String ss = (String)v;
                        pages.set(Integer.parseInt(ss));                        
                    }
                }
            }
            //-----------------------------------------------------------------
        }
        catch ( IOException | NumberFormatException e ) {
            System.err.println("Error reading processing HTML");
        }

        return isLast;
    }

    /**
    Creates a new html based web transaction from the specified pageUrl,
    without taking into account any input cookies.
    TODO: Change this to only call another method, one with cookies handling!
    @param pageUrl
    @param mcookies
    @return cookies
    */
    public ArrayList<String> getInternetPage(
        String pageUrl, ArrayList<String> mcookies)
    {
        //-----------------------------------------------------------------
        HttpURLConnection connection;
        URL server;
        int code;
        
        try {
            server = new URL(pageUrl);

            connection = (HttpURLConnection)server.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            code = connection.getResponseCode();

            //-----------------------------------------------------------------
            int hi = 1;

            String headerline;

            while ( true ) {
                headerline = connection.getHeaderField(hi);
                if ( headerline != null ) {
                    //System.out.println(headerline);
                }
                else {
                    break;
                }
                hi++;
            }

            //-----------------------------------------------------------------
            InputStream is;
            is = connection.getInputStream();

            importDataFromHtml(is);

            //-----------------------------------------------------------------
            Map<String, List<String>> hfs;
            hfs = connection.getHeaderFields();

            addRecievedCookies(hfs, mcookies);
            
            connection.disconnect();
        }
        catch ( Exception e ) 
        {
        	System.out.println("HTTP Error");
        }
        //-----------------------------------------------------------------
        return mcookies;
    }

    private void addRecievedCookies(Map<String, List<String>> hfs, List<String> cookies1) {
        Set<String> s = hfs.keySet();
        //System.out.println("Page response elements");
        for ( Object o : s ) {
            if (o != null) {
                String os = (String)o.toString();
                //System.out.println("  - SET: " + o);
                if (os != null && os.equals("Set-Cookie")) {
                    List<String> lcookies;
                    lcookies = (List<String>)hfs.get(o.toString());
                    for (Object l : lcookies) {
                        if (l != null) {
                            String sl = l.toString();
                            if ( sl.length() > 0 ) {
                                //System.out.println("    . [" + sl + "]");
                                cookies1.add(sl);
                            }
                        }
                    }
                }
            }
        }
    }


    private void processByteHtml(byte b)
    {
        if ( b == '<' ) {
            if ( currentState == INSIDE_TAG ) {
                //System.err.println("Warning: re-entering tag");
                //System.err.print("[!");
            }

            if ( currentSegment.content != null &&
                 currentSegment.content.length() > 0 &&
                 !justSpaces(currentSegment.content) ) {
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
            buffer = elem.content.getBytes();
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
                if ( segmentList.get(i).content.contains(contentKey) ) {
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

    protected static boolean containsCookie(List<String> cookies, String name) {
        int i;
                
        for ( i = 0; i < cookies.size(); i++ ) {
            if ( cookies.get(i).contains(name) ) {
                return true;
            }
        }
        return false;
    }
}

//===========================================================================
//= EOF                                                                     =
//===========================================================================

