//===========================================================================

// Java basic classes
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.TreeSet;

// JDBC classes
//import databaseMysql.ThemeForestDatabaseConnection;

// VSDK classes
import vsdk.toolkit.common.VSDK;
import vsdk.toolkit.io.PersistenceElement;

// Toolkit classes
import webcrawler.TagSegment;
import webcrawler.TaggedHtml;

/**
*/
public class ThemeForestDownloader {

    //private static final MetroCuadradoDatabaseConnection con;

    static {
        //con = new MetroCuadradoDatabaseConnection();
        //ThemeForestDatabaseConnection.createSqlConnection();
    }

    /**
    @param projects
    @param baseUrl
    @param group
    */
    private static void analyzeIndexPage(
        ArrayList<Project> projects, String baseUrl, String group)
    {
        String url = baseUrl + group;

        while ( url != null ) {
            url = downloadProjectsFromIndexPage(url, projects);
        }
    }

    /**
    @param page
    @param projects
    */
    private static String downloadProjectsFromIndexPage(
        String page,
        ArrayList<Project> projects)
    {
        TaggedHtml pageProcessor;

        pageProcessor = new TaggedHtml();

        System.out.println("Bajando: " + page);
        pageProcessor.getInternetPage(page);

        String part = importProjectListFromIndexPage(pageProcessor, projects);
        if ( part == null ) {
            return null;
        }
        else {
            part = TaggedHtml.trimQuotes(part);
        }

        return "http://themeforest.net" + part;
    }

    /**
    @param pageProcessor
    @param projects
    */
    private static String importProjectListFromIndexPage(
        TaggedHtml pageProcessor,
        ArrayList<Project> projects)
    {
        if ( pageProcessor.segmentList == null ) {
            System.out.println("Warning: empty page");
            return null;
        }

        TagSegment ts;
        int i;
        int j;
        String n;
        String v;
        int ulTagLevel = 0;
        boolean insideUlTag = false;
        boolean insideLiTag = false;
        Project p = null;

        boolean isPrice = false;
        boolean isSaleCount = false;
        boolean isTitle = false;

        for ( i = 0; i < pageProcessor.segmentList.size() - 1; i++ ) {
            ts = pageProcessor.segmentList.get(i);

            if ( ts.getTagName() == null ) {
                if ( isPrice && p != null ) {
                    p.setPrice(ts.content);
                    isPrice = false;
                }
                else if ( isSaleCount && p != null ) {
                    String s = TaggedHtml.trimSpaces(ts.content);

                    if ( s.contains("Trending") ) {
                        p.setSalesCount("0");
                    }
                    else if ( s.contains(" Sales") ) {
                        p.setSalesCount(s.replace(" Sales", ""));
                    }
                    else {
                        p.setSalesCount(s.replace(" Sale", ""));
                    }
                    isSaleCount = false;
                }
                else if ( isTitle && p != null ) {
                    p.setTitle(ts.content);
                    isTitle = false;
                }
                continue;
            }

            /*
            System.out.println("    " + ts.getTagName());
            for ( j = 0; j < ts.getTagParameters().size(); j++ ) {
                n = ts.getTagParameters().get(j).name;
                v = ts.getTagParameters().get(j).value;
                System.out.println("        " + n + " = " + v + "; ");
            }
            */

            if ( ts.getTagName().equals("UL") ) {
                for ( j = 0; j < ts.getTagParameters().size(); j++ ) {
                    n = ts.getTagParameters().get(j).name;
                    v = ts.getTagParameters().get(j).value;
                    if ( n.equals("class") && v.contains("item-list") ) {
                        if ( ulTagLevel == 0 ) {
                            insideUlTag = true;
                        }
                        break;
                    }
                }
                ulTagLevel++;
            }
            else if ( ts.getTagName().equals("/UL") ) {
                if ( insideUlTag && ulTagLevel == 1 ) {
                    insideUlTag = false;
                }
                ulTagLevel--;
            }
            else if ( ulTagLevel == 1 && ts.getTagName().equals("LI") ) {
                for ( j = 0; j < ts.getTagParameters().size(); j++ ) {
                    n = ts.getTagParameters().get(j).name;
                    v = ts.getTagParameters().get(j).value;
                    if ( n.equals("data-item-id") ) {
                        insideLiTag = true;
                        p = new Project();
                        p.setId(v);
                    }
                }
            }
            else if ( ulTagLevel == 1 && ts.getTagName().equals("/LI") ) {
                if ( p != null && p.getId() != null ) {
                    projects.add(p);
                    p = null;
                }

                insideLiTag = false;
            }
            else if ( p != null && insideLiTag &&
                      ts.getTagName().equals("P") ) {

                for ( j = 0; j < ts.getTagParameters().size(); j++ ) {
                    n = ts.getTagParameters().get(j).name;
                    v = ts.getTagParameters().get(j).value;

                    if ( n.equals("class") && v.contains(
                            "product-list__price-desktop") ) {
                        isPrice = true;
                    }
                }
            }
            else if ( p != null && insideLiTag &&
                      ts.getTagName().equals("I") ) {
                for ( j = 0; j < ts.getTagParameters().size(); j++ ) {
                    n = ts.getTagParameters().get(j).name;
                    v = ts.getTagParameters().get(j).value;

                    if ( n.equals("class") && v.contains("e-icon") ) {
                        isSaleCount = true;
                    }
                }
            }
            else if ( ts.getTagName().equals("A") ) {
                boolean isAuthor = false;
                boolean isNext = false;
                String url = "<unspecified>";
                boolean catCandidate = true;
                for ( j = 0; j < ts.getTagParameters().size(); j++ ) {
                    n = ts.getTagParameters().get(j).name;
                    v = ts.getTagParameters().get(j).value;

                    if ( n.equals("href") ) {
                        url = v;
                    }
                    else if ( n.equals("class") && v.contains("t-link") &&
                        v.contains("color-default") ) {
                        isAuthor = true;
                        catCandidate = false;
                    }
                    else if ( n.equals("class") && v.contains("t-link") &&
                        v.contains("color-inherit") ) {
                        isTitle = true;
                        catCandidate = false;
                    }
                    else if ( n.equals("class") &&
                            v.contains("pagination__next") ) {
                        isNext = true;
                        catCandidate = false;
                    }
                }

                if ( catCandidate && url != null ) {
                    if ( p != null && url.contains("category/") ) {
                        p.addCategory(url);
                    }
                }

                if ( p != null && insideLiTag && isAuthor ) {
                    p.setAuthor(url);
                }
                else if ( p != null && insideLiTag && p.getUrl() == null ) {
                    p.setUrl(url);
                }
                else if ( isNext ) {
                    return url;
                }
            }
        }
        return null;
    }

