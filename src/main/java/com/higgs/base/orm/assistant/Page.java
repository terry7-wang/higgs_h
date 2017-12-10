/*
 *File:Page.java
 *company:ECIQ
 *@version: 1.0
 *Date:2014-7-25
 */
package com.higgs.base.orm.assistant;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;

/**
 * 与具体ORM实现无关的分页参数及查询结果封装. 注意所有序号从1开始
 * 
 * @param <T> Page中记录的类型
 * @author terry
 * @since 1.0
 */
public class Page<T>
{
    // 公共变量
    public static final String ASC = "asc";
    
    public static final String DESC = "desc";
    
    // 分页参数
    protected int pageNo = 1;
    
    protected int pageSize = 1;
    
    protected String orderBy = null;
    
    protected String order = null;
    
    protected boolean autoCount = true;
    
    // 返回结果
    protected List<T> result = Collections.emptyList();
    
    protected long totalCount = -1L;
    
    // 构造函数
    public Page()
    {
    }
    
    public Page(int pageSize)
    {
        setPageSize(pageSize);
    }
    
    public Page(int pageSize, boolean autoCount)
    {
        setPageSize(pageSize);
        setAutoCount(autoCount);
    }
    
    /**
     * 获得当前页的页号
     * @return
     */
    public int getPageNo()
    {
        return this.pageNo;
    }
    
    /**
     * 设置当前页的页号,序号从1开始,低于1时自动调整为1
     * @param pageNo
     */
    public void setPageNo(int pageNo)
    {
        this.pageNo = pageNo;
        
        if (pageNo < 1)
        {
            this.pageNo = 1;
        }
    }
    
    /**
     * 返回Page对象自身的setPageNo函数,可用于连续设置
     * @param thePageNo
     * @return
     */
    public Page<T> pageNo(final int thePageNo) {
        setPageNo(thePageNo);
        return this;
    }
    
    /**
     * 获得每页的记录数量
     * @return
     */
    public int getPageSize()
    {
        return this.pageSize;
    }
    
    /**
     * 设置每页的记录数量,低于1时自动调整为1
     * @param pageSize
     */
    public void setPageSize(int pageSize)
    {
        this.pageSize = pageSize;
        
        if (pageSize < 1)
        {
            this.pageSize = 1;
        }
    }
    
    /**
     * 返回Page对象自身的setPageSize函数,可用于连续设置
     * @param thePageSize
     * @return
     */
    public Page<T> pageSize(final int thePageSize) {
        setPageSize(thePageSize);
        return this;
    }
    
    /**
     * 根据pageNo和pageSize计算当前页第一条记录在总结果集中的位置,序号从1开始
     * @return
     */
    public int getFirst()
    {
        return (this.pageNo - 1) * this.pageSize + 1;
    }
    
    /**
     * 获得排序字段,无默认值. 多个排序字段时用','分隔
     * @return
     */
    public String getOrderBy()
    {
        return this.orderBy;
    }
    
    /**
     * 设置排序字段,多个排序字段时用','分隔
     * @param orderBy
     */
    public void setOrderBy(String orderBy)
    {
        this.orderBy = orderBy;
    }
    
    /**
     * 返回Page对象自身的setOrderBy函数,可用于连续设置
     * @param theOrderBy
     * @return
     */
    public Page<T> orderBy(final String theOrderBy) {
        setOrderBy(theOrderBy);
        return this;
    }
    
    /**
     * 是否已设置排序字段,无默认值
     * @return
     */
    public boolean isOrderBySetted()
    {
        return (StringUtils.isNotBlank(this.orderBy)) && (StringUtils.isNotBlank(this.order));
    }
    
    /**
     * 获得排序方向, 无默认值
     * @return
     */
    public String getOrder()
    {
        return this.order;
    }
    
    /**
     * 设置排序方式向
     * @param order 可选值为desc或asc,多个排序字段时用','分隔
     */
    public void setOrder(String order)
    {
        if(StringUtils.isEmpty(order))
            return;
        
        String[] orders = StringUtils.split(StringUtils.lowerCase(order), ',');
        for (String orderStr : orders)
        {
            if ((StringUtils.equals("desc", orderStr)) || (StringUtils.equals("asc", orderStr)))
                continue;
            throw new IllegalArgumentException("排序方向" + orderStr + "不是合法值");
        }
        
        this.order = StringUtils.lowerCase(order);
    }
    
    /**
     * 获得查询对象时是否先自动执行count查询获取总记录数
     * @return
     */
    public boolean isAutoCount()
    {
        return this.autoCount;
    }
    
    /**
     * 设置查询对象时是否自动先执行count查询获取总记录数.
     * @param autoCount
     */
    public void setAutoCount(boolean autoCount)
    {
        this.autoCount = autoCount;
    }
    
    //-- 访问查询结果函数 --//
    
    /**
     * 获得页内的记录列表
     * @return
     */
    public List<T> getResult()
    {
        return this.result;
    }
    
    /**
     * 设置页内的记录列表
     * @param result
     */
    public void setResult(List<T> result)
    {
        this.result = result;
    }
    
    /**
     * 获得总记录数
     * @return
     */
    public long getTotalCount()
    {
        return this.totalCount;
    }
    
    /**
     * 设置总记录数
     * @param totalCount
     */
    public void setTotalCount(long totalCount)
    {
        this.totalCount = totalCount;
    }
    
    /**
     * 根据pageSize与totalCount计算总页数
     * @return
     */
    public long getTotalPages()
    {
        if (this.totalCount < 0L)
            return -1L;
        
        long count = this.totalCount / this.pageSize;
        if (this.totalCount % this.pageSize > 0L)
        {
            count += 1L;
        }
        return count;
    }
    
    /**
     * 是否还有下一页
     * @return
     */
    public boolean isHasNext()
    {
        return this.pageNo + 1 <= getTotalPages();
    }
    
    /**
     * 取得下页的页号, 序号从1开始
     * 当前页为尾页时仍返回尾页序号
     * @return
     */
    public int getNextPage()
    {
        if (isHasNext())
            return this.pageNo + 1;
        return this.pageNo;
    }
    
    /**
     * 是否还有上一页
     * @return
     */
    public boolean isHasPre()
    {
        return this.pageNo - 1 >= 1;
    }
    
    /**
     * 取得上页的页号, 序号从1开始
     * 当前页为首页时返回首页序号
     * @return
     */
    public int getPrePage()
    {
        if (isHasPre())
            return this.pageNo - 1;
        return this.pageNo;
    }
}