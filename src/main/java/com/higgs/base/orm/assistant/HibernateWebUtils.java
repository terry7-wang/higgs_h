/*
 *File:HibernateWebUtils.java
 *company:ECIQ
 *@version: 1.0
 *Date:2014-7-25
 */
package com.higgs.base.orm.assistant;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.Assert;
import org.springframework.web.util.WebUtils;


/**
 * 
 * @author terry
 * @since 1.0
 */
public class HibernateWebUtils
{
    public static <T, ID> void mergeByCheckedIds(Collection<T> srcObjects, Collection<ID> checkedIds, Class<T> clazz)
    {
        mergeByCheckedIds(srcObjects, checkedIds, clazz, "id");
    }
    
    public static <T, ID> void mergeByCheckedIds(Collection<T> srcObjects, Collection<ID> checkedIds, Class<T> clazz,
        String idName)
    {
        Assert.notNull(srcObjects, "scrObjects不能为空");
        Assert.hasText(idName, "idName不能为空");
        Assert.notNull(clazz, "clazz不能为空");
        
        if (checkedIds == null)
        {
            srcObjects.clear();
            return;
        }
        
        Iterator srcIterator = srcObjects.iterator();
        try
        {
            Object id;
            while (srcIterator.hasNext())
            {
                Object element = srcIterator.next();
                
                id = PropertyUtils.getProperty(element, idName);
                
                if (!checkedIds.contains(id))
                {
                    srcIterator.remove();
                }
                else
                {
                    checkedIds.remove(id);
                }
                
            }
            
            for (Object id1 : checkedIds)
            {
                Object obj = clazz.newInstance();
                PropertyUtils.setProperty(obj, idName, id1);
                srcObjects.add((T)obj);
            }
        }
        catch (Exception e)
        {
            throw ReflectionUtils.convertToUncheckedException(e);
        }
    }
    
    /**
     * 从HttpRequest中创建PropertyFilter列表
     * PropertyFilter命名规则为Filter属性前缀_比较类型属性类型_属性名
     * eg.
     * filter_EQS_name
     * filter_LIKES_name_OR_email
     * 
     * @param request
     * @return
     */
    public static List<PropertyFilter> buildPropertyFilters(HttpServletRequest request)
    {
        return buildPropertyFilters(request, "filter_");
    }
    
    public static SearchFilterProperty buildSearchPropertyFilters(HttpServletRequest request)
    {
        return buildSearchPropertyFilters(request, "searchFilter_", "searchFilterFactory");
    }
    
    public static SearchFilterProperty buildSearchPropertyFilters(HttpServletRequest request, String filterPrefix,
        String filterName)
    {
        Map searchParams = new HashMap();
        Map<String, Object> params = WebUtils.getParametersStartingWith(request, filterPrefix);
        
        for (Map.Entry<String, Object> entry : params.entrySet())
        {
            String key = entry.getKey();
            String value = (String)entry.getValue();
            
            boolean omit = StringUtils.isBlank(value);
            if (omit)
                continue;
            searchParams.put(key, value);
        }
        
        SearchFilterProperty searchFilterProperty = new SearchFilterProperty();
        searchFilterProperty.setFilterName(filterName);
        searchFilterProperty.setParams(searchParams);
        return searchFilterProperty;
    }
    
    public static List<PropertyFilter> buildPropertyFilters(HttpServletRequest request, String filterPrefix)
    {
        List filterList = new ArrayList();
        
        Map<String, Object> filterParamMap = WebUtils.getParametersStartingWith(request, filterPrefix);
        
        for (Map.Entry<String, Object> entry : filterParamMap.entrySet())
        {
            String filterName = entry.getKey();
            String value = (String)entry.getValue();
            
            boolean omit = StringUtils.isBlank(value);
            if (omit)
                continue;
            PropertyFilter filter = new PropertyFilter(filterName, value);
            filterList.add(filter);
        }
        
        return filterList;
    }
    
    public static SearchProperty buildSearchProperty(HttpServletRequest request)
    {
        return buildSearchProperty(request, "search_");
    }
    
    public static SearchProperty buildSearchProperty(HttpServletRequest request, String filterPrefix)
    {
        List propertyNames = new ArrayList();
        StringBuffer propertyValue = new StringBuffer();
        
        Map<String, Object> filterParamMap = WebUtils.getParametersStartingWith(request, filterPrefix);
        
        for (Map.Entry<String, Object> entry : filterParamMap.entrySet())
        {
            String[] filterNames = entry.getKey().split(",");
            propertyNames.addAll(Arrays.asList(filterNames));
            propertyValue.append((String)entry.getValue());
        }
        
        SearchProperty searchProperty = new SearchProperty(propertyNames, propertyValue.toString());
        
        return searchProperty;
    }
}