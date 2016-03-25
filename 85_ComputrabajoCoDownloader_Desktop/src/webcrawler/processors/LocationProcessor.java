//===========================================================================
package webcrawler.processors;

// Java basic classes
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.StringTokenizer;

// MongoDB classes
import com.mongodb.DBObject;

// Application specific classes
import databaseMongo.model.GeographicAdministrativeRegion;
import webcrawler.ComputrabajoTaggedHtml;

/**
*/
public class LocationProcessor {

    /**
    Given a database object with a location label, this method performs a
    process of data normalization. Process follows these steps:
     - Each valid location label should have to parts separated
       by a single slash character.
     - Each half will have its leading and trailing spaces trimmed.
    @param o
    @param index
    @param n
    @param regions can be null
    @param reportAdvances
    @return normalized location string
     */
    public static String processLocation(
        DBObject o, 
        int index, 
        int n, 
        HashMap<String, GeographicAdministrativeRegion> regions, 
        boolean reportAdvances) 
    {
        String id = o.get("_id").toString();
        String l = o.get("location").toString();
        if ( l.equals("null") ) {
            if ( reportAdvances ) {
                System.out.println("    . Bad location, skipping");
            }
            return "null";
        }
        
        //- 1. Check each half ------------------------------------------------
        int i;
        char c;
        int numberOfSeparators = 0;
        for ( i = 0; i < l.length(); i++ ) {
            c = l.charAt(i);
            if (c == '/') {
                numberOfSeparators++;
            }
        }
        String area = null;
        if ( numberOfSeparators == 0 ) {
            area = l;
            if ( regions != null && !regions.containsKey(area) ) {
                GeographicAdministrativeRegion areaRegion;
                areaRegion = new GeographicAdministrativeRegion();
                areaRegion.setNameSpa(area);
                regions.put(area, areaRegion);
            }
            return area;
        } 
        else if ( numberOfSeparators != 1 ) {
            System.out.println("ERROR: Location " + l + " is invalid:");
            System.out.println("  - Wrong number of separators");
            System.out.println("  - ID: " + id);
            return "null";
        }
        String subarea = null;
        StringTokenizer parser = new StringTokenizer(l, "/");
        try {
            area = parser.nextToken();
            subarea = parser.nextToken();
            area = ComputrabajoTaggedHtml.trimSpaces(area);
            subarea = ComputrabajoTaggedHtml.trimSpaces(subarea);
            if ( reportAdvances ) {
                System.out.println(
                    "    . Location: [" + area + "] / [" + subarea + "]");
            }
        } 
        catch (Exception e) {
            System.out.println("ERROR: Can not break string [" + l + "]");
            System.out.println("  - ID: " + id);
        }
        GeographicAdministrativeRegion areaRegion;
        if ( regions != null && regions.containsKey(area) ) {
            areaRegion = regions.get(area);
        } 
        else {
            areaRegion = new GeographicAdministrativeRegion();
            areaRegion.setNameSpa(area);
            if ( regions != null ) {
                regions.put(area, areaRegion);
            }
        }
        areaRegion.insertSubarea(subarea);
        return area + " / " + subarea;
    }

    public static void reportResultingAreas(
        HashMap<String, GeographicAdministrativeRegion> regions) 
    {
        Collection<GeographicAdministrativeRegion> s = regions.values();
        
        System.out.println("- ENCOUNTERED REGIONS: " + regions.size() + " -");
        ArrayList<GeographicAdministrativeRegion> sorted;
        sorted = new ArrayList<GeographicAdministrativeRegion>();
        for ( GeographicAdministrativeRegion g : s ) {
            sorted.add(g);
        }
        Collections.sort(sorted);
        int i;
        for ( i = 0; i < sorted.size(); i++ ) {
            System.out.print(sorted.get(i));
        }
    }
    
}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
