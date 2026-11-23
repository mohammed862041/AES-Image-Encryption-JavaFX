/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.mavenproject1.image; // Declares the image package for converting between BufferedImage and raw encrypted bytes
import com.mycompany.mavenproject1.model.EncryptedImagePackage; // Data holder returned when unpacking an encrypted image
import java.awt.Graphics2D; // Used to redraw non-BGR images into a standard 3-byte BGR BufferedImage format
import java.awt.image.BufferedImage; // Standard AWT in-memory image representation used for pixel manipulation and ImageIO
import java.awt.image.DataBufferByte; // Gives direct byte[] access to a TYPE_3BYTE_BGR image's underlying pixel buffer
import java.nio.ByteBuffer; // Simplifies reading/writing structured binary fields (int, byte, byte[]) inside the metadata row
import java.util.Arrays; // Used to clone the raw pixel array safely without mutating the source image
/**
 *
 * @author Mohammad
 */
// Handles pixel byte extraction, rebuilding decrypted images, and packing/unpacking encryption metadata in an extra image row
public class ImageCryptoProcessor {
    private static final int BYTES_PER_PIXEL = 3; // Each TYPE_3BYTE_BGR pixel occupies exactly 3 bytes (Blue, Green, Red)
    
    
    // Converts any BufferedImage into a flat byte[] of raw 3-byte BGR pixel data ready for AES encryption
    public byte[] extractRgbBytes(BufferedImage source) {
        if(source==null) // Guards against null input before inspecting image properties
            throw new IllegalArgumentException("Source cannot be null. "); // Fails early if no image was provided
        BufferedImage bgrImage; // Will hold a guaranteed TYPE_3BYTE_BGR image so its raster uses DataBufferByte
        if(source.getType() == BufferedImage.TYPE_3BYTE_BGR) // Checks if the image is already in 3-byte BGR format
            bgrImage = source; // Reuses the original image directly to avoid an unnecessary copy
        else{ // Formats like PNG with alpha (ARGB) or indexed palettes must be converted to uniform 3-byte BGR first
            // Allocates a blank TYPE_3BYTE_BGR canvas with the same dimensions as the source image
            bgrImage = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
            Graphics2D g = bgrImage.createGraphics(); // Opens a 2D drawing context to paint onto the new BGR canvas
            g.drawImage(source, 0, 0, null); // Copies and converts all pixels from the source image into the BGR format
            g.dispose(); // Releases native graphics resources immediately after drawing is complete
        }
        // Casts the raster's backing buffer to DataBufferByte to access the raw BGR pixel array directly
        byte[] rawBytes = ((DataBufferByte) bgrImage.getRaster().getDataBuffer()).getData();
        return Arrays.copyOf(rawBytes, rawBytes.length); // Returns a defensive copy so downstream operations cannot mutate the source BufferedImage
    }
    
    // Reconstructs a visible BufferedImage from decrypted BGR pixel bytes using the original image dimensions
    public BufferedImage rebuildPlainImage(int width, int height, byte[] decryptedBytes) {
        int expectedLength = width * height * BYTES_PER_PIXEL; // Calculates exact byte count needed to fill a width x height BGR image
        if(decryptedBytes == null || decryptedBytes.length != expectedLength) // Verifies decrypted output matches expected pixel size (catches wrong key/mode)
            throw new IllegalArgumentException("Decrypted data size does not match image dimensions."); // Reports size mismatch
        // Creates an empty TYPE_3BYTE_BGR image of the original dimensions to receive the decrypted pixels
        BufferedImage plainImage = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        // Gets a direct reference to the new image's internal backing byte array
        byte[] targetBytes = ((DataBufferByte) plainImage.getRaster().getDataBuffer()).getData();
        // Copies all decrypted pixel bytes straight into the BufferedImage's internal buffer
        System.arraycopy(decryptedBytes, 0, targetBytes, 0, decryptedBytes.length);
        return plainImage; // Returns the fully restored plaintext image
    }
    
