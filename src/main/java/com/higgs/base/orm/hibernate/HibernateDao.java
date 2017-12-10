/*
 *File:HibernateDao.java
 *company:ECIQ
 *@version: 1.0
 *Date:2014-7-25
 */
package com.higgs.base.orm.hibernate;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projection;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.internal.CriteriaImpl;
import org.hibernate.transform.ResultTransformer;
import org.hibernate.transform.Transformers;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.type.Type;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import com.higgs.base.orm.assistant.Page;
import com.higgs.base.orm.assistant.PropertyFilter;
import com.higgs.base.orm.assistant.PropertyFilter.MatchType;
import com.higgs.base.orm.assistant.ReflectionUtils;
import com.higgs.base.orm.assistant.SimpleLogicalExpression;
import com.higgs.base.utils.CollectionHelper;
import com.higgs.base.utils.DbHelper;
import com.higgs.base.utils.StringHelper;

/**
 * Hibernat DAO泛型基类 扩展功能包括分页查询,按属性过滤条件列表查询. 可在Service层直接使用,也可以扩展泛型DAO子类使用,见两个构造函数的注释
 * 
 * @param <T> DAO操作的对象类型
 * @param <PK> 主键类型
 * @author terry
 * @since 1.0
 */
@Repository
public class HibernateDao<T, PK extends Serializable> extends SimpleHibernateDao<T, PK>
{
    /**
     * 用于Dao层子类使用的构造函数. 通过子类的泛型定义取得对象类型Class. eg. public class UserDao extends HibernateDao<User, Long>{ }
     */
    public HibernateDao()
    {
    }
    
    /**
     * 用于省略Dao层, Service层直接使用通用HibernateDao的构造函数. 在构造函数中定义对象类型Class. eg. HibernateDao<User, Long> userDao = new
     * HibernateDao<User, Long>(sessionFactory, User.class);
     * 
     * @param sessionFactory
     * @param entityClass
     */
    public HibernateDao(SessionFactory sessionFactory, Class<T> entityClass)
    {
        super(sessionFactory, entityClass);
    }
    
    // -- 分页查询函数 --//
    /**
     * 分页获取全部对象
     * 
     * @param page
     * @return
     */
    public Page<T> getAll(Page<T> page)
    {
        return findPage(page, new Criterion[0]);
    }
    
    /**
     * 按HQL分页查询
     * 
     * @param page 分页参数.不支持其中的orderBy参数
     * @param hql hql语句
     * @param values 数量可变的查询参数,按顺序绑定
     * @return 分页查询结果, 附带结果列表及所有查询时的参数
     */
    public Page<T> findPage(Page<T> page,String hql,Object[] values)
    {
        Assert.notNull(page, "page不能为空");
        
        Query q = createQuery(hql, values);
        
        if (page.isAutoCount())
        {
            long totalCount = countHqlResult(hql, values);
            page.setTotalCount(totalCount);
        }
        
        setPageParameter(q, page);
        List<T> result = q.list();
        page.setResult(result);
        return page;
    }
    
    /**
     * 按HQL分页查询
     * 
     * @param page 分页参数
     * @param hql hql语句
     * @param values 命名参数,按名称绑定
     * @return 分页查询结果, 附带结果列表及所有查询时的参数
     */
    public Page<T> findPage(Page<T> page,String hql,Map<String, Object> values)
    {
        Assert.notNull(page, "page不能为空");
        
        Query q = createQuery(hql, values);
        
        if (page.isAutoCount())
        {
            long totalCount = countHqlResult(hql, values);
            page.setTotalCount(totalCount);
        }
        
        setPageParameter(q, page);
        
        List<T> result = q.list();
        page.setResult(result);
        return page;
    }
    
