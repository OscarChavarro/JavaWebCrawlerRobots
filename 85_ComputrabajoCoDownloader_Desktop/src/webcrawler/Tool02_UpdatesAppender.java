package webcrawler;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import databaseMongo.ComputrabajoMongoDatabaseConnection;
import vsdk.toolkit.common.VSDK;

/**
Takes two databases and sync them. All newly downloaded resumes from source
database are added to a database copy. New resumes are identified by the
transformStatus flag, which is 0.0 if resume is new, and 1.0 if not.

The transformStatus flag is changed to 1.0 in source database, and destination
database is left as a complete copy of source.
*/
public class Tool02_UpdatesAppender {
    public static void main(String args[])
    {
        DBCollection s = null;
        DBCollection t = null;

        try {
            DB sourceDb;
            MongoClient sourceServer;
            sourceServer = new MongoClient("localhost", 27018);
            sourceDb = sourceServer.getDB("computrabajoCo");
            
            DB targetDb;
            MongoClient targetServer;
            targetServer = new MongoClient("localhost", 27017);
            targetDb = targetServer.getDB("computrabajoCo");
            s = sourceDb.getCollection("professionalResume");
            t = targetDb.getCollection("professionalResume");
        }
        catch ( Exception ex ) {
            VSDK.reportMessageWithException(
                null, 
                VSDK.FATAL_ERROR, 
                "createMongoConnection", 
                "Error connecting", 
                ex);
        }
        
        if ( s == null || t == null ) {
            return;
        }

        DBCursor c;
        BasicDBObject regex = new BasicDBObject("$lte", 0.9);
        BasicDBObject filter = new BasicDBObject("transformStatus", regex);
        c = s.find(filter);
        int i;
        
        //System.out.println("Updating: " + c.size());
        for ( i = 0; c.hasNext(); i++ ) {
            DBObject o;
            o = c.next();

            //System.out.println("  . " + o.get("transformStatus"));
            if ( i % 100 == 0 ) {
                System.out.println("  - " + i);
            }
            
            t.insert(o);
                    
            s.update(o, 
                new BasicDBObject("$set", 
                    new BasicDBObject("transformStatus", 1)));
        }
        System.out.println("Total: " + i);
    }
}
