package databaseConnection;

import java.util.ArrayList;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TreeSet;

// MongoDB classes
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoException;
import org.bson.types.ObjectId;

// VSDK classes
import vsdk.toolkit.common.VSDK;

// Application specific classes
import databaseMongo.model.JdbcEntity;

public class DatabaseMongoConnection 
{
	private static DB mongoConnection;
    private static DBCollection properties;


    static 
	{
        mongoConnection = null;
        properties = null;
    }
    
    public DatabaseMongoConnection(String url,int port, String connectionName, String collectionName)
    {
        try 
		{
            MongoClient mongoClient;
            mongoClient = new MongoClient(url, port);
            mongoConnection = mongoClient.getDB(connectionName);
            properties = mongoConnection.getCollection(collectionName);            
        }
        catch ( UnknownHostException ex ) {
            VSDK.reportMessageWithException(
                null, 
                VSDK.FATAL_ERROR, 
                "createMongoConnection", 
                "Error connecting", 
                ex);
        }
    }

    public static DB createMongoConnection(String url,int port, String connectionName, String collectionName)
    {
        try 
		{
            MongoClient mongoClient;
            mongoClient = new MongoClient(url, port);
            mongoConnection = mongoClient.getDB(connectionName);
            properties = mongoConnection.getCollection(collectionName);            
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
    
    public DBCollection createMongoCollection(String collectionName)
    {
    	try
    	{
    		properties = mongoConnection.getCollection(collectionName);
    	}
    	catch ( MongoException ex)
    	{
    		VSDK.reportMessageWithException(
    				null, 
    				VSDK.FATAL_ERROR, 
    				"createMongoCollection", 
    				"Error connecting", 
    				ex);
    	}
        return properties;
    }
    
    public ArrayList<Object> fetchAllPropertiesMongo()
    {
        if ( properties == null ) 
		{
            return null;
        }
        ArrayList<Object> list;
        list = new ArrayList<Object>();
        
        DBCursor c;
        c = properties.find();
        int i = 1;
        while ( c.hasNext() ) 
		{
            DBObject ei = c.next();
            
            /*Property p = new Property();
            p.importMongoFields(ei);*/
            System.out.println("Agregando: " + i);
            list.add(ei);
            i++;
        }
        
        return list;
    }  
    
    public void insertObject(JdbcEntity p)
    {
        if ( properties == null ) 
		{
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
		
	public boolean existInMongoDatabase(String key,String value) 
	{
        if ( properties == null ) 
		{
            System.err.println("ERROR: No esta conectado a Mongo");
            System.exit(1);
        }
        
        BasicDBObject filter;
        //filter = new BasicDBObject("url", url);
        filter = new BasicDBObject(key, value);
        DBObject o = properties.findOne(filter);
        
		if ( o == null ) 
		{
            return false;
        }
        
        //System.out.println("Encontré un predio para la url " + url);
        System.out.println("  - El Objeto ya estaba en la base de datos, con id " + o.get("_id"));
    
        return true;
    }

    public void removeMongo(String id)
	{
        if ( properties == null ) 
		{
            return;
        }
        
        ObjectId oid = new ObjectId(id);
        BasicDBObject filter;
        
        filter = new BasicDBObject("_id", oid);
        properties.remove(filter);
    }

    public void updateDateMongo(String id, DateFormat format) 
	{
        if ( properties == null ) 
		{
            return;
        }
        
        try 
		{
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
        catch ( Exception e ) 
		{
            
        }
    }
	
    public static DBCollection getProperties() 
	{
        return properties;
    }
		
	public static void checkExistingResumesOnDatabase(TreeSet<String> resumeListAlreadyDownloaded,String key) 
    {
        System.out.println("5. Importing all Objects loaded in database... ");
        BasicDBObject query;
        BasicDBObject options;
        
        query = new BasicDBObject();
        options = new BasicDBObject();
        //options.append("sourceUrl", true);
        options.append(key, true);
        DBCursor c = properties.find(query, options);

        System.out.println("  - 5.1. Importing database entries...");
        int i;

        for ( i = 0; c.hasNext(); i++ ) 
		{
            if ( i % 10000 == 0 ) 
			{
                System.out.println("     . " + i);
            }
            //Object o = c.next().get("sourceUrl");
            Object o = c.next().get(key);

            if ( o == null ) 
			{
                continue;
            }
            
            String url = o.toString();
            // Trim "http://empresa.computrabajo.com.ve" out
            url = url.substring(34);            
            if ( url != null && !url.equals("null") ) 
			{
                resumeListAlreadyDownloaded.add(url);
            }
        }

        System.out.println("  - 5.2. Number of resumes already imported in database: " + i);
    }
	
	public String getNameMongoConnection()
	{
		return mongoConnection.getName();
	}
	
}
