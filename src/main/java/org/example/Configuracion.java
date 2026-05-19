package org.example;

public class Configuracion {
    // Carpeta base de descargas
    public static final String CARPETA_DESCARGAS =
            System.getProperty("user.dir") + "/descargas";

    // Tiempo máximo de espera para descargas (segundos)
    public static final int TIMEOUT_DESCARGA = 15;

    // URL de prueba (PDF publico)
    public static final String URL_PDF_PRUEBA =
            "https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf";

    private Configuracion() {
        // Evita instancias
    }

}