    // Packs ciphertext pixels plus an extra bottom row of metadata (cipherLength, ivLength, IV, and AES padding overflow) into one PNG-storable image
    public BufferedImage packEncryptedImage(int width, int height, byte[] ciphertext, byte[] iv) {
        int pixelDataLength = width * height * BYTES_PER_PIXEL; // Number of ciphertext bytes that fit in the main width x height image area
        // PKCS5Padding (ECB/CBC) appends 1..16 extra bytes beyond pixelDataLength that must be saved in the metadata row
        int extraPaddingBytes = ciphertext.length - pixelDataLength;
        int ivLength = (iv != null) ? iv.length : 0; // Stores 16 for CBC/CTR (which have an IV) or 0 for ECB (where iv is null)
        // Total metadata bytes needed: 4 bytes (int cipherLength) + 1 byte (ivLength) + IV bytes + extra AES padding bytes
        int metadataSize = 4 + 1 + ivLength + extraPaddingBytes;
        
        if (width * BYTES_PER_PIXEL < metadataSize) { // Ensures one extra pixel row (width * 3 bytes) is wide enough to hold all metadata
            // Fails if an extremely narrow image cannot fit the metadata header + IV + padding in a single row
            throw new IllegalArgumentException("Image width is too small to store encryption metadata.");  
        }
        
        // Allocates an image with (height + 1) rows: top 'height' rows store encrypted pixels; last row stores encryption metadata
        BufferedImage encryptedImage = new BufferedImage(width, height + 1, BufferedImage.TYPE_3BYTE_BGR);
        // Accesses the new image's backing byte array so we can write ciphertext and metadata directly into pixel memory
        byte[] imageBytes = ((DataBufferByte) encryptedImage.getRaster().getDataBuffer()).getData();
        
        // Copies the main encrypted pixel data into rows 0..(height - 1) so the encrypted image visually shows the cipher pattern
        System.arraycopy(ciphertext, 0, imageBytes, 0, pixelDataLength);
        // Wraps only the extra bottom row (starting at offset pixelDataLength) in a ByteBuffer for structured big-endian binary writes
        ByteBuffer metaBuffer = ByteBuffer.wrap(imageBytes, pixelDataLength, width * BYTES_PER_PIXEL);
        metaBuffer.putInt(ciphertext.length); // Writes the 4-byte total ciphertext length so unpacker knows how many padding bytes exist
        metaBuffer.put((byte) ivLength); // Writes the 1-byte IV length (0 for ECB, 16 for CBC/CTR)
        
        if (iv != null) { // Only writes IV bytes when the mode uses an IV (CBC/CTR)
            metaBuffer.put(iv); // Stores the 16-byte IV inside the metadata row so decryption can recover it later
        }
        
        if (extraPaddingBytes > 0) { // Only writes overflow bytes when padding made ciphertext longer than pixelDataLength
            // Stores the trailing ciphertext padding bytes right after the IV so decryption receives the exact complete ciphertext
            metaBuffer.put(ciphertext, pixelDataLength, extraPaddingBytes);
        }
        return encryptedImage; // Returns the self-contained encrypted image (visual ciphertext + hidden metadata row)
    }
    
    // Reverses packEncryptedImage: reads the extra bottom row to recover original dimensions, IV, and full ciphertext
    public EncryptedImagePackage unpackEncryptedImage(BufferedImage encryptedImage) {
        if (encryptedImage == null || encryptedImage.getHeight() < 2) { // Needs at least 1 pixel row + 1 metadata row (height >= 2)
            throw new IllegalArgumentException("Invalid encrypted image."); // Rejects null or impossibly short images
        }
        int originalWidth = encryptedImage.getWidth(); // Width was unchanged during packing
        int originalHeight = encryptedImage.getHeight() - 1; // Subtracts the 1 extra bottom metadata row to get the original image height
        int pixelDataLength = originalWidth * originalHeight * BYTES_PER_PIXEL; // Byte count of the main encrypted pixel region
        
        byte[] imageBytes = extractRgbBytes(encryptedImage); // Extracts all BGR bytes (main encrypted pixels + bottom metadata row)
        // Wraps the bottom metadata row in a ByteBuffer to read back fields in the exact same order they were written
        ByteBuffer metaBuffer = ByteBuffer.wrap(imageBytes, pixelDataLength, originalWidth * BYTES_PER_PIXEL);
        int cipherLength = metaBuffer.getInt(); // Reads the 4-byte total ciphertext length written during packing
        // Reads the 1-byte IV length and masks with 0xFF to convert Java's signed byte (-128..127) to unsigned int (0..255)
        int ivLength = metaBuffer.get() & 0xFF;
        // Sanity-checks metadata values (AES padding adds at most 16 bytes, and total metadata must fit in the bottom row) to catch plain/unencrypted images
        if (cipherLength < pixelDataLength || cipherLength > pixelDataLength + 16 || (4 + 1 + ivLength + (cipherLength - pixelDataLength)) > originalWidth * BYTES_PER_PIXEL) {
            // Prevents BufferUnderflowException or garbage output if the user tries to decrypt a normal unencrypted image
            throw new IllegalArgumentException("The selected image does not contain valid encryption metadata.");
        }
        

        byte[] iv = null; // Defaults IV to null (expected for ECB mode where ivLength == 0)
        if (ivLength > 0) { // Checks whether an IV was stored in the metadata row (CBC/CTR)
            iv = new byte[ivLength]; // Allocates an array matching the stored IV length (16 bytes)
            metaBuffer.get(iv); // Reads the IV bytes out of the metadata row
        }
        byte[] ciphertext = new byte[cipherLength]; // Allocates the full ciphertext buffer (main pixels + any trailing padding bytes)
        System.arraycopy(imageBytes, 0, ciphertext, 0, pixelDataLength); // Copies the main encrypted pixels from rows 0..(originalHeight - 1)
        int extraPaddingBytes = cipherLength - pixelDataLength; // Calculates how many overflow padding bytes were stored in the metadata row
        if (extraPaddingBytes > 0) { // If padding overflow bytes exist (ECB/CBC), appends them back onto ciphertext
            // Reads the trailing padding bytes from the metadata row into the end of ciphertext so AES decryption can unpad cleanly
            metaBuffer.get(ciphertext, pixelDataLength, extraPaddingBytes);
        }
        
        // Bundles original dimensions, IV, and reassembled ciphertext into one package for the controller to decrypt and rebuild
        return new EncryptedImagePackage(originalWidth, originalHeight, iv, ciphertext);
    }
    
}
