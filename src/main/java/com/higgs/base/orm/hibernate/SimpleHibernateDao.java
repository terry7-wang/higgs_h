/*
 *File:SimpleHibernateDao.java
 *company:ECIQ
 *@version: 1.0
 *Date:2014-7-25
 */
package com.higgs.base.orm.hibernate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.LobHelper;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.type.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.higgs.base.orm.assistant.ReflectionUtils;
import com.higgs.base.utils.BeanFactoryHelper;
import com.higgs.base.utils.BeanHelper;
import com.higgs.base.utils.CollectionHelper;
import com.higgs.base.utils.StringHelper;
import com.higgs.base.utils.UUIDHelper;

/**
 * 封装Hibernate原生API的DAO泛型基类 可在Service层直接使用, 也可以扩展泛型DAO子类使用<br/>
 * 见两个构造函数的注释. 参考Spring2.5自带的Petlinc例子, 取消了HibernateTemplate,直接使用Hibernate原生API
 * 
 * @param <T> DAO操作的对象类型
 * @param <PK> 主键类型
 * 
 * @author terry
 * @since 1.0
 */
@Component
public class SimpleHibernateDao<T, PK extends Serializable>
{
    protected Logger logger = LoggerFactory.getLogger(getClass());
    
    protected SessionFactory sessionFactory;
    
    protected Class<T> entityClass;
    
    /**
     * 用于Dao层子类使用的构造函数. 通过子类的泛型定义取得对象类型Class. eg. public class UserDao extends SimpleHibernateDao<User, Long>
     */
    public SimpleHibernateDao()
    {
        this.entityClass = ReflectionUtils.getSuperClassGenricType(getClass());
    }
    
    /**
     * 用于用于省略Dao层, 在Service层直接使用通用SimpleHibernateDao的构造函数. 在构造函数中定义对象类型Class. eg. SimpleHibernateDao<User, Long> userDao
     * = new SimpleHibernateDao<User, Long>(sessionFactory, User.class);
     * 
     * @param sessionFactory
     * @param entityClass
     */
    public SimpleHibernateDao(SessionFactory sessionFactory, Class<T> entityClass)
    {
        this.sessionFactory = sessionFactory;
        this.entityClass = entityClass;
    }
    
    /**
     * 取得sessionFactory.
     * 
     * @return
     */
    public SessionFactory getSessionFactory()
    {
        return this.sessionFactory;
    }
    
    /**
     * 采用@Autowired按类型注入SessionFactory, 当有多个SesionFactory的时候在子类重载本函数.
     * 
     * @param sessionFactory
     */
    @Autowired
    public void setSessionFactory(SessionFactory sessionFactory)
    {
        this.sessionFactory = sessionFactory;
    }
    
    /**
     * 取得当前Session
     * 
     * @return
     */
    public Session getSession()
    {
        // 直接new DAO类的时候不会自动注入，所以手动获取sessionFactory
        if (null == this.sessionFactory)
        {
            BeanFactory beanFactory = BeanFactoryHelper.getBeanFactory();
            SessionFactory sessionFactory = (SessionFactory)beanFactory.getBean("sessionFactory");
            setSessionFactory(sessionFactory);
            return this.sessionFactory.getCurrentSession();
        }
        return this.sessionFactory.getCurrentSession();
    }
    
    /**
     * 新增的对象
     * 
     * @param entity
     */
    public void persist(T entity)
    {
        Assert.notNull(entity, "entity不能为空");
        getSession().persist(entity);
        this.logger.debug("save entity: {}", entity);
    }
    
    /**
     * 修改的对象
     * 
     * @param entity
     */
    public void merge(T entity)
    {
        Assert.notNull(entity, "entity不能为空");
        getSession().merge(entity);
        this.logger.debug("update entity: {}", entity);
    }
    
    /**
     * 保存
     * 
     * @param entity
     * @param isAdd
     */
    public void saveEntity(T entity,boolean isAdd)
    {
        if (isAdd)
        {
            String keyField = getIdName(entity.getClass());
            String primaryKey = BeanHelper.getProperty(entity, keyField);
            
            if (StringHelper.isEmpty(primaryKey))
            {
                primaryKey = UUIDHelper.generateUUID();
                BeanHelper.setProperty(entity, keyField, primaryKey);
            }
        }
        
        if (isAdd)
            persist(entity);
        else
            merge(entity);
    }
    
    /**
     * 删除对象
     * 
     * @param entity 对象必须是session中的对象或含id属性的transient对象
     */
    public void delete(T entity)
    {
        Assert.notNull(entity, "entity不能为空");
        getSession().delete(entity);
        this.logger.debug("delete entity: {}", entity);
    }
    
