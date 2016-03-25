package databaseMongo;

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

import databaseConnection.DatabaseMongoConnection;
import databaseMongo.model.Product;


public class IngenioDatabaseConnection extends DatabaseMongoConnection
{

	public IngenioDatabaseConnection(
            String url, 
            int port,
            String connectionName, 
            String collectionName) 
	{
		super(url, port, connectionName);
	}
	
	private static DBCollection products;

    static 
    {
    	products = null;
    }
    
    public void insertPropertyMongo(Product p)
    {
        if ( products == null ) {
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
                        
        products.insert(newDocument);
    }
    
    public ArrayList<Product> _fetchAllProductsMongo()
    {
        if ( products == null ) 
		{
            return null;
        }
        ArrayList<Product> list;
        list = new ArrayList<Product>();
        
        DBCursor c;
        c = products.find();
        int i = 1;
        while ( c.hasNext() ) 
		{
            DBObject ei = c.next();
            
            Product p = new Product();
            p.importMongoFields(ei);
            System.out.println("Agregando: " + i);
            list.add(p);
            i++;
        }
        
        return list;
    }  

}
