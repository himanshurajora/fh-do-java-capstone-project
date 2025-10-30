package application;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class MainController {

    @FXML private TableView<LogEntry> logTable;
    @FXML private TableColumn<LogEntry, String> colTimestamp;
    @FXML private TableColumn<LogEntry, String> colTask;

    @FXML private Button slot1;
    @FXML private Button slot2;
    @FXML private Button slot3;
    @FXML private Button slot4;
    @FXML private Button slot5;
    @FXML private Button slot6;
    @FXML private Button slot7;
    @FXML private Button slot8;
    @FXML private Button slot9;
    @FXML private Button slot10;

    @FXML
    private void initialize() {
       
        colTimestamp.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        colTask.setCellValueFactory(new PropertyValueFactory<>("task"));

      
        ObservableList<LogEntry> logs = FXCollections.observableArrayList(
                new LogEntry("11:23 AM", "Retrieve '1988'"),
                new LogEntry("11:20 AM", "Store 'The Secret'"),
                new LogEntry("11:17 AM", "Charge Equipment"),
                new LogEntry("11:12 AM", "Store 'The Variables'")
        );

        logTable.setItems(logs);

        
        slot7.getStyleClass().add("active-slot");
    }
}
