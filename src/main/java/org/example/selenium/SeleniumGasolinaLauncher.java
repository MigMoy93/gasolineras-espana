package org.example.selenium;

import org.example.export.GasolineraExporter;
import org.example.model.Gasolinera;
import org.example.parser.GasolineraParser;
import org.example.export.HistoricoManager;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class SeleniumGasolinaLauncher {

    private static final String URL =
            "https://sedeaplicaciones.minetur.gob.es/ServiciosRESTCarburantes/PreciosCarburantes/EstacionesTerrestres/";

    public static void ejecutar() {

        System.out.println("Iniciando descarga de datos...");

        String json = obtenerJson();

        if (json.startsWith("ERROR")) {
            throw new RuntimeException("Error obteniendo JSON: " + json);
        }

        List<Gasolinera> lista = GasolineraParser.parsear(json);

        System.out.println("Gasolineras procesadas: " + lista.size());
        
        // Genera el dataset completo del día (mapa, listado, etc.)
        guardarGeoJson(lista);
        
        // Actualiza el histórico optimizado (solo cambios de precio)
        HistoricoManager.actualizar(lista);
        
        System.out.println("GeoJSON generado correctamente");
    }

    private static String obtenerJson() {

        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();

        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
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

    private static void guardarGeoJson(List<Gasolinera> lista) {

        String raiz = System.getProperty("user.dir");

        // GeoJSON principal
        GasolineraExporter.exportarGeoJson(lista, raiz + "/gasolineras.geojson");

        // Carpeta histórico
        String carpeta = raiz + "/historico";
        new File(carpeta).mkdirs();

        String fecha = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        GasolineraExporter.exportarGeoJson(
                lista,
                carpeta + "/gasolineras_" + fecha + ".geojson"
        );
    }
}
