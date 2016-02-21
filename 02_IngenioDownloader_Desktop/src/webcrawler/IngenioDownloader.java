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
        /*
        First stage does the following:
          - Builds marPicoCategory collection, which contains categories, 
            subcategories and not showed extra categories.
            Use following query for mongo counting subcategories:
            // db.getCollection('marPicoCategory').find({$and: [{parentCategoryId: {$ne: 0}}, {parentCategoryId: {$exists: true}}]}).sort({parentCategoryId: 1}).count()
          - Insert all category and product links into marPicoElementLink
            collection for further processing at stage 2
        */
        System.out.println("Downloading product indexes:");
        indexProcessor.downloadAllProductIndexes();
        System.out.println("done downloading indexes");

        /*
        Second stage construct marPicoProduct collection:
          - Each product is linked to its respective containing category
        */
        //System.out.println("Downloading marPicoProduct");
        //productProcessor.downloadProductCategoryPages();
        //System.out.println("done downloading marPicoProduct");
    }
}

//==============================================================================
//= EOF                                                                        =
//==============================================================================
