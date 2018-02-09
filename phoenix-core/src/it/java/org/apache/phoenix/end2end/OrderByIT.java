/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.phoenix.end2end;

import static org.apache.phoenix.util.TestUtil.ROW1;
import static org.apache.phoenix.util.TestUtil.ROW2;
import static org.apache.phoenix.util.TestUtil.ROW3;
import static org.apache.phoenix.util.TestUtil.ROW4;
import static org.apache.phoenix.util.TestUtil.ROW5;
import static org.apache.phoenix.util.TestUtil.ROW6;
import static org.apache.phoenix.util.TestUtil.ROW7;
import static org.apache.phoenix.util.TestUtil.ROW8;
import static org.apache.phoenix.util.TestUtil.ROW9;
import static org.apache.phoenix.util.TestUtil.TEST_PROPERTIES;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.phoenix.jdbc.PhoenixStatement;
import org.apache.phoenix.util.PropertiesUtil;
import org.junit.Test;


public class OrderByIT extends BaseHBaseManagedTimeIT {

    @Test
    public void testMultiOrderByExpr() throws Exception {
        String tenantId = getOrganizationId();
        initATableValues(tenantId, getDefaultSplits(tenantId), getUrl());
        String query = "SELECT entity_id FROM aTable ORDER BY b_string, entity_id";
        Properties props = PropertiesUtil.deepCopy(TEST_PROPERTIES);
        Connection conn = DriverManager.getConnection(getUrl(), props);
        try {
            PreparedStatement statement = conn.prepareStatement(query);
            ResultSet rs = statement.executeQuery();
            assertTrue (rs.next());
            assertEquals(ROW1,rs.getString(1));
            assertTrue (rs.next());
            assertEquals(ROW4,rs.getString(1));
            assertTrue (rs.next());
            assertEquals(ROW7,rs.getString(1));
            assertTrue (rs.next());
            assertEquals(ROW2,rs.getString(1));
            assertTrue (rs.next());
            assertEquals(ROW5,rs.getString(1));
            assertTrue (rs.next());
            assertEquals(ROW8,rs.getString(1));
            assertTrue (rs.next());
            assertEquals(ROW3,rs.getString(1));
            assertTrue (rs.next());
            assertEquals(ROW6,rs.getString(1));
            assertTrue (rs.next());
            assertEquals(ROW9,rs.getString(1));

            assertFalse(rs.next());
        } finally {
            conn.close();
        }
    }


    @Test
    public void testDescMultiOrderByExpr() throws Exception {
        String tenantId = getOrganizationId();
        initATableValues(tenantId, getDefaultSplits(tenantId), getUrl());
        String query = "SELECT entity_id FROM aTable ORDER BY b_string || entity_id desc";
        Properties props = PropertiesUtil.deepCopy(TEST_PROPERTIES);
        Connection conn = DriverManager.getConnection(getUrl(), props);
        try {
            PreparedStatement statement = conn.prepareStatement(query);
            ResultSet rs = statement.executeQuery();
            assertTrue (rs.next());
            assertEquals(ROW9,rs.getString(1));
            assertTrue (rs.next());
            assertEquals(ROW6,rs.getString(1));
            assertTrue (rs.next());
            assertEquals(ROW3,rs.getString(1));
            assertTrue (rs.next());
            assertEquals(ROW8,rs.getString(1));
            assertTrue (rs.next());
            assertEquals(ROW5,rs.getString(1));
            assertTrue (rs.next());
            assertEquals(ROW2,rs.getString(1));
            assertTrue (rs.next());
            assertEquals(ROW7,rs.getString(1));
            assertTrue (rs.next());
            assertEquals(ROW4,rs.getString(1));
            assertTrue (rs.next());
            assertEquals(ROW1,rs.getString(1));

            assertFalse(rs.next());
        } finally {
            conn.close();
        }
    }

    @Test
    public void testOrderByWithPosition() throws Exception {
        Properties props = PropertiesUtil.deepCopy(TEST_PROPERTIES);
        Connection conn = DriverManager.getConnection(getUrl(), props);
        conn.setAutoCommit(false);

        try {
            String ddl = "CREATE TABLE t_table " +
                    "  (a_string varchar not null, col1 integer" +
                    "  CONSTRAINT pk PRIMARY KEY (a_string))\n";
            createTestTable(getUrl(), ddl);

            String dml = "UPSERT INTO t_table VALUES(?, ?)";
            PreparedStatement stmt = conn.prepareStatement(dml);
            stmt.setString(1, "a");
            stmt.setInt(2, 40);
            stmt.execute();
            stmt.setString(1, "b");
            stmt.setInt(2, 20);
            stmt.execute();
            stmt.setString(1, "c");
            stmt.setInt(2, 30);
            stmt.execute();
            conn.commit();

            String query = "select count(*), col1 from t_table group by col1 order by 2";
            ResultSet rs = conn.createStatement().executeQuery(query);
            assertTrue(rs.next());
            assertEquals(1,rs.getInt(1));
            assertTrue(rs.next());
            assertEquals(1,rs.getInt(1));
            assertTrue(rs.next());
            assertEquals(1,rs.getInt(1));  
            assertFalse(rs.next());  

            query = "select a_string x, col1 y from t_table order by x";
            rs = conn.createStatement().executeQuery(query);
            assertTrue(rs.next());
            assertEquals("a",rs.getString(1));
            assertEquals(40,rs.getInt(2));
            assertTrue(rs.next());
            assertEquals("b",rs.getString(1));
            assertEquals(20,rs.getInt(2));
            assertTrue(rs.next());
            assertEquals("c",rs.getString(1));  
            assertEquals(30,rs.getInt(2));
            assertFalse(rs.next());  

            query = "select * from t_table order by 2";
            rs = conn.createStatement().executeQuery(query);
            assertTrue(rs.next());
            assertEquals("b",rs.getString(1));
            assertEquals(20,rs.getInt(2));
            assertTrue(rs.next());
            assertEquals("c",rs.getString(1));  
            assertEquals(30,rs.getInt(2));
            assertTrue(rs.next());
            assertEquals("a",rs.getString(1));
            assertEquals(40,rs.getInt(2));
            assertFalse(rs.next());  
        } finally {
            conn.close();
        }
    }


