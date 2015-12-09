//===========================================================================
package webcrawler;

// Java basic classes
import java.io.BufferedInputStream;
import java.io.FileInputStream;

// Toolkit classes
import databaseMongo.model.Resume;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.TreeSet;
import vsdk.toolkit.io.PersistenceElement;

// Application specific classes
import databaseMongo.ComputrabajoDatabaseConnection;

/**
*/
public class ComputrabajoVeDownloader {

    static {
        ComputrabajoDatabaseConnection.createMongoConnection();
    }

    /**
    @param login
    @param password
    @param cookies
    */
    private static TaggedHtml doLoginIntoComputrabajoSystem(
        String login,
        String password,
        ArrayList<String> cookies)
    {
        System.out.println("1. Downloading initial main/front-end page");
        String initialPage = "http://www.computrabajo.com.ve";
        TaggedHtml pageProcessor;
        pageProcessor = new TaggedHtml();
        pageProcessor.getInternetPage(initialPage, cookies, false);

        //printCookies(cookies);

        if ( pageProcessor.segmentList == null ) {
            System.out.println("Warning: empty page");
            return null;
        }

        System.out.println("2. Sending user login credentials");
        String loginJsonPage;
        loginJsonPage = "http://www.computrabajo.com.ve/Ajax/checkLogin.ashx";
        pageProcessor = new TaggedHtml();
        
        TaggedHtml indexPageProcessor;
        indexPageProcessor = pageProcessor.postInternetPageForLogin(
            loginJsonPage, cookies, login, password);
        
        return indexPageProcessor;        
    }

    /**
    @param pageProcessor
    */
    public static void listTagsFromPage(TaggedHtml pageProcessor)
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

        boolean doNext = false;

