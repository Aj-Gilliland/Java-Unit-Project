//stenography imports
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
//encryption imports
import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
//sql imports
import java.sql.*;
import java.util.Base64;


public class Main {
    //encryption
    public static String[] Encrypt(String password) throws InvalidKeySpecException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        char[] newpassword = password.toCharArray();
        //Converts password into a char array as it's more secure
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        int iterationCount = 65536;
        int keyLength = 256;
    /* random generated 16 byte array for salt, num of iterations for key derivation function,
    defines length of derived key in bits, 256 in this case for AES 256 encryption */
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHMACSHA256");
        /* Creates secret key factory instance for key derivation function PBKDF2 using HmacSHA256 as the underlying has function. */
        PBEKeySpec spec = new PBEKeySpec(newpassword, salt, iterationCount, keyLength);
        //Creates specification for the PBKDF2 key derivation function including all the params
        SecretKey secret = skf.generateSecret(spec);
        //Generates a secret key from spec
        SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getEncoded(),"AES");
        //Generates an AES-Specific key from the raw bytes of the generated secret key
        //The above code creates a key from the password
        byte[] ivBytes = new byte[16];
        new SecureRandom().nextBytes(salt);
        //initialize a 16 bit array for initialization Vector
        IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);
        //creates IVParameterSpec object from ivBytes which will initialize the cipher
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
    /*Initializes a cipher instance for AES encryption in CBC mode(Cipher Block Chaining) with PKCS5 Padding
    Note Cipher Block Chaining users the IV for the first block and each subsequent block is stored with the previous ciphertext block/*
     */
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivSpec);
        //initializes cipher in encryption mode with derived AES key and IV
        byte[] encrypted = cipher.doFinal(password.getBytes());
        //encrypts data converting string to byte array and processing with cipher
        String encryptData = Base64.getEncoder().encodeToString(encrypted);
        //encodes encrypted byte array to a Base64 String making it easier to store as plan text
        byte[] iv = ivSpec.getIV();
        String ivString = Base64.getEncoder().encodeToString(iv);
        //Encodes the IvSpec by first getting the bytes which gets converted into a Base64 String
        String encodedKey = Base64.getEncoder().encodeToString(secretKeySpec.getEncoded());
        //Encodes the key into a Base64 String
        String[] data = {ivString, encryptData, encodedKey};
        return data;
    }
    public static void decrypt(String ivSpecString, String encryptData, String encodedKey) throws IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException {
        byte[] ivBytes = Base64.getDecoder().decode(ivSpecString);
        IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);
        //Decodes the ivSpec from the database
        byte[] decodedKey = Base64.getDecoder().decode(encodedKey);
        SecretKeySpec decodedKeySpec = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
        //Decodes the key from the database
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, decodedKeySpec, ivSpec);
        //Re-initialize cipher in decrypt mode with ivspec and key used during encryption
        byte[] original = cipher.doFinal(Base64.getDecoder().decode(encryptData));
        String originalString = new String(original);
        System.out.println("Decryption Succesful your password is " + originalString);
        //Decodes the encryptedData into a base64 string
    }
    //stenography
    public static String decodeImage(String imagePath) {
        try {
            //get image and put on buffers
            BufferedImage img = ImageIO.read(new File(imagePath));
            StringBuilder binaryMessage = new StringBuilder();
            StringBuilder decodedMessage = new StringBuilder();
            //read the LSBs from the image to reconstruct the binary message
            for (int y = 0; y < img.getHeight(); y++) {
                for (int x = 0; x < img.getWidth(); x++) {
                    int color = img.getRGB(x, y);
                    int blue = color & 0xff;
                    int bit = blue & 1; // get LSB of the blue component
                    binaryMessage.append(bit);
                    //every 8 bits, convert binary to character
                    if (binaryMessage.length() == 8) {
                        int charCode = Integer.parseInt(binaryMessage.toString(), 2);
                        //stop if a null character is found
                        if (charCode == 0) {
                            return decodedMessage.toString();
                        }
                        decodedMessage.append((char) charCode);
                        binaryMessage.setLength(0); // Reset for next character
                    }
                }
            }
            return decodedMessage.toString();
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            return null;
        }
    }
    public static void encodeAndSaveImage(String originalImagePath, String message, String outputImagePath) {
        try {
            //get image and put on buffers
            BufferedImage img = ImageIO.read(new File(originalImagePath));
            int messageLength = message.length();
            int[] messageBits = new int[messageLength * 8];
            //convert message to binary
            int messageBitIndex = 0;
            for (char c : message.toCharArray()) {
                for (int i = 7; i >= 0; --i, ++messageBitIndex) {
                    messageBits[messageBitIndex] = (c >> i) & 1;
                }
            }
            //encode message into the image
            int imgIndex = 0;
            for (int y = 0; y < img.getHeight(); y++) {
                for (int x = 0; x < img.getWidth(); x++) {
                    if (imgIndex < messageBits.length) {
                        int color = img.getRGB(x, y);
                        int blue = color & 0xff;
                        //modify the LSB of the blue component
                        blue = (blue & 0xfe) | messageBits[imgIndex++];
                        int newColor = (color & 0xffff00ff) | (blue << 0);
                        img.setRGB(x, y, newColor);
                    }
                }
            }
            //save the modified image
            ImageIO.write(img, "png", new File(outputImagePath));
            System.out.println("Encoding Complete");
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
    //sql and storage
    public static Integer testConnect() {
        try {
            //explicitly load the PostgreSQL JDBC driver class
            Class.forName("org.postgresql.Driver");
            Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/thesafe", "aj", "aj1274414");
            //its a good practice to close the connection
            connection.close();
            System.out.println("Connection successful");
        } catch (SQLException ex) {
            System.out.println("SQLException: " + ex.getMessage());
        } catch (ClassNotFoundException e) {
            System.out.println("ClassNotFoundException: " + e.getMessage());
        }
        return 8;
    }
    public static void main(String[] args) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, InvalidKeySpecException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        System.out.println("Hello World");
        testConnect();
        Encrypt("Kaleigh");
        encodeAndSaveImage("C:/Users/fastc/OneDrive/Desktop/test_folder/mathew.png", "This is a secret message", "C:/Users/fastc/OneDrive/Desktop/test_folder/encoded_mathew.png");
        System.out.println(decodeImage("C:/Users/fastc/OneDrive/Desktop/test_folder/encoded_mathew.png"));;
        System.out.println("The program has finished!!!");
    }

}



