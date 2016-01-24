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
import databaseMongo.IngenioDatabaseConnection;
import databaseMongo.model.Product;

public class IngenioDownloader 
{
	private static final IngenioDatabaseConnection databaseConnection;
	private static DBCollection products;
	public static ArrayList<String> listUrl = new ArrayList();

    static 
    {
        databaseConnection = new IngenioDatabaseConnection("localhost" , 27017, "ingenioRobot", "productList");
        products = databaseConnection.getProperties();
    }
    
    private static void takeUrl(String url)
    {
    	url = "http://www.mppromocionales.com/"+url;
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
	        	BasicDBObject searchQuery = new BasicDBObject();
	        	
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
	                v=v.replaceAll("\"", "");
	                if((v.contains(".jpg")&&v.contains("images/grandes/"))&&(!v.contains("http")))
	                {
	                	if(v.contains("mailto"))
	                	{
	                		break;
	                	}
	                	if(!listUrlImg.contains("http://www.mppromocionales.com/"+v))
	                	{
	                		listUrlImg.add("http://www.mppromocionales.com/"+v);
	                	}
	                }
	            }
	        }
        }
        takeImage(nameProduct,listUrlImg);
        listUrlImg.clear();
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
	                	if(!listUrl.contains("http://www.mppromocionales.com/"+v))
	                	{
	                		takeUrl(v);
	                	}
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
		
