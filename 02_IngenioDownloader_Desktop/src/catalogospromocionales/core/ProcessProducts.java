package catalogospromocionales.core;

import catalogospromocionales.managedb.CategoryDb;
import catalogospromocionales.managedb.Connection;
import catalogospromocionales.model.Category;
import catalogospromocionales.model.Product;

import java.io.UnsupportedEncodingException;
import java.net.*;
import java.util.ArrayList;

/**
 * Created by sarah on 08/05/16.
 */
public class ProcessProducts {


        public static void main(String[] args) throws URISyntaxException, MalformedURLException, UnsupportedEncodingException {

                ArrayList<Product> products = new  ArrayList<Product>();
                ArrayList<Product> allProducts = new  ArrayList<Product>();
                ParserData categoriaParser = new ParserData();
                CategoryDb catalogoManager = new CategoryDb( Connection.getConnection());

                ArrayList<String> cookies = categoriaParser.getCookie(); // se busca una sola vez el cookie por cestiones de desempeño
                System.out.println("cookie ready");


                //   DESCARGAR LISTADO DE CATEGORIAS y los registra e la base de datos si no estan registrados
                ArrayList<Category> listCategorias = categoriaParser.indexCategories(cookies);
                System.out.println("Numero de categorias:"+listCategorias.size());


                for (Category categoria : listCategorias) {
                        // registra las categorias en la based de datos
                        catalogoManager.createCategoria(categoria);
                        System.out.println(categoria);
                }


                System.out.println("-------- catalogan los productos de la categorias ------------------------------");

                for (Category categoria : listCategorias) {
                        // indexa los productos de la ctegoria
                        products =  categoriaParser.manageProductOfCategory(categoria, cookies);

                        //
                        catalogoManager.updateIdCategoriaByUrl(categoria);
                        System.out.println("Numero de produtos :"+products.size());
                        System.out.println("------------------------------");
                        allProducts.addAll(products);


                }







    }


}
