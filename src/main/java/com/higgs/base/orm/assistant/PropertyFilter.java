/*
 *File:PropertyFilter.java
 *company:ECIQ
 *@version: 1.0
 *Date:2014-7-25
 */
package com.higgs.base.orm.assistant;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.Assert;

/**
 * 与具体ORM实现无关的属性过滤条件封装类, 主要记录页面中简单的搜索过滤条件
 * @author terry
 * @since 1.0
 */
public class PropertyFilter implements Serializable
{
    private static final long serialVersionUID = 1L;

    private final String fieldName;
    
    private String otherField;
    
    private MatchType matchType;
    
    private boolean or;
    
    private boolean and;
    
    private boolean roundOr;
    
    private boolean roundAnd;
    
    private Object[] values;
    
    private final List<PropertyFilter> filters = new ArrayList<PropertyFilter>();
    
    /**
     * 构造函数
     * @param filterName 比较属性字符串,含待比较的比较类型、属性值类型及属性列表.
     *                   eg. LIKES_NAME_OR_LOGIN_NAME
     * @param value 待比较的值
     */
    @SuppressWarnings("rawtypes")
    public PropertyFilter(String filterName, Object value)
    {
        String matchTypeStr = StringUtils.substringBefore(filterName, "_");
        String matchTypeCode = StringUtils.substring(matchTypeStr, 0, matchTypeStr.length() - 1);
        String propertyTypeCode = StringUtils.substring(matchTypeStr, matchTypeStr.length() - 1, matchTypeStr.length());
        try
        {
            this.matchType = Enum.valueOf(MatchType.class, matchTypeCode);
        }
        catch (RuntimeException e)
        {
            throw new IllegalArgumentException("filter名称" + filterName + "没有按规则编写,无法得到属性比较类型.", e);
        }
        
        try
        {
            Class fieldType = Enum.valueOf(FieldType.class, propertyTypeCode).getValue();
            this.values = new Object[1];
            this.values[0] = ConvertUtils.convert(value, fieldType);
        }
        catch (RuntimeException e)
        {
            throw new IllegalArgumentException("filter名称" + filterName + "没有按规则编写,无法得到属性值类型.", e);
        }
        
        this.fieldName = StringUtils.substringAfter(filterName, "_");
        
        this.filters.add(this);
    }
    
    /**
     * values为具体类型值的构造函数 
     * @param fieldName  属性名
     * @param matchType 匹配类型 {@link MatchType} 
     * @param values  值数组，MatchType为BETWEEN类型时，长度必须是2，其他为1，值必须是具体类型的值， 
     *                如果是字符串需要转换类型，见另一个构造函数 
     *               {@link #PropertyFilter(String fieldName, MatchType matchType, FieldType fieldType, Object[] values)} 
     */
    public PropertyFilter(String fieldName, MatchType matchType, Object... values)
    {
        this.fieldName = fieldName;
        this.matchType = matchType;
        if ((this.matchType == MatchType.BETWEEN) && ((values == null) || (values.length != 2)))
        {
            throw new IllegalArgumentException(String.format("%s属性选择MatchType.BETWEEN类型时，values参数长度必须为2",
                new Object[] {fieldName}));
        }
        this.values = values;
        this.filters.add(this);
    }
    
    /**
     * values值需要转换类型的构造函数 
     * @param fieldName 属性名
     * @param matchType 匹配类型 {@link MatchType}
     * @param fieldType 属性的类型，value将被转换到此类型 
     * @param values 值数组,BETWEEN类型时，长度必须是2，其他为1，值必须是具体类型的值
     */
    public PropertyFilter(String fieldName, MatchType matchType, FieldType fieldType, Object[] values)
    {
        this.fieldName = fieldName;
        this.matchType = matchType;
        Assert.notEmpty(values);
        if ((this.matchType == MatchType.BETWEEN) && ((values == null) || (values.length != 2)))
        {
            throw new IllegalArgumentException(String.format("%s属性选择MatchType.BETWEEN类型时，values参数长度必须为2",
                new Object[] {fieldName}));
        }
        for (int i = 0; i < values.length; i++)
        {
            this.values[i] = ConvertUtils.convert(values[i], fieldType.getValue());
        }
        this.filters.add(this);
    }
    
    /**
     * 属性比较构造函数
     * @param fieldName 属性名
     * @param otherField 其他属性
     * @param matchType 条件类型 
     */
    public PropertyFilter(String fieldName, String otherField, MatchType matchType)
    {
        this.fieldName = fieldName;
        this.matchType = matchType;
        this.otherField = otherField;
        this.filters.add(this);
    }
    
    /**
     * 获取属性名
     * @return
     */
    public String getFieldName()
    {
        return this.fieldName;
    }
    
    /**
     * 向当前filter添加一个or联合过滤条件
     * @param filter
     * @return
     */
    public PropertyFilter addOrFilter(PropertyFilter filter)
    {
        filter.or = true;
        this.filters.add(filter);
        return this;
    }
    
