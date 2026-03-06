package org.example.selenium;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.example.Configuracion;
import org.example.Util.EsperarDescarga;
import org.example.Util.GestorDescarga;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class SeleniumLauncher {

    public static void lanzarDescarga() {

        WebDriver driver = null;

        try {
            String carpetaDescargas = Configuracion.CARPETA_DESCARGAS;

            // Preferencias de Chrome
            Map<String, Object> prefs = new HashMap<>();
            prefs.put("download.default_directory", new File(carpetaDescargas).getAbsolutePath());
            prefs.put("download.prompt_for_download", false);
            prefs.put("download.directory_upgrade", true);
            prefs.put("safebrowsing.enabled", true);
            prefs.put("plugins.always_open_pdf_externally", true);

            ChromeOptions options = new ChromeOptions();
            options.setExperimentalOption("prefs", prefs);

            WebDriverManager.chromedriver().setup();
            driver = new ChromeDriver(options);

            // Navegar al PDF
            String urlPdf = org.example.selenium.BoeUtils.obtenerUrlPdfDelDia(driver);
            driver.get(urlPdf);

            // Esperar descarga
            File pdfDescargado = EsperarDescarga.esperarDescarga(
                    carpetaDescargas,
                    Configuracion.TIMEOUT_DESCARGA,
                    ".pdf"
            );

            System.out.println("PDF detectado: " + pdfDescargado.getAbsolutePath());

            // Renombrar y mover
            File pdfFinal = GestorDescarga.moverYRnombrar(
                    pdfDescargado,
                    carpetaDescargas
            );

            System.out.println("Descarga finalizada: " + pdfFinal.getAbsolutePath());

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error durante la automatizacion", e);

        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }
}
