package org.example.model;

/**
 * Clase que representa un registro historico de precios.
 *
 * - Se usa Double en lugar de double para permitir valores null.
 * - Esto evita usar valores ficticios como -1 y mejora las graficas.
 */

public class RegistroHistorico {

    // Atributos, el id no es necesario porque se gestiona al generar el Json de historico
    public String fecha;  // Fecha diaria en formato ISO (yyyy-MM-ddT00:00:00Z)

    public Double p95;  // Gasolina 95 (puede ser null)
    public Double d;    // Diesel (puede ser null)

    public RegistroHistorico(String fecha, Double p95, Double d) {
        this.fecha = fecha;
        this.p95 = p95;
        this.d = d;
    }
}
