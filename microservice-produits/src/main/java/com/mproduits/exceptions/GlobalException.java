/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mproduits.exceptions;



/**
 *
 * @author fabrice
 * Cette classe est celle qui va servir a tracker les erreurs non controlees de l'application
 */  

public class GlobalException extends RuntimeException{
 
  /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
private Object[] param;
    private String code="";
    /**
     * Creates a new instance of
     * <code>GlobalException</code> without detail message.
     */
    public GlobalException() {
    }

    /**
     * Constructs an instance of
     * <code>GlobalException</code> with the specified detail message.
     *
     * @param msg the detail message.
     */
 
    public GlobalException(String code, String msg) {
        super(msg);
        this.code=code;
        
    }
    public GlobalException(String code) {
        this.code=code;
        
    }

    public GlobalException(String code,Object[] param) {
        this.code=code;
        this.param = param;
    }
    public GlobalException(String code,Object[] param,String msg) {
        super(msg);
        this.code=code;
        this.param = param;
    }

    /**
     * @return the code
     */
    public String getCode() {
        return code;
    }

    /**
     * @param code the code to set
     */
    public void setCode(String code) {
        this.code = code;
    }

    public Object[] getParam() {
        return param;
    }

    public void setParam(Object[] param) {
        this.param = param;
    }
    

}
