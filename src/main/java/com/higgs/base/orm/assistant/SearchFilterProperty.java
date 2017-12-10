/*
 *File:SearchFilterProperty.java
 *company:ECIQ
 *@version: 1.0
 *Date:2014-7-25
 */
package com.higgs.base.orm.assistant;

import java.util.Map;

/**
 *
 * @author terry
 * @since 1.0
 */

public class SearchFilterProperty
{
  private String filterName = null;

  private Map<String, String> params = null;

  public SearchFilterProperty()
  {
  }

  public SearchFilterProperty(String filterName, Map<String, String> params)
  {
    this.filterName = filterName;
    this.params = params;
  }

  public String getFilterName()
  {
    return this.filterName;
  }

  public void setFilterName(String filterName)
  {
    this.filterName = filterName;
  }

  public Map<String, String> getParams()
  {
    return this.params;
  }

  public void setParams(Map<String, String> params)
  {
    this.params = params;
  }
}