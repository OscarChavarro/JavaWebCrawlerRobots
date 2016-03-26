//===========================================================================
package webcrawler;

// Java basic classes
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Apache HTTP classes
import org.apache.http.ParseException;

// Mongo classes
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

// VSDK classes
import vsdk.toolkit.common.VSDK;

// Application specific classes
import databaseMongo.ComputrabajoMongoDatabaseConnection;
import databaseMongo.model.Resume;
import webcrawler.processors.LocationProcessor;
import webcrawler.processors.ProfessionHintFilter;

/**
This program reads records from "professionalResume" Mongo collection and
writes a transformed version to "professionalResumeTrans".

Estimated running time: for 3'117.859 registers: 2h15min
*/
public class Tool03_TransformationFromRawData2CleanData {

    private static final ComputrabajoMongoDatabaseConnection databaseConnection;
    private static final DBCollection professionalResume;
    private static final DBCollection professionalResumeTrans;

    static {
        databaseConnection = new ComputrabajoMongoDatabaseConnection(
            "localhost", 27017, "computrabajoCo", "professionalResume");
        professionalResume = 
            databaseConnection.createMongoCollection("professionalResume");
        professionalResumeTrans = databaseConnection.createMongoCollection(
            "professionalResumeTransformed");
    }

    /**
     * @param name
     * @return
     */
    public static String transformName(String name) {
        name = name.replaceAll("\\s\\s*", " ");
        name = name.toLowerCase();
        String[] nameAsArray = name.split(" ");
        String nameTrans = "";
        Pattern pat = Pattern.compile("^[A-Z]*");
        Matcher mat;

        for (int i = 0; i < nameAsArray.length; i++) {
            mat = pat.matcher(nameAsArray[i].trim());
            if (!mat.matches()) {
                nameAsArray[i] = nameAsArray[i].toUpperCase().substring(0, 1) + 
                    nameAsArray[i].toLowerCase().substring(1);
            }
            nameTrans = nameTrans + nameAsArray[i] + " ";
        }
        nameTrans = nameTrans.trim();

        return nameTrans;
    }

    public static String validateEmail(String email) {
        Pattern pat = Pattern.compile(
            "^[\\w-]+(\\.[\\w-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");
        Matcher mat = pat.matcher(email);

        if (mat.matches()) {
            return email;
        }

        return null;
    }

    public static String transformMonth(String _date) {
        String date;
        String[] dateAux = _date.split(" de ");

        if (dateAux.length > 2) {

            dateAux[1] = dateAux[1].toLowerCase();

            switch (dateAux[1]) {
                case "enero":
                    dateAux[1] = "01";
                    break;
                case "febrero":
                    dateAux[1] = "02";
                    break;
                case "marzo":
                    dateAux[1] = "03";
                    break;
                case "abril":
                    dateAux[1] = "04";
                    break;
                case "mayo":
                    dateAux[1] = "05";
                    break;
                case "junio":
                    dateAux[1] = "06";
                    break;
                case "julio":
                    dateAux[1] = "07";
                    break;
                case "agosto":
                    dateAux[1] = "08";
                    break;
                case "septiembre":
                    dateAux[1] = "09";
                    break;
                case "octubre":
                    dateAux[1] = "10";
                    break;
                case "noviembre":
                    dateAux[1] = "11";
                    break;
                case "diciembre":
                    dateAux[1] = "12";
                    break;
                default:
                    dateAux[1] = "01";
                    break;
            }
        } else {
            return null;
        }

        date = dateAux[0] + "-" + dateAux[1] + "-" + dateAux[2];

        return date;
    }

    public static String transformDate(String date) {
        String dateTrans = null;
        if (date != null) {
            date = date.replaceAll("\\s\\s*", " ");
            String dateAux = transformMonth(date.trim());

            if (dateAux != null) {
                dateTrans = dateAux;
            } else {
                dateTrans = "01-01-1900";
            }

        }
        return dateTrans;
    }

    public static Date transformSimpleDate(String date) {
        Date dateTrans = null;
        if (date != null) {
            date = date.trim();
            SimpleDateFormat dateFormat;
            dateFormat = new SimpleDateFormat("dd-MM-yyyy'T'HH:mm:ss");

            try {
                dateTrans = dateFormat.parse(date);
            } catch (java.text.ParseException e) {
                VSDK.reportMessageWithException(null, VSDK.FATAL_ERROR,
                        "transformationDateSimple", "format error", e);
            }
        }
        return dateTrans;
    }

