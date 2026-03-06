package org.example.Util;

import java.io.File;

public class EsperarDescarga {


    public static File esperarDescarga(
            String carpeta,
            int timeoutSegundos,
            String ... extensionesValidas ) throws InterruptedException {

        File dir = new File(carpeta);
        int tiempo = 0;

        while (tiempo < timeoutSegundos) {

            File[] archivos = dir.listFiles();
            if (archivos != null) {

                boolean hayTemporal = false;

                for (File f : archivos) {
                    String nombre = f.getName().toLowerCase();

                    // Archivos temporales de Chrome
                    if (nombre.endsWith(".tmp") || nombre.endsWith(".crdownload")) {
                        hayTemporal = true;
                    }
                }

                if (!hayTemporal) {
                    for (File f : archivos) {
                        String nombre = f.getName().toLowerCase();

                        for (String ext : extensionesValidas) {
                            if (nombre.endsWith(ext.toLowerCase())) {
                                return f;
                            }
                        }
                    }
                }
            }

            Thread.sleep(1000);
            tiempo++;
        }

        throw new RuntimeException("La descarga no se completo a tiempo");
    }

}
