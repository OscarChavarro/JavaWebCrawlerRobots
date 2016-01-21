package webcrawler;

import java.util.TreeSet;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import databaseMongo.ComputrabajoDatabaseConnection;

/**
*/
public class Tool04_AnalizerForCleanData {
    private static boolean reportAdvances = false;
    private static final ComputrabajoDatabaseConnection databaseConnection;

    static 
    {
        databaseConnection = new ComputrabajoDatabaseConnection(
            "localhost" , 27017, "computrabajoCo", "professionalResumeTransformed");
    }
    
    public static void main(String args[]) {
        DBCollection professionalResume;
        professionalResume = 
            databaseConnection.getProfessionalResume();
        if ( professionalResume == null ) {
            return;
        }
        
        DBObject filter = new BasicDBObject();
        BasicDBObject options = new BasicDBObject("profilePictureUrl", true);
        //options.append("sort", new BasicDBObject("name", 1));
        DBCursor c = professionalResume.find(filter /*, options */);
        
        int i;
        TreeSet <String> professions;
        professions = new TreeSet<String>();
        for ( i = 0; c.hasNext(); i++ ) {
            DBObject o = c.next();
            
            if ( o.get("_id") == null ) {
                continue;
            }
            
            String id = o.get("_id").toString();
            
            if ( i % 10000 == 0 ) {
                reportAdvances = true;                
            }
            if ( reportAdvances ) {
                System.out.println(
                    "  - (" + (i+1) + " of " + c.count() + "): " + id); 
            }

            if ( o.containsField("professionHint") ) {
                processProfessionHint(o, i, c.count(), professions);
            }

            if ( o.containsField("location") ) {
                //processLocation(o, i, c.count(), regions);
            }
            if ( o.containsField("name") ) {
                //processName(o, i, c.count(), nameElements);
            }
            if ( o.containsField("htmlContent") ) {
                //processHtmlContent(o, i);
            }
            if ( o.containsField("profilePictureUrl") ) {
              //  processProfilePictureUrl(o, id, i);
            }
            //if ( i >= 2000 ) {
            //    break;
            //}
            reportAdvances = false;
        }
        reportResultingProfessionHints(professions);
    }    

    private static void processProfessionHint(
        DBObject o, int i, int count, TreeSet<String> professions) {
        String p = o.get("professionHint").toString();
        
        if ( !professions.contains(p) ) {
            professions.add(p);
        }
    }

    private static void reportResultingProfessionHints(TreeSet<String> professionHints) {
        System.out.println("Number of professions found: " + professionHints.size());
        for ( String si : professionHints ) {
            System.out.println("  - " + si);            
        }
    }

}
