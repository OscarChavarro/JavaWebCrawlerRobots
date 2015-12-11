//===========================================================================
package databaseMongo;

// Java basic classes
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

// Mongo classes
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mongodb.MongoException;
import databaseMongo.model.CamaraDeComercioData;
import org.bson.types.ObjectId;

// VSDK classes
import vsdk.toolkit.common.VSDK;

// Application specific classes
import databaseMongo.model.ContactData;

/**
*/
public class DatabaseConnection80mil {
    private static DB mongoConnection;
    private static DBCollection marketingClientLead;
    private static DBCollection marketingProfessionalLead;

    static {
        mongoConnection = createMongoConnection();
    }

    /**
    @return 
    */
    public static DB createMongoConnection()
    {
        try {
            MongoClient mongoClient;
            mongoClient = new MongoClient("localhost", 27017);
            mongoConnection = mongoClient.getDB("80mil");
            marketingClientLead = 
                mongoConnection.getCollection("marketingClientLeadBogota");            
            marketingProfessionalLead = 
                mongoConnection.getCollection("marketingProfessionalLead");            
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
    public static void insertContactMongo(ContactData r)
    {
        if ( marketingClientLead == null ) {
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
            if ( r.getEmail() == null || r.getEmail().equals("null") ) {
                System.out.println("    . Saltando contacto vacio");
            }
            else {
                marketingClientLead.insert(newDocument);
            }
        }
        catch ( MongoException e ) {
            
            //System.out.println("    . Saltando contacto - ya existía");
            //System.out.println("    . " + r.getEmail());
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
    @param r
    */
    public static void insertCompanyContactMongo(CamaraDeComercioData r)
    {
        if ( marketingProfessionalLead == null ) {
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
            //if ( r.getCdcbLegalRepresentativeEmail() == null || 
            //     r.getCdcbLegalRepresentativeEmail().equals("null") ) {
            //    System.out.println("    . Saltando contacto vacio: " + 
            //    r.getCdcbCompanyName());
            //}
            //else {
                marketingProfessionalLead.insert(newDocument);
            //}
        }
        catch ( MongoException e ) {
            //System.out.println("    . Saltando contacto - ya existía");
            //System.out.println("    . " + r.getEmail());
            /*
            VSDK.reportMessageWithException(
                null, 
                VSDK.WARNING, 
                "insertResumeMongo", 
                "Hoja de vida ya existente en base de datos", e);
            */
        }
    }
}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
