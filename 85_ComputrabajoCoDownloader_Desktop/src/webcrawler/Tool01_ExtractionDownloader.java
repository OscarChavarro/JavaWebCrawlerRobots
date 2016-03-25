//===========================================================================
package webcrawler;

// Java basic classes
import java.io.File;
import java.io.FileOutputStream;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.TreeSet;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

// MongoDB classes
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

// VSDK classes
import vsdk.toolkit.io.PersistenceElement;

// Application specific classes
import databaseMongo.model.Resume;
import databaseMongo.ComputrabajoMongoDatabaseConnection;

/**
*/
public class Tool01_ExtractionDownloader {

    private static final ComputrabajoMongoDatabaseConnection databaseConnection;
    private static final boolean WITH_DEBUG_MESSAGES = false;

    static 
    {
        databaseConnection = new ComputrabajoMongoDatabaseConnection(
            "localhost", 
            27018, 
            "computrabajoCo", 
            "professionalResume");
    }

    /**
    Connects to Computrabajo Colombia system and try to log in with given user
    credentials. On success, login identification cookies are appended to 
    'cookies' list and true is returned.
    @param inLogin username, usually an email address
    @param inPassword current user password
    @param outCookies
    @return true if login process went well, false otherwise.
    */
    private static boolean doLoginIntoComputrabajoSystem(
        final String inLogin,
        final String inPassword,
        ArrayList<String> outCookies)
    {
        System.out.println("1. Downloading initial main/front-end page");
        String initialPage = "http://empresa.computrabajo.com.co/Login.aspx";

        ComputrabajoTaggedHtml pageProcessor;
        pageProcessor = new ComputrabajoTaggedHtml();
        pageProcessor.getInternetPage(initialPage, outCookies, false);
        
        if ( WITH_DEBUG_MESSAGES ) {
            printCookies(outCookies);
        }

        if ( pageProcessor.segmentList2 == null ) {
            System.out.println("Warning: empty page, stage A");
            return false;
        }
        
        HashMap<String, String> initialIdentifiers;
        initialIdentifiers = new HashMap<String, String>();
        extractLoginIdentifiers(pageProcessor, initialIdentifiers);

        System.out.println("3. Sending user login credentials");
        String loginJsonPage = "http://empresa.computrabajo.com.co/Login.aspx";
        pageProcessor = new ComputrabajoTaggedHtml();
        
        return pageProcessor.postInternetPageForLogin(
            loginJsonPage, outCookies, inLogin, inPassword, initialIdentifiers);
    }

    /**
    Detects some FORM INPUT tags which contains identifier data used on login
    process.
    @param pageProcessor
    @param identifiers
    */
    private static void extractLoginIdentifiers(
        ComputrabajoTaggedHtml pageProcessor,
        HashMap<String, String> identifiers)
    {
        if ( pageProcessor.segmentList2 == null ) {
            System.out.println("Warning: empty page stage C");
            return;
        }

        ComputrabajoTagSegment ts;
        int i;
        int j;
        String n;
        String v;
        boolean doNext = false;

        for ( i = 0; i < pageProcessor.segmentList2.size(); i++ ) {
            ts = pageProcessor.segmentList2.get(i);

            if ( ts == null ) {
                continue;
            }
            
            if ( !ts.insideTag && doNext ) {
                doNext = false;
                System.out.println(ts.getContent());
            }
            
            String tn = ts.getTagName();
            
            if ( tn != null && tn.equals("INPUT") ) {
                String elementName = null;
                String elementValue = null;
                for ( j = 0; j < ts.getTagParameters().size(); j++ ) {
                    n = ts.getTagParameters().get(j).name;
                    v = ts.getTagParameters().get(j).value;

                    if ( n.equals("name") ) {
                        elementName = ComputrabajoTaggedHtml.trimQuotes(v);
                    }
                    if ( n.equals("value") ) {
                        elementValue = ComputrabajoTaggedHtml.trimQuotes(v);
                    }
                }

                if ( elementName != null && elementValue != null ) {
                    identifiers.put(elementName, elementValue);
                }
            }            
        }
    }

