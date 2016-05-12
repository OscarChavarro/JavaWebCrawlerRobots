package catalogospromocionales.model;

/**
 * Created by gerardo on 11/05/16.
 */
public class Category {

    private String id;
    private String url;
    private String name;

    public Category() {
    }

    public Category(String id, String url, String name) {
        this.id = id;
        this.url = url;
        this.name = name;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Category{" +
                "id='" + id + '\'' +
                ", url='" + url + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