    /**
     * 任意对象的hql分页查询，该方法可以支持返回自定义的数组对象的分页查询<br/>
     * 例如： select a,b from a,b where ... 的使用
     * 
     * @param page 分页参数
     * @param hql hql语句
     * @param values 参数对象
     * @return
     */
    public Page<Object> findPage4Object(Page<Object> page,String hql,Map<String, Object> values)
    {
        Assert.notNull(page, "page不能为空");
        
        Query q = createQuery(hql, values);
        
        if (page.isAutoCount())
        {
            long totalCount = countHqlResult(hql, values);
            page.setTotalCount(totalCount);
        }
        
        setPageParameter(q, page);
        
        List<Object> result = q.list();
        page.setResult(result);
        return page;
    }
    
    /**
     * 任意对象的hql分页查询，该方法可以支持返回自定义的数组对象的分页查询<br/>
     * 例如： select a,b from a,b where ... 的使用
     * 
     * @param page 分页参数
     * @param hql hql语句
     * @param values 参数对象
     * @return
     */
    public Page<Map<String, Object>> findPage4Sql(Page<Map<String, Object>> page,String sql,Map<String, Object> values,
        Map<String, Class<?>> colTypes)
    {
        Assert.notNull(page, "page不能为空");
        if (page.isAutoCount())
        {
            long totalCount = countSqlResult(sql, values);
            page.setTotalCount(totalCount);
        }
        
        List<Map<String, Object>> result = queryBySql(sql, values, colTypes, page);
        page.setResult(result);
        return page;
    }
    
    /**
     * 使用Sql语句进行查询
     * 
     * @param sql 原声SQL
     * @param conditions 查询条件
     * @param colTypes 特殊需要进行转换的列集合
     * @param page 分页条件，可为空
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>>
        queryBySql(String sql,Map<String, Object> conditions,Map<String, Class<?>> colTypes,Page<?> page)
    {
        SQLQuery query = createSqlQuery(sql, conditions);
        query.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
        
        if (page != null)
        {
            setPageParameter(query, page);
            
            if (page.isAutoCount())
            {
                long totalCount = countSqlResult(sql, conditions);
                page.setTotalCount(totalCount);
            }
        }
        
        if (CollectionHelper.isNotEmpty(colTypes))
        {
            Type hibernateType;
            for (Map.Entry<String, Class<?>> colTypeEntry : colTypes.entrySet())
            {
                Class<?> valueCls = colTypeEntry.getValue();
                
                if (Long.class == valueCls)
                    hibernateType = StandardBasicTypes.LONG;
                else if (Double.class == valueCls)
                    hibernateType = StandardBasicTypes.DOUBLE;
                else if (Date.class == valueCls)
                    hibernateType = StandardBasicTypes.DATE;
                else
                    hibernateType = StandardBasicTypes.STRING;
                
                query.addScalar(colTypeEntry.getKey(), hibernateType);
            }
        }
        
        return query.list();
    }
    
    /**
     * 按Criteria分页查询
     * 
     * @param page 分页参数
     * @param criterions
     * @return 分页查询结果.附带结果列表及所有查询时的参数
     */
    public Page<T> findPage(Page<T> page,Criterion[] criterions)
    {
        Assert.notNull(page, "page不能为空");
        
        Criteria c = createCriteria(criterions);
        
        if (page.isAutoCount())
        {
            long totalCount = countCriteriaResult(c);
            page.setTotalCount(totalCount);
        }
        
        setPageParameter(c, page);
        List result = c.list();
        page.setResult(result);
        return page;
    }
    
    /**
     * 按Criteria分页查询
     * 
     * @param page
     * @param aliases
     * @param criterions
     * @return
     */
    public Page<T> findPage(Page<T> page,Map<String, String> aliases,Criterion[] criterions)
    {
        Assert.notNull(page, "page不能为空");
        
        Criteria c = createCriteria(aliases, criterions);
        
        if (page.isAutoCount())
        {
            long totalCount = countCriteriaResult(c);
            page.setTotalCount(totalCount);
        }
        
        setPageParameter(c, page);
        List result = c.list();
        page.setResult(result);
        return page;
    }
    