    /**
     * 按id删除对象
     * 
     * @param id
     */
    public void delete(PK id)
    {
        Assert.notNull(id, "id不能为空");
        delete(load(id)); // 删除前，先加载该对象
        this.logger.debug("delete entity {},id is {}", this.entityClass.getSimpleName(), id);
    }
    
    /**
     * 按id获取对象,用get
     * 
     * @param id
     * @return
     */
    public T get(PK id)
    {
        Assert.notNull(id, "id不能为空");
        return (T)getSession().get(this.entityClass, id);
    }
    
    /**
     * 按id获取对象,用load
     * 
     * @param id
     * @return
     */
    public T load(PK id)
    {
        Assert.notNull(id, "id不能为空");
        return (T)getSession().load(this.entityClass, id);
    }
    
    /**
     * 获取全部对象
     * 
     * @return
     */
    public List<T> getAll()
    {
        return find(new Criterion[0]);
    }
    
    /**
     * 按属性查找对象列表, 匹配方式为相等
     * 
     * @param propertyName
     * @param value
     * @return
     */
    public List<T> findBy(String propertyName,Object value)
    {
        Assert.hasText(propertyName, "propertyName不能为空");
        Criterion criterion = Restrictions.eq(propertyName, value);
        return find(new Criterion[] {criterion});
    }
    
    /**
     * 按属性查找唯一对象, 匹配方式为相等
     * 
     * @param propertyName
     * @param value
     * @return
     */
    public T findUniqueBy(String propertyName,Object value)
    {
        Assert.hasText(propertyName, "propertyName不能为空");
        Criterion criterion = Restrictions.eq(propertyName, value);
        return (T)createCriteria(new Criterion[] {criterion}).uniqueResult();
    }
    
    /**
     * 根据一组id获取一组对象
     * 
     * @param ids
     * @return
     */
    public List<T> findByIds(List<PK> ids)
    {
        return find(new Criterion[] {Restrictions.in(getIdName(), ids)});
    }
    
    /**
     * 按HQL查询对象列表
     * 
     * @param hql
     * @param values 参数按顺序绑定
     * @return
     */
    public <X> List<X> find(String hql,Object[] values)
    {
        return createQuery(hql, values).list();
    }
    
    /**
     * 按HQL查询对象列表
     * 
     * @param hql
     * @param values 命名参数,按名称绑定
     * @return
     */
    public <X> List<X> find(String hql,Map<String, Object> values)
    {
        return createQuery(hql, values).list();
    }
    
    /**
     * 按HQL查询唯一对象
     * 
     * @param hql
     * @param values 按顺序绑定
     * @return
     */
    public <X> X findUnique(String hql,Object[] values)
    {
        return (X)createQuery(hql, values).uniqueResult();
    }
    
    /**
     * 按HQL查询唯一对象
     * 
     * @param hql
     * @param values 命名参数,按名称绑定
     * @return
     */
    public <X> X findUnique(String hql,Map<String, Object> values)
    {
        return (X)createQuery(hql, values).uniqueResult();
    }
    
    /**
     * 按SQL查询唯一对象
     * 
     * @param sql
     * @param values 命名参数,按名称绑定
     * @return
     */
    public <X> X findUnique4Sql(String sql,Map<String, Object> values)
    {
        return (X)createSqlQuery(sql, values).uniqueResult();
    }
    
    /**
     * 执行HQL进行批量修改/删除操作
     * 
     * @param hql
     * @param values 按顺序绑定
     * @return
     */
    public int batchExecute(String hql,Object[] values)
    {
        return createQuery(hql, values).executeUpdate();
    }
    
    /**
     * 执行HQL进行批量修改/删除操作
     * 
     * @param hql
     * @param values 命名参数,按名称绑定
     * @return
     */
    public int batchExecute(String hql,Map<String, Object> values)
    {
        return createQuery(hql, values).executeUpdate();
    }
    
    /**
     * 根据查询HQL与参数列表创建Query对象. 与find()函数可进行更加灵活的操作
     * 
     * @param queryString
     * @param values 按顺序绑定
     * @return
     */
    public Query createQuery(String queryString,Object[] values)
    {
        Assert.hasText(queryString, "queryString不能为空");
        Query query = getSession().createQuery(queryString);
        if (values != null)
        {
            for (int i = 0; i < values.length; i++)
            {
                if (null != values[i])
                {
                    // 这里考虑传入的参数是什么类型，不同类型使用的方法不同
                    if (values[i] instanceof Collection<?>)
                    {
                        query.setParameterList(String.valueOf(i), (Collection<?>)values[i]);
                    }
                    else if (values[i] instanceof Object[])
                    {
                        query.setParameterList(String.valueOf(i), (Object[])values[i]);
                    }
                    else
                    {
                        query.setParameter(i, values[i]);
                    }
                }
            }
        }
        return query;
    }
    
