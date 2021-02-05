package com.example.ejemplofirebase;

public class Mensaje {

    public String mensaje;
    public String fecha;
    public String nombre;
    public String email;
    public String foto;
    public String meme;

    public Mensaje(String mensaje, String fecha, String nombre, String email, String foto) {
        this.mensaje = mensaje;
        this.fecha = fecha;
        this.nombre = nombre;
        this.email = email;
        this.foto = foto;
    }

    public Mensaje(String mensaje, String fecha, String nombre, String email, String foto, String meme) {
        this.mensaje = mensaje;
        this.fecha = fecha;
        this.nombre = nombre;
        this.email = email;
        this.foto = foto;
        this.meme = meme;
    }
}
