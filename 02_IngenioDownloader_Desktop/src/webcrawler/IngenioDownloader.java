package webcrawler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;

import org.apache.http.ParseException;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.DuplicateKeyException;
import databaseMongo.IngenioDatabaseConnection;
import databaseMongo.model.Product;

public class IngenioDownloader 
{
	private static final IngenioDatabaseConnection databaseConnection;
	private static DBCollection products,linkProducts;
	public static BasicDBObject searchQuery = new BasicDBObject();
	public static BasicDBObject searchLink = new BasicDBObject();

    static 
    {
        databaseConnection = new IngenioDatabaseConnection("localhost" , 27017, "ingenioRobot", "productList");
        products = databaseConnection.getProperties();
        linkProducts = databaseConnection.createMongoCollection("productLink");
    }
    
    private static void takeUrl(String url)
    {
    	url = "http://www.mppromocionales.com/"+url;
        TaggedHtml pageProcessor;
        
        searchLink.append("_id", url);
        
        if(linkProducts.findOne(searchLink)!=null)
        {
        	return;
        }
        
        linkProducts.insert(searchLink);
        
        searchLink.clear();

        pageProcessor = new TaggedHtml();
        pageProcessor.getInternetPage(url);
        FindHref(pageProcessor);
    }
    
    private static void processUrl()
    {
    	TaggedHtml pageProcessor = new TaggedHtml();
    	
    	DBCursor c = linkProducts.find();
    	DBObject ei;
    	String url;
    	
    	while(c.hasNext())
    	{
    		ei = c.next();
    		url = ei.get("_id").toString();
    		if(url.contains("detallesvar.php"))
    		{
    			pageProcessor.getInternetPage(url);
    			buildEntryFromPage(pageProcessor,url);
    			pageProcessor = new TaggedHtml();
    		}
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
	        Product p = new Product();
	        TagSegment ts;
	        int i;

	        boolean doMaterial = false;
	        boolean doMeasures = false;
	        boolean doPrintArea = false;
	        boolean doBrand = false;
	        boolean doPacking = false;
	        Date importDate = new Date();
	        
	        p.setUrl(url);
	        
	        for ( i = 0; i < pageProcessor.segmentList.size(); i++ ) 
	        {
	            ts = pageProcessor.segmentList.get(i);
	            
	            if ( !ts.insideTag  )
	            {
					if  ( ts.content.contains("MATERIAL") ) 
					{
						doMaterial = true;
	                }
	                else if ( doMaterial ) {
	                    p.setMaterial(ts.content);
	                    p.setName(pageProcessor.segmentList.get(i-13).content.trim());
	                    p.setDescription(pageProcessor.segmentList.get(i-8).content);
	                    doMaterial = false;
	                }
	                else if  ( ts.content.contains("MEDIDAS") ) 
					{
	                	doMeasures = true;
	                }
	                else if ( doMeasures ) {
	                    p.setMeasures(ts.content);
	                    doMeasures = false;
	                }
	                else if  ( ts.content.contains("REA IMPRESI") ) 
					{
	                	doPrintArea = true;
	                }
	                else if ( doPrintArea ) {
	                    p.setPrintArea(ts.content);
	                    doPrintArea = false;
	                }
	                else if  ( ts.content.contains("MARCA") ) 
					{
	                	doBrand = true;
	                }
	                else if ( doBrand ) {
	                    p.setBrand(ts.content);
	                    doBrand = false;
	                }
	                else if  ( ts.content.contains("EMPAQUE") ) 
					{
	                	doPacking = true;
	                }
	                else if ( doPacking ) {
	                    p.setPacking(ts.content);
	                    p.setPrice(0.0);
	                    doPacking = false;
	                }
	            }
	        }
	        
	        if(p.getName() != null && p.getName() != "")
	        {
//	        	BasicDBObject searchQuery = new BasicDBObject();
	        	
	            try 
    	        {
	            	searchQuery.append("_id", p.getUrl());
	            	searchQuery.append("name", p.getName());
    	        	searchQuery.append("description", p.getDescription());
    	        	searchQuery.append("material", p.getMaterial());
    	        	searchQuery.append("brand", p.getBrand());
    	        	searchQuery.append("measures", p.getMeasures());
    	        	searchQuery.append("printArea", p.getPrintArea());
    	        	searchQuery.append("price", p.getPrice());
    	        	searchQuery.append("packing", p.getPacking());
    	        	searchQuery.append("importDate", importDate);
    	        	
    	        	try 
    	        	{
    	        		products.insert(searchQuery);
    	        		searchQuery.clear();
    	        		FindHrefImage(pageProcessor,p.getName());
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
    
    private static void takeImage(String nameProduct,ArrayList<String> listUrlImg) throws IOException
    {
       	nameProduct=nameProduct.replace(".","");
       	nameProduct = nameProduct.trim();
    	File route = new File("images/"+nameProduct);
    	URL url;
    	URLConnection urlCon;
    	InputStream is;
    	FileOutputStream fos;
    	int read;
    	String urlAux;
    	byte[] array = new byte[1000];
    	
    	if(!route.exists())
    	{
    		route.mkdirs();
    	}
	    for(int i = 0; i<listUrlImg.size();i++)
	    {
    		try 
    		{
    			urlAux = listUrlImg.get(i);
	            if(!urlAux.contains("\\s"))
	            {
	    			url = new URL(urlAux);
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
	        } 
    		catch (IOException e) 
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
	                v=v.replaceAll("\"", "");
	                if((v.contains(".jpg")&&v.contains("images/grandes/"))&&(!v.contains("http")))
	                {
	                	if(v.contains("mailto"))
	                	{
	                		break;
	                	}
	                	if(!listUrlImg.contains("http://www.mppromocionales.com/"+v))
	                	{
	                		v=v.replaceAll(" ", "%20");
	                		listUrlImg.add("http://www.mppromocionales.com/"+v);
	                	}
	                }
	            }
	        }
        }
        try 
        {
        	nameProduct = nameProduct.replaceAll("([^\\w\\.@-])", "");
			takeImage(nameProduct,listUrlImg);
		} 
        catch (IOException e) 
        {
			e.printStackTrace();
		}
        
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
	                v=v.replaceAll("/", "");
	                if(v.contains("detallesvar.php")||v.contains("productos.php"))
	                {
	                	if(v.contains("mailto"))
	                	{
	                		break;
	                	}
	                	searchLink.append("_id","http://www.mppromocionales.com/"+v);
	                	if(linkProducts.findOne(searchLink)==null)
	                	{
	                		searchLink.clear();
	                		takeUrl(v);
	                	}
	                	searchLink.clear();
	                }
	            }
	        }
        }
    }

    
	public static void main(String[] args) 
	{
		String url = "menuproductos.php";
		
		System.out.println("add to list a link products...\nplease wait");
		takeUrl(url);
		System.out.println("completed");
		
		System.out.println("processing url's...\nplease wait");
		processUrl();
		System.out.println("completed");
	}
}
