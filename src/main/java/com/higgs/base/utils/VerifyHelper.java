package com.higgs.base.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 校验工具类
 * 
 * @author terry
 * @since 1.0
 */
public class VerifyHelper
{
    /**
     * 验证邮箱的格式
     * 
     * @param email
     * @return
     */
    public static boolean isEmail(String email)
    {
        Pattern p = Pattern.compile("\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*");
        Matcher m = p.matcher(email);
        return m.find();
    }
    
    /**
     * 验证邮政编码
     * 
     * @param chinese
     * @return
     */
    public static boolean isPostalcode(String postalcode)
    {
        Pattern p = Pattern.compile("^[0-9 ]{6}$");
        Matcher m = p.matcher(postalcode);
        return m.find();
    }
    
    /**
     * 电话号码正则表达式（支持手机号码，3-4位区号，7-8位直播号码，1－4位分机号）
     * 
     * @param phone
     * @return
     */
    
    public static boolean isTelPhone(String tel)
    {
        Pattern p = Pattern.compile("^([0-9]{4}+-+[0-9]{8}|[0-9]{4}+-+[0-9]{7}|[0-9]{7}|[0-9]{11}|[0-9]{8})+$");
        Matcher m = p.matcher(tel);
        return m.find();
    }
    
    /**
     * 密码验证^([a-zA-Z0-9! @ # $ % ? _ - < > / | & * ( ) ^ ]){6,20}$
     */
    public static boolean isPwd(String pwd)
    {
        Pattern p = Pattern.compile("^([a-zA-Z0-9! @ # $ % ? _ - < > / | & * ( ) ^ ]){6,16}$");
        Matcher m = p.matcher(pwd);
        return m.find();
    }
    
    /**
     * 验证数字
     * 
     * @param str
     * @return
     */
    public static boolean isNumber(String str)
    {
        Pattern p = Pattern.compile("^[0-9-]+$");
        Matcher m = p.matcher(str);
        return m.find();
    }
    
    /**
     * 正整数
     * 
     * @param str
     * @return
     */
    public static boolean isPositiveNumber(String str)
    {
        Pattern p = Pattern.compile("^[0-9]*[1-9][0-9]*$");
        Matcher m = p.matcher(str);
        return m.find();
    }
    
    /**
     * 正整数和0 非负整数
     * 
     * @param str
     * @return
     */
    public static boolean isNonNegativeIntegers(String str)
    {
        Pattern p = Pattern.compile("^\\d+$");
        Matcher m = p.matcher(str);
        return m.find();
    }
    
    /**
     * 正浮点小数 小数点后两位数 小数点前8位数
     * 
     * @param str
     * @return
     */
    public static boolean ispositiveNumber2(String str)
    {
        Pattern p = Pattern.compile("^\\d{1,8}(\\.\\d{0,2})?$");
        Matcher m = p.matcher(str);
        return m.find();
    }
    
    /**
     * 身份证
     * @param idCard
     * @return
     */
    public static boolean isIdCard(String idCard)
    {
        Pattern p = null;
        Matcher m = null;
        if(idCard.length() == 15)
        {
            p = Pattern.compile("^[1-9]\\d{7}((0\\d)|(1[0-2]))(([0|1|2]\\d)|3[0-1])\\d{3}$");
            m = p.matcher(idCard);
            return m.find();
        }
        else if(idCard.length() == 18)
        {
            p = Pattern.compile("^[1-9]\\d{5}[1-9]\\d{3}((0\\d)|(1[0-2]))(([0|1|2]\\d)|3[0-1])\\d{4}$");
            m = p.matcher(idCard);
            return m.find();
        }

        return false;
    }
    
}
