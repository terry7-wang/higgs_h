package com.higgs.base.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

/**
 * 继承StringUtils的集合助手类
 * 
 * @author terry
 * @since 1.0
 */
public class StringHelper extends StringUtils
{
    
    /**
     * 判断目标字符串数据中存在有为空的字符
     * 
     * @param strs 目标字符串数组
     * @return 是否含空串
     */
    public static boolean hasEmpty(String... strs)
    {
        boolean flag = false;
        for (String str : strs)
        {
            flag = isEmpty(str);
            if (flag)
            {
                break;
            }
        }
        return flag;
    }
    
    /**
     * 判断目标字符串数据中，是否均为非空字符串
     * 
     * @param strs 目标字符串数组
     * @return 是否均为非空字符串
     */
    public static boolean bothNotEmpty(String... strs)
    {
        boolean flag = true;
        for (String str : strs)
        {
            flag = isNotEmpty(str);
            if (!flag)
            {
                break;
            }
        }
        return flag;
    }
    
    /**
     * 输出对象的toString()方法，该方法如果obj为null，输出defStr的内容，多用于文本的输出和输入
     * 
     * @param obj 目标对象
     * @param defStr 默认的字符串，为空时，默认为""
     * @return toString方法的字符串，如果对象为null，转换成defStr，
     */
    public static String toStrWithDef(Object obj,String defStr)
    {
        if (StringUtils.isEmpty(defStr))
        {
            defStr = "";
        }
        return obj == null ? defStr : obj.toString();
    }
    
    /**
     * 判断字符数组中，是否存在目标字符串
     * 
     * @param src 匹配的目标字符串
     * @param strs 字符数组
     * @return 是否存在
     */
    public static boolean existsInArray(String src,String[] strs)
    {
        return existsInArray4Case(src, strs, false);
    }
    
    /**
     * 判断字符数组中，是否存在目标字符串
     * 
     * @param src 匹配的目标字符串
     * @param strs 字符数组
     * @return 是否存在
     */
    public static boolean existsInArray4Case(String src,String[] strs,boolean matchCase)
    {
        if ((isEmpty(src)) || (strs == null))
        {
            return false;
        }
        
        boolean flag = false;
        for (String str : strs)
        {
            if (matchCase)
            {
                flag = equals(src, str);
            }
            else
            {
                flag = equalsIgnoreCase(src, str);
            }
            
            if (flag)
            {
                break;
            }
        }
        return flag;
    }
    
    /**
     * 判断目标字符串中，是否包含组中的任意一个关键字
     * 
     * @param src 搜索源
     * @param keys 关键字数组
     * @return 是否包含
     */
    public static boolean containsAnyKey4Case(String src,String[] keys,boolean matchCase)
    {
        if ((isEmpty(src)) || (keys == null))
        {
            return false;
        }
        
        boolean flag = false;
        for (String key : keys)
        {
            if (matchCase)
            {
                flag = contains(src, key);
            }
            else
            {
                flag = containsIgnoreCase(src, key);
            }
            
            if (flag)
            {
                break;
            }
        }
        return flag;
    }
    
