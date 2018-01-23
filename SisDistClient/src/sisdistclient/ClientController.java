/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sisdistclient;

import com.ComExecutor;
import com.ConnectionManager;
import core.Client;
import core.DynamicClassManager;
import core.Response;
import java.net.URL;
import java.util.NoSuchElementException;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lib.json.JsonArray;
import lib.json.JsonObject;

/**
 *
 * @author renee
 */
public class ClientController implements Initializable
{
    private Client cli;
    private ConnectionManager con = ConnectionManager.getInstance();
    private Response resp;
    private DynamicClassManager dynamicClassManager = DynamicClassManager.getInstance();

    @FXML
    private Button button;

    @FXML
    private TextArea txAreaCode;

    @FXML
    private Button btSolicitarCod;

    @FXML
    private TextField inputIp;

    @FXML
    private TextField tfClasse;

    @FXML
    private TextField tfversao;

    @FXML
    private Label labelStatus;

    @FXML
    private TextArea txAreaResults;

    @FXML
    private Label label;

    @FXML
    private TextField tfParametros;

    @FXML
    private Button btSolicitar;

    @FXML
    private TextField tfMethod;

    @FXML
    private TextField inputName;

    @FXML
    private void handleButtonAction(ActionEvent event)
    {
        //Configura dados de conexao
        cli = Client.getInstance();
        cli.serverAddressProperty().set(inputIp.getText());
        cli.nameProperty().set(inputName.getText());

        startListeners();

        con.newConnection(cli.serverAddressProperty().get());
        con.getConnectInstance().setName(cli.nameProperty().get());
        ComExecutor.getInstance().getExecutor().execute(con.getConnectInstance());

        inputName.setDisable(true);
        inputIp.setDisable(true);

    }

    private void startListeners()
    {
        labelStatus.textProperty().bind(ConnectionManager.getInstance().getMessage());

        ConnectionManager.getInstance().getStatus().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) ->
        {
            if(newValue.intValue() == 4)
            {
                button.setVisible(false);
            } else
            {
                inputName.setDisable(false);
                inputIp.setDisable(false);
                Response.getInstance().setWait(true);
                button.setVisible(true);
            }
        });

    }

    @Override
    public void initialize(URL url, ResourceBundle rb)
    {
        initilizaClasses();
        resp = Response.getInstance();
        txAreaResults.textProperty().bind(resp.responseProperty());
        listenButton();
    }

    public void initilizaClasses()
    {
        dynamicClassManager.insert("Fibonacci", "public class Fibonacci{public int fib(int n){int i = 1, j = 0;int t;for(int k = 0; k < n; k++){t = i + j;i = j;j = t;}return j;}public int getVersion(){return 1;}}", 1);
        dynamicClassManager.insert("Math", "public class Math{public int soma(int x, int y){return x + y;}public int mult(int x, int y){return x * y;}public int sub(int x, int y){return x - y;}public int getVersion(){return 1;}}", 1);
    }

    private boolean capturaValores(JsonArray jArray)
    {
        if(!tfParametros.getText().toString().equals(""))
        {
            String str = tfParametros.getText().toString();
            StringTokenizer s = new StringTokenizer(str, ",");

            boolean cont = true;
            while(cont)
            {
                try
                {
                    jArray.add(Integer.parseInt(s.nextToken()));
                } catch(NumberFormatException e)
                {
                    return false;
                } catch(NoSuchElementException e)
                {
                    cont = false;
                }
            }
        } else
        {
//            Platform.runLater(() ->
//            {
//                tfParametros.setText("0");
//            });
//            jArray.add(0);
        }

        return true;
    }

    private void listenButton()
    {
        btSolicitar.disableProperty().bind(Response.getInstance().waitProperty());
        btSolicitarCod.disableProperty().bind(Response.getInstance().waitProperty());
        txAreaCode.disableProperty().bind(Response.getInstance().waitProperty());
        tfClasse.disableProperty().bind(Response.getInstance().waitProperty());
        tfversao.disableProperty().bind(Response.getInstance().waitProperty());
        tfMethod.disableProperty().bind(Response.getInstance().waitProperty());
        tfParametros.disableProperty().bind(Response.getInstance().waitProperty());
    }

    @FXML
    void solicitar(ActionEvent event)
    {
        JsonObject jObj = new JsonObject();
        JsonArray jArray = new JsonArray();

        if(!tfClasse.getText().toString().equals(""))
        {
            if(!tfMethod.getText().toString().equals(""))
            {
                if(capturaValores(jArray))
                {
                    try
                    {
                        jObj.add("success", true);
                        jObj.add("op", "request");
                        jObj.add("class", tfClasse.getText().toString());
                        jObj.add("method", tfMethod.getText().toString());
                        //                    jObj.add("version", 1);
                        jObj.add("params", jArray);

                        Response.getInstance().setWait(true);

                        con.getConnectInstance().sendJson(jObj);
                    } catch(NumberFormatException x)
                    {
                        mensagemErro("Dados Incorretos");
                    }
                } else
                {
                    mensagemErro("Parametros Errados");
                }
            } else
            {
                mensagemErro("Campo Metodo Vazio");
            }
        } else
        {
            mensagemErro("Campo Classe Vazio");
        }
    }

    @FXML
    void solicitarCod(ActionEvent event)
    {
        JsonObject jObj = new JsonObject();
        JsonArray jArray = new JsonArray();

        if(!tfClasse.getText().toString().equals(""))
        {
            if(!tfMethod.getText().toString().equals(""))
            {
                if(!txAreaCode.getText().toString().equals(""))
                {
                    if(!tfversao.getText().toString().equals(""))
                    {
                        if(capturaValores(jArray))
                        {
                            try
                            {
                                jObj.add("success", true);
                                jObj.add("op", "newclass");
                                jObj.add("class", tfClasse.getText().toString());
                                jObj.add("method", tfMethod.getText().toString());
                                jObj.add("version", Integer.parseInt(tfversao.getText().toString()));
                                jObj.add("code", txAreaCode.getText().toString());
                                jObj.add("params", jArray);

                                Response.getInstance().setWait(true);

                                con.getConnectInstance().sendJson(jObj);
                                
                                if(dynamicClassManager.verificaVersaoMaior(tfClasse.getText().toString(), Integer.parseInt(tfversao.getText().toString())))
                                {
                                    dynamicClassManager.remove(tfClasse.getText().toString());
                                    dynamicClassManager.insert(tfClasse.getText().toString(), txAreaCode.getText().toString(), Integer.parseInt(tfversao.getText().toString()));
                                }
                                
                            } catch(NumberFormatException x)
                            {
                                mensagemErro("Dados Incorretos");
                            }
                        } else
                        {
                            mensagemErro("Parametros Errados");
                        }
                    } else
                    {
                        mensagemErro("Campo Versão Vazio");
                    }
                } else
                {
                    mensagemErro("Campo Código Vazio");
                }
            } else
            {
                mensagemErro("Campo Metodo Vazio");
            }
        } else
        {
            mensagemErro("Campo Classe Vazio");
        }
    }

    private void mensagemErro(String str)
    {
        Stage dialog = new Stage();
        dialog.setHeight(85);
        dialog.setWidth(200);
        dialog.setTitle("Erro");
        dialog.initStyle(StageStyle.UTILITY);
        Scene scene = new Scene(new Group(new Text(50, 25, str)));
        dialog.setScene(scene);
        dialog.show();
    }
}
