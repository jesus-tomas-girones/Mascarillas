package es.upv.master.android.reconocimientofacial.model;

public class Label {
    private String name;
    private int numero;
    private float x;
    private float y;

    public Label() {
    }

    public Label(String name, int numero, float x, float y) {
        this.name = name;
        this.numero = numero;
        this.x = x;
        this.y = y;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNumero() {
        return numero;
    }

    public void setNumero(int numero) {
        this.numero = numero;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }
}
