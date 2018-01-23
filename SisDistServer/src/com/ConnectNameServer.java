/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com;

import core.DynamicClass;
import core.DynamicClassManager;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import lib.json.JsonObject;

/**
 *
 * @author renee
 */
public class ConnectNameServer implements Runnable {

    private DynamicClassManager dynamicManager;
    private Selector selector;
    private SelectionKey key;
    private Iterator keys;
    private SocketChannel tempChannel;
    private ByteBuffer buffer;
    private JsonObject clientsRepport;
    private volatile int readyChannels;

    private final int DEFAULT_PORT = 9053;
    private String nameServerIp;
//    SocketChannel socketChannel;

    public ConnectNameServer(String ip) throws IOException {
        dynamicManager = DynamicClassManager.getInstance();
        buffer = ByteBuffer.allocateDirect(512);
        nameServerIp = ip;
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);
        socketChannel.connect(new InetSocketAddress(nameServerIp, DEFAULT_PORT));
        selector = Selector.open();
        key = socketChannel.register(selector, SelectionKey.OP_CONNECT);
        System.out.println("NEW CONNEC");
    }

    private void processRead(String data) {
//        if (data.length() < 6) {
//            return;
//        }
        JsonObject json = JsonObject.readFrom(data);
        JsonObject jSonResp;
        System.out.println("RCVD <-" + json.toString());
        if (json.get("success").isTrue() && json.get("op").isString()) {
            //Se a operacao for de handshake
            switch (json.get("op").asString()) {
                case "Hello!":
                    sendHandshake(json);
                    //Processo de inicializacao
                    break;
                case "init":
                    if (json.get("message").asString().equals("Welcome Server")) {
                        setName(json.get("name").asString());
                    }
                    break;
                case "cache":
                    jSonResp = new JsonObject();
                    jSonResp.add("success", true);
                    jSonResp.add("op", "response");
                    jSonResp.add("class", json.get("class").asString());
                    jSonResp.add("method", json.get("method").asString());
                    jSonResp.add("params", json.get("params").asArray());

                    if (json.get("cached").asBoolean() == true) {
                        jSonResp.add("success", true);
                        jSonResp.add("value", json.get("response").asString());
                        try {
                            //responde ao cliente
                            ConnectionManager.getInstance().getRequest(json.get("request").asInt()).write(ByteBuffer.wrap(jSonResp.toString().getBytes("UTF-8")));

                            // ler hash de requisicao e mandar jSon
                        } catch (UnsupportedEncodingException ex) {
                            Logger.getLogger(ConnectNameServer.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (IOException ex) {
                            Logger.getLogger(ConnectNameServer.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    } else {
                        if (dynamicManager.hasClass(json.get("class").asString()) > 0) {
                            dynamicManager.execute(json.get("request").asInt(), dynamicManager.getClass(json.get("class").asString()), json.get("method").asString(), json.get("params").asArray());
                        }
//                        jSon.add("result", json.get("result").asString());
//                        jSon.remove("op");
//                        jSon.add("op", "cache");
//                        jSon.add("op-cache", "add");
//                        try {
//                            System.out.println("SEND SERVERNAME -> " + jSon.toString());
//                        } catch (IOException ex) {
//                            Logger.getLogger(ConnectNameServer.class.getName()).log(Level.SEVERE, null, ex);
//                        }

                    }
                    break;
                case "code":
                    if (dynamicManager.hasClass(json.get("class").asString()) < json.get("version").asInt()) {
                        dynamicManager.updateClass(json.get("class").asString(), json.get("version").asInt(), json.get("code").asString());
                    }
                    if (json.get("cached").asBoolean()) {
                        jSonResp = new JsonObject();
                        jSonResp.add("success", true);
                        jSonResp.add("op", "response");
                        jSonResp.add("class", json.get("class").asString());
                        jSonResp.add("method", json.get("method").asString());
                        jSonResp.add("params", json.get("params").asArray());
                        jSonResp.add("value", json.get("response").asString());
                        jSonResp.add("code", json.get("code").asString());
                        try {
                            //responde ao cliente
                            ConnectionManager.getInstance().getRequest(json.get("request").asInt()).write(ByteBuffer.wrap(jSonResp.toString().getBytes("UTF-8")));
                        } catch (UnsupportedEncodingException ex) {
                            Logger.getLogger(ConnectNameServer.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (IOException ex) {
                            Logger.getLogger(ConnectNameServer.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    } else {
                        dynamicManager.execute(json.get("request").asInt(), dynamicManager.getClass(json.get("class").asString()), json.get("method").asString(), json.get("params").asArray());
                    }
                    break;
                case "codeupdate":
                    if (dynamicManager.hasClass(json.get("class").asString()) > 0) {
                        jSonResp = new JsonObject();
                        jSonResp.add("success", true);
                        jSonResp.add("op", "noclass");
                        jSonResp.add("class", json.get("class").asString());
                        jSonResp.add("method", json.get("method").asString());
                        jSonResp.add("params", json.get("params").asArray());
                        jSonResp.add("code", dynamicManager.getClass(json.get("class").asString()).codeProperty().get());
                        try {
                            tempChannel.write(ByteBuffer.wrap(jSonResp.toString().getBytes("UTF-8")));
                        } catch (IOException ex) {
                            Logger.getLogger(ConnectNameServer.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    break;
                case "noclass":
                    jSonResp = new JsonObject();
                    jSonResp.add("success", true);
                    jSonResp.add("op", "noclass");
                    jSonResp.add("class", json.get("class").asString());
                    jSonResp.add("method", json.get("method").asString());
                    jSonResp.add("params", json.get("params").asArray());
                    try {
                        //responde ao cliente
                        ConnectionManager.getInstance().getRequest(json.get("request").asInt()).write(ByteBuffer.wrap(jSonResp.toString().getBytes("UTF-8")));
                    } catch (UnsupportedEncodingException ex) {
                        Logger.getLogger(ConnectNameServer.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IOException ex) {
                        Logger.getLogger(ConnectNameServer.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    break;
            }
        } else {
            System.out.println("Erro ao obter dados.");
        }
    }

    private void sendHandshake(JsonObject json) {
        json.remove("message");
        json.set("op", "init");
        json.add("type", "Server");
        json.add("port", ConnectionManager.getInstance().getServerPort());
        System.out.println("SEND -> " + json.toString());
        try {
            tempChannel.write(ByteBuffer.wrap(json.toString().getBytes("UTF-8")));
        } catch (IOException ex) {
            Logger.getLogger(ConnectNameServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void sendJson(JsonObject json) {
        ComExecutor.getInstance().getExecutor().execute(() -> {
            try {
                System.out.println("SEND NAME->" + json);
                tempChannel.write(ByteBuffer.wrap(json.toString().getBytes("UTF-8")));
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(ConnectNameServer.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(ConnectNameServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
    }

    private void setName(final String name) {
        ConnectionManager.getInstance().setServerName(name);
//        System.out.println(ConnectionManager.getInstance().getServerName().get());
    }

    private void sendUpdate() throws IOException {
        if (ComExecutor.getInstance().getExecutor().isShutdown()) {
            closeConnection();
        } else {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                //Just silence
            }
            clientsRepport = new JsonObject();
            clientsRepport.add("success", true);
            clientsRepport.add("op", "update-server");
            clientsRepport.add("clients", ConnectionManager.getInstance().getConnectedClients().intValue());
            System.out.println("SEND -> " + clientsRepport);
//                        clientsRepport.add("clients", new Random().nextInt(1000));

            tempChannel.write(ByteBuffer.wrap(clientsRepport.toString().getBytes("UTF-8")));
        }
    }

    private void closeConnection() throws IOException {
        ComExecutor.getInstance().removeAll();
        selector.close();
        Platform.runLater(() -> {
            ConnectionManager.getInstance().getServerName().set("Servidor Distribuido");
        });
    }

    @Override
    public void run() {
        //
        while (selector.isOpen()) {
            try {
                //
                readyChannels = selector.select();

                if (readyChannels == 0) {
                    continue;
                }

                //pega os selectors ativos
                keys = selector.selectedKeys().iterator();

                while (keys.hasNext()) {
                    key = (SelectionKey) keys.next();

                    //prevent the same key from coming up again
//                    if (!key.isValid()) {
//                        continue;
//                    }
                    if (key.isConnectable()) {

                        try {
                            tempChannel = (SocketChannel) key.channel();
                            System.out.println("Nova conexao de: " + tempChannel.getRemoteAddress().toString().substring(1));
                            //registra o canal para I/O
                            while (!tempChannel.finishConnect()) {
                            }
                            tempChannel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                        } catch (IOException e) {
                            System.out.println("TASKS: " + ComExecutor.getInstance().taskNumber());
                            ComExecutor.getInstance().removeAll();
                            System.out.println("TASKS: " + ComExecutor.getInstance().taskNumber());
                        }
                    } else {
                        if (key.isReadable()) {
                            buffer.clear();

                            tempChannel = (SocketChannel) key.channel();

                            int numBytesRead = tempChannel.read(buffer);

                            if (numBytesRead == -1) {
                                System.out.println("Conexao finalizada");
                                closeConnection();
                            } else {
                                buffer.flip();
                                byte[] data = new byte[numBytesRead];
                                buffer.get(data, 0, numBytesRead);
                                //envia json para ser interpretado
                                processRead(new String(data, "UTF-8"));
                                buffer.clear();
                            }
                        } else {
                            if (key.isWritable()) {

                                sendUpdate();

                            }
                        }
                    }

                    //remove o selector
                    keys.remove();

                }
            } catch (IOException ex) {
                ComExecutor.getInstance().removeAll();
                Logger.getLogger(ConnectNameServer.class.getName()).log(Level.INFO, null, ex);

            }
        }
    }

}
