package org.example.selenium;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.time.LocalDate;

public class BoeUtils {

    public static String obtenerUrlPdfDelDia(WebDriver driver) {

        LocalDate hoy = LocalDate.now();

        String urlDia = String.format(
                "https://www.boe.es/boe/dias/%d/%02d/%02d/",
                hoy.getYear(),
                hoy.getMonthValue(),
                hoy.getDayOfMonth()
        );

        driver.get(urlDia);

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        /*
         * Enlace al PDF del BOE del dia.
         * El BOE usa enlaces claros a .pdf
         */
        WebElement enlacePdf = wait.until(
                ExpectedConditions.presenceOfElementLocated(
                        By.cssSelector("a[href$='.pdf']")
                )
        );

        String urlPdf = enlacePdf.getAttribute("href");

        if (urlPdf == null || !urlPdf.endsWith(".pdf")) {
            throw new RuntimeException("No se pudo localizar el PDF del BOE del dia");
        }

        return urlPdf;
    }
}
