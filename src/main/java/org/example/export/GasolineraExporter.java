package org.example.export;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.example.model.Gasolinera;

import java.io.FileWriter;
import java.time.LocalDate;
import java.util.List;

public class GasolineraExporter {

    public static void exportarGeoJson(List<Gasolinera> lista, String rutaSalida) {

        JsonObject root = new JsonObject();

        root.addProperty("type", "FeatureCollection");
        root.addProperty("fechaActualizacion", LocalDate.now().toString());

        JsonArray features = new JsonArray();

        for (Gasolinera g : lista) {

            JsonObject feature = new JsonObject();

            feature.addProperty("type", "Feature");

            JsonObject geometry = new JsonObject();
            geometry.addProperty("type", "Point");

            JsonArray coords = new JsonArray();
            coords.add(g.lon);
            coords.add(g.lat);

            geometry.add("coordinates", coords);

            JsonObject properties = new JsonObject();
            properties.addProperty("id", g.id);
            properties.addProperty("r", g.r);
            properties.addProperty("m", g.m);
            properties.addProperty("p95", g.p95);
            properties.addProperty("d", g.d);

            feature.add("geometry", geometry);
            feature.add("properties", properties);

            features.add(feature);
        }

        root.add("features", features);

        Gson gson = new Gson();

        try (FileWriter fw = new FileWriter(rutaSalida)) {

            gson.toJson(root, fw);

        } catch (Exception e) {

            throw new RuntimeException("Error exportando GeoJSON", e);
        }
    }
}