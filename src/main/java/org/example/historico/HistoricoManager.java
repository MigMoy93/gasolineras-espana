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
 * - Mantiene un archivo JSON con una lista plana de registros (Op anterior agrupar por id).
 * - Solo se guarda un nuevo registro cuando el precio cambia (si no, coge el anterior).
 * - Si el precio viene como -1 (no disponible), se convierte a null.
 *
 * FUNCIONALIDAD:
 * - Reducir tamaño del historico
 * - Evitar duplicados innecesarios
 * - Facilitar el uso posterior en la app (graficas, consultas, etc.)
 *
 * FORMATO DE SALIDA:
 * [
 *   {
 *     "id_gasolinera": "4970",
 *     "fecha": "2026-03-19T00:00:00Z",
 *     "precio_gasolina": 1.769,
 *     "precio_diesel": 1.899
 *   }
 * ]
 */

public class HistoricoManager {

    private static final String RUTA = "historico/historico.json";

    /**
     * Metodo principal que actualiza el historico con los datos del dia actual
     */
    public static void actualizar(List<Gasolinera> lista) {

        // Cargar historico existente (si no existe, se crea vacio)
        Map<String, List<RegistroHistorico>> historico = cargarHistorico();

        // Fecha en formato ISO fijo (00:00:00Z)
        String fechaHoy = LocalDate.now() + "T00:00:00Z";

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

            // Convertir -1 a null (mejor para graficas)
            Double p95 = (g.p95 == -1) ? null : g.p95;
            Double diesel = (g.diesel == -1) ? null : g.diesel;

            // Gestion de valores null
            // Si hay un valor anterior, se hereda
            if (ultimo != null) {

                if (p95 == null) {
                    p95 = ultimo.p95;
                }

                if (diesel == null) {
                    diesel = ultimo.d;
                }
            }

            // Si sigue sin haber datos validos, se ignora la gasolinera
            if (p95 == null && diesel == null) {
                continue;
            }

            // Primera vez que aparece la gasolinera → guardar directamente
            if (ultimo == null) {

                registros.add(new RegistroHistorico(fechaHoy, p95, diesel));
                continue;
            }

            // Comparar si hay cambio de precio
            // Solo se guarda si cambia gasolina o diesel
            if (!Objects.equals(ultimo.p95, p95) || !Objects.equals(ultimo.d, diesel)) {

                registros.add(new RegistroHistorico(fechaHoy, p95, diesel));
            }
        }

        // Guardar historico actualizado en archivo
        guardarHistorico(historico);
    }

    /**
     * Carga el historico desde el archivo JSON
     * FORMATO: lista plana de objetos
     */
    private static Map<String, List<RegistroHistorico>> cargarHistorico() {

        Map<String, List<RegistroHistorico>> historico = new HashMap<>();

        File file = new File(RUTA);

        // Si no existe el archivo, devolver mapa vacio
        if (!file.exists()) {
            return historico;
        }

        try (FileReader reader = new FileReader(file)) {

            JsonElement elemento = JsonParser.parseReader(reader);

            // Si no es un array, se ignora (formato incorrecto)
            if (!elemento.isJsonArray()) {
                return historico;
            }

            JsonArray array = elemento.getAsJsonArray();

            // Recorrer cada registro
            for (JsonElement e : array) {

                JsonObject obj = e.getAsJsonObject();

                String id = obj.get("id_gasolinera").getAsString();
                String fecha = obj.get("fecha").getAsString();

                // Leer valores, permitiendo null
                Double p95 = obj.get("precio_gasolina").isJsonNull()
                        ? null
                        : obj.get("precio_gasolina").getAsDouble();

                Double diesel = obj.get("precio_diesel").isJsonNull()
                        ? null
                        : obj.get("precio_diesel").getAsDouble();

                List<RegistroHistorico> lista = historico.get(id);

                if (lista == null) {
                    lista = new ArrayList<>();
                    historico.put(id, lista);
                }

                lista.add(new RegistroHistorico(fecha, p95, diesel));
            }

        } catch (Exception e) {
            throw new RuntimeException("Error leyendo historico", e);
        }

        return historico;
    }

    /**
     * Guarda el historico en formato JSON
     * lista plana de objetos (uno por registro)
     */
    private static void guardarHistorico(Map<String, List<RegistroHistorico>> historico) {

        JsonArray root = new JsonArray();

        // Recorrer cada gasolinera
        for (String id : historico.keySet()) {

            List<RegistroHistorico> lista = historico.get(id);

            // Convertir cada registro a JSON
            for (RegistroHistorico r : lista) {

                JsonObject obj = new JsonObject();

                obj.addProperty("id_gasolinera", id);
                obj.addProperty("fecha", r.fecha);

                // Si es null → JSON null (no numero falso)
                if (r.p95 != null) {
                    obj.addProperty("precio_gasolina", r.p95);
                } else {
                    obj.add("precio_gasolina", JsonNull.INSTANCE);
                }

                if (r.d != null) {
                    obj.addProperty("precio_diesel", r.d);
                } else {
                    obj.add("precio_diesel", JsonNull.INSTANCE);
                }

                root.add(obj);
            }
        }

        try {

            // Crear carpeta si no existe
            new File("historico").mkdirs();

            FileWriter fw = new FileWriter(RUTA);

            // Pretty para que se lea bien, da formato extendido
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(root, fw);

            fw.close();

        } catch (Exception e) {
            throw new RuntimeException("Error guardando historico", e);
        }
    }
}
