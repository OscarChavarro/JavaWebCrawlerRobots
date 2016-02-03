package webcrawler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.http.ParseException;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.DuplicateKeyException;

import databaseMongo.DomiciliosDatabaseConnection;
import databaseMongo.model.Franchise;
import databaseMongo.model.Product;

public class DomicilosDownloader 
{
	private static final DomiciliosDatabaseConnection databaseConnection;
	private static DBCollection franchise,linkFranchise;
	public static BasicDBObject searchQuery = new BasicDBObject();
	public static BasicDBObject searchSubQuery = new BasicDBObject();
	public static BasicDBObject searchLink = new BasicDBObject();

    static 
    {
        databaseConnection = new DomiciliosDatabaseConnection("localhost" , 27017, "domicilosRobot", "franchiseList");
        franchise = databaseConnection.getProperties();
        linkFranchise = databaseConnection.createMongoCollection("linkFranchise");
    }
    

    private static void takeUrl(String url)
    {
    	url = "http://domicilios.com/bogota/pedir"+url;
    	   	
    	String aux;
    	
    	TaggedHtml pageProcessor = new TaggedHtml();
    	TagSegment ts = new TagSegment();
    	
    	pageProcessor.getInternetPage(url);
    	
    	if(pageProcessor.segmentList.size()==0)
    	{
    		return;
    	}
    	
    	for (int i = 0; i < pageProcessor.segmentList.size(); i++ ) 
        {
            ts = pageProcessor.segmentList.get(i);
            if(ts.content.contains("href")&&ts.content.contains("bogota")&&!ts.content.contains("/pedir/")&&!ts.content.contains("/promociones/")&&!ts.content.contains("mailto")&&!ts.content.contains("/contactenos/")&&!ts.content.contains("/buscar"))
            {
            	for(int j=0; j<ts.getTagParameters().size();j++)
            	{
            		if(ts.getTagParameters().get(j).name.contains("href"))
            		{
            			aux = ts.getTagParameters().get(j).value.replaceAll("\"", "");
            			url = "http://domicilios.com"+aux;
            			
            			searchLink.append("_id", url);
            			
            	        try 
        	        	{
            	        	linkFranchise.insert(searchLink);
                	        searchLink.clear();
        	            } 
        	        	catch (DuplicateKeyException e) 
        	        	{
        	                System.out.println("Url already in use: " + url);
        	        	}
            		}
            	}
            }
            
        }	
    }
    
    private static void takeUrlImage(String url,String franchiseName)
    {
        TaggedHtml pageProcessor = new TaggedHtml();
        
        pageProcessor.getInternetPage(url);
        String link=null;
        String title = null;
        int posIn;
        int posEnd;
        
        for(int i = 0; i<pageProcessor.segmentList.size();i++)
        {
        	url = pageProcessor.segmentList.get(i).content;
        	if((url.contains("data-src=")&&url.contains("/timthumb?src")&&url.contains("jpg&")))
        	{
        		link = url;
        	}	
        	if(url.contains("class=\"title")&&link!=null)
			{
        		title = pageProcessor.segmentList.get(i+1).content;
        		posIn=link.indexOf("/timthumb?src");
        		posEnd = link.indexOf("jpg&");
        		try
        		{
        			link = "http://www.domicilios.com"+link.substring(posIn, posEnd+3)+"&q=100&a=c&zc=1&ct=1&w=440&h=217";
        		}
        		catch(Exception e)
        		{
        			e.printStackTrace();
        		}
        		
        		takeImage(franchiseName,title,link);
        		
        		link = null;
			}
        }
        if(title==null)
    		System.out.println("Image not available");
    }

