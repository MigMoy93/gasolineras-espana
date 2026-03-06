package org.example;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.example.ui.VentanaLanzar;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.File;
import java.util.HashMap;
import java.util.Map;


public class Main {

    public static void main(String[] args) throws InterruptedException {

        new VentanaLanzar();

    }
}
