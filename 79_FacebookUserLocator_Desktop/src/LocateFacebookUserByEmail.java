//===========================================================================   

// Java basic classes
import java.io.IOException;

// Swing/Awt classes                                                            
import java.awt.Robot;
import java.awt.Toolkit;

// Toolkit classes
import awt.RobotUtils;
import com.mongodb.BasicDBList;
import java.awt.AWTException;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.net.UnknownHostException;

// MongoDB classes
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import java.util.ArrayList;
import java.util.Date;

/**
*/
public class LocateFacebookUserByEmail {
    public static void main(String args[])
    {
        try {
            //----
            MongoClient mongoClient1;
            mongoClient1 = new MongoClient("localhost", 27017);
            MongoClient mongoClient2;
            mongoClient2 = new MongoClient("localhost", 27018);
            DB transformed;
            DB origin;
            transformed = mongoClient1.getDB("computrabajoCo");
            origin = mongoClient2.getDB("computrabajoCo");
            DBCollection professionalResumeTransformed;
            professionalResumeTransformed = 
                transformed.createCollection(
                    "professionalResumeTransformed", null);
            DBCollection professionalResume;
            professionalResume = 
                origin.createCollection(
                    "professionalResume", null);

            //----            
            Toolkit.getDefaultToolkit();
            Robot r;
            r = new Robot();
                        
            //----            
            BasicDBObject searchFilter;

            searchFilter = defineSearchCriteria();            
            //---
            
            DBObject options = new BasicDBObject("email", 1);

            System.out.println("Buscando elementos en la base de datos...");
            DBCursor c;
            c = professionalResumeTransformed.find(searchFilter, options);


            int n = c.count();
            System.out.println("Correos a bajar:" + n);
            int i;

            r.delay(2000);

            ArrayList<String> emails = new ArrayList<String>();
            for ( i = 1; c.hasNext(); i++ ) {
                DBObject e = c.next();
                if ( e == null ) {
                    continue;
                }
                Object ee;
                ee = e.get("email");
                if ( ee == null ) {
                    continue;
                }
                String email = ee.toString();
                emails.add(email);
            }
            
            for ( i = 0; i < emails.size(); i++ ) {
                String email = emails.get(i);
                System.out.println("  - (" + (i+1) + "/" + n + ")" + email);
                searchFacebookAccountByMail(
                    r, email, professionalResumeTransformed, professionalResume);
                
                if ( i >= 10 ) {
                    //System.exit(1);
                }                
            }
        } 
        catch ( UnknownHostException e ) {
        }
        catch ( AWTException ex ) {
        }
    }

    private static BasicDBObject defineSearchCriteria() {
        BasicDBObject searchFilter;
        searchFilter = new BasicDBObject();
        BasicDBList arr;
        BasicDBObject filter;
        //---
        arr = new BasicDBList();
        
        filter = new BasicDBObject("location",
                new BasicDBObject("$regex", ".*ogot*"));
        arr.add(filter);
        
        filter = new BasicDBObject("facebookUrl",
                new BasicDBObject("$exists", 0));
        arr.add(filter);
        
        filter = new BasicDBObject("gender", "f");
        arr.add(filter);
        
        filter = new BasicDBObject("age", new BasicDBObject("$lte", 16));
        arr.add(filter);
        searchFilter.append("$and", arr);
        return searchFilter;
    }

    private static void searchFacebookAccountByMail(
        Robot r, 
        String email, 
        DBCollection professionalResumeTransformed, 
        DBCollection professionalResume) 
    {
        try {
            int xBase = 1440;
            int yBase = 0;
            
            // INTRO: start in home
            //r.delay(500);
            //r.mouseMove(xBase + 1020, yBase + 130);
            //r.delay(100);
            //r.mouseMove(xBase + 1022, yBase + 130);
            //RobotUtils.click(r);
            //r.delay(1500);
            
            // PART A: select facebook search area
            r.delay(500);
            r.mouseMove(xBase + 800, yBase + 130);
            r.delay(100);
            r.mouseMove(xBase + 802, yBase + 130);
            RobotUtils.click(r);
            r.delay(500);
            
            // PART B: paste email into Facebook search area
            ClipboardOwner clipboardOwner;
            clipboardOwner = null;
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            RobotUtils.writeStringCPWithDelay(email, clipboard, clipboardOwner, r, 1500);
            r.delay(1000);
            
            // PART C: select browser URL text area
            r.delay(500);
            r.mouseMove(xBase + 800, yBase + 72);
            r.delay(100);
            r.mouseMove(xBase + 802, yBase + 72);
            RobotUtils.click(r);
            
            // PART D: copy URL into clipboard
            RobotUtils.copyMacro(r);
            String response;
            
            clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            
            response = (String)clipboard.getData(DataFlavor.stringFlavor);
            
            String validated = getFacebookProfileUrl(response);
            
            System.out.println("    . " + validated);
            updateFacebookInfoOnDatabaseCollection(
                professionalResume, email, validated);
            updateFacebookInfoOnDatabaseCollection(
                professionalResumeTransformed, email, validated);
        } 
        catch (UnsupportedFlavorException ex) {

        } 
        catch (IOException ex) {

        }
    }

    private static void updateFacebookInfoOnDatabaseCollection(
        DBCollection professionalResume,
        String email, 
        String validated) 
    {
        // Update databases
        DBObject searchKey = new BasicDBObject("email", email);
        BasicDBObject options;
        options = new BasicDBObject("_id", 1);
        DBCursor c = professionalResume.find(searchKey, options);
        while ( c.hasNext() ) {
            DBObject e = c.next();
            BasicDBObject newValues = new BasicDBObject();
            Date d = new Date();
            newValues.append("$set",
                    new BasicDBObject("facebookUrl", validated).
                            append("facebookCheckDate", d));
            professionalResume.update(e, newValues);
        }
    }

    /**
    Return facebook URL if this is a valid profile, null if not
    */
    private static String getFacebookProfileUrl(String url) {
        if ( url.contains("https://www.facebook.com/search/top/") ) {
            return "?";
        }
        return url.replace("?fref=ts", "");
    }
}

//===========================================================================   
//= EOF                                                                     =
//===========================================================================   
