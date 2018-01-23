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
import java.util.LinkedList;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 *
 * @author renee
 */
public class ClientManager {

    private final HashMap<SocketChannel, Client> clientHash;
    private final ObservableList<Client> clientList;
    private final HashMap<SocketChannel, List<byte[]>> dataQueue;


    public ClientManager() {
        clientHash = new HashMap<>();
        clientList = FXCollections.observableList(new LinkedList<>());
        dataQueue = new HashMap<>();
    }

    /**
     * Adiciona Clientes
     * @param channel
     * @param cli
     * @return boolean
     */
    public boolean add(SocketChannel channel, Client cli) {
        if (!clientHash.containsKey(channel)) {
            // Adiciona cliente na hash
            clientHash.put(channel, cli);
            // Adiciona cliente na lista para a tableview
            clientList.add(cli);
            // Cria uma queue de dados
            dataQueue.put(channel, new ArrayList<>());
            return true;
        }
        return false;
    }

    /**
     * Remove Cliente
     * @param channel
     * @return 
     */
    public boolean remove(SocketChannel channel) {
        if (clientHash.containsKey(channel)) {
            // Remove o cliente da tableview
            clientList.remove(clientHash.get(channel));
            // Remove o cliente da hash
            clientHash.remove(channel);
            // Remove a queue
            dataQueue.remove(channel);
            return true;
        }
        return false;
    }
    

    /**
     * Encontra cliente pelo nome
     * @param channel
     * @return 
     */
    public Client find(SocketChannel channel) {
        if (clientHash.containsKey(channel)) {
            return clientHash.get(channel);
        }
        return null;
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
    
    /**
     * 
     * @param channel
     * @return 
     */
    public List<byte[]> getQueuedData(SocketChannel channel){
        return dataQueue.get(channel);
    }
    
    /**
     * 
     * @param channel 
     */
    public void clearQueue(SocketChannel channel){
        dataQueue.get(channel).clear();
    }

    /**
     * 
     * @return 
     */
    public HashMap<SocketChannel, Client> getClientHash() {
        return clientHash;
    }

    /**
     * 
     * @return 
     */
    public ObservableList<Client> getClientList() {
        return clientList;
    }
    
    public int getClientNumber(){
        return clientList.size();
    }
}
