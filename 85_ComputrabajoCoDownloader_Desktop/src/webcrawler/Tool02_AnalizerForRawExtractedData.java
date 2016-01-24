package webcrawler;

// Java basic classes
import webcrawler.processors.NameProcessor;
import webcrawler.processors.FieldProcessors;
import java.util.HashMap;

// Mongo classes
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

// Application specific classes
import databaseMongo.ComputrabajoDatabaseConnection;
import databaseMongo.model.GeographicAdministrativeRegion;
import databaseMongo.model.NameElement;

/**
*/
public class Tool02_AnalizerForRawExtractedData {
    private static boolean reportAdvances = false;
    private static final ComputrabajoDatabaseConnection databaseConnection;

    static
    {
        databaseConnection = new ComputrabajoDatabaseConnection("localhost" , 
            27017, "computrabajoCo", "professionalResume");
    }    

    public static void main(String args[])
    {
        DBCollection professionalResume;
        professionalResume = 
            databaseConnection.getProfessionalResume();
        if ( professionalResume == null ) {
            return;
        }
        
        DBObject filter = new BasicDBObject();
        BasicDBObject options;
        options = new BasicDBObject("name", true);
        DBCursor c = professionalResume.find(filter, options);
        
        int i;
        HashMap<String, GeographicAdministrativeRegion> regions;
        regions = new HashMap<String, GeographicAdministrativeRegion>();
        HashMap<String, NameElement> nameElements;
        nameElements = new HashMap<String, NameElement>();
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

            if ( o.containsField("location") ) {
                FieldProcessors.processLocation(o, i, c.count(), regions, reportAdvances);
            }
            if ( o.containsField("name") ) {
                NameProcessor.processName(o, i, c.count(), nameElements, reportAdvances);
            }
            //if ( o.containsField("htmlContent") ) {
            //    processHtmlContent(o, i);
            //}
            if ( o.containsField("profilePictureUrl") ) {
              //  processProfilePictureUrl(o, id, i);
            }
            //if ( i >= 2000 ) {
            //    break;
            //}
            reportAdvances = false;
        }
        FieldProcessors.reportResultingAreas(regions);
        NameProcessor.reportNameElements(nameElements);
    }

}
