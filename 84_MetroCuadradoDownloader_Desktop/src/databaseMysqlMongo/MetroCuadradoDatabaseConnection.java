//===========================================================================
package databaseMysqlMongo;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import org.bson.types.ObjectId;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

// Application specific classes
import databaseConnection.DatabaseMongoConnection;
import databaseMysqlMongo.model.Property;

/**
*/
public class MetroCuadradoDatabaseConnection extends DatabaseMongoConnection{
    
	private static DBCollection properties;

    static 
    {
        properties = null;
    }

    public MetroCuadradoDatabaseConnection(
        String url, int port, String connectionName, String collectionName) {
            super(url, port, connectionName);
            properties = getMongoConnection().getCollection(collectionName);
    }
    
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
    
    public ArrayList<Property> _fetchAllPropertiesMongo()
    {
        if ( properties == null ) 
		{
            return null;
        }
        ArrayList<Property> list;
        list = new ArrayList<Property>();
        
        DBCursor c;
        c = properties.find();
        int i = 1;
        while ( c.hasNext() ) 
		{
            DBObject ei = c.next();
            
            Property p = new Property();
            p.importMongoFields(ei);
            System.out.println("Agregando: " + i);
            list.add(p);
            i++;
        }
        
        return list;
    }  

}
//===========================================================================
//= EOF                                                                     =
//===========================================================================
