package com.mycompany.mavenproject1; // Declares the root package for this application

import java.io.IOException; // Handles file/resource loading errors when reading the FXML UI
import javafx.application.Application; // Base class required for every JavaFX desktop application
import javafx.fxml.FXMLLoader; // Loads the UI layout defined in an .fxml file
import javafx.scene.Parent; // Base node type for the root container of a JavaFX scene graph
import javafx.scene.Scene; // Holds all visual UI elements displayed inside the window
import javafx.stage.Stage; // Represents the main desktop window (top-level container)

// Main JavaFX application class responsible for launching and showing the UI window
public class App extends Application { // أو App لو عملتله Rename

    // Called automatically by the JavaFX runtime after launch() to build and show the primary window
    @Override
    public void start(Stage stage) throws IOException { // Entry point for setting up the main Stage (window)
        // Locates MainView.fxml on the classpath so FXMLLoader can construct the UI hierarchy and controller
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mycompany/mavenproject1/MainView.fxml"));
        Parent root = loader.load(); // Parses the FXML file, initializes MainController, and returns the root layout node
        
        Scene scene = new Scene(root); // Wraps the loaded root UI layout inside a JavaFX Scene
        stage.setTitle("AES Image Encryption & Decryption"); // Sets the text shown in the window's title bar
        stage.setScene(scene); // Attaches the Scene to the primary application window
        stage.show(); // Displays the window on the user's screen
    }

    public static void main(String[] args) { // Standard Java main method used to start the JavaFX lifecycle
        launch(args); // Starts the JavaFX toolkit and invokes start(Stage) on the JavaFX Application Thread
    }
}