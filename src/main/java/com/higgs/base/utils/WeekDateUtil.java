/*
 *File:WeekDateUtil.java
 *company:higgs
 *@version: 1.0
 *Date:2016年1月21日
 */
package com.higgs.base.utils;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * 
 * @author terry
 * @since 1.0
 */
public class WeekDateUtil
{
    /**
     * @title 获取周六和周日是工作日的情况，注意，日期必须写全： 2016-1-1必须写成：2016-01-01
     * @return 周末是工作日的列表
     */
    private static List<String> getWeekendIsWorkDateList()
    {
        List<String> list = new ArrayList<String>();
        list.add("2016-01-24");
        return list;
    }
    
    /**
     * @title 获取周一到周五是假期的情况， 注意，日期必须写全： 2016-1-1必须写成：2016-01-01
     * @return 平时是假期的列表
     */
    private static List<String> getWeekdayIsHolidayList()
    {
        List<String> list = new ArrayList<String>();
        list.add("2016-01-28");
        list.add("2016-01-29");
        return list;
    }
    
    /**
     * @title 判断是否为工作日
     * @detail 工作日计算: 1、正常工作日，并且为非假期 2、周末被调整成工作日
     * @param calendar 日期
     * @return 是工作日返回true，非工作日返回false
     */
    public static boolean isWorkday(GregorianCalendar calendar)
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        if (calendar.get(GregorianCalendar.DAY_OF_WEEK) != GregorianCalendar.SATURDAY
            && calendar.get(GregorianCalendar.DAY_OF_WEEK) != GregorianCalendar.SUNDAY)
        {
            // 周一到周五，判断是否是假期
            return !getWeekdayIsHolidayList().contains(sdf.format(calendar.getTime()));
        }
        else
        {
            // 周末，判断是否工作日
            return getWeekendIsWorkDateList().contains(sdf.format(calendar.getTime()));
        }
    }
    
    /**
     * 获取指定天数后的工作日
     * 
     * @param days 指定天数
     * @return 指定天数后的工作日
     */
    public static Date getWorkdayAddDay(Date specifyDate, Integer days)
    {
        if (days != null)
        {
            try
            {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                GregorianCalendar gc = new GregorianCalendar();
                gc.setTime(sdf.parse(DateHelper.getFormatDate(specifyDate, "yyyy-MM-dd")));
                for (int i = 0; i < days; i++)
                {
                	//如果不是工作日，则i-1
                    if (!isWorkday(gc))
                    {
                    	i--;
                    }
                	gc.add(GregorianCalendar.DATE, 1);
                }
                return gc.getTime();
            }
            catch (ParseException e)
            {
                e.printStackTrace();
            }
        }
        return new Date();
    }
}
