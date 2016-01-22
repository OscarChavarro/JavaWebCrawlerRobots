package webcrawler;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import databaseMongo.ComputrabajoDatabaseConnection;
import databaseMongo.model.NameElement;
import databaseMongo.model.ProfessionHint;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
*/
public class Tool04_AnalizerForCleanData {
    private static boolean reportAdvances = false;
    private static final ComputrabajoDatabaseConnection databaseConnection;

    static 
    {
        databaseConnection = new ComputrabajoDatabaseConnection(
            "localhost" , 27017, "computrabajoCo", "professionalResumeTransformed");
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
        HashMap<String, NameElement> nameElements;
        nameElements = new HashMap<String, NameElement>();
        
        HashMap <String, ProfessionHint> professions;
        professions = new HashMap<String, ProfessionHint>();
        
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
                //processProfessionHint(o, i, c.count(), professions);
            }

            if ( o.containsField("name") ) {
                NameProcessor.processName(o, i, c.count(), nameElements, reportAdvances);
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
        NameProcessor.reportNameElements(nameElements);        
        reportResultingProfessionHints(professions);
    }    

    private static void processProfessionHint(
        DBObject o, int i, int count, HashMap<String, ProfessionHint> professions) {
        String p = o.get("professionHint").toString();
        
        if ( !professions.containsKey(p) ) {
            ProfessionHint pv;
            pv = new ProfessionHint();
            pv.setApareancesCount(1);
            pv.setContent(p);
            professions.put(p, pv);
        }
        else {
            professions.get(p).incrementCount();
        }
    }

    private static void reportResultingProfessionHints(
        HashMap<String, ProfessionHint> professionHints) 
    {
        System.out.println("Cantidad de profesiones encontradas: " + 
            professionHints.size());
        ArrayList<ProfessionHint> orderedSet;
        orderedSet = new ArrayList<>();
        int rareProfessions = 0;
        int threshold = 10;
        for ( String si : professionHints.keySet() ) {
            ProfessionHint ph = professionHints.get(si);
            if ( ph.getApareancesCount() >= threshold ) {
                orderedSet.add(ph);
            }
            else {
                rareProfessions++;
            }
        }
        
        Collections.sort(orderedSet);
        int i;
        System.out.println("  - Profesiones extrañas, con menos de " + threshold + " personas en cada una (no mostradas): " + rareProfessions);
        int n = 0;
        for ( i = 0; i < orderedSet.size(); i++ ) {
            n += orderedSet.get(i).getApareancesCount();
        }
        System.out.println("  - Profesiones comunes, con " + threshold + " o más personas en cada una (mostradas a continuación): " + n);
        for ( i = 0; i < orderedSet.size(); i++ ) {
            System.out.println("  - " + orderedSet.get(i));
        }
    }

}