    @Test
    public void testColumnFamily() throws Exception {
        Properties props = PropertiesUtil.deepCopy(TEST_PROPERTIES);
        Connection conn = DriverManager.getConnection(getUrl(), props);
        conn.setAutoCommit(false);

        try {
            String ddl = "CREATE TABLE x_table " +
                    "  (a_string varchar not null, cf1.a integer, cf1.b varchar, col1 integer, cf2.c varchar, cf2.d integer, col2 integer" +
                    "  CONSTRAINT pk PRIMARY KEY (a_string))\n";
            createTestTable(getUrl(), ddl);
            String dml = "UPSERT INTO x_table VALUES(?,?,?,?,?,?,?)";
            PreparedStatement stmt = conn.prepareStatement(dml);
            stmt.setString(1, "a");
            stmt.setInt(2, 40);
            stmt.setString(3, "aa");
            stmt.setInt(4, 10);
            stmt.setString(5, "bb");
            stmt.setInt(6, 20);
            stmt.setInt(7, 1);
            stmt.execute();
            stmt.setString(1, "c");
            stmt.setInt(2, 30);
            stmt.setString(3, "cc");
            stmt.setInt(4, 50);
            stmt.setString(5, "dd");
            stmt.setInt(6, 60);
            stmt.setInt(7, 3);
            stmt.execute();
            stmt.setString(1, "b");
            stmt.setInt(2, 40);
            stmt.setString(3, "bb");
            stmt.setInt(4, 5);
            stmt.setString(5, "aa");
            stmt.setInt(6, 80);
            stmt.setInt(7, 2);
            stmt.execute();
            conn.commit();

            String query = "select * from x_table order by 2, 5";
            ResultSet rs = conn.createStatement().executeQuery(query);
            assertTrue(rs.next());
            assertEquals("c",rs.getString(1));
            assertEquals(30,rs.getInt(2));
            assertEquals("cc",rs.getString(3));
            assertEquals(50,rs.getInt(4));
            assertEquals("dd",rs.getString(5));
            assertEquals(60,rs.getInt(6));
            assertEquals(3,rs.getInt(7));
            assertTrue(rs.next());
            assertEquals("b",rs.getString(1));  
            assertEquals(40,rs.getInt(2));
            assertEquals("bb",rs.getString(3));
            assertEquals(5,rs.getInt(4));
            assertEquals("aa",rs.getString(5));
            assertEquals(80,rs.getInt(6));
            assertEquals(2,rs.getInt(7));   
            assertTrue(rs.next());
            assertEquals("a",rs.getString(1));  
            assertEquals(40,rs.getInt(2));
            assertEquals("aa",rs.getString(3));
            assertEquals(10,rs.getInt(4));
            assertEquals("bb",rs.getString(5));
            assertEquals(20,rs.getInt(6));
            assertEquals(1,rs.getInt(7));         
            assertFalse(rs.next());  

            query = "select * from x_table order by 7";
            rs = conn.createStatement().executeQuery(query);
            assertTrue(rs.next());
            assertEquals("a",rs.getString(1));  
            assertEquals(40,rs.getInt(2));
            assertEquals("aa",rs.getString(3));
            assertEquals(10,rs.getInt(4));
            assertEquals("bb",rs.getString(5));
            assertEquals(20,rs.getInt(6));
            assertEquals(1,rs.getInt(7));  
            assertTrue(rs.next());
            assertEquals("b",rs.getString(1));  
            assertEquals(40,rs.getInt(2));
            assertEquals("bb",rs.getString(3));
            assertEquals(5,rs.getInt(4));
            assertEquals("aa",rs.getString(5));
            assertEquals(80,rs.getInt(6));
            assertEquals(2,rs.getInt(7));  
            assertTrue(rs.next());
            assertEquals("c",rs.getString(1));
            assertEquals(30,rs.getInt(2));
            assertEquals("cc",rs.getString(3));
            assertEquals(50,rs.getInt(4));
            assertEquals("dd",rs.getString(5));
            assertEquals(60,rs.getInt(6));
            assertEquals(3,rs.getInt(7));
            assertFalse(rs.next());  
        } finally {
            conn.close();
        }
    }

