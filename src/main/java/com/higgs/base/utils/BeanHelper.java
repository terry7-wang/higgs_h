package com.higgs.base.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.converters.DateConverter;
import org.apache.commons.beanutils.converters.DoubleConverter;
import org.apache.commons.beanutils.converters.IntegerConverter;
import org.apache.commons.beanutils.converters.LongConverter;
import org.dom4j.Element;

public class BeanHelper extends BeanUtils
{
    static
    {
        // 注册日期转换器
        DateConverter converter = new DateConverter(null); // 设置默认值，否则解析空字符串时会出现异常
        converter.setPatterns(new String[] {"yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd", "yyyy/MM/dd"});
        ConvertUtils.register(converter, Date.class);
        
        IntegerConverter intConverter = new IntegerConverter(null);
        ConvertUtils.register(intConverter, Integer.class);
        
        LongConverter longConverter = new LongConverter(null);
        ConvertUtils.register(longConverter, LongConverter.class);
        
        DoubleConverter doubleConverter = new DoubleConverter(null);
        ConvertUtils.register(doubleConverter, Double.class);
    }
    
    /**
     * 复制Bean上的所有属性
     * 
     * @param dest 目标对象
     * @param orig 源对象
     */
    public static void copyProperties(Object dest,Object orig)
    {
        if (dest == null || orig == null)
            return;
        
        try
        {
            BeanUtils.copyProperties(dest, orig);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
    
    /**
     * 设置属性值
     * 
     * @param bean 目标对象
     * @param propName 属性名称
     * @param value 值
     */
    public static void setProperty(Object bean,String propName,Object value)
    {
        try
        {
            BeanUtils.setProperty(bean, propName, value);
        }
        catch (Exception ex)
        {
            System.err.println("属性【" + propName + "】设值时出现错误！value的类型为：" + value.getClass());
        }
    }
    
    /**
     * 获取属性值
     * 
     * @param bean 目标对象
     * @param propName 属性名称
     * @return 值
     */
    public static String getProperty(Object bean,String propName)
    {
        if (bean == null || StringHelper.isEmpty(propName))
            return "";
        
        String value = null;
        try
        {
            value = BeanUtils.getProperty(bean, propName);
        }
        catch (Exception ex)
        {
            System.err.println("属性【" + propName + " 】取值时出现错误！");
        }
        return value;
    }
    
    /**
     * 复制一个实体
     * 
     * @param srcBean 源实体
     * @return 复制对象
     */
    public static Object cloneBean(Object srcBean)
    {
        if (srcBean == null)
            return null;
        
        Object cloneBean = null;
        try
        {
            cloneBean = BeanUtils.cloneBean(srcBean);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return cloneBean;
    }
    
    /**
     * 返回对象上所有声明的属性，其中声明为 static 的字段会被过滤
     * 
     * @param clz
     * @return
     */
    public static List<String> getPropertyNames(Class<?> clz)
    {
        Field[] fields = clz.getDeclaredFields();
        List<String> fieldList = new ArrayList<String>();
        
        for (Field field : fields)
        {
            Class<?> fieldType = field.getType(); // 属性所使用的Java类型
            if (Modifier.isStatic(field.getModifiers()) || Iterable.class.isAssignableFrom(fieldType)
                || Map.class.isAssignableFrom(fieldType))
                continue;
            
            fieldList.add(field.getName());
        }
        
        return fieldList;
    }
    
    /**
     * 获取所有声明的属性，包含从父类上继承得到的属性，但子类覆盖父类的属性会被去重复
     * 
     * @param cls 指定类
     * @param filterStatic 是否过滤静态属性
     * @param fliterCollection 是否过滤集合属性，包括了List、Set、Map
     * @return
     */
    public static List<Field> getProperties(Class<?> cls,boolean filterStatic,boolean fliterCollection)
    {
        List<Field> list = new ArrayList<Field>();
        Field[] fields = null;
        
        while (cls != null)
        {
            fields = cls.getDeclaredFields();
            for (Field field : fields)
            {
                if (filterStatic && Modifier.isStatic(field.getModifiers())) // 过滤静态属性
                    continue;
                
                Class<?> fieldType = field.getType(); // 属性所使用的Java类型
                if (fliterCollection && (Iterable.class.isAssignableFrom(fieldType) || Map.class.isAssignableFrom(fieldType))) // 过滤集合
                    continue;
                
                list.add(field);
            }
            
            cls = cls.getSuperclass(); // 迭代遍历
        }
        
        // 去除重复一致的属性（子类覆盖的属性）
        Set<Field> set = new TreeSet<Field>(new Comparator<Field>()
        {
            public int compare(Field o1,Field o2)
            {
                String name1 = o1.getName();
                String name2 = o2.getName();
                return name1.compareToIgnoreCase(name2);
            }
        });
        
        set.addAll(list); // 添加至Set 中，过滤重复属性，并排序
        list.clear();
        list.addAll(set);
        return list;
    }
    
    /**
     * 返回对象上所有声明的属性的Db形式，其中声明为 static 的字段会被过滤
     * 
     * @param clz
     * @return
     */
    public static List<String> getBeanProperties2DbStyle(Class<?> clz)
    {
        Field[] fields = clz.getDeclaredFields();
        List<String> fieldList = new ArrayList<String>();
        for (Field field : fields)
        {
            if (Modifier.isStatic(field.getModifiers())) // 过滤静态属性
                continue;
            
            Class<?> fieldType = field.getType(); // 属性所使用的Java类型
            if (Iterable.class.isAssignableFrom(fieldType) || Map.class.isAssignableFrom(fieldType)) // 过滤集合
                continue;
            
            fieldList.add(StringHelper.toDbColumnStyle(field.getName()));
        }
        return fieldList;
    }
    
    /**
     * 格式化实体中的String类型的字段的值，该方法常用于xml输出时使用
     * 
     * @param bean 对应实体
     * @param defalutStr 默认值
     */
    public static void formatBean4StrField(Object bean,String defalutStr)
    {
        if (bean == null)
            return;
        
        if (defalutStr == null)
            defalutStr = "";
        
        Class<?> clz = bean.getClass();
        List<String> fields = BeanHelper.getPropertiesByClass(clz, String.class);
        for (String field : fields)
        {
            String value = BeanHelper.getProperty(bean, field);
            if (value == null)
                BeanHelper.setProperty(bean, field, defalutStr);
        }
    }
    
    /**
     * 获取Bean上指定类型的属性名
     * 
     * @param clz 目标类
     * @param namedClz 指定的属性类型
     * @return
     */
    public static List<String> getPropertiesByClass(Class<?> clz,Class<?> namedClz)
    {
        Field[] fields = clz.getDeclaredFields(); // 获得所有声明过的字段
        List<String> list = new ArrayList<String>();
        
        for (Field field : fields)
        {
            // 静态的字段不进行比较
            if (Modifier.isStatic(field.getModifiers()))
                continue;
            
            // 如果字段的声明的类型和指定的类型一致，加入集合
            if (namedClz.isAssignableFrom(field.getType()))
                list.add(field.getName());
        }
        
        return list;
    }
    
    /**
     * 通过Request对象，指定往Bean中注入值
     * 
     * @param bean
     * @param request
     * @param paramPrefix
     */
    public static void setPropertiesByRequest(Object bean,HttpServletRequest request,String paramPrefix)
    {
        if (bean == null || request == null)
            return;
        
        Map<String, Object> reqPararms = WebHelper.getParametersStartingWith(request, paramPrefix);
        
        for (Map.Entry<String, Object> paramEntry : reqPararms.entrySet())
        {
            String paramName = paramEntry.getKey();
            String paramValue = StringHelper.toStrWithDef(paramEntry.getValue(), "");
            if (StringHelper.isEmpty(paramValue))
                paramValue = null;
            
            try
            {
                setProperty(bean, paramName, paramValue);
            }
            catch (Exception ex)
            {
                String msg = "类[" + bean.getClass() + "]的属性:" + paramName + "值转换时出现异常！";
                System.err.println(msg);
            }
        }
    }
    
    /**
     * 通过Request对象，指定往Bean中注入值
     * 
     * @param bean 目标对象
     * @param request req请求
     * @throws ParseException
     */
    @Deprecated
    public static void setPropertiesByRequest(Object bean,HttpServletRequest request)
    {
        if (bean == null || request == null)
            return;
        
        Enumeration<?> reqPararms = request.getParameterNames();
        
        while (reqPararms.hasMoreElements())
        {
            String paramName = reqPararms.nextElement().toString();
            String paramValue = request.getParameter(paramName);
            try
            {
                setProperty(bean, paramName, paramValue);
            }
            catch (Exception ex)
            {
                String msg = "类[" + bean.getClass() + "]的属性:" + paramName + "值转换时出现异常！";
                System.err.println(msg);
            }
        }
    }
    
    /**
     * 从Request中抽取Bean所对应的参数，抽取后，String类型的参数将转换成Bean上同属性所对应的数据类型
     * 
     * @param request 请求
     * @param prefix 参数前缀
     * @param cls 参数对应的类
     * @return
     */
    public static Map<String, Object> extractBeanParamsFromRequest(HttpServletRequest request,String prefix,Class<?> cls)
    {
        Map<String, Object> paramMap = WebHelper.getParametersStartingWith(request, prefix);
        Map<String, Object> resultMap = new HashMap<String, Object>();
        
        Field field = null;
        Class<?> fieldType = null;
        Object value = null;
        
        for (Map.Entry<String, Object> paramEntry : paramMap.entrySet())
        {
            String paramName = paramEntry.getKey();
            String paramValue = StringHelper.toStrWithDef(paramEntry.getValue(), "");
            
            // 如果查询参数值为空，略过
            if (StringHelper.isEmpty(paramValue))
                continue;
            
            // 寻找目标类上是否存在参数所对应的字段
            try
            {
                field = cls.getDeclaredField(paramName);
            }
            catch (Exception e)
            {
                continue; // 如果没有找到对应的字段，略过
            }
            
            try
            {
                // 如果存在对应字段，则获取对应字段的声明类型，将字符形式的参数值转换成对应的类型的值，存放在映射中
                fieldType = field.getType();
                value = ConvertUtils.convert(paramValue, fieldType);
                if (value != null)
                    resultMap.put(paramName, value);
            }
            catch (Exception ex)
            {
                String msg = "类[" + cls + "]的属性:" + paramName + "值转换时出现异常！";
                System.err.println(msg);
            }
        }
        
        return resultMap;
    }
    
    /**
     * 通过Request对象，指定往Map中注入值
     * 
     * @param request 请求
     * @param map 参数映射
     * @param cls 反射取属性需要的类
     */
    @Deprecated
    public static void putMapByRequest(HttpServletRequest request,Map<String, Object> map,Class<?> cls)
    {
        if (map == null || request == null)
            return;
        
        Field field = null;
        Class<?> fieldType = null;
        Object value = null;
        
        Enumeration<?> reqPararms = request.getParameterNames(); // 获取request中的所有请求参数
        
        while (reqPararms.hasMoreElements())
        {
            String paramName = reqPararms.nextElement().toString();
            String paramValue = request.getParameter(paramName);
            
            // 如果查询参数值为空，略过
            if (StringHelper.isEmpty(paramValue))
                continue;
            
            // 寻找目标类上是否存在参数所对应的字段
            try
            {
                field = cls.getDeclaredField(paramName);
            }
            catch (Exception e)
            {
                continue; // 如果没有找到对应的字段，略过
            }
            
            try
            {
                // 如果存在对应字段，则获取对应字段的声明类型，将字符形式的参数值转换成对应的类型的值，存放在映射中
                fieldType = field.getType();
                value = ConvertUtils.convert(paramValue, fieldType);
                if (value != null)
                    map.put(paramName, value);
            }
            catch (Exception ex)
            {
                String msg = "类[" + cls + "]的属性:" + paramName + "值转换时出现异常！";
                System.err.println(msg);
            }
        }
    }
    
    /**
     * 将Xml结点的结点元素值赋值到对应对象上，仅支持节点上的元素写法为数据库写法的结点 如 BEAN_TYPE
     * 
     * @param entity
     * @param entityEle
     */
    public static void setPropertiesByXmlElement(Object entity,Element entityEle)
    {
        if (entity == null || entityEle == null)
            return;
        
        // 获取该结点下的所有属性结点
        for (Element propertyEle : (List<Element>)entityEle.elements())
        {
            // 如果该结点无子结点，说明该属性为单对象属性，直接进行赋值
            if (CollectionHelper.isEmpty(propertyEle.elements()))
            {
                // 通常，Xml上的属性结点都是类属性的数据库形式
                String propertyName = StringHelper.toBeanPropertyStyle(propertyEle.getName());
                BeanHelper.setProperty(entity, propertyName, propertyEle.getTextTrim());
            }
        }
    }
    
    /**
     * 将Orcale查询结果中的Oracle类型值 转换成对应的 Java 类型
     * 
     * @param convertClass
     * @param oracleValue
     * @return
     */
    public static Object convertOracleValue2JavaType(Class<?> convertClass,Object oracleValue)
    {
        if (convertClass == null)
            return oracleValue;
        
        Object destValue = null;
        if (oracleValue == null)
            return destValue;
        
        if (convertClass == Long.class && oracleValue.getClass() == BigDecimal.class)
        {
            BigDecimal numberValue = (BigDecimal)oracleValue;
            destValue = numberValue.longValue();
        }
        else if (convertClass == Double.class && oracleValue.getClass() == BigDecimal.class)
        {
            BigDecimal numberValue = (BigDecimal)oracleValue;
            destValue = numberValue.doubleValue();
        }
        
        return destValue;
    }
    
    /**
     * 获取类的泛型对象 A extend B<String> 获取T的实际类型:String
     * 
     * @param cls
     * @return
     */
    public static Type getGenericClass(Class<?> cls)
    {
        // 获得类对应的泛型参数的实际类型
        ParameterizedType genType = (ParameterizedType)(cls.getGenericSuperclass());
        return (genType.getActualTypeArguments()[0]);
    }
}
