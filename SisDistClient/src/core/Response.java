/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package core;

import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 *
 * @author UFOP
 */
public class Response
{
    private SimpleBooleanProperty wait;
    private SimpleStringProperty str;

    private Response()
    {
        str = new SimpleStringProperty("");
        wait = new SimpleBooleanProperty(true);
    }

    public static Response getInstance()
    {
        return ResponseHolder.INSTANCE;
    }

    private static class ResponseHolder
    {

        private static final Response INSTANCE = new Response();
    }

    public void update(String string)
    {
        Platform.runLater(() ->
        {
            if(!str.get().equals(""))
                str.set(str.get() + "\n" + string);
            else
                str.set(string);
        });
    }
    
    public void setWait(final boolean bool)
    {
        Platform.runLater(() ->
        {
            wait.set(bool);
        }); 
    }
    
    public SimpleStringProperty responseProperty()
    {
        return str;
    }
    
    public SimpleBooleanProperty waitProperty()
    {
        return wait;
    }
}