    @Test
    public void testOrderByWithJoin() throws Exception {
        Properties props = PropertiesUtil.deepCopy(TEST_PROPERTIES);
        Connection conn = DriverManager.getConnection(getUrl(), props);
        conn.setAutoCommit(false);

        try {
            String ddl = "CREATE TABLE s_table " +
                    "  (a_string varchar not null, cf1.a integer, cf1.b varchar, col1 integer, cf2.c varchar, cf2.d integer " +
                    "  CONSTRAINT pk PRIMARY KEY (a_string))\n";
            createTestTable(getUrl(), ddl);
            String dml = "UPSERT INTO s_table VALUES(?,?,?,?,?,?)";
            PreparedStatement stmt = conn.prepareStatement(dml);
            stmt.setString(1, "a");
            stmt.setInt(2, 40);
            stmt.setString(3, "aa");
            stmt.setInt(4, 10);
            stmt.setString(5, "bb");
            stmt.setInt(6, 20);
            stmt.execute();
            stmt.setString(1, "c");
            stmt.setInt(2, 30);
            stmt.setString(3, "cc");
            stmt.setInt(4, 50);
            stmt.setString(5, "dd");
            stmt.setInt(6, 60);
            stmt.execute();
            stmt.setString(1, "b");
            stmt.setInt(2, 40);
            stmt.setString(3, "bb");
            stmt.setInt(4, 5);
            stmt.setString(5, "aa");
            stmt.setInt(6, 80);
            stmt.execute();
            conn.commit();

            ddl = "CREATE TABLE t_table " +
                    "  (a_string varchar not null, col1 integer" +
                    "  CONSTRAINT pk PRIMARY KEY (a_string))\n";
            createTestTable(getUrl(), ddl);

            dml = "UPSERT INTO t_table VALUES(?, ?)";
            stmt = conn.prepareStatement(dml);
            stmt.setString(1, "a");
            stmt.setInt(2, 40);
            stmt.execute();
            stmt.setString(1, "b");
            stmt.setInt(2, 20);
            stmt.execute();
            stmt.setString(1, "c");
            stmt.setInt(2, 30);
            stmt.execute();
            conn.commit();

            String query = "select t1.* from s_table t1 join t_table t2 on t1.a_string = t2.a_string order by 3";
            ResultSet rs = conn.createStatement().executeQuery(query);
            assertTrue(rs.next());
            assertEquals("a",rs.getString(1));  
            assertEquals(40,rs.getInt(2));
            assertEquals("aa",rs.getString(3));
            assertEquals(10,rs.getInt(4));
            assertEquals("bb",rs.getString(5));
            assertEquals(20,rs.getInt(6));
            assertTrue(rs.next());
            assertEquals("b",rs.getString(1));  
            assertEquals(40,rs.getInt(2));
            assertEquals("bb",rs.getString(3));
            assertEquals(5,rs.getInt(4));
            assertEquals("aa",rs.getString(5));
            assertEquals(80,rs.getInt(6));         
            assertTrue(rs.next());
            assertEquals("c",rs.getString(1));
            assertEquals(30,rs.getInt(2));
            assertEquals("cc",rs.getString(3));
            assertEquals(50,rs.getInt(4));
            assertEquals("dd",rs.getString(5));
            assertEquals(60,rs.getInt(6));
            assertFalse(rs.next());  

            query = "select t1.a_string, t2.col1 from s_table t1 join t_table t2 on t1.a_string = t2.a_string order by 2";
            rs = conn.createStatement().executeQuery(query);
            assertTrue(rs.next());
            assertEquals("b",rs.getString(1));  
            assertEquals(20,rs.getInt(2));
            assertTrue(rs.next());
            assertEquals("c",rs.getString(1));  
            assertEquals(30,rs.getInt(2));
            assertTrue(rs.next());
            assertEquals("a",rs.getString(1));  
            assertEquals(40,rs.getInt(2));
            assertFalse(rs.next()); 
        } catch (SQLException e) {
        } finally {
            conn.close();
        }
    }

    @Test
    public void testOrderByWithUnionAll() throws Exception {
        Properties props = PropertiesUtil.deepCopy(TEST_PROPERTIES);
        Connection conn = DriverManager.getConnection(getUrl(), props);
        conn.setAutoCommit(false);

        try {
            String ddl = "CREATE TABLE x_table " +
                    "  (a_string varchar not null, cf1.a integer, cf1.b varchar, col1 integer, cf2.c varchar, cf2.d integer " +
                    "  CONSTRAINT pk PRIMARY KEY (a_string))\n";
            createTestTable(getUrl(), ddl);
            String dml = "UPSERT INTO x_table VALUES(?,?,?,?,?,?)";
            PreparedStatement stmt = conn.prepareStatement(dml);
            stmt.setString(1, "a");
            stmt.setInt(2, 40);
            stmt.setString(3, "aa");
            stmt.setInt(4, 10);
            stmt.setString(5, "bb");
            stmt.setInt(6, 20);
            stmt.execute();
            stmt.setString(1, "c");
            stmt.setInt(2, 30);
            stmt.setString(3, "cc");
            stmt.setInt(4, 50);
            stmt.setString(5, "dd");
            stmt.setInt(6, 60);
            stmt.execute();
            stmt.setString(1, "b");
            stmt.setInt(2, 40);
            stmt.setString(3, "bb");
            stmt.setInt(4, 5);
            stmt.setString(5, "aa");
            stmt.setInt(6, 80);
            stmt.execute();
            conn.commit();

            ddl = "CREATE TABLE y_table " +
                    "  (a_string varchar not null, col1 integer" +
                    "  CONSTRAINT pk PRIMARY KEY (a_string))\n";
            createTestTable(getUrl(), ddl);

            dml = "UPSERT INTO y_table VALUES(?, ?)";
            stmt = conn.prepareStatement(dml);
            stmt.setString(1, "aa");
            stmt.setInt(2, 40);
            stmt.execute();
            stmt.setString(1, "bb");
            stmt.setInt(2, 10);
            stmt.execute();
            stmt.setString(1, "cc");
            stmt.setInt(2, 30);
            stmt.execute();
            conn.commit();

            String query = "select a_string, cf2.d from x_table union all select * from y_table order by 2";
            ResultSet rs = conn.createStatement().executeQuery(query);
            assertTrue(rs.next());
            assertEquals("bb",rs.getString(1));  
            assertEquals(10,rs.getInt(2));
            assertTrue(rs.next());
            assertEquals("a",rs.getString(1));  
            assertEquals(20,rs.getInt(2));      
            assertTrue(rs.next());
            assertEquals("cc",rs.getString(1));
            assertEquals(30,rs.getInt(2));
            assertTrue(rs.next());
            assertEquals("aa",rs.getString(1));  
            assertEquals(40,rs.getInt(2));
            assertTrue(rs.next());
            assertEquals("c",rs.getString(1));  
            assertEquals(60,rs.getInt(2));      
            assertTrue(rs.next());
            assertEquals("b",rs.getString(1));
            assertEquals(80,rs.getInt(2));
            assertFalse(rs.next());  
        } finally {
            conn.close();
        }
    }