    public <E> Page<E> findPage(Class<E> cls,Page<E> page,Map<String, String> aliases,Criterion[] criterions)
    {
        Assert.notNull(page, "page不能为空");
        
        Criteria c = createCriteria(cls, aliases, criterions);
        
        if (page.isAutoCount())
        {
            long totalCount = countCriteriaResult(c);
            page.setTotalCount(totalCount);
        }
        
        setPageParameter(c, page);
        List result = c.list();
        page.setResult(result);
        return page;
    }
    
    /**
     * 设置分页参数到Query对象,辅助函数
     * 
     * @param q
     * @param page
     * @return
     */
    protected Query setPageParameter(Query q,Page<?> page)
    {
        q.setFirstResult(page.getFirst() - 1);
        q.setMaxResults(page.getPageSize());
        return q;
    }
    
    /**
     * 设置分页参数到Criteria对象,辅助函数
     * 
     * @param c
     * @param page
     * @return
     */
    protected Criteria setPageParameter(Criteria c,Page<?> page)
    {
        // hibernate的firstResult的序号从0开始
        c.setFirstResult(page.getFirst() - 1);
        c.setMaxResults(page.getPageSize());
        
        if (page.isOrderBySetted())
        {
            String[] orderByArray = StringUtils.split(page.getOrderBy(), ',');
            String[] orderArray = StringUtils.split(page.getOrder(), ',');
            
            Assert.isTrue(orderByArray.length == orderArray.length, "分页多重排序参数中,排序字段与排序方向的个数不相等");
            
            for (int i = 0; i < orderByArray.length; i++)
            {
                if ("asc".equals(orderArray[i]))
                {
                    c.addOrder(Order.asc(orderByArray[i]));
                }
                else
                {
                    c.addOrder(Order.desc(orderByArray[i]));
                }
            }
        }
        return c;
    }
    
    /**
     * 执行count查询获得本次Hql查询所能获得的对象总数 本函数只能自动处理简单的hql语句,复杂的hql查询请另行编写count语句查询
     * 
     * @param hql
     * @param values
     * @return
     */
    protected long countHqlResult(String hql,Object[] values)
    {
        Long count = Long.valueOf(0L);
        String fromHql = hql;
        // select子句与order by子句会影响count查询,进行简单的排除
        if (fromHql.indexOf("from") > 0)
        {
            fromHql = "from " + StringUtils.substringAfter(fromHql, "from");
            fromHql = StringUtils.substringBefore(fromHql, "order by");
        }
        else if (fromHql.indexOf("FROM") > 0)
        {
            fromHql = "FROM " + StringUtils.substringAfter(fromHql, "FROM");
            fromHql = StringUtils.substringBefore(fromHql, "ORDER BY");
        }
        
        String countHql = "select count(*) " + fromHql;
        try
        {
            count = (Long)findUnique(countHql, values);
        }
        catch (Exception e)
        {
            throw new RuntimeException("hql can't be auto count, hql is:" + countHql, e);
        }
        return count.longValue();
    }
    
    /**
     * 执行count查询获得本次Hql查询所能获得的对象总数
     * 
     * @param hql
     * @param values
     * @return
     */
    protected long countHqlResult(String hql,Map<String, Object> values)
    {
        Long count = Long.valueOf(0L);
        String fromHql = hql;
        
        // 截取From 后的子句
        Matcher mat = Pattern.compile("(?<=from\\s{1,10}).*", Pattern.CASE_INSENSITIVE).matcher(hql);
        if (mat.find())
            fromHql = mat.group().trim();
        
        // 如果存在Order by 子句，截取Order by 前的子句
        if (StringHelper.containsIgnoreCase(hql, "order by"))
        {
            mat.reset();
            mat = Pattern.compile(".*(?=order by)", Pattern.CASE_INSENSITIVE).matcher(fromHql);
            if (mat.find())
                fromHql = mat.group().trim();
        }
        
        String countHql = "select count(1) from " + fromHql;
        try
        {
            count = (Long)findUnique(countHql, values);
        }
        catch (Exception e)
        {
            throw new RuntimeException("hql can't be auto count, hql is:" + countHql, e);
        }
        
        return count.longValue();
    }
    
