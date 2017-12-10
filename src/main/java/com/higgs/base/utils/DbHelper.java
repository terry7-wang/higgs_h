/*
 *File:DaoHelper.java
 *company:ECIQ
 *@version: 1.0
 *Date:2014-6-17
 */
package com.higgs.base.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 数据库操作助手
 * 
 * @author terry
 * @since 1.0
 */
public class DbHelper
{
    public static final String ORACLE_DRIVER = "oracle.jdbc.driver.OracleDriver";
    public static final String SQL_SERVER_DRIVER = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    
    /**
     * 打开数据库链接
     * 
     * @param driver 驱动类名
     * @param dbUrl 数据库地址
     * @param userName 用户名
     * @param userPwd 密码
     * @return
     */
    public static Connection openConnection(String driver,String dbUrl,String userName,String userPwd)
    {
        Connection conn = null;
        try
        {
            Class.forName(driver);
            conn = DriverManager.getConnection(dbUrl, userName, userPwd);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return conn;
    }
    
    /**
     * 关闭数据库链接
     * 
     * @param conn
     */
    public static void closeConnection(Connection conn)
    {
        if (conn == null)
        {
            return;
        }
        try
        {
            conn.close();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }
    
    /**
     * 查看数据库的表结构信息
     * 
     * @param conn
     * @param tableName
     */
    public static void showMetaInfo(Connection conn,String tableName)
    {
        String sql = "select * from " + tableName;
        try
        {
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql);
            ResultSetMetaData rsmd = rs.getMetaData();
            
            // 获取字段数
            int columns = rsmd.getColumnCount();
            for (int i = 1; i <= columns; i++)
            {
                System.out.println("Column Label:" + rsmd.getColumnLabel(i));
                System.out.println("Column Name:" + rsmd.getColumnName(i));
                System.out.println("Column Type:" + rsmd.getColumnType(i));
                System.out.println("Column Type Name:" + rsmd.getColumnTypeName(i));
                System.out.println("Column Display Size:" + rsmd.getColumnDisplaySize(i));
                System.out.println("Column Class Name:" + rsmd.getColumnClassName(i));
                System.out.println("*******************");
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        finally
        {
            DbHelper.closeConnection(conn);
        }
    }
    
    public static void sysoutOracleTCloumns(Connection conn,String tableName,String Owner) throws SQLException
    {
        List<HashMap<String, String>> columns = new ArrayList<HashMap<String, String>>();
        Statement stmt = conn.createStatement();
        String sql = "select " + "         comments as \"Name\"," + "         a.column_name \"Code\","
            + "         a.DATA_TYPE as \"DataType\"," + "         b.comments as \"Comment\","
            + "         decode(c.column_name,null,'FALSE','TRUE') as \"Primary\","
            + "         decode(a.NULLABLE,'N','TRUE','Y','FALSE','') as \"Mandatory\"," + "         '' \"sequence\"" + "   from "
            + "       all_tab_columns a, " + "       all_col_comments b," + "       (" + "        select a.constraint_name, a.column_name"
            + "          from user_cons_columns a, user_constraints b" + "         where a.constraint_name = b.constraint_name"
            + "               and b.constraint_type = 'P'" + "               and a.table_name = '" + tableName + "'" + "       ) c"
            + "   where " + "     a.Table_Name=b.table_Name " + "     and a.column_name=b.column_name" + "     and a.Table_Name='" + tableName
            + "'     and a.COLUMN_NAME = c.column_name(+)"
            + "  order by a.COLUMN_ID";
        System.out.println(sql);
        ResultSet rs = stmt.executeQuery(sql);
        while (rs.next())
        {
            HashMap<String, String> map = new HashMap<String, String>();
            map.put("Name", rs.getString("Name"));
            map.put("Code", rs.getString("Code"));
            map.put("DataType", rs.getString("DataType"));
            map.put("Comment", rs.getString("Comment"));
            map.put("Primary", rs.getString("Primary"));
            map.put("Mandatory", rs.getString("Mandatory"));
            columns.add(map);
        }
        
        // 输出
        for (HashMap<String, String> map : columns)
        {
            String Name = map.get("Name");
            String Code = map.get("Code");
            String Comment = map.get("Comment");
            String DataType = map.get("DataType");
            String Primary = map.get("Primary");
            Name = Comment.split("\\s+")[0];
            String Mandatory = map.get("Mandatory");
            String sequence = map.get("sequence");
            String str = "table.cols.add(new Column(\"" + Name + "\",\"" + Code + "\",\"" + Comment + "\",\"" + DataType + "\",\""
                + Primary + "\",\"" + Mandatory + "\",\"" + (sequence == null ? "" : sequence) + "\"));";
            System.out.println(str);
        }
    }
    
    /**
     * 将集合中元素转换成 in 所使用的 ('xxx','yyy') 参数形式，非字符类型的参数，不带单引号
     * 
     * @param list
     * @return
     */
    public static String asStyle4InParam(List<?> list)
    {
        if (CollectionHelper.isEmpty(list))
        {
            return "()";
        }
        
        StringBuilder sb = new StringBuilder("(");
        boolean isString = false;
        // 获取第1个元素的类型
        Class<?> targetCls = CollectionHelper.getFirstElement(list).getClass();
        if (targetCls.equals(String.class))
        {
            isString = true;
        }
        
        for (Object element : list)
        {
            String elementVal = element.toString();
            sb.append("'").append(elementVal).append("',");
        }
        
        String paramsStr = sb.substring(0, sb.length() - 1);
        if (!isString)
        {
            paramsStr = paramsStr.replaceAll("'", "");
        }
        return paramsStr + ")";
    }
    
    public static void main(String[] args) throws SQLException
    {
        String dbUrl = "jdbc:oracle:thin:@192.168.21.160:1521:ora10g";
        String userName = "pld";
        String userPwd = "pld";
        
        Connection conn = DbHelper.openConnection(ORACLE_DRIVER, dbUrl, userName, userPwd);
        DbHelper.sysoutOracleTCloumns(conn, "PLD_ENT_INFO","");
        DbHelper.closeConnection(conn);
    }
    
}
