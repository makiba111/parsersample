package com.github.makiba111.parsersample;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class ExecJavaP {
	private static final String JAVAP_PATH ;
	static {
		Properties p = new Properties();
		String path = null;
		try {
			p.load(Files.newBufferedReader(Paths.get("javap.properties"), StandardCharsets.UTF_8));
			path = p.getProperty("JAVAP");
		} catch (IOException e) {
			e.printStackTrace();
		}
		JAVAP_PATH = path;
		System.out.println("javap.exe path=" + JAVAP_PATH);
	}

	public static void main(String[] args) throws IOException {
		String rootPath = args[0];
		String outputPath = args[1];
		executeByRootPath(rootPath, outputPath);
	}

	public static void executeByRootPath(String rootPath, String outputPath) throws IOException {
		File p = new File(rootPath).getCanonicalFile();

		List<File> fileList = new ArrayList<File>();
		List<String> classFullNameList = new ArrayList<String>();
		System.out.println(p);
		Files.walk(p.toPath()).filter(f -> f.toFile().isFile() && f.toString().toLowerCase().endsWith(".class"))
			.forEach(file -> {
				fileList.add(file.toFile());
				String path = file.toUri().getPath().substring(p.toPath().toUri().getPath().length()).replace(".class", "").replaceAll("/", ".");
				classFullNameList.add(path);
				System.out.println(path);
			});

		classFullNameList.forEach(className -> {
			javapExecute(p, className, outputPath);
		});
	}

	private static void javapExecute(File root, String className, String outputPath){
		String[] command = {
				"cmd", "/c",
//				JAVAP_PATH, "-c", "-l", className,
//				JAVAP_PATH, "-c", "-p", "-l", className,
				JAVAP_PATH, "-c", "-private", "-v", className,
				">", new File(outputPath + className).getAbsolutePath()
		};

        Process process = null;
        try {
        	ProcessBuilder pb = new ProcessBuilder(command);
        	pb.redirectErrorStream(true);
        	pb.directory(root);// run dir.
        	process = pb.start();
        } catch (IOException e) {
//            e.printStackTrace();
        }
        try {
        	process.waitFor(); // プロセスの正常終了まで待機させる
        	InputStream is = process.getInputStream();	//標準出力
        	printInputStream(is);
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
	}

	public static void printInputStream(InputStream is) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		try {
			for (;;) {
				String line = br.readLine();
				if (line == null) break;
				System.out.println(line);
			}
		} finally {
			br.close();
		}
	}

}