//		listUrl.add("http://www.mppromocionales.com/detallesvar.php?idprod=16738&cat_id=27");
//		listUrl.add("http://www.mppromocionales.com/detallesvar.php?idprod=14099&cat_id=185");
//		listUrl.add("http://www.mppromocionales.com/detallesvar.php?idprod=14730&cat_id=185");
//		listUrl.add("http://www.mppromocionales.com/detallesvar.php?idprod=18029&cat_id=185");
//		listUrl.add("http://www.mppromocionales.com/detallesvar.php?idprod=18099&cat_id=185");
//		listUrl.add("http://www.mppromocionales.com/detallesvar.php?idprod=18038&cat_id=185");
//		listUrl.add("http://www.mppromocionales.com/detallesvar.php?idprod=19142&cat_id=185");
//		listUrl.add("http://www.mppromocionales.com/detallesvar.php?idprod=16040&cat_id=185");
//		listUrl.add("http://www.mppromocionales.com/detallesvar.php?idprod=21035&cat_id=185");
//		listUrl.add("http://www.mppromocionales.com/detallesvar.php?idprod=18950&cat_id=185");
//		listUrl.add("http://www.mppromocionales.com/detallesvar.php?idprod=21098&cat_id=94");
//		listUrl.add("http://www.mppromocionales.com/detallesvar.php?idprod=21076&cat_id=94");
//		listUrl.add("http://www.mppromocionales.com/detallesvar.php?idprod=15694&cat_id=94");
//		listUrl.add("http://www.mppromocionales.com/detallesvar.php?idprod=21079&cat_id=94");
//		listUrl.add("http://www.mppromocionales.com/detallesvar.php?idprod=19151&cat_id=94");
//		listUrl.add("http://www.mppromocionales.com/detallesvar.php?idprod=19135&cat_id=93");
//		listUrl.add("http://www.mppromocionales.com/detallesvar.php?idprod=21762&cat_id=93");
//		listUrl.add("http://www.mppromocionales.com/detallesvar.php?idprod=15430&cat_id=93");
//		listUrl.add("http://www.mppromocionales.com/detallesvar.php?idprod=21489&cat_id=91");
//		listUrl.add("http://www.mppromocionales.com/detallesvar.php?idprod=13577&cat_id=91");
//		listUrl.add("http://www.mppromocionales.com/detallesvar.php?idprod=13581&cat_id=91");
//		listUrl.add("http://www.mppromocionales.com/detallesvar.php?idprod=14166&cat_id=91");
//		listUrl.add("http://www.mppromocionales.com/detallesvar.php?idprod=14053&cat_id=91");
//		listUrl.add("http://www.mppromocionales.com/detallesvar.php?idprod=13651&cat_id=90");
//		listUrl.add("http://www.mppromocionales.com/detallesvar.php?idprod=17823&cat_id=90");
//		listUrl.add("http://www.mppromocionales.com/detallesvar.php?idprod=14385&cat_id=90");
//		listUrl.add("http://www.mppromocionales.com/detallesvar.php?idprod=16881&cat_id=90");
//		listUrl.add("http://www.mppromocionales.com/detallesvar.php?idprod=17932&cat_id=90");
//		listUrl.add("http://www.mppromocionales.com/detallesvar.php?idprod=16591&cat_id=90");
//		listUrl.add("http://www.mppromocionales.com/detallesvar.php?idprod=14062&cat_id=88");
//		listUrl.add("http://www.mppromocionales.com/detallesvar.php?idprod=17866&cat_id=88");
//		listUrl.add("http://www.mppromocionales.com/detallesvar.php?idprod=14323&cat_id=88");
//		listUrl.add("http://www.mppromocionales.com/detallesvar.php?idprod=13653&cat_id=88");
//		listUrl.add("http://www.mppromocionales.com/detallesvar.php?idprod=18476&cat_id=203");
//		listUrl.add("http://www.mppromocionales.com/detallesvar.php?idprod=20735&cat_id=203");
//		listUrl.add("http://www.mppromocionales.com/detallesvar.php?idprod=14339&cat_id=203");
//		listUrl.add("http://www.mppromocionales.com/detallesvar.php?idprod=21880&cat_id=202");
//		listUrl.add("http://www.mppromocionales.com/detallesvar.php?idprod=21885&cat_id=202");
//		listUrl.add("http://www.mppromocionales.com/detallesvar.php?idprod=21921&cat_id=202");
//		listUrl.add("http://www.mppromocionales.com/detallesvar.php?idprod=21895&cat_id=202");
//		listUrl.add("http://www.mppromocionales.com/detallesvar.php?idprod=21934&cat_id=202");
//		listUrl.add("http://www.mppromocionales.com/detallesvar.php?idprod=16720&cat_id=201");
//		listUrl.add("http://www.mppromocionales.com/detallesvar.php?idprod=16711&cat_id=201");
//		listUrl.add("http://www.mppromocionales.com/detallesvar.php?idprod=16712&cat_id=201");
//		listUrl.add("http://www.mppromocionales.com/detallesvar.php?idprod=16811&cat_id=201");
//		listUrl.add("http://www.mppromocionales.com/detallesvar.php?idprod=13998&cat_id=201");
//		listUrl.add("http://www.mppromocionales.com/detallesvar.php?idprod=16767&cat_id=201");
//		listUrl.add("http://www.mppromocionales.com/detallesvar.php?idprod=22312&cat_id=200");
//		listUrl.add("http://www.mppromocionales.com/detallesvar.php?idprod=19192&cat_id=200");
//		listUrl.add("http://www.mppromocionales.com/detallesvar.php?idprod=19193&cat_id=200");
//		listUrl.add("http://www.mppromocionales.com/detallesvar.php?idprod=21466&cat_id=200");
//		listUrl.add("http://www.mppromocionales.com/detallesvar.php?idprod=21467&cat_id=200");
//		listUrl.add("http://www.mppromocionales.com/detallesvar.php?idprod=21181&cat_id=200");
//		listUrl.add("http://www.mppromocionales.com/detallesvar.php?idprod=21182&cat_id=200");
//		listUrl.add("http://www.mppromocionales.com/detallesvar.php?idprod=21468&cat_id=200");
//		listUrl.add("http://www.mppromocionales.com/detallesvar.php?idprod=21470&cat_id=200");
//		listUrl.add("http://www.mppromocionales.com/detallesvar.php?idprod=21651&cat_id=200");
//		listUrl.add("http://www.mppromocionales.com/detallesvar.php?idprod=21484&cat_id=200");
//		listUrl.add("http://www.mppromocionales.com/detallesvar.php?idprod=20527&cat_id=200");
//		listUrl.add("http://www.mppromocionales.com/detallesvar.php?idprod=20533&cat_id=200");
//		listUrl.add("http://www.mppromocionales.com/detallesvar.php?idprod=21496&cat_id=200");
		
		System.out.println("processing url's...\nplease wait");
		processUrl();
		System.out.println("completed");
	}
}