    /**
     * 根据查询HQL与参数列表创建Query对象. 与find()函数可进行更加灵活的操作
     * 
     * @param queryString
     * @param values 命名参数,按名称绑定
     * @return
     */
    public Query createQuery(String queryString,Map<String, Object> values)
    {
        Assert.hasText(queryString, "queryString不能为空");
        Query query = getSession().createQuery(queryString);
        if (values != null)
        {
            Set<String> keySet = values.keySet();
            for (String key : keySet)
            {
                Object obj = values.get(key);
                if (null != obj)
                {
                    // 这里考虑传入的参数是什么类型，不同类型使用的方法不同
                    if (obj instanceof Collection<?>)
                    {
                        query.setParameterList(key, (Collection<?>)obj);
                    }
                    else if (obj instanceof Object[])
                    {
                        query.setParameterList(key, (Object[])obj);
                    }
                    else
                    {
                        query.setParameter(key, obj);
                    }
                }
            }
        }
        return query;
    }
    
    /**
     * 根据查询原生SQL与参数列表创建Query对象. 与find()函数可进行更加灵活的操作
     * 
     * @param queryString
     * @param values 按顺序绑定
     * @return
     */
    public SQLQuery createSqlQuery(String queryString,Object[] values)
    {
        Assert.hasText(queryString, "queryString不能为空");
        SQLQuery query = getSession().createSQLQuery(queryString);
        if (values != null)
        {
            for (int i = 0; i < values.length; i++)
            {
                if (null != values[i])
                {
                    // 这里考虑传入的参数是什么类型，不同类型使用的方法不同
                    if (values[i] instanceof Collection<?>)
                    {
                        query.setParameterList(String.valueOf(i), (Collection<?>)values[i]);
                    }
                    else if (values[i] instanceof Object[])
                    {
                        query.setParameterList(String.valueOf(i), (Object[])values[i]);
                    }
                    else
                    {
                        query.setParameter(i, values[i]);
                    }
                }
            }
        }
        return query;
    }
    
    /**
     * 根据查询原生SQL与参数列表创建Query对象. 与find()函数可进行更加灵活的操作
     * 
     * @param queryString
     * @param values 命名参数,按名称绑定
     * @return
     */
    public SQLQuery createSqlQuery(String queryString,Map<String, Object> values,Map<String, Class<?>> fieldsClsMap)
    {
        Assert.hasText(queryString, "queryString不能为空");
        SQLQuery query = getSession().createSQLQuery(queryString);
        
        if (CollectionHelper.isEmpty(values))
            return query;
        
        for (Map.Entry<String, Object> conditionEntry : values.entrySet())
        {
            String condName = conditionEntry.getKey();
            Object condValue = conditionEntry.getValue();
            
            /* 1. 如果condValue 为空，查找是否有对应的字段类型，如果有对应的类型定义，进行Null的特殊处理 */
            if (condValue == null && CollectionHelper.isNotEmpty(fieldsClsMap))
            {
                Class<?> fieldCls = fieldsClsMap.get(condName);
                Type hibernateType = convertJdbcType2HibernateType(fieldCls);
                
                if(hibernateType != null)
                    query.setParameter(condName, null, hibernateType);
            }
            
            /* 2.如果condValue 不为空，如果是集合类型的字段，需要进行特殊处理 */
            else if (condValue != null)
            {
                // 这里考虑传入的参数是什么类型，不同类型使用的方法不同
                if (condValue instanceof Collection<?>)
                    query.setParameterList(condName, (Collection<?>)condValue);
                else if (condValue instanceof Object[])
                    query.setParameterList(condName, (Object[])condValue);
                else
                    query.setParameter(condName, condValue);
            }
        }
        
        return query;
    }
    
    /**
     * 根据查询原生SQL与参数列表创建Query对象. 与find()函数可进行更加灵活的操作
     * 
     * @param queryString
     * @param values 命名参数,按名称绑定
     * @return
     */
    public SQLQuery createSqlQuery(String queryString,Map<String, Object> values)
    {
        return createSqlQuery(queryString,values,null);
    }
    
    /**
     * 按Criteria查询对象列表
     * 
     * @param criterions
     * @return
     */
    public List<T> find(Criterion[] criterions)
    {
        return createCriteria(criterions).list();
    }
    
    /**
     * 按Criteria查询对象列表
     * 
     * @param aliases
     * @param criterions
     * @return
     */
    public List<T> find(Map<String, String> aliases,Criterion[] criterions)
    {
        return createCriteria(aliases, criterions).list();
    }
    
