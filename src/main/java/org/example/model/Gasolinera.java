package org.example.model;

public class Gasolinera {

    public String id;
    public String rotulo;
    public String direccion;
    public String municipio;
    public String provincia;
    public String cp;
    public String horario;
    public String tipoVenta;

    public double lat;
    public double lon;

    public double p95;
    public double p98;
    public double diesel;
    public double dieselPremium;

    public double glp;
    public double gnc;
    public double gnl;

    public Gasolinera(String id, String rotulo, String direccion,
                      String municipio, String provincia, String cp,
                      String horario, String tipoVenta,
                      double lat, double lon,
                      double p95, double p98,
                      double diesel, double dieselPremium,
                      double glp, double gnc, double gnl) {

        this.id = id;
        this.rotulo = rotulo;
        this.direccion = direccion;
        this.municipio = municipio;
        this.provincia = provincia;
        this.cp = cp;
        this.horario = horario;
        this.tipoVenta = tipoVenta;

        this.lat = lat;
        this.lon = lon;

        this.p95 = p95;
        this.p98 = p98;
        this.diesel = diesel;
        this.dieselPremium = dieselPremium;

        this.glp = glp;
        this.gnc = gnc;
        this.gnl = gnl;
    }
}