    /**
     * 执行count查询获得本次Sql查询所能获得的对象总数
     * 
     * @param sql
     * @param values
     * @return
     */
    protected long countSqlResult(String sql,Map<String, Object> values)
    {
        Long count = Long.valueOf(0L);
        String fromSql = sql;
        
        // 截取From 后的子句
        Matcher mat = Pattern.compile("(?<=from\\s{1,10}).*", Pattern.CASE_INSENSITIVE).matcher(sql);
        if (mat.find())
            fromSql = mat.group().trim();
        
        // 如果存在Order by 子句，截取Order by 前的子句
        if (StringHelper.containsIgnoreCase(sql, "order by"))
        {
            mat.reset();
            mat = Pattern.compile(".*(?=order by)", Pattern.CASE_INSENSITIVE).matcher(fromSql);
            if (mat.find())
                fromSql = mat.group().trim();
        }
        
        String countSql = "select count(1) from " + fromSql;
        try
        {
            BigDecimal countNum = (BigDecimal)findUnique4Sql(countSql, values);
            count = countNum.longValue();
        }
        catch (Exception e)
        {
            throw new RuntimeException("hql can't be auto count, sql is:" + countSql, e);
        }
        
        return count.longValue();
    }
    
    public long countHqlResult(Class<?> cls,Map<String, Object> values)
    {
        
        StringBuilder hql = new StringBuilder("Select Count(1) From " + cls.getName() + " t Where 1=1 ");
        
        for (Map.Entry<String, Object> paramEntry : values.entrySet())
        {
            String paramProperty = paramEntry.getKey();
            hql.append(String.format(" And t.%s = :%s ", new Object[] {paramProperty, paramProperty}));
        }
        
        Query query = createQuery(hql.toString(), values);
        
        return (long) CollectionHelper.getFirstElement(query.list());
    }
    
    /**
     * 执行count查询获得本次Criteria查询所能获得的对象总数
     * 
     * @param c
     * @return
     */
    protected long countCriteriaResult(Criteria c)
    {
        CriteriaImpl impl = (CriteriaImpl)c;
        
        // 先把Projection、ResultTransformer、OrderBy取出来,清空三者后再执行Count操作
        Projection projection = impl.getProjection();
        ResultTransformer transformer = impl.getResultTransformer();
        
        List orderEntries = null;
        try
        {
            orderEntries = (List)ReflectionUtils.getFieldValue(impl, "orderEntries");
            ReflectionUtils.setFieldValue(impl, "orderEntries", new ArrayList());
        }
        catch (Exception e)
        {
            this.logger.error("不可能抛出的异常:{}", e.getMessage());
        }
        
        // 执行Count查询
        long totalCount = ((Long)c.setProjection(Projections.rowCount()).uniqueResult()).longValue();
        
        // 将之前的Projection,ResultTransformer和OrderBy条件重新设回去
        c.setProjection(projection);
        
        if (projection == null)
        {
            c.setResultTransformer(CriteriaSpecification.ROOT_ENTITY);
        }
        if (transformer != null)
        {
            c.setResultTransformer(transformer);
        }
        try
        {
            ReflectionUtils.setFieldValue(impl, "orderEntries", orderEntries);
        }
        catch (Exception e)
        {
            this.logger.error("不可能抛出的异常:{}", e.getMessage());
        }
        
        return totalCount;
    }
    
    // -- 属性过滤条件(FieldFilter)查询函数 --//
    /**
     * 按属性查找对象列表,支持多种匹配方式
     * 
     * @param propertyName
     * @param value
     * @param matchType 匹配方式,目前支持的取值见FieldFilter的MatcheType enum
     * @return
     */
    public List<T> findBy(String propertyName,Object value,MatchType matchType)
    {
        Object[] fieldValues = {value};
        Criterion criterion = buildPropertyFilterCriterion(propertyName, fieldValues, null, matchType);
        Map aliases = buildAlias(propertyName);
        
        return find(aliases, new Criterion[] {criterion});
    }
    
