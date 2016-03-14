//==============================================================================
package webcrawler;

// MongoDB classes
import com.mongodb.DBCollection;

// Application specific classes
import databaseMongo.IngenioDatabaseConnection;
import java.util.ArrayList;

public class IngenioDownloader {
    public static void main(String[] args) 
    {
        // Init database connections
        IngenioDatabaseConnection databaseConnection;
        DBCollection marPicoProduct;
        DBCollection marPicoElementLink;
        DBCollection marPicoCategory;

        databaseConnection = new IngenioDatabaseConnection(
            "localhost", 27017, "ingenio", null);
        marPicoProduct = databaseConnection.createMongoCollection("marPicoProduct");
        marPicoElementLink = databaseConnection.createMongoCollection("marPicoElementLink");
        marPicoCategory = databaseConnection.createMongoCollection("marPicoCategory");

        ArrayList<String> cookies;

        cookies = new ArrayList<String>();

        /*
        First stage does the following:
          - Builds marPicoCategory collection, which contains categories, 
            subcategories and not showed extra categories.
            Use following query for mongo counting subcategories:
            // db.getCollection('marPicoCategory').find({$and: [{parentCategoryId: {$ne: 0}}, {parentCategoryId: {$exists: true}}]}).sort({parentCategoryId: 1}).count()
          - Insert all category and product links into marPicoElementLink
            collection for further processing at stage 2
        */
        System.out.println("1. Downloading product indexes:");
        indexProcessor.downloadAllProductIndexes(
            cookies,
            marPicoElementLink, marPicoCategory, true);
        System.out.println("done downloading indexes");

        /*
        Second stage construct marPicoProduct collection:
          - Each product is linked to its respective containing category
        */
        System.out.println("2. Downloading marPicoProduct");
        cookies = new ArrayList<String>();
        productProcessor.downloadProductCategoryPages(
            cookies,
            marPicoElementLink, marPicoProduct);
        System.out.println("done downloading marPicoProduct");
    }
}

//==============================================================================
//= EOF                                                                        =
//==============================================================================
