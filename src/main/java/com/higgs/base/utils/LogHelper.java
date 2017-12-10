/*
 *File:LogHelper.java
 *company:ECIQ
 *@version: 1.0
 *Date:2014-7-7
 */
package com.higgs.base.utils;

import org.apache.log4j.Logger;

/**
 * 日志记录
 * 
 * @author terry
 * @since 1.0
 */
public class LogHelper
{
    public static String LV_INFO = "INFO";
    public static String LV_DEBUG = "DEBUG";
    
    private static Logger logger = Logger.getLogger(LogHelper.class);
    
    /**
     * 记录日志消息，只支持记录info和debug级别，error级别使用logError方法
     * 
     * @param msg 消息内容
     * @param logLv 日志级别，例：LogHelper.LV_INFO
     */
    public static void logMsg(String msg,String logLv)
    {
        if (LV_INFO.equals(logLv))
        {
            logger.info(msg);
        }
        else
        {
            logger.debug(msg);
        }
    }
    
    /**
     * 记录异常信息
     * 
     * @param e 异常
     * @param msg 异常信息
     */
    public static void logError(Throwable e,String msg)
    {
        logger.error(msg, e);
        throw new RuntimeException(e);
    }
    
    /**
     * 记录异常信息
     * 
     * @param e 异常
     * @param msg 异常信息
     */
    public static void logRecord(Throwable e,String msg)
    {
        logger.error(msg, e);
    }
    
    
}
