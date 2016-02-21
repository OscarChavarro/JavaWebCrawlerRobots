//==============================================================================
package webcrawler;

// MongoDB classes
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;

// Application specific classes
import databaseMongo.IngenioDatabaseConnection;

public class IngenioDownloader {
    private static final IngenioDatabaseConnection databaseConnection;
    public static final DBCollection marPicoProduct;
    public static final DBCollection marPicoElementLink;
    public static final DBCollection marPicoCategory;
    public static BasicDBObject searchQuery = new BasicDBObject();

    static {
        databaseConnection = new IngenioDatabaseConnection(
            "localhost", 27017, "ingenio", "marPicoProduct");
        marPicoProduct = databaseConnection.getProperties();
        marPicoElementLink = databaseConnection.createMongoCollection("marPicoElementLink");
        marPicoCategory = databaseConnection.createMongoCollection("marPicoCategory");
    }

    public static void main(String[] args) 
    {
        System.out.println("Downloading product indexes:");
        indexProcessor.downloadAllProductIndexes();
        System.out.println("done downloading indexes");

        System.out.println("Downloading marPicoProduct");
        productProcessor.downloadProductCategoryPages();
        System.out.println("done downloading marPicoProduct");
    }
}

//==============================================================================
//= EOF                                                                        =
//==============================================================================
