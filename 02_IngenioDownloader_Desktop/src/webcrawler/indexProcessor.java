package webcrawler;

// MongoDB classes
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

/**
*/
public class indexProcessor {

    public static void downloadAllProductIndexes() 
    {
        DBCursor c = IngenioDownloader.productLink.find();
        int linksBefore = c.count();
        System.out.println("  * Links before: " + linksBefore);

        String url = "menuproductos.php";
        downloadIndexLinks(url);
        c = IngenioDownloader.productLink.find();
        DBObject ei;
        
        while ( c.hasNext() ) {
            ei = c.next();
            url = ei.get("sourceUrl").toString();
            if ( url.contains("productos.php") ) {
                downloadIndexLinks(url);
            }
        }

        c = IngenioDownloader.productLink.find();
        int linksAfter = c.count();
        System.out.println("  * Links after: " + linksAfter);
        if ( linksBefore != linksAfter ) {
            downloadAllProductIndexes();
        }
    }

    private static void downloadIndexLinks(String url) {
        if ( !url.contains("http://") ) {
            url = "http://www.mppromocionales.com/" + url;
        }
        System.out.println("  - " + url);
        TaggedHtml pageProcessor;
        IngenioDownloader.searchLink.append("sourceUrl", url);
        if ( IngenioDownloader.productLink.findOne(
                IngenioDownloader.searchLink) != null ) {
            pageProcessor = new TaggedHtml();
            pageProcessor.getInternetPage(url);
            findHref(pageProcessor);
        } 
        else {
            IngenioDownloader.productLink.insert(IngenioDownloader.searchLink);
            System.out.println(IngenioDownloader.searchLink);
        }
        IngenioDownloader.searchLink.clear();
    }

    private static void findHref(TaggedHtml pageProcessor) {
        TagSegment ts;
        int i;
        int j;
        String n;
        String v;
        boolean insideLink = false;
        String l = "";
        
        for ( i = 0; i < pageProcessor.segmentList.size(); i++ ) {
            ts = pageProcessor.segmentList.get(i);
            
            if ( !ts.insideTag && insideLink ) {
                String s;
                s = ts.getContent().replace("&nbsp;", " ");
                s = s.trim();
                IngenioDownloader.searchLink.append(
                    "sourceLinkName", s);
                IngenioDownloader.searchLink.append(
                    "sourceUrl", l);
                String url = l;
                
                if ( url.contains("/productos.php") ) {
                    System.out.println("  * Category link:" + s);
                    BasicDBObject ca = new BasicDBObject();
                    int id;
                    if ( !url.contains("cat_id=") ) {
                        continue;
                    }
                    int ni = url.indexOf("cat_id=") + 7;
                    String nu = url.substring(ni);
                    id = Integer.parseInt(nu);
                    ca.append("nameSpa", s);
                    ca.append("id", id);
                    try {
                        IngenioDownloader.category.insert(ca);
                    }
                    catch ( Exception e ) {

                    }
                }
                if ( IngenioDownloader.productLink.findOne(
                        IngenioDownloader.searchLink) == null ) {
                    try {
                        IngenioDownloader.productLink.insert(
                            IngenioDownloader.searchLink);
                    }
                    catch ( Exception e ) {

                    }
                }
                IngenioDownloader.searchLink.clear();
                insideLink = false;
            }
            
            for ( j = 0; j < ts.getTagParameters().size(); j++ ) {
                n = ts.getTagParameters().get(j).name;
                v = ts.getTagParameters().get(j).value;
                if ( n.equals("href") ) {
                    v = v.replaceAll("\"", "");
                    v = v.replaceAll("/", "");
                    if ( v.contains("detallesvar.php") || 
                         v.contains("productos.php") ) {
                        if ( v.contains("mailto") ) {
                            break;
                        }
                        l = "http://www.mppromocionales.com/" + v;
                        insideLink = true;
                    }
                }
            }
        }
    }  
}