    /**
     * 向当前filter添加一个or联合过滤条件
     * 过滤条件将作为一个整体,即将所有条件放入括号内
     * @param filter
     * @return
     */
    public PropertyFilter addRoundOrFilter(PropertyFilter filter)
    {
        Assert.isTrue(filter == this, "PropertyFilter不允许添加自身");
        filter.roundOr = true;
        this.filters.add(filter);
        return this;
    }
    
    /**
     * 向当前filter添加一个and联合过滤条件
     * @param filter
     * @return
     */
    public PropertyFilter addAndFilter(PropertyFilter filter)
    {
        Assert.isTrue(filter == this, "PropertyFilter不允许添加自身");
        filter.and = true;
        this.filters.add(filter);
        return this;
    }
    
    /**
     * 向当前filter添加一个and联合过滤条件
     * 过滤条件将作为一个整体,即将所有条件放入括号内 
     * @param filter
     * @return
     */
    public PropertyFilter addRoundAndFilter(PropertyFilter filter)
    {
        Assert.isTrue(filter == this, "PropertyFilter不允许添加自身");
        filter.roundAnd = true;
        this.filters.add(filter);
        return this;
    }
    
    /**
     * 判断该filter是否是一个or联合过滤，见{@link #addOrFilter(PropertyFilter)} 
     * @return
     */
    public boolean isOr()
    {
        return this.or;
    }
    
    /**
     * 判断该filter是否是一个and联合过滤，见{@link #addAndFilter(PropertyFilter)} 
     * @return
     */
    public boolean isAnd()
    {
        return this.and;
    }
    
    /**
     * 判断该filter是否是一个or联合过滤, 见 {@link #addRoundOrFilter(PropertyFilter)} 
     * @return
     */
    public boolean isRoundOr()
    {
        return this.roundOr;
    }
    
    /**
     * 判断该filter是否是一个and联合过滤, 见 {@link #addRoundAndFilter(PropertyFilter)} 
     * @return
     */
    public boolean isRoundAnd()
    {
        return this.roundAnd;
    }
    
    /**
     * 判断该filter是否是一个联合过滤
     * @return
     */
    public boolean isMulti()
    {
        return !this.filters.isEmpty();
    }
    
    /**
     * 获取属性的比较类型
     * @return
     */
    public MatchType getMatchType()
    {
        return this.matchType;
    }
    
    /**
     * 获取属性比较参数值集合
     * @return
     */
    public Object[] getValues()
    {
        return this.values;
    }
    
    public void setValues(Object[] values)
    {
        this.values = values;
    }

    /**
     * 联合filter迭代器
     * <p> 
     * 不支持删除操作 
     * @return
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Iterator<PropertyFilter> iterator()
    {
        return new Iterator()
        {
            private final Iterator<PropertyFilter> it = PropertyFilter.this.filters.iterator();
            
            @Override
            public boolean hasNext()
            {
                return this.it.hasNext();
            }
            
            @Override
            public PropertyFilter next()
            {
                return this.it.next();
            }
            
            @Override
            public void remove()
            {
                throw new UnsupportedOperationException();
            }
        };
    }
    
    /**
     * 联合filter作为一个过滤条件
     * @param filter
     * @return
     */
    public PropertyFilter joinFilter(PropertyFilter filter)
    {
        Assert.isTrue(filter == this, "PropertyFilter不允许添加自身");
        this.filters.add(filter);
        return this;
    }
    
    /**
     * 其他field，两个属性比较时
     * @return
     */
    public String getOtherField()
    {
        return this.otherField;
    }

    /**
     * 属性类型
     */
    public static enum FieldType
    {
        S(String.class), D(Date.class), I(Integer.class), DB(Double.class), L(Long.class), B(Boolean.class);
        
        private Class<?> clazz;
        
        private FieldType(Class<?> clazz)
        {
            this.clazz = clazz;
        }
        
        public Class<?> getValue()
        {
            return this.clazz;
        }
    }

    /**
     * 属性比较类型枚举
     */
    public static enum MatchType
    {
        /**
         * = 等于
         */
        EQ,
        
        /**
         * 等于另一个属性
         */
        EQF,
        
        /**
         * like '%value%'
         */
        LIKE,
        
        /**
         * like 'value%'
         */
        LIKESTART,
        
        /**
         * like '%value'
         */
        LIKEEND,
        
        /**
         * < 小于
         */
        LT,
        
        /**
         * 小于另一个属性
         */
        LTF,
        
        /**
         * > 大于
         */
        GT,
        
        /**
         * 大于另一个属性
         */
        GTF,
        
        /**
         * <= 小于等于
         */
        LE,
        
        /**
         * 小于等于另一个属性
         */
        LEF,
        
        /**
         * >= 大于等于
         */
        GE,
        
        /**
         * 大于等于另一个属性
         */
        GEF,
        
        /**
         * between
         */
        BETWEEN,
        
        /**
         * <> 不等于
         */
        NE,
        
        /**
         * 不等于另一个属性
         */
        NEF,
        
        /**
         * 为空
         */
        ISNULL,
        
        /**
         * 非空
         */
        NNULL,
        
        /**
         * IN
         */
        IN;
    }
}