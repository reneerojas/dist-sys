/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package core.cache;

import java.util.HashMap;

/**
 *
 * @author renee
 */
public class DynamicClassManager {

    final private HashMap<String, DynamicClass> classHash;

    private DynamicClassManager() {
        classHash = new HashMap<>();
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
                dyn.versionProperty().set(versao);
                dyn.codeProperty().set(code);
                System.out.println("Class "+name+" updated");
            }
        } else {
            newClass(name, versao, code);
                System.out.println("Class "+name+" created");
        }
    }

    public DynamicClass getClass(String name) {
        return classHash.get(name);
    }

}
