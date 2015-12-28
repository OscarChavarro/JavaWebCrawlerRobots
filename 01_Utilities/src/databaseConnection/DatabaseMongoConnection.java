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
import databaseMongo.model.Property;
import databaseMongo.model.Resume;
import databaseMongo.model.ContactData;
import databaseMongo.model.CamaraDeComercioData;

public class DatabaseMongoConnection 
{
	private static DB mongoConnection;
    private static DBCollection properties;
	private static DBCollection properties2;

    static 
	{
        mongoConnection = null;
        properties = null;
		properties2 = null;
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
    
	public static DB createMongoConnection(String url,int port, String connectionName, String collectionName, String collectionName2)
    {
        try 
		{
            MongoClient mongoClient;
            mongoClient = new MongoClient(url, port);
            mongoConnection = mongoClient.getDB(connectionName);
            properties = 
                mongoConnection.getCollection(collectionName);
            properties2 = 
                mongoConnection.getCollection(collectionName2);
        }
        catch ( UnknownHostException ex ) 
		{
            VSDK.reportMessageWithException(
                null, 
                VSDK.FATAL_ERROR, 
                "createMongoConnection", 
                "Error connecting", 
                ex);
        }
        return mongoConnection;
    }
	
    public ArrayList<Property> fetchAllPropertiesMongo()
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
    
    public void insertPropertyMongo(Property p)
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
	
	public static void insertResumeMongo(Resume r)
    {
        if ( getProfessionalResume() == null ) 
		{
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

        try 
		{
            if ( r.getName() == null || r.getName().equals("null") ) 
			{
                System.out.println("    . Saltando hoja de vida vacia");
            }
            else 
			{
                getProfessionalResume().insert(newDocument);
            }
        }
        catch ( MongoException e ) 
		{
            System.out.println("    . Saltando hoja de vida - ya existía "+
                "(debería actualizarse?)");
            System.out.println("    . " + r.getSourceUrl());

        }
    }
	
	public static void insertContactMongo(ContactData r)
    {
        if ( properties == null ) 
		{
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

        try 
		{
            if ( r.getEmail() == null || r.getEmail().equals("null") ) 
			{
                System.out.println("    . Saltando contacto vacio");
            }
            else 
			{
                properties.insert(newDocument);
            }
        }
        catch ( MongoException e ) 
		{
            
        }
    }
	
	public static void insertCompanyContactMongo(CamaraDeComercioData r)
    {
        if ( properties2 == null ) 
		{
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

        try 
		{
			properties2.insert(newDocument);
        }
        catch ( MongoException e ) 
		{

        }
    }
	
	public boolean existInMongoDatabase(String url) 
	{
        if ( properties == null ) 
		{
            System.err.println("ERROR: No esta conectado a Mongo");
            System.exit(1);
        }
        
        BasicDBObject filter;
        filter = new BasicDBObject("url", url);
        DBObject o = properties.findOne(filter);
        
		if ( o == null ) 
		{
            return false;
        }
        
        //System.out.println("Encontré un predio para la url " + url);
        System.out.println("  - El predio ya estaba en la base de datos, con id " + o.get("_id"));
    
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
	
	public static DBCollection getProfessionalResume() 
	{
        return properties;
    }
	
	public static void checkExistingResumesOnDatabase(TreeSet<String> resumeListAlreadyDownloaded) 
    {
        System.out.println("5. Importing all URLs loaded in database... ");
        BasicDBObject query;
        BasicDBObject options;
        
        query = new BasicDBObject();
        options = new BasicDBObject();
        options.append("sourceUrl", true);
        DBCursor c = properties.find(query, options);

        System.out.println("  - 5.1. Importing database entries...");
        int i;

        for ( i = 0; c.hasNext(); i++ ) 
		{
            if ( i % 10000 == 0 ) 
			{
                System.out.println("     . " + i);
            }
            Object o = c.next().get("sourceUrl");

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
	
}
