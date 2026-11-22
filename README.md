# AES Image Encryption & Decryption (JavaFX)

![Java](https://img.shields.io/badge/Java-27-orange?logo=openjdk)
![JavaFX](https://img.shields.io/badge/JavaFX-27.0.1-blue)
![Maven](https://img.shields.io/badge/Maven-Build-C71A36?logo=apachemaven)
![AES](https://img.shields.io/badge/Encryption-AES--128-green)

A desktop application built with **Java 27** and **JavaFX 27** for encrypting and decrypting digital images using the **Advanced Encryption Standard (AES-128)** across three block cipher modes of operation: **ECB**, **CBC**, and **CTR**.

---

## Key Features

- **Multi-Mode AES-128 Encryption & Decryption:**
  - **ECB (Electronic Codebook):** Demonstrates deterministic block encryption and visual pattern leakage on structured images.
  - **CBC (Cipher Block Chaining):** Uses a cryptographically secure 16-byte Initialization Vector (IV) and PKCS#5 padding to eliminate visual patterns.
  - **CTR (Counter Mode):** Operates as a stream cipher with zero padding overhead using a random 16-byte IV/Nonce.
- **Custom Lossless Image Packaging (`ImageCryptoProcessor`):**
  - Extracts raw 24-bit `TYPE_3BYTE_BGR` pixel data for direct byte-level encryption.
  - Embeds encryption metadata (ciphertext length, IV length, IV bytes, and extra block-padding bytes) into an appended bottom pixel row (`height + 1`) and exports strictly in **PNG** format to prevent lossy compression corruption.
- **Symmetric Key Management (`KeyManager`):**
  - Generates 128-bit AES secret keys via `SecureRandom`.
  - Supports Base64 encoding/decoding for quick copying, as well as saving and loading `.key` / `.txt` key files.
- **Interactive Side-by-Side GUI:**
  - Built with JavaFX and FXML (`BorderPane` layout) featuring side-by-side image previews for input (plaintext/ciphertext) and output results, along with real-time status feedback.

---

## Project Architecture

The codebase follows a clean modular architecture separating cryptographic logic, image processing, data models, and UI controllers:

```text
src/main/
├── java/com/mycompany/mavenproject1/
│   ├── App.java                       # JavaFX Application entry point (loads MainView.fxml)
│   ├── Launcher.java                  # Fat-JAR wrapper entry point for standalone execution
│   ├── controller/
│   │   └── MainController.java        # UI event handling, FileChoosers, and service orchestration
│   ├── crypto/
│   │   ├── AesCipherService.java      # Core AES encryption/decryption engine & SecureRandom IV generation
│   │   ├── EncryptionMode.java        # Enum defining ECB, CBC, and CTR transformations & IV rules
│   │   └── KeyManager.java            # AES-128 key generation, Base64 conversion, and file I/O
│   ├── image/
│   │   └── ImageCryptoProcessor.java  # BGR byte extraction, metadata row packing/unpacking, image reconstruction
│   └── model/
│       └── EncryptedImagePackage.java # Immutable data carrier for unpacked dimensions, IV, and ciphertext
└── resources/com/mycompany/mavenproject1/
    └── MainView.fxml                  # FXML UI layout definition
```

---

## Security & Visual Analysis (ECB vs. CBC & CTR)

| Cipher Mode | Padding | IV Required | Visual Security on Images |
| :--- | :--- | :--- | :--- |
| **AES-ECB** | PKCS5Padding | No | **Weak:** Identical 16-byte plaintext pixel blocks produce identical ciphertext blocks, leaving image contours visible. |
| **AES-CBC** | PKCS5Padding | Yes (16 bytes) | **Strong:** Chaining with a random IV diffuses pixel patterns into uniform random noise. |
| **AES-CTR** | NoPadding | Yes (16 bytes) | **Strong:** Counter-based keystream produces uniform random noise with zero ciphertext expansion. |

---

## Getting Started

### Prerequisites
- **JDK 27** (or compatible JDK matching `pom.xml`)
- **Apache Maven 3.8+**

### Build & Run from Source

1. **Clone the repository:**
   ```bash
   git clone https://github.com/YOUR_USERNAME/AES-Image-Encryption-JavaFX.git
   cd AES-Image-Encryption-JavaFX
   ```

2. **Build the standalone executable Fat JAR using Maven:**
   ```bash
   mvn clean package
   ```

3. **Run the packaged application:**
   ```bash
   java -jar target/JavaFXApp-1.0-SNAPSHOT.jar
   ```

---

### Quick Start (Download & Run)
1. Download the latest `JavaFXApp-1.0-SNAPSHOT.jar` from the [Releases](../../releases) page.
2. Make sure **JDK 27** is installed on your machine.
3. Run the application:
   ```bash
   java -jar JavaFXApp-1.0-SNAPSHOT.jar

---
## Usage Workflow

1. **Generate or Load a Key:** Click **Generate Key** to create a new 128-bit AES key (optionally click **Save Key** to store it for later decryption).
2. **Select Cipher Mode:** Choose `ECB`, `CBC`, or `CTR` from the **Mode** dropdown menu.
3. **Encrypt an Image:** Click **Select Image...** to load a plaintext image (`.png`, `.jpg`, `.jpeg`, `.bmp`), then click **Encrypt Image** and save the encrypted result via **Save Output Image...** (exported as lossless `.png`).
4. **Decrypt an Image:** Load the encrypted `.png` file, ensure the matching **Mode** and **AES Key** are set, and click **Decrypt Image** to restore the exact original image.

---

## Screenshots

<!-- Add screenshots of your application here after pushing them to a /screenshots folder -->
<!-- Example: ![Main Interface](screenshots/main_ui.png) -->

---

## Technologies Used

- **Java 27** (`javax.crypto`, `java.awt.image`, `java.nio.ByteBuffer`)
- **JavaFX 27.0.1** (`javafx-controls`, `javafx-fxml`)
- **Apache Maven** (`javafx-maven-plugin`, `maven-shade-plugin`)