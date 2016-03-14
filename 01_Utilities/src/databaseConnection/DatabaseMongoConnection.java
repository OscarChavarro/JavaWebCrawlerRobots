package databaseConnection;

import java.net.UnknownHostException;

// MongoDB classes
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mongodb.MongoException;

// VSDK classes
import vsdk.toolkit.common.VSDK;

public class DatabaseMongoConnection 
{
    private static DB mongoConnection;

    static 
	{
        mongoConnection = null;
    }
    
    public DatabaseMongoConnection(String url,int port, String connectionName, String collectionName)
    {
        try 
	{
            MongoClient mongoClient;
            mongoClient = new MongoClient(url, port);
            mongoConnection = mongoClient.getDB(connectionName);
        }
        catch ( Exception ex ) {
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
        }
        catch ( Exception ex ) {
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
        DBCollection c = null;
    	try {
    	    c = mongoConnection.getCollection(collectionName);
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
        return c;
    }
	
    public String getNameMongoConnection()
    {
            return mongoConnection.getName();
    }
	
}
