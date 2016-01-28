package webcrawler;

// Java basic classes
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.util.HashMap;

// MongoDB driver classes
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

// VSDK classes
import vsdk.toolkit.io.PersistenceElement;

// Application specific classes
import databaseMongo.ComputrabajoDatabaseConnection;
import databaseMongo.model.NameElement;
import databaseMongo.model.ProfessionHint;
import databaseMongo.model.EmailElement;
import java.util.TreeSet;
import webcrawler.processors.GenderProcessor;
import webcrawler.processors.ProfessionHintProcessor;
import webcrawler.processors.NameProcessor;
import webcrawler.processors.EmailProcessor;
//import webcrawler.processors.RelationshipStatusProcessor;

/**
This tool also updates emailStatus to -10 for emails on invalid domains.
*/
public class Tool04_AnalizerForCleanData {
    private static boolean reportAdvances = false;
    private static final ComputrabajoDatabaseConnection databaseConnection;
    private static FileOutputStream emailMarkFos;

    static 
    {
	emailMarkFos = null;
        databaseConnection = new ComputrabajoDatabaseConnection(
            "localhost" , 27017, "computrabajoCo", 
            "professionalResumeTransformed");
    }

    private static HashMap<String, EmailElement> loadEmailElementCache()
    {
	try {
            HashMap<String, EmailElement> e;
   	    File fd = new File("./etc/emailDomainsCache.bin");
	    if ( fd.exists() ) {
                FileInputStream fis;
                fis = new FileInputStream(fd);
                ObjectInputStream ois;
                ois = new ObjectInputStream(fis);
                e = (HashMap<String, EmailElement>)ois.readObject();
	    }
	    else {
		e = new HashMap<String, EmailElement>();
	    }
            return e;
	}
	catch ( IOException ex ) {
	    System.exit(1);
        }
        catch ( ClassNotFoundException ex ) {
	    System.exit(1);
	}
        return null;
    }

    private static void saveEmailElementCache(
        HashMap<String, EmailElement> emailElements)
    {
	try {
	    File fd = new File("./etc/emailDomainsCache.bin");
	    FileOutputStream fos;
	    fos = new FileOutputStream(fd);
	    ObjectOutputStream oos = new ObjectOutputStream(fos);
	    oos.writeObject(emailElements);
	    fos.close();
	}
	catch ( Exception e ) {
	}
    }

    private static void addInvalidEmailMark(DBObject o)
    {
	try {
	    if ( emailMarkFos == null ) {
		File fd = new File("./output/markInvalidEmails.mongo");
		emailMarkFos = new FileOutputStream(fd);
	    }
	    String l;
	    l = "db.professionalResumeTransformed.update({_id: \"";
	    l += o.get("_id").toString();
	    l += "\"}, {$set: {emailStatus: -10}});";
	    PersistenceElement.writeAsciiLine(emailMarkFos, l);
	}
	catch ( Exception e ) {
	}
    }

    private static void updateEmailStatusForInvalidDomains(
        DBCollection professionalResume,
        HashMap<String, EmailElement> emailElements)
    {
        DBObject filter = new BasicDBObject("emailStatus", 0.0);
        DBCursor c = professionalResume.find(filter);

        int i;
	int invalidCount = 0;
	System.out.println("Marking invalid emails... ");
        for ( i = 0; c.hasNext(); i++ ) {
            DBObject o = c.next();
            
            if ( o.get("emailStatus") == null ) {
                continue;
            }
            if ( o.get("email") == null ) {
                continue;
            }
	    String email = o.get("email").toString();
	    String domain = EmailProcessor.getDomainFromEmail(email);

	    boolean isInvalid = false;
	    if ( !emailElements.containsKey(domain) ) {
		isInvalid = true;
	    }
	    else {
		EmailElement ee;
		ee = emailElements.get(domain);
		if ( !ee.getValid() ) {
		    isInvalid = true;
		}
	    }

	    if ( isInvalid ) {
		addInvalidEmailMark(o);
		invalidCount++;
	    }
	}
	try {
  	    emailMarkFos.flush();
	    emailMarkFos.close();
	}
	catch ( Exception e ) {
	}
	System.out.println("Invalid emails: " + invalidCount + " of " + i);
    }

    public static void main(String args[]) {
        DBCollection professionalResume;
        professionalResume = 
            databaseConnection.getProfessionalResume();
        if ( professionalResume == null ) {
            return;
        }
        
        DBObject filter = new BasicDBObject();
        //BasicDBObject options = new BasicDBObject("profilePictureUrl", true);
        DBCursor c = professionalResume.find(filter /*, options */);

        // Temporary datastructures for analysis and report generation
        HashMap<String, EmailElement> emailElements;
        emailElements = loadEmailElementCache();

        HashMap<String, NameElement> nameElements;
        nameElements = new HashMap<String, NameElement>();
        
        HashMap <String, ProfessionHint> professions;
        professions = new HashMap<String, ProfessionHint>();
        
        TreeSet <String> relations;
        relations = new TreeSet<String>();
        
        // Main loop
        int i;
        for ( i = 0; c.hasNext(); i++ ) {
            DBObject o = c.next();
            
            if ( o.get("_id") == null ) {
                continue;
            }
            
            String id = o.get("_id").toString();
            
            if ( i % 10000 == 0 ) {
                reportAdvances = true;                
            }
            if ( reportAdvances ) {
                System.out.println(
                    "  - (" + (i+1) + " of " + c.count() + "): " + id); 
            }

            boolean considerThis = true;
            /*
            if ( o.containsField("location") ) {
                //processLocation(o, i, c.count(), regions);
                String l = o.get("location").toString();
                if ( l.contains("ogot") ) {
                    considerThis = true;
                }
            }
            */

            if ( considerThis && o.containsField("professionHint") ) {
                ProfessionHintProcessor.processProfessionHint(
                    o, i, c.count(), professions);
            }

            //if ( considerThis && o.containsField("pair") ) {
                // Already done at stage Tool03_TransformationFromRawData2CleanData
                //RelationshipStatusProcessor.processRelationshipStatus(
                //    o, i, c.count(), relations, reportAdvances);
            //}

            if ( o.containsField("name") ) {
                NameProcessor.processName(
		    o, i, c.count(), nameElements, reportAdvances);
            }
            if ( o.containsField("email") ) {
                EmailProcessor.processEmail(
		    o, i, c.count(), emailElements, reportAdvances);
            }
            if ( o.containsField("htmlContent") ) {
                //processHtmlContent(o, i);
            }
            if ( o.containsField("profilePictureUrl") ) {
              //  processProfilePictureUrl(o, id, i);
            }
            //if ( i >= 2000 ) {
            //    break;
            //}
            reportAdvances = false;
        }

        saveEmailElementCache(emailElements);
        NameProcessor.reportNameElements(nameElements);        
        EmailProcessor.reportEmailElements(emailElements);        
        ProfessionHintProcessor.reportResultingProfessionHints(professions);
        //RelationshipStatusProcessor.reportResultingRelationshipStatuses(relations);
        GenderProcessor.calculateGender(professionalResume, nameElements);
        //RelationshipStatusProcessor.calculateRelationshipStatus(professionalResume);
	updateEmailStatusForInvalidDomains(professionalResume, emailElements);
    }
}
