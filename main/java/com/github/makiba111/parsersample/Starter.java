package com.github.makiba111.parsersample;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import com.github.makiba111.parsersample.dto.Data;

public class Starter {

	public static void main(String[] args) throws IOException, SQLException {
		String rootPath = args[0];
		String outputPath = args[1];
		String databaseInstance = args[2];

		ExecJavaP.executeByRootPath(rootPath, outputPath);

		List<Data> list = JavaPparser.parseAllByRootDir(outputPath);
		DatabaseAccessor dba = new DatabaseAccessor(databaseInstance);
		Connection conn = dba.dbInit();
		try {
			dba.insertData(conn, list);
			dba.updateExtra(false);
			dba.search(true);
		} finally {
			dba.dbClose(conn);
		}
	}

}