    /**
    Print debug information from current cookies list.
    @param cookies 
    */
    private static void printCookies(ArrayList<String> cookies) 
    {
        int i;

        System.out.println("  - Current cookies set with " + cookies.size() + 
            " elements:");
        for ( i = 0; i < cookies.size(); i++ ) {
            System.out.println("    . " + cookies.get(i));
        }
    }

    /**
    Downloads index pages. The main objective for this method is to get a list
    of resume URL links, which are returned on listOfResumeLinks parameter.
    @param outListOfResumeLinks
    @param inOutCookies 
    */
    private static void downloadIndexPages(
        TreeSet<String> outListOfResumeLinks,
        ArrayList<String> inOutCookies) 
    {
        ComputrabajoTaggedHtml pageProcessor;
        pageProcessor = new ComputrabajoTaggedHtml();
        pageProcessor.getInternetPage(
            "http://empresa.computrabajo.com.co/Company/Cvs", 
            inOutCookies, false);
                
        int n;
        n = getNumberOfResumes(pageProcessor);
        System.out.println("  - 5.1. Preparing for downloading " + n + 
            " resumes from "  + (n/20) + " listing pages:" );
        
        int i; // 106700
        // 1000 paginas de 20 se bajan ambas fases en 3h13min..
        int nb = 34; // Bloques de a 4000 listas, 80000 hojas de vida
        // El viernes a las 3:12pm se baja el bloque 34
        int start;
        int end;
        //start = (n/20) - (nb+1)*4000;
        start = 1;
        //end = (n/20) - (nb)*4000 + 100;
        end = 1000;
        for ( i = start; i <= (n/20) + 1; i++ ) {
            // Process current page
            Date date = new Date();
            DateFormat format = new SimpleDateFormat(
                "yyyy-MM-dd'T'HH:mm:ss'Z'", 
                Locale.ENGLISH);

            System.out.println("    . Downloading listing page " + 
                i + " of " + 
                (n/20 + 1) + " on time " + format.format(date));
            importResumeLinksFromIndexPage(pageProcessor, outListOfResumeLinks);
            
            if ( i == end ) {
                System.out.println(
                    "***** SEQUENCE DONE, ENDING INDEX PAGES DOWNLOAD! *****");
                break;
            }

            // Advance to next
            pageProcessor = new ComputrabajoTaggedHtml();
            pageProcessor.getInternetPage(
                "http://empresa.computrabajo.com.co/Company/Cvs/?p=" + 
                 (i+1), inOutCookies, false);
        }
    }

    /**
    Identifies the total number of resumes available for download from an index
    page which data is stored at inPageProcessor parameter.
    @param inPageProcessor
    @return 
    */
    private static int getNumberOfResumes(
        ComputrabajoTaggedHtml inPageProcessor) 
    {
        ComputrabajoTagSegment ts;
        int i;
        int j;
        String n;
        String v;
        boolean insideSpan = false;
        boolean insideTitleSection = false;

        for ( i = 0; i < inPageProcessor.segmentList2.size(); i++ ) {
            ts = inPageProcessor.segmentList2.get(i);

            if ( !ts.insideTag ) {
                if ( insideTitleSection && insideSpan ) {
                    int nn = convertToInteger(ts.getContent());
                    return nn;
                }
            }
            String tn = ts.getTagName();

            if ( tn != null && tn.equals("SECTION") ) {
                for ( j = 0; j < ts.getTagParameters().size(); j++ ) {
                    n = ts.getTagParameters().get(j).name;
                    v = ts.getTagParameters().get(j).value;
                    if ( n.equals("class") && v.contains("breadinfo") ) {
                        insideTitleSection = true;
                        break;
                    }
                }
            }
            else if ( tn != null && tn.equals("/SECTION") ) {
                insideTitleSection = false;
            }

            if ( tn != null && tn.equals("SPAN") ) {
                insideSpan = true;
            }
            else if ( tn != null && tn.equals("/SPAN") ) {
                insideSpan = false;
            }            
        }     
        return 0;
    }

