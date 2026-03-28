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

        // Creamos el navegador
        WebDriver driver = new ChromeDriver(options);

        // aumentar timeout para evitar ScriptTimeoutException (Saltó el error en una ocasion)
        driver.manage().timeouts().scriptTimeout(java.time.Duration.ofSeconds(60));

        try {

            // Abrimos una página vacía para poder ejecutar JS
            driver.get("about:blank");

            // Permite ejecutar JavaScript dentro del navegador
            JavascriptExecutor js = (JavascriptExecutor) driver;

            // Ejecutamos fetch dentro del navegador (simula scraping)
            return (String) js.executeAsyncScript(
                    "const callback = arguments[0];" +
                            "fetch('" + URL + "')" +
                            ".then(r => r.text())" +
                            ".then(t => callback(t))" +
                            ".catch(e => callback('ERROR:' + e));"
            );

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
