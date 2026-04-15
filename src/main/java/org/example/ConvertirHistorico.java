package org.example;

import java.io.FileReader;
import java.io.FileWriter;
import java.util.Map;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Clase auxiliar para cambiar el formato del historico.
 *
 * Antes los datos estaban guardados por id y en arrays,
 * ahora se pasan a una lista de objetos mas clara y facil de usar (sobre todo en graficas).
 *
 * Tambien se cambia:
 * - -1 por null
 * - formato de fecha a tipo ISO
 *
 * Esta clase solo se usa una vez para adaptar el historico antiguo.
 */

public class ConvertirHistorico {

    public static void main(String[] args) throws Exception {

        JsonObject antiguo = JsonParser.parseReader(
                new FileReader("historico/historico.json")
        ).getAsJsonObject();

        JsonArray nuevo = new JsonArray();

        for (Map.Entry<String, JsonElement> entry : antiguo.entrySet()) {

            String id = entry.getKey();
            JsonArray registros = entry.getValue().getAsJsonArray();

            for (JsonElement e : registros) {

                JsonArray reg = e.getAsJsonArray();

                String fecha = reg.get(0).getAsString() + "T00:00:00Z";
                double p95 = reg.get(1).getAsDouble();
                double diesel = reg.get(2).getAsDouble();

                JsonObject obj = new JsonObject();
                obj.addProperty("id_gasolinera", id);
                obj.addProperty("fecha", fecha);

                obj.addProperty("precio_gasolina", p95 == -1 ? null : p95);
                obj.addProperty("precio_diesel", diesel == -1 ? null : diesel);

                nuevo.add(obj);
            }
        }

        FileWriter fw = new FileWriter("historico/historico_nuevo.json");
        new GsonBuilder().setPrettyPrinting().create().toJson(nuevo, fw);
        fw.close();

        System.out.println("Historico convertido correctamente");
    }
}