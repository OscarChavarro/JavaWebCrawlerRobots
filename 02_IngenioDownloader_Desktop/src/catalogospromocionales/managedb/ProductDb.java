package catalogospromocionales.managedb;

import catalogospromocionales.model.Product;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import databaseMongo.IngenioDatabaseConnection;

/**
 * Created by gerardo on 11/05/16.
 */
public class ProductDb {



    public static final String ID = "id";
    public static final String NOMBRE = "nombre";
    public static final String PATH = "path";
    public static final String CATEGORIA_ID = "cat_id";

    private DBCollection productosCollection;

    public ProductDb(IngenioDatabaseConnection databaseConnection) {
        productosCollection = databaseConnection.createMongoCollection("productos");
    }

    public DBCollection getProductosCollection() {
        return productosCollection;
    }



    public Product findById(String id){
        DBObject searchObject = new BasicDBObject(ID,id);
        DBObject product = getProductosCollection().findOne(searchObject);
        return populateProducto(product);
    }


    public Product createProduct(Product producto){
        if(findById(producto.getId())!=null) return null;
        BasicDBObject insProducto = new BasicDBObject();
        insProducto.append(ID, producto.getId());
        insProducto.append(CATEGORIA_ID, producto.getCatId());
        insProducto.append(NOMBRE, producto.getNombre());
        insProducto.append(PATH, producto.getPath());
        getProductosCollection().insert(insProducto);
        return producto;
    }


    private Product populateProducto(DBObject dbProducto){
        if(dbProducto==null)  return null;
        return  new Product((String)dbProducto.get(ID),(String)dbProducto.get(NOMBRE),(String)dbProducto.get(PATH),(String)dbProducto.get(CATEGORIA_ID));
    }


    public static void main(String[] args) {

        ProductDb producManager = new ProductDb(Connection.getConnection());

        // test buscar producto
        System.out.println(producManager.findById("123"));

        // test create producto
        producManager.createProduct(new Product("1234","LAPICES TEST","/promo/lapiz/xxx/lapiz","124"));


    }

}
