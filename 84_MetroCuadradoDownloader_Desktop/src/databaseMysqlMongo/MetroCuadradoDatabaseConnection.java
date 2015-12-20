//===========================================================================
package databaseMysqlMongo;

// Java basic classes
import java.util.ArrayList;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

// MongoDB classes
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import org.bson.types.ObjectId;

// VSDK classes
import vsdk.toolkit.common.VSDK;

// Application specific classes
import databaseMysqlMongo.model.Property;

/**
*/
public class MetroCuadradoDatabaseConnection {
    private static DB mongoConnection;
    private static DBCollection properties;

    static {
        mongoConnection = null;
        properties = null;
    }

    /**
    */
    public static DB createMongoConnection()
    {
        try {
            MongoClient mongoClient;
            mongoClient = new MongoClient("localhost" , 27017);
            mongoConnection = mongoClient.getDB("domolyRobot");
            properties = mongoConnection.getCollection("landPropertyInSale_test");            
        } 
        catch ( UnknownHostException ex ) {
            VSDK.reportMessageWithException(null, VSDK.FATAL_ERROR, "createMongoConnection", "Error connecting", ex);
        }
        return mongoConnection;
    }
    
    /**
    @return 
    */
    public ArrayList<Property> fetchAllPropertiesMongo()
    {
        if ( properties == null ) {
            return null;
        }
        ArrayList<Property> list;
        list = new ArrayList<Property>();
        
        DBCursor c;
        c = properties.find();
        int i = 1;
        while ( c.hasNext() ) {
            DBObject ei = c.next();
            
            Property p = new Property();
            p.importMongoFields(ei);
            System.out.println("Agregando: " + i);
            list.add(p);
            i++;
        }
        
        return list;
    }
    
    /**
    @param p 
    */
    public void insertPropertyMongo(Property p)
    {
        if ( properties == null ) {
            return;
        }
        
        BasicDBObject newDocument;
        
        Date date = new Date();
        ObjectId oid = new ObjectId();
        p.set_id(oid.toHexString());
        newDocument = p.exportMongoDocument();
        
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", 
            Locale.ENGLISH);

        newDocument.append("importDate", format.format(date));
                        
        properties.insert(newDocument);
    }

    public boolean existInMongoDatabase(String url) {
        if ( properties == null ) {
            System.err.println("ERROR: No esta conectado a Mongo");
            System.exit(1);
        }
        
        BasicDBObject filter;
        
        filter = new BasicDBObject("url", url);
        DBObject o = properties.findOne(filter);
        

        if ( o == null ) {
            return false;
        }
        
        //System.out.println("Encontré un predio para la url " + url);
        System.out.println("  - El predio ya estaba en la base de datos, con id " + o.get("_id"));
    
        return true;
    }

    public void removeMongo(String id) {
        if ( properties == null ) {
            return;
        }
        
        ObjectId oid = new ObjectId(id);
        BasicDBObject filter;
        
        filter = new BasicDBObject("_id", oid);
        properties.remove(filter);
    }

    public void updateDateMongo(String id, DateFormat format) {
        if ( properties == null ) {
            return;
        }
        
        try {
            ObjectId oid = new ObjectId(id);
            BasicDBObject filter;

            filter = new BasicDBObject("_id", oid);

            BasicDBObject newDocument;
            newDocument = new BasicDBObject();
            newDocument.append("$set",
                new BasicDBObject().append(
                        "importDate", format.parse("2015-09-16T00:00:00Z")));

            properties.update(filter, newDocument);
            System.out.println("Updating date on " + id);
        }
        catch ( Exception e ) {
            
        }
    }
}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
