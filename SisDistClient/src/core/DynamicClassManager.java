/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package core;

import java.util.HashMap;

/**
 *
 * @author Gustavo Amorim
 */
public class DynamicClassManager
{
    private HashMap<String, DynamicClass> hashClasses;

    private DynamicClassManager()
    {
        hashClasses = new HashMap<>();
    }

    public static DynamicClassManager getInstance()
    {
        return DynamicClassManagerHolder.INSTANCE;
    }

    private static class DynamicClassManagerHolder
    {

        private static final DynamicClassManager INSTANCE = new DynamicClassManager();
    }
    
    public void insert(String nome, String cod, int version)
    {
        hashClasses.put(nome, new DynamicClass(nome, cod, version));
    }
    
    public void remove(String nome)
    {
        hashClasses.remove(nome);
    }
    
    // retorna true caso contenha o nome e a versao passada for maior
    public boolean verificaVersaoMaior(String nome, int version)
    {
        if(hashClasses.containsValue(nome))
            if(hashClasses.get(nome).getVersao() < version)
                return true;
        
        return false;
    }

    public DynamicClass getDynamicClass(String name)
    {
        return hashClasses.get(name);
    }
}