    /**
    Parses inContent String in search for an integer number, taking care of
    trimming out unneeded format characters.
    @param inContent
    @return 
    */
    private static int convertToInteger(String inContent) 
    {
        int i;
        String m = "";
        for ( i = 0; i < inContent.length(); i++ ) {
            char c;
            c = inContent.charAt(i);
            if ( Character.isDigit(c) ) {
                m += c;    
            }
        }
        
        return Integer.parseInt(m);
    }

    /**
    @param inPageProcessor
    @param outResumeLinks 
    */
    private static void importResumeLinksFromIndexPage(
        ComputrabajoTaggedHtml inPageProcessor, 
        TreeSet<String> outResumeLinks) 
    {
        ComputrabajoTagSegment ts;
        int i;
        int j;
        String n;
        String v;
        boolean insideList = false;
        String lastUrl = "";

        for ( i = 0; i < inPageProcessor.segmentList2.size(); i++ ) {
            
            ts = inPageProcessor.segmentList2.get(i);
            if ( ts == null ) continue;

            String tn = ts.getTagName();
            if ( tn != null && tn.equals("UL") ) {
                for ( j = 0; j < ts.getTagParameters().size(); j++ ) {
                    n = ts.getTagParameters().get(j).name;
                    v = ts.getTagParameters().get(j).value;
                    if ( n.equals("id") && v.contains("p_candidatos") ) {
                        insideList = true;
                        break;
                    }
                }
            }
            else if ( insideList && tn != null && tn.equals("A") ) {
                for ( j = 0; j < ts.getTagParameters().size(); j++ ) {
                    n = ts.getTagParameters().get(j).name;
                    v = ts.getTagParameters().get(j).value;
                    if ( n.equals("href") ) {
                        if ( !v.equals(lastUrl) ) {
                            lastUrl = v;
                            addNewResumeToBeDownloaded(outResumeLinks, v);
                        }
                    }
                }                
            }
        }
    }

    /**
    @param resumeLinks
    @param url 
    */
    private static void addNewResumeToBeDownloaded(
        TreeSet<String> resumeLinks, String url) 
    {
        try {
            if ( resumeLinks.contains(url) ||
                 !url.contains("/Company/Cvs/hojas-de-vida/")) {
                return;
            }
            
            url = ComputrabajoTaggedHtml.trimQuotes(url);
            
            resumeLinks.add(url);
            File fd;
            fd = new File("partialResumeListCache.txt");
            FileOutputStream fos;
            fos = new FileOutputStream(fd, true);
            PersistenceElement.writeAsciiLine(fos, url);
            fos.close();
        }
        catch ( Exception e ) {
            
        }
    }

    /**
    @param list
    @param filename 
    */
    private static void importListFromCache(
        TreeSet<String> list,
        String filename)
    {
        System.out.println("  - 1.1. Importing loaded profiles cache from " + 
            filename);
        try {
            File fd;
            fd = new File(filename);
            FileInputStream fis;
            fis = new FileInputStream(fd);
            BufferedInputStream bis;
            bis = new BufferedInputStream(fis);
            
            while ( bis.available() > 0 ) {
                String l = PersistenceElement.readAsciiLine(bis);
                int i;
                String ll = "";
                for ( i = 0; i < l.length(); i++ ) {
                    char c = l.charAt(i);
                    if ( c == '\t' ) {
                        continue;
                    }
                    ll += c;
                }
                if ( ll.charAt(0) == '/' ) {
                    list.add(ll);
                }
            }
            bis.close();
        }
        catch ( Exception e ) {
            
        }
    }
    
    /**
    @param resumeList
    @param filename 
    */
    private static void exportListToCache(
        TreeSet<String> resumeList, String filename) 
    {
        try {
            File fd;
            fd = new File(filename);
            FileOutputStream fos;
            fos = new FileOutputStream(fd, true);
            
            for ( String url : resumeList ) {
                PersistenceElement.writeAsciiLine(fos, url);                
            }
            
            fos.close();
        }
        catch ( Exception e ) {
            
        }
    }

