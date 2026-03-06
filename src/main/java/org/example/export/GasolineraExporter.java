package org.example.export;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.example.model.Gasolinera;

import java.io.FileWriter;
import java.util.List;

public class GasolineraExporter {

    public static void exportar(List<Gasolinera> lista, String rutaSalida) {

        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();

        try (FileWriter fw = new FileWriter(rutaSalida)) {
            gson.toJson(lista, fw);
        } catch (Exception e) {
            throw new RuntimeException("Error exportando JSON limpio", e);
        }
    }
}
