/*
 *File:StreamHelper.java
 *company:ECIQ
 *@version: 1.0
 *Date:2014-8-29
 */
package com.higgs.base.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.sql.Blob;
import java.sql.SQLException;

/**
 * 流处理帮助类
 * 
 * @author terry
 * @since 1.0
 */
public class StreamHelper
{
    /**
     * inputStream转String
     * 
     * @param in
     * @param format 字符格式
     * @return
     * @throws IOException
     */
    public static String inputStream2String(InputStream in, String format)
        throws IOException
    {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] data = new byte[8192];
        int count = -1;
        while ((count = in.read(data, 0, 8192)) != -1)
            outStream.write(data, 0, count);
        
        data = null;
        String result;
        if (StringHelper.isNotEmpty(format))
        {
            result = new String(outStream.toByteArray(), format);
        }
        else
        {
            result = new String(outStream.toByteArray());
        }
        return result;
    }
    
    /**
     * String转inputStream
     * 
     * @param str
     * @param format 字符格式
     * @return
     * @throws IOException
     */
    public static InputStream string2InputStream(String str, String format)
        throws IOException
    {
        InputStream in;
        if (StringHelper.isNotEmpty(format))
        {
            in = new ByteArrayInputStream(str.getBytes(format));
        }
        else
        {
            in = new ByteArrayInputStream(str.getBytes());
        }
        
        return in;
    }
    
    /**
     * 将InputStream转换成byte数组
     * 
     * @param in
     * @return
     * @throws IOException
     */
    public static byte[] inputStreamToByte(InputStream in)
        throws IOException
    {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] data = new byte[8192];
        int count = -1;
        while ((count = in.read(data, 0, 8192)) != -1)
            outStream.write(data, 0, count);
        data = null;
        return outStream.toByteArray();
    }
    
    /**
     * 将byte数组转换成InputStream
     * 
     * @param in
     * @return
     * @throws Exception
     */
    public static InputStream byteToInputStream(byte[] in)
        throws Exception
    {
        ByteArrayInputStream is = new ByteArrayInputStream(in);
        return is;
    }
    
    /**
     * byte转String
     * 
     * @param byteValue
     * @param format
     * @return
     * @throws UnsupportedEncodingException
     */
    public static String byte2String(byte[] byteValue, String format)
        throws UnsupportedEncodingException
    {
        if (null != byteValue)
        {
            return new String(byteValue, format);
        }
        return "";
    }
    
    /**
     * blob字段转成Base64后的字符串
     * 
     * @param blob
     * @param format
     * @return
     * @throws IOException
     * @throws SQLException
     */
    public static String blob2Base64(Blob blob, String format)
        throws IOException, SQLException
    {
        if (null != blob)
        {
            return SecurityHelper.encode2Base64(StreamHelper.inputStream2String(blob.getBinaryStream(), format), format);
        }
        
        return "";
    }
    
    /**
     * byte[]转Base64后的字符串
     * 
     * @param data
     * @param format
     * @return
     * @throws IOException
     * @throws SQLException
     */
    public static String byte2Base64(byte[] data, String format)
        throws IOException, SQLException
    {
        if (null != data)
        {
            return SecurityHelper.encode2Base64(StreamHelper.inputStream2String(new ByteArrayInputStream(data), format),
                format);
        }
        
        return "";
    }
    
    /**
     * 合并2个字节数组
     * 
     * @param byte_1
     * @param byte_2
     * @return
     */
    public static byte[] byteMerger(byte[] byte_1, byte[] byte_2)
    {
        byte[] byte_3 = new byte[byte_1.length + byte_2.length];
        System.arraycopy(byte_1, 0, byte_3, 0, byte_1.length);
        System.arraycopy(byte_2, 0, byte_3, byte_1.length, byte_2.length);
        return byte_3;
    }
}
