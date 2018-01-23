/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com;

import core.Server;
import core.ServerManager;
import core.cache.DynamicClassManager;
import core.cache.DynamicResultsCache;
import core.log.Log;
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
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import lib.json.JsonObject;

/**
 * Classe que implementa o servidor de nomes
 *
 * @author renee
 */
public final class NameServer implements Runnable {

    private final ServerManager serverManager;
    private ServerSocketChannel serverSocketChannel;
    private Selector selector;

    private DynamicResultsCache dynCache;
    private DynamicClassManager dynClass;

    //apenas uma referencia
    private ByteBuffer buf = ByteBuffer.allocateDirect(512);

//    private JsonObject tempJson;
    Charset charset = Charset.defaultCharset();
    CharsetDecoder decoder = charset.newDecoder();

    private String[] remoteAddress;

    private enum CloseType {

        ERROR, CLOSE
    };

    /**
     *
     */
    public NameServer() {
        //Get Singleton instance
        serverManager = ConnectionManager.getInstance().getServerManagerInstance();
        dynCache = DynamicResultsCache.getInstance();
        dynClass = DynamicClassManager.getInstance();
    }

    @Override
    public void run() {
        try {
            selector = Selector.open();
            serverSocketChannel = ServerSocketChannel.open();
            //verifica se ambos estao abertos
            if ((serverSocketChannel.isOpen()) && (selector.isOpen())) {

                //configure non-blocking mode
                serverSocketChannel.configureBlocking(false);
                //opcoes de buffer
                serverSocketChannel.setOption(StandardSocketOptions.SO_RCVBUF, 1024);
                serverSocketChannel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
                // 53 referencia a porta DNS :D
                serverSocketChannel.bind(new InetSocketAddress(9053));
                //registrra um canal com o seletor
                serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

                //display a waiting message while ... waiting!
                System.out.println("Aguardando conexões...");

                while (selector.isOpen()) {
                    //Seleciona todos os canais com operações agendadas
                    selector.select();

                    //Verifica se o seletor ainda está aberto
                    if (selector.isOpen()) {

                        //existe algo para processar no seletor
                        Iterator keys = selector.selectedKeys().iterator();

                        while (keys.hasNext()) {
                            SelectionKey key = (SelectionKey) keys.next();
                            if (key.isAcceptable()) {
                                this.processAccept(key, selector);
                            } else if (key.isReadable()) {
                                this.processRead(key);
                            } else if (key.isWritable()) {
                                this.processWrite(key);
                            }
                            //remove a chave para evitar processamento duplo
                            keys.remove();
                        }
                    }
                }
            } else {
                System.out.println("The server socket channel or selector cannot be opened!");
            }
        } catch (IOException ex) {
            Logger.getLogger(NameServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void processAccept(SelectionKey key, Selector selector) throws IOException {
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        SocketChannel socketChannel = serverChannel.accept();
        socketChannel.configureBlocking(false);
        System.out.println("Nova conexao de: " + socketChannel.getRemoteAddress().toString().substring(1));
        //write a welcome message
        //register channel with selector for further I/O
//        serverManager.dataTrack.put(socketChannel, new ArrayList<byte[]>());

        this.handShake(socketChannel, selector);

        socketChannel.register(selector, SelectionKey.OP_READ);
    }

    /**
     * Envia o Handshake
     *
     * @param socketChannel
     * @param selector
     * @throws UnsupportedEncodingException
     * @throws IOException
     */
    private void handShake(SocketChannel socketChannel, Selector selector)
            throws UnsupportedEncodingException, IOException {
        JsonObject json = new JsonObject();
        json.add("success", true);
        json.add("op", "Hello!");

        //Ordena os servidores mais livres
        serverManager.sortFreestServer();

        String s = json.toString();
        System.out.println("SEND -> " + s);

        socketChannel.write(ByteBuffer.wrap(s.getBytes("UTF-8")));
    }

    /**
     * Processa as mensagens que estao chegando
     *
     * @param key
     * @throws UnsupportedEncodingException
     * @throws IOException
     */
    private void processRead(SelectionKey key) throws UnsupportedEncodingException, IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        buf.clear();
        int numRead = -1;
        try {
            numRead = socketChannel.read(buf);
        } catch (IOException e) {
            System.err.println("Erro na conexao");
            removeConnection(socketChannel, Log.Types.ERROR);
        }
        if (numRead == -1) {
            try {
                System.out.println("Conexao encerrada.");
                removeConnection(socketChannel, Log.Types.CLOSE);
                socketChannel.close();
                key.cancel();
                return;
            } catch (IOException ex) {
                Logger.getLogger(NameServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        buf.flip();
        byte[] data = new byte[numRead];
        buf.get(data, 0, numRead);

        JsonObject json = JsonObject.readFrom(new String(data, "UTF-8"));
        System.out.println("RCVD <- " + json);
        if (json.get("success").asBoolean()) {
            this.processMessage(socketChannel, json);
        }
        buf.clear();
    }

    /**
     * Remove a conexao se for servidor e loga o tipo de encerramento
     *
     * @param channel
     * @param type
     */
    private void removeConnection(SocketChannel channel, Log.Types type) {
        try {
            if (serverManager.getServerHash().containsKey(channel)) {
                Log.getInstance().addLog(type, "Server Connection - " + channel.getRemoteAddress());
                serverManager.remove(channel);
            } else {
                Log.getInstance().addLog(type, "Client Connection - " + channel.getRemoteAddress());
            }
        } catch (IOException ex) {
            Logger.getLogger(NameServer.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     * Processa as mensagens ja convertidas em JSON
     *
     * @param channel
     * @param json
     * @throws IOException
     */
    private void processMessage(SocketChannel channel, JsonObject json) throws IOException {
        JsonObject tempJson = new JsonObject();
        switch (json.get("op").asString()) {
            case "init":
                switch (json.get("type").asString()) {
                    case "Server":
                        System.out.println("=== New Server ===");
                        int port = json.get("port").asInt();
                        String name = this.initServer(channel, port);

                        tempJson.add("success", true);
                        tempJson.add("op", "init");
                        tempJson.add("message", "Welcome Server");
                        tempJson.add("name", name);
                        channel.write(ByteBuffer.wrap(tempJson.toString().getBytes("UTF-8")));
                        Log.getInstance().addLog(Log.Types.INIT, "Server - " + name + " - " + channel.getRemoteAddress().toString().substring(1) + " - Listem on " + port);
                        break;
                    case "Client":
                        System.out.println("=== New Client ====");

                        if (serverManager.getServerList().size() > 0) {
                            tempJson.add("success", true);
                            tempJson.add("op", "init");
                            tempJson.add("server", serverManager.getServerList().get(0).addressProperty().get());
                            tempJson.add("port", serverManager.getServerList().get(0).portProperty().get());
                            Log.getInstance().addLog(Log.Types.INIT, "Client - " + channel.getRemoteAddress().toString().substring(1) + " - sent to: " + serverManager.getServerList().get(0).nameProperty().get());
                        } else {
                            tempJson.add("success", false);
                            tempJson.add("message", "No Server Found! Sorry.");
//                            Log.getInstance().addLog(Log.Types.INIT, "Client - " + channel.getRemoteAddress().toString().substring(1) + " - sent to: " + serverManager.getServerList().get(0).nameProperty().get());
                        }
                        channel.write(ByteBuffer.wrap(tempJson.toString().getBytes("UTF-8")));
                        break;
                }
                break;
            case "update-server":
                final int num = json.get("clients").asInt();
                Platform.runLater(() -> {
                    if (serverManager.getServerHash().get(channel) != null) {
//                        System.out.println(num);
                        serverManager.getServerHash().get(channel).clientsProperty().set(num);
                        Log.getInstance().addLog(Log.Types.UPDATE, serverManager.getServerHash().get(channel).nameProperty().get() + " - Clients: " + num);
                    }
                });
//                System.out.println(serverManager.getServerHash().get(channel).nameProperty().get() + " -> " + serverManager.getServerHash().get(channel).clientsProperty().get());
                break;
            case "cache":
                switch (json.get("op-cache").asString()) {
                    case "add":
                        if (!json.get("result").isNull()) {
                            //Loga resposta adicionada no cache
                            Log.getInstance().addLog(Log.Types.UPDATE, serverManager.getServerHash().get(channel).nameProperty().get() + " - Cache Add: Class(" + json.get("class").asString() + ") - Method(" + json.get("method").asString() + ") - params(" + json.get("params").asArray() + ") - value(" + json.get("result").asString() + ")");
                            dynCache.newResponse(json.get("class").asString(), json.get("version").asInt(), json.get("method").asString(), json.get("params").asArray(), json.get("result").asString());
                        }
                        break;
                    case "check":
                        String result = dynCache.getResponse(json.get("class").asString(), json.get("version").asInt(), json.get("method").asString(), json.get("params").asArray());
                        tempJson = new JsonObject();
                        tempJson.add("success", true);
                        tempJson.add("op", json.get("op").asString());
                        tempJson.add("class", json.get("class").asString());
                        tempJson.add("method", json.get("method").asString());
                        tempJson.add("params", json.get("params").asArray());
                        if (result != null) {
                            tempJson.add("cached", true);
                            tempJson.add("result", result);
                            //Loga hit no cache
                            Log.getInstance().addLog(Log.Types.UPDATE, serverManager.getServerHash().get(channel).nameProperty().get() + " - Cache HIT: Class(" + json.get("class").asString() + ") - Method(" + json.get("method").asString() + ") - params(" + json.get("params").asArray() + ") - value(" + result + ")");

                        } else {
                            tempJson.add("cached", false);
                        }
                        channel.write(ByteBuffer.wrap(tempJson.toString().getBytes("UTF-8")));
                        break;
                }
                break;
            case "request":
                //marcador de possibilidade de resposta em cache
                boolean canHasCached = false;
                tempJson.add("success", true);
                tempJson.add("class", json.get("class").asString());
                tempJson.add("method", json.get("method").asString());
                tempJson.add("params", json.get("params").asArray());
                //nem server nem nameServer possuem classe
                if (dynClass.hasClass(json.get("class").asString()) == 0 && json.get("version").asInt() == 0) {
                    tempJson.add("op", "noclass");
                    Log.getInstance().addLog(Log.Types.UPDATE, serverManager.getServerHash().get(channel).nameProperty().get() + " - Class NOT found: " + json.get("class").asString());

                    //NameServer possui versao mais atualizada do codigo
                } else if (dynClass.hasClass(json.get("class").asString()) > json.get("version").asInt()) {
                    tempJson.add("version", dynClass.getClass(json.get("class").asString()).getVersion());
                    tempJson.add("code", dynClass.getClass(json.get("class").asString()).codeProperty().get());
                    Log.getInstance().addLog(Log.Types.UPDATE, serverManager.getServerHash().get(channel).nameProperty().get() + " - Class update sent: " + json.get("class").asString());
                    canHasCached = true;
                    //NameServer possui versao mais velha do codigo
                } else if (dynClass.hasClass(json.get("class").asString()) < json.get("version").asInt()) {

                    //Ambos possuem a mesma versao
                } else if (dynClass.hasClass(json.get("class").asString()) == json.get("version").asInt() && dynClass.hasClass(json.get("class").asString()) != 0) {
                    canHasCached = true;
                    // TRATAR REQUISICAO DE CODIGO
                }

                //Se houver possibilidade de cache
                if (canHasCached) {
                    String resp = dynCache.getResponse(json.get("class").asString(), json.get("version").asInt(), json.get("method").asString(), json.get("params").asArray());
                    if (resp != null) {
                        //Loga hit no cache
                        Log.getInstance().addLog(Log.Types.UPDATE, serverManager.getServerHash().get(channel).nameProperty().get() + " - Cache HIT: Class(" + json.get("class").asString() + ") - Method(" + json.get("method").asString() + ") - params(" + json.get("params").asArray() + ") - value(" + resp + ")");
                        tempJson.add("result", resp);
                        tempJson.add("cached", true);
                    } else {
                        tempJson.add("cached", false);
                    }

                }
                //adiciona identificador de requisicao
                tempJson.add("request", json.get("request").asInt());
                channel.write(ByteBuffer.wrap(tempJson.toString().getBytes("UTF-8")));
                break;
            case "code":
                tempJson.add("success", true);
                tempJson.add("class", json.get("class").asString());
                tempJson.add("method", json.get("method").asString());
                tempJson.add("params", json.get("params").asArray());
                if (dynClass.hasClass(json.get("class").asString()) == 0 && json.get("version").asInt() == 0) {
                    tempJson.add("op", "noclass");
                    Log.getInstance().addLog(Log.Types.UPDATE, serverManager.getServerHash().get(channel).nameProperty().get() + " - No class on cache: " + json.get("class").asString());
                } else if (dynClass.hasClass(json.get("class").asString()) > json.get("version").asInt()) {
                    //Loga envio de update de codigo
                    Log.getInstance().addLog(Log.Types.UPDATE, serverManager.getServerHash().get(channel).nameProperty().get() + " - Class update sent: " + json.get("class").asString());
                    tempJson.add("success", true);
                    tempJson.add("op", "code");
                    tempJson.add("version", dynClass.getClass(json.get("class").asString()).getVersion());
                    tempJson.add("code", dynClass.getClass(json.get("class").asString()).codeProperty().get());
                    //Se a classe for mais antiga, atualiza
                } else if (dynClass.hasClass(json.get("class").asString()) < json.get("version").asInt()) {
                    if (json.get("code") != null) {
//                        System.out.println("ver ->" + dynClass.getClass(json.get("class").asString()).getVersion() + " - " + json.get("version").asInt());
                        dynClass.updateClass(json.get("class").asString(), json.get("version").asInt(), json.get("code").asString());
                        //Loga recebimento de update de codigo
                        tempJson.add("op", "cache");
                    } else {
                        System.out.println("Request update");
                        tempJson.add("op", "codeupdate");
                    }
                    Log.getInstance().addLog(Log.Types.UPDATE, serverManager.getServerHash().get(channel).nameProperty().get() + " - Class update received: " + json.get("class").asString());
                } else {
                    tempJson.add("op", "cache");
                }

                if (dynCache.hasClass(json.get("class").asString()) != null) {
                    System.out.println("Has result cache :" + dynCache.hasClass(json.get("class").asString()).hasMethod(json.get("method").asString(), json.get("params").asArray()));

                }
                //Se houver resposta no cache já responde
                String resp = dynCache.getResponse(json.get("class").asString(), json.get("version").asInt(), json.get("method").asString(), json.get("params").asArray());
                if (resp != null) {
                    System.out.println("JA NO CACHE");
                    //Loga hit no cache
                    Log.getInstance().addLog(Log.Types.UPDATE, serverManager.getServerHash().get(channel).nameProperty().get() + " - Cache HIT: Class(" + json.get("class").asString() + ") - Method(" + json.get("method").asString() + ") - params(" + json.get("params").asArray() + ") - value(" + resp + ")");
                    tempJson.add("method", json.get("method").asString());
                    tempJson.add("params", json.get("params").asArray());
                    tempJson.add("response", resp);
                    tempJson.add("cached", true);
                } else {
                    tempJson.add("cached", false);
                }

                if (json.get("request") != null) {
                    tempJson.add("request", json.get("request").asInt());
                }
                channel.write(ByteBuffer.wrap(tempJson.toString().getBytes("UTF-8")));
                break;

            case "log":
                Log.getInstance().addLog(Log.Types.UPDATE, json.get("msg").asString());
                break;
        }
    }

    /**
     * Inicia o registro do servidor
     *
     * @param channel
     * @param port
     * @return
     * @throws IOException
     */
    private String initServer(SocketChannel channel, int port) throws IOException {
        //cria um numero randomico para como nome do servidor
        int randNumber = new Random().nextInt(100);

        //Se o nome já existir cria outro
        while (!serverManager.avaiableName("Server-" + randNumber)) {
            randNumber = new Random().nextInt(1000);
        }

        //getRemoteAddress retorna algo como /127.0.0.1:36666
        //remove a barra e quebra no : para ter ip e porta
        remoteAddress = channel.getRemoteAddress().toString().substring(1).split(":");

        //Adiciona servidor na lista de servidores
        serverManager.add(channel, new Server("Server-" + randNumber, remoteAddress[0], port));

        return "Server-" + randNumber;
    }

    /**
     * Escreve dados que foram armazenados no buffer do cannal nao utilizado
     * nesta versao
     *
     * @param key
     * @throws IOException
     */
    private void processWrite(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        List<byte[]> channelData = serverManager.getQueuedData(socketChannel);
        Iterator<byte[]> its = channelData.iterator();
        while (its.hasNext()) {
            byte[] it = its.next();
            its.remove();
            socketChannel.write(ByteBuffer.wrap(it));
        }
        serverManager.clearQueue(socketChannel);
        key.interestOps(SelectionKey.OP_READ);
    }

    /**
     * Para o servidor de nomes
     *
     * @throws IOException
     */
    public void stop() throws IOException {
        selector.close();
        serverSocketChannel.close();
    }

}
