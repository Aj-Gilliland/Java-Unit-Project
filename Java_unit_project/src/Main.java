import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.sql.*;
import java.util.Base64;


public class Main {

    public static Integer testConnect() {
        try {
            // Explicitly load the PostgreSQL JDBC driver class
            Class.forName("org.postgresql.Driver");
            Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/thesafe", "aj", "aj1274414");
            // It's a good practice to close the connection
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
        System.out.println("The program has finished!!!");
        Encrypt("Kaleigh");
    }

    public static void Encrypt(String password) throws InvalidKeySpecException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        char[] newpassword = password.toCharArray();
        //Converts password into a char array as it's more secure
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        int iterationCount = 65536;
        int keyLength = 256;
        /* random generated 16 byte array for salt, num of iterations for key derivation function,
        defines length of derived key in bits, 256 in this case for AES 256 encryption */
        SecretKeyFactory skf;
        try {
            skf = SecretKeyFactory.getInstance("PBKDF2WithHMACSHA256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
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
        System.out.println("IvSpec: " + ivSpec);
        System.out.println("EncryptedData: " + encryptData);
        System.out.println("BaseKey: " + secretKeySpec);
        //Just testing
        String encodedKey = Base64.getEncoder().encodeToString(secretKeySpec.getEncoded());
        System.out.println("EncodedKey: " + encodedKey);
        //Encodes the key
        decrypt(ivSpec, encryptData, encodedKey);
    }
    public static void decrypt(javax.crypto.spec.IvParameterSpec ivSpec, java.lang.String encryptData, String encodedKey) throws IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException {
        byte[] decodedKey = Base64.getDecoder().decode(encodedKey);
        SecretKeySpec decodedKeySpec = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
        System.out.println("DecodedKey: " + decodedKeySpec);
        //Decodes the key from the database
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, decodedKeySpec, ivSpec);
        //Re-initialize cipher in decrypt mode with ivspec and key used during encryption
        byte[] original = cipher.doFinal(Base64.getDecoder().decode(encryptData));
        System.out.println("DecryptedData in bytes: " + original);
        String originalString = new String(original);
        //Decodes the encryptedData into a base64 string
        System.out.println("Data as a string: " + originalString);
    }


}



