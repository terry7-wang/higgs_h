/*
 *File:WebHelper.java
 *company:ECIQ
 *@version: 1.0
 *Date:2015-1-5
 */
package com.higgs.base.utils;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.util.WebUtils;

/**
 * Web开发助手类
 * @author terry
 * @since 1.0
 */
public class WebHelper extends WebUtils
{
    /**
     * 从Request中获取参数，该方法会自动对参数值进行utf-8的解码
     * @param request
     * @param paramName
     * @return
     */
    public static String getParameter(HttpServletRequest request,String paramName){
        return StringHelper.getUtf8Str(request.getParameter(paramName), true);
    }

    /**
     * 打印Request中的参数
     * 
     * @param request
     */
    public static void printRequestParamList(HttpServletRequest request)
    {
        Enumeration<?> paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements())
        {
            String paramName = (String)paramNames.nextElement();
            System.out.println(" 提交参数：键：" + paramName + "，值 : " + request.getParameter(paramName));
        }
    }
    
}
