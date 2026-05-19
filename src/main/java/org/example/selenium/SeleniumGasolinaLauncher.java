package org.example.selenium;

import org.example.export.GasolineraExporter;
import org.example.export.HistoricoManager;
import org.example.model.Gasolinera;
import org.example.parser.GasolineraParser;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.File;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

// Clase encargada de descargar los datos de gasolineras con Selenium.
// Se ejecuta en GitHub Actions (Linux headless) para generar el dataset de la app.
//
// NOTA TÉCNICA: se usa executeAsyncScript (API de Selenium) en lugar de body.getText()
// porque el JSON pesa ~17MB. Chrome renderiza ese volumen en su visor JSON como un árbol
// DOM de miles de nodos, y getText() agota el timeout intentando recorrerlos todos.
// executeAsyncScript hace una petición XHR al mismo origen y devuelve el texto crudo
// directamente, evitando el renderizado del visor y el timeout.

public class SeleniumGasolinaLauncher {

    // URL de la API pública del Ministerio de Energía
    private static final String URL =
            "https://sedeaplicaciones.minetur.gob.es/ServiciosRESTCarburantes/PreciosCarburantes/EstacionesTerrestres/";

    // Timeout para la descarga del JSON (~17MB). 120s es suficiente en GitHub Actions.
    private static final int TIMEOUT_JSON_SEGUNDOS = 120;

    public static void ejecutar() {

        System.out.println("Iniciando descarga de datos...");

        String json = obtenerJson();

        if (json == null || json.startsWith("ERROR")) {
            throw new RuntimeException("Error obteniendo JSON: " + json);
        }

        List<Gasolinera> lista = GasolineraParser.parsear(json);

        System.out.println("Gasolineras procesadas: " + lista.size());

        guardarGeoJson(lista);

        HistoricoManager.actualizar(lista);

        System.out.println("GeoJSON generado correctamente");
    }

    private static String obtenerJson() {

        // WebDriverManager descarga el chromedriver compatible con el Chrome instalado
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");          // Sin interfaz gráfica (obligatorio en GitHub Actions)
        options.addArguments("--no-sandbox");             // Necesario en Linux
        options.addArguments("--disable-dev-shm-usage"); // Evita errores de memoria compartida en Linux
        options.addArguments("--disable-gpu");            // Evita errores de renderizado en headless

        WebDriver driver = new ChromeDriver(options);

        // Tiempo máximo que Selenium espera a que el script XHR devuelva el JSON
        driver.manage().timeouts().scriptTimeout(Duration.ofSeconds(TIMEOUT_JSON_SEGUNDOS));

        try {

            // Paso 1: Selenium navega a la URL para establecer el contexto de origen.
            // Al estar en la misma URL que la API, la petición XHR posterior
            // es "same-origin" y no hay restricciones CORS.
            driver.get(URL);

            // Paso 2: Selenium ejecuta un script XHR asíncrono (JavascriptExecutor es API de Selenium).
            // El script hace una petición HTTP a la misma URL y devuelve el JSON crudo,
            // sin que Chrome lo renderice en su visor (lo que causaba TimeoutException con getText()).
            String json = (String) ((JavascriptExecutor) driver).executeAsyncScript(
                    "var callback = arguments[arguments.length - 1];" +
                    "var xhr = new XMLHttpRequest();" +
                    "xhr.open('GET', location.href);" +
                    "xhr.onload  = function() { callback(xhr.responseText); };" +
                    "xhr.onerror = function() { callback('ERROR: XHR fallido'); };" +
                    "xhr.send();"
            );

            System.out.println("JSON recibido. Primeros 80 chars: " +
                    json.substring(0, Math.min(80, json.length())));

            return json;

        } finally {
            driver.quit(); // Siempre cerramos el navegador
        }
    }

    private static void guardarGeoJson(List<Gasolinera> lista) {

        String raiz = System.getProperty("user.dir");

        // Archivo principal que usa la app Android
        GasolineraExporter.exportarGeoJson(lista, raiz + "/gasolineras.geojson");

        // Copia diaria en la carpeta histórico
        String carpeta = raiz + "/historico";
        new File(carpeta).mkdirs();

        String fecha = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        GasolineraExporter.exportarGeoJson(lista, carpeta + "/gasolineras_" + fecha + ".geojson");
    }
}
