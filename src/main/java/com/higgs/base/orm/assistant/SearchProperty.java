/*
 *File:SearchProperty.java
 *company:ECIQ
 *@version: 1.0
 *Date:2014-7-25
 */
package com.higgs.base.orm.assistant;

import java.util.List;

/**
 *
 * @author terry
 * @since 1.0
 */
public class SearchProperty
{
  private List<String> propertyNames = null;

  private String propertyValue = null;

  public SearchProperty()
  {
  }

  public SearchProperty(List<String> propertyNames, String propertyValue)
  {
    this.propertyNames = propertyNames;
    this.propertyValue = propertyValue;
  }

  public void setPropertyNames(List<String> propertyNames)
  {
    this.propertyNames = propertyNames;
  }

  public List<String> getPropertyNames()
  {
    return this.propertyNames;
  }

  public void setPropertyValue(String propertyValue)
  {
    this.propertyValue = propertyValue;
  }

  public String getPropertyValue()
  {
    return this.propertyValue;
  }
}