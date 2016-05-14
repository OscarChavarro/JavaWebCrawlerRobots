package catalogospromocionales.managedb;

import catalogospromocionales.parser.ParseProduct;
import catalogospromocionales.model.Existencia;
import catalogospromocionales.model.Product;
import catalogospromocionales.utils.HtmlParser;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import databaseMongo.IngenioDatabaseConnection;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gerardo on 11/05/16.
 */
public class ProductDb {



    public static final String ID = "id";
    public static final String NOMBRE = "nombre";
    public static final String PATH = "path";
    public static final String CATEGORIA_ID = "cat_id";
    public static final String URL_IMAGE = "urlImage";
    public static final String NOMBRE_COMPLETO = "nombreArticulo";
    public static final String REFERENCIA = "referencia";
    public static final String UNIDADES = "unidaes";
    public static final String EMPAQUE = "empaque";
    public static final String DESCRIPCION = "descripcion";

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


    public Product create(Product producto){
        if(findById(producto.getId())!=null) return null;
        BasicDBObject insProducto = new BasicDBObject();
        insProducto.append(ID, producto.getId());
        insProducto.append(CATEGORIA_ID, producto.getCatId());
        insProducto.append(NOMBRE, producto.getNombre());
        insProducto.append(PATH, producto.getPath());
        getProductosCollection().insert(insProducto);
        return producto;
    }

    public Product update(Product producto){


        if (producto.getId() == null) return null    ;
        BasicDBObject query = new BasicDBObject(ID, producto.getId());

        BasicDBObject insProducto = new BasicDBObject();
        insProducto.append(URL_IMAGE, producto.getUrlImage());
        insProducto.append(NOMBRE_COMPLETO, producto.getNombreArticulo());
        insProducto.append(REFERENCIA, producto.getReferencia());
        insProducto.append(UNIDADES, producto.getUnidaes());
        insProducto.append(EMPAQUE, producto.getEmpaque());
        insProducto.append(DESCRIPCION, producto.getDescripcion());


        // arega la existencia del producto
        List<DBObject> array = new ArrayList<DBObject>();
        for(Existencia existencia:producto.getExistenca())
        {
            BasicDBObject existenciaDb = new BasicDBObject();
            existenciaDb.append("color",existencia.getColor());
            existenciaDb.append("cantidad",existencia.getCantidad());
            array.add(existenciaDb);

        }
        insProducto.put("existencia", array);

        DBObject update = new BasicDBObject();
        update.put("$set", insProducto);
        return populateProducto(getProductosCollection().findAndModify(query, update));

    }


    private Product populateProducto(DBObject dbProducto){
        if(dbProducto==null)  return null;
        return  new Product((String)dbProducto.get(ID),(String)dbProducto.get(NOMBRE),(String)dbProducto.get(PATH),(String)dbProducto.get(CATEGORIA_ID));
    }


    public static void main(String[] args) {

        ProductDb producManager = new ProductDb(Connection.getConnection());
        ParseProduct parseProduct = new ParseProduct();
        // test buscar producto
        System.out.println(producManager.findById("123"));

        // test create producto
        Product product = new Product("1731","Silla Plegable con Brazos","/p/silla-plegable-con-brazos/1731/395","395");
        producManager.create(product);

        product = parseProduct.parseProduct(product, HtmlParser.getCookie());
        producManager.update(product);


    }

}
