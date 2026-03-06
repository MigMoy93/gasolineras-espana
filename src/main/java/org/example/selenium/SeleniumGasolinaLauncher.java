package org.example.selenium;

import org.example.Configuracion;
import org.example.export.GasolineraExporter;
import org.example.model.Gasolinera;
import org.example.parser.GasolineraParser;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class SeleniumGasolinaLauncher {

    private static final String URL =
            "https://sedeaplicaciones.minetur.gob.es/ServiciosRESTCarburantes/PreciosCarburantes/EstacionesTerrestres/";

    private static final String SUBCARPETA = "gasolineras";

    public static void ejecutar() {

        // 1️⃣ Obtener JSON crudo (en memoria)
        String json = obtenerJson();

        // 2️⃣ Parsear y limpiar
        List<Gasolinera> lista = GasolineraParser.parsear(json);

        // 3️⃣ Guardar JSON limpio
        guardarJsonLimpio(lista);

        System.out.println("Proceso completado. Gasolineras: " + lista.size());
    }

    // ==============================
    // OBTENER JSON (SIN GUARDAR BASURA)
    // ==============================

    private static String obtenerJson() {

        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--disable-gpu");

        WebDriver driver = new ChromeDriver(options);

        try {
            driver.get("about:blank");

            JavascriptExecutor js = (JavascriptExecutor) driver;

            return (String) js.executeAsyncScript(
                    "const callback = arguments[0];" +
                            "fetch('" + URL + "')" +
                            ".then(r => r.text())" +
                            ".then(t => callback(t))" +
                            ".catch(e => callback('ERROR:' + e));"
            );

        } finally {
            driver.quit();
        }
    }

    // ==============================
    // GUARDAR JSON LIMPIO
    // ==============================

    private static void guardarJsonLimpio(List<Gasolinera> lista) {

        LocalDate hoy = LocalDate.now();

        File carpeta = new File(
                Configuracion.CARPETA_DESCARGAS + "/" +
                        SUBCARPETA + "/clean/" +
                        hoy.getYear() + "/" +
                        String.format("%02d", hoy.getMonthValue()) + "/" +
                        String.format("%02d", hoy.getDayOfMonth())
        );

        carpeta.mkdirs();

        String fechaHora = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

        File archivo = new File(
                carpeta,
                "gasolineras_" + fechaHora + ".json"
        );

        GasolineraExporter.exportar(lista, archivo.getAbsolutePath());

        System.out.println("JSON limpio guardado en: " + archivo.getAbsolutePath());
    }
}
