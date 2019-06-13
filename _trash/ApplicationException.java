/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.piroyon.imagej;

/**
 *
 * @author hiroyo
 */
public class ApplicationException extends Exception {
    public ApplicationException() {
        super();
    }
    public ApplicationException(Throwable e) {
		super(e);
    }
    public ApplicationException(String mes) {
		super(mes);
    }
}