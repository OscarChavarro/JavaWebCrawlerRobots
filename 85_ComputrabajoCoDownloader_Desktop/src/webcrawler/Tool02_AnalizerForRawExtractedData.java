package webcrawler;

// Java basic classes
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

// Mongo classes
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

// VSDK classes
import vsdk.toolkit.common.VSDK;
import vsdk.toolkit.io.PersistenceElement;

// Application specific classes
import databaseMongo.ComputrabajoDatabaseConnection;
import databaseMongo.model.GeographicAdministrativeRegion;
import databaseMongo.model.HtmlExtraInformation;
import databaseMongo.model.NameElement;

/**
*/
public class Tool02_AnalizerForRawExtractedData {
    private static boolean reportAdvances = false;
    private static final ComputrabajoDatabaseConnection databaseConnection;

    static
    {
        databaseConnection = new ComputrabajoDatabaseConnection("localhost" , 
            27017, "computrabajoCo", "professionalResume");
    }


    /**
    Given a database object with a location label, this method performs a 
    process of data normalization following these steps:
      -  Each valid location label should have to parts separated
         by a single slash character. 
      - Each half will have its leading and trailing spaces trimmed.
    */
    private static void processLocation(DBObject o, int index, int n,
        HashMap<String, GeographicAdministrativeRegion> regions) 
    {
        String id = o.get("_id").toString();
        String l = o.get("location").toString();
        if ( l.equals("null") ) {
            if ( reportAdvances ) {
                System.out.println("    . Curriculo vacio ... saltando"); 
            }
            return;
        }
        
        //- 1. Check each half ------------------------------------------------
        int i;
        char c;
        int numberOfSeparators = 0;
        for ( i = 0; i < l.length(); i++ ) {
            c = l.charAt(i);
            if ( c == '/' ) {
                numberOfSeparators++;
            }
        }

        String area = null;

        if ( numberOfSeparators == 0 ) {
            area = l;
            if ( !regions.containsKey(area) ) {
                GeographicAdministrativeRegion areaRegion;
                areaRegion = new GeographicAdministrativeRegion();
                areaRegion.setNameSpa(area);
                regions.put(area, areaRegion);
            }
            return;
        }
        else if ( numberOfSeparators != 1 ) {
            System.out.println("ERROR: La ubicacion " + l + " es invalida:");
            System.out.println("  - Numero incorrecto de separadores");
            System.out.println("  - ID: " + id);
            return;
        }
        
        //- 2. Get first and second parts -------------------------------------
        String subarea = null;
        StringTokenizer parser = new StringTokenizer(l, "/");

        try {
            area = parser.nextToken();
            subarea = parser.nextToken();

            area = TaggedHtml.trimSpaces(area);
            subarea = TaggedHtml.trimSpaces(subarea);
            if ( reportAdvances ) {
                System.out.println(
                    "    . Location: [" + area + "] / [" + subarea + "]");
            }
        }
        catch ( Exception e ) {
            System.out.println("ERROR: No puedo romper la cadena [" + l + "]");
            System.out.println("  - ID: " + id);
        }
        
        //- 3. Build places indexes -------------------------------------------
        GeographicAdministrativeRegion areaRegion;
        if ( regions.containsKey(area) ) {
            areaRegion = regions.get(area);
        }
        else {
            areaRegion = new GeographicAdministrativeRegion();
            areaRegion.setNameSpa(area);
            regions.put(area, areaRegion);
        }

        areaRegion.insertSubarea(subarea);
        
        //---------------------------------------------------------------------
    }

