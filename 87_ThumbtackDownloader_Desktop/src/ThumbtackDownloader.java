//===========================================================================

// Java basic classes
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.net.UnknownHostException;

// MongoDB driver classes
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

// VSDK classes
import vsdk.toolkit.common.VSDK;
import vsdk.toolkit.io.PersistenceElement;

// Toolkit classes
import webcrawler.TagSegment;
import webcrawler.TaggedHtml;

// Application specific classes
import model.FormQuestionElement;
import model.FormQuestion;
import model.Form;

/**
*/
public class ThumbtackDownloader {
    public static final String dataFolder = 
        "/Users/oscar/usr/Abako/80mil/datos/formulariosThumbtack";

    /**
    @param page
    @param projects
    */
    private static void downloadProjectsFromIndexPage(
        ArrayList<ProfessionalService> services,
        String page,
        String areaName)
    {
        TaggedHtml pageProcessor;

        pageProcessor = new TaggedHtml();

        System.out.println("Bajando: " + page);
        pageProcessor.getInternetPage(page);

        processThumbtackServicesList(services, pageProcessor, areaName);
        //listTagsFromPage(pageProcessor);
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
    private static void processThumbtackServicesList(
            ArrayList<ProfessionalService> services, 
            TaggedHtml pageProcessor,
            String areaName)
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

        boolean insideColumn = false;
        boolean doService = false;
        boolean doCategory = false;
        String currentCategoryName = "<undefined>";
        String link = "";

        System.out.println("= " + areaName + " ========================");
        
        for ( i = 0; i < pageProcessor.segmentList.size(); i++ ) {
            ts = pageProcessor.segmentList.get(i);

            if ( ts == null ) {
                continue;
            }

            if ( !ts.insideTag && doService ) {
                doService = false;
                String serviceName;
                serviceName = TaggedHtml.trimSpaces(ts.content);
                System.out.println("  - " + serviceName);
                
                ProfessionalService p;
                p = new ProfessionalService();
                p.setAreaName(areaName);
                p.setCategoryName(currentCategoryName);
                p.setServiceName(serviceName);
                p.setLink(link);
                
                services.add(p);
                
                continue;
            }

            if ( !ts.insideTag && doCategory ) {
                doCategory = false;
                currentCategoryName = TaggedHtml.trimSpaces(ts.content);
                System.out.println(currentCategoryName);
                continue;
            }

            if ( ts.getTagName() == null ) {
                continue;
            }
            
            if ( ts.getTagName() != null && ts.getTagName().equals("UL") ) {
                for ( j = 0; j < ts.getTagParameters().size(); j++ ) {
                    n = ts.getTagParameters().get(j).name;
                    v = ts.getTagParameters().get(j).value;

                    if ( n.equals("class") && v.contains("column") ) {
                        insideColumn = true;
                    }
                }
            }
            else if ( ts.getTagName() != null && ts.getTagName().equals("DIV") ) {
                for ( j = 0; j < ts.getTagParameters().size(); j++ ) {
                    n = ts.getTagParameters().get(j).name;
                    v = ts.getTagParameters().get(j).value;

                    if ( n.equals("class") && v.contains("link") ) {
                        insideColumn = true;
                    }
                }
            }
            else if ( ts.getTagName().equals("/UL") ) {
                insideColumn = false;
            }
            else if ( ts.getTagName().equals("A") && insideColumn ) {
                
                for ( j = 0; j < ts.getTagParameters().size(); j++ ) {
                    n = ts.getTagParameters().get(j).name;
                    v = ts.getTagParameters().get(j).value;
                    v = TaggedHtml.trimQuotes(v);

                    if ( n.equals("href") ) {
                        link = "https://www.thumbtack.com" + v;
                    }
                }
                doService = true;
            }
            else if ( ts.getTagName().equals("H2") ) {
                doCategory = true;
            }
        }
    }

