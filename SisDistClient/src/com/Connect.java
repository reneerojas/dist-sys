/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com;

import core.DynamicClass;
import core.DynamicClassManager;
import core.Response;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import lib.json.JsonArray;
import lib.json.JsonObject;

/**
 *
 * @author renee
 */
public class Connect implements Runnable
{

    private Selector selector;
    private SelectionKey key;
    private Iterator keys;
    private SocketChannel tempChannel;
    private ByteBuffer buffer;
    private String nameServer;
    private SocketChannel socketChannel;
    final int DEFAULT_PORT = 9053;
    private boolean isServer = false;
    private String clientName;

    public Connect(String ip) throws IOException
    {

        nameServer = ip;

        buffer = ByteBuffer.allocateDirect(512);

        connectToNameServer(DEFAULT_PORT);

        ConnectionManager.getInstance().getClosing().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) ->
        {
            if(newValue)
            {
                try
                {
                    close();
                }
                catch(IOException ex)
                {
                    Logger.getLogger(Connect.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });

    }

    /**
     * Interrompe conexao atual e inicia uma com o servidor de nomes
     *
     * @param port
     * @throws IOException
     */
    private void connectToNameServer(int port) throws IOException
    {

        finish();
        isServer = false;
        socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);
        socketChannel.connect(new InetSocketAddress(nameServer, port));

        selector = Selector.open();
        key = socketChannel.register(selector, SelectionKey.OP_CONNECT);

    }

    /**
     * Interrompe conexao atual e inicia uma com o servidor
     *
     * @param port
     * @throws IOException
     */
    private void connectToServer(String ip, int port) throws IOException
    {

        finish();
        isServer = true;
        socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);
        socketChannel.connect(new InetSocketAddress(ip, port));

