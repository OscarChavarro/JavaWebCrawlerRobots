package webcrawler;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import databaseMongo.ComputrabajoDatabaseConnection;
import databaseMongo.model.Resume;
import databaseMongo.model.ResumeTrans;

public class ComputrabajoCoTransformationRules {
	
	private static final ComputrabajoDatabaseConnection databaseConnection;
	private static DBCollection professionalResume,professionalResumeTrans;
    

    static 
    {
        databaseConnection = new ComputrabajoDatabaseConnection("localhost" , 27017, "domolyRobot", "professionalResume");
        professionalResume = databaseConnection.getProfessionalResume();
        professionalResumeTrans = databaseConnection.createMongoCollection("professionalResumeTrans");
    }
    
    public static String TransformationName(String name)
    {
    	name = name.replaceAll("\\s\\s*"," ");
    	String[] nameAsArray = name.split(" ");
    	String nameTrans="";
    	Pattern pat = Pattern.compile("^[A-Z].*");
        Matcher mat;
        
		 for (int i = 0; i < nameAsArray.length; i++) 
		 {
			 mat = pat.matcher(nameAsArray[i].trim());
		     if (!mat.matches())
		     {
		    	 nameAsArray[i] = nameAsArray[i].toUpperCase().substring(0, 1)+nameAsArray[i].substring(1);
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
    
    public static Date TransformationDate(String date)
    {
    	 Date dateTrans = null;
    	 if(date != null)
    	 {
    		 date=date.replaceAll("\\s\\s*"," ");
	    	 String dateAux=TransformationMonth(date.trim());
	    	 //SimpleDateFormat dateFormat = new SimpleDateFormat("dd 'de' MMMM 'de' yyyy");
	    	 SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
	    	 
	    	 try 
	    	 {
				dateTrans = dateFormat.parse(dateAux);
	    	 } 
	    	 catch (java.text.ParseException e) 
	    	 {
				e.printStackTrace();
	    	 }
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
		        	payAsArray[i]=payAsArray[i].replace(",", "");
		        	pay = Double.parseDouble(payAsArray[i]);
		        	return pay;
		        }
		 }
		return pay;
	}

    public static ResumeTrans TransformationResume(DBCursor cursor)
    {
    	String auxString;
   	 	Double auxDouble;
   	 	int auxInt;
   	 	Date auxDate;
   	 	ResumeTrans resume = new ResumeTrans();
   	 	
	   	 while(cursor.hasNext())
	   	 {
	   		 try
	   		 {
	   			 auxString = TransformationName((String) cursor.next().get("name"));
	   			 resume.setName(auxString);
	   			 
	   			auxDate = TransformationDate((String) cursor.next().get("lastUpdateDate"));
	   			resume.setLastUpdateDate(auxDate);
	   			
	   			auxInt = (int)cursor.next().get("age");
	   			resume.setAge(auxInt);
	   			 
	   			databaseConnection.insertResumeMongo(professionalResumeTrans, resume);
	   			 
	   			 System.out.println("String: "+resume.getName());
	   			 System.out.println("Date: "+resume.getLastUpdateDate()+"\n");
	   		 }
	   		 catch(Exception e)
	   		 {
	   			System.out.println("Ocurrio una excepcion"); 
	   		 }
	   	 }

	   	 return null;
    }
    
    public static void main(String args[])
    {
    	DBCollection professionalResume,professionalResumeTrans;
        professionalResume = databaseConnection.getProfessionalResume();
        professionalResumeTrans = databaseConnection.createMongoCollection("professionalResumeTrans");
        
        if ( professionalResume == null ) 
        {
            return;
        }    
        
        ArrayList<Resume> listResume =  databaseConnection._fetchAllProductsMongo();
        //ArrayList<ResumeTrans> listResumeTrans = new ArrayList<ResumeTrans>();
        ResumeTrans var = new ResumeTrans();
        
        for(int i = 0; i<listResume.size(); i++)
        {
        	var.setName(TransformationName(listResume.get(i).getName()));
        	
        	//Pendiente por transformacion
        	var.setLocation(listResume.get(i).getLocation());
        	//Pendiente por transformacion
        	
        	var.setAge(listResume.get(i).getAge());
        	var.setLastUpdateDate((TransformationDate(listResume.get(i).getLastUpdateDate())));
        	var.setProfilePictureUrl(listResume.get(i).getProfilePictureUrl());
        	var.setJobSearchStatus(listResume.get(i).getJobSearchStatus());
        	var.setWantedPayment(TransformationPayment(listResume.get(i).getWantedPayment()));
        	var.setDescriptionTitle(listResume.get(i).getDescriptionTitle());
        	var.setResumeLink(listResume.get(i).getResumeLink());
        	
        	//Pendiente por transformacion
        	var.setProfesion("");
        	//Pendiente por transformacion
        	
        	var.setRegistrationDate((TransformationDate(listResume.get(i).getRegistrationDate())));
        	var.setLastLoginDate((TransformationDate(listResume.get(i).getLastLoginDate())));
        	var.setPair(listResume.get(i).getPair());
        	var.setSourceUrl(listResume.get(i).getSourceUrl());
        	var.setPhone(listResume.get(i).getPhone());
        	var.setEmail(ValidateEmail(listResume.get(i).getEmail()));
        	//var.setExtractionDate((TransformationDateSimple(listResume.get(i).geti
        	
        	
        	databaseConnection.insertResumeMongo(professionalResumeTrans, var);

        	
        	System.out.println(var.getRegistrationDate());
        	System.out.println(var.getAge());
        }
        
    }

}
