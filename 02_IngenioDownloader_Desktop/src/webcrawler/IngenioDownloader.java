package webcrawler;

// Java basic classes
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;

// Apache http classes
import org.apache.http.ParseException;

// MongoDB classes
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.DuplicateKeyException;

// Application specific classes
import databaseMongo.IngenioDatabaseConnection;
import databaseMongo.model.Product;

public class IngenioDownloader {
    private static final IngenioDatabaseConnection databaseConnection;
    private static final DBCollection products;
    private static final DBCollection linkProducts;
    public static BasicDBObject searchQuery = new BasicDBObject();
    public static BasicDBObject searchLink = new BasicDBObject();

    static {
        databaseConnection = new IngenioDatabaseConnection(
            "localhost", 27017, "ingenio", "productList");
        products = databaseConnection.getProperties();
        linkProducts = databaseConnection.createMongoCollection("productLink");
    }

    private static void downloadProductListFromCategoryIndex(String url) 
    {
        if ( !url.contains("http://") ) {
            url = "http://www.mppromocionales.com/" + url;
        }
        System.out.println("  - " + url);
        
        TaggedHtml pageProcessor;
        searchLink.append("sourceUrl", url);

        if ( linkProducts.findOne(searchLink) != null ) {
            pageProcessor = new TaggedHtml();
            pageProcessor.getInternetPage(url);
            findHref(pageProcessor);
        } 
        else {
            linkProducts.insert(searchLink);
            System.out.println(searchLink);
        }
        searchLink.clear();
    }

    private static void downloadProductCategoryPages() 
    {
        TaggedHtml pageProcessor = new TaggedHtml();

        DBCursor c = linkProducts.find();
        DBObject ei;
        String url;

        while ( c.hasNext() ) {
            ei = c.next();
            url = ei.get("sourceUrl").toString();
            System.out.println("  - " + url);
            if ( url.contains("detallesvar.php") ) {
                pageProcessor.getInternetPage(url);
                buildProductEntryFromPage(pageProcessor, url);
                pageProcessor = new TaggedHtml();
            }
        }
    }