    /**
    Controls individual resume profile downloading taking into account the
    current marked for processing. Note that this algorithm allows to run
    the program incrementally, adding new profiles to currently downloaded
    ones.
    @param resumeList
    @param cookies 
    */
    private static void downloadResumes(
        TreeSet<String> resumeList, ArrayList<String> cookies) 
    {
        int n = resumeList.size();
        int i = 1;
        System.out.println("7. Downloading individual resumes: " + n);
        for ( String url : resumeList ) {
            System.out.println("  - Downloading resume " + i + " of " + n);
            ComputrabajoTaggedHtml pageProcessor;
            pageProcessor = new ComputrabajoTaggedHtml();
            String totalUrl = "http://empresa.computrabajo.com.co" + url;
            pageProcessor.getInternetPage(totalUrl, cookies, true);
            Resume r;
            r = importResumeFromPage(
                pageProcessor, 
                "http://empresa.computrabajo.com.co" + url, 
                cookies);
            if ( r != null ) {
		databaseConnection.insertResume(r);
	    }
            i++;
        }
    }

    /**
    Given a downloaded resume profile downloaded from Computrabajo web system
    and stored as HTML page, this method builds an in RAM Resume data structure
    ready for database storage.
    @param pageProcessor
    @param originUrl
    @param cookies
    @return 
    */
    private static Resume importResumeFromPage(
        ComputrabajoTaggedHtml pageProcessor, 
        String originUrl,
        ArrayList<String> cookies) 
    {
        Resume r = new Resume();
        r.setSourceUrl(originUrl);
        ComputrabajoTagSegment ts;
        int i;
        int j;
        String n;
        String v;
        boolean insideSection = false;
        boolean insideArticle = false;
        boolean insideHeader = false;
        boolean insideLeftDiv = false;
        boolean insideRightDiv = false;
        boolean redirectWarning = false;
        boolean nextTitle = false;
        boolean nextEmail = false;
        boolean nextPhone = false;
        boolean nextLocation = false;
        boolean nextAge = false;
        boolean nextPair = false;
        boolean nextWorking = false;
        boolean nextYes = false;
        boolean nextNo = false;
        boolean nextPayment = false;
        boolean nextResume = false;
        boolean nextDocumentId = false;
        String lastInside = "";
        String lastSpanType = "";
        String htmlContent = "";
        int divLevel = 0;

	if ( pageProcessor == null || pageProcessor.segmentList2 == null ) {
            return null;
	}
        
        for ( i = 0; i < pageProcessor.segmentList2.size(); i++ ) {
            ts = pageProcessor.segmentList2.get(i);

            if ( ts == null ) {
                continue;
            }
            
            if ( !ts.insideTag ) {
                String content = ts.getContent();
                if ( insideSection ) {
                    lastInside = content;
                }
                
                if ( insideRightDiv ) {
                    htmlContent += content;
                }

                if ( nextTitle ) {
                    r.setName(content);
                    nextTitle = false;
                }
                
                if ( insideLeftDiv ) {
                    if ( nextEmail ) {
                        r.setEmail(content);
                        nextEmail = false;
                    }
                    if ( nextPhone ) {
                        r.setPhone(content);
                        nextPhone = false;
                    }
                    if ( nextLocation ) {
                        r.setLocation(content);
                        nextLocation = false;
                    }
                    if ( nextDocumentId ) {
                        r.setDocumentId(content);
                        nextDocumentId = false;
                    }
                    if ( nextAge ) {
                        r.setAge(Integer.parseInt(content));
                        nextAge = false;
                    }
                    if ( nextPair ) {
                        r.setPair(content);
                        nextPair = false;
                    }
                    if ( nextWorking ) {
                        r.setJobSearchStatus(content);
                        nextWorking = false;
                    }
                    if ( nextYes ) {
                        nextYes = false;
                        r.setBinaryValue(content, true);
                    }
                    if ( nextNo ) {
                        r.setBinaryValue(content, true);
                        nextNo = false;
                    }
                    if ( nextPayment ) {
                        r.setWantedPayment(content);
                        nextPayment = false;
                    }
                }
                
                if ( content.equals("Object moved to ") ) {
                    redirectWarning = true;
                }
            }
            
            String tn = ts.getTagName();
            ArrayList<ComputrabajoTagParameter> tp = ts.getTagParameters();
            
            if ( tn == null ) {
                continue;
            }

            if ( tn.equals("A") && nextResume ) {
                for ( j = 0; j < tp.size(); j++ ) {
                    n = ts.getTagParameters().get(j).name;
                    v = ts.getTagParameters().get(j).value;
                    if ( n.equals("href") ) {
                        r.setResumeLink(v);
                        nextResume = false;
                        break;
                    }
                }
            }
            if ( tn.equals("A") && redirectWarning ) {
                ComputrabajoTaggedHtml ntp = new ComputrabajoTaggedHtml();

                for ( j = 0; j < tp.size(); j++ ) {
                    n = ts.getTagParameters().get(j).name;
                    v = ts.getTagParameters().get(j).value;
                    if ( n.equals("href") ) {
                        System.out.println("JUMPING TO PAGE REDIRECT!");
                        v = ComputrabajoTaggedHtml.trimQuotes(v);
                        ntp.getInternetPage(
                            "http://empresa.computrabajo.com.co" + v, 
                            cookies, true);

                        return importResumeFromPage(ntp, originUrl, cookies);
                    }
                }

            }
            
            if ( tn.equals("ARTICLE") ) {
                for ( j = 0; tp != null && j < tp.size(); j++ ) {
                   n = ts.getTagParameters().get(j).name;
                   v = ts.getTagParameters().get(j).value;
                   
                   if ( n.equals("id") && v.contains("candidato") ) {
                       insideArticle = true;
                   }
                }
            }
            else if ( tn.equals("/ARTICLE") ) {
                insideArticle = false;
            }
            
            if ( tn.equals("SECTION") && insideArticle ) {
                if ( tp == null || tp.size() <= 1 ) {
                    insideSection = true;                    
                }
            }
                        
            if ( tn.equals("/SECTION") ) {
                if ( insideSection ) {
                    insideSection = false;
                }
            }

            if ( tn.equals("HEADER") ) {
                insideHeader = true;                    
            }
            if ( tn.equals("/HEADER") ) {
                insideHeader = false;
            }
            if ( insideHeader && tn.equals("STRONG") ) {
                nextTitle = true;
                insideHeader = false;
                continue;
            }

            if ( !insideArticle ) {
                continue;
            }
            
            if ( tn.equals("DIV") ) {
                divLevel++;
                for ( j = 0; tp != null && j < tp.size(); j++ ) {
                    n = ts.getTagParameters().get(j).name;
                    v = ts.getTagParameters().get(j).value;

                    if ( n.equals("class") ) {
                        if ( v.contains("cm-3 box_p_icon") ) {
                            insideLeftDiv = true;
                        }
                        else if ( v.contains("cm-9") ) {
                            insideLeftDiv = false;
                            insideRightDiv = true;
                            divLevel = 1;
                        }
                        else if ( v.contains("candidato_pdf") ) {
                            nextResume = true;
                        }
                    }
                }
                if ( insideRightDiv ) {
                    continue;
                }
            }
            if ( tn.equals("/DIV") ) {
                if ( divLevel == 1 && insideRightDiv ) {
                    insideRightDiv = false;
                }
                divLevel--;
            }
            else if ( tn.equals("IMG") ) {
                for ( j = 0; tp != null && j < tp.size(); j++ ) {
                    n = ts.getTagParameters().get(j).name;
                    v = ts.getTagParameters().get(j).value;
                    if ( n.equals("src") ) {
                        String vv;
                        vv = fixStringForUtf(v);
                        vv = ComputrabajoTaggedHtml.trimQuotes(vv);
                        r.setProfilePictureUrl(vv);
                    }
                }   
            }
            else if ( tn.equals("SPAN") ) {
                lastSpanType = lastInside;
                for ( j = 0; tp != null && j < tp.size(); j++ ) {
                    n = ts.getTagParameters().get(j).name;
                    v = ts.getTagParameters().get(j).value;
                    if ( n.equals("class") && insideLeftDiv ) {
                        if ( v.contains("icon email") ) {
                            nextEmail = true;
                        }
                        else if ( v.contains("icon defic") ) {
                            nextDocumentId = true;
                        }
                        else if ( v.contains("icon mvl") ) {
                            nextPhone = true;
                        }
                        else if ( v.contains("icon mvl") ) {
                            nextPhone = true;
                        }
                        else if ( v.contains("icon pais") ) {
                            nextLocation = true;
                        }
                        else if ( v.contains("icon edad") ) {
                            nextAge = true;
                        }
                        else if ( v.contains("icon pareja") ) {
                            nextPair = true;
                        }
                        else if ( v.contains("icon si") ) {
                            nextYes = true;
                        }
                        else if ( v.contains("icon no") ) {
                            nextNo = true;
                        }
                        else if ( v.contains("icon salario") ) {
                            nextPayment = true;
                        }
                        else if ( v.contains("icon departamento") ) {
                        
                        }
                        else {
                            System.out.println(
                                "ERROR: Unknown icon type: " + v);
                            System.exit(666);
                        }
                    }
                }
            }
            else if ( tn.equals("/SPAN") ) {
                if ( lastSpanType.contains("login") ) {
                    r.setLastLoginDate(lastInside);
                }
                else if ( lastSpanType.contains("modifica") ) {
                    r.setLastUpdateDate(lastInside);
                }
                else if ( lastSpanType.contains("Registrado") ) {
                    r.setRegistrationDate(lastInside);
                }
            }
            
            if ( insideRightDiv ) {
                htmlContent += "<" + tn;
                if ( tp != null && tp.size() > 1 ) {
                    htmlContent += " ";
                }
                for ( j = 1; tp != null && j < tp.size(); j++ ) {
                    n = ts.getTagParameters().get(j).name;
                    v = ts.getTagParameters().get(j).value;
                    htmlContent += n + "=" + v + " ";
                }
                htmlContent += ">";
            }
        }

        r.setHtmlContent(htmlContent);
        return r;
    }

