package application.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class DashboardController {

    @FXML private Button borrowBookButton;

    @FXML
    private void initialize() {
        borrowBookButton.setOnAction(e -> System.out.println("Borrow button clicked"));
    }
}