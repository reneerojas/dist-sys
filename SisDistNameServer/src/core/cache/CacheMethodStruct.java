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
public class CacheMethodStruct {

    private final String method;
    private final HashMap<String, String> responses;

    public CacheMethodStruct(String meth, JsonArray par, String resp) {
        method = meth;
        responses = new HashMap<>();
        responses.put(par.toString(), resp);
    }

    public String getMethod() {
        return method;
    }

    public String getResponse(JsonArray par) {
//        System.out.println("respostas "+responses.toString());
        if (responses.containsKey(par.toString())) {
            System.out.println("RESOSTA");
            return responses.get(par.toString());
        } else {
            return null;
        }
    }

    public void newResponse(JsonArray par, String val) {
        System.out.println("nova resposta:" + par.toString() + " - " + val);
        if (!responses.containsKey(par.toString())) {
            responses.put(par.toString(), val);
        }
    }

    public void updateResponse(JsonArray par, String val) {
        if (!getResponse(par).equals(val)) {
            responses.remove(val);
            responses.put(par.toString(), val);
        }
    }

}
