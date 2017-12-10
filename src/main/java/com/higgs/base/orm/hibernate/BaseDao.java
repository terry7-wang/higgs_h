/*
 *File:BaseDao.java
 *company:ECIQ
 *@version: 1.0
 *Date:2014-12-17
 */
package com.higgs.base.orm.hibernate;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * BaseServer用的DAO
 * @author terry
 * @since 1.0
 */
@Repository
public class BaseDao extends HibernateDao<Object, Serializable>
{
    /**
     * 保存已经创建的DAO对象，避免一直重复创建相同的
     */
    private final static Map<String, HibernateDao<?, Serializable>> daoMap = new HashMap<String, HibernateDao<?, Serializable>>();
    
    /**
     * 自动注入sessionFactory
     */
    @Autowired
    SessionFactory sessionFactory;
    
    /**
     * 创建需传入类型的DAO
     */
    private volatile static HibernateDao<?, Serializable> dao;
    
    /**
     * 根据传入的对象获取相应的DAO
     * 
     * @param clazz
     * @return
     */
    public <T> HibernateDao<?, Serializable> getDao(Class<T> clazz)
    {
        // 根据传入的类型名称寻找各自的DAO
        dao = daoMap.get(clazz.getName());
        // 如果对象为空则创建一个,使用双重锁
        if (null == dao)
        {
            synchronized (HibernateDao.class)
            {
                if (null == dao)
                {
                    // 根据传入的类型创建对应的DAO
                    dao = new HibernateDao<T, Serializable>(sessionFactory, clazz);
                    // 如果map数量大于50，清空下map，重新添加
                    if (daoMap.size() > 50)
                    {
                        daoMap.clear();
                    }
                    // 将创建好的DAO存入map，方便下次取用
                    daoMap.put(clazz.getName(), dao);
                    return dao;
                }
                else
                {
                    // 根据传入的类型名称寻找各自的DAO
                    dao = daoMap.get(clazz.getName());
                }
            }
        }
        return dao;
    }
}
