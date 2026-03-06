package org.example.selenium;

import org.example.export.GasolineraExporter;
import org.example.model.Gasolinera;
import org.example.parser.GasolineraParser;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class SeleniumGasolinaLauncher {

    private static final String URL =
            "https://sedeaplicaciones.minetur.gob.es/ServiciosRESTCarburantes/PreciosCarburantes/EstacionesTerrestres/";

    public static void ejecutar() {

        String json = obtenerJson();

        List<Gasolinera> lista = GasolineraParser.parsear(json);

        if (json.startsWith("ERROR")) {
            throw new RuntimeException("Error obteniendo JSON: " + json);
        }

        guardarGeoJson(lista);
    }

    private static String obtenerJson() {

        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();

        options.setBinary("/usr/bin/google-chrome");    // ubica a selenium la ubicacion de chrome

        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080"); // Configuracion segura para ejecuciones online

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

        GasolineraExporter.exportarGeoJson(lista, raiz + "/gasolineras.geojson");

        String fecha = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        GasolineraExporter.exportarGeoJson(lista, raiz + "/gasolineras_" + fecha + ".geojson");
    }
}