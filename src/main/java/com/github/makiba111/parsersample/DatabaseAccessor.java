package com.github.makiba111.parsersample;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Properties;

import com.github.makiba111.parsersample.dto.Data;

public class DatabaseAccessor {

	private static final String SEARCH_SQL ;
	static {
		Properties p = new Properties();
		String sql = null;
		try {
			p.load(Files.newBufferedReader(Paths.get("sql.properties"), StandardCharsets.UTF_8));
			sql = p.getProperty("SQL");
		} catch (IOException e) {
			e.printStackTrace();
		}
		SEARCH_SQL = sql;
		System.out.println("sql=" + sql);
	}

	private String instanceId;

    /**
     * pragma setting.
     *  - jornal_mode
     *  - sync_mode
     *  - case_sensitive_like
     */
    public static final Properties getProperties() {
        Properties prop = new Properties();
        prop.put("journal_mode", "MEMORY");
        prop.put("sync_mode", "OFF");
        prop.put("case_sensitive_like", "1");
        //prop.put("temp_store_directory", "'C:\temp\'");
        return prop;
    }

	public DatabaseAccessor(String instanceId) {
		this.instanceId = instanceId;
		try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

    public void search() {
        Statement stmt = null;
        String dbHeader = "jdbc:sqlite:" + instanceId;
        try (Connection conn = DriverManager.getConnection(dbHeader, getProperties())) { //try-with-resources
            conn.setAutoCommit(true);

            stmt = conn.createStatement();
            ResultSet result = stmt.executeQuery(SEARCH_SQL);

            while (result.next()) {
            	int depth = result.getInt("DEPTH");
//            	String parent_node_id = result.getString("PARENT_NODE_ID");
//            	String child_node_id = result.getString("CHILD_NODE_ID");
            	String full = result.getString("FULL_PATH");
            	String key = result.getString("AKEY");

            	System.out.println(depth + " - " + full + " " + key);
            }
//            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
        	try {
				stmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
        }
    }

    public void insertData(List<Data> list) {
        Statement stmt;
        String dbHeader = "jdbc:sqlite:" + instanceId;
        PreparedStatement pstmt;
        try (Connection conn = DriverManager.getConnection(dbHeader, getProperties())) { //try-with-resources
            conn.setAutoCommit(false);
            stmt = conn.createStatement();
            stmt.executeUpdate(
            		"CREATE TABLE IF NOT EXISTS java_node ("
            		+ " parent_node_id TEXT,"
            		+ " child_node_id TEXT,"
            		+ " child_node_line INTEGER,"
            		+ " parent_node_inner_literal TEXT,"
            		+ " parent_node_use_key TEXT,"
            		+ "PRIMARY KEY(parent_node_id, child_node_id))");

            pstmt = conn.prepareStatement("INSERT INTO java_node VALUES (?, ?, ?, ?, null)");
            for (Data d : list) {
                pstmt.setString(1, d.getMethodStruct());
                pstmt.setString(2, d.getMethodCallerObject());
                pstmt.setString(3, d.getMethodCallerLineNo());
                pstmt.setString(4, d.getMethodLiteral());
                pstmt.addBatch();
            }
            pstmt.executeBatch();

            conn.createStatement().executeUpdate("update java_node set parent_node_use_key='SELECT' where parent_node_inner_literal like '%SELECT%';");
            conn.createStatement().executeUpdate("update java_node set parent_node_use_key='SELECT' where parent_node_inner_literal like '%select%';");

            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
