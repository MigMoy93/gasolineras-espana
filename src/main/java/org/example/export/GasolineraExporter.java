package org.example.export;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.example.model.Gasolinera;

import java.io.FileWriter;
import java.time.LocalDate;
import java.util.List;

/**
 * Genera un archivo GeoJSON a partir de una lista de gasolineras.
 *
 * - Convierte cada gasolinera en una Feature con geometría (coordenadas)
 *   y propiedades (datos de la estación).
 * - Incluye tanto datos básicos como precios y combustibles disponibles.
 * - El resultado es compatible con mapas (Google Maps, Leaflet, etc.).
 */
public class GasolineraExporter {

    public static void exportarGeoJson(List<Gasolinera> lista, String rutaSalida) {

        JsonObject root = new JsonObject();

        // Tipo de colección GeoJSON + fecha de generación
        root.addProperty("type", "FeatureCollection");
        root.addProperty("fechaActualizacion", LocalDate.now().toString());

        JsonArray features = new JsonArray();

        for (Gasolinera g : lista) {

            JsonObject feature = new JsonObject();

            // Cada gasolinera es una "Feature"
            feature.addProperty("type", "Feature");

            // Geometría: coordenadas [longitud, latitud]
            JsonObject geometry = new JsonObject();
            geometry.addProperty("type", "Point");

            JsonArray coords = new JsonArray();
            coords.add(g.lon);
            coords.add(g.lat);

            geometry.add("coordinates", coords);

            // Propiedades: datos de la gasolinera
            JsonObject properties = new JsonObject();

            // Datos generales
            properties.addProperty("id", g.id);
            properties.addProperty("rotulo", g.rotulo);
            properties.addProperty("direccion", g.direccion);
            properties.addProperty("municipio", g.municipio);
            properties.addProperty("provincia", g.provincia);
            properties.addProperty("cp", g.cp);
            properties.addProperty("horario", g.horario);
            properties.addProperty("tipoVenta", g.tipoVenta);

            // Precios principales
            properties.addProperty("p95", g.p95);
            properties.addProperty("p98", g.p98);
            properties.addProperty("diesel", g.diesel);
            properties.addProperty("dieselPremium", g.dieselPremium);

            // Otros combustibles (pueden venir como -1 si no existen)
            properties.addProperty("glp", g.glp);
            properties.addProperty("gnc", g.gnc);
            properties.addProperty("gnl", g.gnl);

            feature.add("geometry", geometry);
            feature.add("properties", properties);

            features.add(feature);
        }

        root.add("features", features);

        Gson gson = new Gson();

        try (FileWriter fw = new FileWriter(rutaSalida)) {

            // Escritura final del archivo GeoJSON
            gson.toJson(root, fw);

        } catch (Exception e) {

            throw new RuntimeException("Error exportando GeoJSON", e);
        }
    }
}
