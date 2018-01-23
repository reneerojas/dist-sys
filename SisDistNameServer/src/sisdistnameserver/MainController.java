/**
 * Sample Skeleton for 'main.fxml' Controller Class
 */
package sisdistnameserver;

import com.ConnectionManager;
import core.Server;
import core.ServerManager;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class MainController {

    private ServerManager serverManager;

    @FXML // ResourceBundle that was given to the FXMLLoader
    private ResourceBundle resources;

    @FXML // URL location of the FXML file that was given to the FXMLLoader
    private URL location;

    @FXML // fx:id="serversTable"
    private TableView<Server> serversTable; // Value injected by FXMLLoader

    @FXML // This method is called by the FXMLLoader when initialization is complete
    void initialize() {
        assert serversTable != null : "fx:id=\"serversTable\" was not injected: check your FXML file 'main.fxml'.";
        System.out.println("teste");

        //Acessa a instancia do servidor de nomes
        serverManager = ConnectionManager.getInstance().getServerManagerInstance();

        final TableColumn ip = (TableColumn) serversTable.getColumns().get(0);
        final TableColumn porta = (TableColumn) serversTable.getColumns().get(1);
        final TableColumn nome = (TableColumn) serversTable.getColumns().get(2);
        final TableColumn clientes = (TableColumn) serversTable.getColumns().get(3);
        ip.setCellValueFactory(new PropertyValueFactory("address"));
        porta.setCellValueFactory(new PropertyValueFactory("port"));
        nome.setCellValueFactory(new PropertyValueFactory("name"));
        clientes.setCellValueFactory(new PropertyValueFactory("clients"));
        clientes.sortNodeProperty();

        serversTable.setItems(serverManager.getServerList());
        
       
//        serversTable.getItems().addListener(new ListChangeListener<Server>() {
//
//            @Override
//            public void onChanged(ListChangeListener.Change<? extends Server> c) {
//                Platform.runLater(() -> {
//                    serversTable.sort();
//                    System.out.println("sort!");
//                });
//            }
//        });
    }
}
