/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com;

import core.Client;
import core.ClientManager;
import core.DynamicClassManager;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import lib.json.JsonArray;
import lib.json.JsonObject;

/**
 *
 * @author renee
 */
public final class Server implements Runnable {

    private int port;
    private ClientManager clientManager;
    private DynamicClassManager dynamicManager;
    private Selector selector;
    private ServerSocketChannel serverSocketChannel;
    private ByteBuffer buf = ByteBuffer.allocateDirect(1024);

    Charset charset = Charset.defaultCharset();
    CharsetDecoder decoder = charset.newDecoder();

    private SocketChannel socketChannel;

//    private JsonObject tempJson;
    private String[] remoteAddress;

    public Server() {
        //Get Singleton instance
        clientManager = ConnectionManager.getInstance().getClientManagerInstance();

        dynamicManager = DynamicClassManager.getInstance();

        //open Selector and ServerSocketChannel by calling the open() method
        try {
            selector = Selector.open();
            serverSocketChannel = ServerSocketChannel.open();

            //check that both of them were successfully opened
            if ((serverSocketChannel.isOpen()) && (selector.isOpen())) {

                //configure non-blocking mode
                serverSocketChannel.configureBlocking(false);
                //set some options
                serverSocketChannel.setOption(StandardSocketOptions.SO_RCVBUF, 1024);
                serverSocketChannel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
                // 0 para pegar qualquer porta disponível
                InetSocketAddress address = new InetSocketAddress(0);
                serverSocketChannel.bind(address);

                //register the current channel with the given selector
                serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

                //remove a barra e quebra no : para ter ip e porta
                String[] addr = remoteAddress = serverSocketChannel.getLocalAddress().toString().substring(1).split(":");

                port = Integer.parseInt(addr[addr.length - 1]);

                //Notifica o gerenciador de conexao
                this.notifyPort();

                //display a waiting message while ... waiting!
                System.out.println("Aguardando conexões na porta: " + port);
            } else {
                System.out.println("The server socket channel or selector cannot be opened!");
            }
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Notifica o gerenciador de conexão que uma porta foi atribuida
     */
    private void notifyPort() {
        Platform.runLater(() -> {
            ConnectionManager.getInstance().setServerPort(port);
        });
    }

    private void processAccept(SelectionKey key, Selector selector) throws IOException {
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        socketChannel = serverChannel.accept();
        socketChannel.configureBlocking(false);
        System.out.println("Nova conexao de: " + socketChannel.getRemoteAddress());
        //write a welcome message
        JsonObject json = new JsonObject();
        json.add("success", true);
        json.add("op", "Hello!");
        System.out.println("Sent -> " + json);
        socketChannel.write(ByteBuffer.wrap(json.toString().getBytes("UTF-8")));
        //register channel with selector for further I/O
//        serverManager.dataTrack.put(socketChannel, new ArrayList<byte[]>());

        socketChannel.register(selector, SelectionKey.OP_READ);
    }

    private void initClient(SocketChannel channel, String name) throws IOException {
        //getRemoteAddress retorna algo como /127.0.0.1:36666
        //remove a barra e quebra no : para ter ip e porta
        remoteAddress = channel.getRemoteAddress().toString().substring(1).split(":");

        ConnectionManager.getInstance().getClientManagerInstance().add(channel, new Client(remoteAddress[0], remoteAddress[1], name));
        ConnectionManager.getInstance().updateConnectedClients();
    }

    private void processRead(SelectionKey key) throws IOException {
        socketChannel = (SocketChannel) key.channel();
        buf.clear();
        int numRead = -1;
        try {
            numRead = socketChannel.read(buf);
        } catch (IOException e) {
            System.err.println("Erro na conexao");
        }
        if (numRead == -1) {
            try {
//                clientManager.remove(socketChannel);
                System.out.println("Conexao encerrada: " + socketChannel.getRemoteAddress());
                ConnectionManager.getInstance().getClientManagerInstance().remove(socketChannel);
                ConnectionManager.getInstance().updateConnectedClients();
                socketChannel.close();
                key.cancel();
                return;
            } catch (IOException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        try {
            buf.flip();
            byte[] data = new byte[numRead];
            buf.get(data, 0, numRead);
            System.out.println(" RCVD <- " + new String(data));
            JsonObject json = JsonObject.readFrom(new String(data, "UTF-8"));
            if (json.isObject() && json.get("success").asBoolean()) {
//                System.out.println("Json: " + json);
                this.processMessage(socketChannel, json);
            }
            buf.clear();
            // write back to client
//            serverManager.queueData(socketChannel, buf.array());
//            key.interestOps(SelectionKey.OP_WRITE);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void processMessage(SocketChannel channel, JsonObject json) throws IOException {
        JsonObject tempJson;
        String op = json.get("op").asString();
        int ver;
        switch (op) {
            case "init":
                System.out.println("=== New Client ===");
                this.initClient(channel, (json.get("name").isNull() ? "Client" : json.get("name").asString()));
                tempJson = new JsonObject();
                tempJson.add("success", true);
                tempJson.add("op", "registered");
                tempJson.add("message", "Welcome to server " + ConnectionManager.getInstance().getServerName().get());
                System.out.println("SEND -> " + json);
                channel.write(ByteBuffer.wrap(tempJson.toString().getBytes("UTF-8")));
                break;
            case "request":
                System.out.println("=== New Client ===");
                ver = dynamicManager.hasClass(json.get("class").asString());
                // verificar versao tambem em servidor e cache, para executar ou nao
//                if (ver == 0) {
                tempJson = new JsonObject();
                tempJson.add("success", true);
                tempJson.add("op", "code");
                tempJson.add("version", ver);
                tempJson.add("class", json.get("class").asString());
                tempJson.add("method", json.get("method").asString());
                tempJson.add("params", json.get("params").asArray());

                //envia req para o nameserver
                tempJson.add("request", ConnectionManager.getInstance().insertReq(channel));
                ConnectionManager.getInstance().getNameServerInstance().sendJson(tempJson);

//                }
//                else if (json.get("version").asInt() == ver) {
//                    dynamicManager.execute(channel, dynamicManager.getClass(json.get("name").asString()), json.get("method").asString(), json.get("params").asArray());
//                }
                break;
            case "code":
                tempJson = new JsonObject();
                tempJson.add("success", true);
                tempJson.add("op", "noclass");
                tempJson.add("class", json.get("class").asString());
                tempJson.add("method", json.get("method").asString());
                tempJson.add("params", json.get("params").asArray());
                System.out.println("SEND -> " + json);
                channel.write(ByteBuffer.wrap(tempJson.toString().getBytes("UTF-8")));
                break;

            case "newclass":
                ver = dynamicManager.hasClass(json.get("class").asString());
                //Server nao tem a classe
                if (ver == 0) {
                    tempJson = new JsonObject();
                    tempJson.add("success", true);
                    tempJson.add("op", "code");
                    tempJson.add("class", json.get("class").asString());
                    tempJson.add("version", json.get("version").asInt());
                    tempJson.add("method", json.get("method").asString());
                    tempJson.add("params", json.get("params").asArray());
                    tempJson.add("code", json.get("code").asString());
                    tempJson.add("request", ConnectionManager.getInstance().insertReq(channel));
                    
                    //Notifica nameserver de um novo codigo
                    ConnectionManager.getInstance().getNameServerInstance().sendJson(tempJson);
                    dynamicManager.newClass(json.get("class").asString(), json.get("version").asInt(), json.get("code").asString());
                    dynamicManager.execute(tempJson.get("request").asInt(), dynamicManager.getClass(json.get("class").asString()), json.get("method").asString(), json.get("params").asArray());
                    //Recebeu uma versao maior
                } else if (json.get("version").asInt() > ver) {
                    dynamicManager.updateClass(json.get("class").asString(), json.get("version").asInt(), json.get("code").asString());
                    dynamicManager.execute(ConnectionManager.getInstance().insertReq(channel), dynamicManager.getClass(json.get("class").asString()), json.get("method").asString(), json.get("params").asArray());
                }
                break;
        }
    }

    public void response(SocketChannel channel, String cl, String method, JsonArray params, String value) throws IOException {
        ComExecutor.getInstance().getExecutor().execute(() -> {
            JsonObject json = new JsonObject();
            json.add("success", true);
            json.add("op", "response");
            json.add("class", cl);
            json.add("method", method);
            json.add("params", params);
            json.add("value", value);
            System.out.println("SEND -> " + json);
            try {
                socketChannel.write(ByteBuffer.wrap(json.toString().getBytes("UTF-8")));
            } catch (IOException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
    }

    private void processWrite(SelectionKey key) throws IOException {
        socketChannel = (SocketChannel) key.channel();
        List<byte[]> channelData = clientManager.getQueuedData(socketChannel);
        Iterator<byte[]> its = channelData.iterator();
        while (its.hasNext()) {
            byte[] it = its.next();
            its.remove();
            socketChannel.write(ByteBuffer.wrap(it));
        }
        clientManager.clearQueue(socketChannel);
        key.interestOps(SelectionKey.OP_READ);
    }

    public int getPort() {
        return port;
    }

    public void stop() throws IOException {
        selector.close();
    }

    @Override
    public void run() {

        while (selector.isOpen()) {
            try {
                //wait for incomming events
                selector.select();

                if (selector.isOpen()) {
                    //there is something to process on selected keys
                    Iterator keys = selector.selectedKeys().iterator();
                    while (keys.hasNext()) {
                        SelectionKey key = (SelectionKey) keys.next();
                        //prevent the same key from coming up again
                        keys.remove();
                        if (!key.isValid()) {
                            continue;
                        }
                        if (key.isAcceptable()) {
                            this.processAccept(key, selector);
                        } else if (key.isReadable()) {
                            this.processRead(key);
                        } else if (key.isWritable()) {
                            this.processWrite(key);
                        }
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

}
