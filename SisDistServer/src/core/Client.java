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
    private final SimpleStringProperty address;
    private final SimpleStringProperty port;
    private final SimpleStringProperty name;
    
    public Client(String address, String port, String name){
        this.address = new SimpleStringProperty(address);
        this.port = new SimpleStringProperty(port);
        this.name = new SimpleStringProperty(name);
    }

    public SimpleStringProperty addressProperty() {
        return address;
    }

    public SimpleStringProperty portProperty() {
        return port;
    }

    public SimpleStringProperty nameProperty() {
        return name;
    }
    
}
