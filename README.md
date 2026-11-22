# AES Image Encryption & Decryption (JavaFX)

A desktop application built with **Java 27** and **JavaFX 27** for encrypting and decrypting digital images using the **Advanced Encryption Standard (AES-128)** across multiple block cipher modes of operation (**ECB**, **CBC**, and **CTR**).

---

## Key Features

- **Multi-Mode AES-128 Encryption & Decryption:**
  - **ECB (Electronic Codebook):** Demonstrates deterministic block encryption and pattern leakage on visual data.
  - **CBC (Cipher Block Chaining):** Uses a cryptographically secure 16-byte Initialization Vector (IV) and PKCS#5 padding to eliminate visual patterns.
  - **CTR (Counter Mode):** Operates as a stream cipher without padding overhead using a random 16-byte IV/Nonce.
- **Custom Lossless Image Packaging (`ImageCryptoProcessor`):**
  - Extracts raw 24-bit `TYPE_3BYTE_BGR` pixel data for direct byte-level encryption.
  - Embeds encryption metadata (ciphertext length, IV length, IV bytes, and extra block-padding bytes) into an appended bottom pixel row (`height + 1`) and exports strictly in **PNG** format to prevent lossy compression corruption.
- **Symmetric Key Management (`KeyManager`):**
  - Generates 128-bit AES secret keys via `SecureRandom`.
  - Supports Base64 encoding/decoding for clipboard sharing, as well as exporting and importing `.key` / `.txt` key files.
- **Interactive Side-by-Side GUI:**
  - Built with JavaFX and FXML (`BorderPane` layout) featuring real-time image previews for input (plaintext/ciphertext) and output results, alongside live status feedback.

---

## Project Architecture

The codebase follows a modular separation of concerns:

```text
src/main/
├── java/com/mycompany/mavenproject1/
│   ├── App.java                      # JavaFX Application entry point (loads MainView.fxml)
│   ├── Launcher.java                 # Fat-JAR wrapper entry point for standalone execution
│   ├── controller/
│   │   └── MainController.java       # UI event handling, FileChoosers, and service orchestration
│   ├── crypto/
│   │   ├── AesCipherService.java     # Core AES encryption/decryption engine & SecureRandom IV generation
│   │   ├── EncryptionMode.java       # Enum defining ECB, CBC, and CTR transformations & IV rules
│   │   └── KeyManager.java           # AES-128 key generation, Base64 conversion, and file I/O
│   ├── image/
│   │   └── ImageCryptoProcessor.java # BGR byte extraction, metadata row packing/unpacking, image reconstruction
│   └── model/
│       └── EncryptedImagePackage.java# Immutable data carrier for unpacked dimensions, IV, and ciphertext
└── resources/com/mycompany/mavenproject1/
    └── MainView.fxml                 # FXML UI layout definition
