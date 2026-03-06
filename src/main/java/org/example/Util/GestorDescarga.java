package org.example.Util;

import java.io.File;
import java.time.LocalDate;

public class GestorDescarga {

    public static File moverYRnombrar(File pdf, String carpetaBase) {

        LocalDate hoy = LocalDate.now();

        File carpetaDestino = new File(
                carpetaBase + "/" + hoy.getYear() + "/" +
                        String.format("%02d", hoy.getMonthValue())
        );

        carpetaDestino.mkdirs();

        File destino = new File(
                carpetaDestino,
                "BOE_" + hoy + ".pdf"
        );


        System.out.println("Moviendo a: " + destino.getAbsolutePath());

        pdf.renameTo(destino);
        return destino;
    }
}
