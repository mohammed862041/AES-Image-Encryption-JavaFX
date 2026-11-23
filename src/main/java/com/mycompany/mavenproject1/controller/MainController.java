package com.mycompany.mavenproject1.controller; // Declares the controller package connecting the JavaFX UI to backend logic

import com.mycompany.mavenproject1.crypto.AesCipherService; // Performs AES encryption, decryption, and IV generation
import com.mycompany.mavenproject1.crypto.EncryptionMode; // Enum listing supported AES modes (ECB, CBC, CTR)
import com.mycompany.mavenproject1.crypto.KeyManager; // Generates, encodes/decodes, saves, and loads AES keys
import com.mycompany.mavenproject1.image.ImageCryptoProcessor; // Extracts pixels and packs/unpacks encrypted images with metadata
import java.awt.image.BufferedImage; // Holds the actual input/output image pixels in memory for processing and saving
import java.net.URL; // Parameter provided by JavaFX Initializable pointing to the loaded FXML resource
import java.util.ResourceBundle; // Parameter provided by JavaFX Initializable for localization resources (if any)
import javafx.fxml.FXML; // Annotation linking Java fields and event methods to fx:id and onAction entries in MainView.fxml
import javafx.fxml.Initializable; // Interface whose initialize() method runs automatically after FXML controls are injected
import javafx.scene.control.ComboBox; // Dropdown control used to choose the AES EncryptionMode
import javafx.scene.control.Label; // Text label used to display status and error feedback at the bottom of the UI
import javafx.scene.control.TextField; // Editable text input displaying the Base64-encoded AES key
import javafx.scene.image.ImageView; // UI component that renders input and output image previews
import java.io.File; // Represents files chosen by the user in open/save dialogs
import javafx.stage.FileChooser; // Native OS file picker dialog for opening and saving keys and images
import javax.crypto.SecretKey; // Represents the decoded AES secret key passed to AesCipherService
import java.io.ByteArrayInputStream; // Feeds in-memory PNG bytes into JavaFX's Image constructor
import java.io.ByteArrayOutputStream; // Captures PNG bytes written by ImageIO in memory without creating a temp file
import javafx.scene.image.Image; // JavaFX image class displayed inside ImageView components
import javax.imageio.ImageIO; // Reads image files into BufferedImage and writes BufferedImage out as PNG files
import com.mycompany.mavenproject1.model.EncryptedImagePackage; // Holds unpacked dimensions, IV, and ciphertext during decryption

// JavaFX controller for MainView.fxml; handles all button clicks, file dialogs, and encryption/decryption workflows
public class MainController implements Initializable {

    @FXML private ComboBox<EncryptionMode> modeComboBox; // Bound to fx:id="modeComboBox" for choosing ECB, CBC, or CTR
    @FXML private TextField keyTextField; // Bound to fx:id="keyTextField" where the Base64 AES key is shown or pasted
    @FXML private ImageView inputImageView; // Bound to fx:id="inputImageView" to preview the selected input image
    @FXML private ImageView outputImageView; // Bound to fx:id="outputImageView" to preview the encrypted or decrypted result
    @FXML private Label statusLabel; // Bound to fx:id="statusLabel" to show success (green) or error (red) messages

    // ربط طبقات الـ Backend اللي بنيناها في Phase 4
    // Instantiates the backend service classes used to manage keys, run AES, and process image pixels
    private final KeyManager keyManager = new KeyManager(); // Handles key generation, Base64 conversion, and key file I/O
    private final AesCipherService cipherService = new AesCipherService(); // Handles AES encryption, decryption, and IV creation
    private final ImageCryptoProcessor imageProcessor = new ImageCryptoProcessor(); // Handles pixel extraction and metadata packing/unpacking

    private BufferedImage currentInputImage; // Stores the full-resolution input image currently loaded from disk
    private BufferedImage currentOutputImage; // Stores the latest encrypted or decrypted result image so it can be saved