    private static void processUrl()
    {
    	TaggedHtml pageProcessor = new TaggedHtml();
        
    	DBCursor c = linkFranchise.find();
    	DBObject ei;
    	String url;
    	
    	while(c.hasNext())
    	{
    		ei = c.next();
    		url = ei.get("_id").toString();
			pageProcessor.getInternetPage(url);
			buildEntryFromPage(pageProcessor,url);
			pageProcessor = new TaggedHtml();
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
	        String aux;
	        Date importDate = new Date();
	        boolean doCategory = false;
	        boolean doMinimumOrder = false;
	        boolean doHomeCost = false;
	        boolean doCommentsNum = false;
	        boolean doProductName = false;
	        boolean doProductDescription = false;
	        boolean doProductPrice = false;
	        int positiveComm = 0;
	        int negativeComm = 0;
	        String n;
            String v;
            String auxName;
            Product product = new Product();
            ArrayList<String> paymentMethod = new ArrayList<String>();
            Set<BasicDBObject> hs = new HashSet<>();
            

	        f.setUrl(url);
	        for ( i = 0; i < pageProcessor.segmentList.size(); i++ ) 
	        {
	            ts = pageProcessor.segmentList.get(i);
	            
	            if ( !ts.insideTag  )
	            {
	            	
	            	if  ( ts.content.contains("Visita") ) 
					{
						doCategory = true;
	                }
	                else if ( doCategory ) {
	                    f.setCategory(ts.content);
	                    doCategory = false;
	                }
					if  ( ts.content.contains("Pedido m") && ts.content.contains("nimo") ) 
					{
						doMinimumOrder = true;
	                }
	                else if ( doMinimumOrder ) {
	                	
	                	aux = ts.content.trim();
	                    f.setMinimumOrder(aux);
	                    doMinimumOrder = false;
	                }
					if  ( ts.content.contains("Costo Domicilio")) 
					{ 
						doHomeCost = true;
	                }
	                else if ( doHomeCost ) {
	                	if(ts.content.contains("todos de pago"))
	                		f.setHomeCost("N/A");
	                	else
	                		f.setHomeCost(ts.content.trim());
	                    doHomeCost = false;
	                }
					if  ( ts.content.contains("Comentarios")) 
					{ 
						doCommentsNum = true;
	                }
	                else if ( doCommentsNum ) {
	                	f.setCommentsNum(Integer.parseInt(ts.content.trim()));
	                    doCommentsNum = false;
	                }
	            }
	            
	            for ( int j = 0; j < ts.getTagParameters().size(); j++ ) 
	            {
	                n = ts.getTagParameters().get(j).name;
	                v = ts.getTagParameters().get(j).value;
	                
	                if((doProductName)&&(!ts.content.contains("</div>"))&&(!ts.content.contains("<div"))&&(!ts.content.contains("?")))
	                {
	                	product.setTitle(ts.content);
	                	doProductName = false;
	                }
	                if((doProductDescription)&&(!ts.content.contains("</div>"))&&(!ts.content.contains("<div")))
	                {
	                	product.setDescription(ts.content);
	                	doProductDescription = false;
	                }
	                if((doProductPrice)&&(!ts.content.contains("</div>"))&&(!ts.content.contains("<div")))
	                {
	                	product.setPrice(ts.content);
	                	doProductPrice = false;
	                }
	                
	                if(product.getPrice()!=null && product.getDescription()!=null && product.getTitle()!=null)
	                {
	                	searchSubQuery.append("title", product.getTitle());
	                	searchSubQuery.append("description", product.getDescription());
	                	searchSubQuery.append("price", product.getPrice());
	                	
	                	hs.add(searchSubQuery);
	                	searchSubQuery = new BasicDBObject();
	                }
	                
	                if ((n.equals("class")) && (v.contains("title")))
	                {
	                	doProductName = true;
	                }
	                if ((n.equals("class")) && (v.contains("description")))
	                {
	                	doProductDescription = true;
	                }
	                if (n.contains("itemprice"))
	                {
	                	doProductPrice = true;
	                }

	                if ((n.equals("class")) && (v.contains("asset pago")))
	                {
	                	paymentMethod.add(ts.getTagParameters().get(j+1).value.replaceAll("\"", ""));
	                	f.setPaymentMethod(paymentMethod);
	                }
	                
	                if ((n.equals("establecimiento_nombre")))
	                {
	                	aux= ts.getTagParameters().get(j+1).name.replaceAll("\"", "");
	                	aux= aux.replace(";", "");
	                	f.setName(aux);
	                }
	                if ((n.equals("establecimiento_rating")))
	                {
	                	aux= ts.getTagParameters().get(j+1).name.replaceAll("\"", "");
	                	aux= aux.replace(";", "").trim();
	                	if(aux.isEmpty()||aux.equals("")||aux.equals(null))
	                	{
	                		aux="0";
	                	}
	                	f.setRating(aux);
	                }
	                if ((n.equals("es_franquicia")))
	                {
	                	aux= ts.getTagParameters().get(j+1).name.replaceAll("\"", "");
	                	aux= aux.replace(";", "");
	                	f.setIsFranchise(aux);
	                }
	                if ((n.equals("pedidos_online")))
	                {
	                	aux= ts.getTagParameters().get(j+1).name.replaceAll("\"", "");
	                	aux= aux.replace(";", "");
	                	f.setOrderOnline(aux);
	                }
	                
	            }
	            f.setPaymentMethod(paymentMethod);
	        }
	        
	        if(f.getName() != null && f.getName() != "")
	        {	
	            try 
    	        {
	            	positiveComm = numPositveComm(f.getRating(),f.getCommentsNum());
	            	negativeComm = f.getCommentsNum() - positiveComm;
	            	searchQuery.append("_id", f.getUrl());
	            	searchQuery.append("name", f.getName());
	            	searchQuery.append("category", f.getCategory());
	            	searchQuery.append("minimumOrder", f.getMinimumOrder());
	            	searchQuery.append("minimumOrder", f.getMinimumOrder());
    	        	searchQuery.append("homeCost", f.getHomeCost());
    	        	searchQuery.append("numTotalComments",f.getCommentsNum());
    	        	searchQuery.append("numPositiveComments",positiveComm);
    	        	searchQuery.append("numNegativeComments",negativeComm);
    	        	searchQuery.append("rating",f.getRating());
    	        	searchQuery.append("isFranchise",f.getIsFranchise());
    	        	searchQuery.append("orderOnline",f.getOrderOnline());
    	        	searchQuery.append("paymentMethod",f.getPaymentMethod());
    	        	searchQuery.append("productsList", hs);
    	        	searchQuery.append("importDate",importDate);
    	        	
    	        	try 
    	        	{
    	        		franchise.insert(searchQuery);
    	        		auxName = f.getName().replaceAll("([^\\w\\.@-])", "");
    	        		takeUrlImage(f.getUrl(),auxName);
    	        		searchQuery.clear();
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
    
    private static int numPositveComm(String rat, int comment)
    {
    	int positiveComment= (int)(comment * (Double.parseDouble(rat) / 5));
    	
    	return positiveComment;
    }
    
    private static void takeImage(String franchiseName,String nameProduct,String UrlImg)
    {
       	nameProduct=nameProduct.replace(".","");
    	File route = new File("images/"+franchiseName);
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
	    
    	try 
		{
            url = new URL(UrlImg);
            urlCon = url.openConnection();
            is = urlCon.getInputStream();
            fos = new FileOutputStream(route+"/"+nameProduct+".jpg");
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
    
	public static void main(String[] args)
	{
		System.out.println("add to list a link products...\nplease wait");
		takeUrl("/pizza.html");
		takeUrl("/menudeldia.html");
		takeUrl("/alitas.html");
		takeUrl("/arepas-y-empanadas.html");
		takeUrl("/carnes-y-parrilla.html");
		takeUrl("/comida-arabe.html");
		takeUrl("/comida-asiatica.html");
		takeUrl("/comida-china.html");
		takeUrl("/comida-colombiana.html");
		takeUrl("/comida-de-mar.html");
		takeUrl("/comida-internacional.html");
		takeUrl("/comida-italiana.html");
		takeUrl("/comida-mexicana.html");
		takeUrl("/comida-turca.html");
		takeUrl("/crepes-y-wraps.html");
		takeUrl("/desayunos.html");
		takeUrl("/hamburguesas-y-perros-calientes.html");
		takeUrl("/helados.html");
		takeUrl("/licores-y-bebidas.html");
		takeUrl("/mascotas.html");
		takeUrl("/minimercado.html");
		takeUrl("/panaderia-pasteleria-y-helados.html");
		takeUrl("/pollo.html");
		takeUrl("/premium.html");
		takeUrl("/saludable-ensaladas-y-vegetariana.html");
		takeUrl("/sanduches.html");
		takeUrl("/sopas.html");
		takeUrl("/sushi.html");
		takeUrl("/droguerias.html");
		System.out.println("completed");
		
		System.out.println("processing url's...\nplease wait");
		processUrl();
		System.out.println("completed");
	}
}
