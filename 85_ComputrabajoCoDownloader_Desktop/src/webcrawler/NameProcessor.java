package webcrawler;

import com.mongodb.DBObject;
import databaseMongo.model.NameElement;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.StringTokenizer;
import vsdk.toolkit.common.VSDK;
import vsdk.toolkit.io.PersistenceElement;

/**
*/
public class NameProcessor {

    public static int minNumberOfElements;
    public static int maxNumberOfElements;
    
    static {
        minNumberOfElements = Integer.MAX_VALUE;
        maxNumberOfElements = Integer.MIN_VALUE;
    }
    
    public static void reportNameElements(HashMap<String, NameElement> nameElements) {
        Collection<NameElement> s = nameElements.values();
        System.out.println("- ENCOUNTERED NAME ELEMENTS: " + nameElements.size() + "-");
        ArrayList<NameElement> sorted;
        sorted = new ArrayList<NameElement>();
        for (NameElement ne : s) {
            sorted.add(ne);
        }
        Collections.sort(sorted);
        int i;
        try {
            File fd = new File("./output/reports/nameElements.csv");
            FileOutputStream fos;
            fos = new FileOutputStream(fd);
            BufferedOutputStream bos;
            bos = new BufferedOutputStream(fos);
            for (i = 0; i < sorted.size(); i++) {
                String l;
                NameElement n = sorted.get(i);
                l = n.getName() + "\t" + n.getApareancesCount() + "\t" + VSDK.formatDouble(n.getPositionAverage(), 5);
                PersistenceElement.writeAsciiLine(bos, l);
            }
            bos.close();
            fos.close();
        } 
        catch (Exception ex) {
        }
        
        System.out.println("Shorter name in elements: " + minNumberOfElements);
        System.out.println("Longest name in elements: " + maxNumberOfElements);
    }

    private static String normalizeName(String input) {
        String ni = input.toLowerCase();

        if ( input.contains("Lemes") ) {
            System.out.println("*******TUNTUN");
        }
        
        ni = ni.replace(".", " ");
        ni = ni.replace(",", " ");
        ni = ni.replace("-", " ");
        ni = ni.replace("/", " ");
        ni = ni.replace("\\", " ");
        ni = ni.replace("\"", " ");
        ni = ni.replace("\'", " ");
        ni = ni.replace("*", " ");
        ni = ni.replace("+", " ");
        ni = ni.replace("(", " ");
        ni = ni.replace(")", " ");
        ni = ni.replace("[", " ");
        ni = ni.replace("]", " ");
        ni = ni.replace("{", " ");
        ni = ni.replace("}", " ");
        ni = ni.replace("á", "a");
        ni = ni.replace("é", "e");
        ni = ni.replace("í", "i");
        ni = ni.replace("ó", "o");
        ni = ni.replace("ú", "u");
        ni = ni.replace("Á", "a");
        ni = ni.replace("É", "e");
        ni = ni.replace("Í", "i");
        ni = ni.replace("Ó", "o");
        ni = ni.replace("Ú", "u");
        ni = ni.replace("à", "a");
        ni = ni.replace("è", "e");
        ni = ni.replace("ì", "i");
        ni = ni.replace("ò", "o");
        ni = ni.replace("ù", "u");
        ni = ni.replace("À", "a");
        ni = ni.replace("È", "e");
        ni = ni.replace("Ì", "i");
        ni = ni.replace("Ò", "o");
        ni = ni.replace("Ù", "u");
        ni = ni.trim();
        
        if ( ni.length() < 1 ) {
            return null;
        }

        if ( ni.equals("de") ) {
            return null;
        }

        if ( ni.equals("la") ) {
            return null;
        }

        // Some people includes a URL in their name
        if ( ni.contains("http://") ) {
            return null;
        }
        
        // Some people includes contact email in their name
        if ( ni.contains("@") ) {
            return null;
        }
        
        // Some people includes their contact phone number in their name
        if ( allDigits(ni) ) {
            return null;
        }
        
        //----
        String nn = "";
        String ln;
        ln = ni.toLowerCase();
        int i;
        for ( i = 0; i < ln.length(); i++ ) {
            if (i == 0) {
                nn += ("" + ln.charAt(i)).toUpperCase();
            } else {
                nn += ln.charAt(i);
            }
        }
        return nn;
    }

    public static void processName(DBObject o, int index, int n, HashMap<String, NameElement> elements, boolean reportAdvances) {
        String name = o.get("name").toString();
        String id = o.get("_id").toString();
        
        if ( name.equals("null") ) {
            return;
        }
        if ( reportAdvances ) {
            System.out.println("    . Names: [" + name + "]");
        }
        StringTokenizer parser = new StringTokenizer(name, " ()-.,:;/'[]|\"");
        int nt = parser.countTokens();
        int i;
        for ( i = 0; parser.hasMoreTokens(); i++ ) {
            String ni = normalizeName(parser.nextToken());
            
            if ( ni != null ) {
                NameElement ne;
                if ( elements.containsKey(ni) ) {
                    ne = elements.get(ni);
                } 
                else {
                    ne = new NameElement();
                    ne.setName(ni);
                    elements.put(ni, ne);
                }
                double nnt;
                if (nt == 1) {
                    nnt = 0;
                } else {
                    nnt = ((double) i) / ((double) (nt - 1));
                }
                if (nnt < 0 || nnt > 1.0 || nnt == Double.NaN) {
                    System.out.println("Parando! ERROR ALGORITMO");
                    System.exit(1);
                }
                ne.addElement(nnt);
            }
        }
        
        if ( minNumberOfElements > i ) {
            minNumberOfElements = i;
        }
        if ( maxNumberOfElements < i ) {
            maxNumberOfElements = i;
        }

        if ( i < 2 /*|| i > 6*/ ) {
            System.out.println("**** " + name);
        }
    }   

    private static boolean allDigits(String s) {
        int i;
        for ( i = 0; i < s.length(); i++ ) {
            if ( !Character.isDigit(s.charAt(i)) ) {
                return false;
            }
        }
        return true;
    }
}
