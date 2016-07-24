package databaseConnection;

// MongoDB classes
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mongodb.MongoException;

// VSDK classes
import vsdk.toolkit.common.VSDK;

public class DatabaseMongoConnection 
{
    protected static DB mongoConnection;

    static 
	{
        setMongoConnection(null);
    }

    /**
     * @return the mongoConnection
     */
    public static DB getMongoConnection() {
        return mongoConnection;
    }

    /**
     * @param aMongoConnection the mongoConnection to set
     */
    public static void setMongoConnection(DB aMongoConnection) {
        mongoConnection = aMongoConnection;
    }
    
    public DatabaseMongoConnection(
        String url,
        int port, 
        String connectionName)
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
            setMongoConnection(mongoClient.getDB(connectionName));
        }
        catch ( Exception ex ) {
            VSDK.reportMessageWithException(
                null, 
                VSDK.FATAL_ERROR, 
                "createMongoConnection", 
                "Error connecting", 
                ex);
        }
        return getMongoConnection();
    }
    
    public DBCollection createMongoCollection(String collectionName)
    {
        DBCollection c = null;
    	try {
    	    c = getMongoConnection().getCollection(collectionName);
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
            return getMongoConnection().getName();
    }
	
}