    private static void buildProductEntryFromPage(
        TaggedHtml pageProcessor, String url) 
    {
        if ( pageProcessor.segmentList == null ) {
            System.out.println("Warning: empty page");

        } else {
            Product p = new Product();
            TagSegment ts;
            int i;

            boolean doMaterial = false;
            boolean doMeasures = false;
            boolean doPrintArea = false;
            boolean doBrand = false;
            boolean doPacking = false;
            Date importDate = new Date();

            p.setUrl(url);

            for (i = 0; i < pageProcessor.segmentList.size(); i++) {
                ts = pageProcessor.segmentList.get(i);

                if (!ts.insideTag) {
                    if (ts.content.contains("MATERIAL")) {
                        doMaterial = true;
                    } 
                    else if (doMaterial) {
                        String n;
                        String d;
                        p.setMaterial(ts.content);
                        n = pageProcessor.segmentList.get(i - 13).content.trim();
                        p.setName(n);
                        d = pageProcessor.segmentList.get(i - 8).content;
                        p.setDescription(d);
                        doMaterial = false;
                    } 
                    else if (ts.content.contains("MEDIDAS")) {
                        doMeasures = true;
                    } 
                    else if (doMeasures) {
                        p.setMeasures(ts.content);
                        doMeasures = false;
                    } 
                    else if (ts.content.contains("REA IMPRESI")) {
                        doPrintArea = true;
                    } 
                    else if (doPrintArea) {
                        p.setPrintArea(ts.content);
                        doPrintArea = false;
                    } 
                    else if (ts.content.contains("MARCA")) {
                        doBrand = true;
                    } 
                    else if (doBrand) {
                        p.setBrand(ts.content);
                        doBrand = false;
                    } 
                    else if (ts.content.contains("EMPAQUE")) {
                        doPacking = true;
                    } 
                    else if (doPacking) {
                        p.setPacking(ts.content);
                        p.setPrice(0.0);
                        doPacking = false;
                    }
                }
            }

            if ( p.getName() != null && !p.getName().isEmpty() ) {
                try {
                    searchQuery.append("sourceUrl", p.getUrl());
                    searchQuery.append("name", p.getName());
                    searchQuery.append("description", p.getDescription());
                    searchQuery.append("material", p.getMaterial());
                    searchQuery.append("brand", p.getBrand());
                    searchQuery.append("measures", p.getMeasures());
                    searchQuery.append("printArea", p.getPrintArea());
                    searchQuery.append("price", p.getPrice());
                    searchQuery.append("packing", p.getPacking());
                    searchQuery.append("importDate", importDate);

                    try {
                        products.insert(searchQuery);
                        searchQuery.clear();
                        FindHrefImage(pageProcessor, p.getName());
                    } catch ( DuplicateKeyException e ) {
                        System.out.println("Url already in use: " + url);
                    }
                } catch ( ParseException e ) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static void takeImage(
        String nameProduct, ArrayList<String> listUrlImg) throws IOException 
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
        for ( i = 0; i < listUrlImg.size(); i++ ) {
            try {
                urlAux = listUrlImg.get(i);
                if ( !urlAux.contains("\\s") ) {
                    String filename;
                    filename = route + "/" + 
                        nameProduct + "_" + i + ".jpg";

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
            } catch ( IOException e ) {
                e.printStackTrace();
            }
        }
    }

    public static void FindHrefImage(TaggedHtml pageProcessor, String nameProduct) {
        TagSegment ts;
        int i, j;
        String n;
        String v;
        ArrayList<String> listUrlImg = new ArrayList<String>();

        for (i = 0; i < pageProcessor.segmentList.size(); i++) {
            ts = pageProcessor.segmentList.get(i);

            for (j = 0; j < ts.getTagParameters().size(); j++) {
                n = ts.getTagParameters().get(j).name;
                v = ts.getTagParameters().get(j).value;
                if (n.equals("href")) {
                    v = v.replaceAll("\"", "");
                    if ((v.contains(".jpg") && v.contains("images/grandes/")) && (!v.contains("http"))) {
                        if (v.contains("mailto")) {
                            break;
                        }
                        v = v.replaceAll(" ", "%20");
                        if (!listUrlImg.contains("http://www.mppromocionales.com/" + v)) {
                            listUrlImg.add("http://www.mppromocionales.com/" + v);
                        }
                    }
                }
            }
        }
        try {
            takeImage(nameProduct, listUrlImg);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void findHref(TaggedHtml pageProcessor) {
        TagSegment ts;
        int i, j;
        String n;
        String v;

        for (i = 0; i < pageProcessor.segmentList.size(); i++) {
            ts = pageProcessor.segmentList.get(i);

            for (j = 0; j < ts.getTagParameters().size(); j++) {
                n = ts.getTagParameters().get(j).name;
                v = ts.getTagParameters().get(j).value;
                if (n.equals("href")) {
                    v = v.replaceAll("\"", "");
                    v = v.replaceAll("/", "");
                    if ( v.contains("detallesvar.php") || 
                         v.contains("productos.php") ) {
                        if (v.contains("mailto")) {
                            break;
                        }
                        searchLink.append("sourceUrl", "http://www.mppromocionales.com/" + v);
                        if (linkProducts.findOne(searchLink) == null) {
                            linkProducts.insert(searchLink);
                            searchLink.clear();
                        } else {
                            searchLink.clear();
                        }
                    }
                }
            }
        }
    }

    public static void main(String[] args) {
        String url = "menuproductos.php";

        System.out.println("Downloading product indexes...");
        downloadProductListFromCategoryIndex(url);

        DBCursor c = linkProducts.find();
        DBObject ei;

        while ( c.hasNext() ) {
            ei = c.next();
            url = ei.get("sourceUrl").toString();
            if ( url.contains("productos.php") ) {
                downloadProductListFromCategoryIndex(url);
            }
        }
        System.out.println("complete");

        System.out.println("Downloading product category pages");
        downloadProductCategoryPages();
        System.out.println("complete");
    }
}
