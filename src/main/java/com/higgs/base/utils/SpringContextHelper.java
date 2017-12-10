/*
 *File:BeanFactoryHelper.java
 *company:ECIQ
 *@version: 1.0
 *Date:2015-6-18
 */
package com.higgs.base.utils;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Spring容器获取类
 * 
 * @author terry
 * @since 1.0
 */
@Component
public class SpringContextHelper implements ApplicationContextAware
{
    private static ApplicationContext springContext;
    
    @Override
    public void setApplicationContext(ApplicationContext springContext) throws BeansException
    {
        SpringContextHelper.springContext = springContext;
    }
    
    public static ApplicationContext getSpringContext()
    {
        return springContext;
    }
    
    public static Object getBean(String beanName)
    {
        return springContext.getBean(beanName);
    }
    
}
