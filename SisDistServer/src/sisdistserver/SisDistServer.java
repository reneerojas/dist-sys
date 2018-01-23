/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sisdistserver;

import com.ComExecutor;
import com.ConnectionManager;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 *
 * @author renee
 */
public class SisDistServer extends Application {

    private Stage stage;

    /**
     * Singleton instance
     *
     * @return
     */
    public static SisDistServer getInstance() {
        return SisDistServerHolder.INSTANCE;
    }

    private static class SisDistServerHolder {

        private static final SisDistServer INSTANCE = new SisDistServer();
    }

    @Override
    public void start(Stage primaryStage) {
        stage = primaryStage;
        ConnectionManager.getInstance().getServerInstance();

        try {
            Parent page = (Parent) FXMLLoader.load(SisDistServer.class.getResource("server.fxml"));
            Scene scene = new Scene(page, 600, 400);

            stage.setTitle("Server");
            stage.setScene(scene);
            stage.show();
            closeActions();
        } catch (IOException ex) {
            Logger.getLogger(SisDistServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        

    }

    private void closeActions() {
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                try {
                    ConnectionManager.getInstance().getServerInstance().stop();
                    ComExecutor.getInstance().getExecutor().shutdownNow();
                    Platform.exit();
                } catch (IOException ex) {
                    Logger.getLogger(SisDistServer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}
