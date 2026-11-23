/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.mavenproject1.model; // Declares the model package holding plain data carrier classes

/**
 *
 * @author Mohammad
 */
// Immutable data container that holds everything extracted from an encrypted image so it can be decrypted and rebuilt
public class EncryptedImagePackage {
    private final int originalWidth; // Original image width in pixels (needed to rebuild the decrypted BufferedImage)
    private final int originalHeight; // Original image height in pixels (excluding the extra bottom metadata row)
    private final byte[] iv; // Initialization Vector extracted from metadata (null for ECB mode, 16 bytes for CBC/CTR)
    private final byte[] ciphertext; // Complete encrypted pixel bytes, including any AES padding bytes at the end

    // Initializes this package with the unpacked image dimensions, IV, and full ciphertext
    public EncryptedImagePackage(int originalWidth, int originalHeight, byte[] iv, byte[] ciphertext) {
        this.originalWidth = originalWidth; // Stores the original image width
        this.originalHeight = originalHeight; // Stores the original image height
        this.iv = iv; // Stores the Initialization Vector (or null if the mode does not use an IV)
        this.ciphertext = ciphertext; // Stores the encrypted byte array to pass to the AES decryptor
    }

    public int getOriginalWidth() { // Returns the original width of the image before encryption
        return originalWidth; // Exposes the stored width field
    }

    public int getOriginalHeight() { // Returns the original height of the image before the metadata row was appended
        return originalHeight; // Exposes the stored height field
    }

    public byte[] getIv() { // Returns the extracted IV needed for CBC/CTR decryption (or null for ECB)
        return iv; // Exposes the stored IV byte array
    }

    public byte[] getCiphertext() { // Returns the full encrypted payload ready for AES decryption
        return ciphertext; // Exposes the stored ciphertext byte array
    }
    
    
}
