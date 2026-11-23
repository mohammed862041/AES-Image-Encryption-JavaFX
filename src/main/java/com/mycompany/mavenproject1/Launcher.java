package com.mycompany.mavenproject1; // Declares the root package for this application

// Separate launcher class that does NOT extend Application; needed so JavaFX starts cleanly from a non-modular classpath
public class Launcher {
    public static void main(String[] args) { // JVM entry point used to bootstrap the JavaFX app without module errors
        // Delegates startup and command-line arguments to the real JavaFX Application class
        App.main(args); // أو Mavenproject1.main(args) حسب اسم الكلاس الرئيسي عندك
    }
}