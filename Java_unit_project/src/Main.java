
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
import java.util.Objects;
import java.util.Scanner;

public class Main {
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
    private static final String URL = "jdbc:postgresql://localhost:5432/thesafe";
    private static final String USER = "aj";
    private static final String PASSWORD = "aj1274414";

    //encryption
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
    //stenography below
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
            //tracks current index of the messageBits array
            int imgIndex = 0;
            //loops through the y (rows) of px's
            for (int y = 0; y < img.getHeight(); y++) {
                //loops through the x (columns) of px's
                for (int x = 0; x < img.getWidth(); x++) {
                    //makes sure the correct bits are being selected
                    if (imgIndex < messageBits.length) {
                        //gets that pixels color and isolates the blue section
                        int color = img.getRGB(x, y);
                        int blue = color & 0xff;
                        //modify the LSB (least signifigant bit) of the blue component
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
    //creates the sql entrys vvvv
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
    public static Boolean checkpassword(String password, String username) throws InvalidAlgorithmParameterException, IllegalBlockSizeException, NoSuchPaddingException, BadPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        try{
            //Note the variables below must be correct and if a value cannot be returned ensure to return false before attempting the decrypt function
            String ivSpec = "";
            String encryptedData = "";
            //Note the encrypted Data is stored inside the image
            String encodedKey = "";
        } catch (Exception e){
            e.printStackTrace();
        }


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
        return new String[];
    }

    public static void displayPasswords(String[] passwords){
        for (int i = 0; i < passwords.length; i++) {
            System.out.println(i + ". " + passwords[i]);
        }
    }

    public static void addPassword(){

    }



    public static String login() throws InvalidAlgorithmParameterException, IllegalBlockSizeException, NoSuchPaddingException, BadPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("Enter username: ");
            String username = scanner.nextLine();
            System.out.println("Enter password: ");
            String password = scanner.nextLine();
            boolean loggedIn = checkpassword(password, username);
            if (loggedIn) {
                return username;
            }
        }
    }

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









    public static Object register() throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, InvalidKeySpecException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Please enter username: ");
        String username = scanner.nextLine();

        System.out.println("Please enter password: ");
        String password = scanner.nextLine();
        String[] data = Encrypt(password);

        System.out.println("Please enter an image path: ");
        String imagePath = scanner.nextLine();

        encodeAndSaveImage(imagePath, data[1], "imagePath");
        //data[1] has the encrypted data

    }

    public static void main(String[] args) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, InvalidKeySpecException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        System.out.println("Welcome to Stealth Key");
        testConnect();
        System.out.println("login or register?");
        Scanner scanner = new Scanner(System.in);
        while (true) {
            String logOrReg = scanner.nextLine();
            if (Objects.equals(logOrReg, "login")){
                String username = login();
                System.out.println("Please enter correct username and password");
            } else if (Objects.equals(logOrReg, "register")) {
                register();
                break;
            }
            else{
                System.out.println("Please enter login or register");
            }
        }

        boolean a = true;
        while (a) {
            System.out.println("[all], [add], [update]");
            String option = scanner.nextLine();
            a = switch (option) {
                case "all" ->
                    //displayPasswords();
                        true;
                case "add" ->
                    //addPassword();
                        true;
                case "update" ->
                    //updatePassword();
                        true;
                case "quit" ->
                    false;
                default -> true;
            };
        }

        createPerson("AJRocks","This is the first test of ivspec in the db","This is the first test of encryption keys in the db",encodeImage("C:\\Users\\fastc\\OneDrive\\Desktop\\mathew.png","message goes here"));
        String[] data = Encrypt("aj");
        decrypt(data[0], data[1], data[2]);

        //encodeAndSaveImage("C:/Users/fastc/OneDrive/Desktop/test_folder/mathew.png", "This is a secret message", "C:/Users/fastc/OneDrive/Desktop/test_folder/encoded_mathew.png");
        //System.out.println("The program has finished!!!");
    }


}



