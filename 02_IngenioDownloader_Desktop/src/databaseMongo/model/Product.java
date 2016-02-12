package databaseMongo.model;

public class Product extends JdbcEntity {

    private String name;
    private String description;
    private String material;
    private String measures;
    private String printArea;
    private String brand;
    private String packing;
    private double price;
    private String url;

    public Product() {
        super();
        this.name = "";
        this.description = "";
        this.material = "";
        this.measures = "";
        this.printArea = "";
        this.brand = "";
        this.packing = "";
        this.url = "";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMaterial() {
        return material;
    }

    public void setMaterial(String material) {
        this.material = material;
    }

    public String getMeasures() {
        return measures;
    }

    public void setMeasures(String measures) {
        this.measures = measures;
    }

    public String getPrintArea() {
        return printArea;
    }

    public void setPrintArea(String printArea) {
        this.printArea = printArea;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getPacking() {
        return packing;
    }

    public void setPacking(String packing) {
        this.packing = packing;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}
