
import databaseMysqlMongo.MetroCuadradoDatabaseConnection;
import databaseMysqlMongo.model.Property;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

/**
*/
public class MetroCuadradoDatabaseFixer {
    private static final MetroCuadradoDatabaseConnection databaseConnection;

    static {
        databaseConnection = new MetroCuadradoDatabaseConnection();
        //MetroCuadradoDatabaseConnection.createMongoConnection();
        MetroCuadradoDatabaseConnection.createMongoConnection("localhost" , 27017, "domolyRobot", "landPropertyInSale_test");
    }

    /**
    Deletes repeated elements. 
    */
    private static void searchRepeatedElements() {
        ArrayList<Property> list = databaseConnection._fetchAllPropertiesMongo();
        HashMap<String, Property> map;
        
        map = new HashMap<String, Property>();
        int i;
        System.out.println("Properties found: " + list.size());
        for ( i = 0; i < list.size(); i++ ) {
            String url = list.get(i).getUrl();
            if ( !map.containsKey(url)) {
                map.put(url, list.get(i));
            }
            else {
                System.out.println("ERROR: Repetido: ");
                System.out.println("  - Original: " + map.get(url).get_id());
                System.out.println("  - Ofensivo: " + list.get(i).get_id());
                databaseConnection.removeMongo(list.get(i).get_id());
            }
        }
    }

    /**
    Assigns a given date.
    */
    private static void updateDate()
    {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", 
                Locale.ENGLISH);
        ArrayList<Property> list = databaseConnection._fetchAllPropertiesMongo();
        HashMap<String, Property> map;
        
        map = new HashMap<String, Property>();
        int i;
        System.out.println("Properties found: " + list.size());
        for ( i = 0; i < list.size(); i++ ) {
            databaseConnection.updateDateMongo(list.get(i).get_id(), format);
        }
    }
    
    public static void main(String args[])
    {
        //searchRepeatedElements();
        updateDate();        
    }
}
