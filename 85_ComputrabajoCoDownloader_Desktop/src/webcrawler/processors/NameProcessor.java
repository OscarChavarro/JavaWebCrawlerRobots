package webcrawler.processors;

// Java basic classes
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.StringTokenizer;

// Mongodb driver classes
import com.mongodb.DBObject;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;

// VSDK classes
import vsdk.toolkit.common.VSDK;
import vsdk.toolkit.io.PersistenceElement;

// Application specific classes
import databaseMongo.model.NameElement;
import java.io.FileInputStream;
import java.util.TreeSet;

/**
*/
public class NameProcessor {

    public static int minNumberOfElements;
    public static int maxNumberOfElements;
    private static TreeSet<String> lastNamesFromKnownList;
    
    static {
        minNumberOfElements = Integer.MAX_VALUE;
        maxNumberOfElements = Integer.MIN_VALUE;
        lastNamesFromKnownList = null;
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
            File fdn = new File("./output/reports/nameHints.csv");
            File fdln = new File("./output/reports/lastnameHints.csv");
            File fdmale = new File("./output/reports/nameMaleHints.csv");
            File fdfemale = new File("./output/reports/nameFemaleHints.csv");
            File fdneutral = new File("./output/reports/nameNeutralHints.csv");

            FileOutputStream fos;
            FileOutputStream fosn;
            FileOutputStream fosln;
            FileOutputStream fosmale;
            FileOutputStream fosfemale;
            FileOutputStream fosneutral;
            fos = new FileOutputStream(fd);
            fosn = new FileOutputStream(fdn);
            fosln = new FileOutputStream(fdln);
            fosmale = new FileOutputStream(fdmale);
            fosfemale = new FileOutputStream(fdfemale);
            fosneutral = new FileOutputStream(fdneutral);
            
            for ( i = 0; i < sorted.size(); i++ ) {
                String l;
                NameElement n = sorted.get(i);
                double f = n.getPositionAverage();
                l = n.getName() + "\t" + n.getApareancesCount() + "\t" + VSDK.formatDouble(f, 5);
                PersistenceElement.writeAsciiLine(fos, l);
                if ( f < 0.45 ) {
                    PersistenceElement.writeAsciiLine(fosn, l);
                    int h = genreHint(n.getName());
                    switch ( h ) {
                      case NameElement.GENRE_MALE:
                        PersistenceElement.writeAsciiLine(fosmale, n.getName());
                        break;
                      case NameElement.GENRE_FEMALE:
                        PersistenceElement.writeAsciiLine(fosfemale, n.getName());
                        break;
                      case NameElement.GENRE_UNKNOWN: default:
                        PersistenceElement.writeAsciiLine(fosneutral, n.getName());
                        break;
                    }
                }
                if ( f > 0.45 ) {
                    PersistenceElement.writeAsciiLine(fosln, l);
                }
            }
            fos.close();
            fosn.close();
            fosln.close();
            fosmale.close();
            fosfemale.close();
            fosneutral.close();
        } 
        catch (Exception ex) {
        }
        
