package webcrawler.processors;

// Basic Java classes
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.TreeSet;

// MongoDB driver classes
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

// VSDK classes
import vsdk.toolkit.io.PersistenceElement;

// Application classes
import databaseMongo.model.NameElement;
import vsdk.toolkit.common.VSDK;

/**
*/
public class GenderProcessor {

    public static void calculateGender(
        DBCollection professionalResume, 
        HashMap<String, NameElement> nameElements) 
    {
        try {
            //----
            TreeSet<String> femaleNames;
            TreeSet<String> maleNames;
            
            femaleNames = loadNamesHints("./etc/femaleNames.txt");
            maleNames = loadNamesHints("./etc/maleNames.txt");
            
            //----
            File fd = new File("./output/updateGender.mongo");
            FileOutputStream fos;
            fos = new FileOutputStream(fd);
            System.out.println("Generating genres...");

            DBObject filter = new BasicDBObject();
            DBCursor c = professionalResume.find(filter);

            int i;
            int unknownCounter = 0;
            int maleCounter = 0;
            int femaleCounter = 0;
            
            for ( i = 0; c.hasNext(); i++ ) {
                DBObject o = c.next();

                if ( o == null || o.get("name") == null || o.get("_id") == null ) {
                    continue;
                }
                String name = o.get("name").toString();
                String id = o.get("_id").toString();
                int hint;
                hint = processNameForGender(
                    id, name, nameElements, maleNames, femaleNames);

                String gender = "?";
                if ( hint == 0 ) {
                    unknownCounter++;
                }
                else if ( hint == 1 ) {
                    gender = "m";
                    maleCounter++;
                }
                else if ( hint == 2 ) {
                    gender = "f";
                    femaleCounter++;
                }
                
                String l = "db.professionalResumeTransformed.update({_id: \"";
                l += id;
                l += "\"}, {$set: {gender: \"" + gender + "\"}});";
                PersistenceElement.writeAsciiLine(fos, l);
            }
            
            System.out.println("Ok, gender generated done.");
            System.out.println("  - Total registers: " + i);
            System.out.println("  - Males: " + maleCounter);
            System.out.println("  - Females: " + femaleCounter);
            System.out.println("  - Unknown: " + unknownCounter);
        }
        catch ( Exception e ) {
            VSDK.reportMessageWithException(
                null, VSDK.FATAL_ERROR, 
                "GenderProcessor.calculateGender", "error on process", e);
        }
    }

    /**
     * @param id
     * @param name
     * @param nameElements
     * @return 0: unknown, 1: male, 2: female
     */
    private static int processNameForGender(
        String id, 
        String name, 
        HashMap<String, NameElement> nameElements,
        TreeSet<String> maleNames,
        TreeSet<String> femaleNames) 
    {
        StringTokenizer parser = new StringTokenizer(name, " ()-.,:;/'[]|\"");
        boolean fhint, mhint;
        boolean fhint0, mhint0;
        
        int i;
        fhint = mhint = false;
        fhint0 = mhint0 = false;
        int nt = parser.countTokens();

        for ( i = 0; parser.hasMoreTokens(); i++ ) {
            String t = parser.nextToken();
            if ( t == null ) {
                break;
            }
            String ni = NameProcessor.normalizeName(t);
            
            if ( ni == null ) {
                continue;
            }
            
            double nnt;
            if ( nt == 1 ) {
                nnt = 0;
            } else {
                nnt = ((double) i) / ((double) (nt - 1));
            }
            if ( nnt < 0 || nnt > 0.5 || nnt == Double.NaN ) {
                break;
            }

            if ( maleNames.contains(ni) ) {
                mhint = true;
                if ( i == 0 ) {
                    mhint0 = true;
                }
            }
            if ( femaleNames.contains(ni) ) {
                fhint = true;
                if ( i == 0 ) {
                    fhint0 = true;
                }
            }
        }

        if ( !mhint && !fhint ) {
            //System.out.println("  * Name (not registered): " + name);
            return NameElement.GENRE_UNKNOWN;
        }
        else if ( mhint && !fhint ) {
            return NameElement.GENRE_MALE;
        }
        else if ( !mhint && fhint ) {
            return NameElement.GENRE_FEMALE;
        }
        else if ( mhint && fhint ) {
            if ( mhint0 ) {
                return NameElement.GENRE_MALE;
            }
            else if ( fhint0 ) {
                return NameElement.GENRE_FEMALE;
            }
            System.out.println("  * Name (ambigous): " + name);
            return NameElement.GENRE_UNKNOWN;
        }
        
        return 0;
    }

    private static TreeSet<String> loadNamesHints(String filename) 
        throws Exception
    {
        File fd = new File(filename);
        FileInputStream fis;

        fis = new FileInputStream(filename);
        TreeSet<String> s;
        s = new TreeSet<String>();
        
        while ( fis.available() > 0 ) {
            String n;
            n = PersistenceElement.readAsciiLine(fis);
            s.add(n);
        }

        return s;
    }
    
}
