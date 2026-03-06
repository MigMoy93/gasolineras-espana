package org.example.parser;

import com.google.gson.*;
import org.example.model.Gasolinera;

import java.util.ArrayList;
import java.util.List;

public class GasolineraParser {

    public static List<Gasolinera> parsear(String jsonTexto) {

        List<Gasolinera> lista = new ArrayList<>();

        JsonObject raiz = JsonParser.parseString(jsonTexto).getAsJsonObject();

        JsonArray estaciones = raiz.getAsJsonArray("ListaEESSPrecio");

        for (JsonElement elemento : estaciones) {

            JsonObject obj = elemento.getAsJsonObject();

            String id = getTexto(obj, "IDEESS");
            String rotulo = getTexto(obj, "Rótulo");
            String municipio = getTexto(obj, "Municipio");

            double lat = getNumero(obj, "Latitud");
            double lon = getNumero(obj, "Longitud (WGS84)", "Longitud");

            double p95 = getNumero(obj, "Precio Gasolina 95 E5");
            double diesel = getNumero(obj, "Precio Gasóleo A", "Precio Gasoleo A");

            if (id.isEmpty()) continue;
            if (lat == -1 || lon == -1) continue;

            Gasolinera g = new Gasolinera(
                    id,
                    rotulo,
                    municipio,
                    lat,
                    lon,
                    p95,
                    diesel
            );

            lista.add(g);
        }

        return lista;
    }

    private static String getTexto(JsonObject obj, String campo) {

        if (obj.has(campo) && !obj.get(campo).isJsonNull()) {

            return obj.get(campo).getAsString().trim();
        }

        return "";
    }

    private static double getNumero(JsonObject obj, String... campos) {

        for (String campo : campos) {

            if (obj.has(campo) && !obj.get(campo).isJsonNull()) {

                String valor = obj.get(campo).getAsString();

                if (valor.isEmpty()) continue;

                try {
                    return Double.parseDouble(valor.replace(",", "."));
                } catch (Exception ignored) {}
            }
        }

        return -1;
    }
}