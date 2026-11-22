/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.mavenproject1.crypto;

import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
/**
 *
 * @author Mohammad
 */
public class AesCipherService {
    public static final int AES_BLOCK_SIZE = 16;
    
    public byte[] generateIv(){
        byte[] iv = new byte[AES_BLOCK_SIZE];
        new SecureRandom().nextBytes(iv);
        return iv;
    }
    
    public byte[] encrypt(byte[] plaintext, SecretKey key, EncryptionMode mode, byte[] iv) throws GeneralSecurityException{
        return processCipher(Cipher.ENCRYPT_MODE, plaintext, key, mode, iv);
    }
    
    public byte[] decrypt(byte[] ciphertext, SecretKey key, EncryptionMode mode, byte[] iv) throws GeneralSecurityException{
        return processCipher(Cipher.DECRYPT_MODE, ciphertext, key, mode, iv);
    }
    
    private byte[] processCipher(int opMode, byte[] data, SecretKey key, EncryptionMode mode, byte[] iv) throws GeneralSecurityException{
        if(data==null || key==null || mode ==null)
            throw new IllegalArgumentException("data or key or mode cannot be null. ");
        Cipher cipher=Cipher.getInstance(mode.getTransformation());
        if (mode.isIvRequired()){
            if (iv == null || iv.length != AES_BLOCK_SIZE) {
                throw new IllegalArgumentException("Valid 16-byte IV is required for " + mode);
            }
            cipher.init(opMode, key, new IvParameterSpec(iv));
        }else
            cipher.init(opMode, key);
        return cipher.doFinal(data);
                
    }
}