    // Runs automatically right after FXMLLoader injects all @FXML controls, before the window is shown
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // نملى الـ ComboBox بالـ 3 Modes ونختار ECB كافتراضي
        modeComboBox.getItems().setAll(EncryptionMode.values()); // Populates the dropdown with all EncryptionMode enum constants (ECB, CBC, CTR)
        modeComboBox.setValue(EncryptionMode.ECB); // Selects ECB as the default mode so the dropdown is never null on startup
    }

    // Helper that updates the bottom status bar text and switches its color between red (error) and green (success)
    private void updateStatus(String message, boolean isError) {
        statusLabel.setText("Status: " + message); // Displays the formatted status message to the user
        if (isError) { // Applies bold red styling when reporting a failure or validation error
            statusLabel.setStyle("-fx-text-fill: #dc2626; -fx-font-weight: bold;"); // Red text for errors
        } else { // Applies bold green styling when reporting a successful operation
            statusLabel.setStyle("-fx-text-fill: #16a34a; -fx-font-weight: bold;"); // Green text for normal/success feedback
        }
    }
    // Triggered by the "Generate Key" button (onAction="#onGenerateKey" in MainView.fxml)
    @FXML
    private void onGenerateKey() {
        try{ // Catches any crypto/generation failure so the UI stays responsive and shows an error message
            SecretKey key = keyManager.generateKey(); // Generates a new random 128-bit AES secret key
            String base64Key = keyManager.encodeKeyToString(key); // Converts the binary key into Base64 text for display
            keyTextField.setText(base64Key); // Places the new Base64 key into the UI text field
            updateStatus("New 128-bit AES key generated.", false); // Confirms key generation in the status bar
        }catch (Exception e){ // Handles unexpected key generation errors
            updateStatus("Failed to generate key: " + e.getMessage(), true); // Displays the failure reason in red
        }
    }

    // Triggered by the "Save Key" button (onAction="#onSaveKey" in MainView.fxml)
    @FXML
    private void onSaveKey() {
        try{ // Validates the key first and handles any I/O or format errors cleanly
            SecretKey key = keyManager.decodeKeyFromString(keyTextField.getText()); // Parses and validates the key currently in the text field before opening the dialog
            FileChooser fileChooser = new FileChooser(); // Creates a native OS Save File dialog so the user can choose where to store the key
            fileChooser.setTitle("Save AES Key"); // Sets the dialog window title
            // Restricts the dialog view to .key and .txt files so saved keys use clean text extensions
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Key Files (*.key, *.txt)", "*.key", "*.txt"));
            fileChooser.setInitialFileName("aes_secret.key"); // Suggests a default filename so the user doesn't have to type one
            // Opens the modal Save dialog anchored to the current window; returns null if the user clicks Cancel
            File file = fileChooser.showSaveDialog(keyTextField.getScene().getWindow());
            if (file != null) { // Proceeds only if the user selected a file instead of canceling
                keyManager.saveKeyToFile(key, file); // Writes the Base64 key string to the selected file
                updateStatus("Key saved to " + file.getName(), false); // Confirms the saved filename in the status bar
            }
            
        }catch (Exception e){ // Catches invalid key format or file write errors
            updateStatus("Failed to save key: " + e.getMessage(), true); // Displays the error in red
        }
    }

    // Triggered by the "Load Key" button (onAction="#onLoadKey" in MainView.fxml)
    @FXML
    private void onLoadKey() {
        try{ // Handles invalid key file contents or I/O errors
            FileChooser fileChooser = new FileChooser(); // Creates a native OS Open File dialog to pick a saved key file
            fileChooser.setTitle("Load AES Key"); // Sets the dialog window title
            // Filters the file browser to show .key and .txt files by default
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Key Files (*.key, *.txt)", "*.key", "*.txt"));
            // Opens the modal Open dialog anchored to the app window; returns null if canceled
            File file = fileChooser.showOpenDialog(keyTextField.getScene().getWindow());
            if (file != null) { // Proceeds only if the user chose a file
                SecretKey key = keyManager.loadKeyFromFile(file); // Reads, Base64-decodes, and validates the AES key from disk
                keyTextField.setText(keyManager.encodeKeyToString(key)); // Displays the normalized Base64 key in the UI text field
                updateStatus("Key loaded from " + file.getName(), false); // Confirms which key file was loaded
            }
        }catch (Exception e){ // Catches file read errors or corrupted/invalid key files
            updateStatus("Failed to load key: " + e.getMessage(), true); // Displays the failure message in red
        }
    }
    
    // Converts an AWT BufferedImage into a JavaFX Image via an in-memory PNG stream (avoids needing the javafx-swing module)
    private Image convertToFxImage(BufferedImage bufferedImage) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream(); // In-memory buffer to hold encoded PNG bytes
        ImageIO.write(bufferedImage, "png", out); // Encodes the BufferedImage losslessly as PNG bytes into the memory buffer
        return new Image(new ByteArrayInputStream(out.toByteArray())); // Wraps those PNG bytes in a stream so JavaFX Image can decode and render them
    }

    // Triggered by the "Select Image..." button (onAction="#onSelectImage" in MainView.fxml)
    @FXML
    private void onSelectImage() {
        try{ // Handles unsupported formats or I/O errors when loading an image
            FileChooser fileChooser = new FileChooser(); // Creates a native OS Open File dialog to pick an input image
            fileChooser.setTitle("Select Image"); // Sets the dialog window title
            fileChooser.getExtensionFilters().add( // Limits the picker to common lossless and standard raster image formats
                new FileChooser.ExtensionFilter("Image Files (*.png, *.jpg, *.jpeg, *.bmp)", "*.png", "*.jpg", "*.jpeg", "*.bmp")
            );
            // Opens the modal Open dialog anchored to the app window; returns null if canceled
            File file = fileChooser.showOpenDialog(inputImageView.getScene().getWindow());
            if (file != null) { // Proceeds only if the user selected an image file
                BufferedImage loadedImage = ImageIO.read(file); // Reads and decodes the image file into an AWT BufferedImage
                if (loadedImage == null) { // ImageIO.read returns null instead of throwing if the file is not a recognized image
                    throw new IllegalArgumentException("Unsupported or corrupted image file."); // Surfaces a clear error to the user
                }
                currentInputImage = loadedImage; // Stores the loaded image in memory as the active input for encrypt/decrypt
                inputImageView.setImage(convertToFxImage(currentInputImage)); // Displays a preview of the loaded image in the left ImageView

                // نفضي مكان النتيجة القديمة
                // Clears any stale output image so the user doesn't mistake an old result for the newly selected input
                currentOutputImage = null; // Drops reference to the previous output BufferedImage
                outputImageView.setImage(null); // Clears the right-hand preview pane

                // Shows the loaded filename and its pixel dimensions in the status bar
                updateStatus("Loaded image: " + file.getName() + " (" + currentInputImage.getWidth() + "x" + currentInputImage.getHeight() + ")", false);
            }
        }catch (Exception e){ // Catches unreadable, corrupted, or unsupported image files
            updateStatus("Failed to load image: " + e.getMessage(), true); // Displays the error in red
        }
    }

    // Triggered by the "Save Output Image..." button (onAction="#onSaveOutputImage" in MainView.fxml)
    @FXML
    private void onSaveOutputImage() {
        try{ // Ensures an output exists and handles disk write failures
            if (currentOutputImage == null) { // Prevents saving before an encrypt or decrypt operation has produced an output
                throw new IllegalStateException("No output image to save. Please encrypt or decrypt an image first."); // Prompts the user to run an operation first
            }
            FileChooser fileChooser = new FileChooser(); // Creates a native OS Save File dialog for exporting the output image
            fileChooser.setTitle("Save Output Image"); // Sets the dialog window title
            // Enforces PNG because PNG is lossless; lossy formats like JPEG would corrupt ciphertext pixels and the metadata row
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG Image (*.png)", "*.png"));
            fileChooser.setInitialFileName("output_" + modeComboBox.getValue() + ".png"); // Suggests a descriptive default filename including the active AES mode
            // Opens the modal Save dialog anchored to the app window; returns null if canceled
            File file = fileChooser.showSaveDialog(outputImageView.getScene().getWindow());
            if (file != null) { // Proceeds only if the user confirmed a destination path
                ImageIO.write(currentOutputImage, "png", file); // Writes the output BufferedImage losslessly as PNG so decryption stays 100% exact
                updateStatus("Output image saved to " + file.getName(), false); // Confirms the saved filename in the status bar
            }
            
        }catch (Exception e){ // Catches missing output or disk I/O errors
            updateStatus("Failed to save image: " + e.getMessage(), true); // Displays the error in red
        }
    }

    // Triggered by the "Encrypt Image" button (onAction="#onEncryptImage" in MainView.fxml)
    @FXML
    private void onEncryptImage() {
        try{ // Catches missing input, invalid key, image width errors, or cipher exceptions
            if (currentInputImage == null) { // Ensures the user loaded an image before encrypting
                throw new IllegalStateException("Please select an image first."); // Guides the user to select an image
            }
            SecretKey key = keyManager.decodeKeyFromString(keyTextField.getText()); // Decodes and validates the Base64 AES key from the text field
            EncryptionMode mode = modeComboBox.getValue(); // Reads the chosen AES mode (ECB, CBC, or CTR) from the dropdown
            // Generates a fresh random 16-byte IV for CBC/CTR, or passes null for ECB (which doesn't use an IV)
            byte[] iv = mode.isIvRequired() ? cipherService.generateIv() : null;
            byte[] plainPixels = imageProcessor.extractRgbBytes(currentInputImage); // Extracts the raw 3-byte BGR pixel array from the input image
            byte[] ciphertext = cipherService.encrypt(plainPixels, key, mode, iv); // Encrypts the raw pixel bytes with AES in the selected mode
            // Packs the encrypted pixel bytes plus the bottom metadata row (length, IV, padding overflow) into a viewable output image
            currentOutputImage = imageProcessor.packEncryptedImage(
                    currentInputImage.getWidth(), // Passes original image width so pixel rows align properly
                    currentInputImage.getHeight(), // Passes original image height (packEncryptedImage adds +1 row for metadata)
                    ciphertext, // Passes the full encrypted byte array
                    iv // Passes the IV (or null for ECB) so it can be embedded in the metadata row for later decryption
            );
            outputImageView.setImage(convertToFxImage(currentOutputImage)); // Renders the encrypted image preview in the right pane
            updateStatus("Image encrypted successfully using AES-" + mode + ".", false); // Reports successful encryption and mode used
        }catch (Exception e){ // Catches validation, crypto, or packing failures
            updateStatus("Encryption failed: " + e.getMessage(), true); // Displays the exact error in red
        }
    }

    // Triggered by the "Decrypt Image" button (onAction="#onDecryptImage" in MainView.fxml)
    @FXML
    private void onDecryptImage() {
        try{ // Catches missing input, invalid key, missing metadata, or wrong key/mode decryption errors
            if (currentInputImage == null) { // Ensures the user loaded an encrypted image before decrypting
                throw new IllegalStateException("Please select an encrypted image first."); // Guides the user to select an encrypted image
            }
            SecretKey key = keyManager.decodeKeyFromString(keyTextField.getText()); // Decodes and validates the Base64 AES key from the text field
            EncryptionMode mode = modeComboBox.getValue(); // Reads the AES mode (must match the mode used during encryption)
            // Reads the bottom metadata row and extracts original dimensions, stored IV, and full ciphertext (including padding)
            EncryptedImagePackage pkg = imageProcessor.unpackEncryptedImage(currentInputImage);
            // Decrypts the full ciphertext back into raw BGR pixel bytes using the key, selected mode, and unpacked IV
            byte[] decryptedPixels = cipherService.decrypt(pkg.getCiphertext(), key, mode, pkg.getIv());
            // Rebuilds the original plaintext BufferedImage from the decrypted BGR pixel bytes
            currentOutputImage = imageProcessor.rebuildPlainImage(
                    pkg.getOriginalWidth(), // Restores original width from unpacked metadata
                    pkg.getOriginalHeight(), // Restores original height (without the extra metadata row)
                    decryptedPixels // Supplies the decrypted BGR pixel bytes to fill the image
            );
            outputImageView.setImage(convertToFxImage(currentOutputImage)); // Renders the recovered plaintext image in the right preview pane
            updateStatus("Image decrypted successfully using AES-" + mode + ".", false); // Reports successful decryption and mode used
        }catch (Exception e){ // Catches wrong key, wrong mode (e.g. BadPaddingException), or non-encrypted image errors
            updateStatus("Decryption failed: " + e.getMessage(), true); // Displays the decryption failure reason in red
        }
    }
    
    
}