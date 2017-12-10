/*
 *File:DateHelper.java
 *company:ECIQ
 *@version: 1.0
 *Date:2014-6-13
 */
package com.higgs.base.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 
 * @author terry
 * @since 1.0
 */
public class DateHelper
{
    public final static String FORMAT_DATE = "yyyy-MM-dd";
    
    public final static String FORMAT_TIME = "yyyy-MM-dd HH:mm:ss";
    
    public static String getFormatDate(Date date, String formatter)
    {
        if (date == null || StringHelper.isEmpty(formatter))
        {
            return "";
        }
        
        SimpleDateFormat sdf = new SimpleDateFormat(formatter);
        return sdf.format(date);
    }
    
    public static Date strToDate(String strDate, String format)
    {
        if (StringHelper.isEmpty(strDate) || StringHelper.isEmpty(format))
        {
            return null;
        }
        Date date = null;
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        try
        {
            date = sdf.parse(strDate);
        }
        catch (ParseException e)
        {
            e.printStackTrace();
        }
        return date;
    }
    
    /**
     * 格式化日期
     * 
     * @param date
     * @param format
     * @return
     */
    public static Date formatDate(Date date, String format)
    {
        String strDate = getFormatDate(date, format);
        date = strToDate(strDate, format);
        
        return date;
    }
    
    /**
     * 获取传入的N个月后的时间
     * @return
     */
    public static Date getNumMonthAfterCurrentTime(int num)
    {
        Calendar cal = Calendar.getInstance();
        cal.add(cal.MONTH, num);
        return cal.getTime();
    }
    
    public static void main(String[] args)
    {
        String str = DateHelper.getFormatDate(new Date(), DateHelper.FORMAT_TIME);
        System.out.println(str);
    }
}
