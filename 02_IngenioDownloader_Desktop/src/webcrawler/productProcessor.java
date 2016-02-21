package webcrawler;

// Java basic classes
import com.mongodb.BasicDBObject;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;

// MongoDB classes
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.DuplicateKeyException;

// Apache http classes
import org.apache.http.ParseException;

// VSDK classes
import vsdk.toolkit.common.VSDK;

// Application specific classes
import databaseMongo.model.Product;

/**
*/
public class productProcessor {

    public static void FindHrefImage(
        TaggedHtml pageProcessor, String nameProduct) 
    {
        TagSegment ts;
        int i;
        int j;
        String n;
        String v;

        ArrayList<String> listUrlImg = new ArrayList<String>();
        for ( i = 0; i < pageProcessor.segmentList.size(); i++ ) {
            ts = pageProcessor.segmentList.get(i);
            for ( j = 0; j < ts.getTagParameters().size(); j++ ) {
                n = ts.getTagParameters().get(j).name;
                v = ts.getTagParameters().get(j).value;
                if (n.equals("href")) {
                    v = v.replaceAll("\"", "");
                    if ( (v.contains(".jpg") && 
                          v.contains("images/grandes/")) && 
                         (!v.contains("http")) ) {
                        if (v.contains("mailto")) {
                            break;
                        }
                        v = v.replaceAll(" ", "%20");
                        if ( !listUrlImg.contains(
                                "http://www.mppromocionales.com/" + v) ) {
                            listUrlImg.add("http://www.mppromocionales.com/" + v);
                        }
                    }
                }
            }
        }
        /*
        takeImage(nameProduct, listUrlImg);
        */
    }

    private static void takeImage(
        String nameProduct, ArrayList<String> listUrlImg) 
    {
        nameProduct = nameProduct.replaceAll("([^\\w\\.@-])", "");
        File route;
        route = new File("images/" + nameProduct);
        URL url;
        URLConnection urlCon;
        InputStream is;
        FileOutputStream fos;
        int read;
        String urlAux;
        byte[] array = new byte[1000];
        if ( !route.exists() ) {
            route.mkdirs();
        }
        int i;
        for (i = 0; i < listUrlImg.size(); i++) {
            try {
                urlAux = listUrlImg.get(i);
                if (!urlAux.contains("\\s")) {
                    String filename;
                    filename = route + "/" + nameProduct + "_" + i + ".jpg";
                    url = new URL(urlAux);
                    urlCon = url.openConnection();
                    is = urlCon.getInputStream();
                    fos = new FileOutputStream(filename);
                    read = is.read(array);
                    while (read > 0) {
                        fos.write(array, 0, read);
                        read = is.read(array);
                    }
                    is.close();
                    fos.close();
                }
            } 
            catch (IOException e) {
                VSDK.reportMessageWithException(null, VSDK.FATAL_ERROR, "takeImage", "Error downloading individual image", e);
            }
        }
    }

    private static void buildProductEntryFromPage(
        TaggedHtml pageProcessor, String url) 
    {
        if ( pageProcessor.segmentList == null ) {
            System.out.println("Warning: empty page");
        } 
        else {
            Product p;
            p = new Product();
            p.setUrl(url);
            TagSegment ts;
            int i;
            boolean doMaterial = false;
            boolean doMeasures = false;
            boolean doPrintArea = false;
            boolean doBrand = false;
            boolean doPacking = false;
            Date importDate = new Date();

            for (i = 0; i < pageProcessor.segmentList.size(); i++) {
                ts = pageProcessor.segmentList.get(i);
                if ( !ts.insideTag ) {
                    if ( ts.content.contains("MATERIAL") ) {
                        doMaterial = true;
                    } 
                    else if ( doMaterial ) {
                        String n;
                        String d;
                        p.setMaterial(ts.content);
                        n = pageProcessor.segmentList.get(i - 13).content.trim();
                        p.setName(n);
                        d = pageProcessor.segmentList.get(i - 8).content;
                        p.setDescription(d);
                        doMaterial = false;
                    } 
                    else if ( ts.content.contains("MEDIDAS") ) {
                        doMeasures = true;
                    } 
                    else if ( doMeasures ) {
                        p.setMeasures(ts.content);
                        doMeasures = false;
                    } 
                    else if ( ts.content.contains("REA IMPRESI") ) {
                        doPrintArea = true;
                    } 
                    else if ( doPrintArea ) {
                        p.setPrintArea(ts.content);
                        doPrintArea = false;
                    } 
                    else if ( ts.content.contains("MARCA") ) {
                        doBrand = true;
                    } 
                    else if ( doBrand ) {
                        p.setBrand(ts.content);
                        doBrand = false;
                    } 
                    else if ( ts.content.contains("EMPAQUE") ) {
                        doPacking = true;
                    } 
                    else if ( doPacking ) {
                        p.setPacking(ts.content);
                        p.setPrice(0.0);
                        doPacking = false;
                    }
                }
            }
            if ( p.getName() != null && !p.getName().isEmpty() ) {
                try {
                    IngenioDownloader.searchQuery.append("sourceUrl", p.getUrl());
                    IngenioDownloader.searchQuery.append("name", p.getName());
                    IngenioDownloader.searchQuery.append("description", p.getDescription());
                    IngenioDownloader.searchQuery.append("material", p.getMaterial());
                    IngenioDownloader.searchQuery.append("brand", p.getBrand());
                    IngenioDownloader.searchQuery.append("measures", p.getMeasures());
                    IngenioDownloader.searchQuery.append("printArea", p.getPrintArea());
                    IngenioDownloader.searchQuery.append("price", p.getPrice());
                    IngenioDownloader.searchQuery.append("packing", p.getPacking());
                    IngenioDownloader.searchQuery.append("importDate", importDate);
                    try {
                        IngenioDownloader.marPicoProduct.insert(IngenioDownloader.searchQuery);
                        IngenioDownloader.searchQuery.clear();
                        FindHrefImage(pageProcessor, p.getName());
                    } catch (DuplicateKeyException e) {
                        System.out.println("Url already in use: " + url);
                    }
                } 
                catch (ParseException e) {
                    VSDK.reportMessageWithException(null, VSDK.FATAL_ERROR, 
                        "buildProductEntryFromPage", "Parse error", e);
                }
            }
        }
    }

    /**
    Gets specific marPicoProduct from already links on collection "marPicoElementLink".
    Populates collection "productList".
     */
    static void downloadProductCategoryPages() {
        DBCursor c = IngenioDownloader.marPicoElementLink.find();
        while (c.hasNext()) {
            DBObject ei;
            String url;
            String n;
            ei = c.next();
            url = ei.get("sourceUrl").toString();
            Object nn = ei.get("sourceLinkName");
            if ( nn != null ) {
                n = nn.toString();
            }
            else {
                n = "null";
            }
            System.out.println("  - " + url);
            if ( url.contains("detallesvar.php") ) {
                TaggedHtml pageProcessor;
                pageProcessor = new TaggedHtml();
                pageProcessor.getInternetPage(url);
                buildProductEntryFromPage(pageProcessor, url);
            } 
        }
    }
    
}