    @Test
    public void testOrderByWithExpression() throws Exception {
        Properties props = PropertiesUtil.deepCopy(TEST_PROPERTIES);
        Connection conn = DriverManager.getConnection(getUrl(), props);
        conn.setAutoCommit(false);

        try {
            String ddl = "CREATE TABLE e_table " +
                    "  (a_string varchar not null, col1 integer, col2 integer, col3 timestamp, col4 varchar" +
                    "  CONSTRAINT pk PRIMARY KEY (a_string))\n";
            createTestTable(getUrl(), ddl);

            Date date = new Date(System.currentTimeMillis());
            String dml = "UPSERT INTO e_table VALUES(?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(dml);
            stmt.setString(1, "a");
            stmt.setInt(2, 40);
            stmt.setInt(3, 20);
            stmt.setDate(4, new Date(date.getTime()));
            stmt.setString(5, "xxyy");
            stmt.execute();
            stmt.setString(1, "b");
            stmt.setInt(2, 50);
            stmt.setInt(3, 30);
            stmt.setDate(4, new Date(date.getTime()-500));
            stmt.setString(5, "yyzz");
            stmt.execute();
            stmt.setString(1, "c");
            stmt.setInt(2, 60);
            stmt.setInt(3, 20);
            stmt.setDate(4, new Date(date.getTime()-300));
            stmt.setString(5, "ddee");
            stmt.execute();
            conn.commit();

            String query = "SELECT col1+col2, col4, a_string FROM e_table ORDER BY 1, 2";
            ResultSet rs = conn.createStatement().executeQuery(query);
            assertTrue(rs.next());
            assertEquals("a", rs.getString(3));
            assertTrue(rs.next());
            assertEquals("c", rs.getString(3));
            assertTrue(rs.next());
            assertEquals("b", rs.getString(3));
            assertFalse(rs.next());
        } catch (SQLException e) {
        } finally {
            conn.close();
        }
    }
    
    @Test
    public void testOrderByRVC() throws Exception {
        Properties props = PropertiesUtil.deepCopy(TEST_PROPERTIES);
        Connection conn = DriverManager.getConnection(getUrl(), props);
        String ddl = "create table test1 (testpk varchar not null primary key, l_quantity decimal(15,2), l_discount decimal(15,2))";
        conn.createStatement().execute(ddl);

        PreparedStatement stmt = conn.prepareStatement("upsert into test1 values ('a',0.1,0.9)");
        stmt.execute();
        stmt = conn.prepareStatement(" upsert into test1 values ('b',0.5,0.5)");
        stmt.execute();
        stmt = conn.prepareStatement(" upsert into test1 values ('c',0.9,0.1)");
        stmt.execute();
        conn.commit();

        ResultSet rs;
        stmt = conn.prepareStatement("select l_discount,testpk from test1 order by (l_discount,l_quantity)");
        rs = stmt.executeQuery();
        assertTrue(rs.next());
        assertEquals(0.1, rs.getDouble(1), 0.01);
        assertEquals("c", rs.getString(2));
        assertTrue(rs.next());
        assertEquals(0.5, rs.getDouble(1), 0.01);
        assertEquals("b", rs.getString(2));
        assertTrue(rs.next());
        assertEquals(0.9, rs.getDouble(1), 0.01);
        assertEquals("a", rs.getString(2));
    }

