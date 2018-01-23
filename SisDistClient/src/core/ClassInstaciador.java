/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package core;

/**
 *
 * @author UFOP
 */
public class ClassInstaciador extends ClassLoader
{
    public ClassInstaciador(ClassLoader parent)
    {
        super(parent);
    }

    public Class loadClass(String name, String buffer) throws ClassFormatError
    {
        byte[] classData = buffer.getBytes();

        return defineClass("codes.Math2", classData, 0, classData.length);
    }
}
