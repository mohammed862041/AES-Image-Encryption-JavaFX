/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.mavenproject1.crypto; // Declares the crypto package for AES key management

import java.io.File; // Represents a file path on disk for saving or loading keys
import java.io.IOException; // Checked exception thrown if reading or writing a key file fails
import java.nio.file.Files; // Utility class for reading and writing text files in one call
import java.security.NoSuchAlgorithmException; // Thrown if the JVM does not support the requested crypto algorithm
import java.util.Base64; // Encodes raw key bytes into printable ASCII text and decodes them back
import javax.crypto.KeyGenerator; // Cryptographically secure generator for symmetric secret keys
import javax.crypto.SecretKey; // Standard Java representation of a symmetric encryption key
import javax.crypto.spec.SecretKeySpec; // Reconstructs a SecretKey object from raw key bytes and an algorithm name
/**
 *
 * @author Mohammad
 */
// Handles generating, Base64-encoding/decoding, saving, and loading AES secret keys
public class KeyManager {
    private static final String ALGORITHM = "AES"; // Algorithm identifier passed to KeyGenerator and SecretKeySpec
    private static final int DEFAULT_KEY_SIZE = 128; // Default AES key strength in bits (16 bytes)
    
    // الدالة الافتراضية (بتستخدم 128 أوتوماتيك)
    // Convenience overload that generates an AES key using the default 128-bit size
    public SecretKey generateKey() throws NoSuchAlgorithmException {
        return generateKey(DEFAULT_KEY_SIZE); // Delegates to the parameterized generateKey(int) method with 128 bits
    }

    // الدالة المرنة (بتسمح بـ 128 أو 192 أو 256)
    // Generates a new random AES key with an explicit bit length (128, 192, or 256)
    public SecretKey generateKey(int keySize) throws NoSuchAlgorithmException {
        if (keySize != 128 && keySize != 192 && keySize != 256) { // Validates that keySize is one of the 3 standard AES bit lengths
            // Rejects unsupported key sizes early with a clear error message
            throw new IllegalArgumentException("Invalid AES key size. Must be 128, 192, or 256 bits.");
        }
        KeyGenerator keyGen = KeyGenerator.getInstance(ALGORITHM); // Requests an AES KeyGenerator from the Java Cryptography Architecture
        keyGen.init(keySize); // Configures the generator to produce a key of the requested bit length using a secure random source
        return keyGen.generateKey(); // Generates and returns the new random AES SecretKey
    }
    
    // Converts a binary SecretKey into a Base64 string so it can be safely displayed in a text field or saved in a text file
    public String encodeKeyToString(SecretKey key) {
        if (key == null) { // Guards against null input before attempting to read key bytes
            throw new IllegalArgumentException("SecretKey cannot be null."); // Signals invalid caller input
        }
        return Base64.getEncoder().encodeToString(key.getEncoded()); // Gets the raw key bytes and encodes them into Base64 text
    }
    
    // Converts a Base64-encoded key string from the UI or a file back into a usable AES SecretKey
    public SecretKey decodeKeyFromString(String encodedKey) {
        if (encodedKey == null || encodedKey.trim().isEmpty()) { // Rejects null, empty, or whitespace-only key strings
            throw new IllegalArgumentException("Encoded key cannot be null or empty."); // Prevents decoding an empty key
        }
        byte[] keyBytes = Base64.getDecoder().decode(encodedKey.trim()); // Strips surrounding whitespace/newlines and decodes Base64 into raw bytes
        // التحقق إن طول المفتاح بالبايت هو 16 (128-bit) أو 24 (192-bit) أو 32 (256-bit)
        // Ensures the decoded byte count matches AES-128 (16 B), AES-192 (24 B), or AES-256 (32 B) before building the key
        if (keyBytes.length != 16 && keyBytes.length != 24 && keyBytes.length != 32) {
            // Throws a descriptive error if the user pasted a truncated or malformed key
            throw new IllegalArgumentException("Invalid AES key length: " + keyBytes.length + " bytes. Must be 16, 24, or 32 bytes.");
        }
        return new SecretKeySpec(keyBytes, ALGORITHM); // Wraps the validated raw bytes into an AES SecretKeySpec object
    }
    
    // Writes the Base64-encoded representation of the AES key to the specified file on disk
    public void saveKeyToFile(SecretKey key, File file) throws IOException{
        Files.writeString(file.toPath(), encodeKeyToString(key)); // Encodes the key to Base64 text and saves it as UTF-8 to the file path
    }
    
    // Reads a Base64-encoded AES key from a file on disk and reconstructs the SecretKey
    public SecretKey loadKeyFromFile(File file) throws IOException{
        String encodedKey = Files.readString(file.toPath()); // Reads the entire file content as a String
        return decodeKeyFromString(encodedKey); // Decodes and validates the Base64 string into an AES SecretKey
    }
}
