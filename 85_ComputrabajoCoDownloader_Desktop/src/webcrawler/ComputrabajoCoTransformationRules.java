package webcrawler;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.ParseException;

import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.QueryBuilder;

import databaseMongo.ComputrabajoDatabaseConnection;
import databaseMongo.model.Resume;
import databaseMongo.model.ResumeTrans;

public class ComputrabajoCoTransformationRules {
	
	private static final ComputrabajoDatabaseConnection databaseConnection;
	private static DBCollection professionalResume,professionalResumeTrans;
    

    static 
    {
        databaseConnection = new ComputrabajoDatabaseConnection("localhost" , 27017, "computrabajoCo", "professionalResume");
        professionalResume = databaseConnection.getProfessionalResume();
        professionalResumeTrans = databaseConnection.createMongoCollection("professionalResumeTrans");
    }
    
    public static String TransformationName(String name)
    {
    	name = name.replaceAll("\\s\\s*"," ");
    	name = name.toLowerCase();
    	String[] nameAsArray = name.split(" ");
    	String nameTrans="";
    	Pattern pat = Pattern.compile("^[A-Z]*");
        Matcher mat;
        
		 for (int i = 0; i < nameAsArray.length; i++) 
		 {
			 mat = pat.matcher(nameAsArray[i].trim());
		     if (!mat.matches())
		     {
		    	 nameAsArray[i] = nameAsArray[i].toUpperCase().substring(0, 1)+nameAsArray[i].toLowerCase().substring(1);
		     }
		     nameTrans=nameTrans+nameAsArray[i]+" ";
		 }
		 nameTrans = nameTrans.trim();
		
    	return nameTrans;
    }
    
    public static String ValidateEmail(String email)
    {
    	Pattern pat = Pattern.compile("^[\\w-]+(\\.[\\w-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");
        Matcher mat = pat.matcher(email);
   	 	
        if(mat.matches())
        {
        	return email;
        }
    	
    	return null;
    }
    
    public static String TransformationMonth(String _date)
    {
    	String date=null;
    	String[] dateAux = _date.split(" de ");
    	
    	dateAux[1] = dateAux[1].toLowerCase();
    	
    	switch(dateAux[1])
    	{
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
    	
    	date = dateAux[0]+"-"+dateAux[1]+"-"+dateAux[2];
    	
    	return date;
    }
    
    public static String TransformationDate(String date)
    {
    	 String dateTrans = null;
    	 if(date != null)
    	 {
    		 date=date.replaceAll("\\s\\s*"," ");
	    	 String dateAux=TransformationMonth(date.trim());
	    	 //SimpleDateFormat dateFormat = new SimpleDateFormat("dd 'de' MMMM 'de' yyyy");
	    	 SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
	    	 
	    	 dateTrans = dateAux;

    	 }
    	return dateTrans;
    }
    
    public static Date TransformationDateSimple(String date)
    {
    	 Date dateTrans = null;
    	 if (date != null)
    	 {
	    	 date=date.trim();
	    	 //SimpleDateFormat dateFormat = new SimpleDateFormat("dd 'de' MMMM 'de' yyyy");
	    	 SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	    	 
	    	 try 
	    	 {
				dateTrans = dateFormat.parse(date);
	    	 } 
	    	 catch (java.text.ParseException e) 
	    	 {
				e.printStackTrace();
	    	 }
    	 }
    	return dateTrans;
    }
    
    public static String TransformationProfession(String html)
    {
    	String profession = "";
    	
    	
    	String[] stringAux = html.split("</H2></LI>");
    	
    	profession=stringAux[0].trim().replace("<UL><LI><H2>", "");
    	
    	profession= profession.replaceAll("\\s\\s*"," ");
    	
    	profession = profession.toUpperCase().substring(0, 1) + profession.toLowerCase().substring(1);
    	
    	return profession.trim();
    }
    
    public static Double TransformationPayment(String _pay)
    {
		Pattern pat = Pattern.compile("^[0-9.,]*");
		String[] payAsArray = _pay.split(" ");
		Double pay = 0.0;
        Matcher mat;
        
        for (int i = 0; i < payAsArray.length; i++) 
		 {
		        mat = pat.matcher(payAsArray[i]);
		        if (mat.matches())
		        {
		        	payAsArray[i]=payAsArray[i].replace(".", "");
		        	payAsArray[i]=payAsArray[i].replace(",", ".");
		        	pay = Double.parseDouble(payAsArray[i]);
		        	return pay;
		        }
		 }
		return pay;
	}


    
    public static void main(String args[])
    {
    	DBCollection professionalResume,professionalResumeTrans;
        professionalResume = databaseConnection.getProfessionalResume();
        professionalResumeTrans = databaseConnection.createMongoCollection("professionalResumeTrans");
        SimpleDateFormat date = new SimpleDateFormat("dd-MM-yyyy",Locale.ENGLISH);
        Date transformationName = new Date();
        
        if ( professionalResume == null ) 
        {
            return;
        }    
        
        ArrayList<Resume> listResume =  databaseConnection._fetchAllProductsMongo();
        
        BasicDBObject searchQuery = new BasicDBObject();
        for(int i = 0; i<listResume.size(); i++)
        {
	        try 
	        {
	        	searchQuery.append("_id", listResume.get(i).get_id());
	        	searchQuery.append("name", TransformationName(listResume.get(i).getName()));
	        	searchQuery.append("location", listResume.get(i).getLocation());
	        	searchQuery.append("country", listResume.get(i).getCountry());
	        	searchQuery.append("email", ValidateEmail(listResume.get(i).getEmail()));
	        	searchQuery.append("phone", listResume.get(i).getPhone());
	        	searchQuery.append("lastUpdateDate", date.parse(TransformationDate(listResume.get(i).getLastUpdateDate())));
	        	searchQuery.append("profilePictureUrl", listResume.get(i).getProfilePictureUrl());
	        	searchQuery.append("age", listResume.get(i).getAge());
	        	searchQuery.append("pair", listResume.get(i).getPair());
	        	searchQuery.append("jobSearchStatus", listResume.get(i).getJobSearchStatus());
	        	searchQuery.append("wantedPayment", TransformationPayment(listResume.get(i).getWantedPayment()));
	        	searchQuery.append("descriptionTitle", listResume.get(i).getDescriptionTitle());
	        	searchQuery.append("resumeLink", listResume.get(i).getResumeLink());
	        	searchQuery.append("profession", TransformationProfession(listResume.get(i).getHtmlContent()));
	        	searchQuery.append("registrationDate", date.parse(TransformationDate(listResume.get(i).getRegistrationDate())));
	        	searchQuery.append("lastLoginDate", date.parse(TransformationDate(listResume.get(i).getLastLoginDate())));
	        	searchQuery.append("sourceUrl", listResume.get(i).getSourceUrl());
	        	searchQuery.append("transformationDate", transformationName);
	        	
	        	if(professionalResumeTrans.findOne(listResume.get(i).get_id()) == null)
	        	{
	        		professionalResumeTrans.insert(searchQuery);
	        	}
	        } 
	        catch (ParseException e) 
	        {
	                e.printStackTrace();
	        } 
	        catch (java.text.ParseException e) 
	        {
	        	e.printStackTrace();
			}       
        }
    }
}
