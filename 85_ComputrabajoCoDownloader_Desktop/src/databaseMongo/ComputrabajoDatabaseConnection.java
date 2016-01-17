//===========================================================================
package databaseMongo;

// Java basic classes
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TreeSet;

// Mongo classes
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import org.bson.types.ObjectId;

// Application specific classes
import databaseMongo.model.Resume;
import databaseMongo.model.ResumeTrans;
import databaseConnection.DatabaseMongoConnection;;


/**
*/
public class ComputrabajoDatabaseConnection extends  DatabaseMongoConnection{

    private static DBCollection professionalResume;

    static 
    {
        professionalResume = null;
    }
    
    public ComputrabajoDatabaseConnection(String url, int port, String connectionName, String collectionName) 
    {
		super(url, port, connectionName, collectionName);
		professionalResume = super.getProperties();
	}


    public void insertResumeMongo(Resume r)
    {
        if ( this.getProfessionalResume() == null ) {
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
            System.out.println("    . Saltando hoja de vida - ya existe "+
                "(deberá actualizarse?)");
            System.out.println("    . " + r.getSourceUrl());
        }
    }
    
    public void insertResumeMongo(DBCollection collection, ResumeTrans r)
    {
        if ( collection == null ) {
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
                collection.insert(newDocument);
            }
        }
        catch ( MongoException e ) {
            System.out.println("    . Saltando hoja de vida - ya existe "+
                "(deberá actualizarse?)");
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
    public DBCollection getProfessionalResume()
    {
        return professionalResume;
    }

    public void checkExistingResumesOnDatabase(
        TreeSet<String> resumeListAlreadyDownloaded) 
    {
        super.checkExistingResumesOnDatabase(resumeListAlreadyDownloaded, "sourceUrl");	
    }
    
    public ArrayList<Resume> _fetchAllProductsMongo()
    {
        if ( professionalResume == null ) 
		{
            return null;
        }
        ArrayList<Resume> list;
        list = new ArrayList<Resume>();
        
        DBCursor c;
        c = professionalResume.find();
        int i = 1;
        Resume r = new Resume();
        DBObject ei;
        //while ( c.hasNext() )
        while ( i<100 )
		{
            ei = c.next();
            r = new Resume();
            r.importMongoFields(ei);
            list.add(r);
            i++;
        }
        
        return list;
    }  
}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
