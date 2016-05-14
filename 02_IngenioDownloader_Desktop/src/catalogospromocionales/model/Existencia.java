package catalogospromocionales.model;

/**
 * Created by gerardo on 14/05/16.
 */
public class Existencia {

    String color;
    int cantidad;


    public Existencia() {
    }

    public Existencia(String color, int cantidad) {
        this.color = color;
        this.cantidad = cantidad;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    @Override
    public String toString() {
        return "Existencia{" +
                "color='" + color + '\'' +
                ", cantidad=" + cantidad +
                '}';
    }
}
