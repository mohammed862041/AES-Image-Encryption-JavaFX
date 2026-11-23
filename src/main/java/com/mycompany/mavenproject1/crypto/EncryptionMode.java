/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package com.mycompany.mavenproject1.crypto; // Declares the crypto package for AES encryption utilities

/**
 *
 * @author Mohammad
 */
// Enumerates the supported AES block cipher modes along with their JCA transformation strings and IV requirements
public enum EncryptionMode {
    ECB("AES/ECB/PKCS5Padding", false), // Electronic Codebook: encrypts each block independently; uses PKCS5 padding and needs no IV
    CBC("AES/CBC/PKCS5Padding", true), // Cipher Block Chaining: chains each block with the previous one; uses PKCS5 padding and requires an IV
    CTR("AES/CTR/NoPadding", true); // Counter mode: turns AES into a stream cipher; needs no padding (same output length) and requires an IV/nonce
    
    private final String transformation; // Full JCA Cipher transformation name ("Algorithm/Mode/Padding") passed to Cipher.getInstance()
    private final boolean ivRequired; // Flag indicating whether this mode needs a 16-byte Initialization Vector (IV)

    // Private enum constructor that binds each mode constant to its JCA transformation string and IV flag
    private EncryptionMode(String transformation, boolean ivRequired) {
        this.transformation = transformation; // Saves the transformation string for this mode
        this.ivRequired = ivRequired; // Saves whether an IV must be provided for this mode
    }
    
    public String getTransformation() { // Returns the JCA transformation string used to configure javax.crypto.Cipher
        return this.transformation; // Exposes the stored transformation string
    }

    public boolean isIvRequired() { // Returns true if this mode requires a 16-byte IV (CBC and CTR), false otherwise (ECB)
        return this.ivRequired; // Exposes the stored IV requirement flag
    }
    
}
