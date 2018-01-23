/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package core.cache;

import java.util.HashMap;
import lib.json.JsonArray;

/**
 *
 * @author renee
 */
public class CacheClassStruct {

    private final String name;
    private int version;
    private final HashMap<String, CacheMethodStruct> methodHash;

    public CacheClassStruct(String name) {
        this.name = name;
        methodHash = new HashMap<>();
    }

    public String getName() {
        return name;
    }

    public int getVersion() {
        return version;
    }

    public void newVersion(int ver) {
        methodHash.clear();
        version = ver;
    }

    public String hasMethod(String method, JsonArray par) {
        if (methodHash.containsKey(method)) {
//            System.out.println("Metodo"+ methodHash.get(method));
            return methodHash.get(method).getResponse(par);
        }
        return null;
    }

    public void newResponse(String method, JsonArray par, String value) {
        String response = null;
        if (methodHash.containsKey(par.toString())) {
            response = methodHash.get(method).getResponse(par);
        }
        if (response == null) {
            methodHash.put(method, new CacheMethodStruct(method, par, value));
        } else if (!response.equals(value)) {
            methodHash.get(method).updateResponse(par, value);
        }
    }
}
