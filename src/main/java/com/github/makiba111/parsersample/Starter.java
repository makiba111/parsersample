package com.github.makiba111.parsersample;

import java.io.IOException;
import java.util.List;

import com.github.makiba111.parsersample.dto.Data;

public class Starter {

	public static void main(String[] args) throws IOException {
		String rootPath = args[0];
		String outputPath = args[1];
		String databaseInstance = args[2];

		ExecJavaP.executeByRootPath(rootPath, outputPath);

		List<Data> list = JavaPparser.listFile(outputPath);
		DatabaseAccessor dba = new DatabaseAccessor(databaseInstance);
		dba.insertData(list);

		dba.search();
	}

}
