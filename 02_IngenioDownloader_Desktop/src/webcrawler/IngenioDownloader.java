package webcrawler;

import java.util.ArrayList;
import java.util.Date;

import org.apache.http.ParseException;
import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
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
    
    private static void processUrl(String url)
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
        buildEntryFromPage(pageProcessor,url);
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
	                    System.out.println(p.getName());
	                    System.out.println(p.getDescription());
	                    System.out.println(ts.content);
	                    doMaterial = false;
	                }
	                else if  ( ts.content.contains("MEDIDAS") ) 
					{
	                	doMeasures = true;
	                }
	                else if ( doMeasures ) {
	                    p.setMeasures(ts.content);
	                    System.out.println(ts.content);
	                    doMeasures = false;
	                }
	                else if  ( ts.content.contains("REA IMPRESI") ) 
					{
	                	doPrintArea = true;
	                }
	                else if ( doPrintArea ) {
	                    p.setPrintArea(ts.content);
	                    System.out.println(ts.content);
	                    doPrintArea = false;
	                }
	                else if  ( ts.content.contains("MARCA") ) 
					{
	                	doBrand = true;
	                }
	                else if ( doBrand ) {
	                    p.setBrand(ts.content);
	                    System.out.println(ts.content);
	                    doBrand = false;
	                }
	                else if  ( ts.content.contains("EMPAQUE") ) 
					{
	                	doPacking = true;
	                }
	                else if ( doPacking ) {
	                    p.setPacking(ts.content);
	                    p.setPrice(0.0);
	                    System.out.println(ts.content);
	                    doPacking = false;
	                }
	            }
	            //LLAMAR A FUNCION QUE BUSQUE IMAGENES
	            //LLAMAR A FUNCION QUE PROCESE LOS HREF
	        }
	        if(p.getName() != null && p.getName() != "")
	        {
	        	BasicDBObject searchQuery = new BasicDBObject();
	        	
	            try 
    	        {
    	        	searchQuery.append("name", p.getName());
    	        	searchQuery.append("description", p.getDescription());
    	        	searchQuery.append("material", p.getMaterial());
    	        	searchQuery.append("brand", p.getBrand());
    	        	searchQuery.append("measures", p.getMeasures());
    	        	searchQuery.append("printArea", p.getPrintArea());
    	        	searchQuery.append("price", p.getPrice());
    	        	searchQuery.append("packing", p.getPacking());
    	        	searchQuery.append("url", p.getUrl());
    	        	searchQuery.append("importDate", importDate);
    	            	        	
    	        	if(products.findOne(p.getUrl()) == null)
    	        	{
    	        		products.insert(searchQuery);
    	        	}
    	        } 
    	        catch (ParseException e) 
    	        {
	                e.printStackTrace();
    	        }
	        }
	        
	        FindHref(pageProcessor);
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
	                if(v.contains(".php"))
	                {
	                	if(!listUrl.contains("http://www.mppromocionales.com/"+v))
	                	{
	                		processUrl(v);
	                	}
	                }
	            }
	        }
        }
    }

    
	public static void main(String[] args) 
	{
		String url = "menuproductos.php";
		url = "detallesvar.php?idprod=16664&cat_id=26";
		processUrl(url);
        //FALTA METODO PARA DESCARGAR IMAGENES
		//FALTA METODO PARA INICIAR SESION
		//DESPUES DE INICIAR SESION FALTA TOMAR EL PRECIO DEL PRODUCTO
	}
}
