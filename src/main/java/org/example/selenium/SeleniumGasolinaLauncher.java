package org.example.selenium;

import org.example.export.GasolineraExporter;
import org.example.export.HistoricoManager;
import org.example.model.Gasolinera;
import org.example.parser.GasolineraParser;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

// Clase encargada de descargar los datos de gasolineras con Selenium.
// Se ejecuta en GitHub Actions (Linux headless) para generar el dataset de la app.

public class SeleniumGasolinaLauncher {

    // URL de la API pública del Ministerio de Energía
    private static final String URL =
            "https://sedeaplicaciones.minetur.gob.es/ServiciosRESTCarburantes/PreciosCarburantes/EstacionesTerrestres/";

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

        // Descarga automáticamente el chromedriver compatible con el Chrome instalado
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");          // Sin interfaz gráfica (obligatorio en GitHub Actions)
        options.addArguments("--no-sandbox");             // Necesario en Linux
        options.addArguments("--disable-dev-shm-usage"); // Evita errores de memoria compartida en Linux
        options.addArguments("--disable-gpu");            // Evita errores de renderizado en headless

        WebDriver driver = new ChromeDriver(options);

        try {

            // Selenium navega a la URL de la API del Ministerio
            driver.get(URL);

            // Espera activa (máx. 30s) hasta que el body contenga JSON real.
            // Chrome puede mostrar texto de carga o del visor antes de tener el JSON,
            // por eso comprobamos que el contenido empiece por '{' (inicio de JSON válido).
            new WebDriverWait(driver, Duration.ofSeconds(30))
                    .until(d -> d.findElement(By.tagName("body"))
                                 .getText()
                                 .trim()
                                 .startsWith("{"));

            // Leemos el JSON del body (en Linux headless devuelve el texto puro)
            String json = driver.findElement(By.tagName("body")).getText().trim();

            // Log para verificar en GitHub Actions que el JSON llega correctamente
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
