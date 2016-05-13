package catalogospromocionales.model;

/**
 * Created by gerardo on 11/05/16.
 */
public class Product {


    private String id;
    private String nombre;
    private String path;
    private String catId;


    private String urlImage;
    private String nombreArticulo;
    private String referencia;



    public Product() {
    }

    public Product(String id, String nombre, String path, String catId) {
        this.id = id;
        this.nombre = nombre;
        this.path = path;
        this.catId = catId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getCatId() {
        return catId;
    }

    public void setCatId(String catId) {
        this.catId = catId;
    }

    @Override
    public String toString() {
        return "Product{" +
                "id='" + id + '\'' +
                ", nombre='" + nombre + '\'' +
                ", path='" + path + '\'' +
                ", catId='" + catId + '\'' +
                '}';
    }


    public String getUrlImage() {
        return urlImage;
    }

    public void setUrlImage(String urlImage) {
        this.urlImage = urlImage;
    }

    public String getNombreArticulo() {
        return nombreArticulo;
    }

    public void setNombreArticulo(String nombreArticulo) {
        this.nombreArticulo = nombreArticulo;
    }

    public String getReferencia() {
        return referencia;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }
}
