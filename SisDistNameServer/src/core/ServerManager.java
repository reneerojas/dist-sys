/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package core;

//for no sync or overhead in threadsafe
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 *
 * @author Reneé Rojas
 */
public class ServerManager {

    private final HashMap<SocketChannel, Server> serverHash;
    private final HashSet<String> serverNames;
    private final ObservableList<Server> serverList;
    private final HashMap<SocketChannel, List<byte[]>> dataQueue;

    public ServerManager() {
        serverHash = new HashMap<>();
        serverNames = new HashSet<>();
        serverList = FXCollections.observableList(new LinkedList<Server>());
        dataQueue = new HashMap<>();
    }

    /**
     * Adiciona Servidor
     *
     * @param channel
     * @param serv
     * @return boolean
     */
    public boolean add(SocketChannel channel, Server serv) {
        if (!serverHash.containsKey(channel)) {
            serverNames.add(serv.nameProperty().get());
            //adiciona server na hashtable
            serverHash.put(channel, serv);
            //adiciona na lista
            serverList.add(serv);
            //cria uma fila de dados
            dataQueue.put(channel, new ArrayList<>());
            return true;
        }
        return false;
    }

    /**
     * Remove Servidor
     *
     * @param channel
     * @return
     */
    public boolean remove(SocketChannel channel) {
        if (serverHash.containsKey(channel)) {
            serverNames.remove(serverHash.get(channel).nameProperty().get());
            serverList.remove(serverHash.get(channel));
            serverHash.remove(channel);
            return true;
        }
        return false;
    }

    /**
     * Encontra servidor pelo socket
     *
     * @param channel
     * @return
     */
    public Server find(SocketChannel channel) {
        if (serverHash.containsKey(channel)) {
            return serverHash.get(channel);
        }
        return null;
    }

    /**
     * Valida se o nome para o servidor está disponivel
     *
     * @param name
     * @return
     */
    public boolean avaiableName(String name) {
        if (serverNames.contains(name)) {
            return false;
        }
        return true;
    }

    /**
     * Armazena dados para serem transmitidos
     *
     * @param channel
     * @param data
     */
    public void queueData(SocketChannel channel, byte[] data) {
        if (dataQueue.containsKey(channel)) {
            dataQueue.get(channel).add(data);
        }
    }

    public List<byte[]> getQueuedData(SocketChannel channel) {
        return dataQueue.get(channel);
    }

    public void clearQueue(SocketChannel channel) {
        dataQueue.get(channel).clear();
    }

    public HashMap<SocketChannel, Server> getServerHash() {
        return serverHash;
    }

    public ObservableList<Server> getServerList() {
        return serverList;
    }

    public void sortFreestServer() {
        Platform.runLater(() -> {
            FXCollections.sort(serverList);
        });
    }
}