    /**
     * 按属性过滤条件列表查找对象列表
     * 
     * @param propertyName
     * @param values
     * @param matchType
     * @return
     */
    public List<T> findBy(String propertyName,Object[] values,MatchType matchType)
    {
        Criterion criterion = buildPropertyFilterCriterion(propertyName, values, null, matchType);
        Map aliases = buildAlias(propertyName);
        
        return find(aliases, new Criterion[] {criterion});
    }
    
    /**
     * 按属性过滤条件列表查找对象列表
     * 
     * @param propertyName
     * @param ohterField
     * @param matchType
     * @return
     */
    public List<T> findBy(String propertyName,String ohterField,MatchType matchType)
    {
        Criterion criterion = buildPropertyFilterCriterion(propertyName, null, ohterField, matchType);
        Map aliases = buildAlias(propertyName);
        
        return find(aliases, new Criterion[] {criterion});
    }
    
    /**
     * 按属性过滤条件列表查找对象列表
     * 
     * @param filters
     * @return
     */
    public List<T> find(List<PropertyFilter> filters)
    {
        Criterion[] criterions = buildPropertyFilterCriterions(filters);
        Map aliases = buildAlias(filters);
        
        return find(aliases, criterions);
    }
    
    /**
     * 按属性过滤条件列表分页查找对象
     * 
     * @param page
     * @param filters
     * @return
     */
    public Page<T> findPage(Page<T> page,List<PropertyFilter> filters)
    {
        Criterion[] criterions = buildPropertyFilterCriterions(filters);
        Map aliases = buildAlias(filters);
        
        return findPage(page, aliases, criterions);
    }
    
    /**
     * 按属性过滤条件列表分页查找对象
     * 
     * @param page
     * @param filters
     * @return
     */
    public <E> Page<E> findPage(Class<E> cls,Page<E> page,List<PropertyFilter> filters)
    {
        Criterion[] criterions = buildPropertyFilterCriterions(filters);
        Map aliases = buildAlias(filters);
        
        return findPage(cls, page, aliases, criterions);
    }
    
    /**
     * 按属性条件列表创建Criterion数组,辅助函数
     * 
     * @param filter
     * @return
     */
    protected Criterion buildPropertyFilterCriterion(PropertyFilter filter)
    {
        return buildPropertyFilterCriterion(filter.getFieldName(), filter.getValues(), filter.getOtherField(), filter.getMatchType());
    }
    
    /**
     * 按属性条件参数创建Criterion,辅助函数
     * 
     * @param filter
     * @return
     */
    protected Criterion buildMultiFieldFilter(PropertyFilter filter)
    {
        Criterion last = null;
        for (Iterator it = filter.iterator(); it.hasNext();)
        {
            PropertyFilter ff = (PropertyFilter)it.next();
            Criterion c = buildPropertyFilterCriterion(ff);
            if (ff.isAnd())
            {
                last = new SimpleLogicalExpression(last, c, "and");
            }
            else if (ff.isOr())
            {
                last = new SimpleLogicalExpression(last, c, "or");
            }
            else if (ff.isRoundAnd())
            {
                last = Restrictions.and(last, c);
            }
            else if (ff.isRoundOr())
            {
                last = Restrictions.or(last, c);
            }
            else
            {
                last = c;
            }
        }
        return last;
    }
    
