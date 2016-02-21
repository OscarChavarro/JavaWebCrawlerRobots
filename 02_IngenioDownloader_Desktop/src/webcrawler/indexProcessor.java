package webcrawler;

// MongoDB classes
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

/**
*/
public class indexProcessor {
    private static final BasicDBObject searchLink = new BasicDBObject();
    private static int lastCategoryId;

    private static void downloadIndexPage(
        String url, String linkName, boolean buildCategories) 
    {
        if ( !url.contains("http://") ) {
            url = "http://www.mppromocionales.com/" + url;
        }
        System.out.println("  - " + url);
        TaggedHtml pageProcessor;
        searchLink.append("sourceUrl", url);
        if ( IngenioDownloader.marPicoElementLink.findOne(
                searchLink) != null ) {
            // If element exist, analyze its children
            pageProcessor = new TaggedHtml();
            pageProcessor.getInternetPage(url);
            findHref(pageProcessor, buildCategories);
        } 
        else {
            // If element does not exist, insert it
            IngenioDownloader.marPicoElementLink.insert(searchLink);
            //System.out.println(searchLink);
        }

        if ( buildCategories && 
             url.contains("/productos.php") && linkName != null ) {
            registerNewCategoryOnDatabase(url, linkName, true, 0);
        }
        searchLink.clear();
    }

    private static void findHref(
        TaggedHtml pageProcessor, boolean buildCategories) 
    {
        TagSegment ts;
        int i;
        int j;
        String n;
        String v;
        boolean insideLink = false;
        String l = "";
        boolean insideStrong = false;
        boolean nextWithStrong = true;

        for ( i = 0; i < pageProcessor.segmentList.size(); i++ ) {
            ts = pageProcessor.segmentList.get(i);
            
            if ( !ts.insideTag && insideLink ) {
                String s;
                s = ts.getContent().replace("&nbsp;", " ");
                s = s.trim();
                searchLink.append("sourceLinkName", s);
                searchLink.append("sourceUrl", l);
                String url = l;
                String linkName = s;
                
                if ( buildCategories && url.contains("/productos.php") ) {
                    registerNewCategoryOnDatabase(
                        url, linkName, nextWithStrong, lastCategoryId);
                }
                if ( IngenioDownloader.marPicoElementLink.findOne(searchLink) 
                        == null ) {
                    try {
                        IngenioDownloader.marPicoElementLink.insert(
                            searchLink);
                    }
                    catch ( Exception e ) {

                    }
                }
                searchLink.clear();
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
                        nextWithStrong = insideStrong;
                    }
                }
                else if ( n.equals("strong") ) {
                    insideStrong = true;
                }
                else if ( n.equals("/strong") ) {
                    insideStrong = false;
                }
            }
        }
    }  

    private static void registerNewCategoryOnDatabase(
        String url, 
        String linkName, 
        boolean isCategory,
        int parentCategoryId) 
        throws NumberFormatException 
    {
        BasicDBObject ca = new BasicDBObject();
        int id;
        if ( !url.contains("cat_id=") ) {
            return;
        }
        int ni = url.indexOf("cat_id=") + 7;
        String nu = url.substring(ni);
        id = Integer.parseInt(nu);
        ca.append("nameSpa", linkName);
        ca.append("id", id);
                
        try {
            IngenioDownloader.marPicoCategory.insert(ca);
            System.out.println("  - New category link: " + linkName);
        }
        catch ( Exception e ) {
            //System.out.println("  * Recategorizando a " + linkName);
            DBObject searchKey = new BasicDBObject("id", id);
            DBObject cc = IngenioDownloader.marPicoCategory.findOne(searchKey);
            if ( cc != null && cc.get("parentCategoryId") != null ) {
                //System.out.println("* No recategorizo a " + linkName);
            }
            else {
                //System.out.println("    . New parent category: " + parentCategoryId);
                //System.out.println("    . New mark: " + isCategory);
                
                if ( isCategory ) {
                    DBObject newValues = new BasicDBObject(
                        new BasicDBObject("$set",
                            new BasicDBObject("parentCategoryId", 0)));
                    IngenioDownloader.marPicoCategory.update(searchKey, newValues);                    
                }
                else if ( parentCategoryId != 0 ) {
                    DBObject newValues = new BasicDBObject(
                        new BasicDBObject("$set",
                            new BasicDBObject("parentCategoryId", parentCategoryId)));
                    IngenioDownloader.marPicoCategory.update(searchKey, newValues);
                }
            }
        }
        if ( isCategory ) {
            //System.out.println("**** IS! --> " + linkName);
            lastCategoryId = id;
        }
    }

    public static void downloadAllProductIndexes(boolean buildCategories) 
    {
        DBCursor c = IngenioDownloader.marPicoElementLink.find();
        int linksBefore = c.count();
        //System.out.println("  * Links before: " + linksBefore);

        String url = "menuproductos.php";
        downloadIndexPage(url, null, buildCategories);

        c = IngenioDownloader.marPicoElementLink.find();
        DBObject ei;

        while ( c.hasNext() ) {
            ei = c.next();
            url = ei.get("sourceUrl").toString();
            String n = null;
            Object sn = ei.get("sourceLinkName");
            if ( sn != null ) {
                n = sn.toString();
            }
            if ( url.contains("productos.php") ) {
                downloadIndexPage(url, n, buildCategories);
            }
        }

        // Second level URLs are for getting product elements, not
        // unregistered categories
        c = IngenioDownloader.marPicoElementLink.find();
        int linksAfter = c.count();
        //System.out.println("  * Links after: " + linksAfter);
        if ( linksBefore != linksAfter ) {
            downloadAllProductIndexes(false);
        }
    }
}
