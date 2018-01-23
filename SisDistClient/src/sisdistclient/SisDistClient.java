/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sisdistclient;

import com.ComExecutor;
import com.ConnectionManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 *
 * @author renee
 */
public class SisDistClient extends Application {

    private Stage stage;

    @Override
    public void start(Stage primStage) throws Exception {
        stage = primStage;

        Parent root = FXMLLoader.load(getClass().getResource("client.fxml"));

        Scene scene = new Scene(root);

        stage.setScene(scene);
        stage.show();
        closeActions();
    }

    private void closeActions() {
        stage.setOnCloseRequest((WindowEvent event) -> {
            ComExecutor.getInstance().getExecutor().shutdownNow();
            ConnectionManager.getInstance().setClosing();
            Platform.exit();
        });
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}
