package com.github.makiba111.parsersample;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.makiba111.parsersample.dto.ClassStruct;
import com.github.makiba111.parsersample.dto.LineNumberTable;
import com.github.makiba111.parsersample.dto.MethodCallerObject;
import com.github.makiba111.parsersample.dto.MethodObject;

public class JavaPperser {
	private static final Pattern COMPILE_LINE = Pattern.compile("Compiled from \\\".+\\.java\"");
	private static final Pattern CLASS_LINE = Pattern.compile("(public )?(final )?class (.+)(extends )?.*\\{");
	private static final Pattern METHOD_LIKE_LINE = Pattern.compile("(public|protected|private )?(static )?(final )?(void|.+ )?(.+)\\((.*)?\\);");
	private static final Pattern CODE_LINE = Pattern.compile("Code:");
	private static final Pattern CODE_OPE_LINE = Pattern.compile("(\\d+): (invoke.+) (#[0-9]+)\\s+(.+)");
	private static final Pattern CALLER_LINE = Pattern.compile("// Method\\s+(.+):\\((.+)?\\)(.+)?");
	private static final Pattern LINENUMBER_LINE = Pattern.compile("LineNumberTable:");
	private static final Pattern LINENUMBER_OP_LINE = Pattern.compile("line\\s+(\\d+):\\s+(\\d+)");// line 33: 63
	private static final String INVOKE_SPECIAL = "invokespecial";


	public static void main(String[] args) throws IOException {
		String rootPath = args[0];
		listFile(rootPath);
	}

	public static void listFile(String rootPath) throws IOException {
		File p = new File(rootPath).getCanonicalFile();

		Files.walk(p.toPath()).filter(f -> f.toFile().isFile())
			.forEach(file -> {
				try {
					parse(file.toFile());
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
	}

	public static ClassStruct parse(File file) throws FileNotFoundException, IOException {
		ClassStruct clazz = new ClassStruct();

		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			MethodObject mObject = null;

			String line;
			while((line = reader.readLine()) != null) {
//				System.out.println(line);
				line = line.trim();

				if (COMPILE_LINE.matcher(line).matches()) {
//					System.out.println(true);

				} else if (CLASS_LINE.matcher(line).matches()) {
					Matcher m = CLASS_LINE.matcher(line);
					m.find();
					String className = m.group(3);
					clazz.setClassName(className);

System.out.println("* " + className);
				} else if (METHOD_LIKE_LINE.matcher(line).matches()) {

					Matcher m = METHOD_LIKE_LINE.matcher(line);
					m.find();

					mObject = new MethodObject();

					String retV = trim(m.group(4));
					String mname = trim(m.group(5));
					String mp = trim(m.group(6));

					mObject.setMethodReturn(retV);
					mObject.setMethodName(mname);
					mObject.setMethodParameters(mp);

System.out.println("++++:" + mObject);

				} else if(CODE_OPE_LINE.matcher(line).matches()) {
					Matcher m = CODE_OPE_LINE.matcher(line);
					m.find();

					String ll = m.group(1);
					String ap = trim(m.group(2));
					String op = m.group(3);
					String caller = trim(m.group(4));
//System.out.println("    " + ll + " :: " + ap + " :: " + op + " :: " + caller );

					Matcher callerMatcher = CALLER_LINE.matcher(caller);
					if(!ap.equals(INVOKE_SPECIAL) && callerMatcher.find()) {
						String callerMethod = trim(callerMatcher.group(1));
						String callerParameters = trim(callerMatcher.group(2));
						String callerReturn = trim(callerMatcher.group(3));

						MethodCallerObject callerObject = new MethodCallerObject();
						callerObject.setIndex(Integer.parseInt(ll));
						callerObject.setMethodName(callerMethod);
						callerObject.setMethodParameters(callerParameters);
						callerObject.setMethodReturn(callerReturn);

						mObject.getMethodCallerList().add(callerObject);

System.out.println("    @@@@ " + callerObject);
					}

				} else if(LINENUMBER_OP_LINE.matcher(line).matches()) {
					Matcher m = LINENUMBER_OP_LINE.matcher(line);
					m.find();

					String ll = m.group(1);
					String methodIndex = m.group(2);
System.out.println("        " + ll + " " + methodIndex);

					LineNumberTable obj = new LineNumberTable();
					obj.setIndex(Integer.parseInt(ll));
					obj.setIndex(Integer.parseInt(methodIndex));

					mObject.getLineNumberTableList().add(obj);
				}
			}

		}
		return clazz;
	}

	private static String trim(String a) { return a == null ?  "": a.trim(); }

}
