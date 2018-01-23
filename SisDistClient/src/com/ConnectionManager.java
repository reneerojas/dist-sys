/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 *
 * @author renee
 */
public class ConnectionManager
{

    private SimpleIntegerProperty conStatus;
    private SimpleStringProperty conMessage;
    private SimpleBooleanProperty close;
    private Connect connect;

    private ConnectionManager()
    {
        conStatus = new SimpleIntegerProperty(0);
        conMessage = new SimpleStringProperty();
        close = new SimpleBooleanProperty(false);
    }

    public static ConnectionManager getInstance()
    {
        return ConnectionManagerHolder.INSTANCE;
    }

    private static class ConnectionManagerHolder
    {

        private static final ConnectionManager INSTANCE = new ConnectionManager();
    }

    public SimpleBooleanProperty getClosing()
    {
        return close;
    }

    public void setClosing()
    {
        close.set(true);
    }

    /**
     * 0 - nao conectado 1 - conexao iniciada 2 - buscando servidores 3 -
     * conectado com servidor
     *
     * @return
     */
    public SimpleIntegerProperty getStatus()
    {
        return conStatus;
    }

    public SimpleStringProperty getMessage()
    {
        return conMessage;
    }

    public Connect getConnectInstance()
    {
        return connect;
    }

    public Connect newConnection(final String con)
    {
        try
        {
            if (connect != null)
            {
                connect.endConnection();
            }
            connect = new Connect(con);
        } catch (IOException ex)
        {
            Logger.getLogger(ConnectionManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return connect;
    }

    public Connect getConnectInstance(final String con)
    {
        if (connect == null)
        {
            try
            {
                connect = new Connect(con);
            } catch (IOException ex)
            {
                Logger.getLogger(ConnectionManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return connect;
    }
}
