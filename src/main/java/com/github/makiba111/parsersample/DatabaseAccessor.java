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

	private static final String DB_TEMP_DIR;
	private static final String DDL_SQL;
	private static final String INSERT_SQL;
	private static final String UPDATE_SQL;
	private static final String SEARCH_SQL;

	static {
		Properties p = new Properties();
		String dbTemp = null;
		String ddl = null;
		String insert = null;
		String update = null;
		String search = null;

		try {
			p.load(Files.newBufferedReader(Paths.get("sql.properties"), StandardCharsets.UTF_8));
			dbTemp = p.getProperty("TEMP.DIR");
			ddl = p.getProperty("SQL.DDL");
			ddl = p.getProperty("SQL.DDL");
			insert = p.getProperty("SQL.INSERT");
			update = p.getProperty("SQL.UPDATE");
			search = p.getProperty("SQL.SEARCH");
		} catch (IOException e) {
			e.printStackTrace();
		}
		DB_TEMP_DIR = dbTemp;
		DDL_SQL = ddl;
		INSERT_SQL = insert;
		UPDATE_SQL = update;
		SEARCH_SQL = search;
	}

	private String instanceId;

	/**
	 * pragma setting.
	 *   - jornal_mode
	 *   - sync_mode
	 *   - case_sensitive_like
	 */
	public static final Properties getProperties(boolean isCaseSensitive) {
		Properties prop = new Properties();
		prop.put("journal_mode", "MEMORY");
		prop.put("sync_mode", "OFF");
		if (isCaseSensitive) {
			prop.put("case_sensitive_like", "1");
		}
		if (DB_TEMP_DIR != null) {
			prop.put("temp_store_directory", "'" + DB_TEMP_DIR + "'");
		}
		return prop;
	}

	private String getConnectString() {
		return "jdbc:sqlite:" + instanceId;
	}

	public DatabaseAccessor(String instanceId) {
		this.instanceId = instanceId;
		try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	public Connection dbInit() throws SQLException {
		Connection conn = DriverManager.getConnection(this.getConnectString(), getProperties(true));
		conn.setAutoCommit(false);
		Statement stmt = conn.createStatement();
		stmt.executeUpdate(DDL_SQL);
		stmt.close();
		conn.commit();
		return conn;
	}

	public void dbClose(Connection conn) {
		try {
			conn.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public void insertData(Connection conn, List<Data> list) {
		PreparedStatement pstmt;
		try {
			pstmt = conn.prepareStatement(INSERT_SQL);
			for (Data d : list) {
				pstmt.setString(1, d.getMethodStruct());
				pstmt.setString(2, d.getMethodCallerObject());
				pstmt.setString(3, d.getMethodCallerLineNo());
				pstmt.setString(4, d.getDuplicateMark());
				pstmt.setString(5, d.getMethodLiteral());
				pstmt.addBatch();
			}
			pstmt.executeBatch();
			conn.commit();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void updateExtra(boolean isCaseSensitive) throws SQLException {
		Statement stmt = null;
		try (Connection conn = DriverManager.getConnection(this.getConnectString(), getProperties(isCaseSensitive))) {
			conn.setAutoCommit(true);
			String[] sqls = UPDATE_SQL.split(";");
			stmt = conn.createStatement();
			for (String sql : sqls) {
				int count = stmt.executeUpdate(sql);
				System.out.println(count + " row updates. " + sql);
			}
			//conn.commit();
		} finally {
			if (stmt != null) {
				stmt.close();
			}
		}
	}

	public void search(boolean isCaseSensitive) {
		Statement stmt = null;
		try (Connection conn = DriverManager.getConnection(this.getConnectString(), getProperties(isCaseSensitive))) {
			conn.setAutoCommit(true);

			stmt = conn.createStatement();
			ResultSet result = stmt.executeQuery(SEARCH_SQL);

			while (result.next()) {
				int depth = result.getInt("DEPTH");
				// String parent_node_id = result.getString("PARENT_NODE_ID");
				// String child_node_id = result.getString("CHILD_NODE_ID");
				String full = result.getString("FULL_PATH");
				String key = result.getString("AKEY");

				System.out.println(depth + " - " + full + " " + key);
			}
			// conn.commit();
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

}
