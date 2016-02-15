//==============================================================================
package webcrawler;

// MongoDB classes
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;

// Application specific classes
import databaseMongo.IngenioDatabaseConnection;

public class IngenioDownloader {
    private static final IngenioDatabaseConnection databaseConnection;
    public static final DBCollection products;
    public static final DBCollection productLink;
    public static final DBCollection category;
    public static BasicDBObject searchQuery = new BasicDBObject();
    public static BasicDBObject searchLink = new BasicDBObject();

    static {
        databaseConnection = new IngenioDatabaseConnection(
            "localhost", 27017, "ingenio", "productList");
        products = databaseConnection.getProperties();
        productLink = databaseConnection.createMongoCollection("productLink");
        category = databaseConnection.createMongoCollection("category");
    }

    public static void main(String[] args) 
    {
        System.out.println("Downloading product indexes:");
        indexProcessor.downloadAllProductIndexes();
        System.out.println("done downloading indexes");

        System.out.println("Downloading products");
        productProcessor.downloadProductCategoryPages();
        System.out.println("done downloading products");
    }
}

//==============================================================================
//= EOF                                                                        =
//==============================================================================
