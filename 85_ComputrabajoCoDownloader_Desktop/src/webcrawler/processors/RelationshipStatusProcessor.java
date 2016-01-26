package webcrawler.processors;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import databaseMongo.model.NameElement;
import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.TreeSet;
import vsdk.toolkit.common.VSDK;
import vsdk.toolkit.io.PersistenceElement;

/**
*/
public class RelationshipStatusProcessor {

    public static void processRelationshipStatus(
        DBObject o, 
        int i, 
        int count, 
        TreeSet<String> relations, 
        boolean reportAdvances) 
    {
        String status = "?";
        
        if ( o == null ) {
	    return;
	}
	if ( o.get("pair") != null ) {
            String source = o.get("pair").toString();
            if ( source.contains("Solt") ) {
                status = "s";
            }
            else if ( source.contains("Casa") ) {
                status = "c";
            }
            else if ( source.contains("Divo") ) {
                status = "d";
            }
            else if ( source.contains("Viud") ) {
                status = "v";
            }
            else {
                //status = source;
            }
        }
        
        if ( !relations.contains(status) ) {
            relations.add(status);
        }
        if ( reportAdvances ) {
            System.out.println("    . Relationship status: [" + status + "]");
        }
    }

    public static void reportResultingRelationshipStatuses(TreeSet<String> relations) {
        System.out.println("Relaciones de pareja encontradas:");
        for ( String r : relations ) {
            System.out.println("  - " + r);
        }
    }

    public static void calculateRelationshipStatus(
        DBCollection professionalResume) 
    {
        try {
            //----
            File fd = new File("./output/updateRelationships.mongo");
            FileOutputStream fos;
            fos = new FileOutputStream(fd);
            System.out.println("Generating relationships...");

            DBObject filter = new BasicDBObject();
            DBCursor c = professionalResume.find(filter);

            int i;
            int unknownCounter = 0;
            int maleCounter = 0;
            int femaleCounter = 0;
            
            for ( i = 0; c.hasNext(); i++ ) {
                DBObject o = c.next();

                if ( o == null || o.get("pair") == null || o.get("_id") == null ) {
                    continue;
                }
                String source = o.get("pair").toString();
                String id = o.get("_id").toString();
                String status = "?";
                if ( source.contains("Solt") ) {
                    status = "s";
                }
                else if ( source.contains("Casa") ) {
                    status = "c";
                }
                else if ( source.contains("Divo") ) {
                    status = "d";
                }
                else if ( source.contains("Viud") ) {
                    status = "v";
                }

                String l = "db.professionalResumeTransformed.update({_id: \"";
                l += id;
                l += "\"}, {$set: {pair: \"" + status + "\"}});";
                PersistenceElement.writeAsciiLine(fos, l);
            }

            System.out.println("Ok, relationship status generated done.");
        }
        catch ( Exception e ) {
            VSDK.reportMessageWithException(
                null, VSDK.FATAL_ERROR, 
                "GenderProcessor.calculateGender", "error on process", e);
        }
    }

}
