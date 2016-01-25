package webcrawler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;

import org.apache.http.ParseException;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DuplicateKeyException;

import databaseMongo.DomiciliosDatabaseConnection;
import databaseMongo.model.Franchise;
import databaseMongo.model.Product;

public class DomicilosDownloader 
{
	private static final DomiciliosDatabaseConnection databaseConnection;
	private static DBCollection franchise;
	public static ArrayList<String> listUrl = new ArrayList<String>();

    static 
    {
        databaseConnection = new DomiciliosDatabaseConnection("localhost" , 27017, "domicilosRobot", "franchiseList");
        franchise = databaseConnection.getProperties();
    }
    
    private static void takeUrl(String url)
    {
    	url = "http://domicilios.com"+url;
        TaggedHtml pageProcessor;
        
        if(listUrl.contains(url))
        {
        	return;
        }
        
        listUrl.add(url);

        pageProcessor = new TaggedHtml();
        pageProcessor.getInternetPage(url);
        FindHref(pageProcessor);
    }
    
    private static void processUrl()
    {
    	TaggedHtml pageProcessor = new TaggedHtml();
        
    	for(int i=0; i<listUrl.size(); i++ )
    	{
    		pageProcessor.getInternetPage(listUrl.get(i));
            buildEntryFromPage(pageProcessor,listUrl.get(i));
    	}
    }

    private static void buildEntryFromPage(TaggedHtml pageProcessor, String url)
    {
        if ( pageProcessor.segmentList == null )
        {
            System.out.println("Warning: empty page");
            
        }
        else
        {
	        Franchise f = new Franchise();
	        TagSegment ts;
	        int i;
	        Date importDate = new Date();

	        
	        for ( i = 0; i < pageProcessor.segmentList.size(); i++ ) 
	        {
	            ts = pageProcessor.segmentList.get(i);
	            
	            if ( !ts.insideTag  )
	            {
					
	            }
	        }
	        
	        if(f.getName() != null && f.getName() != "")
	        {
	        	BasicDBObject searchQuery = new BasicDBObject();
	        	
	            try 
    	        {
	            	searchQuery.append("_id", f.getUrl());
	            	searchQuery.append("name", f.getName());
    	        	searchQuery.append("importDate", importDate);
    	        	
    	        	try 
    	        	{
    	        		franchise.insert(searchQuery);
//    	        		FindHrefImage(pageProcessor,f.getName());
    	            } 
    	        	catch (DuplicateKeyException e) 
    	        	{
    	                System.out.println("Url already in use: " + url);
    	        	}
    	        } 
    	        catch (ParseException e) 
    	        {
	                e.printStackTrace();
    	        }
	        }
        }
    }
    
    private static void takeImage(String nameProduct,ArrayList<String> listUrlImg)
    {
       	nameProduct=nameProduct.replace(".","");
    	File route = new File("images/"+nameProduct);
    	URL url;
    	URLConnection urlCon;
    	InputStream is;
    	FileOutputStream fos;
    	int read;
    	byte[] array = new byte[1000];
    	
    	if(!route.exists())
    	{
    		route.mkdirs();
    	}
	    for(int i = 0; i<listUrlImg.size();i++)
	    {
    		try 
    		{
	            url = new URL(listUrlImg.get(i));
	            urlCon = url.openConnection();
	            is = urlCon.getInputStream();
	            fos = new FileOutputStream(route+"/"+nameProduct+"_"+i+".jpg");
	            read = is.read(array);
	            while (read > 0) 
	            {
	                fos.write(array, 0, read);
	                read = is.read(array);
	            }
	            is.close();
	            fos.close();
	        } 
    		catch (Exception e) 
    		{
	            e.printStackTrace();
	        }	
    	}
    }
    
    public static void FindHrefImage(TaggedHtml pageProcessor, String nameProduct)
    {
    	TagSegment ts;
    	int i,j;
        String n;
        String v;
        ArrayList<String> listUrlImg = new ArrayList<String>();
                
        for ( i = 0; i < pageProcessor.segmentList.size(); i++ ) 
        {
            ts = pageProcessor.segmentList.get(i);
        
	    	for ( j = 0; j < ts.getTagParameters().size(); j++ ) 
	        {
	            n = ts.getTagParameters().get(j).name;
	            v = ts.getTagParameters().get(j).value;
	            if ( n.equals("href") )
	            {
	            	System.out.println(v);
	                v=v.replaceAll("\"", "");
	                if((v.contains(".jpg")&&v.contains("//domicilios.com/img/resources/menus/")))
	                {
	                	if(v.contains("mailto"))
	                	{
	                		break;
	                	}
	                	if(!listUrlImg.contains(v))
	                	{
	                		listUrlImg.add(v);
	                	}
	                }
	            }
	        }
        }
        takeImage(nameProduct,listUrlImg);
    }
    
    public static void FindHref(TaggedHtml pageProcessor)
    {
    	TagSegment ts;
    	int i,j;
        String n;
        String v;
                
        for ( i = 0; i < pageProcessor.segmentList.size(); i++ ) 
        {
            ts = pageProcessor.segmentList.get(i);
        
	    	for ( j = 0; j < ts.getTagParameters().size(); j++ ) 
	        {
	            n = ts.getTagParameters().get(j).name;
	            v = ts.getTagParameters().get(j).value;
	            if ( n.equals("href") )
	            {
	                v=v.replaceAll("\"", "");
	                if(v.contains("/bogota/pedir")&&v.contains(".html"))
	                {
	                	if(v.contains("mailto"))
	                	{
	                		break;
	                	}
	                	if(!listUrl.contains("http://domicilios.com"+v))
	                	{
	                		System.out.println(v);
	                		takeUrl(v);
	                	}
	                }
	            }
	        }
        }
    }

	public static void main(String[] args)
	{
		String url = "/bogota/";
		
		System.out.println("add to list a link products...\nplease wait");
		takeUrl(url);
		System.out.println("completed");
		
//		System.out.println("processing url's...\nplease wait");
//		processUrl();
//		System.out.println("completed");
	}
}
