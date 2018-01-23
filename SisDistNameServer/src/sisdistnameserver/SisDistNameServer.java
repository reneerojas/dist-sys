/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sisdistnameserver;

import com.ComExecutor;
import com.ConnectionManager;
import com.NameServer;
import core.cache.DynamicClassManager;
import core.cache.DynamicResultsCache;
import core.log.Log;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
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
public class SisDistNameServer extends Application {

    private DynamicResultsCache dynCache;
    private DynamicClassManager dynClass;
    private NameServer nameServer;
    private Stage stage;
    private ComExecutor executor;
    private Log log;

    /**
     * Singleton instance
     *
     * @return
     */
    public static SisDistNameServer getInstance() {
        return SisDistNameServerHolder.INSTANCE;
    }

    private static class SisDistNameServerHolder {

        private static final SisDistNameServer INSTANCE = new SisDistNameServer();
    }

    public SisDistNameServer() {
        executor = ComExecutor.getInstance();
        //inicia logs
        log = Log.getInstance();
        dynCache = DynamicResultsCache.getInstance();
        dynClass = DynamicClassManager.getInstance();
    }

    @Override
    public void start(Stage primaryStage) {
        stage = primaryStage;
        closeActions();
        System.out.println("start");
        try {
            Parent page = (Parent) FXMLLoader.load(SisDistNameServer.class.getResource("main.fxml"));

            Scene scene = new Scene(page, 600, 400);

            stage.setTitle("Name Server");
            stage.setScene(scene);
            stage.show();
            nameServer = ConnectionManager.getInstance().getNameServerInstance();

            closeActions();
        } catch (IOException ex) {
            System.out.println("err");
            Logger.getLogger(SisDistNameServer.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void closeActions() {
        stage.setOnCloseRequest((WindowEvent event) -> {
            try {
                log.close();
                executor.getExecutor().shutdown();
                ConnectionManager.getInstance().getNameServerInstance().stop();
                Platform.exit();
            } catch (IOException ex) {
                Logger.getLogger(SisDistNameServer.class.getName()).log(Level.SEVERE, null, ex);
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
