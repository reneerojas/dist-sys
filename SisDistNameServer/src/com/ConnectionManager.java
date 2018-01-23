/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com;

import core.ServerManager;

/**
 *
 * @author renee
 */
public class ConnectionManager {

    private NameServer nameServer;
    private ServerManager serverManager;

    private ConnectionManager() {
    }

    public static ConnectionManager getInstance() {
        return ConnectionManagerHolder.INSTANCE;
    }

    private static class ConnectionManagerHolder {

        private static final ConnectionManager INSTANCE = new ConnectionManager();
    }

    public ServerManager getServerManagerInstance() {
        if (serverManager == null) {
            serverManager = new ServerManager();
        }
        return serverManager;
    }

    public NameServer getNameServerInstance() {
        if (nameServer == null) {
            nameServer = new NameServer();
            ComExecutor.getInstance().getExecutor().execute(() -> {
                nameServer.run();
            });
        }
        return nameServer;
    }
}
