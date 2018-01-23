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
public class DynamicResultsCache {

    private final HashMap<String, CacheClassStruct> classHash;


    private DynamicResultsCache() {
        classHash = new HashMap<>();
    }
    
    public static DynamicResultsCache getInstance() {
        return DynamicResultsHolder.INSTANCE;
    }

    private static class DynamicResultsHolder {

        private static final DynamicResultsCache INSTANCE = new DynamicResultsCache();
    }
    

    public CacheClassStruct hasClass(String name) {
        if (classHash.containsKey(name)) {
            return classHash.get(name);
        }
        return null;
    }

    public void newClass(String name) {
        System.out.println("New class on cache: "+name);
        classHash.put(name, new CacheClassStruct(name));
    }

    public String getResponse(String cl, int version, String meth, JsonArray par) {
        CacheClassStruct c = hasClass(cl);
        if (c != null && c.getVersion() >= version) {
            return c.hasMethod(meth, par);
        }
        return null;
    }

    public void newResponse(String cl, int version, String meth, JsonArray par, String val) {
        System.out.println("New response on cache: "+cl+" - "+meth +"-"+val );
        if (val != null) {
            CacheClassStruct c = hasClass(cl);
            if (c == null) {
                newClass(cl);
                c = hasClass(cl);
            } else if (c != null && c.getVersion() < version) {
                c.newVersion(version);
            }
            c.newResponse(meth, par, val);
        }
    }

}