    /**
     * 按属性条件参数创建Criterion,辅助函数
     * 
     * @param fieldName
     * @param fieldValues
     * @param otherField
     * @param matchType
     * @return
     */
    protected Criterion buildPropertyFilterCriterion(String fieldName,Object[] fieldValues,String otherField,
        MatchType matchType)
    {
        Assert.hasText(fieldName, "fieldName不能为空");
        Criterion criterion = null;
        try
        {
            if (MatchType.EQ.equals(matchType))
            {
                criterion = Restrictions.eq(fieldName, fieldValues[0]);
            }
            else if (MatchType.LE.equals(matchType))
            {
                criterion = Restrictions.le(fieldName, fieldValues[0]);
            }
            else if (MatchType.LT.equals(matchType))
            {
                criterion = Restrictions.lt(fieldName, fieldValues[0]);
            }
            else if (MatchType.GE.equals(matchType))
            {
                criterion = Restrictions.ge(fieldName, fieldValues[0]);
            }
            else if (MatchType.GT.equals(matchType))
            {
                criterion = Restrictions.gt(fieldName, fieldValues[0]);
            }
            else if (MatchType.NE.equals(matchType))
            {
                criterion = Restrictions.ne(fieldName, fieldValues[0]);
            }
            else if (MatchType.EQF.equals(matchType))
            {
                criterion = Restrictions.eqProperty(fieldName, otherField);
            }
            else if (MatchType.LEF.equals(matchType))
            {
                criterion = Restrictions.leProperty(fieldName, otherField);
            }
            else if (MatchType.LTF.equals(matchType))
            {
                criterion = Restrictions.ltProperty(fieldName, otherField);
            }
            else if (MatchType.GEF.equals(matchType))
            {
                criterion = Restrictions.geProperty(fieldName, otherField);
            }
            else if (MatchType.GTF.equals(matchType))
            {
                criterion = Restrictions.gtProperty(fieldName, otherField);
            }
            else if (MatchType.NEF.equals(matchType))
            {
                criterion = Restrictions.neProperty(fieldName, otherField);
            }
            else if (MatchType.LIKE.equals(matchType))
            {
                // criterion = Restrictions.sqlRestriction("{alias}." + fieldName + " LIKE (?)",
                // "%" + fieldValues[0].toString() + "%",
                // StandardBasicTypes.STRING);
                criterion = Restrictions.like(fieldName, (String)fieldValues[0], MatchMode.ANYWHERE);
            }
            else if (MatchType.LIKESTART.equals(matchType))
            {
                // criterion = Restrictions.sqlRestriction("{alias}." + fieldName + " LIKE (?)",
                // fieldValues[0].toString()
                // + "%", StandardBasicTypes.STRING);
                criterion = Restrictions.like(fieldName, (String)fieldValues[0], MatchMode.START);
            }
            else if (MatchType.LIKEEND.equals(matchType))
            {
                // criterion = Restrictions.sqlRestriction("{alias}." + fieldName + " LIKE (?)",
                // "%" + fieldValues[0].toString(),
                // StandardBasicTypes.STRING);
                criterion = Restrictions.like(fieldName, (String)fieldValues[0], MatchMode.END);
            }
            else if (MatchType.BETWEEN.equals(matchType))
            {
                criterion = Restrictions.between(fieldName, fieldValues[0], fieldValues[1]);
            }
            else if (MatchType.ISNULL.equals(matchType))
            {
                criterion = Restrictions.isNull(fieldName);
            }
            else if (MatchType.NNULL.equals(matchType))
            {
                criterion = Restrictions.isNotNull(fieldName);
            }
            else if (MatchType.IN.equals(matchType))
            {
                criterion = Restrictions.in(fieldName, fieldValues);
            }
        }
        catch (Exception e)
        {
            throw ReflectionUtils.convertToUncheckedException(e);
        }
        return criterion;
    }
    
    /**
     * 按属性条件参数创建Criterion,辅助函数
     * 
     * @param filters
     * @return
     */
    protected Criterion[] buildPropertyFilterCriterions(List<PropertyFilter> filters)
    {
        List criterionList = new ArrayList();
        for (PropertyFilter filter : filters)
        {
            if (!filter.isMulti())
            {
                criterionList.add(buildPropertyFilterCriterion(filter.getFieldName(), filter.getValues(), filter.getOtherField(),
                    filter.getMatchType()));
            }
            else
            {
                criterionList.add(buildMultiFieldFilter(filter));
            }
        }
        return (Criterion[])criterionList.toArray(new Criterion[criterionList.size()]);
    }
    