        selector = Selector.open();
        key = socketChannel.register(selector, SelectionKey.OP_CONNECT);

    }

    private void finish()
    {
        try
        {
            if(socketChannel != null && socketChannel.isOpen())
            {
                socketChannel.close();
            }
            if(selector != null && selector.isOpen())
            {
                selector.close();
            }
        }
        catch(IOException ex)
        {
            Logger.getLogger(Connect.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void processRead(String data)
    {
        if(data.length() < 6)
        {
            return;
        }
        System.out.println("RCVD <- " + data);
        JsonObject json = JsonObject.readFrom(data);

        if(json.isObject())
        {
            if(json.get("success").isTrue())
            {
                //Se a operacao for de handshake
                switch(json.get("op").asString())
                {
                    case "Hello!":
                        System.out.println("got hello!");
                        this.sendHandshake(json);
                        //Processo de inicializacao
                        break;
                    case "init":
                        try
                        {
                            System.out.println("Init conn");
                            this.connectToServer(json.get("server").asString().startsWith("127.0") ? nameServer : json.get("server").asString(), json.get("port").asInt());
                        }
                        catch(IOException ex)
                        {
                            Logger.getLogger(Connect.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        break;
                    case "registered":
                        Response.getInstance().setWait(false);
                        Platform.runLater(() ->
                        {
                            ConnectionManager.getInstance().getMessage().set(json.get("message").asString());
                            ConnectionManager.getInstance().getStatus().set(4);
                        });
                        System.out.println("Conexao registrada no server");
                        break;
                    case "noclass":
                        JsonObject jObj = new JsonObject();
                        DynamicClass dyClass = DynamicClassManager.getInstance().getDynamicClass(json.get("class").asString());

                        if(dyClass != null)
                        {
                            jObj.add("success", true);
                            jObj.add("op", "newclass");
                            jObj.add("class", dyClass.getName());
                            jObj.add("version", dyClass.getVersao());
                            jObj.add("method", json.get("method").asString());
                            jObj.add("params", json.get("params").asArray());
                            jObj.add("code", dyClass.getCodigo());

                            this.sendJson(jObj);
                        }
                        else
                        {
                            System.out.println("Não existe o código");
                            Response.getInstance().setWait(false);
                        }
                        break;
                    case "response":
                        Response.getInstance().setWait(false);
                        String parametros = "";
                        JsonArray jArray = json.get("params").asArray();
                        for(int i = 0; i < jArray.size(); i++)
                        {
                            parametros = parametros.concat(jArray.get(i).asInt() + ",");
                        }

                        Response.getInstance().update(json.get("class").asString() + ": " + json.get("method").asString() + "(" + parametros + ") = " + json.get("value").asString());    
                        break;
                }
            }
            else
            {
                try
                {
                    System.out.println("FALSE");
                    Platform.runLater(() ->
                    {
                        ConnectionManager.getInstance().getMessage().set(json.get("message").asString());
                        ConnectionManager.getInstance().getStatus().set(0);
                    });
                    Thread.sleep(5000);
                    connectToNameServer(DEFAULT_PORT);
                }
                catch(InterruptedException | IOException ex)
                {
                    Logger.getLogger(Connect.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        }
        else
        {
            System.out.println("Erro ao obter dados.");
        }
    }

    /**
     * Envia o Handshake
     *
     * @param json
     */
    private void sendHandshake(JsonObject json)
    {
        json.remove("message");
        json.set("op", "init");
        json.add("type", "Client");
        if(isServer)
        {
            json.add("name", clientName);
        }
        try
        {
            System.out.println(json.toString().length() + " Send -> " + json);
            socketChannel.write(ByteBuffer.wrap(json.toString().getBytes("UTF-8")));
        }
        catch(IOException ex)
        {
            Logger.getLogger(Connect.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void run()
    {
        //
        while(selector.isOpen())
        {
            try
            {
                //
                selector.select();

                //pega os selectors ativos
                keys = selector.selectedKeys().iterator();

                while(keys.hasNext())
                {
                    key = (SelectionKey) keys.next();
                    tempChannel = (SocketChannel) key.channel();

                    //prevent the same key from coming up again
                    if(!key.isValid())
                    {
                        continue;
                    }
                    if(key.isConnectable())
                    {
                        System.out.println("Nova conexao com: " + tempChannel.getRemoteAddress());
                        //registra o canal para I/O
                        while(!tempChannel.finishConnect())
                        {

                        }
                        Platform.runLater(() ->
                        {
                            ConnectionManager.getInstance().getMessage().set("Iniciando conexão");
                            ConnectionManager.getInstance().getStatus().set(4);
                        });
                        tempChannel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);

                    }
                    else
                    {
                        if(key.isReadable())
                        {
                            buffer.clear();

                            int numBytesRead = tempChannel.read(buffer);

                            if(numBytesRead == -1)
                            {
                                tempChannel.close();
                                System.out.println("Conexao encerrada...");
                                Response.getInstance().setWait(true);
                                endConnection();
                                connectToNameServer(DEFAULT_PORT);
                            }
                            else
                            {
                                buffer.flip();
                                byte[] data = new byte[numBytesRead];
                                buffer.get(data, 0, numBytesRead);
                                //envia json para ser interpretado
                                processRead(new String(data, "UTF-8"));
                                buffer.clear();
                            }
                        }
                        else
                        {
                            if(key.isWritable())
                            {
                                Thread.sleep(2000);
                            }
                        }
                    }

                    //remove o selector
                    keys.remove();
                }
            }
            catch(ConnectException ex)
            {
                endConnection();
                try
                {
                    connectToNameServer(DEFAULT_PORT);
                }
                catch(IOException ex1)
                {
                    Logger.getLogger(Connect.class.getName()).log(Level.SEVERE, null, ex1);
                }
            }
            catch(InterruptedException ex)
            {

            }
            catch(IOException ex)
            {
                Logger.getLogger(Connect.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void endConnection()
    {
        Platform.runLater(() ->
        {
            ConnectionManager.getInstance().getMessage().set("Conexao encerrada...");
            ConnectionManager.getInstance().getStatus().set(0);
        });
    }

    public void setName(String name)
    {
        clientName = name;
    }

    public boolean isConnected()
    {
        return socketChannel.isConnected();
    }

    public void close() throws IOException
    {
        socketChannel.close();
        selector.close();
        ComExecutor.getInstance().removeAll();
        ComExecutor.getInstance().getExecutor().shutdownNow();
    }

    public void sendJson(final JsonObject jObj)
    {
        ComExecutor.getInstance().getExecutor().execute(() ->
        {
            try
            {
                System.out.println(" Send CLI-> " + jObj);
                socketChannel.write(ByteBuffer.wrap(jObj.toString().getBytes("UTF-8")));
            }
            catch(IOException ex)
            {
                Logger.getLogger(Connect.class.getName()).log(Level.SEVERE, null, ex);
            }

        });
    }
}
