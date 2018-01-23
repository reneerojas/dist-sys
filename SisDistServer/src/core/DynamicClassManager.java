/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package core;

import com.ConnectionManager;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import lib.json.JsonArray;
import lib.json.JsonObject;

/**
 *
 * @author renee
 */
public class DynamicClassManager {

    final private HashMap<String, DynamicClass> classHash;
    private DynamicCompiler dynCompiler;

    private DynamicClassManager() {
        classHash = new HashMap<>();
        dynCompiler = DynamicCompiler.getInstance();
    }

    public static DynamicClassManager getInstance() {
        return ClassManagerHolder.INSTANCE;
    }

    private static class ClassManagerHolder {

        private static final DynamicClassManager INSTANCE = new DynamicClassManager();
    }

    /**
     * Possui classe nessa versao? retorna 0 (nao existe ou a versao da classe)
     *
     * @param name
     * @param versao
     * @return int
     */
    public int hasClass(String name) {
        if (classHash.containsKey(name)) {
            return classHash.get(name).getVersion();
        }
        return 0;
    }

    /**
     * Adiciona classe na hash
     *
     * @param name
     * @param versao
     * @param code
     */
    public void newClass(String name, int versao, String code) {
        classHash.put(name, new DynamicClass(name, versao, code));
        dynCompiler.initClass(classHash.get(name));
    }

    /**
     * Atualiza classe apenas se for versao superior
     *
     * @param name
     * @param versao
     * @param code
     */
    public void updateClass(String name, int versao, String code) {
        if (classHash.containsKey(name)) {
            DynamicClass dyn = classHash.get(name);
            if (dyn.getVersion() < versao) {

                //responde ao cache a nova versao do codigo
                JsonObject json = new JsonObject();
                json.add("success", true);
                json.add("op", "code");
                json.add("class", dyn.getName());
                json.add("version", dyn.getVersion());
                json.add("code", code);
                ConnectionManager.getInstance().getNameServerInstance().sendJson(json);

                dyn.versionProperty().set(versao);
                dyn.codeProperty().set(code);
                dynCompiler.initClass(dyn);
            }
        }else{
            newClass(name, versao, code);
        }
    }

    public DynamicClass getClass(String name) {
        return classHash.get(name);
    }

    public void execute(int req, DynamicClass dyn, String method, JsonArray params) {
        try {
            SocketChannel sc = ConnectionManager.getInstance().getRequest(req);

            //responde ao cliente
            String result = dynCompiler.run(dyn, method, params);
            ConnectionManager.getInstance().getServerInstance().response(sc, dyn.getName(), method, params, result);

            //responde ao cache novo valor
            JsonObject json = new JsonObject();
            json.add("success", true);
            json.add("op", "cache");
            json.add("op-cache", "add");
            json.add("class", dyn.getName());
            json.add("version", dyn.getVersion());
            json.add("method", method);
            json.add("params", params);
            json.add("result", result);
            ConnectionManager.getInstance().getNameServerInstance().sendJson(json);
        } catch (IOException ex) {
            Logger.getLogger(DynamicClassManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
