import java.util.ArrayList;
import java.net.UnknownHostException;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
//import org.bson.types.ObjectId;

/**
*/
public class DatabaseQuestionTranslator {
    public static void main(String args[]) {
        try {
            System.out.println("=============================================");
            MongoClient mongoClient;
            mongoClient = new MongoClient("www.80mil.com" , 27017);
            DB db = mongoClient.getDB("80mil");
            DBCollection serviceRequestFormQuery = db.getCollection("serviceRequestFormQuery");

            ArrayList<DBObject> questionsToTranslate;            
            questionsToTranslate = createQuestionsList(serviceRequestFormQuery);
            translateQuestionsList(questionsToTranslate, serviceRequestFormQuery);            
            
            fixDetails(serviceRequestFormQuery);
        }
        catch (UnknownHostException ex) {
            System.out.println("ERROR: " + ex.getMessage());
        }
    }    

    private static void translateQuestionsList(ArrayList<DBObject> questionsToTranslate, DBCollection serviceRequestFormQuery) {
        int i;
        for ( i = 0; i < questionsToTranslate.size(); i++ ) {
            translateToSpanish(
                    serviceRequestFormQuery,
                    questionsToTranslate,
                    questionsToTranslate.get(i));
        }
        System.out.println("Al final habían sin repetir: " + questionsToTranslate.size());
    }

    private static ArrayList<DBObject> createQuestionsList(DBCollection serviceRequestFormQuery) {
        ArrayList<DBObject> questionsToTranslate;
        questionsToTranslate = new ArrayList<DBObject>();
        DBCursor c = serviceRequestFormQuery.find();
        int count = 0;
        while ( c.hasNext() ) {
            DBObject o = c.next();
            Object id = o.get("_id");
            Object eng = o.get("descriptionEng");
            Object spa = o.get("descriptionSpa");
            
            if ( spa == null || eng == null ) {
                System.out.println("Invalid question " + id);
                continue;
            }
            String engName = eng.toString();
            String spaName = spa.toString();
            
            if ( engName.startsWith("PREGUNTA") ) {
                continue;
            }
            
            if( !engName.equals(spaName) ) {
                System.out.println("  - " + eng);
                continue;
            }
            
            questionsToTranslate.add(o);
            
            count++;
        }
        System.out.println("Cargadas " + count + " preguntas");
        return questionsToTranslate;
    }
    
    private static void translateToSpanish(
            DBCollection serviceRequestFormQuery,
            ArrayList<DBObject> questionsToTranslate, 
            DBObject o) {
        Object eng = o.get("descriptionEng");
        String engName = eng.toString();

        System.out.println("Traduciendo: " + engName);
        int i;
        String myId = o.get("_id").toString();
        ArrayList<String> idsToTranslate;
        idsToTranslate = new ArrayList<String>();
        idsToTranslate.add(myId);
        for ( i = 0; i < questionsToTranslate.size(); i++ ) {
            String otherId;
            String otherName;
            otherId = questionsToTranslate.get(i).get("_id").toString();
            otherName = questionsToTranslate.get(i).get("descriptionEng").toString();
            if ( !myId.equals(otherId) &&
                  engName.equals(otherName) ) {
                idsToTranslate.add(otherId);
                questionsToTranslate.remove(i);
                i--;
            }
        }

        System.out.println("  - Traduciendo: " + engName);
        String spaName = TranslatorRobot.translateUsingGoogle(
            engName, "en", "es");
        if ( engName.contains("?") ) {
            spaName = "¿" + spaName + "?";
        }
        System.out.println("  - Traducido: " + spaName);

        for ( i = 0; i < idsToTranslate.size(); i++ ) {
            String id = idsToTranslate.get(i);
            System.out.println("  - Se inserta mensaje en español para el ID: " + id);
            DBObject searchQuery;
            searchQuery = new BasicDBObject();
            searchQuery.put("_id", id);
            
            BasicDBObject newDocument;
            newDocument = new BasicDBObject();
            newDocument.append("$set", 
                    new BasicDBObject().append("descriptionSpa", spaName));
            serviceRequestFormQuery.update(searchQuery, newDocument);
        }
    }

    private static void fixDetails(DBCollection serviceRequestFormQuery) {
        DBCursor c = serviceRequestFormQuery.find();
        int n = c.size();
        int count = 1;
        while ( c.hasNext() ) {
            DBObject o = c.next();
            Object spa = o.get("descriptionSpa");

            String spaFixed;
            spaFixed = fixString(spa.toString());
            
            System.out.println("Arreglando(" + count + "/" + n + "):");
            System.out.println("  -> " + spa.toString());
            System.out.println("  <- " + spaFixed);

            Object id = o.get("_id");
            //ObjectId oid = new ObjectId(id.toString());
            DBObject searchQuery;
            searchQuery = new BasicDBObject();            
            searchQuery.put("_id", id);

            BasicDBObject newDocument;
            newDocument = new BasicDBObject();
            newDocument.append("$set",
                new BasicDBObject().append("descriptionSpa", spaFixed));

            serviceRequestFormQuery.update(searchQuery, newDocument);

            count++;
        }
    }

    private static String fixString(String in) {
        char c;
        int i;
        String out = "";
        boolean tu = true;
        
        int numberOfQuestionMarks = 0;
        
        for ( i = 0; i < in.length(); i++ ) {
            c = in.charAt(i);
            if ( tu ) {
                c = Character.toUpperCase(c);
                tu = false;
            }
            out += c;
            //if ( c == '¿'  ) {
            //MODIFICADO DM 09/12
            if ( c == 'A'  ) {
                numberOfQuestionMarks++;
                tu = true;
            }
        }

        if ( numberOfQuestionMarks > 1 ) {
            in = out;
            out = "";
            int noqm = 0;
            tu = true;
            for ( i = 0; i < in.length(); i++ ) {
                c = in.charAt(i);
              //if ( c == '¿'  ) {
                //MODIFICADO DM 09/12
                if ( c == 'A'  ) {                    
                    tu = true;
                }
                //if ( c == '¿' && noqm < numberOfQuestionMarks-1 ) {
              //MODIFICADO DM 09/12
                if ( c == 'A' && noqm < numberOfQuestionMarks-1 ) {	
                    noqm++;
                }
                else {
                    if ( tu ) {
                        out += Character.toUpperCase(c);
                        tu = false;
                    }
                    else {
                        out += c;
                    }
                }
            } 
        }
        
        return out;
    }
}
