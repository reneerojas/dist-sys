/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package core;

import javafx.beans.property.SimpleStringProperty;

/**
 *
 * @author renee
 */
public class Client {
    private final SimpleStringProperty serverAddress;
    private final SimpleStringProperty serverPort;
    private final SimpleStringProperty name;
    
    private Client() {
        this.serverAddress = new SimpleStringProperty();
        this.serverPort = new SimpleStringProperty();
        this.name = new SimpleStringProperty();
    }
    
    public static Client getInstance() {
        return ClientHolder.INSTANCE;
    }
    
    private static class ClientHolder {

        private static final Client INSTANCE = new Client();
    }
    
     public SimpleStringProperty serverAddressProperty() {
        return serverAddress;
    }

    public SimpleStringProperty serverPortProperty() {
        return serverPort;
    }

    public SimpleStringProperty nameProperty() {
        return name;
    }
}
