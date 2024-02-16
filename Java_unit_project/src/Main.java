
import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.sql.*;
import java.util.Base64;

public class Main {
    private static final String URL = "jdbc:postgresql://localhost:5432/thesafe";
    private static final String USER = "aj";
    private static final String PASSWORD = "aj1274414";

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
    //secret file for local storage //for the time being consider this nixed
    public static void makeHiddenFile (){
        try {
            String desktopPath = System.getProperty("user.home") + File.separator + "OneDrive" + File.separator + "Desktop" + File.separator;
            String invisibleDirName = "\u00A0"; //inviseble file name unicode char
            String fullPath = desktopPath + invisibleDirName;
            File dir = new File(fullPath);//\VvV/\VvV/\VvV/_helps make invise file_\VvV/\VvV/\VvV/
            boolean created = dir.mkdirs(); //use mkdirs() to create the directory (and any necessary parent directories so DONT CHANGE THE FILE PATH)
            Path path = Paths.get(fullPath);//makes Path obj to use in setAtt...
            Files.setAttribute(path, "dos:hidden", true);//hides invisible file
        } catch (IOException e){
            System.out.println(e);
        }
    }
    //stenography
    public static byte[] encodeImage(String originalImagePath, String message) {
        try {
            BufferedImage img = ImageIO.read(new File(originalImagePath));
            int imageWidth = img.getWidth();
            int imageHeight = img.getHeight();
            int[] messageBits = new int[message.length() * 8 + 8];
            //convert message to binary
            int messageBitIndex = 0;
            for (char c : message.toCharArray()) {
                for (int i = 7; i >= 0; --i, ++messageBitIndex) {
                    messageBits[messageBitIndex] = (c >> i) & 1;
                }
            }
            //add a terminating character to the message (this is how ill identify the end of the message)
            for (int i = 7; i >= 0; --i, ++messageBitIndex) {
                messageBits[messageBitIndex] = 0;
            }
            if (messageBitIndex > imageWidth * imageHeight) {
                System.err.println("You cant fit that message into that picture!!!");
                return null;
            }
            //encode message into the image
            int bitIndex = 0;
            for (int i = 0; i < imageHeight && bitIndex < messageBitIndex; i++) {
                for (int x = 0; x < imageWidth && bitIndex < messageBitIndex; x++) {
                    int color = img.getRGB(x, i);
                    int blue = color & 0xff;
                    int newBlue = (blue & 0xfe) | messageBits[bitIndex]; //change LSB of blue channel
                    int newColor = (color & 0xffff00ff) | (newBlue << 0);
                    img.setRGB(x, i, newColor);
                    bitIndex++;
                }
            }
            //convert the BufferedImage to a byte array
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            if (ImageIO.write(img, "png", baos)) {
                baos.flush();
                return baos.toByteArray();
            } else {
                System.err.println("Could not write image using ImageIO");
                return null;
            }
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            return null;
        }
    }
    public static String decodeImage(byte[] imageData) {
        try {
            //convert byte array into a BufferedImage
            BufferedImage img = ImageIO.read(new ByteArrayInputStream(imageData));
            StringBuilder binaryMessage = new StringBuilder();
            StringBuilder decodedMessage = new StringBuilder();
            //decode the LSBs from the image to reconstruct the binary message
            for (int y = 0; y < img.getHeight(); y++) {
                for (int x = 0; x < img.getWidth(); x++) {
                    int color = img.getRGB(x, y);
                    int blue = color & 0xff;
                    int bit = blue & 1; //extract LSB of the blue component
                    binaryMessage.append(bit);
                    //convert every 8 bits into a character
                    if (binaryMessage.length() == 8) {
                        int charCode = Integer.parseInt(binaryMessage.toString(), 2);
                        //stop decoding if a null character (end of message marker) is found
                        if (charCode == 0) {
                            return decodedMessage.toString();
                        }
                        decodedMessage.append((char) charCode);
                        binaryMessage.setLength(0); //reset for the next character
                    }
                }
            }
            return decodedMessage.toString();
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            return null;
        }
    }
    //vvvvv_these image getter helper functions are only for the master password_vvvvv
    public static void storeImageAtId(byte[] imageBytes, int id) {
        String sql = "UPDATE person SET master_image = ? WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setBytes(1, imageBytes);
            pstmt.setInt(2, id);
            pstmt.executeUpdate();
            System.out.println("Image stored successfully for person: " + id);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    public static byte[] retrieveImageById(int id) {
        String sql = "SELECT master_image FROM person WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getBytes("master_image");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }
    //vvvvv_creates thing else_vvvvv
    public static void createPerson(String username, String masterIvspec, String masterKey, byte[] masterImage) {
        //SQL INSERT statement
        String sql = "INSERT INTO person (username, master_ivspec, master_key, master_image) VALUES (?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            //set parameters for the prepared statement
            pstmt.setString(1, username);
            pstmt.setString(2, masterIvspec);
            pstmt.setString(3, masterKey);
            pstmt.setBytes(4, masterImage);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("A new person was inserted successfully.");
            } else {
                System.out.println("A new person could not be inserted.");
            }
        } catch (SQLException e) {
            System.err.println("SQL exception occurred: " + e.getMessage());
        }
    }
    public static void createPassword(String title, String ivspec, String key, byte[] image, int person_id) {
        //SQL INSERT statement
        String sql = "INSERT INTO password (title, ivspec, key, image, person_id) VALUES (?, ?, ?, ?,?)";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            //set parameters for the prepared statement
            pstmt.setString(1, title);
            pstmt.setString(2, ivspec);
            pstmt.setString(3, key);
            pstmt.setBytes(4, image);
            pstmt.setInt(5,person_id);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("A new password was inserted successfully.");
            } else {
                System.out.println("A new password could not be inserted.");
            }
        } catch (SQLException e) {
            System.err.println("SQL exception occurred: " + e.getMessage());
        }
    }

        //sql and storage