    /**
    Not implemented yet. 
    @todo It is possible that some URLs are being missed due to not being fixed
    @param v
    */
    private static String fixStringForUtf(String v) {
        return v;
    }
    
    /**
    Connects to Mongo database and builds a TreeSet list in RAM containing URLs
    of currently downloaded resume profiles.
    @param properties
    @param resumeListAlreadyDownloaded 
    */
    private static void checkExistingResumesOnDatabase(
        DBCollection properties, 
        TreeSet<String> resumeListAlreadyDownloaded)
    {
        System.out.println("1. Importing all Objects loaded in database... ");
        BasicDBObject query;
        BasicDBObject options;

        query = new BasicDBObject();
        options = new BasicDBObject();
        options.append("sourceUrl", true);                                                                                   
        options.append("_id", false);                                                                                   
        DBCursor c = properties.find(query, options);

        System.out.println("  - 1.1. Importing database entries...");
        int i;

        for ( i = 0; c.hasNext(); i++ )
                {
            if ( i % 10000 == 0 )
                        {
                System.out.println("     . " + i);
            }
            Object o = c.next().get("sourceUrl");                                                                            

            if ( o == null ) {
                continue;
            }

            String url = o.toString();
            // Trim "http://empresa.computrabajo.com.co" out                                                                   
            url = url.substring(34);
            if ( url != null && !url.equals("null") )
                        {
                resumeListAlreadyDownloaded.add(url);
            }
        }

        System.out.println(
            "  - 1.2. Number of resumes already imported in database: " + i);
    }
    
