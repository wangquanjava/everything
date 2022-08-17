package com.github.everything.orm;

import com.google.common.collect.Lists;
import lombok.Data;

import java.sql.*;
import java.util.List;

public class JdbcDemo {
    public static void main(String[] args) {
        ResultSet re = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        Connection connection = null;
        Statement statement = null;
        try {
            connection = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/test", "root", "123456");
            statement = connection.createStatement();
            String sql = "select * from flow";
            re = statement.executeQuery(sql);
            List<Flow> objects = Lists.newArrayList();
            while (re.next()) {
                Flow flow = new Flow();
                flow.setId(re.getInt("id"));
                flow.setTableName(re.getString("table_name"));
                objects.add(flow);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Data
    private static class Flow {
        private int id;
        private String tableName;
    }
}
