/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.mavenproject1.image;
import com.mycompany.mavenproject1.model.EncryptedImagePackage;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.nio.ByteBuffer;
import java.util.Arrays;
/**
 *
 * @author Mohammad
 */
public class ImageCryptoProcessor {
    private static final int BYTES_PER_PIXEL = 3;
    
    
    public byte[] extractRgbBytes(BufferedImage source) {
        if(source==null)
            throw new IllegalArgumentException("Source cannot be null. ");
        BufferedImage bgrImage;
        if(source.getType() == BufferedImage.TYPE_3BYTE_BGR)
            bgrImage = source;
        else{
            bgrImage = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
            Graphics2D g = bgrImage.createGraphics();
            g.drawImage(source, 0, 0, null);
            g.dispose();
        }
        byte[] rawBytes = ((DataBufferByte) bgrImage.getRaster().getDataBuffer()).getData();
        return Arrays.copyOf(rawBytes, rawBytes.length);   
    }
    
    public BufferedImage rebuildPlainImage(int width, int height, byte[] decryptedBytes) {
        int expectedLength = width * height * BYTES_PER_PIXEL;
        if(decryptedBytes == null || decryptedBytes.length != expectedLength)
            throw new IllegalArgumentException("Decrypted data size does not match image dimensions.");
        BufferedImage plainImage = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        byte[] targetBytes = ((DataBufferByte) plainImage.getRaster().getDataBuffer()).getData();
        System.arraycopy(decryptedBytes, 0, targetBytes, 0, decryptedBytes.length);
        return plainImage;
    }
    
    public BufferedImage packEncryptedImage(int width, int height, byte[] ciphertext, byte[] iv) {
        int pixelDataLength = width * height * BYTES_PER_PIXEL;
        int extraPaddingBytes = ciphertext.length - pixelDataLength;
        int ivLength = (iv != null) ? iv.length : 0;
        int metadataSize = 4 + 1 + ivLength + extraPaddingBytes;
        
        if (width * BYTES_PER_PIXEL < metadataSize) {
            throw new IllegalArgumentException("Image width is too small to store encryption metadata.");  
        }
        
        BufferedImage encryptedImage = new BufferedImage(width, height + 1, BufferedImage.TYPE_3BYTE_BGR);
        byte[] imageBytes = ((DataBufferByte) encryptedImage.getRaster().getDataBuffer()).getData();
        
        System.arraycopy(ciphertext, 0, imageBytes, 0, pixelDataLength);
        ByteBuffer metaBuffer = ByteBuffer.wrap(imageBytes, pixelDataLength, width * BYTES_PER_PIXEL);
        metaBuffer.putInt(ciphertext.length);
        metaBuffer.put((byte) ivLength);
        
        if (iv != null) {
            metaBuffer.put(iv);
        }
        
        if (extraPaddingBytes > 0) {
            metaBuffer.put(ciphertext, pixelDataLength, extraPaddingBytes);
        }
        return encryptedImage;
    }
    
    public EncryptedImagePackage unpackEncryptedImage(BufferedImage encryptedImage) {
        if (encryptedImage == null || encryptedImage.getHeight() < 2) {
            throw new IllegalArgumentException("Invalid encrypted image.");
        }
        int originalWidth = encryptedImage.getWidth();
        int originalHeight = encryptedImage.getHeight() - 1;
        int pixelDataLength = originalWidth * originalHeight * BYTES_PER_PIXEL;
        
        byte[] imageBytes = extractRgbBytes(encryptedImage);
        ByteBuffer metaBuffer = ByteBuffer.wrap(imageBytes, pixelDataLength, originalWidth * BYTES_PER_PIXEL);
        int cipherLength = metaBuffer.getInt();
        int ivLength = metaBuffer.get() & 0xFF;
        if (cipherLength < pixelDataLength || cipherLength > pixelDataLength + 16 || (4 + 1 + ivLength + (cipherLength - pixelDataLength)) > originalWidth * BYTES_PER_PIXEL) {
            throw new IllegalArgumentException("The selected image does not contain valid encryption metadata.");
        }
        

        byte[] iv = null;
        if (ivLength > 0) {
            iv = new byte[ivLength];
            metaBuffer.get(iv);
        }
        byte[] ciphertext = new byte[cipherLength];
        System.arraycopy(imageBytes, 0, ciphertext, 0, pixelDataLength);
        int extraPaddingBytes = cipherLength - pixelDataLength;
        if (extraPaddingBytes > 0) {
            metaBuffer.get(ciphertext, pixelDataLength, extraPaddingBytes);
        }
        
        return new EncryptedImagePackage(originalWidth, originalHeight, iv, ciphertext);
    }
    
}