    protected Map<String, String> buildAlias(String fieldName)
    {
        Map aliases = new HashMap();
        int index = fieldName.indexOf(".");
        if (index > 0)
        {
            String value = fieldName.substring(0, index);
            aliases.put(value, value);
        }
        
        return aliases;
    }
    
    protected Map<String, String> buildAlias(List<PropertyFilter> filters)
    {
        Map aliases = new HashMap();
        for (PropertyFilter propertyFilter : filters)
        {
            String fieldName = propertyFilter.getFieldName();
            int index = fieldName.indexOf(".");
            if (index <= 0)
                continue;
            String value = fieldName.substring(0, index);
            aliases.put(value, value);
        }
        
        return aliases;
    }
    
    /**
     * 判断对象的属性值在数据库内是否唯一 在修改对象的情景下,如果属性新修改的值(value)等于属性原来的值(orgValue)则不作比较
     * 
     * @param propertyName
     * @param newValue
     * @param oldValue
     * @return
     */
    public boolean isPropertyUnique(String propertyName,Object newValue,Object oldValue)
    {
        if ((newValue == null) || (newValue.equals(oldValue)))
            return true;
        Object object = findUniqueBy(propertyName, newValue);
        return object == null;
    }
    
    /**
     * 构建属性过滤器，该方法不支持Between类型的匹配
     * 
     * @param params 参数集合 <属性名，匹配值>
     * @param matchTypes 运算符集合，格式为：<属性名,匹配符>，为空时，使用默认的 MatchType.EQ 相等匹配符
     * @return
     */
    public List<PropertyFilter> buildPropertyFilters(Map<String, Object> params,Map<String, MatchType> matchTypes)
    {
        MatchType defMatch = MatchType.EQ;
        boolean hasSpecMatch = CollectionHelper.isNotEmpty(matchTypes);
        
        MatchType matchType;
        PropertyFilter filter;
        List<PropertyFilter> filters = new ArrayList<PropertyFilter>();
        for (Map.Entry<String, Object> entry : params.entrySet())
        {
            String property = entry.getKey();
            
            // 如果存在特殊匹配符集，且属性在其中有对应值时，使用对应的匹配符，否则使用默认匹配符
            if (hasSpecMatch && matchTypes.containsKey(property))
                matchType = matchTypes.get(property);
            else
                matchType = defMatch;
            
            filter = new PropertyFilter(property, matchType, entry.getValue());
            filters.add(filter);
        }
        
        return filters;
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
        Criterion[] criterions = buildPropertyFilterCriterions(filters); // 构建查询条件
        
        Criteria crit = createCriteria(cls, null, criterions); // 构建查询体
        
        // 构建排序
        for (Map.Entry<String, String> order : orders.entrySet())
        {
            String orderProperty = order.getKey();
            String orderType = order.getValue().toLowerCase();
            
            if ("desc".equals(orderType))
                crit.addOrder(Order.desc(orderProperty));
            else
                crit.addOrder(Order.asc(orderProperty));
        }
        
        return crit.list();
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
        /* 1. 分解Hql语句，分割成查询部分和排序部分 */
        StringBuilder hqlBuff = new StringBuilder("from " + cls.getName() + " t Where 1=1 ");
        
        /* 2.拼接动态参数部分 */
        if (CollectionHelper.isNotEmpty(conditions))
        {
            for (Map.Entry<String, Object> cond : conditions.entrySet())
            {
                String condProperty = cond.getKey();
                Object condParams = cond.getValue();
                
                if (condParams instanceof Collection<?> || condParams instanceof Object[])
                    hqlBuff.append(String.format(" And t.%s In (:%s) ", new Object[] {condProperty, condProperty}));
                else
                    hqlBuff.append(String.format(" And t.%s = :%s ", new Object[] {condProperty, condProperty}));
                
            }
        }
        
        if (CollectionHelper.isNotEmpty(orders))
        {
            String orderHql = buildHqlOrders("t", orders);
            hqlBuff.append(orderHql);
        }
        
        Query query = createQuery(hqlBuff.toString(), conditions);
        return query.list();
    }
    
