/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package com.mycompany.mavenproject1.crypto;

/**
 *
 * @author Mohammad
 */
public enum EncryptionMode {
    ECB("AES/ECB/PKCS5Padding", false),
    CBC("AES/CBC/PKCS5Padding", true),
    CTR("AES/CTR/NoPadding", true);
    
    private final String transformation;
    private final boolean ivRequired;

    private EncryptionMode(String transformation, boolean ivRequired) {
        this.transformation = transformation;
        this.ivRequired = ivRequired;
    }
    
    public String getTransformation() {
        return this.transformation;
    }

    public boolean isIvRequired() {
        return this.ivRequired;
    }
    
}
