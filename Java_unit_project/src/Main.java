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
import java.util.Objects;
import java.util.Scanner;


public class Main {
    //encryption
    public static String[] Encrypt(String password) throws InvalidKeySpecException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        char[] passwordChars = password.toCharArray();
        //Converts password into a char array as it's more secure
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        // random generated 16 byte array for salt, num of iterations for key derivation function,
        // defines length of derived key in bits, 256 in this case for AES 256 encryption
        SecretKeySpec secretKeySpec = new SecretKeySpec(deriveKeyFromPassword(passwordChars, salt).getEncoded(), "AES");
        //Creates AES specific key from the deriveKeyFromPassword function
        byte[] ivBytes = new byte[16];
        new SecureRandom().nextBytes(ivBytes);
        //initialize a 16 bit array for initialization Vector
        Cipher cipher = initiateCipher(secretKeySpec, new IvParameterSpec(ivBytes), Cipher.ENCRYPT_MODE);
        //initializes cipher in encryption mode with derived AES key and IV
        byte[] encryptedData = cipher.doFinal(password.getBytes());
        //encrypts data converting string to byte array and processing with cipher
        return new String[]{
                Base64.getEncoder().encodeToString(ivBytes),
                Base64.getEncoder().encodeToString(encryptedData),
                Base64.getEncoder().encodeToString(secretKeySpec.getEncoded())
        };
    }

    public static SecretKey deriveKeyFromPassword(char[] password, byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
        int iterationCount = 65536;
        int keyLength = 256;
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHMACSHA256");
        /* Creates secret key factory instance for key derivation function PBKDF2 using HmacSHA256 as the underlying has function. */
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterationCount, keyLength);
        //Creates specification for the PBKDF2 key derivation function including all the params
        return skf.generateSecret(spec);
        //Generate secret key from spec and return
    }
    public static Cipher initiateCipher(SecretKeySpec secretKeySpec, IvParameterSpec iv, int mode) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        //Initializes a cipher instance for AES encryption in CBC mode(Cipher Block Chaining) with PKCS5 Padding
        // Note Cipher Block Chaining users the IV for the first block and each subsequent block is stored with the previous ciphertext block/*
        cipher.init(mode, secretKeySpec, iv);
        return cipher;

    }
    public static String decrypt(String ivSpecString, String encryptData, String encodedKey) throws IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException {
        byte[] ivBytes = Base64.getDecoder().decode(ivSpecString);
        //Decodes the ivSpec from the database
        byte[] decodedKey = Base64.getDecoder().decode(encodedKey);
        SecretKeySpec decodedKeySpec = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
        //Decodes the key from the database
        Cipher cipher = initiateCipher(decodedKeySpec, new IvParameterSpec(ivBytes), Cipher.DECRYPT_MODE);
        //Re-initialize cipher in decrypt mode with ivSpec and key used during encryption
        byte[] original = cipher.doFinal(Base64.getDecoder().decode(encryptData));
        System.out.println(new String(original));
        return new String(original);
        //Decodes the encryptedData into a base64 string and returns it
    }
    //stenography
    public static void encodeAndSaveImage(String originalImagePath, String message, String outputImagePath) {
        try {
            // Load the original image
            BufferedImage img = ImageIO.read(new File(originalImagePath));
            int messageLength = message.length();
            int[] messageBits = new int[messageLength * 8];
            // Convert message to binary
            int messageBitIndex = 0;
            for (char c : message.toCharArray()) {
                for (int i = 7; i >= 0; --i, ++messageBitIndex) {
                    messageBits[messageBitIndex] = (c >> i) & 1;
                }
            }
            // Encode message into the image
            int imgIndex = 0;
            for (int y = 0; y < img.getHeight(); y++) {
                for (int x = 0; x < img.getWidth(); x++) {
                    if (imgIndex < messageBits.length) {
                        int color = img.getRGB(x, y);
                        int blue = color & 0xff;
                        // Modify the LSB of the blue component
                        blue = (blue & 0xfe) | messageBits[imgIndex++];
                        int newColor = (color & 0xffff00ff) | (blue << 0);
                        img.setRGB(x, y, newColor);
                    }
                }
            }
            // Save the modified image
            ImageIO.write(img, "png", new File(outputImagePath));
            System.out.println("Encoding Complete");
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
    //sql and storage
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
        System.out.println("Welcome to Stealth Key");
        testConnect();
        System.out.println("login or register?");
        Scanner scanner = new Scanner(System.in);
        while (true) {
            String logOrReg = scanner.nextLine();
            if (Objects.equals(logOrReg, "login")){
                login();
                break;
            } else if (Objects.equals(logOrReg, "register")) {
                register();
                break;
            }
            else{
                System.out.println("Please enter login or register");
            }
        }



        String[] data = Encrypt("aj");
        decrypt(data[0], data[1], data[2]);

        //encodeAndSaveImage("C:/Users/fastc/OneDrive/Desktop/test_folder/mathew.png", "This is a secret message", "C:/Users/fastc/OneDrive/Desktop/test_folder/encoded_mathew.png");
        //System.out.println("The program has finished!!!");
    }

    public static Boolean checkpassword(String password, String username) throws InvalidAlgorithmParameterException, IllegalBlockSizeException, NoSuchPaddingException, BadPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        String ivSpec = "";
        String encryptedData = "";
        String encodedKey = "";
        //Pull all variables above from database filtered using username that is passed through
        return Objects.equals(password, decrypt(ivSpec, encryptedData, encodedKey));
    }

    public static String[] getPasswords(String username, String action){
        if (Objects.equals(action, "All")){
            //Proceed to return all passwords for that user
            //user_password = getUserpassword
            //while (Objects.equals(checkpassword(user_password, username), false))
            //proceed to make user either quit or enter the correct password

        }
        try {
            //Try to return the password by which equals the title of

        } catch (Exception e) {
            //Inside the catch I think it would be better to put them inside a while loop to ask what is wanted but it may be best to just return them to the main program
            throw new RuntimeException(e);
        }
        //down here we will return a list of passwords
        return new String[]{

        };
    }


    public static void displayPasswords(String[] passwords){
        for (int i = 0; i < passwords.length; i++) {
            System.out.println(i + ". " + passwords[i]);
        }
    }

    public static void login(){

    }

    public static void register(){

    }




}



