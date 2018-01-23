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
public class DynamicClass {
    private final SimpleStringProperty name;
    private final SimpleStringProperty code;
    private final SimpleIntegerProperty version;
    
    public DynamicClass(String nome, int versao, String codigo){
        name = new SimpleStringProperty(nome);
        code = new SimpleStringProperty(codigo);
        version = new SimpleIntegerProperty(versao);
    }

    public SimpleStringProperty nameProperty() {
        return name;
    }

    public SimpleStringProperty codeProperty() {
        return code;
    }

    public SimpleIntegerProperty versionProperty() {
        return version;
    }
    
    public String getName(){
        return name.get();
    }
    
    public int getVersion(){
        return version.get();
    }
    
    
}
