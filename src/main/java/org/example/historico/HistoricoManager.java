package org.example.export;

import com.google.gson.*;
import org.example.model.Gasolinera;
import org.example.model.RegistroHistorico;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.time.LocalDate;
import java.util.*;

/**
 * Clase encargada de gestionar el historico de precios de las gasolineras.
 *
 * FUNCIONAMIENTO:
 * - Mantiene un archivo JSON donde cada gasolinera (id) tiene una lista de cambios de precio.
 * - Solo se guarda un nuevo registro cuando el precio cambia, si no, se usara el ultimo precio.
 * - Si el precio viene como -1 (no disponible), se usa el ultimo valor conocido.
 *
 * FUNCIONALIDAD:
 * - Reducir tamaño del historico
 * - Evitar duplicados innecesarios
 * - Facilitar el uso posterior en la app (graficas, consultas, etc.)
 */

public class HistoricoManager {

    private static final String RUTA = "historico/historico.json";

    /**
     * Metodo principal que actualiza el historico con los datos del dia actual
     */
    public static void actualizar(List<Gasolinera> lista) {

        // Cargar historico existente (si no existe, se crea vacio)
        Map<String, List<RegistroHistorico>> historico = cargarHistorico();

        String fechaHoy = LocalDate.now().toString();

        // Recorrer todas las gasolineras del dia actual
        for (Gasolinera g : lista) {

            List<RegistroHistorico> registros = historico.get(g.id);

            // Si no existe la gasolinera en el historico, se crea su lista
            if (registros == null) {
                registros = new ArrayList<>();
                historico.put(g.id, registros);
            }

            RegistroHistorico ultimo = null;

            // Obtener ultimo registro guardado (si existe)
            if (!registros.isEmpty()) {
                ultimo = registros.get(registros.size() - 1);
            }

            double p95 = g.p95;
            double diesel = g.diesel;

            // Gestion de valores -1 (no disponibles)
            // Si hay un valor anterior, se hereda
            if (ultimo != null) {

                if (p95 == -1) {
                    p95 = ultimo.p95;
                }

                if (diesel == -1) {
                    diesel = ultimo.d;
                }
            }

            // Si sigue sin haber datos validos, se ignora la gasolinera
            if (p95 == -1 && diesel == -1) {
                continue;
            }

            // Primera vez que aparece la gasolinera → guardar directamente
            if (ultimo == null) {

                registros.add(new RegistroHistorico(fechaHoy, p95, diesel));
                continue;
            }

            // Comparar si hay cambio de precio
            // Solo se guarda si cambia gasolina o diesel
            if (ultimo.p95 != p95 || ultimo.d != diesel) {

                registros.add(new RegistroHistorico(fechaHoy, p95, diesel));
            }
        }

        // Guardar historico actualizado en archivo
        guardarHistorico(historico);
    }

    /**
     * Carga el historico desde el archivo JSON
     */
    private static Map<String, List<RegistroHistorico>> cargarHistorico() {

        Map<String, List<RegistroHistorico>> historico = new HashMap<>();

        File file = new File(RUTA);

        // Si no existe el archivo, devolver mapa vacio
        if (!file.exists()) {
            return historico;
        }

        try (FileReader reader = new FileReader(file)) {

            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();

            // Recorrer cada gasolinera (id)
            for (String id : json.keySet()) {

                JsonArray array = json.getAsJsonArray(id);

                List<RegistroHistorico> lista = new ArrayList<>();

                // Convertir cada registro a objeto Java
                for (JsonElement e : array) {

                    JsonArray reg = e.getAsJsonArray();

                    String fecha = reg.get(0).getAsString();
                    double p95 = reg.get(1).getAsDouble();
                    double d = reg.get(2).getAsDouble();

                    lista.add(new RegistroHistorico(fecha, p95, d));
                }

                historico.put(id, lista);
            }

        } catch (Exception e) {
            throw new RuntimeException("Error leyendo historico", e);
        }

        return historico;
    }

    /**
     * Guarda el historico en formato JSON
     */
    private static void guardarHistorico(Map<String, List<RegistroHistorico>> historico) {

        JsonObject root = new JsonObject();

        // Recorrer cada gasolinera
        for (String id : historico.keySet()) {

            JsonArray array = new JsonArray();

            List<RegistroHistorico> lista = historico.get(id);

            // Convertir cada registro a JSON (formato compacto)
            for (RegistroHistorico r : lista) {

                JsonArray reg = new JsonArray();
                reg.add(r.fecha);
                reg.add(r.p95);
                reg.add(r.d);

                array.add(reg);
            }

            root.add(id, array);
        }

        try {

            // Crear carpeta si no existe
            new File("historico").mkdirs();

            FileWriter fw = new FileWriter(RUTA);

            new Gson().toJson(root, fw);

            fw.close();

        } catch (Exception e) {
            throw new RuntimeException("Error guardando historico", e);
        }
    }
}