    /**
    Removes already downloaded elements from list of new elements to download.
    @param resumeListToDownload
    @param resumeListAlreadyDownloaded 
    */
    private static void removeExistingResumesInFileCache(
        TreeSet<String> resumeListToDownload, 
        TreeSet<String> resumeListAlreadyDownloaded) 
    {
        System.out.println("6. Removing existing resumes from download list");
        System.out.println(
            "  - 6.1. Items before: " + resumeListToDownload.size());
        for ( String l : resumeListAlreadyDownloaded ) {
            while ( resumeListToDownload.contains(l) ) {
                resumeListToDownload.remove(l);
            }
        }
        System.out.println(
            "  - 6.2. Items after: " + resumeListToDownload.size());
    }

    /**
    Removes all existing URLs in database from resumeListToDownload.
    @param resumeListToDownload
    @param professionalResume 
    */
    private static void removeExistingResumesInDatabase(
        TreeSet<String> resumeListToDownload, 
        DBCollection professionalResume) {
        System.out.println("8. Checking URLs to download against database: " +
            resumeListToDownload.size());
        int i = 1;
        ArrayList<String> zombie = new ArrayList<String>();
        for ( String partialUrl : resumeListToDownload ) {
            String url = "http://empresa.computrabajo.com.co" + partialUrl;
            if ( urlExistsInDatabase(url, professionalResume) ) {
                zombie.add(partialUrl);
            }
            
            if ( i % 50 == 0 ) {
                System.out.println(
                    "  . Excluding existing resumes from download set: " + 
                        i + "/" + resumeListToDownload.size());
            }
            
            i++;
        }

        for ( i = 0; i < zombie.size(); i++ ) {
            resumeListToDownload.remove(zombie.get(i));
        }
        System.out.println("  - 8.1. Final set size: " + resumeListToDownload.size());
    }

