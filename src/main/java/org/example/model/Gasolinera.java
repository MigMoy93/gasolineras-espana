package org.example.model;

public class Gasolinera {

    private String id;
    private String marca;
    private String direccion;
    private String municipio;
    private String provincia;
    private double lat;
    private double lon;
    private double gasolina95;
    private double gasoleoA;
    private String horario;

    public Gasolinera(
            String id,
            String marca,
            String direccion,
            String municipio,
            String provincia,
            double lat,
            double lon,
            double gasolina95,
            double gasoleoA,
            String horario
    ) {
        this.id = id;
        this.marca = marca;
        this.direccion = direccion;
        this.municipio = municipio;
        this.provincia = provincia;
        this.lat = lat;
        this.lon = lon;
        this.gasolina95 = gasolina95;
        this.gasoleoA = gasoleoA;
        this.horario = horario;
    }

    // Getters (solo getters, no hace falta más)
    public String getId() { return id; }
    public String getMarca() { return marca; }
    public String getDireccion() { return direccion; }
    public String getMunicipio() { return municipio; }
    public String getProvincia() { return provincia; }
    public double getLat() { return lat; }
    public double getLon() { return lon; }
    public double getGasolina95() { return gasolina95; }
    public double getGasoleoA() { return gasoleoA; }
    public String getHorario() { return horario; }
}
