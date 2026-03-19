package org.example.parser;

import com.google.gson.*;
import org.example.model.Gasolinera;

import java.util.ArrayList;
import java.util.List;


/*
 * Parsea el JSON obtenido del servicio del Ministerio y lo transforma en una lista de objetos Gasolinera.
 *
 * - Extrae los campos relevantes de cada estación de servicio.
 * - Convierte los valores numéricos teniendo en cuenta el formato (coma/punto).
 * - Gestiona datos faltantes o inválidos asignando -1 como valor por defecto.
 * - Filtra registros sin identificador o sin coordenadas válidas.
 *
 * El resultado es una colección de objetos lista para ser utilizada en la generación de GeoJSON y en el procesamiento del histórico.
 */
public class GasolineraParser {
    
    public static List<Gasolinera> parsear(String jsonTexto) {
    
        List<Gasolinera> lista = new ArrayList<>();
    
        JsonObject raiz = JsonParser.parseString(jsonTexto).getAsJsonObject();
        JsonArray estaciones = raiz.getAsJsonArray("ListaEESSPrecio");
    
        for (JsonElement elemento : estaciones) {
    
            JsonObject obj = elemento.getAsJsonObject();
    
            String id = getTexto(obj, "IDEESS");
            String rotulo = getTexto(obj, "Rótulo");
            String direccion = getTexto(obj, "Dirección");
            String municipio = getTexto(obj, "Municipio");
            String provincia = getTexto(obj, "Provincia");
            String cp = getTexto(obj, "C.P.");
            String horario = getTexto(obj, "Horario");
            String tipoVenta = getTexto(obj, "Tipo Venta");
    
            double lat = getNumero(obj, "Latitud");
            double lon = getNumero(obj, "Longitud (WGS84)", "Longitud");
    
            double p95 = getNumero(obj, "Precio Gasolina 95 E5");
            double p98 = getNumero(obj, "Precio Gasolina 98 E5");
    
            double diesel = getNumero(obj, "Precio Gasóleo A", "Precio Gasoleo A");
            double dieselPremium = getNumero(obj, "Precio Gasóleo Premium");
    
            double glp = getNumero(obj, "Precio Gases licuados del petróleo");
            double gnc = getNumero(obj, "Precio Gas Natural Comprimido");
            double gnl = getNumero(obj, "Precio Gas Natural Licuado");
    
            if (id.isEmpty()) continue;
            if (lat == -1 || lon == -1) continue;
    
            Gasolinera g = new Gasolinera(
                    id,
                    rotulo,
                    direccion,
                    municipio,
                    provincia,
                    cp,
                    horario,
                    tipoVenta,
                    lat,
                    lon,
                    p95,
                    p98,
                    diesel,
                    dieselPremium,
                    glp,
                    gnc,
                    gnl
            );
    
            lista.add(g);
        }
    
        return lista;
    }

    /*
        MEtodos auxiliares para el tratamiento de datos incompletos o mal formateados, asignando valores por defecto y evitar posibles errores en el sistema
    */
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
