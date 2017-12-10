/*
 *File:SimpleLogicalExpression.java
 *company:ECIQ
 *@version: 1.0
 *Date:2014-7-25
 */
package com.higgs.base.orm.assistant;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.criterion.CriteriaQuery;
import org.hibernate.criterion.Criterion;
import org.hibernate.engine.spi.TypedValue;

/**
 * 
 * @author terry
 * @since 1.0
 */
public class SimpleLogicalExpression implements Criterion
{
    private static final long serialVersionUID = 8924312928598715984L;
    
    private final Criterion lhs;
    
    private final Criterion rhs;
    
    private final String op;
    
    public SimpleLogicalExpression(Criterion lhs, Criterion rhs, String op)
    {
        this.lhs = lhs;
        this.rhs = rhs;
        this.op = op;
    }
    
    @Override
    public TypedValue[] getTypedValues(Criteria criteria, CriteriaQuery criteriaQuery)
        throws HibernateException
    {
        TypedValue[] lhstv = this.lhs.getTypedValues(criteria, criteriaQuery);
        TypedValue[] rhstv = this.rhs.getTypedValues(criteria, criteriaQuery);
        TypedValue[] result = new TypedValue[lhstv.length + rhstv.length];
        System.arraycopy(lhstv, 0, result, 0, lhstv.length);
        System.arraycopy(rhstv, 0, result, lhstv.length, rhstv.length);
        return result;
    }
    
    @Override
    public String toSqlString(Criteria criteria, CriteriaQuery criteriaQuery)
        throws HibernateException
    {
        return ' ' + this.lhs.toSqlString(criteria, criteriaQuery) + ' ' + getOp() + ' '
            + this.rhs.toSqlString(criteria, criteriaQuery) + ' ';
    }
    
    public String getOp()
    {
        return this.op;
    }
    
    @Override
    public String toString()
    {
        return this.lhs.toString() + ' ' + getOp() + ' ' + this.rhs.toString();
    }
}
