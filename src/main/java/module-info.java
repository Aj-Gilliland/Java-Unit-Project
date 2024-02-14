module org.example.javafx_project {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.dlsc.formsfx;

    opens org.example.javafx_project to javafx.fxml;
    exports org.example.javafx_project;
}