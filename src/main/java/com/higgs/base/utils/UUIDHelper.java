/*
 *File:UUIDGenerator.java
 *company:ECIQ
 *@version: 1.0
 *Date:2014-6-16
 */
package com.higgs.base.utils;

import java.util.UUID;

/**
 *
 * @author terry
 * @since 1.0
 */
public class UUIDHelper {
    
    public static String generateUUID(){
            return UUID.randomUUID().toString().replace("-", "").toUpperCase();  
    }
    
    public static void main(String[] args) {
        System.out.println(UUIDHelper.generateUUID());
    }
    
}
