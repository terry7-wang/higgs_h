/*
 *File:SecurityHelper.java
 *company:ECIQ
 *@version: 1.0
 *Date:2014-8-24
 */
package com.higgs.base.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;

import com.higgs.base.constants.ExtConstants;

/**
 * 安全相关帮助类
 * 
 * @author terry
 * @since 1.0
 */
public class SecurityHelper
{
    /**
     * 获取文件的Md5值
     * 
     * @param file 文件
     * @return md5值
     * @throws IOException
     */
    public static String getFileMd5(File file)
        throws IOException
    {
        InputStream data = null;
        String md5 = "";
        try
        {
            data = new BufferedInputStream(new FileInputStream(file));
            md5 = DigestUtils.md5Hex(data).toUpperCase();
        }
        finally
        {
            FileHelper.closeStream(data, null);
        }
        return md5;
    }
    
    /**
     * 获取Md5加密后的字符串
     * 
     * @param str 字符串
     * @return md5值
     */
    public static String getStringMd5(String str)
    {
        return DigestUtils.md5Hex(str).toUpperCase();
    }
    
    /**
     * 获取16位的Md5加密后的字符串
     * 
     * @param str 字符串
     * @return md5值
     */
    public static String getString16Md5(String str)
    {
        return DigestUtils.md5Hex(str).toUpperCase().substring(8, 24);
    }
    
    /**
     * 获取Md5加密后的字符串
     * 
     * @param byteValue byte字节数组
     * @return md5值
     */
    public static String getByteMd5(byte[] byteValue)
    {
        return DigestUtils.md5Hex(byteValue).toUpperCase();
    }
    
    /**
     * 获取Md5加密后的字符串
     * 
     * @param byteValue byte字节数组
     * @return md5值
     */
    public static byte[] getByteMd5ByByte(byte[] byteValue)
    {
        return DigestUtils.md5(byteValue);
    }
    
    /**
     * HMAC-MD5算法
     * @param data
     * @param key
     * @return
     * @throws Exception
     */
    public static byte[] encryptHMAC(byte[] data, String key)
        throws Exception
    {
        SecretKey sk = new SecretKeySpec(key.getBytes(), "HmacMD5");
        Mac mac = Mac.getInstance(sk.getAlgorithm());
        mac.init(sk);
        return mac.doFinal(data);
    }
    
    /**
     * 获得对应Base64加密后的字符串
     * 
     * @param data
     * @return
     * @throws UnsupportedEncodingException
     */
    public static String encode2Base64(String str, String encode)
    {
        if (StringHelper.isEmpty(str))
        {
            return "";
        }
        
        if (StringHelper.isEmpty(encode))
        {
            encode = ExtConstants.DEF_ENCODE;
        }
        String encodeStr = "";
        try
        {
            encodeStr = Base64.encodeBase64String(str.getBytes(encode));
        }
        catch (UnsupportedEncodingException e)
        {
            LogHelper.logError(e, "编码转换错误");
        }
        return encodeStr;
    }
    
    /**
     * 解码Base64加密后的字符串
     * 
     * @param str 加密字符串
     * @return 解码后的字符串
     * @throws IOException
     */
    public static String decode4Base64(String str, String encode)
    {
        if (StringHelper.isEmpty(str))
        {
            return "";
        }
        
        if (StringHelper.isEmpty(encode))
        {
            encode = ExtConstants.DEF_ENCODE;
        }
        
        // BASE64Decoder decoder = new BASE64Decoder();
        byte[] data = Base64.decodeBase64(str);
        String decodeStr = "";
        try
        {
            decodeStr = new String(data, encode);
        }
        catch (UnsupportedEncodingException e)
        {
            LogHelper.logError(e, "编码转换错误");
        }
        return decodeStr;
    }
    
    /**
     * 获取SHA256编码后的字符串
     * @param strByte
     * @return
     */
    public static byte[] getSHA256String(byte[] strByte)
    {
        return DigestUtils.sha256(strByte);
    }
    
    public static void main(String[] args)
        throws Exception
    {
    }
    
}