//    public static String[] queryPasswordByUsername(usernameInput) {
//        Connection connection = null;
//        Statement stmt = null;
//        ResultSet rs = null;
//        try {
//            //explicitly load the PostgreSQL JDBC driver class
//            Class.forName("org.postgresql.Driver");
//            //establish a connection to the database
//            connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/thesafe", "aj", "aj1274414");
//            //create a statement object to execute the query
//            stmt = connection.createStatement();
//            //execute a SQL query and retrieve the result set
//            String sql = "SELECT username FROM person WHERE username = "+usernameInput;
//            rs = stmt.executeQuery(sql);
//            //process the result set
//            while (rs.next()) {//built in method that evalutes false once you see all the data with in
//                String username = rs.getString("username");
//                String ivspec = rs.getString("master_ivspec");
//                String key = rs.getString("master_key");
//                String image = rs.getString("master_image");
//                System.out.println(ivspec);
//                System.out.println(key);
//                System.out.println(image);
//                //return {ivspec,key,image_content}
//            }
//        } catch (SQLException ex) {//phat validation
//            System.out.println("SQLException: " + ex.getMessage());
//        } catch (ClassNotFoundException e) {
//            System.out.println("ClassNotFoundException: " + e.getMessage());
//        } finally {//closes resources in the end no matter what
//            try {
//                if (rs != null) rs.close();
//                if (stmt != null) stmt.close();
//                if (connection != null) connection.close();
//            } catch (SQLException ex) {
//                System.out.println("SQLException on closing: " + ex.getMessage());
//            }
//        }
//    }









    public static void main(String[] args) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, InvalidKeySpecException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        System.out.println("Thank you for choosing Stealth Key");
        //storeImageAtId(encodeImage("C:\\Users\\fastc\\OneDrive\\Desktop\\mathew.png","message goes here"),2);
        //System.out.println(decodeImage(retrieveImageById(2)));
        createPerson("AJRocks","This is the first test of ivspec in the db","This is the first test of encryption keys in the db",encodeImage("C:\\Users\\fastc\\OneDrive\\Desktop\\mathew.png","message goes here"));
        System.out.println("The program has finished!!!");
    }

}



