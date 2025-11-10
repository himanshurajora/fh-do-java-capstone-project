package application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class LibraryApplication extends Application {
    
    private DashboardController controller;
    
    @Override
    public void start(Stage primaryStage) {
        try {
            Logger.logSystem("INFO", "Starting Library Application UI");
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource("LibraryDashboard.fxml"));
            Parent root = loader.load();
            controller = loader.getController();
            
            Scene scene = new Scene(root);
            
            primaryStage.setTitle("Automated Library Storage System");
            primaryStage.setScene(scene);
            primaryStage.setOnCloseRequest(e -> {
                if (controller != null) {
                    controller.shutdown();
                }
                Logger.logSystem("INFO", "Application closed");
            });
            
            primaryStage.show();
            
            Logger.logSystem("INFO", "Application UI started successfully");
            
        } catch (Exception e) {
            e.printStackTrace();
            Logger.logSystem("ERROR", "Failed to start application: " + e.getMessage());
        }
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}

