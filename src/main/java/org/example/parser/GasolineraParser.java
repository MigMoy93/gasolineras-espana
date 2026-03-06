package org.example.parser;

import com.google.gson.*;
import org.example.model.Gasolinera;

import java.util.ArrayList;
import java.util.List;

public class GasolineraParser {

    public static List<Gasolinera> parsear(String jsonTexto) {

        List<Gasolinera> lista = new ArrayList<>();

        try {
            JsonObject raiz = JsonParser
                    .parseString(jsonTexto)
                    .getAsJsonObject();

            JsonArray estaciones = raiz.getAsJsonArray("ListaEESSPrecio");

            for (JsonElement elemento : estaciones) {

                JsonObject obj = elemento.getAsJsonObject();

                // Campos mínimos obligatorios
                if (!existe(obj, "IDEESS")) continue;
                if (!existe(obj, "Latitud")) continue;
                if (!existe(obj, "Longitud")) continue;

                Gasolinera g = new Gasolinera(
                        getTexto(obj, "IDEESS"),
                        getTexto(obj, "Rótulo"),
                        getTexto(obj, "Dirección"),
                        getTexto(obj, "Municipio"),
                        getTexto(obj, "Provincia"),
                        getNumero(obj, "Latitud"),
                        getNumero(obj, "Longitud"),
                        getNumero(obj, "Precio Gasolina 95 E5"),
                        getNumero(obj, "Precio Gasóleo A", "Precio Gasoleo A"),
                        getTexto(obj, "Horario")
                );

                lista.add(g);
            }

        } catch (Exception e) {
            throw new RuntimeException("Error parseando JSON de gasolineras", e);
        }

        return lista;
    }

    // =========================
    // MÉTODOS AUXILIARES
    // =========================

    private static boolean existe(JsonObject obj, String campo) {
        return obj.has(campo)
                && !obj.get(campo).isJsonNull()
                && !obj.get(campo).getAsString().trim().isEmpty();
    }

    private static String getTexto(JsonObject obj, String campo) {
        return existe(obj, campo) ? obj.get(campo).getAsString() : "";
    }

    private static double getNumero(JsonObject obj, String... campos) {
        for (String campo : campos) {
            if (existe(obj, campo)) {
                return convertirNumero(obj.get(campo).getAsString());
            }
        }
        return -1;
    }

    private static double convertirNumero(String texto) {
        return Double.parseDouble(texto.replace(",", "."));
    }
}