        System.out.println("Shorter name in elements: " + minNumberOfElements);
        System.out.println("Longest name in elements: " + maxNumberOfElements);
    }

    public static String normalizeName(String input) {
        String ni = input.toLowerCase();

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
        ni = ni.replace("Ñ", "N");
        ni = ni.replace("ñ", "n");
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
        
        if ( ni.length() <= 1 ) {
            return null;
        }

        if ( ni.equals("de") ) {
            return null;
        }

        if ( ni.equals("del") ) {
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

    public static void processName(
        DBObject o,
	int index,
	int n,
	HashMap<String, NameElement> elements,
	boolean reportAdvances)
    {
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
                if ( nt == 1 ) {
                    nnt = 0;
                }
                else {
                    nnt = ((double) i) / ((double) (nt - 1));
                }
                if ( nnt < 0 || nnt > 1.0 || nnt == Double.NaN ) {
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

    private static int genreHint(String name) {
        int n = name.length();
        if ( name.charAt(n - 1) == 'o' ) {
            return NameElement.GENRE_MALE;
        }
        else if ( name.charAt(n - 1) == 'a' ) {
            return NameElement.GENRE_FEMALE;
        }
        return NameElement.GENRE_UNKNOWN;
    }

    public static void calculateFirstNames(
        DBCollection professionalResume, 
        HashMap<String, NameElement> nameElements) 
    {
        loadLastnamesList("./etc/lastNames.txt");
        try {
            File fd = new File("./output/setFirstNames.mongo");
            FileOutputStream fos;
            fos = new FileOutputStream(fd);
            DBCursor c;
            BasicDBObject filter = new BasicDBObject();
            BasicDBObject options = new BasicDBObject();
            options.append("name", 1);
            options.append("_id", 1);
            //c = professionalResume.find(filter, options);
            c = professionalResume.find();
            System.out.println("Calculating first names: " + c.size());

            int i;
            for ( i = 0; c.hasNext(); i++ ) {
                boolean debug = (i % 1000 == 0);
                DBObject o = c.next();
                if ( !o.containsField("name") ) {
                    continue;
                }
                String n = o.get("name").toString();

                if ( debug ) {
                    System.out.println("  - Processing full name " + i + " / " + 
                        c.size() + " : [" + n + "]");
                }

                StringTokenizer parser = new StringTokenizer(n, " ");
                int j;
                String firstName = "";
                for ( j = 1; parser.hasMoreTokens(); j++ ) {
                    String t;
                    t = parser.nextToken();
                    boolean isLastName = false;
                    String ln;
                    ln = normalizeName(t);

                    //if ( debug ) {
                    //    System.out.println("    . Normalized name element: [" + ln + "]");
                    //}

                    if ( lastNamesFromKnownList != null && ln != null &&
                         lastNamesFromKnownList.contains(ln) ) {
                        isLastName = true;
                    }
                    
                    //if ( nameElements.containsKey(t) ) {
                    //    NameElement ne = nameElements.get(t);
                    //    if ( ne.getPositionAverage() >= 0.5 ) {
                    //        isLastName = true;
                    //    }
                    //}

                    if ( j == 1 ) {
                        firstName = t;
                    }
                    else if ( isLastName ) {
                        break;
                    }
                    else {
                        firstName += " " + t;
                    }
                }

                if ( debug ) {
                    System.out.println("    * Firstname: " + firstName);
                }
                
                firstName = trimExtraNameElements(firstName);
                
                //BasicDBObject content = new BasicDBObject();
                //content.append("firstName", firstName);
                //BasicDBObject newFirstName = new BasicDBObject();
                //newFirstName.append("$set", content);
                //professionalResume.update(o, newFirstName);
                
                PersistenceElement.writeAsciiLine(fos, 
                    "db.professionalResumeTransformed.update({_id: \"" + 
                    o.get("_id") + "\"}, {$set: {firstName: \"" + 
                    firstName + "\"}});");
            }
            fos.close();
        }
        catch ( Exception e ) {
            System.out.println("ERROR: last name proccessing loop!");
            try { Thread.sleep(5000); } catch (Exception ee ){}
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void loadLastnamesList(String filename) {
        if ( lastNamesFromKnownList != null ) {
            return;
        }
        lastNamesFromKnownList = new TreeSet<String>();
        try {
            File fd = new File(filename);
            FileInputStream fis;
            fis = new FileInputStream(fd);
            
            while ( fis.available() > 0 ) {
                String lastname = PersistenceElement.readAsciiLine(fis);
                System.out.println("  - " + lastname);
                lastNamesFromKnownList.add(lastname);
            }
            
            fis.close();
        }
        catch ( Exception e ) {
            
        }
    }

    private static String trimExtraNameElements(String firstName) {
        StringTokenizer parser = new StringTokenizer(firstName, " ");
        if ( parser.countTokens() <= 1 ) {
            return firstName;
        }
        String cn = "";
        int i;
        int count = parser.countTokens();
        String arr[] = new String[count];
        
        for ( i = 0; i < count; i++ ) {
            arr[i] = parser.nextToken();
        }
        
        int n = count;
        if ( arr[count-1].equals("De") ) {
            n--;
        }
        if ( arr[count-1].equals("La") && arr[count-2].equals("De") ) {
            n -= 2;
        }
        for ( i = 0; i < n; i++ ) {
            cn = cn + " " + arr[i];
        }
        return cn;
    }
}