    /**
    @param pageProcessor
    */
    private static void listTagsFromPage(TaggedHtml pageProcessor)
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
                System.out.println(ts.content);
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
    @param pageProcessor
    */
    private static void analyzeProjectPage(TaggedHtml pageProcessor, Project p)
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

        boolean insideTable = false;
        boolean nextVar = false;
        boolean nextValue = false;
        boolean nextCreationDate = false;
        boolean nextLastUpdateDate = false;
        boolean nextReport = false;
        boolean nextWidgetReady = false;
        boolean nextCompatibleBrowsers = false;
        boolean nextCompatibleWith = false;
        boolean nextSoftwareVersion = false;
        boolean nextThemeForestFiles = false;
        boolean nextColumns = false;
        boolean nextDocumentation = false;
        boolean nextLayout = false;
        boolean nextTags = false;
        boolean nextHighResolution = false;
        boolean nextFramework = false;
        boolean nextComments1 = false;
        boolean nextComments2 = false;
        boolean nextRating = false;

        for ( i = 0; i < pageProcessor.segmentList.size(); i++ ) {
            ts = pageProcessor.segmentList.get(i);

            if ( !ts.insideTag && nextComments2 ) {
                nextComments2 = false;
                p.setNumberOfComments(ts.content);
            }

            if ( !ts.insideTag && nextRating ) {
                StringTokenizer parser;
                parser = new StringTokenizer(ts.content, " ");

                parser.nextToken();
                p.setRanking(parser.nextToken());
                parser.nextToken();
                parser.nextToken();
                parser.nextToken();
                p.setNumberOfRankings(parser.nextToken());
                nextRating = false;
            }

            if ( !ts.insideTag && nextVar ) {
                if ( ts.content.contains("Created") ) {
                    nextCreationDate = true;
                }
                else if ( ts.content.contains("Last Update") ) {
                    nextLastUpdateDate = true;
                }
                else if ( ts.content.contains("Widget Ready") ) {
                    nextWidgetReady = true;
                }
                else if ( ts.content.contains("Compatible Browsers") ) {
                    nextCompatibleBrowsers = true;
                }
                else if ( ts.content.contains("Compatible With") ) {
                    nextCompatibleWith = true;
                }
                else if ( ts.content.contains("Software Version") ) {
                    nextSoftwareVersion = true;
                }
                else if ( ts.content.contains("ThemeForest Files Included") ) {
                    nextThemeForestFiles = true;
                }
                else if ( ts.content.contains("Columns") ) {
                    nextColumns = true;
                }
                else if ( ts.content.contains("Documentation") ) {
                    nextDocumentation = true;
                }
                else if ( ts.content.contains("Layout") ) {
                    nextLayout = true;
                }
                else if ( ts.content.contains("Tags") ) {
                    nextTags = true;
                }
                else if ( ts.content.contains("High Resolution") ) {
                    nextHighResolution = true;
                }
                else if ( ts.content.contains("Framework") ) {
                    nextFramework = true;
                }

                else {
                    System.out.println("**UNPROCESSED_VAR: " + ts.content);
                    nextReport = true;
                }

                nextVar = false;
            }
            else if ( !ts.insideTag && nextValue ) {
                nextValue = false;

                if ( nextReport ) {
                    System.out.println("  - Value = " + ts.content);
                    nextReport = false;
                }

                if ( nextCreationDate ) {
                    p.setCreationDate(ts.content);
                    nextCreationDate = false;
                }

                if ( nextLastUpdateDate ) {
                    p.setLastUpdateDate(ts.content);
                    nextLastUpdateDate = false;
                }

                if ( nextWidgetReady ) {
                    p.setWidgetReady(ts.content);
                }

                if ( nextCompatibleBrowsers ) {
                    p.setCompatibleBrowsers(ts.content);
                    nextCompatibleBrowsers = false;
                }

                if ( nextCompatibleWith ) {
                    p.setCompatibleWith(ts.content);
                    nextCompatibleWith = false;
                }

                if ( nextSoftwareVersion ) {
                    p.setSoftwareVersion(ts.content);
                    nextSoftwareVersion = false;
                }

                if ( nextThemeForestFiles ) {
                    p.setThemeForestFiles(ts.content);
                    nextThemeForestFiles = false;
                }

                if ( nextColumns ) {
                    p.setColumns(ts.content);
                    nextColumns = false;
                }

                if ( nextDocumentation ) {
                    p.setDocumentation(ts.content);
                    nextDocumentation = false;
                }

                if ( nextLayout ) {
                    p.setLayout(ts.content);
                    nextLayout = false;
                }

                if ( nextTags ) {
                    p.setTags(ts.content);
                    nextTags = false;
                }

                if ( nextHighResolution ) {
                    p.setHighResolution(ts.content);
                    nextHighResolution = false;
                }

                if ( nextFramework ) {
                    p.setFramework(ts.content);
                    nextFramework = false;
                }


            }

            if ( ts.getTagName() == null ) {

            }
            else if ( ts.getTagName().equals("DIV") ) {
                for ( j = 0; j < ts.getTagParameters().size(); j++ ) {
                    n = ts.getTagParameters().get(j).name;
                    v = ts.getTagParameters().get(j).value;

                    if ( n.equals("class") && v.contains("meta-attributes") ) {
                       insideTable = true;
                    }
                }
            }
            else if ( ts.getTagName().equals("SPAN") ) {
                for ( j = 0; j < ts.getTagParameters().size(); j++ ) {
                    n = ts.getTagParameters().get(j).name;
                    v = ts.getTagParameters().get(j).value;

                    if ( n.equals("class") && v.contains("rating-detailed__average") ) {
                        nextRating = true;
                    }
                }
            }
            else if ( ts.getTagName().equals("/DIV") ) {
                insideTable = false;
            }
            else if ( ts.getTagName().equals("/SPAN") && nextComments1 ) {
                nextComments1 = false;
                nextComments2 = true;
            }
            else if ( ts.getTagName().equals("STRONG") ) {
                for ( j = 0; j < ts.getTagParameters().size(); j++ ) {
                    n = ts.getTagParameters().get(j).name;
                    v = ts.getTagParameters().get(j).value;

                    if ( n.equals("class") && v.contains("sidebar-stats__number") ) {
                        nextComments1 = true;
                    }
                }
            }
            else if ( ts.getTagName().equals("TD") ) {
                for ( j = 0; j < ts.getTagParameters().size(); j++ ) {
                    n = ts.getTagParameters().get(j).name;
                    v = ts.getTagParameters().get(j).value;

                    if ( n.equals("class") &&
                         v.contains("meta-attributes__attr-name") ) {
                        nextVar = true;
                    }
                    else if ( n.equals("class") &&
                              v.contains("meta-attributes__attr-detail") ) {
                        nextValue = true;
                    }
                }
            }
        }
    }

    /**
    @param filename
    */
    private static ArrayList<Project> loadBasicProjectDataFromLocalCache(
        String filename)
    {
        ArrayList<Project> projects;

        projects = new ArrayList<Project>();

        try {
            File fd;
            fd = new File(filename);
            FileInputStream fis;
            fis = new FileInputStream(fd);
            BufferedInputStream bis;
            bis = new BufferedInputStream(fis);

            while ( bis.available() > 0 ) {
                String line;
                line = PersistenceElement.readAsciiLine(bis);

                StringTokenizer parser = new StringTokenizer(line, "\t");

                ArrayList<String> t;
                t = new ArrayList<String>();
                while ( parser.hasMoreTokens() ) {
                    t.add(parser.nextToken());
                }

                int n;
                n = t.size();
                if ( n != 7 ) {
                    System.out.println(
                            "Error: wrong simple project model with: " + n);
                }

                Project p = new Project();
                p.setId(t.get(0));
                p.setTitle(t.get(1));
                p.setUrl(t.get(2));
                p.setAuthor(t.get(3));
                p.setPrice(t.get(4));
                p.setSalesCount(t.get(5));

                String c;
                c = t.get(6);
                parser = new StringTokenizer(c, ";");
                while ( parser.hasMoreTokens() ) {
                    p.addCategory(parser.nextToken());
                }

                projects.add(p);
            }
        }
        catch ( Exception e ) {
            VSDK.reportMessageWithException(
                null, VSDK.FATAL_ERROR,
                "ThemeForestDownloader.loadBasicProjectDataFromLocalCache",
                "Error loading data", e);
        }

        return projects;
    }

    /**
    @param projects
    */
    private static TreeSet<String> processBasicProjectData(
        ArrayList<Project> projects)
    {
        System.out.println("PROCESSING BASIC THEMES: " + projects.size());

        TreeSet<String> categories;

        categories = new TreeSet<String>();

        for ( Object e : projects ) {
            Project p = (Project)e;

            int i;
            for ( i = 0; i < p.getCategories().size(); i++ ) {
                if ( !categories.contains(p.getCategories().get(i))) {
                    categories.add(p.getCategories().get(i));
                }
            }
        }

        int i;
        System.out.println("CATEGORIES: " + categories.size());
        for ( String e : categories ) {
            System.out.println("  - " + e);
        }
        return categories;
    }

    /**
    @param projects
    @param sortedCategories
    */
    private static void processExtendedProjectData(
        ArrayList<Project> projects,
        TreeSet<String> sortedCategories)
    {
        System.out.println("DOWNLOADING EXTENDED THEME PROJECT DATA: " +
            projects.size());

        //---------------------------------------------------------------------
        try {
            File fd = new File("reportStage2.txt");
            FileOutputStream fos;
            fos = new FileOutputStream(fd);

            int i;
            String header = "ID\tTITLE\tURL\tAUTHOR\tPRICE\tSALES\t";
            header +=
                "creationDate\tlastUpdateDate\twidgetReady\t" +
                "compatibleBrowsers\tcompatibleWith\t" +
                "softwareVersion\tthemeForestFiles\tcolumns\t" +
                "documentation\tlayout\ttags\thighResolution\tframework\t";
            for ( String c : sortedCategories ) {
                header += c;
                header += "\t";
            }

            PersistenceElement.writeAsciiLine(fos, header);

            for ( i = 0; i < projects.size(); i++ ) {
                Project p = projects.get(i);
                TaggedHtml pageProcessor;

                String page = p.getUrl();

                pageProcessor = new TaggedHtml();

                System.out.println("Bajando pagina extendida: " + "http://themeforest.com"+page);
                pageProcessor.getInternetPage("http://themeforest.com"+page);

                analyzeProjectPage(pageProcessor, p);

                PersistenceElement.writeAsciiLine(fos,
                        p.toStringExtended(sortedCategories));
                fos.flush();
            }
        }
        catch ( Exception e ) {

        }
    }

    /**
    @return
    */
    public static ArrayList<Project>
    downloadBasicProjectDataFromThemeForestWebpage()
    {
        ArrayList<Project> projects;

        projects = new ArrayList<Project>();

        analyzeIndexPage(projects,
            "http://themeforest.net/category/wordpress/", "blog-magazine");
        analyzeIndexPage(projects,
            "http://themeforest.net/category/wordpress/", "buddypress");
        analyzeIndexPage(projects,
            "http://themeforest.net/category/wordpress/", "corporate");
        analyzeIndexPage(projects,
            "http://themeforest.net/category/wordpress/", "creative");
        analyzeIndexPage(projects,
            "http://themeforest.net/category/wordpress/", "directory-listings");
        analyzeIndexPage(projects,
            "http://themeforest.net/category/wordpress/", "ecommerce");
        analyzeIndexPage(projects,
            "http://themeforest.net/category/wordpress/", "education");
        analyzeIndexPage(projects,
            "http://themeforest.net/category/wordpress/", "entertainment");
        analyzeIndexPage(projects,
            "http://themeforest.net/category/wordpress/", "mobile");
        analyzeIndexPage(projects,
            "http://themeforest.net/category/wordpress/", "nonprofit");
        analyzeIndexPage(projects,
            "http://themeforest.net/category/wordpress/", "real-estate");
        analyzeIndexPage(projects,
            "http://themeforest.net/category/wordpress/", "retail");
        analyzeIndexPage(projects,
            "http://themeforest.net/category/wordpress/", "technology");
        analyzeIndexPage(projects,
            "http://themeforest.net/category/wordpress/", "wedding");
        analyzeIndexPage(projects,
            "http://themeforest.net/category/wordpress/", "miscellaneous");

        // Export total set
        try {
            File fd = new File("reportStage1.txt");
            FileOutputStream fos;
            fos = new FileOutputStream(fd);

            for ( Project p : projects ) {
                PersistenceElement.writeAsciiLine(fos, p.toString());
            }
            fos.close();
        }
        catch ( Exception e ) {
            VSDK.reportMessageWithException(null, VSDK.FATAL_ERROR,
                "error", "error", e);
        }
        return projects;
    }

    /**
    @param args
    */
    public static void main(String args [])
    {
        String filename = "reportStage1.txt";
        File fd = new File(filename);

        ArrayList<Project> projects;

        if ( !fd.exists() ) {
            projects = downloadBasicProjectDataFromThemeForestWebpage();
        }
        else {
            projects = loadBasicProjectDataFromLocalCache(filename);
        }

        TreeSet<String> categoryValues;
        TreeSet<String> widgetReadyValues;
        TreeSet<String> compatibleBrowsersValues;
        TreeSet<String> compatibleWithValues;
        TreeSet<String> softwareVersionValues;
        TreeSet<String> themeForestFilesValues;
        TreeSet<String> columnsValues;
        TreeSet<String> documentationValues;
        TreeSet<String> layoutValues;
        TreeSet<String> tagsValues;
        TreeSet<String> highResolutionValues;
        TreeSet<String> frameworkValues;

        categoryValues = processBasicProjectData(projects);
        processExtendedProjectData(projects, categoryValues);

        int i;

        filename = "reportStage3.txt";
        try {
            fd = new File(filename);
            FileOutputStream fos;
            fos = new FileOutputStream(fd);
            BufferedOutputStream bos;
            bos = new BufferedOutputStream(fos);

            String header = "ID\tTITLE\tURL\tAUTHOR\tPRICE\tSALES\t";
            header +=
                "CREATION_DATE\t" +
                "LAST_UPDATE_DATE\t" +
                "RANKING (-1 if none)\t" +
                "NUMBER_OF_RANKING_VOTES\t" +
                "AUTHOR_EXPERIENCE\t";
                //"widgetReady\t" +
                //"compatibleBrowsers\t"+
                //"compatibleWith\t" +
                //"softwareVersion\t" +
                //"themeForestFiles\t" +
                //"columns\t" +
                //"documentation\t" +
                //"layout\t" +
                //"tags\t" +
                //"highResolution\t" +
                //"framework\t";
            for ( String c : categoryValues ) {
                header += c;
                header += "\t";
            }

            PersistenceElement.writeAsciiString(bos, header);

            widgetReadyValues = getValuesForSet(
                bos, projects, "widgetReady");
            compatibleBrowsersValues = getValuesForSet(
                bos, projects, "compatibleBrowsers");
            compatibleWithValues = getValuesForSet(
                bos, projects, "compatibleWith");
            softwareVersionValues = getValuesForSet(
                bos, projects, "softwareVersion");
            themeForestFilesValues = getValuesForSet(
                bos, projects, "themeForestFiles");
            columnsValues = getValuesForSet(
                bos, projects, "columns");
            documentationValues = getValuesForSet(
                bos, projects, "documentation");
            layoutValues = getValuesForSet(
                bos, projects, "layout");
            tagsValues = getValuesForSet(
                bos, projects, "tags");
            highResolutionValues = getValuesForSet(
                bos, projects, "highResolution");
            frameworkValues = getValuesForSet(
                bos, projects, "framework");
            PersistenceElement.writeAsciiString(bos, "\n");

            HashMap<String, AuthorCount> authorValues;

            authorValues = new HashMap<String, AuthorCount>();
            for ( i = 0; i < projects.size(); i++ ) {
                String a;
                a = projects.get(i).getAuthor();
                if ( !authorValues.containsKey(a) ) {
                    authorValues.put(a, new AuthorCount(a));
                }
                else {
                    authorValues.get(a).increment();
                }
            }

            for ( i = 0; i < projects.size(); i++ ) {
                String line;
                line = projects.get(i).toStringComplete(
                    categoryValues,
                    widgetReadyValues,
                    compatibleBrowsersValues,
                    compatibleWithValues,
                    softwareVersionValues,
                    themeForestFilesValues,
                    columnsValues,
                    documentationValues,
                    layoutValues,
                    tagsValues,
                    highResolutionValues,
                    frameworkValues,
                    authorValues);
                PersistenceElement.writeAsciiLine(bos, line);
            }
        }
        catch ( Exception e ) {
            System.out.println("ERROR:");
            VSDK.reportMessageWithException(null,
                VSDK.FATAL_ERROR,
                "main",
                "Error",
                e);
        }
    }

    private static TreeSet<String> getValuesForSet(
        OutputStream os,
        ArrayList<Project> projects, String varname)
    {
        int i;
        TreeSet<String> values;

        values = new TreeSet<String>();

        if ( projects == null || projects.size() < 1 ) {
            return null;
        }

        ArrayList<String> vars;
        vars = projects.get(0).getEncapsulatedVariables();
        boolean isPossible = false;

        for ( i = 0; i < vars.size(); i++ ) {
            if ( vars.get(i).contains(varname) &&
                 vars.get(i).contains("java.lang.String:") ) {
                isPossible = true;
                break;
            }
        }

        if ( !isPossible ) {
            return null;
        }

        try {
            String methodName =
                "get" + varname.substring(0, 1).toUpperCase() +
                varname.substring(1);
            for ( i = 0; i < projects.size(); i++ ) {
                Project p;
                p = projects.get(i);
                Class c = p.getClass();
                Method m = c.getMethod(methodName);
                Object o = m.invoke(p);

                if ( o instanceof String ) {
                    String s;
                    s = (String)o;
                    if ( !values.contains(s) ) {
                        values.add(s);
                    }
                }
            }

            for ( String s : values ) {
                PersistenceElement.writeAsciiString(os, varname + ":" + s + "\t");
            }

        }
        catch ( Exception e ) {
            System.out.println("ERROR! " + e.getMessage());
        }
        return values;
    }
}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
