/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.mavenproject1.model;

/**
 *
 * @author Mohammad
 */
public class EncryptedImagePackage {
    private final int originalWidth;
    private final int originalHeight;
    private final byte[] iv;
    private final byte[] ciphertext;

    public EncryptedImagePackage(int originalWidth, int originalHeight, byte[] iv, byte[] ciphertext) {
        this.originalWidth = originalWidth;
        this.originalHeight = originalHeight;
        this.iv = iv;
        this.ciphertext = ciphertext;
    }

    public int getOriginalWidth() {
        return originalWidth;
    }

    public int getOriginalHeight() {
        return originalHeight;
    }

    public byte[] getIv() {
        return iv;
    }

    public byte[] getCiphertext() {
        return ciphertext;
    }
    
    
}
