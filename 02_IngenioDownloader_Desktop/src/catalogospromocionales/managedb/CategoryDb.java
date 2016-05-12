package catalogospromocionales.managedb;

import catalogospromocionales.model.Category;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import databaseMongo.IngenioDatabaseConnection;

import java.util.ArrayList;

/**
 * Created by gerardo on 11/05/16.
 */
public class CategoryDb {

    private DBCollection categoriasCollection;


    public static final String ID ="id";
    public static final String NAME="name";
    public static final String URL="url";


    public CategoryDb(IngenioDatabaseConnection databaseConnection) {
        categoriasCollection = databaseConnection.createMongoCollection("categorias");
    }


    public DBCollection getCategoriasCollection() {
        return categoriasCollection;
    }



    public Category updateIdCategoriaByUrl(Category category) {
        if (category.getId() == null || category.getId().isEmpty()) return null    ;
        BasicDBObject query = new BasicDBObject(URL, category.getUrl());
        DBObject update = new BasicDBObject();
        update.put("$set", new BasicDBObject(ID, category.getId()));
        return populateCategoria(getCategoriasCollection().findAndModify(query, update));
    }


    public Category findByUrl(String url){
        DBObject searchKey = new BasicDBObject(URL, url);
        DBCursor cursor = getCategoriasCollection().find(searchKey);
        return populateCategoria(cursor);

    }


    public Category findById(String id){
        DBObject searchKey = new BasicDBObject(ID, id);
        DBCursor cursor = getCategoriasCollection().find(searchKey);
        return populateCategoria(cursor);
    }


    public void createCategoria(Category categoria){
        // valida que no exista el url
        if(findByUrl(categoria.getUrl())!=null) return;
        BasicDBObject ca = new BasicDBObject();
        ca.append(NAME, categoria.getName());
        ca.append(URL, categoria.getUrl());
        ca.append(ID,categoria.getId());
        getCategoriasCollection().insert(ca);

    }


    public ArrayList<Category> findAll(){
        ArrayList<Category> list = new ArrayList<>();
        DBCursor allCategorias = getCategoriasCollection().find();
        for(DBObject object:allCategorias.toArray()){
            list.add(populateCategoria(object));
        }
        return list;
    }

    private Category populateCategoria(DBCursor cursor){
        if (cursor.hasNext()) {
            DBObject categoria = cursor.next();
            return new Category((String)categoria.get(ID),(String)categoria.get(URL),(String)categoria.get(NAME));
        }
        return null;
    }

    private Category populateCategoria(DBObject categoria){
        if(categoria!=null)
            return new Category((String)categoria.get(ID),(String)categoria.get(URL),(String)categoria.get(NAME));
        return null;
    }


    /**
     *  algunos test case
     * @param args
     */
    public static void main(String[] args) {


        CategoryDb catalogoManager = new CategoryDb( Connection.getConnection());

        //  test buscar por url categoria encontrda
        System.out.println(catalogoManager.findByUrl("/promocionales/callaway.html"));

        //  test buscar por url categoria no encontrda
        System.out.println(catalogoManager.findByUrl("/www.google.com"));

        // test update categoria
        System.out.println(catalogoManager.updateIdCategoriaByUrl(new Category("123","/promocionales/callaway.html","test cat")));

        //test create categoria
        catalogoManager.createCategoria(new Category("123581","/prueba/test3","prueba create2"));

        //  test buscar por url categoria no encontrda
        System.out.println(catalogoManager.findById("12358"));


        System.out.println("-------lista categorias -------");
        for(Category categoria:catalogoManager.findAll())
        {
            System.out.println(categoria);
        }

    }

}