    /**
    Check if given url already exist in database.
    @param url
    @param professionalResume
    @return 
    */
    private static boolean urlExistsInDatabase(
        String url, DBCollection professionalResume) 
    {
        BasicDBObject filter = new BasicDBObject("sourceUrl", url);
        BasicDBObject options = new BasicDBObject("sourceUrl", true);
        DBObject o;
        
        o = professionalResume.findOne(filter, options);
        return (o != null);
    }
    
    /**
    Main program connects to Computrabajo Colombia web page, logs in using a
    registered user and downloads resume profiles data incrementally (taking
    in to account previously downloaded profiles). Resulting information is
    stored on a Mongo database.
     
    Program is tested to download about 80.000 resume profiles in 10 hours
    as of march 2016.
    @param args not used
    */
    public static void main(String args [])
    {
        ComputrabajoTaggedHtml indexPageProcessor;
        ArrayList<String> cookies;
        cookies = new ArrayList<String>();

        String totalCacheFilename = "totalResumeListCache.txt";
        File fd = new File(totalCacheFilename);
        TreeSet<String> resumeListToDownload;
        resumeListToDownload = new TreeSet<String>();
        TreeSet<String> resumeListAlreadyDownloaded;
        resumeListAlreadyDownloaded = new TreeSet<String>();

        if ( fd.exists() ) {
            importListFromCache(resumeListToDownload, totalCacheFilename);
        }
        System.out.println(
            "0. Number of URLs on cache: " + resumeListToDownload.size());

        //checkExistingResumesOnDatabase(
        //    databaseConnection.getProfessionalResume(),
        //    resumeListAlreadyDownloaded);

        boolean ready = doLoginIntoComputrabajoSystem(
            "hismael@80milprofesionales.com", "d.jfoQ?1*", cookies);
        
        if ( ready == false ) {
            System.out.println("99. Incorrect login. Quitting.");
            return;
        }

        System.out.println("5. Accesing resume lists");
        indexPageProcessor = new ComputrabajoTaggedHtml();
        //downloadIndexPages(resumeListToDownload, cookies);
        //removeExistingResumesInFileCache(
        //    resumeListToDownload, resumeListAlreadyDownloaded);
        removeExistingResumesInDatabase(
            resumeListToDownload, databaseConnection.getProfessionalResume());
        exportListToCache(resumeListToDownload, totalCacheFilename);
        downloadResumes(resumeListToDownload, cookies);
    }
}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
