/*
 *File:BaseService.java
 *company:ECIQ
 *@version: 1.0
 *Date:2014-11-11
 */
package com.higgs.base.orm.hibernate;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.criterion.Criterion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.higgs.base.orm.assistant.PropertyFilter;
import com.higgs.base.utils.BeanHelper;
import com.higgs.base.utils.StringHelper;
import com.higgs.base.utils.UUIDHelper;

/**
 * 基础service类，提供基本的增删改查
 * 
 * @author terry
 * @since 1.0
 */
@Service
@Transactional
public class BaseService
{
    /**
     * 创建object类型的DAO，用来做保存、修改、删除对象等
     */
//    @Resource(name ="baseDao")
    @Autowired
    protected BaseDao baseDao;
    
    /**
     * 根据ID获取对象
     * 
     * @param clazz
     * @param id
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T> T getById(Class<T> clazz,String id)
    {
        return (T)baseDao.getDao(clazz).get(id);
    }
    
    /**
     * 查询该对象的列表
     * 
     * @param clazz
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> queryAll(Class<T> clazz)
    {
        return (List<T>)baseDao.getDao(clazz).getAll();
    }
    
    /**
     * 按属性查找唯一对象, 匹配方式为相等
     * 
     * @param clazz
     * @param propertyName
     * @param value
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T> T findUniqueBy(Class<T> clazz,String propertyName,Object value)
    {
        return (T)baseDao.getDao(clazz).findUniqueBy(propertyName, value);
    }
    
    /**
     * 按Criteria中设置的值查询唯一对象
     * 
     * @param clazz
     * @param criterions
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T> T findUnique(Class<T> clazz,Criterion[] criterions)
    {
        return (T)baseDao.getDao(clazz).findUnique(criterions);
    }
    
    /**
     * 按属性判断对象是否是唯一对象, 匹配方式为相等
     * @param clazz
     * @param propertyName
     * @param value
     * @return
     */
    public <T> boolean ifUnique(Class<T> clazz,String propertyName,Object value)
    {
        try
        {
            if(null != (T)baseDao.getDao(clazz).findUniqueBy(propertyName, value))
                return true;
        }
        catch (Exception e)
        {
            return true;
        }
        
        return false;
    }
    
    /**
     * 按Criteria中设置的值查询是否是唯一对象
     * 
     * @param clazz
     * @param criterions
     * @return
     */
    public <T> boolean ifUnique(Class<T> clazz,Criterion[] criterions)
    {
        try
        {
            if(null != (T)baseDao.getDao(clazz).findUnique(criterions))
                return true;
        }
        catch (Exception e)
        {
            return true;
        }
        
        return false;
    }
    
    /**
     * 按属性查找对象列表, 匹配方式为相等
     * 
     * @param clazz
     * @param propertyName
     * @param value
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> queryBy(Class<T> clazz,String propertyName,Object value)
    {
        return (List<T>)baseDao.getDao(clazz).findBy(propertyName, value);
    }

    /**
     * 按criterions查找对象列表
     * @param clazz
     * @param criterions
     * @param <T>
     * @return
     */
    public <T>List<T> queryBy(Class<T> clazz,Criterion[] criterions)
    {
        return (List<T>)baseDao.getDao(clazz).find(criterions);
    }
    
    /**
     * 保存
     * 
     * @param entity
     */
    public void save(Object entity)
    {
        baseDao.saveEntity(entity, true);
    }
    
    /**
     * 修改
     * 
     * @param entity
     */
    public void update(Object entity)
    {
        baseDao.saveEntity(entity, false);
    }
    
    /**
     * 根据ID删除对象
     * 
     * @param clazz
     * @param id
     */
    public <T> void deleteById(Class<T> clazz,String id)
    {
        baseDao.getDao(clazz).delete(id);
    }
    
    /**
     * 删除对象
     * 
     * @param entity
     */
    public void delete(Object entity)
    {
        baseDao.delete(entity);
    }
    
    public void saveEntity(Object entity,boolean isAdd)
    {
        if (isAdd)
        {
            String keyField = baseDao.getIdName(entity.getClass());
            String primaryKey = BeanHelper.getProperty(entity, keyField);
            
            if (StringHelper.isEmpty(primaryKey))
            {
                primaryKey = UUIDHelper.generateUUID();
                BeanHelper.setProperty(entity, keyField, primaryKey);
            }
        }
        
        baseDao.save(entity, isAdd);
    }
    
    /**
     * 高级查询
     * 
     * @param cls 查询类
     * @param filters 查询过滤条件
     * @param orders 结果集排序
     * @return
     */
    public <X> List<X> advancedQuery(Class<X> cls,List<PropertyFilter> filters,LinkedHashMap<String, String> orders)
    {
        if (cls == null)
            return new ArrayList<X>();
        
        return baseDao.advancedQuery(cls, filters, orders);
    }
    
    /**
     * 快速自定义高级查询，该查询只支持条件匹配为相等的情况，效率比advancedQuery略高
     * 
     * @param cls 对应查询类
     * @param conditions 查询条件，不需要时可为空
     * @param orders 排序，不需要时可为空
     * @return
     */
    public <X> List<X> advancedQuery4Quickly(Class<X> cls,Map<String, Object> conditions,LinkedHashMap<String, String> orders)
    {
        if (cls == null)
            return new ArrayList<X>();
        
        return baseDao.advancedQuery4Quickly(cls, conditions, orders);
    }
    
    public BaseDao getBaseDao()
    {
        return baseDao;
    }
    
}
