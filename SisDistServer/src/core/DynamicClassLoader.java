/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package core;

/**
 *
 * @author renee
 */
public class DynamicClassLoader extends ClassLoader {

    public DynamicClassLoader(ClassLoader parent) {
        super(parent);
    }


    public Class loadClass(String name, String code) throws ClassNotFoundException {

        byte[] classData = code.getBytes();

        return defineClass(name, classData, 0, classData.length);

    }

}
