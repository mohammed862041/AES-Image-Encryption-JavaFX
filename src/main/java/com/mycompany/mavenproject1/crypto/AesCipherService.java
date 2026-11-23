/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.mavenproject1.crypto; // Declares the crypto package containing AES encryption/decryption services

import java.security.GeneralSecurityException; // Superclass for all JCA crypto exceptions (bad key, bad padding, invalid IV, etc.)
import java.security.SecureRandom; // Cryptographically strong random number generator used for generating unpredictable IVs
import javax.crypto.Cipher; // Core JCA class that performs symmetric encryption and decryption
import javax.crypto.SecretKey; // Represents the AES secret key passed into the cipher
import javax.crypto.spec.IvParameterSpec; // Wraps a raw IV byte array into the parameter format required by Cipher.init()
/**
 *
 * @author Mohammad
 */
// Service class that performs AES encryption, decryption, and secure IV generation across ECB, CBC, and CTR modes
public class AesCipherService {
    public static final int AES_BLOCK_SIZE = 16; // AES always uses 128-bit (16-byte) blocks, so IVs must be exactly 16 bytes
    
    // Creates a fresh cryptographically random 16-byte IV so encrypting the same image twice produces different ciphertext
    public byte[] generateIv(){
        byte[] iv = new byte[AES_BLOCK_SIZE]; // Allocates a 16-byte array matching one AES block
        new SecureRandom().nextBytes(iv); // Fills the array with unpredictable random bytes (never use java.util.Random for crypto)
        return iv; // Returns the generated IV for use with CBC or CTR mode
    }
    
    // Encrypts raw plaintext bytes using the given AES key, mode, and optional IV
    public byte[] encrypt(byte[] plaintext, SecretKey key, EncryptionMode mode, byte[] iv) throws GeneralSecurityException{
        return processCipher(Cipher.ENCRYPT_MODE, plaintext, key, mode, iv); // Delegates to shared helper configured in ENCRYPT_MODE
    }
    
    // Decrypts ciphertext bytes back into plaintext using the given AES key, mode, and optional IV
    public byte[] decrypt(byte[] ciphertext, SecretKey key, EncryptionMode mode, byte[] iv) throws GeneralSecurityException{
        return processCipher(Cipher.DECRYPT_MODE, ciphertext, key, mode, iv); // Delegates to shared helper configured in DECRYPT_MODE
    }
    
    // Shared internal helper that sets up the JCA Cipher and executes either encryption or decryption to avoid duplicating logic
    private byte[] processCipher(int opMode, byte[] data, SecretKey key, EncryptionMode mode, byte[] iv) throws GeneralSecurityException{
        if(data==null || key==null || mode ==null) // Validates that required inputs are present before configuring the cipher
            throw new IllegalArgumentException("data or key or mode cannot be null. "); // Fails fast with an explicit error
        // Creates a Cipher instance for the exact "AES/<Mode>/<Padding>" transformation defined by the selected EncryptionMode
        Cipher cipher=Cipher.getInstance(mode.getTransformation());
        if (mode.isIvRequired()){ // CBC and CTR modes require an IvParameterSpec during initialization
            if (iv == null || iv.length != AES_BLOCK_SIZE) { // Ensures the caller supplied a valid 16-byte IV
                throw new IllegalArgumentException("Valid 16-byte IV is required for " + mode); // Prevents invalid IV exceptions later
            }
            // Initializes the cipher with the operation mode (encrypt/decrypt), AES key, and wrapped 16-byte IV
            cipher.init(opMode, key, new IvParameterSpec(iv));
        }else // ECB mode does not use an IV; passing an IV to ECB would throw InvalidAlgorithmParameterException
            cipher.init(opMode, key); // Initializes the ECB cipher with just the operation mode and AES key
        return cipher.doFinal(data); // Processes all input bytes in one shot (applying or verifying/stripping padding as needed)
                
    }
}
