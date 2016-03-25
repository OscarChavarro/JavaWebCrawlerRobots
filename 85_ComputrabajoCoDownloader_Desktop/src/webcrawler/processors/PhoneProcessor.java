package webcrawler.processors;

import com.mongodb.DBObject;
import databaseMongo.model.NameElement;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

class CCounter
{
    int n;
    
    public CCounter()
    {
        n = 1;
    }
}

/**
*/
public class PhoneProcessor {

    private static final HashMap<Character, CCounter> characters;
    
    static {
        characters = new HashMap<Character, CCounter>();
    }
    
    public static void processPhone(
        DBObject o, 
        int index, 
        int count, 
        HashMap<String, NameElement> nameElements, 
        boolean reportAdvances) 
    {
        String p = o.get("phone").toString();
        
        if ( reportAdvances ) {
            System.out.println("    . Phone: " + p);
        }
        
        int i;
        char c;
        for ( i = 0; i < p.length(); i++ ) {
            c = p.charAt(i);
            
            if ( Character.isAlphabetic(c) ) {
                System.out.println("  * RARE PHONE: " + p);
                break;
            }
            
            //if ( characters.containsKey(c) ) {
            //    characters.get(c).n++;
            //}
            //else {
            //    characters.put(c, new CCounter());
            //}
        }
    }

    public static void reportPhoneElements() {
        System.out.println("PHONE CHARACTERS");
        Set<Entry<Character, CCounter>> s = characters.entrySet();
        
        for ( Entry<Character, CCounter> e : s ) {
            System.out.println(
                "  - Char [" + e.getKey() + "]: " + e.getValue().n);
        }
    }
    
}