    /**
     * 构建Hql的查询条件部分的同时，会将参数设置至参数表中
     * 
     * @param alias 对应的对象别名
     * @param filter 查询过滤器
     * @param params 参数表
     * @return
     */
    public String buildHqlCond(String alias,PropertyFilter filter,Map<String, Object> params)
    {
        StringBuilder condBuff = new StringBuilder();
        
        if (StringHelper.isEmpty(alias))
            alias = "t";
        
        String paramName = filter.getFieldName(); // 属性名
        MatchType matcher = filter.getMatchType(); // 匹配方式
        String matcherStr = " = ";
        Object paramValue = filter.getValues()[0]; // 查询值
        
        // 如果是IN 形式的匹配项，直接转换成语句，不需要再逐一设值
        if (MatchType.IN.equals(matcher))
        {
            String inParam = DbHelper.asStyle4InParam((List<?>)paramValue);
            condBuff.append(" AND " + alias + "." + paramName + " IN ").append(inParam);
            return condBuff.toString();
        }
        
        // 如果是Like查询，还需要调整追加对应的通配符
        if (MatchType.LIKESTART.equals(matcher))
        {
            paramValue = paramValue + "%";
            matcherStr = " LIKE ";
        }
        
        else if (MatchType.LIKE.equals(matcher))
        {
            paramValue = "%" + paramValue + "%";
            matcherStr = " LIKE ";
        }
        
        condBuff.append(" AND " + alias + "." + paramName + matcherStr + " :" + paramName);
        params.put(paramName, paramValue); // 将参数和值设置进map中备用
        
        return condBuff.toString();
    }
    
    /**
     * 构建Hql/Sql的Order排序部分的语句
     * 
     * @param alias 对应的别名
     * @param orders 排序表
     * @return
     */
    public String buildHqlOrders(String alias,LinkedHashMap<String, String> orders)
    {
        if (CollectionHelper.isEmpty(orders))
            return "";
        
        String fmt = alias + ".%s %s,";
        StringBuilder orderBuff = new StringBuilder(" Order By ");
        for (Map.Entry<String, String> order : orders.entrySet())
        {
            String orderProperty = order.getKey();
            String orderType = order.getValue().toUpperCase();
            if (!"DESC".equals(orderType))
                orderType = "ASC";
            
            orderBuff.append(String.format(fmt, orderProperty, orderType));
        }
        
        return orderBuff.substring(0, orderBuff.length() - 1);
    }
    
    /**
     * 根据filter内的值构建出对应的数据库条件语句(返回的语句以AND开头，所以不要作为WHERE后面第一个条件)
     * 
     * @param alias 表别名
     * @param filter 查询过滤器
     * @param params 参数Map
     * @return
     */
    public String buildFilterStr(String alias,PropertyFilter filter,Map<String, Object> params)
    {
        StringBuilder filterStr = new StringBuilder("");
        
        String paramName = filter.getFieldName(); // 属性名
        MatchType matcher = filter.getMatchType(); // 匹配方式
        Object paramValue = filter.getValues()[0]; // 查询值
        
        if (MatchType.EQ.equals(matcher))
        {
            filterStr.append(" AND " + alias + "." + paramName + " =:" + paramName);
        }
        else if (MatchType.IN.equals(matcher))
        {
            filterStr.append(" AND " + alias + "." + paramName + " IN(:" + paramName + ")");
        }
        else if (MatchType.LIKE.equals(matcher))
        {
            filterStr.append(" AND " + alias + "." + paramName + " LIKE :" + paramName + "");
            paramValue = "%" + paramValue + "%";
        }
        
        if (StringHelper.isNotEmpty(filterStr.toString()))
        {
            params.put(paramName, paramValue);
        }
        
        return filterStr.toString();
    }
}
