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
public class DynamicClass
{
    private String name;
    private String codigo;
    private int versao;

    public DynamicClass(String name, String codigo, int versao)
    {
        this.name = name;
        this.codigo = codigo;
        this.versao = versao;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getCodigo()
    {
        return codigo;
    }

    public void setCodigo(String codigo)
    {
        this.codigo = codigo;
    }

    public int getVersao()
    {
        return versao;
    }

    public void setVersao(int versao)
    {
        this.versao = versao;
    }
}
