package org.example.model;

public class RegistroHistorico {

    // Atributos, el id no es necesario porque se gestiona al generar el Json de historico
    public String fecha;  // Fecha diaria
    public double p95;  // Gasolina 95
    public double d;  // Diesel

    public RegistroHistorico(String fecha, double p95, double d) {
        this.fecha = fecha;
        this.p95 = p95;
        this.d = d;
    }
}