    /**
     * 按Criteria查询唯一对象
     * 
     * @param criterions
     * @return
     */
    public T findUnique(Criterion[] criterions)
    {
        return (T)createCriteria(criterions).uniqueResult();
    }
    
    /**
     * 按Criteria查询唯一对象
     * 
     * @param aliases
     * @param criterions
     * @return
     */
    public T findUnique(Map<String, String> aliases,Criterion[] criterions)
    {
        return (T)createCriteria(aliases, criterions).uniqueResult();
    }
    
    /**
     * 根据Criterion条件创建Criteria. 与find()函数可进行更加灵活的操作
     * 
     * @param criterions
     * @return
     */
    public Criteria createCriteria(Criterion[] criterions)
    {
        Criteria criteria = getSession().createCriteria(this.entityClass);
        
        for (Criterion c : criterions)
        {
            criteria.add(c);
        }
        return criteria;
    }
    
    /**
     * 根据Criterion条件创建Criteria. 与find()函数可进行更加灵活的操作
     * 
     * @param aliases
     * @param criterions
     * @return
     */
    public Criteria createCriteria(Map<String, String> aliases,Criterion[] criterions)
    {
        Criteria criteria = getSession().createCriteria(this.entityClass);
        
        if (aliases != null)
        {
            for (String key : aliases.keySet())
            {
                criteria.createAlias(key, aliases.get(key));
            }
        }
        
        for (Criterion c : criterions)
        {
            criteria.add(c);
        }
        return criteria;
    }
    
    /**
     * 支持任意对象的条件查询
     * 
     * @param cls
     * @param aliases
     * @param criterions
     * @return
     */
    public Criteria createCriteria(Class<?> cls,Map<String, String> aliases,Criterion[] criterions)
    {
        Criteria criteria = getSession().createCriteria(cls);
        
        if (aliases != null)
        {
            for (String key : aliases.keySet())
            {
                criteria.createAlias(key, aliases.get(key));
            }
        }
        
        for (Criterion c : criterions)
        {
            criteria.add(c);
        }
        return criteria;
    }
    
    /**
     * 初始化entity的直接属性和关联集合
     * 
     * @param entity
     */
    public void initEntity(T entity)
    {
        Hibernate.initialize(entity);
    }
    
    /**
     * 初始化一组entity的直接属性和关联集合
     * 
     * @param entityList
     */
    public void initEntity(List<T> entityList)
    {
        for (Object entity : entityList)
        {
            Hibernate.initialize(entity);
        }
    }
    
    /**
     * 为Query添加distinct transformer. 预加载关联对象的HQL会引起主对象重复, 需要进行distinct处理
     * 
     * @param query
     * @return
     */
    public Query distinct(Query query)
    {
        query.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
        return query;
    }
    
    /**
     * 为Criteria添加distinct transformer. 预加载关联对象的HQL会引起主对象重复, 需要进行distinct处理
     * 
     * @param criteria
     * @return
     */
    public Criteria distinct(Criteria criteria)
    {
        criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
        return criteria;
    }
    
    public <X> List<X> distinct(List<X> list)
    {
        Set set = new LinkedHashSet(list);
        return new ArrayList(set);
    }
    
    /**
     * 取得对象的主键名
     * 
     * @return
     */
    public String getIdName(Class<?> cls)
    {
        ClassMetadata meta = getSessionFactory().getClassMetadata(cls);
        return meta.getIdentifierPropertyName();
    }
    
    /**
     * 取得对象的主键名
     * 
     * @return
     */
    public String getIdName()
    {
        return getIdName(this.entityClass);
    }
    
    /**
     * 获取LobHelper
     * 
     * @return
     */
    public LobHelper getLobHelper()
    {
        return getSession().getLobHelper();
    }
    
    /**
     * 保存新增或修改的对象
     * 
     * @param entity
     * @param ifAdd 是否新增
     */
    public void save(Object entity,boolean isAdd)
    {
        Assert.notNull(entity, "entity不能为空");
        if (isAdd)
        {
            getSession().persist(entity);
        }
        else
        {
            getSession().merge(entity);
        }
        
        this.logger.debug("save entity: {}", entity);
    }
    
    protected Type convertJdbcType2HibernateType(Class<?> jdbcType)
    {
        if (jdbcType == null)
            return null;
        
        Type hibernateType;
        if (Integer.class == jdbcType)
            hibernateType = StandardBasicTypes.INTEGER;
        else if (Long.class == jdbcType)
            hibernateType = StandardBasicTypes.LONG;
        else if (Double.class == jdbcType)
            hibernateType = StandardBasicTypes.DOUBLE;
        else if (Date.class == jdbcType)
            hibernateType = StandardBasicTypes.DATE;
        else
            hibernateType = StandardBasicTypes.STRING;
        
        return hibernateType;
    }
    
}