    private static void processName(DBObject o, int index, int n,
        HashMap<String, NameElement> elements) 
    {
        String name = o.get("name").toString();
        String id = o.get("_id").toString();

        if ( name.equals("null")) {
            return;
        }

        if ( reportAdvances ) {
            System.out.println("    . Names: [" + name + "]");
        }

        //---------------------------------------------------------------------
        StringTokenizer parser = new StringTokenizer(name, " ()-.,:;/'[]|\"");
        int nt = parser.countTokens();
        int i;
        for ( i = 0; parser.hasMoreTokens(); i++ ) {
            String ni = normalizeName(parser.nextToken());
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
                nnt = ((double)i) / ((double)(nt-1));
            }
            
            if ( nnt < 0 || nnt > 1.0 || nnt == Double.NaN ) {
                System.out.println("Parando! ERROR ALGORITMO");
                System.exit(1);
            }

            ne.addElement(nnt);
        }

        //---------------------------------------------------------------------
    }
    
    private static void reportResultingAreas(
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

    private static void reportNameElements(
        HashMap<String, NameElement> nameElements) 
    {
        Collection<NameElement> s = nameElements.values();
                
        System.out.println("- ENCOUNTERED NAME ELEMENTS: " + 
            nameElements.size() + "-");
        ArrayList<NameElement> sorted;
        sorted = new ArrayList<NameElement>();
        for ( NameElement ne : s ) {
            sorted.add(ne);
        }
        Collections.sort(sorted);
        int i;
        
        //---
        try {
            File fd = new File("./output/reports/nameElements.csv");
            FileOutputStream fos;
            fos = new FileOutputStream(fd);
            BufferedOutputStream bos;
            bos = new BufferedOutputStream(fos);
            for ( i = 0; i < sorted.size(); i++ ) {
                String l;
                NameElement n = sorted.get(i);
                System.out.println("  - " + (i+1) + ": " +  n);
                l = n.getName() + "\t" + n.getApareancesCount() + "\t" +
                    VSDK.formatDouble(n.getPositionAverage(), 5);
                PersistenceElement.writeAsciiLine(bos, l);
            }
            bos.close();
            fos.close();
        } 
        catch ( Exception ex ) {
        }
    }

    private static String normalizeName(String ni) {
        String nn = "";
        String ln;
        
        ln = ni.toLowerCase();
        int i;
        for ( i = 0; i < ln.length(); i++ ) {
            if ( i == 0 ) {
                nn += ("" + ln.charAt(i)).toUpperCase();                                
            }
            else {
                nn += ln.charAt(i);                
            }
        }
        
        return nn;
    }

    /**
    @param pageProcessor
    @param h
    @param elementCount
    @param id
    */
    public static void processHtmlStructure(
        TaggedHtml pageProcessor,
        HtmlExtraInformation h,
        int elementCount,
        String id)
    {
        if ( pageProcessor.segmentList == null ) {
            System.out.println("Warning: empty page");
            return;
        }

        TagSegment ts;
        int i;
        int j;
        String n;
        String v;

        boolean nextH2 = false;

        for ( i = 0; i < pageProcessor.segmentList.size(); i++ ) {
            ts = pageProcessor.segmentList.get(i);

            if ( ts == null ) {
                continue;
            }

            if ( !ts.insideTag ) {
                String trimmedContent = TaggedHtml.trimSpaces(ts.getContent());
                if ( nextH2 ) {
                    nextH2 = false;
                    h.processH2(trimmedContent);
                }
            }
            
            String tn = ts.getTagName();
            
            if ( tn == null || tn.isEmpty() ) {
                continue;
            }
            
            if ( tn.equals("UL") ) {
                
            }
            else if ( tn.equals("/UL") ) {
                
            } 
            else if ( tn.equals("LI") ) {
                
            }
            else if ( tn.equals("/LI") ) {
                
            }
            else if ( tn.equals("DIV") ) {
                
            }
            else if ( tn.equals("/DIV") ) {
                
            }
            else if ( tn.equals("HR") ) {
                
            }
            else if ( tn.equals("P") ) {
                
            }
            else if ( tn.equals("/P") ) {
                
            }
            else if ( tn.equals("SPAN") ) {
                
            }
            else if ( tn.equals("/SPAN") ) {
                
            }
            else if ( tn.equals("H1") ) {
                
            }
            else if ( tn.equals("/H1") ) {
                
            }
            else if ( tn.equals("H2") ) {
                h.setNh2(h.getNh2()+1);
                nextH2 = true;
            }
            else if ( tn.equals("/H2") ) {
                
            }
            else if ( tn.equals("H3") ) {
                
            }
            else if ( tn.equals("/H3") ) {
                
            }
            else if ( tn.equals("H4") ) {
                
            }
            else if ( tn.equals("/H4") ) {
                
            }
            else if ( tn.equals("H5") ) {
                
            }
            else if ( tn.equals("/H5") ) {
                
            }
            else if ( tn.equals("H6") ) {
                
            }
            else if ( tn.equals("/H6") ) {
                
            }
            else if ( tn.equals("FONT") ) {
                
            }
            else if ( tn.equals("/FONT") ) {
                
            }
            else if ( tn.equals("STRONG") ) {
                
            }
            else if ( tn.equals("/STRONG") ) {
                
            }
            else if ( tn.equals("U") ) {
                
            }
            else if ( tn.equals("/U") ) {
                
            }
            else if ( tn.equals("BR") ) {
                
            }
            else if ( tn.equals("BR/") ) {
                
            }
            else if ( tn.equals("B") ) {
                
            }
            else if ( tn.equals("/B") ) {
                
            }
            else if ( tn.equals("B/") ) {
                
            }
            else if ( tn.equals("I") ) {
                
            }
            else if ( tn.equals("/I") ) {
                
            }
            else if ( tn.equals("OL") ) {
                
            }
            else if ( tn.equals("/OL") ) {
                
            }
            else if ( tn.equals("EM") ) {
                
            }
            else if ( tn.equals("/EM") ) {
                
            }
            else if ( tn.equals("BIG") ) {
                
            }
            else if ( tn.equals("/BIG") ) {
                
            }
            else if ( tn.equals("SUP") ) {
                
            }
            else if ( tn.equals("/SUP") ) {
                
            }
            else if ( tn.equals("SMALL") ) {
                
            }
            else if ( tn.equals("/SMALL") ) {
                
            }
            else if ( tn.equals("ST1:PLACENAME") ) {
                
            }
            else if ( tn.equals("/ST1:PLACENAME") ) {
                
            }
            else if ( tn.equals("ST1:PLACETYPE") ) {
                
            }
            else if ( tn.equals("/ST1:PLACETYPE") ) {
                
            }
            else if ( tn.equals("ST1:PLACE") ) {
                
            }
            else if ( tn.equals("/ST1:PLACE") ) {
                
            }
            else if ( tn.equals("ST1:STATE") ) {
                
            }
            else if ( tn.equals("/ST1:STATE") ) {
                
            }
            else if ( tn.equals("ST1:COUNTRY-REGION") ) {
                
            }
            else if ( tn.equals("/ST1:COUNTRY-REGION") ) {
                
            }
            else if ( tn.equals("BLOCKQUOTE") ) {
                
            }
            else if ( tn.equals("/BLOCKQUOTE") ) {
                
            }
            else if ( tn.equals("A") ) {
                
            }
            else if ( tn.equals("/A") ) {
                
            }
            else if ( tn.equals("TABLE") ) {
                
            }
            else if ( tn.equals("/TABLE") ) {
                
            }
            else if ( tn.equals("TR") ) {
                
            }
            else if ( tn.equals("/TR") ) {
                
            }
            else if ( tn.equals("TH") ) {
                
            }
            else if ( tn.equals("/TH") ) {
                
            }
            else if ( tn.equals("TD") ) {
                
            }
            else if ( tn.equals("/TD") ) {
                
            }
            else if ( tn.equals("DT") ) {
                
            }
            else if ( tn.equals("/DT") ) {
                
            }
            else if ( tn.equals("DL") ) {
                
            }
            else if ( tn.equals("/DL") ) {
                
            }
            else if ( tn.equals("DIR") ) {
                
            }
            else if ( tn.equals("/DIR") ) {
                
            }
            else if ( tn.equals("IMG") ) {
                
            }
            else if ( tn.equals("/IMG") ) {
                
            }
            else if ( tn.equals("CENTER") ) {
                
            }
            else if ( tn.equals("/CENTER") ) {
                
            }
            else if ( tn.equals("FORM") ) {
                
            }
            else if ( tn.equals("/FORM") ) {
                
            }
            else if ( tn.equals("SUM") ) {
                
            }
            else if ( tn.equals("/SUM") ) {
                
            }
            else if ( tn.equals("INPUT") ) {
                
            }
            else if ( tn.equals("/INPUT") ) {
                
            }
            else if ( tn.equals("SELECT") ) {
                
            }
            else if ( tn.equals("/SELECT") ) {
                
            }
            else if ( tn.equals("TEXTAREA") ) {
                
            }
            else if ( tn.equals("/TEXTAREA") ) {
                
            }
            else if ( tn.equals("TT") ) {
                
            }
            else if ( tn.equals("/TT") ) {
                
            }
            else if ( tn.equals("STYLE") ) {
                
            }
            else if ( tn.equals("/STYLE") ) {
                
            }
            else if ( tn.equals("OPTION") ) {
                
            }
            else if ( tn.equals("/OPTION") ) {
                
            }
            else if ( tn.equals("!COMMENT") ) {
                
            }
            else if ( tn.equals("WBR") ) {
                
            }
            else if ( tn.equals("V:TEXTBOX") ) {
                
            }
            else if ( tn.equals("/V:TEXTBOX") ) {
                
            }
            else if ( tn.equals("V:OVAL") ) {
                
            }
            else if ( tn.equals("/V:OVAL") ) {
                
            }
            else if ( tn.equals("W:ANCHORLOCK") ) {
                
            }
            else if ( tn.equals("/W:ANCHORLOCK") ) {
                
            }
            else if ( tn.equals("NOSCRIPT") ) {
                
            }
            else if ( tn.equals("/NOSCRIPT") ) {
                
            }
            else if ( tn.equals("V:SHADOW") ) {
                
            }
            else if ( tn.equals("/V:SHADOW") ) {
                
            }
            else if ( tn.equals("V:H") ) {
                
            }
            else if ( tn.equals("/V:H") ) {
                
            }
            else if ( tn.equals("V:FORMULAS") ) {
                
            }
            else if ( tn.equals("/V:FORMULAS") ) {
                
            }
            else if ( tn.equals("V:SHAPE") ) {
                
            }
            else if ( tn.equals("/V:SHAPE") ) {
                
            }
            else if ( tn.equals("TBODY") ) {
                
            }
            else if ( tn.equals("/TBODY") ) {
                
            }
            else if ( tn.equals("V:PATH") ) {
                
            }
            else if ( tn.equals("/V:PATH") ) {
                
            }
            else if ( tn.equals("V:LINE") ) {
                
            }
            else if ( tn.equals("/V:LINE") ) {
                
            }
            else if ( tn.equals("V:F") ) {
                
            }
            else if ( tn.equals("/V:F") ) {
                
            }
            else if ( tn.equals("V:HANDLES") ) {
                
            }
            else if ( tn.equals("/V:HANDLES") ) {
                
            }
            else if ( tn.equals("W:SDT") ) {
                
            }
            else if ( tn.equals("/W:SDT") ) {
                
            }
            else if ( tn.equals("W:WRAP") ) {
                
            }
            else if ( tn.equals("/W:WRAP") ) {
                
            }
            else if ( tn.equals("?XML:NAMESPACE") ) {

            }
            else if ( tn.equals("O:P") ) {

            }
            else if ( tn.equals("/O:P") ) {

            }
            else if ( tn.equals("O:WRAPBLOCK") ) {
                
            }
            else if ( tn.equals("/O:WRAPBLOCK") ) {
                
            }
            else if ( tn.equals("O:WRAPLOCK") ) {
                
            }
            else if ( tn.equals("/O:WRAPLOCK") ) {
                
            }
            else if ( tn.equals("O:LOCK") ) {
                
            }
            else if ( tn.equals("/O:LOCK") ) {
                
            }
            else if ( tn.equals("O:TOP") ) {
                
            }
            else if ( tn.equals("/O:TOP") ) {
                
            }
            else if ( tn.equals("O:RIGHT") ) {
                
            }
            else if ( tn.equals("/O:RIGHT") ) {
                
            }
            else if ( tn.equals("O:LEFT") ) {
                
            }
            else if ( tn.equals("/O:LEFT") ) {
                
            }
            else if ( tn.equals("O:BOTTOM") ) {
                
            }
            else if ( tn.equals("/O:BOTTOM") ) {
                
            }
            else if ( tn.equals("O:COLUMN") ) {
                
            }
            else if ( tn.equals("/O:COLUMN") ) {
                
            }
            else if ( tn.equals("V:SHAPETYPE") ) {
                
            }
            else if ( tn.equals("/V:SHAPETYPE") ) {
                
            }
            else if ( tn.equals("V:RECT") ) {
                
            }
            else if ( tn.equals("/V:RECT") ) {
                
            }
            else if ( tn.equals("V:SHADOW") ) {
                
            }
            else if ( tn.equals("/V:SHADOW") ) {
                
            }
            else if ( tn.equals("V:TEXTPATH") ) {
                
            }
            else if ( tn.equals("/V:TEXTPATH") ) {
                
            }
            else if ( tn.equals("V:IMAGEDATA") ) {
                
            }
            else if ( tn.equals("/V:IMAGEDATA") ) {
                
            }
            else if ( tn.equals("V:STROKE") ) {
                
            }
            else if ( tn.equals("/V:STROKE") ) {
                
            }
            else if ( tn.equals("V:FILL") ) {
                
            }
            else if ( tn.equals("/V:FILL") ) {
                
            }
            else if ( tn.equals("V:H") ) {
                
            }
            else if ( tn.equals("/V:H") ) {
                
            }
            else if ( tn.equals("ST1:PERSONNAME") ) {
                
            }
            else if ( tn.equals("/ST1:PERSONNAME") ) {
                
            }
            else if ( tn.equals("ST1:METRICCONVERTER") ) {
                
            }
            else if ( tn.equals("/ST1:METRICCONVERTER") ) {
                
            }
            else if ( tn.equals("-") ) {
                
            }
            else if ( tn.equals("ESTUDIOS") ) {
                
            }
            else if ( tn.equals("COLGROUP") ) {
                
            }
            else if ( tn.equals("COL") ) {
                
            }
            else if ( tn.equals("COLOR") ) {
                
            }
            else if ( tn.equals("COLOR,") ) {
                
            }
            else if ( tn.equals("/COLOR") ) {
                
            }
            else if ( tn.equals("INS") ) {
                
            }
            else if ( tn.equals("/INS") ) {
                
            }
            else if ( tn.equals("NOBR") ) {
                
            }
            else if ( tn.equals("/NOBR") ) {
                
            }
            else if ( tn.equals("BODY") ) {
                
            }
            else if ( tn.equals("/BODY") ) {
                
            }
            else if ( tn.equals("ALEXIS") ) {
                
            }
            else if ( tn.equals("SKYPE:SPAN") ) {
                
            }
            else if ( tn.equals("/SKYPE:SPAN") ) {
                
            }
            else if ( tn.equals("ATOMICELEMENT") ) {
                
            }
            else if ( tn.equals("/ATOMICELEMENT") ) {
                
            }
            else if ( tn.equals("DISPONIBILIDAD") ) {
                
            }
            else if ( tn.equals("BARRIO") ) {
                
            }
            else if ( tn.equals("EXPERIENCIA") ) {
                
            }
            else {
                System.out.println("WARNING: NO TAG REGISTERED -> " + tn);
                System.out.println("  - i: " + elementCount);
                System.out.println("  - id: " + id);
                //System.exit(1);
            }
            /*
            System.out.println("TAG: " + tn);
            for ( j = 0; j < ts.getTagParameters().size(); j++ ) {
                n = ts.getTagParameters().get(j).name;
                v = ts.getTagParameters().get(j).value;

                System.out.println("  - " + n + " = " + v);
            }
            */
        }
    }

    private static void processHtmlContent(DBObject o, int elementCount) {
        String html = o.get("htmlContent").toString();        
        String id = o.get("_id").toString();
        
        if ( html ==  null || html.equals("null") || html.isEmpty() ) {
            System.out.println("Empty HTML - skipping id " + id);
            return;
        }

        //System.out.println("  - HTML: " + html);
        TaggedHtml page;
        page = new TaggedHtml();
        InputStream is;
        is = new ByteArrayInputStream(html.getBytes());
        page.importDataFromHtml(is);
        
        HtmlExtraInformation h;
        h = new HtmlExtraInformation();
        
        processHtmlStructure(page, h, elementCount, id);
        
        if ( h.getNh2() != 4 ) {
            System.out.println("***** WARNING: H2 apareances: " + h.getNh2());
            System.out.println("  - HTML: " + html);
            //System.exit(2);
        }
    }

    private static String getExtension(String name)
    {
        String m = "";
        
        int i;
        int n = name.length();
        for ( i = n - 1; i >= 0; i-- ) {
            char c = name.charAt(i);
            if ( c == '.' ) {
                return m;
            }
            m = "" + c + m;
        }
        
        return "";
    }
    
    private static void processProfilePictureUrl(DBObject o, String id, int i) {
        try {
            String p = o.get("profilePictureUrl").toString();
            
            if ( p.equals("null") ) {
                return;
            }
            String ext = getExtension(p);

            String filename = "./output/profilePictures/" + id + "." + ext;
            downloadImage(p, filename);
        } catch ( Exception ex ) {
            Logger.getLogger(Tool02_AnalizerForRawExtractedData.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void main(String args[])
    {
        DBCollection professionalResume;
        professionalResume = 
            databaseConnection.getProfessionalResume();
        if ( professionalResume == null ) {
            return;
        }
        
        DBObject filter = new BasicDBObject();
        BasicDBObject options = new BasicDBObject("profilePictureUrl", true);
        //options.append("sort", new BasicDBObject("name", 1));
        DBCursor c = professionalResume.find(filter, options);
        
        int i;
        HashMap<String, GeographicAdministrativeRegion> regions;
        regions = new HashMap<String, GeographicAdministrativeRegion>();
        HashMap<String, NameElement> nameElements;
        nameElements = new HashMap<String, NameElement>();
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

            if ( o.containsField("location") ) {
                processLocation(o, i, c.count(), regions);
            }
            if ( o.containsField("name") ) {
                processName(o, i, c.count(), nameElements);
            }
            if ( o.containsField("htmlContent") ) {
                processHtmlContent(o, i);
            }
            if ( o.containsField("profilePictureUrl") ) {
              //  processProfilePictureUrl(o, id, i);
            }
            //if ( i >= 2000 ) {
            //    break;
            //}
            reportAdvances = false;
        }
        reportResultingAreas(regions);
        reportNameElements(nameElements);
    }

    private static void downloadImage(String url, String filename) 
    {
        File fd;

        fd = new File(filename);

        if ( fd.exists() ) {
            return;
        }

        Process p;
        String arr[] = {"/usr/local/bin/wget", "-O", filename, url};
        try {
            p = Runtime.getRuntime().exec(arr); 
            p.waitFor();
        } catch ( Exception ex ) {
            Logger.getLogger(Tool02_AnalizerForRawExtractedData.class.getName()).
                log(Level.SEVERE, null, ex);
        }
        
    }

}
