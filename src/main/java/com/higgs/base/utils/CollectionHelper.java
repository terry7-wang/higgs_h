package com.higgs.base.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections.CollectionUtils;

/**
 * 继承CollectionUtils的集合助手类
 * 
 * @author terry
 * @since 1.0
 */
public class CollectionHelper extends CollectionUtils
{
    
    /**
     * 获取集合中的首个元素
     * 
     * @param list 目标集合
     * @return 首元素，如果首元素不存在,或者集合无内容，则返回null
     */
    public static <E> E getFirstElement(List<E> list)
    {
        if (CollectionUtils.isEmpty(list))
        {
            return null;
        }
        return list.get(0);
    }
    
    /**
     * 获取集合中的指定位置的元素
     * 
     * @param list 目标集合
     * @param index 元素索引位置
     * @return 指定位置的元素，如果集合为空或者无内容，则返回null
     */
    public static <E> E getIndexElement(List<E> list,int index)
    {
        if (CollectionUtils.isEmpty(list))
        {
            return null;
        }
        return list.get(index);
    }
    
    /**
     * 判断Map集合是否为空
     * 
     * @param map 目标map
     * @return 是否为空
     */
    public static boolean isEmpty(Map<?, ?> map)
    {
        return (map == null) || (map.isEmpty());
    }
    
    /**
     * 判断Map集合是否非空
     * 
     * @param map
     * @return 是否不为空
     */
    public static boolean isNotEmpty(Map<?, ?> map)
    {
        return !isEmpty(map);
    }
    
    /**
     * 从Map集合中，获取指定Key值的元素
     * 
     * @param map
     * @param key
     * @return 指定Key值的元素，如果集合不存在或者为空，返回null
     */
    public static <K, V> V getElement(Map<K, V> map,K key)
    {
        if (isEmpty(map))
        {
            return null;
        }
        return map.get(key);
    }
    
    public static <T> boolean isEmpty(T[] arr)
    {
        if (arr == null || arr.length == 0 )
            return true;
        else
            return false;
    }
    
    public static <T> boolean isNotEmpty(T[] arr){
        return !isEmpty(arr);
    }
    
    public  static <T> T getFirstElement(T[] arr){
        if(isEmpty(arr))
            return null;
        
        return arr[0];        
    }
    
    
    /**
     * 将2个集合中的元素进行去重复合并，合并后的顺序：targetList的新元素将追加在srcList后面
     * 
     * @param srcList 基准集合
     * @param targetList 要被并入的集合
     * @return 合并后的srcList
     */
    public static <E> List<E> addListByDistinct(List<E> srcList,List<E> targetList)
    {
        targetList.removeAll(srcList);
        srcList.addAll(targetList);
        return srcList;
    }
    
    public static <E> String asString(List<E> list,String separtor)
    {
        
        StringBuilder sb = new StringBuilder();
        for (E e : list)
        {
            sb.append(e.toString()).append(separtor);
        }
        
        return sb.substring(0, sb.length() - 1);
    }
    
    public static <E> List<E> asList(Map<?, E> map)
    {
        List<E> list = new ArrayList<E>();
        for (Iterator iterator = map.entrySet().iterator(); iterator.hasNext();)
        {
            Entry<?, E> entry = (Entry)iterator.next();
            list.add(entry.getValue());
        }
        return list;
    }
    
    /**
     * 集合转换成map对象，map使用LinkedHashMap，保留顺序
     * @param list 目标集合
     * @param indexProperty 作为key的索引属性
     * @return
     */
    public static <E> LinkedHashMap<String, E> asMap(List<E> list,String indexProperty)
    {
        LinkedHashMap<String, E> map = new LinkedHashMap<String, E>();
        
        for (E e : list)
        {
            String indexKey = BeanHelper.getProperty(e, indexProperty);
            map.put(indexKey, e);
        }
        
        return map;
    }
    
    public static void main(String[] args)
    {
        List<String> list1 = new ArrayList();
        Collections.addAll(list1, "A", "B", "C");
        
        List<String> list2 = new ArrayList();
        Collections.addAll(list2, "A", "C", "D");
        
        list1 = (List<String>)CollectionHelper.intersection(list1, list2);
        
        for (String str : list1)
        {
            System.out.println(str);
        }
        
        
        
    }
    
}
