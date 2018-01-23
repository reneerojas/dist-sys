/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package core;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 *
 * @author renee
 */
public class Server implements Comparable<Server>{
    private final SimpleStringProperty address;
    private final SimpleIntegerProperty port;
    private final SimpleStringProperty name;
    private final SimpleIntegerProperty clients;
    
    public Server(String name, String addr, Integer port){
        this.address = new SimpleStringProperty(addr);
        this.port = new SimpleIntegerProperty(port);
        this.name = new SimpleStringProperty(name);
        this.clients = new SimpleIntegerProperty(0);
    }

    public SimpleStringProperty addressProperty() {
        return address;
    }

    public SimpleIntegerProperty portProperty() {
        return port;
    }

    public SimpleStringProperty nameProperty() {
        return name;
    }
    

    public SimpleIntegerProperty clientsProperty() {
        return clients;
    }

    @Override
    public int compareTo(Server o) {
       if (this.clients.get() < o.clientsProperty().get()){
           return -1;
       } 
       if(this.clients.get() > o.clientsProperty().get()){
           return 1;
       } 
       return 0;
    }
    
}
