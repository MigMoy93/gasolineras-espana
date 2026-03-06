package org.example.model;

public class Gasolinera {

    public String id;
    public String r;
    public String m;
    public double lat;
    public double lon;
    public double p95;
    public double d;

    public Gasolinera(String id, String rotulo, String municipio,
                      double lat, double lon,
                      double p95, double diesel) {

        this.id = id;
        this.r = rotulo;
        this.m = municipio;
        this.lat = lat;
        this.lon = lon;
        this.p95 = p95;
        this.d = diesel;
    }
}