import java.net.UnknownHostException;
import java.util.ArrayList;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import org.bson.types.ObjectId;

/**
*/
public class DatabaseAnswerOptionTranslator {
    public static void main(String args[])
    {
        try {
            System.out.println("=============================================");
            MongoClient mongoClient;

            mongoClient = new MongoClient("www.80mil.com" , 27017);
            DB db = mongoClient.getDB("80mil");
            DBCollection serviceRequestAnswerOption = db.getCollection("serviceRequestAnswerOption");

            //resetTable(serviceRequestAnswerOption);
            
            //ArrayList<DBObject> optionsToTranslate;
            //optionsToTranslate = createOptionsList(serviceRequestAnswerOption);
            //translateOptionsList(optionsToTranslate, serviceRequestAnswerOption);

            fixDetails(serviceRequestAnswerOption);

            //System.out.println("Al final habían sin repetir: " + optionsToTranslate.size());            
        }
        catch (UnknownHostException ex) {
            System.out.println("ERROR: " + ex.getMessage());
        }
    }

    private static void translateOptionsList(ArrayList<DBObject> optionsToTranslate, DBCollection serviceRequestAnswerOption) {
        int i;
        for ( i = 0; i < optionsToTranslate.size(); i++ ) {
            translateToSpanish(
                    serviceRequestAnswerOption,
                    optionsToTranslate,
                    optionsToTranslate.get(i));
        }
    }

    private static ArrayList<DBObject> createOptionsList(DBCollection serviceRequestAnswerOption) {
        ArrayList<DBObject> optionsToTranslate;
        optionsToTranslate = new ArrayList<DBObject>();
        DBCursor c = serviceRequestAnswerOption.find();
        int count = 0;
        while ( c.hasNext() ) {
            DBObject o = c.next();
            Object id = o.get("_id");
            Object eng = o.get("nameEng");
            Object spa = o.get("nameSpa");
            
            if ( spa == null || eng == null ) {
                System.out.println("Invalid option " + id);
                continue;
            }
            String engName = eng.toString();
            String spaName = spa.toString();
            
            if( !engName.equals(spaName) ) {
                System.out.println("  - " + eng);
                continue;
            }
            optionsToTranslate.add(o);
            count++;
        }
        System.out.println("Cargadas " + count + " opciones de respuesta");
        return optionsToTranslate;
    }
    
    private static void translateToSpanish(
        DBCollection serviceRequestAnswerOption, 
        ArrayList<DBObject> optionsToTranslate, 
        DBObject o) 
    {
        Object eng = o.get("nameEng");
        String engName = eng.toString();

        System.out.println("Traduciendo: " + engName);
        
        int i;
        String myId = o.get("_id").toString();
        ArrayList<String> idsToTranslate;
        idsToTranslate = new ArrayList<String>();
        idsToTranslate.add(myId);
        for ( i = 0; i < optionsToTranslate.size(); i++ ) {
            String otherId;
            String otherName;
            otherId = optionsToTranslate.get(i).get("_id").toString();
            otherName = optionsToTranslate.get(i).get("nameEng").toString();
            if ( !myId.equals(otherId) &&
                  engName.equals(otherName) ) {
                idsToTranslate.add(otherId);
                optionsToTranslate.remove(i);
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
            ObjectId oid = new ObjectId(id);
            
            searchQuery.put("_id", oid);

            BasicDBObject newDocument;
            newDocument = new BasicDBObject();
            newDocument.append("$set", 
                    new BasicDBObject().append("nameSpa", spaName));
            serviceRequestAnswerOption.update(searchQuery, newDocument);
        }
    }

    private static void resetTable(
        DBCollection serviceRequestAnswerOption) 
    {
        DBCursor c = serviceRequestAnswerOption.find();
        int i;
        for ( i = 0; c.hasNext(); i++ ) {
            DBObject o = c.next();
            Object id = o.get("_id");
            Object eng = o.get("nameEng");
            
            System.out.println("Reseteando registro " + i + ": " + eng.toString());
            
            DBObject searchQuery;
            searchQuery = new BasicDBObject();

            ObjectId oid = new ObjectId(id.toString());
            
            searchQuery.put("_id", oid);

            BasicDBObject newDocument;
            newDocument = new BasicDBObject();
            newDocument.append("$set", 
                new BasicDBObject().append("nameSpa", eng.toString()));
            serviceRequestAnswerOption.update(searchQuery, newDocument);
        }
    }

    private static void fixDetails(DBCollection serviceRequestAnswerOption) {
        DBCursor c = serviceRequestAnswerOption.find();
        
        int n = c.size();
        int count = 1;
        
        while ( c.hasNext() ) {
            DBObject o = c.next();
            Object spa = o.get("nameSpa");

            String spaFixed;
            
            spaFixed = fixString(spa.toString());
                                    
            if ( !spa.toString().equals(spaFixed) ) {
                System.out.println("Arreglando(" + count + "/" + n + "):");
                System.out.println("  -> " + spa.toString());
                System.out.println("  <- " + spaFixed);

                Object id = o.get("_id");
                ObjectId oid = new ObjectId(id.toString());
                DBObject searchQuery;

                searchQuery = new BasicDBObject();            
                searchQuery.put("_id", oid);
                BasicDBObject newDocument;
                newDocument = new BasicDBObject();
                newDocument.append("$set", 
                    new BasicDBObject().append("nameSpa", spaFixed));

                serviceRequestAnswerOption.update(searchQuery, newDocument);
            }
            count++;
        }
    }

    private static String fixString(String in) {
        if ( in.equals("selecto") ) {
            return "Seleccione una opción...";
        }
        
        char c;
        int i;
        String out = "";
        for ( i = 0; i < in.length(); i++ ) {
            c = in.charAt(i);
            if ( i == 0 ) {
                c = Character.toUpperCase(c);
            }
            out += c;
        }
        
        return out;
    }
}
