/*
 *File:BeanFactoryHelper.java
 *company:ECIQ
 *@version: 1.0
 *Date:2015-6-18
 */
package com.higgs.base.utils;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.stereotype.Component;

/**
 * BeanFactory获取类
 * @author terry
 * @since 1.0
 */
@Component
public class BeanFactoryHelper implements BeanFactoryAware
{
    // BEAN工厂
    private static BeanFactory beanFactory; 
    
    @Override
    public void setBeanFactory(BeanFactory bf)
        throws BeansException
    {
        BeanFactoryHelper.beanFactory = bf;
    }

	public static BeanFactory getBeanFactory() {
		return beanFactory;
	}
}
