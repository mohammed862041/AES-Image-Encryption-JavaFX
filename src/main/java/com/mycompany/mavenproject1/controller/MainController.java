package com.mycompany.mavenproject1.controller;

import com.mycompany.mavenproject1.crypto.AesCipherService;
import com.mycompany.mavenproject1.crypto.EncryptionMode;
import com.mycompany.mavenproject1.crypto.KeyManager;
import com.mycompany.mavenproject1.image.ImageCryptoProcessor;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import java.io.File;
import javafx.stage.FileChooser;
import javax.crypto.SecretKey;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import javafx.scene.image.Image;
import javax.imageio.ImageIO;
import com.mycompany.mavenproject1.model.EncryptedImagePackage;

public class MainController implements Initializable {

    @FXML private ComboBox<EncryptionMode> modeComboBox;
    @FXML private TextField keyTextField;
    @FXML private ImageView inputImageView;
    @FXML private ImageView outputImageView;
    @FXML private Label statusLabel;

    // ربط طبقات الـ Backend اللي بنيناها في Phase 4
    private final KeyManager keyManager = new KeyManager();
    private final AesCipherService cipherService = new AesCipherService();
    private final ImageCryptoProcessor imageProcessor = new ImageCryptoProcessor();

    private BufferedImage currentInputImage;
    private BufferedImage currentOutputImage;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // نملى الـ ComboBox بالـ 3 Modes ونختار ECB كافتراضي
        modeComboBox.getItems().setAll(EncryptionMode.values());
        modeComboBox.setValue(EncryptionMode.ECB);
    }

    private void updateStatus(String message, boolean isError) {
        statusLabel.setText("Status: " + message);
        if (isError) {
            statusLabel.setStyle("-fx-text-fill: #dc2626; -fx-font-weight: bold;");
        } else {
            statusLabel.setStyle("-fx-text-fill: #16a34a; -fx-font-weight: bold;");
        }
    }
    @FXML
    private void onGenerateKey() {
        try{
            SecretKey key = keyManager.generateKey();
            String base64Key = keyManager.encodeKeyToString(key);
            keyTextField.setText(base64Key);
            updateStatus("New 128-bit AES key generated.", false);
        }catch (Exception e){
            updateStatus("Failed to generate key: " + e.getMessage(), true);
        }
    }

    @FXML
    private void onSaveKey() {
        try{
            SecretKey key = keyManager.decodeKeyFromString(keyTextField.getText());
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save AES Key");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Key Files (*.key, *.txt)", "*.key", "*.txt"));
            fileChooser.setInitialFileName("aes_secret.key");
            File file = fileChooser.showSaveDialog(keyTextField.getScene().getWindow());
            if (file != null) {
                keyManager.saveKeyToFile(key, file);
                updateStatus("Key saved to " + file.getName(), false);
            }
            
        }catch (Exception e){
            updateStatus("Failed to save key: " + e.getMessage(), true);
        }
    }

    @FXML
    private void onLoadKey() {
        try{
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Load AES Key");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Key Files (*.key, *.txt)", "*.key", "*.txt"));
            File file = fileChooser.showOpenDialog(keyTextField.getScene().getWindow());
            if (file != null) {
                SecretKey key = keyManager.loadKeyFromFile(file);
                keyTextField.setText(keyManager.encodeKeyToString(key));
                updateStatus("Key loaded from " + file.getName(), false);
            }
        }catch (Exception e){
            updateStatus("Failed to load key: " + e.getMessage(), true);
        }
    }
    
    private Image convertToFxImage(BufferedImage bufferedImage) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "png", out);
        return new Image(new ByteArrayInputStream(out.toByteArray()));
    }

    @FXML
    private void onSelectImage() {
        try{
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Image");
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files (*.png, *.jpg, *.jpeg, *.bmp)", "*.png", "*.jpg", "*.jpeg", "*.bmp")
            );
            File file = fileChooser.showOpenDialog(inputImageView.getScene().getWindow());
            if (file != null) {
                BufferedImage loadedImage = ImageIO.read(file);
                if (loadedImage == null) {
                    throw new IllegalArgumentException("Unsupported or corrupted image file.");
                }
                currentInputImage = loadedImage;
                inputImageView.setImage(convertToFxImage(currentInputImage));

                // نفضي مكان النتيجة القديمة
                currentOutputImage = null;
                outputImageView.setImage(null);

                updateStatus("Loaded image: " + file.getName() + " (" + currentInputImage.getWidth() + "x" + currentInputImage.getHeight() + ")", false);
            }
        }catch (Exception e){
            updateStatus("Failed to load image: " + e.getMessage(), true);
        }
    }

    @FXML
    private void onSaveOutputImage() {
        try{
            if (currentOutputImage == null) {
                throw new IllegalStateException("No output image to save. Please encrypt or decrypt an image first.");
            }
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Output Image");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG Image (*.png)", "*.png"));
            fileChooser.setInitialFileName("output_" + modeComboBox.getValue() + ".png");
            File file = fileChooser.showSaveDialog(outputImageView.getScene().getWindow());
            if (file != null) {
                ImageIO.write(currentOutputImage, "png", file);
                updateStatus("Output image saved to " + file.getName(), false);
            }
            
        }catch (Exception e){
            updateStatus("Failed to save image: " + e.getMessage(), true);
        }
    }

    @FXML
    private void onEncryptImage() {
        try{
            if (currentInputImage == null) {
                throw new IllegalStateException("Please select an image first.");
            }
            SecretKey key = keyManager.decodeKeyFromString(keyTextField.getText());
            EncryptionMode mode = modeComboBox.getValue();
            byte[] iv = mode.isIvRequired() ? cipherService.generateIv() : null;
            byte[] plainPixels = imageProcessor.extractRgbBytes(currentInputImage);
            byte[] ciphertext = cipherService.encrypt(plainPixels, key, mode, iv);
            currentOutputImage = imageProcessor.packEncryptedImage(
                    currentInputImage.getWidth(),
                    currentInputImage.getHeight(),
                    ciphertext,
                    iv
            );
            outputImageView.setImage(convertToFxImage(currentOutputImage));
            updateStatus("Image encrypted successfully using AES-" + mode + ".", false);
        }catch (Exception e){
            updateStatus("Encryption failed: " + e.getMessage(), true);
        }
    }

    @FXML
    private void onDecryptImage() {
        try{
            if (currentInputImage == null) {
                throw new IllegalStateException("Please select an encrypted image first.");
            }
            SecretKey key = keyManager.decodeKeyFromString(keyTextField.getText());
            EncryptionMode mode = modeComboBox.getValue();
            EncryptedImagePackage pkg = imageProcessor.unpackEncryptedImage(currentInputImage);
            byte[] decryptedPixels = cipherService.decrypt(pkg.getCiphertext(), key, mode, pkg.getIv());
            currentOutputImage = imageProcessor.rebuildPlainImage(
                    pkg.getOriginalWidth(),
                    pkg.getOriginalHeight(),
                    decryptedPixels
            );
            outputImageView.setImage(convertToFxImage(currentOutputImage));
            updateStatus("Image decrypted successfully using AES-" + mode + ".", false);
        }catch (Exception e){
            updateStatus("Decryption failed: " + e.getMessage(), true);
        }
    }
    
    
}