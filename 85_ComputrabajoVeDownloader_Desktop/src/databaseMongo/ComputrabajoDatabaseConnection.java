//===========================================================================
package databaseMongo;

// Java basic classes
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TreeSet;

// Mongo classes
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;
import com.mongodb.MongoException;
import org.bson.types.ObjectId;

// VSDK classes
import vsdk.toolkit.common.VSDK;

// Application specific classes
//import databaseMysql.model.Property;
import databaseMongo.model.Resume;

/**
*/
public class ComputrabajoDatabaseConnection {
    private static DB mongoConnection;
    private static DBCollection professionalResume;

    static {
        mongoConnection = createMongoConnection();
    }

    public static DB createMongoConnection()
    {
        try {
            MongoClient mongoClient;
            mongoClient = new MongoClient("localhost", 27017);
            mongoConnection = mongoClient.getDB("domolyRobot");
            professionalResume = 
                mongoConnection.getCollection("professionalResume");            
        }
        catch ( UnknownHostException ex ) {
            VSDK.reportMessageWithException(
                null, 
                VSDK.FATAL_ERROR, 
                "createMongoConnection", 
                "Error connecting", 
                ex);
        }
        return mongoConnection;
    }

    /**
    @param r
    */
    public static void insertResumeMongo(Resume r)
    {
        if ( getProfessionalResume() == null ) {
            return;
        }
        
        BasicDBObject newDocument;

        Date date = new Date();
        ObjectId oid = new ObjectId();
        r.set_id(oid.toHexString());
        newDocument = r.exportMongoDocument();

        DateFormat format = new SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss'Z'", 
            Locale.ENGLISH);

        newDocument.append("importDate", format.format(date));

        try {
            if ( r.getName() == null || r.getName().equals("null") ) {
                System.out.println("    . Saltando hoja de vida vacia");
            }
            else {
                getProfessionalResume().insert(newDocument);
            }
        }
        catch ( MongoException e ) {
            System.out.println("    . Saltando hoja de vida - ya existía "+
                "(debería actualizarse?)");
            System.out.println("    . " + r.getSourceUrl());
            /*
            VSDK.reportMessageWithException(
                null, 
                VSDK.WARNING, 
                "insertResumeMongo", 
                "Hoja de vida ya existente en base de datos", e);
            */
        }
    }

    /**
     * @return the professionalResume
     */
    public static DBCollection getProfessionalResume() {
        return professionalResume;
    }

    public static void checkExistingResumesOnDatabase(
        TreeSet<String> resumeListAlreadyDownloaded) 
    {
        System.out.println("5. Importing all URLs loaded in database... ");
        BasicDBObject query;
        BasicDBObject options;
        
        query = new BasicDBObject();
        options = new BasicDBObject();
        options.append("sourceUrl", true);
        DBCursor c = professionalResume.find(query, options);

        System.out.println("  - 5.1. Importing database entries...");
        int i;

        for ( i = 0; c.hasNext(); i++ ) {
            if ( i % 10000 == 0 ) {
                System.out.println("     . " + i);
            }
            Object o = c.next().get("sourceUrl");

            if ( o == null ) {
                continue;
            }
            
            String url = o.toString();
            // Trim "http://empresa.computrabajo.com.ve" out
            url = url.substring(34);            
            if ( url != null && !url.equals("null") ) {
                resumeListAlreadyDownloaded.add(url);
            }
        }

        System.out.println("  - 5.2. Number of resumes already imported in database: " + i);
    }
}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
