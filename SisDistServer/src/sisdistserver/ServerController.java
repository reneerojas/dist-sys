/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sisdistserver;

import com.ComExecutor;
import com.ConnectionManager;
import core.DynamicCompiler;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

/**
 * FXML Controller class
 *
 * @author renee
 */
public class ServerController implements Initializable {

    @FXML
    private Label lbServerName;

    @FXML
    private TextField inputServerAddr;

    @FXML
    void connectToServer(ActionEvent event) {
        try {
            ComExecutor.getInstance().start();
            ComExecutor.getInstance().getExecutor().execute(ConnectionManager.getInstance().newNameServerInstance(inputServerAddr.getText()));
        } catch (IOException ex) {
            Logger.getLogger(SisDistServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Initializes the controller class.
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        lbServerName.textProperty().bind(ConnectionManager.getInstance().getServerName());

//        DynamicCompiler dynComp = DynamicCompiler.getInstance();
//        dynComp.test();
    }

}
