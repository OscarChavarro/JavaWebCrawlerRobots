package webcrawler;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.Header;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.ProtocolVersion;
import org.apache.http.RequestLine;
import org.apache.http.client.RedirectStrategy;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;

public class MyRedirectStrategy implements RedirectStrategy 
{
    private final HttpGet source;
    private final ArrayList<String> cookies;
    private String redirectUrl = "http://bogotasex.com";
    private Header intermediateHeaders[] = null;
    private HttpParams intermediateParameters = null;
    
    public MyRedirectStrategy(HttpGet connection, ArrayList<String> cookies)
    {
        source = connection;
        this.cookies = cookies;
        
    }
    
    @Override
    public boolean 
    isRedirected(
        HttpRequest hr, 
        HttpResponse intermediateResponse, 
        HttpContext hc)
        throws ProtocolException 
    {
        //System.out.println("  - Determining if is redirected");
        if ( intermediateResponse.getStatusLine().toString().equals("HTTP/1.1 302 Found") ) {
            //System.out.println("    . YES, it is redirected");
            //System.out.println("  - Here, system has access to intermediate page response");
            Header h[] = intermediateResponse.getAllHeaders();
            intermediateHeaders = h;
            int i;
            TaggedHtml.addHeadersToCookies(h, cookies);

            for ( i = 0; i < h.length; i++ ) {
                if ( h[i].getName().equals("Location") ) {
                    redirectUrl = h[i].getValue();
                    //System.out.println("    . Changing redirect location to " + redirectUrl);
                }
            }

            intermediateParameters = intermediateResponse.getParams();
            // Here we have the intermediate content
            //try {
                //TaggedHtml intermediatePage = new TaggedHtml();
                //intermediatePage.importDataFromHtml(intermediateResponse.getEntity().getContent());
                //ComputrabajoVeDownloader.listTagsFromPage(intermediatePage);
            //} 
            //catch (IOException ex) {
            //} 
            //catch (UnsupportedOperationException ex) {
            //}    
            return true;
        }
        //System.out.println("    . NO, it is not redirected, it is " + 
        //    intermediateResponse.getStatusLine());
        return false;
    }

    @Override
    public HttpUriRequest getRedirect(
        HttpRequest hr, HttpResponse hr1, HttpContext hc) 
            throws ProtocolException {
        return new HttpUriRequest() {

            @Override
            public String getMethod() {
                //System.out.println("  - Setting redirection method to GET");
                return "GET";
            }

            @Override
            public URI getURI() {
                
                if ( redirectUrl.startsWith("/") ) {
                    redirectUrl = "http://empresa.computrabajo.com.ve" + redirectUrl;
                }
                
                //System.out.println("  - Setting redirection URL to: " + redirectUrl);
                URI x = null;
                try {
                    x = new URI(redirectUrl);
                    //x = new URI("http://support.endlezztheme.com");
                } 
                catch ( URISyntaxException ex ) {
                    Logger.getLogger(TaggedHtml.class.getName()).log(Level.SEVERE, null, ex);
                }
                return x;
            }

            @Override
            public Header[] getAllHeaders() {
                //System.out.println("  - Getting all headers for REDIRECT page:");
                Header headers[];
                
                /*if ( intermediateHeaders != null ) {
                    headers = intermediateHeaders;
                }
                else {
                    headers = source.getAllHeaders();
                }*/
                headers = source.getAllHeaders();

                int i;
                for ( i = 0; i < headers.length; i++ ) {
                    //System.out.println("    . " + headers[i].getName() + " = " + headers[i].getValue() + ";");
                    if ( headers[i].getName().equals("Host") ) {
                        headers[i] = new BasicHeader("Host", "empresa.computrabajo.com.ve");
                    }
                }
                return headers;
            }

            @Override
            public void setHeaders(Header[] headers) {
                Header h[];
                
                if ( intermediateHeaders != null ) {
                    h = intermediateHeaders;
                }
                else {
                    h = headers;
                }
                
                /*
                System.out.println("  - Setting redirect headers: " + h.length);
                int i;
                for ( i = 0; i < h.length; i++ ) {
                    System.out.println("    . " + h[i].getName() + " = " + h[i].getValue() + ";");
                }
                */
                TaggedHtml.addHeadersToCookies(h, cookies);
            }

            @Override
            public HttpParams getParams() {
                //System.out.println("  - Getting redirect parameters");
                return intermediateParameters;
                
            }

            @Override
            public void removeHeader(Header header) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void removeHeaders(String string) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public HeaderIterator headerIterator() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public HeaderIterator headerIterator(String string) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void setParams(HttpParams hp) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
            
            @Override
            public void abort() throws UnsupportedOperationException {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public boolean isAborted() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public RequestLine getRequestLine() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public ProtocolVersion getProtocolVersion() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public boolean containsHeader(String string) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public Header[] getHeaders(String string) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public Header getFirstHeader(String string) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public Header getLastHeader(String string) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void addHeader(Header header) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void addHeader(String string, String string1) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void setHeader(Header header) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void setHeader(String string, String string1) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

        };    
    }
}
