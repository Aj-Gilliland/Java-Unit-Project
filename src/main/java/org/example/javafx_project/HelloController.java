package org.example.javafx_project;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class HelloController {

    @FXML
    private Button clickMeButton;

    @FXML
    private void handleClick() {
        System.out.println("Button clicked");
        // Call your existing methods here
        try {
            // Example: Encrypting a password
            String password = "secret";
            String[] encryptedData = Main.Encrypt(password);
            System.out.println("Encrypted password: " + encryptedData[1]);

            // Example: Decrypting a password
            Main.decrypt(encryptedData[0], encryptedData[1], encryptedData[2]);

            // Example: Encoding and saving image
            Main.encodeAndSaveImage("original.png", "Secret message", "output.png");

            // Example: Decoding image
            String decodedMessage = Main.decodeImage("output.png");
            System.out.println("Decoded message: " + decodedMessage);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}