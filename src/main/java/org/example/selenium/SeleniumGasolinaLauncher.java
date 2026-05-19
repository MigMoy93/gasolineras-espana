package org.example.selenium;

import org.example.export.GasolineraExporter;
import org.example.model.Gasolinera;
import org.example.parser.GasolineraParser;
import org.example.export.HistoricoManager;

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

// Clase encargada de descargar los datos de gasolineras, procesarlos. Ejecutándose en un entorno Linux (GitHub Actions) para generar el dataset de la app.
// Usa Selenium para obtener el JSON, crea el GeoJSON para la app y guarda un histórico diario.

public class SeleniumGasolinaLauncher {

    // URL del ministerio de donde sacamos los datos (API pública)
    private static final String URL =
            "https://sedeaplicaciones.minetur.gob.es/ServiciosRESTCarburantes/PreciosCarburantes/EstacionesTerrestres/";

    public static void ejecutar() {

        // Mensaje para logs (útil en GitHub Actions)
        System.out.println("Iniciando descarga de datos...");

        // Obtenemos el JSON usando Selenium
        String json = obtenerJson();

        // Si algo falla en la descarga, paramos el programa
        if (json.startsWith("ERROR")) {
            throw new RuntimeException("Error obteniendo JSON: " + json);
        }

        // Convertimos el JSON a objetos Java (Gasolinera)
        List<Gasolinera> lista = GasolineraParser.parsear(json);

        // Para comprobar que realmente hay datos (~11.000 normalmente)
        System.out.println("Gasolineras procesadas: " + lista.size());
        
        // Genera el dataset principal (para el mapa y la app)
        guardarGeoJson(lista);
        
        // Actualiza histórico optimizado (solo cambios de precio)
        HistoricoManager.actualizar(lista);
        
        System.out.println("GeoJSON generado correctamente");
    }

    private static String obtenerJson() {

        // Descarga automáticamente el driver de Chrome compatible
        WebDriverManager.chromedriver().setup();

        // Configuración del navegador
        ChromeOptions options = new ChromeOptions();

        // Ejecuta Chrome sin interfaz (obligatorio en GitHub Actions)
        options.addArguments("--headless=new");

        // Flags necesarios para evitar errores en Linux
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");

        // Desactiva el visor JSON de Chrome: sin esto, --headless=new envuelve
        // la respuesta JSON en HTML de visor (añade texto "Raw data" u otros
        // elementos de UI antes del '{'), lo que rompe el parse de Gson.
        options.addArguments("--disable-features=JSONView");

        // Creamos el navegador
        WebDriver driver = new ChromeDriver(options);

        try {
        
            // Abrimos directamente la URL del ministerio
            driver.get(URL);
        
            // Espera hasta que el body tenga contenido real (máx 15s)
            new WebDriverWait(driver, Duration.ofSeconds(15))
                    .until(d -> !d.findElement(By.tagName("body")).getText().isEmpty());
        
            String bodyText = driver.findElement(By.tagName("body")).getText();

            // Red de seguridad: si quedara algún prefijo de texto antes del JSON,
            // lo descartamos buscando el primer '{'
            int inicio = bodyText.indexOf('{');
            String json = (inicio > 0) ? bodyText.substring(inicio) : bodyText;
        
            return json;
        
        } finally {
        
            // Cerramos el navegador siempre
            driver.quit();
        }
    }

    private static void guardarGeoJson(List<Gasolinera> lista) {

        // Ruta raíz del proyecto (donde se ejecuta el programa)
        String raiz = System.getProperty("user.dir");

        // Archivo principal (el que usará la app Android)
        GasolineraExporter.exportarGeoJson(lista, raiz + "/gasolineras.geojson");

        // Carpeta donde guardamos histórico diario
        String carpeta = raiz + "/historico";

        // Crea la carpeta si no existe
        new File(carpeta).mkdirs();

        // Fecha actual para el nombre del archivo
        String fecha = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        // Guardamos una copia del dataset de hoy
        GasolineraExporter.exportarGeoJson(
                lista,
                carpeta + "/gasolineras_" + fecha + ".geojson"
        );
    }
}