        for ( i = 0; i < pageProcessor.segmentList.size(); i++ ) {
            ts = pageProcessor.segmentList.get(i);

            if ( !ts.insideTag && doNext ) {
                doNext = false;
                System.out.println(ts.getContent());
            }
            System.out.println("TAG: " + ts.getTagName());

            for ( j = 0; j < ts.getTagParameters().size(); j++ ) {
                n = ts.getTagParameters().get(j).name;
                v = ts.getTagParameters().get(j).value;

                System.out.println("  - " + n + " = " + v);
            }
        }
    }

    /**
    @param cookies 
    */
    private static void printCookies(ArrayList<String> cookies) {
        int i;

        System.out.println("  - Iniciando con " + cookies.size() + " cookies:");
        for ( i = 0; i < cookies.size(); i++ ) {
            System.out.println("    . " + cookies.get(i));
        }
    }

    private static void analizeIndexPages(
        TreeSet<String> listOfResumeLinks,
        TaggedHtml parentPageProcessor, ArrayList<String> cookies) 
    {
        if ( parentPageProcessor.segmentList == null ) {
            System.out.println("Warning: empty page");
            return;
        }
        
        // Here we have first internal page, which is of no use
        //listTagsFromPage(parentPageProcessor);

        System.out.println("4. Accesing resume lists");
        TaggedHtml pageProcessor;
        pageProcessor = new TaggedHtml();
        pageProcessor.getInternetPage(
            "http://empresa.computrabajo.com.ve/Company/Cvs/", cookies, false);
        
        int n;
        n = getNumberOfResumes(pageProcessor);
        System.out.println("  - 4.1. Preparing for downloading " + n + 
            " resumes from "  + (n/20) + " listing pages:" );
        
        int i;
        for ( i = 1; i <= (n/20) + 1; i++ ) {
            System.out.println("    . Downloading listing page " + i + " of " + 
                (n/20 + 1));
            importResumeLinksFromListPage(pageProcessor, listOfResumeLinks);
            pageProcessor = new TaggedHtml();
            pageProcessor.getInternetPage(
                "http://empresa.computrabajo.com.ve/Company/Cvs/?p=" + (i+1), 
                cookies, false);

            if ( i >= 30 ) {
                break;
            }
        }
    }

    private static int getNumberOfResumes(TaggedHtml pageProcessor) {
        TagSegment ts;
        int i;
        int j;
        String n;
        String v;
        boolean insideSpan = false;
        boolean insideTitleSection = false;

        for ( i = 0; i < pageProcessor.segmentList.size(); i++ ) {
            ts = pageProcessor.segmentList.get(i);

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

    private static int convertToInteger(String content) {
        int i;
        String m = "";
        for ( i = 0; i < content.length(); i++ ) {
            char c;
            c = content.charAt(i);
            if ( Character.isDigit(c) ) {
                m += c;    
            }
        }
        
        return Integer.parseInt(m);
    }

    /**
    */
    private static void importResumeLinksFromListPage(
        TaggedHtml pageProcessor, 
        TreeSet<String> resumeLinks) 
    {
        TagSegment ts;
        int i;
        int j;
        String n;
        String v;
        boolean insideList = false;
        String lastUrl = "";

        for ( i = 0; i < pageProcessor.segmentList.size(); i++ ) {
            
            ts = pageProcessor.segmentList.get(i);
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
                            addNewResume(resumeLinks, v);
                        }
                    }
                }                
            }
        }
    }

    private static void addNewResume(TreeSet<String> resumeLinks, String url) 
    {
        try {
            if ( resumeLinks.contains(url) ||
                 !url.contains("/Company/Cvs/curriculums/")) {
                return;
            }
            
            url = TaggedHtml.trimQuotes(url);
            
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
    */
    private static void importListFromCache(
        TreeSet<String> list,
        String filename)
    {
        System.out.println("  - 3.1. Importing loaded profiles cache");
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
                list.add(ll);
            }
            bis.close();
        }
        catch ( Exception e ) {
            
        }
    }
    
    /**
    */
    private static void exportList(TreeSet<String> resumeList) {
        try {
            File fd;
            fd = new File("totalResumeListCache.txt");
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

    private static void downloadResumes(
        TreeSet<String> resumeList, ArrayList<String> cookies) 
    {
        int n = resumeList.size();
        int i = 1;
        System.out.println("7. Analizing individual resumes");
        for ( String url : resumeList ) {
            System.out.println("  - Downloading resume " + i + " of " + n);
            TaggedHtml pageProcessor;
            pageProcessor = new TaggedHtml();
            pageProcessor.getInternetPage(
                "http://empresa.computrabajo.com.ve" + url, cookies, true);
            Resume r;
            r = importResumeFromPage(pageProcessor, 
                "http://empresa.computrabajo.com.ve" + url, cookies);
            ComputrabajoDatabaseConnection.insertResumeMongo(r);
            i++;
        }
    }

    private static Resume importResumeFromPage(
        TaggedHtml pageProcessor, 
        String originUrl,
        ArrayList<String> cookies) 
    {
        Resume r = new Resume();
        r.setSourceUrl(originUrl);
        TagSegment ts;
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
        String lastInside = "";
        String lastSpanType = "";
        String htmlContent = "";
        int divLevel = 0;
        
        for ( i = 0; i < pageProcessor.segmentList.size(); i++ ) {
            ts = pageProcessor.segmentList.get(i);

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
            ArrayList<TagParameter> tp = ts.getTagParameters();
            
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
                TaggedHtml ntp = new TaggedHtml();

                for ( j = 0; j < tp.size(); j++ ) {
                    n = ts.getTagParameters().get(j).name;
                    v = ts.getTagParameters().get(j).value;
                    if ( n.equals("href") ) {
                        System.out.println("JUMPING TO PAGE REDIRECT!");
                        v = TaggedHtml.trimQuotes(v);
                        ntp.getInternetPage(
                            "http://empresa.computrabajo.com.ve" + v, 
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
                        vv = TaggedHtml.trimQuotes(vv);
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
                                "ERROR: Tipo de icono desconocido: " + v);
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
    Not implemented yet
    */
    private static String fixStringForUtf(String v) {
        return v;
    }

    /**
    @param args
    */
    public static void main(String args [])
    {
        TaggedHtml indexPageProcessor;
        ArrayList<String> cookies;
        cookies = new ArrayList<String>();

        indexPageProcessor = doLoginIntoComputrabajoSystem(
            "talent%40abakoventures.com", "holachao", cookies);
        if ( indexPageProcessor != null ) {
            TreeSet<String> resumeListToDownload;
            resumeListToDownload = new TreeSet<String>();
            TreeSet<String> resumeListAlreadyDownloaded;
            resumeListAlreadyDownloaded = new TreeSet<String>();
            String filename = "totalResumeListCache.txt";
            File fd = new File(filename);
            if ( fd.exists() ) {
                importListFromCache(resumeListToDownload, filename);
            }
            analizeIndexPages(
                resumeListToDownload, indexPageProcessor, cookies);
              
            ComputrabajoDatabaseConnection.checkExistingResumesOnDatabase(
                resumeListAlreadyDownloaded);
            removeExistingResumes(
                resumeListToDownload, resumeListAlreadyDownloaded);
            exportList(resumeListToDownload);
            downloadResumes(resumeListToDownload, cookies);
        }
    }

    private static void removeExistingResumes(
        TreeSet<String> resumeListToDownload, 
        TreeSet<String> resumeListAlreadyDownloaded) 
    {
        System.out.println("6. Removing existing resumes from download list");
        System.out.println("  - 6.1. Items before: " + 
            resumeListToDownload.size());
        for ( String l : resumeListAlreadyDownloaded ) {
            while ( resumeListToDownload.contains(l) ) {
                resumeListToDownload.remove(l);
            }
        }
        System.out.println("  - 6.2. Items after: " + 
            resumeListToDownload.size());
    }
}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