    /**
    @param args
    */
    public static void main(String args [])
    {        
        try {
            String filename = "report.txt";
            ArrayList<ProfessionalService> services;
            ArrayList<Form> forms;
            
            services = new ArrayList<ProfessionalService>();
            forms = new ArrayList<Form>();

            File fd = new File(filename);
            FileOutputStream fos;
            fos = new FileOutputStream(fd);
            //BufferedOutputStream bos;
            //bos = new attic remodel(fos);

            String header = "AREA\tCATEGORY\tSERVICE";
            PersistenceElement.writeAsciiLine(fos, header);

            downloadProjectsFromIndexPage(services, 
                    "https://www.thumbtack.com/home-improvement",
                    "Home");
            downloadProjectsFromIndexPage(services, 
                    "https://www.thumbtack.com/events",
                    "Events");
            downloadProjectsFromIndexPage(services, 
                    "https://www.thumbtack.com/lessons",
                    "Lessons");
            
            downloadProjectsFromIndexPage(services, 
                    "https://www.thumbtack.com/wellness",
                    "Wellness");
            downloadProjectsFromIndexPage(services, 
                    "https://www.thumbtack.com/more-services",
                    "More");
            
            //translateCategories(services);
            //translateServices(services, fos);
            downloadForms(services, forms);
            //exportFormsReport(forms);
            exportFormsToMongoDB(services, forms);
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

    private static void downloadForms(
        ArrayList<ProfessionalService> services,
        ArrayList<Form> forms) 
            throws Exception 
    {
        int i;
        File fd;
        boolean e;

        for ( i = 0; i < services.size(); i++ ) {
            fd = new File(dataFolder + "/" + VSDK.formatNumberWithinZeroes(i, 4) + ".html");
            e = fd.exists();
            if ( !e ) {
                FormDownloaderRobot.downloadForm(services.get(i), i);
                Thread.sleep(2000);
            }
            if ( !e ) {
                //System.exit(9);
            }
            System.out.println("Making up form " + (i+1) + " of " + services.size());
            
            Form f;
            f = processForm(services.get(i), i);
            forms.add(f);
        }
    }

    private static void translateServices(
        ArrayList<ProfessionalService> services, FileOutputStream fos) 
            throws Exception 
    {
        int i;

        for ( i = 0; i < services.size(); i++ ) {
            String msg =
                TranslatorRobot.translateUsingGoogle(
                    services.get(i).getServiceName(), "en", "es");
            System.out.println("MENSAJE:");
            System.out.println("  <- " + services.get(i).getServiceName());
            System.out.println("  -> " + msg);
            PersistenceElement.writeAsciiLine(
                    fos, services.get(i).toString() + "\t" + msg);
            fos.flush();
        }
    }
    
    private static void translateCategories(
        ArrayList<ProfessionalService> services) 
            throws Exception 
    {
        int i;
        
        TreeSet<String> categories;
        
        categories = new TreeSet<String>();
        
        for ( i = 0; i < services.size(); i++ ) {
            ProfessionalService ps;
            ps = services.get(i);
            
            if ( !categories.contains(ps.getCategoryName()) ) {
                categories.add(ps.getCategoryName());
            }
        }
        
        File fd;
        FileOutputStream fos;
        
        fd = new File("reportCategories.txt");
        fos = new FileOutputStream(fd);

        i = 0;
        for ( String e : categories ) {
            String msg = TranslatorRobot.translateUsingGoogle(e, "en", "es");
            System.out.println("MENSAJE " + (i+1) + ":");
            System.out.println("  <- " + e);
            System.out.println("  -> " + msg);
            PersistenceElement.writeAsciiLine(fos, e + "\t" + msg);            
            i++;
        }
    }

    private static Form processForm(ProfessionalService get, int i) {
        try {
            String filename;
            filename = dataFolder + "/" + VSDK.formatNumberWithinZeroes(i, 4) + ".html";
            System.out.println("======================================================================");
            System.out.println(
                "Processing previously downloaded form from file " + filename);
            File fd;
            FileInputStream fis;
            
            fd = new File(filename);
            fis = new FileInputStream(fd);

            TaggedHtml pageProcessor;

            pageProcessor = new TaggedHtml();
            pageProcessor.importDataFromHtml(fis);

            return analyzeForm(pageProcessor);
        }
        catch ( FileNotFoundException e ) {
            System.out.println("ERROR - File not found!");
            System.exit(1);
        }
        return null;
    }

    private static Form analyzeForm(TaggedHtml pageProcessor) {
        if ( pageProcessor.segmentList == null ) {
            System.out.println("Warning: empty page");
            return null;
        }

        int formNumber = 1;
        TagSegment ts;
        int i;
        int j;
        String n;
        String v;
        boolean inForm = false;
        boolean inSet = false;
        boolean doNext = false;
        Form f = new Form();
        FormQuestion q = null;
        FormQuestionElement e = null;
        String currentSelectGroupId = null;

        for ( i = 0; i < pageProcessor.segmentList.size(); i++ ) {
            ts = pageProcessor.segmentList.get(i);

            if ( !ts.insideTag && doNext ) {
                doNext = false;
                if ( e != null ) {
                    e.setContent(TaggedHtml.trimSpaces(ts.content));
                    e = null;
                }
            }

            String t;
            t = ts.getTagName();
            if ( t == null || t.length() < 1 ) {
                continue;
            }
            
            if ( t.equals("FORM") ) {
                if ( formNumber == 3 ) {
                    inForm = true;
                }
                formNumber++;
            }
            else if ( t.equals("/FORM") ) {
                inForm = false;
            }
            else if ( inForm && t.equals("FIELDSET") ) {
                q = new FormQuestion();
                inSet = true;
            }
            else if ( t.equals("/FIELDSET") ) {
                if ( q != null ) {
                    f.getQuestions().add(q);
                    q = null;
                }
                inSet = false;
            }
            else if ( inSet && q != null && t.equals("LABEL") ) {
                e = new FormQuestionElement();
                q.getElements().add(e);
                e.setType("label");
                doNext = true;
            }
            else if ( inSet && q != null && t.equals("LEGEND") ) {
                e = new FormQuestionElement();
                q.getElements().add(e);
                e.setType("legend");
                for ( j = 0; j < ts.getTagParameters().size(); j++ ) {
                    n = ts.getTagParameters().get(j).name;
                    v = ts.getTagParameters().get(j).value;
                                        
                    if ( n.equals("class") ) {
                        e.setClassName(TaggedHtml.trimQuotes(v));
                    }
                }
            }
            else if ( inSet && q != null && t.equals("INPUT") ) {
                e = new FormQuestionElement();
                q.getElements().add(e);
                
                for ( j = 0; j < ts.getTagParameters().size(); j++ ) {
                    n = ts.getTagParameters().get(j).name;
                    v = ts.getTagParameters().get(j).value;
                    
                    if ( n.equals("type") ) {
                        e.setType(TaggedHtml.trimQuotes(v));
                    }
                    else if ( n.equals("value") ) {
                        e.setValue(TaggedHtml.trimQuotes(v));
                    }
                    else if ( n.equals("name") ) {
                        e.setName(TaggedHtml.trimQuotes(v));
                    }
                    else if ( n.equals("placeholder") ) {
                        e.setPlaceholder(TaggedHtml.trimQuotes(v));
                    }
                }
                doNext = true;
            }
            else if ( inSet && q != null && t.equals("SELECT") ) {
                e = new FormQuestionElement();
                q.getElements().add(e);
                
                for ( j = 0; j < ts.getTagParameters().size(); j++ ) {
                    n = ts.getTagParameters().get(j).name;
                    v = ts.getTagParameters().get(j).value;
                    
                    if ( n.equals("id") ) {
                        currentSelectGroupId = TaggedHtml.trimQuotes(v);
                    }
                }
            }
            else if ( inSet && q != null && t.equals("OPTION") ) {
                e = new FormQuestionElement();
                q.getElements().add(e);
                
                e.setType("select_option");
                e.setId(currentSelectGroupId);
                for ( j = 0; j < ts.getTagParameters().size(); j++ ) {
                    n = ts.getTagParameters().get(j).name;
                    v = ts.getTagParameters().get(j).value;

                    if ( n.equals("value") ) {
                        e.setValue(TaggedHtml.trimQuotes(v));
                    }
                    else if ( n.equals("label") ) {
                        e.setName(TaggedHtml.trimQuotes(v));
                    }
                }
                doNext = true;
            }
            else if ( inSet && q != null && t.equals("BUTTON") ) {
                e = new FormQuestionElement();
                q.getElements().add(e);
                
                e.setType("submit");
                doNext = true;
            }
            else if ( inSet && q != null && t.equals("TEXTAREA") ) {
                e = new FormQuestionElement();
                q.getElements().add(e);
                e.setType("textarea");
                for ( j = 0; j < ts.getTagParameters().size(); j++ ) {
                    n = ts.getTagParameters().get(j).name;
                    v = ts.getTagParameters().get(j).value;
                    if ( n.equals("name") ) {
                        e.setName(TaggedHtml.trimQuotes(v));
                    }
                }
            }
            else if ( t.equals("/SELECT") ) {
                currentSelectGroupId = null;
            }
            else if ( t.equals("/LABEL") || t.equals("/INPUT") ) {
                e = null;
            }
        }
        
        f.print();
                
        return f;
    }

    private static void exportFormsReport(ArrayList<Form> forms) {
        try {
            File fd;
            FileOutputStream fos;
            
            fd = new File(dataFolder + "/formsReport.csv");
            fos = new FileOutputStream(fd);
            
            int i;
            for ( i = 0; i < forms.size(); i++ ) {
                String str;
                str = "ID_" + VSDK.formatNumberWithinZeroes(i, 4) + ", ";
                str += forms.get(i).exportToReport();
                PersistenceElement.writeAsciiLine(fos, str);
            }
            
            fos.flush();
            
        }
        catch ( Exception e ) {
            System.out.println("ERROR: exportFormsReport failed!");
        }
    }

    private static void exportFormsToMongoDB(
        ArrayList<ProfessionalService> services,
        ArrayList<Form> forms)
    {
        try {
            int i;
            
            System.out.println("=============================================");
            MongoClient mongoClient;

            mongoClient = new MongoClient("www.80mil.com" , 27017);

            DB db = mongoClient.getDB("80mil");
            DBCollection professionalCategory = db.getCollection("professionalCategory");
            DBCollection professionalService = db.getCollection("professionalService");

            for ( i = 0; i < services.size(); i++ ) {
                ProfessionalService p;
                p = services.get(i);
                String c;
                c = p.getCategoryName().replace("&amp;", "&");
                System.out.println("CATEGORY: " + c);
                
                BasicDBObject query;
                query = new BasicDBObject("nameEng", c);
                
                DBObject answer = professionalCategory.findOne(query);
                if ( answer != null && answer.containsField("_id") ) {
                    String idcat = answer.get("_id").toString();
                    System.out.println("  - PROFESSIONAL CATEGORY ID: " + idcat);
                    //---
                    String s;
                    s = p.getServiceName().replace("&amp;", "&");
                    query = new BasicDBObject("nameEng", s);
                    query.append("parentProfessionalCategoryId", idcat);
                    answer = professionalService.findOne(query);
                    if ( answer != null && answer.containsField("_id") ) {
                        String idser = answer.get("_id").toString();
                        System.out.println("    . Professional service: " + idser);
                        updateFormInMongoDB(db, idser, forms.get(i));
                    }
                }
            }
        } 
        catch (UnknownHostException ex) {
            Logger.getLogger(ThumbtackDownloader.class.getName()).log(
                    Level.SEVERE, null, ex);
        }
    }

    private static void updateFormInMongoDB(DB db, String idser, Form f) {
        DBCollection serviceRequestForm;
        serviceRequestForm = db.getCollection("serviceRequestForm");
        BasicDBObject query;
        query = new BasicDBObject("parentProfessionalServiceId", idser);
        DBObject answer;
        answer = serviceRequestForm.findOne(query);

        if ( answer != null ) {
            // Acá se deberían borrar todas las opciones de respuesta que
            // están asociadas al formulario
            serviceRequestForm.remove(query);
        }
        
        f.insertInMongoDB(db, idser);
    }
}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