    @Test
    public void testAggregateOrderBy() throws Exception {
        Properties props = PropertiesUtil.deepCopy(TEST_PROPERTIES);
        Connection conn = DriverManager.getConnection(getUrl(), props);
        String tableName = "testAggregateOrderBy";
        String ddl = "create table " + tableName + " (ID VARCHAR NOT NULL PRIMARY KEY, VAL1 VARCHAR, VAL2 INTEGER)";
        conn.createStatement().execute(ddl);

        conn.createStatement().execute("upsert into " + tableName + " values ('ABC','aa123', 11)");
        conn.createStatement().execute("upsert into " + tableName + " values ('ABD','ba124', 1)");
        conn.createStatement().execute("upsert into " + tableName + " values ('ABE','cf125', 13)");
        conn.createStatement().execute("upsert into " + tableName + " values ('ABF','dan126', 4)");
        conn.createStatement().execute("upsert into " + tableName + " values ('ABG','elf127', 15)");
        conn.createStatement().execute("upsert into " + tableName + " values ('ABH','fan128', 6)");
        conn.createStatement().execute("upsert into " + tableName + " values ('AAA','get211', 100)");
        conn.createStatement().execute("upsert into " + tableName + " values ('AAB','hat212', 7)");
        conn.createStatement().execute("upsert into " + tableName + " values ('AAC','aap12', 2)");
        conn.createStatement().execute("upsert into " + tableName + " values ('AAD','ball12', 3)");
        conn.createStatement().execute("upsert into " + tableName + " values ('AAE','inn2110', 13)");
        conn.createStatement().execute("upsert into " + tableName + " values ('AAF','key2112', 40)");
        conn.commit();

        ResultSet rs;
        PhoenixStatement stmt = conn.createStatement().unwrap(PhoenixStatement.class);
        rs = stmt.executeQuery("select distinct ID, VAL1, VAL2 from " + tableName + " where ID in ('ABC','ABD','ABE','ABF','ABG','ABH','AAA', 'AAB', 'AAC','AAD','AAE','AAF') order by VAL1");
        assertFalse(stmt.getQueryPlan().getOrderBy().getOrderByExpressions().isEmpty());
        assertTrue(rs.next());
        assertEquals("ABC", rs.getString(1));
        assertEquals("aa123", rs.getString(2));
        assertTrue(rs.next());
        assertEquals("aap12", rs.getString(2));
        assertTrue(rs.next());
        assertEquals("ba124", rs.getString(2));
        assertTrue(rs.next());
        assertEquals("ball12", rs.getString(2));
        assertTrue(rs.next());
        assertEquals("cf125", rs.getString(2));
        assertTrue(rs.next());
        assertEquals("dan126", rs.getString(2));
        assertTrue(rs.next());
        assertEquals("elf127", rs.getString(2));
        assertTrue(rs.next());
        assertEquals("fan128", rs.getString(2));
        assertTrue(rs.next());
        assertEquals("get211", rs.getString(2));
        assertTrue(rs.next());
        assertEquals("hat212", rs.getString(2));
        assertTrue(rs.next());
        assertEquals("inn2110", rs.getString(2));
        assertTrue(rs.next());
        assertEquals("AAF", rs.getString(1));
        assertEquals("key2112", rs.getString(2));
        assertFalse(rs.next());
    }

    @Test
    public void testAggregateOptimizedOutOrderBy() throws Exception {
        Properties props = PropertiesUtil.deepCopy(TEST_PROPERTIES);
        Connection conn = DriverManager.getConnection(getUrl(), props);
        String tableName = "testAggregateOptimized";
        String ddl = "create table " + tableName + " (K1 VARCHAR NOT NULL, K2 VARCHAR NOT NULL, VAL1 VARCHAR, VAL2 INTEGER, CONSTRAINT pk PRIMARY KEY(K1,K2))";
        conn.createStatement().execute(ddl);

        conn.createStatement().execute("upsert into " + tableName + " values ('ABC','ABC','aa123', 11)");
        conn.createStatement().execute("upsert into " + tableName + " values ('ABD','ABC','ba124', 1)");
        conn.createStatement().execute("upsert into " + tableName + " values ('ABE','ABC','cf125', 13)");
        conn.createStatement().execute("upsert into " + tableName + " values ('ABF','ABC','dan126', 4)");
        conn.createStatement().execute("upsert into " + tableName + " values ('ABG','ABC','elf127', 15)");
        conn.createStatement().execute("upsert into " + tableName + " values ('ABH','ABC','fan128', 6)");
        conn.createStatement().execute("upsert into " + tableName + " values ('AAA','ABC','get211', 100)");
        conn.createStatement().execute("upsert into " + tableName + " values ('AAB','ABC','hat212', 7)");
        conn.createStatement().execute("upsert into " + tableName + " values ('AAC','ABC','aap12', 2)");
        conn.createStatement().execute("upsert into " + tableName + " values ('AAD','ABC','ball12', 3)");
        conn.createStatement().execute("upsert into " + tableName + " values ('AAE','ABC','inn2110', 13)");
        conn.createStatement().execute("upsert into " + tableName + " values ('AAF','ABC','key2112', 40)");
        conn.commit();

        ResultSet rs;
        PhoenixStatement stmt = conn.createStatement().unwrap(PhoenixStatement.class);
        rs = stmt.executeQuery("select distinct K2, VAL1, VAL2 from " + tableName + " where K2 = 'ABC' order by VAL1");
        assertTrue(stmt.getQueryPlan().getOrderBy().getOrderByExpressions().isEmpty());
        assertTrue(rs.next());
        assertEquals("ABC", rs.getString(1));
        assertEquals("aa123", rs.getString(2));
        assertTrue(rs.next());
        assertEquals("aap12", rs.getString(2));
        assertTrue(rs.next());
        assertEquals("ba124", rs.getString(2));
        assertTrue(rs.next());
        assertEquals("ball12", rs.getString(2));
        assertTrue(rs.next());
        assertEquals("cf125", rs.getString(2));
        assertTrue(rs.next());
        assertEquals("dan126", rs.getString(2));
        assertTrue(rs.next());
        assertEquals("elf127", rs.getString(2));
        assertTrue(rs.next());
        assertEquals("fan128", rs.getString(2));
        assertTrue(rs.next());
        assertEquals("get211", rs.getString(2));
        assertTrue(rs.next());
        assertEquals("hat212", rs.getString(2));
        assertTrue(rs.next());
        assertEquals("inn2110", rs.getString(2));
        assertTrue(rs.next());
        assertEquals("ABC", rs.getString(1));
        assertEquals("key2112", rs.getString(2));
        assertFalse(rs.next());
    }

