package com.group8.csit228capstone;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

public class AdminController {
    @FXML
    private TextField textTitle;
    @FXML
    private TextField textDescription;
    @FXML
    private TextField textDate;
    @FXML
    private TextField textLocation;
    @FXML
    private TextField textTotalSeats;
    @FXML
    private Button btnSave;
    @FXML
    private Button btnUpdate;
    @FXML
    private Button btnDelete;
    @FXML
    private Button btnClear;

    @FXML
    public void handleSave(ActionEvent actionEvent) {
        System.out.println("Details Saved");
    }

    @FXML
    public void handleUpdate(ActionEvent actionEvent) {
    }

    @FXML
    public void handleDelete(ActionEvent actionEvent) {
    }

    @FXML
    public void handleClear(ActionEvent actionEvent) {
        textTitle.clear();
        textDate.clear();
        textDescription.clear();
        textLocation.clear();
        textTotalSeats.clear();
    }
}