    /**
     * 转换Bean的属性名为对应的数据库的字段名，例如propName -> PROP_NAME
     * 
     * @param propName 属性名
     * @return 对应的数据库字段名
     */
    public static String toDbColumnStyle(String propName)
    {
        if (isEmpty(propName))
        {
            return "";
        }
        
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < propName.length(); i++)
        {
            char cur = propName.charAt(i);
            if (Character.isUpperCase(cur))
            {
                sb.append("_");
                sb.append(cur);
            }
            else
            {
                sb.append(cur);
            }
        }
        return sb.toString().toUpperCase();
    }
    
    /**
     * 将字段名转成
     * 
     * @param fieldName
     * @return
     */
    public static String toBeanPropertyStyle(String fieldName)
    {
        if (isEmpty(fieldName))
        {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        boolean flag = false;
        for (int i = 0; i < fieldName.length(); i++)
        {
            char cur = fieldName.charAt(i);
            if (cur == '_')
            {
                flag = true;
            }
            else
            {
                if (flag)
                {
                    sb.append(Character.toUpperCase(cur));
                    flag = false;
                }
                else
                {
                    sb.append(Character.toLowerCase(cur));
                }
            }
        }
        return sb.toString();
    }
    
    /**
     * 首字母小写
     * 
     * @param str
     * @return
     */
    public static String toFirstLowerCase(String str)
    {
        if (isEmpty(str))
        {
            return "";
        }
        String firstChar = str.substring(0, 1);
        return str.replaceFirst(firstChar, firstChar.toLowerCase());
    }
    
    /**
     * 字符的首字母大写
     * 
     * @param str
     * @return
     */
    public static String toFirstUpperCase(String str)
    {
        if (isEmpty(str))
        {
            return "";
        }
        String firstChar = str.substring(0, 1);
        return str.replaceFirst(firstChar, firstChar.toUpperCase());
    }
    
    /**
     * 转换数字为指定的数字格式，例如00.0
     * 
     * @param num 目标数字
     * @param formatter 格式
     * @return
     */
    public static String toStdIntStr(Integer num,String formatter)
    {
        DecimalFormat df = new DecimalFormat();
        df.applyPattern(formatter);
        return df.format(num);
    }
    
    /**
     * 转换数字为26个字母显示（ 字母26进制）
     * 
     * @param data
     * @return
     */
    public static String to26Letter(int data)
    {
        String[] arr = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W",
            "X", "Y", "Z"};
        
        int bit = data / 25;
        int rest = data % 25;
        String higher = "";
        if (bit >= 1)
        {
            higher = arr[bit - 1];
        }
        
        return higher + arr[rest];
    }
    
    /**
     * 获取句子的第1个单词
     * 
     * @param src 目标字符串
     * @return 首单词
     */
    public static String getFirstWord(String src)
    {
        Pattern pat = Pattern.compile("\\b\\w+\\b");
        Matcher mat = pat.matcher(src);
        return mat.find() ? mat.group() : "";
    }
    
    /**
     * 去除字符串的最后一位
     * 
     * @param str
     * @return
     */
    public static String removeLastSite(String str)
    {
        if (isNotEmpty(str))
        {
            return str.substring(0, str.length() - 1);
        }
        
        return "";
    }
    
    /**
     * 获取Utf-8编译后的字符串
     * 
     * @param str
     * @param isDecode 是否为解码，如果为false，则表示将目标内容编码成utf-8方式的字符串
     * @return
     */
    public static String getUtf8Str(String str,boolean isDecode)
    {
        if (isEmpty(str))
            return "";
        
        String returnStr = "";
        try
        {
            returnStr = isDecode ? URLDecoder.decode(str, "UTF-8") : URLEncoder.encode(str, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            String codingType = isDecode ? "解码" : "编码";
            LogHelper.logError(e, String.format("内容[%s]在进行%s时出现异常，原因：%s", new Object[] {str, codingType, e.getMessage()}));
        }
        
        return returnStr;
    }
    
    /**
     * equals对比多组值（|| 或的关系）
     * 
     * @param mainStr 主值
     * @param contrastStrs 对比值
     * @return
     */
    public static boolean equalsMultiOr(String mainStr,String... contrastStrs)
    {
        for (String contrast : contrastStrs)
        {
            if (contrast.equals(mainStr))
                return true;
        }
        return false;
    }
    
    /**
     * equals对比多组值（&& 与的关系）
     * 
     * @param mainStr 主值
     * @param contrastStrs 对比值
     * @return
     */
    public static boolean equalsMultiAnd(String mainStr,String... contrastStrs)
    {
        for (String contrast : contrastStrs)
        {
            if (!contrast.equals(mainStr))
                return false;
        }
        return true;
    }
    
    /**
     * 获取系统中常用代码的有效数字，规则为：以2位一个组，如果最后2位为00，则该2位为无效数字<br/>
     * 该方法常配合模糊查询来通过使用<br/>
     * 例如：120300 -> 1203
     * 
     * @param code
     * @return
     */
    public static String getCodeValidValue(String code)
    {
        if (isEmpty(code))
            return "";
        
        String lastTwoCode;
        int len;
        while ((len = code.length()) > 0)
        {
            lastTwoCode = code.substring(len - 2, len); // 截取最后2位
            if ("00".equals(lastTwoCode))
            {
                code = code.substring(0, len - 2);
            }
            else
                break;
        }
        return code;
    }
    
    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception
    {
        System.out.println(StringHelper.toBeanPropertyStyle("QUESTION_ANSWER"));
    }
    
}