    @Test
    public void testNullsLastWithDesc() throws Exception {
        Connection conn=null;
        try {
            Properties props = PropertiesUtil.deepCopy(TEST_PROPERTIES);
            conn = DriverManager.getConnection(getUrl(), props);

            String tableName="testNullLastWithDesc";
            conn.createStatement().execute("DROP TABLE if exists "+tableName);
            String sql="CREATE TABLE "+tableName+" ( "+
                "ORGANIZATION_ID VARCHAR,"+
                "CONTAINER_ID VARCHAR,"+
                "ENTITY_ID VARCHAR NOT NULL,"+
                "CONSTRAINT TEST_PK PRIMARY KEY ( "+
                "ORGANIZATION_ID DESC,"+
                "CONTAINER_ID DESC,"+
                "ENTITY_ID"+
                "))";
            conn.createStatement().execute(sql);

            conn.createStatement().execute("UPSERT INTO "+tableName+" VALUES ('a',null,'11')");
            conn.createStatement().execute("UPSERT INTO "+tableName+" VALUES (null,'2','22')");
            conn.createStatement().execute("UPSERT INTO "+tableName+" VALUES ('c','3','33')");
            conn.commit();

            //-----ORGANIZATION_ID

            sql="SELECT CONTAINER_ID,ORGANIZATION_ID FROM "+tableName+" order by ORGANIZATION_ID ASC NULLS FIRST";
            ResultSet rs=conn.prepareStatement(sql).executeQuery();
            assertResultSet(rs, new String[][]{{"2",null},{null,"a"},{"3","c"},});

            sql="SELECT CONTAINER_ID,ORGANIZATION_ID FROM "+tableName+" order by ORGANIZATION_ID ASC NULLS LAST";
            rs=conn.prepareStatement(sql).executeQuery();
            assertResultSet(rs, new String[][]{{null,"a"},{"3","c"},{"2",null}});

            sql="SELECT CONTAINER_ID,ORGANIZATION_ID FROM "+tableName+" order by ORGANIZATION_ID DESC NULLS FIRST";
            rs=conn.prepareStatement(sql).executeQuery();
            assertResultSet(rs, new String[][]{{"2",null},{"3","c"},{null,"a"}});

            sql="SELECT CONTAINER_ID,ORGANIZATION_ID FROM "+tableName+" order by ORGANIZATION_ID DESC NULLS LAST";
            rs=conn.prepareStatement(sql).executeQuery();
            assertResultSet(rs, new String[][]{{"3","c"},{null,"a"},{"2",null}});

            //----CONTAINER_ID

            sql="SELECT CONTAINER_ID,ORGANIZATION_ID FROM "+tableName+" order by CONTAINER_ID ASC NULLS FIRST";
            rs=conn.prepareStatement(sql).executeQuery();
            assertResultSet(rs, new String[][]{{null,"a"},{"2",null},{"3","c"}});

            sql="SELECT CONTAINER_ID,ORGANIZATION_ID FROM "+tableName+" order by CONTAINER_ID ASC NULLS LAST";
            rs=conn.prepareStatement(sql).executeQuery();
            assertResultSet(rs, new String[][]{{"2",null},{"3","c"},{null,"a"}});

            sql="SELECT CONTAINER_ID,ORGANIZATION_ID FROM "+tableName+" order by CONTAINER_ID DESC NULLS FIRST";
            rs=conn.prepareStatement(sql).executeQuery();
            assertResultSet(rs, new String[][]{{null,"a"},{"3","c"},{"2",null}});

            sql="SELECT CONTAINER_ID,ORGANIZATION_ID FROM "+tableName+" order by CONTAINER_ID DESC NULLS LAST";
            rs=conn.prepareStatement(sql).executeQuery();
            assertResultSet(rs, new String[][]{{"3","c"},{"2",null},{null,"a"}});

            conn.createStatement().execute("UPSERT INTO "+tableName+" VALUES (null,null,'44')");
            conn.commit();

            //-----ORGANIZATION_ID ASC  CONTAINER_ID ASC

            sql="SELECT CONTAINER_ID,ORGANIZATION_ID FROM "+tableName+" order by ORGANIZATION_ID NULLS FIRST,CONTAINER_ID NULLS FIRST";
            rs=conn.prepareStatement(sql).executeQuery();
            assertResultSet(rs, new String[][]{{null,null},{"2",null},{null,"a"},{"3","c"}});

            sql="SELECT CONTAINER_ID,ORGANIZATION_ID FROM "+tableName+" order by ORGANIZATION_ID NULLS FIRST,CONTAINER_ID NULLS LAST";
            rs=conn.prepareStatement(sql).executeQuery();
            assertResultSet(rs, new String[][]{{"2",null},{null,null},{null,"a"},{"3","c"}});

            sql="SELECT CONTAINER_ID,ORGANIZATION_ID FROM "+tableName+" order by ORGANIZATION_ID NULLS LAST,CONTAINER_ID NULLS FIRST";
            rs=conn.prepareStatement(sql).executeQuery();
            assertResultSet(rs, new String[][]{{null,"a"},{"3","c"},{null,null},{"2",null}});

            sql="SELECT CONTAINER_ID,ORGANIZATION_ID FROM "+tableName+" order by ORGANIZATION_ID NULLS LAST,CONTAINER_ID NULLS LAST";
            rs=conn.prepareStatement(sql).executeQuery();
            assertResultSet(rs, new String[][]{{null,"a"},{"3","c"},{"2",null},{null,null}});


            //-----ORGANIZATION_ID ASC  CONTAINER_ID DESC

            sql="SELECT CONTAINER_ID,ORGANIZATION_ID FROM "+tableName+" order by ORGANIZATION_ID NULLS FIRST,CONTAINER_ID DESC NULLS FIRST";
            rs=conn.prepareStatement(sql).executeQuery();
            assertResultSet(rs, new String[][]{{null,null},{"2",null},{null,"a"},{"3","c"}});

            sql="SELECT CONTAINER_ID,ORGANIZATION_ID FROM "+tableName+" order by ORGANIZATION_ID NULLS FIRST,CONTAINER_ID DESC NULLS LAST";
            rs=conn.prepareStatement(sql).executeQuery();
            assertResultSet(rs, new String[][]{{"2",null},{null,null},{null,"a"},{"3","c"}});

            sql="SELECT CONTAINER_ID,ORGANIZATION_ID FROM "+tableName+" order by ORGANIZATION_ID NULLS LAST,CONTAINER_ID DESC NULLS FIRST";
            rs=conn.prepareStatement(sql).executeQuery();
            assertResultSet(rs, new String[][]{{null,"a"},{"3","c"},{null,null},{"2",null}});

            sql="SELECT CONTAINER_ID,ORGANIZATION_ID FROM "+tableName+" order by ORGANIZATION_ID NULLS LAST,CONTAINER_ID DESC NULLS LAST";
            rs=conn.prepareStatement(sql).executeQuery();
            assertResultSet(rs, new String[][]{{null,"a"},{"3","c"},{"2",null},{null,null}});

            //-----ORGANIZATION_ID DESC  CONTAINER_ID ASC

            sql="SELECT CONTAINER_ID,ORGANIZATION_ID FROM "+tableName+" order by ORGANIZATION_ID DESC NULLS FIRST,CONTAINER_ID NULLS FIRST";
            rs=conn.prepareStatement(sql).executeQuery();
            assertResultSet(rs, new String[][]{{null,null},{"2",null},{"3","c"},{null,"a"}});

            sql="SELECT CONTAINER_ID,ORGANIZATION_ID FROM "+tableName+" order by ORGANIZATION_ID DESC NULLS FIRST,CONTAINER_ID NULLS LAST";
            rs=conn.prepareStatement(sql).executeQuery();
            assertResultSet(rs, new String[][]{{"2",null},{null,null},{"3","c"},{null,"a"}});

            sql="SELECT CONTAINER_ID,ORGANIZATION_ID FROM "+tableName+" order by ORGANIZATION_ID DESC NULLS LAST,CONTAINER_ID NULLS FIRST";
            rs=conn.prepareStatement(sql).executeQuery();
            assertResultSet(rs, new String[][]{{"3","c"},{null,"a"},{null,null},{"2",null}});

            sql="SELECT CONTAINER_ID,ORGANIZATION_ID FROM "+tableName+" order by ORGANIZATION_ID DESC NULLS LAST,CONTAINER_ID NULLS LAST";
            rs=conn.prepareStatement(sql).executeQuery();
            assertResultSet(rs, new String[][]{{"3","c"},{null,"a"},{"2",null},{null,null}});

            //-----ORGANIZATION_ID DESC  CONTAINER_ID DESC

            sql="SELECT CONTAINER_ID,ORGANIZATION_ID FROM "+tableName+" order by ORGANIZATION_ID DESC NULLS FIRST,CONTAINER_ID DESC NULLS FIRST";
            rs=conn.prepareStatement(sql).executeQuery();
            assertResultSet(rs, new String[][]{{null,null},{"2",null},{"3","c"},{null,"a"}});

            sql="SELECT CONTAINER_ID,ORGANIZATION_ID FROM "+tableName+" order by ORGANIZATION_ID DESC NULLS FIRST,CONTAINER_ID DESC NULLS LAST";
            rs=conn.prepareStatement(sql).executeQuery();
            assertResultSet(rs, new String[][]{{"2",null},{null,null},{"3","c"},{null,"a"}});

            sql="SELECT CONTAINER_ID,ORGANIZATION_ID FROM "+tableName+" order by ORGANIZATION_ID DESC NULLS LAST,CONTAINER_ID DESC NULLS FIRST";
            rs=conn.prepareStatement(sql).executeQuery();
            assertResultSet(rs, new String[][]{{"3","c"},{null,"a"},{null,null},{"2",null}});

            sql="SELECT CONTAINER_ID,ORGANIZATION_ID FROM "+tableName+" order by ORGANIZATION_ID DESC NULLS LAST,CONTAINER_ID DESC NULLS LAST";
            rs=conn.prepareStatement(sql).executeQuery();
            assertResultSet(rs, new String[][]{{"3","c"},{null,"a"},{"2",null},{null,null}});

            //-----CONTAINER_ID ASC  ORGANIZATION_ID ASC

            sql="SELECT CONTAINER_ID,ORGANIZATION_ID FROM "+tableName+" order by CONTAINER_ID NULLS FIRST,ORGANIZATION_ID NULLS FIRST";
            rs=conn.prepareStatement(sql).executeQuery();
            assertResultSet(rs, new String[][]{{null,null},{null,"a"},{"2",null},{"3","c"}});

            sql="SELECT CONTAINER_ID,ORGANIZATION_ID FROM "+tableName+" order by CONTAINER_ID NULLS FIRST,ORGANIZATION_ID NULLS LAST";
            rs=conn.prepareStatement(sql).executeQuery();
            assertResultSet(rs, new String[][]{{null,"a"},{null,null},{"2",null},{"3","c"}});

            sql="SELECT CONTAINER_ID,ORGANIZATION_ID FROM "+tableName+" order by CONTAINER_ID NULLS LAST,ORGANIZATION_ID NULLS FIRST";
            rs=conn.prepareStatement(sql).executeQuery();
            assertResultSet(rs, new String[][]{{"2",null},{"3","c"},{null,null},{null,"a"}});

            sql="SELECT CONTAINER_ID,ORGANIZATION_ID FROM "+tableName+" order by CONTAINER_ID NULLS LAST,ORGANIZATION_ID NULLS LAST";
            rs=conn.prepareStatement(sql).executeQuery();
            assertResultSet(rs, new String[][]{{"2",null},{"3","c"},{null,"a"},{null,null}});

            //-----CONTAINER_ID ASC  ORGANIZATION_ID DESC

            sql="SELECT CONTAINER_ID,ORGANIZATION_ID FROM "+tableName+" order by CONTAINER_ID ASC NULLS FIRST,ORGANIZATION_ID DESC NULLS FIRST";
            rs=conn.prepareStatement(sql).executeQuery();
            assertResultSet(rs, new String[][]{{null,null},{null,"a"},{"2",null},{"3","c"}});

            sql="SELECT CONTAINER_ID,ORGANIZATION_ID FROM "+tableName+" order by CONTAINER_ID ASC NULLS FIRST,ORGANIZATION_ID DESC NULLS LAST";
            rs=conn.prepareStatement(sql).executeQuery();
            assertResultSet(rs, new String[][]{{null,"a"},{null,null},{"2",null},{"3","c"}});

            sql="SELECT CONTAINER_ID,ORGANIZATION_ID FROM "+tableName+" order by CONTAINER_ID ASC NULLS LAST,ORGANIZATION_ID DESC NULLS FIRST";
            rs=conn.prepareStatement(sql).executeQuery();
            assertResultSet(rs, new String[][]{{"2",null},{"3","c"},{null,null},{null,"a"}});

            sql="SELECT CONTAINER_ID,ORGANIZATION_ID FROM "+tableName+" order by CONTAINER_ID ASC NULLS LAST,ORGANIZATION_ID DESC NULLS LAST";
            rs=conn.prepareStatement(sql).executeQuery();
            assertResultSet(rs, new String[][]{{"2",null},{"3","c"},{null,"a"},{null,null}});

            //-----CONTAINER_ID DESC  ORGANIZATION_ID ASC

            sql="SELECT CONTAINER_ID,ORGANIZATION_ID FROM "+tableName+" order by CONTAINER_ID DESC NULLS FIRST,ORGANIZATION_ID ASC NULLS FIRST";
            rs=conn.prepareStatement(sql).executeQuery();
            assertResultSet(rs, new String[][]{{null,null},{null,"a"},{"3","c"},{"2",null}});

            sql="SELECT CONTAINER_ID,ORGANIZATION_ID FROM "+tableName+" order by CONTAINER_ID DESC NULLS FIRST,ORGANIZATION_ID ASC NULLS LAST";
            rs=conn.prepareStatement(sql).executeQuery();
            assertResultSet(rs, new String[][]{{null,"a"},{null,null},{"3","c"},{"2",null}});

            sql="SELECT CONTAINER_ID,ORGANIZATION_ID FROM "+tableName+" order by CONTAINER_ID DESC NULLS LAST,ORGANIZATION_ID ASC NULLS FIRST";
            rs=conn.prepareStatement(sql).executeQuery();
            assertResultSet(rs, new String[][]{{"3","c"},{"2",null},{null,null},{null,"a"}});

            sql="SELECT CONTAINER_ID,ORGANIZATION_ID FROM "+tableName+" order by CONTAINER_ID DESC NULLS LAST,ORGANIZATION_ID ASC NULLS LAST";
            rs=conn.prepareStatement(sql).executeQuery();
            assertResultSet(rs, new String[][]{{"3","c"},{"2",null},{null,"a"},{null,null}});

            //-----CONTAINER_ID DESC  ORGANIZATION_ID DESC

            sql="SELECT CONTAINER_ID,ORGANIZATION_ID FROM "+tableName+" order by CONTAINER_ID DESC NULLS FIRST,ORGANIZATION_ID DESC NULLS FIRST";
            rs=conn.prepareStatement(sql).executeQuery();
            assertResultSet(rs, new String[][]{{null,null},{null,"a"},{"3","c"},{"2",null}});

            sql="SELECT CONTAINER_ID,ORGANIZATION_ID FROM "+tableName+" order by CONTAINER_ID DESC NULLS FIRST,ORGANIZATION_ID DESC NULLS LAST";
            rs=conn.prepareStatement(sql).executeQuery();
            assertResultSet(rs, new String[][]{{null,"a"},{null,null},{"3","c"},{"2",null}});

            sql="SELECT CONTAINER_ID,ORGANIZATION_ID FROM "+tableName+" order by CONTAINER_ID DESC NULLS LAST,ORGANIZATION_ID DESC NULLS FIRST";
            rs=conn.prepareStatement(sql).executeQuery();
            assertResultSet(rs, new String[][]{{"3","c"},{"2",null},{null,null},{null,"a"}});

            sql="SELECT CONTAINER_ID,ORGANIZATION_ID FROM "+tableName+" order by CONTAINER_ID DESC NULLS LAST,ORGANIZATION_ID DESC NULLS LAST";
            rs=conn.prepareStatement(sql).executeQuery();
            assertResultSet(rs, new String[][]{{"3","c"},{"2",null},{null,"a"},{null,null}});
        } finally {
            if(conn!=null) {
                conn.close();
            }
        }
    }

    private void assertResultSet(ResultSet rs,String[][] rows) throws Exception {
        for(int rowIndex=0;rowIndex<rows.length;rowIndex++) {
            assertTrue(rs.next());
            for(int columnIndex=1;columnIndex<= rows[rowIndex].length;columnIndex++) {
                String realValue=rs.getString(columnIndex);
                String expectedValue=rows[rowIndex][columnIndex-1];
                if(realValue==null) {
                    assertTrue(expectedValue==null);
                }
                else {
                    assertTrue(realValue.equals(expectedValue));
                }
            }
        }
        assertTrue(!rs.next());
    }

}