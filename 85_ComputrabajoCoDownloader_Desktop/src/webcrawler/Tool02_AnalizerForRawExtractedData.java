//===========================================================================
package webcrawler;

// Java basic classes
import java.util.HashMap;

// Mongo classes
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

// Application specific classes
import databaseMongo.ComputrabajoMongoDatabaseConnection;
import databaseMongo.model.GeographicAdministrativeRegion;
import databaseMongo.model.NameElement;
import webcrawler.processors.NameProcessor;
import webcrawler.processors.FieldProcessors;
import webcrawler.processors.LocationProcessor;

/**
This tool is deprecated. The initial intent for this program was to analize
information from incoming data in raw source database. It is advised to make 
this kind of analysis over filtered/processed database (that builded by
stage 3 Tool - TransformationFromRawData2CleanData).
*/
@Deprecated
public class Tool02_AnalizerForRawExtractedData {
    private static boolean reportAdvances = false;
    private static final ComputrabajoMongoDatabaseConnection databaseConnection;

    static
    {
        databaseConnection = new ComputrabajoMongoDatabaseConnection(
            "localhost", 
            27017, 
            "computrabajoCo", 
            "professionalResume");
    }    

    private static void processElementsInQuery(
        DBCursor c, 
        HashMap<String, GeographicAdministrativeRegion> regions, 
        HashMap<String, NameElement> nameElements) 
    {
        int i;
        for ( i = 0; c.hasNext(); i++ ) {
            DBObject o = c.next();
            
            if ( o.get("_id") == null ) {
                continue;
            }
            
            String _id = o.get("_id").toString();
            
            if ( i % 10000 == 0 ) {
                reportAdvances = true;                
            }
            if ( reportAdvances ) {
                System.out.println(
                    "  - (" + (i+1) + " of " + c.count() + "): " + _id); 
            }

            if ( o.containsField("location") ) {
                LocationProcessor.processLocation(
                        o, i, c.count(), regions, reportAdvances);
            }
            if ( o.containsField("name") ) {
                NameProcessor.processName(
                        o, i, c.count(), nameElements, reportAdvances);
            }
            //if ( o.containsField("htmlContent") ) {
            //    processHtmlContent(o, i);
            //}
            //if ( o.containsField("profilePictureUrl") ) {
            //    processProfilePictureUrl(o, id, i);
            //}
            reportAdvances = false;
        }
    }

    public static void main(String args[])
    {
        DBCollection professionalResume;
        professionalResume = databaseConnection.getProfessionalResume();
        if ( professionalResume == null ) {
            return;
        }
        
        DBObject filter = new BasicDBObject();
        BasicDBObject options;
        options = new BasicDBObject();
        //options.append("name", true);
        DBCursor c = professionalResume.find(filter, options);
        
        HashMap<String, GeographicAdministrativeRegion> regions;
        regions = new HashMap<String, GeographicAdministrativeRegion>();
        HashMap<String, NameElement> nameElements;
        nameElements = new HashMap<String, NameElement>();

        processElementsInQuery(c, regions, nameElements);
        
        // Export analysis results
        LocationProcessor.reportResultingAreas(regions);
        NameProcessor.reportNameElements(nameElements);
    }
}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