    public static String transformProfession(String html) {
        String profession;

        String[] stringAux = html.split("</H2></LI>");

        profession = stringAux[0].trim().replace("<UL><LI><H2>", "");

        profession = profession.replaceAll("\\s\\s*", " ");

        if ( profession.length() > 1 ) {
            profession = profession.toUpperCase().substring(0, 1) + 
                profession.toLowerCase().substring(1);
        }

        profession = ProfessionHintFilter.normalizeProfessionHint(profession);
        return profession.trim();
    }

    public static Double transformPayment(String _pay) {
        Pattern pat = Pattern.compile("^[0-9.,]*");
        String[] payAsArray = _pay.split(" ");
        Double pay = 0.0;
        Matcher mat;

        for (int i = 0; i < payAsArray.length; i++) {
            mat = pat.matcher(payAsArray[i]);
            if (mat.matches()) {
                payAsArray[i] = payAsArray[i].replace(".", "");
                payAsArray[i] = payAsArray[i].replace(",", ".");
                try {
                    pay = Double.parseDouble(payAsArray[i]);
                }
                catch ( Exception e ) {
                    pay = 0.0;
                }
                return pay;
            }
        }
        return pay;
    }

    public static void processAllRegisters(DBCursor c) {
        SimpleDateFormat date;
        date = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
        Date transformationDate = new Date();
        Resume r = new Resume();
        DBObject ei;
        BasicDBObject searchQuery = new BasicDBObject();
        int count = 1;
        
        while ( c.hasNext() ) {
            if ( count % 1000 == 1 ) {
                System.out.println("  - Processing registry " + count);
            }
            count++;
            
            ei = c.next();
            if ( !r.importMongoFields(ei) ) {
                System.out.println(
                    "Warning: Error importing object with _id: " + r.get_id());
            }

            try {
                searchQuery.append("_id", r.get_id());
                searchQuery.append("name", transformName(r.getName()));
                searchQuery.append("documentId", r.getDocumentId());
                
                if ( ei.containsField("location") ) {
                    String l;
                    l = LocationProcessor.processLocation(
                        ei, 0, 0, null, false);
                    searchQuery.append("location", l);
                }
                else {
                    searchQuery.append("location", "null");
                }
                
                if ( ei.containsField("htmlContent") ) {
                    searchQuery.append("htmlContent", ei.get("htmlContent"));
                }
                else {
                    searchQuery.append("htmlContent", "null");
                }
                //searchQuery.append("location", r.getLocation());
                
                if ( r.getCountry().equals("co") ) {
                    searchQuery.append("country", "co");
                }
                else {
                    searchQuery.append("country", "ve");
                }
                searchQuery.append("email", validateEmail(r.getEmail()));
                searchQuery.append("emailStatus", r.getEmailStatus());
                
                searchQuery.append("phone", r.getPhone());
                try {
                    searchQuery.append("lastUpdateDate", 
                        date.parse(transformDate(r.getLastUpdateDate())));
                    searchQuery.append("registrationDate", 
                        date.parse(transformDate(r.getRegistrationDate())));
                    searchQuery.append("lastLoginDate", 
                        date.parse(transformDate(r.getLastLoginDate())));
                } 
                catch ( Exception e ) {
                    System.out.println(
                        "Warning: Error importing object with _id:" + 
                        r.get_id());
                }
                searchQuery.append("profilePictureUrl", 
                    r.getProfilePictureUrl());
                searchQuery.append("age", r.getAge());
                searchQuery.append("pair", r.getPair());
                //searchQuery.append("jobSearchStatus", r.getJobSearchStatus());
                searchQuery.append("wantedPayment", 
                    transformPayment(r.getWantedPayment()));
                //searchQuery.append("descriptionTitle", 
                //    r.getDescriptionTitle());
                //searchQuery.append("resumeLink", 
                //    r.getResumeLink());
                searchQuery.append("professionHint", 
                    transformProfession(r.getHtmlContent()));
                searchQuery.append("sourceUrl", r.getSourceUrl());
                searchQuery.append("transformationDate", transformationDate);

                if ( professionalResumeTrans.findOne(r.get_id()) == null ) {
                    professionalResumeTrans.insert(searchQuery);
                }
            } 
            catch ( ParseException e ) {
                System.out.println(
                    "Warning: Error importing object with _id: " + r.get_id());
            }
        }
    }

    public static void main(String args[]) {
        DBCursor c;
        c = professionalResume.find();

        if ( c != null ) {
            processAllRegisters(c);
        }
    }
}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
