/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com;

import core.ClientManager;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import lib.json.JsonObject;

/**
 *
 * @author renee
 */
public class ConnectionManager {

    private ConnectNameServer nameServer;
    private Server server;
    private SimpleStringProperty serverName;
    private int serverPort;
    private SimpleIntegerProperty connectedClients;
    private ClientManager clientManager;
    private HashMap<Integer, SocketChannel> requisicoes;
    private int requestNumber = 0;

    private ConnectionManager() {
        serverName = new SimpleStringProperty("Servidor Distribuido");
        connectedClients = new SimpleIntegerProperty(0);

        requisicoes = new HashMap<>();
    }

    public static ConnectionManager getInstance() {
        return ConnectionManagerHolder.INSTANCE;
    }

    private static class ConnectionManagerHolder {

        private static final ConnectionManager INSTANCE = new ConnectionManager();
    }

    public ClientManager getClientManagerInstance() {
        if (clientManager == null) {
            clientManager = new ClientManager();
        }
        return clientManager;

    }

    public ConnectNameServer newNameServerInstance(final String ip) throws IOException {
        if (nameServer == null) {
            nameServer = new ConnectNameServer(ip);
        }
        return nameServer;
    }
    
    public ConnectNameServer getNameServerInstance(){
        return nameServer;
    }

    public Server getServerInstance() {
        if (server == null) {
            server = new Server();
            ComExecutor.getInstance().start();
            ComExecutor.getInstance().getExecutor().execute(() -> {
                server.run();
            });
        }
        return server;
    }

    /**
     *
     * @param port
     */
    public void setServerPort(int port) {
        serverPort = port;
    }

    public int getServerPort() {
        return serverPort;
    }

    /**
     *
     * @param name
     */
    public void setServerName(final String name) {
        Platform.runLater(() -> {
            serverName.set(name);
            System.out.println(name);
        });
    }

    public SimpleStringProperty getServerName() {
        return serverName;
    }

    /**
     *
     * @param clients
     */
    public void updateConnectedClients() {
        Platform.runLater(() -> {
            connectedClients.set(getClientManagerInstance().getClientNumber());
        });
    }

    public SimpleIntegerProperty getConnectedClients() {
        return connectedClients;
    }

    public int insertReq(SocketChannel sc) {
        requestNumber++;
        requisicoes.put(requestNumber, sc);
        return requestNumber;
    }

    public SocketChannel getRequest(int req){
        return requisicoes.get(req);
    }
    
    public void removeReq(int req) {
        requisicoes.remove(req);
    }
}
