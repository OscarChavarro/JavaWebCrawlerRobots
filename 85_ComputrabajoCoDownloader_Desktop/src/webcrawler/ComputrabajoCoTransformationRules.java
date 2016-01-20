package webcrawler;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.ParseException;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import databaseMongo.ComputrabajoDatabaseConnection;
import databaseMongo.model.Resume;
import vsdk.toolkit.common.VSDK;

/**
 * This program reads records from "professionalResume" Mongo collection and
 * writes a transformed version to "professionalResumeTrans".
 */
public class ComputrabajoCoTransformationRules {

    private static final ComputrabajoDatabaseConnection databaseConnection;
    private static DBCollection professionalResume;
    private static DBCollection professionalResumeTrans;

    static {
        databaseConnection = new ComputrabajoDatabaseConnection(
                "localhost", 27017, "computrabajoCo", "professionalResume");
        professionalResume = databaseConnection.getProfessionalResume();
        professionalResumeTrans = databaseConnection.createMongoCollection(
                "professionalResumeTransformed");
    }

    /**
     * @param name
     * @return
     */
    public static String transformationName(String name) {
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

    public static String ValidateEmail(String email) {
        Pattern pat = Pattern.compile(
            "^[\\w-]+(\\.[\\w-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");
        Matcher mat = pat.matcher(email);

        if (mat.matches()) {
            return email;
        }

        return null;
    }

    public static String TransformationMonth(String _date) {
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

    public static String TransformationDate(String date) {
        String dateTrans = null;
        if (date != null) {
            date = date.replaceAll("\\s\\s*", " ");
            String dateAux = TransformationMonth(date.trim());

            if (dateAux != null) {
                dateTrans = dateAux;
            } else {
                dateTrans = "01-01-1900";
            }

        }
        return dateTrans;
    }

    public static Date transformationDateSimple(String date) {
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

    public static String TransformationProfession(String html) {
        String profession = "";

        String[] stringAux = html.split("</H2></LI>");

        profession = stringAux[0].trim().replace("<UL><LI><H2>", "");

        profession = profession.replaceAll("\\s\\s*", " ");

        profession = profession.toUpperCase().substring(0, 1) + 
            profession.toLowerCase().substring(1);

        return profession.trim();
    }

    public static Double TransformationPayment(String _pay) {
        Pattern pat = Pattern.compile("^[0-9.,]*");
        String[] payAsArray = _pay.split(" ");
        Double pay = 0.0;
        Matcher mat;

        for (int i = 0; i < payAsArray.length; i++) {
            mat = pat.matcher(payAsArray[i]);
            if (mat.matches()) {
                payAsArray[i] = payAsArray[i].replace(".", "");
                payAsArray[i] = payAsArray[i].replace(",", ".");
                pay = Double.parseDouble(payAsArray[i]);
                return pay;
            }
        }
        return pay;
    }

    public static void insertMongo(DBCursor c) {
        SimpleDateFormat date;
        date = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
        Date transformationDate = new Date();
        Resume r = new Resume();
        DBObject ei;
        BasicDBObject searchQuery = new BasicDBObject();
        int count = 1;
        
        while ( c.hasNext() ) {
            System.out.println("  - Processing registry " + count);
            count++;
            
            ei = c.next();
            r.importMongoFields(ei);

            try {
                searchQuery.append("_id", r.get_id());
                searchQuery.append("name", transformationName(r.getName()));
                searchQuery.append("documentId", r.getDocumentId());
                searchQuery.append("location", r.getLocation());
                searchQuery.append("country", r.getCountry());
                searchQuery.append("email", ValidateEmail(r.getEmail()));
                searchQuery.append("emailStatus", r.getEmailStatus());
                searchQuery.append("phone", r.getPhone());
                try {
                    searchQuery.append("lastUpdateDate", 
                        date.parse(TransformationDate(r.getLastUpdateDate())));
                } catch (Exception e) {
                    System.out.println(r.get_id());
                }
                searchQuery.append("profilePictureUrl", 
                    r.getProfilePictureUrl());
                searchQuery.append("age", r.getAge());
                searchQuery.append("pair", r.getPair());
                searchQuery.append("jobSearchStatus", r.getJobSearchStatus());
                searchQuery.append("wantedPayment", 
                    TransformationPayment(r.getWantedPayment()));
                searchQuery.append("descriptionTitle", 
                    r.getDescriptionTitle());
                searchQuery.append("resumeLink", 
                    r.getResumeLink());
                searchQuery.append("profession", 
                    TransformationProfession(r.getHtmlContent()));
                searchQuery.append("registrationDate", 
                    date.parse(TransformationDate(r.getRegistrationDate())));
                searchQuery.append("lastLoginDate", 
                    date.parse(TransformationDate(r.getLastLoginDate())));
                searchQuery.append("sourceUrl", r.getSourceUrl());
                searchQuery.append("transformationDate", transformationDate);

                if (professionalResumeTrans.findOne(r.get_id()) == null) {
                    professionalResumeTrans.insert(searchQuery);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            } catch (java.text.ParseException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String args[]) {
        DBCursor c;
        c = professionalResume.find();

        if (c != null) {
            insertMongo(c);
        }
    }
}
