/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.mavenproject1.crypto;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
/**
 *
 * @author Mohammad
 */
public class KeyManager {
    private static final String ALGORITHM = "AES";
    private static final int DEFAULT_KEY_SIZE = 128;
    
    // الدالة الافتراضية (بتستخدم 128 أوتوماتيك)
    public SecretKey generateKey() throws NoSuchAlgorithmException {
        return generateKey(DEFAULT_KEY_SIZE);
    }

    // الدالة المرنة (بتسمح بـ 128 أو 192 أو 256)
    public SecretKey generateKey(int keySize) throws NoSuchAlgorithmException {
        if (keySize != 128 && keySize != 192 && keySize != 256) {
            throw new IllegalArgumentException("Invalid AES key size. Must be 128, 192, or 256 bits.");
        }
        KeyGenerator keyGen = KeyGenerator.getInstance(ALGORITHM);
        keyGen.init(keySize);
        return keyGen.generateKey();
    }
    
    public String encodeKeyToString(SecretKey key) {
        if (key == null) {
            throw new IllegalArgumentException("SecretKey cannot be null.");
        }
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }
    
    public SecretKey decodeKeyFromString(String encodedKey) {
        if (encodedKey == null || encodedKey.trim().isEmpty()) {
            throw new IllegalArgumentException("Encoded key cannot be null or empty.");
        }
        byte[] keyBytes = Base64.getDecoder().decode(encodedKey.trim());
        // التحقق إن طول المفتاح بالبايت هو 16 (128-bit) أو 24 (192-bit) أو 32 (256-bit)
        if (keyBytes.length != 16 && keyBytes.length != 24 && keyBytes.length != 32) {
            throw new IllegalArgumentException("Invalid AES key length: " + keyBytes.length + " bytes. Must be 16, 24, or 32 bytes.");
        }
        return new SecretKeySpec(keyBytes, ALGORITHM);
    }
    
    public void saveKeyToFile(SecretKey key, File file) throws IOException{
        Files.writeString(file.toPath(), encodeKeyToString(key));
    }
    
    public SecretKey loadKeyFromFile(File file) throws IOException{
        String encodedKey = Files.readString(file.toPath());
        return decodeKeyFromString(encodedKey);
    }
}
