package com.github.makiba111.parsersample;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.javaparser.utils.StringEscapeUtils;
import com.github.makiba111.parsersample.dto.ClassStruct;
import com.github.makiba111.parsersample.dto.Data;
import com.github.makiba111.parsersample.dto.LineNumberTable;
import com.github.makiba111.parsersample.dto.MethodCallerObject;
import com.github.makiba111.parsersample.dto.MethodObject;

public class JavaPparser {
//	private static final Pattern COMPILE_LINE = Pattern.compile("Compiled from \\\".+\\.java\"");
//	private static final Pattern CLASS_LINE = Pattern.compile("(public\\s*)?(final\\s*)?class\\s+(.+)\\s*(extends.*)?");
//	private static final Pattern METHOD_LIKE_LINE1 = Pattern.compile("^(public|protected|private)?\\s*(static)?\\s*(final)?\\s*(.+)\\((.+)?\\);");
//	private static final Pattern METHOD_LIKE_LINE2 = Pattern.compile("^(public|protected|private)?\\s*(static)?\\s*(final)?\\s*(\\S+)[(](.*)[)];");
//	private static final Pattern METHOD_LIKE_LINE3 = Pattern.compile("^(public|protected|private)?\\s*(static)?\\s*(final)?\\s*(\\S+)?\\s+(\\S+)[(](.*)[)](\\s+throws\\s+\\S+)?;");
//	private static final Pattern METHOD_LIKE_LINE = Pattern.compile("((public|protected|private)?\\s*(static)?\\s*(final)?\\s*?)(\\S*)\\s+(\\S+?)\\((.*\\))(\\s+throws\\s+\\S+)?;");
	private static final Pattern STATIC_OP_LINE = Pattern.compile("static [{][}];");
	private static final Pattern METHOD_LIKE_LINE = Pattern.compile("(.+[(]).*([)]).*;$");
	private static final Pattern METHOD_STRUCT = Pattern.compile("(public|protected|private)?\\s*(static)?\\s*(final)?\\s*(\\S*)(\\s*\\S*)");
	private static final Pattern NONE_METHOD = Pattern.compile("\\d+:.*");
//	private static final Pattern METHOD_LIKE_LINE = Pattern.compile("(public|protected|private)?\\s*(static)?\\s*(final)?\\s*(.*)\\s?(.+)[(](.+)?[)];");
//	private static final Pattern METHOD_LIKE_LINE = Pattern.compile("(?!#\\d+\\s+)(public|protected|private)?\\s*(static)?\\s*(final)?\\s*(.*);$");
//	private static final Pattern CODE_LINE = Pattern.compile("Code:");
	private static final Pattern CODE_OPE_LINE = Pattern.compile("(\\d+): (invoke.+) (#[0-9]+)\\s+(.+)");
	private static final Pattern CALLER_LINE = Pattern.compile("//\\s*Method\\s+(.+):\\((.+)?\\)(.+)?");
	private static final Pattern STRING_LINE = Pattern.compile("(\\d+):\\s*ldc\\s*(#\\d+)\\s*//\\s*String\\s(.*)");
//	private static final Pattern LINENUMBER_LINE = Pattern.compile("LineNumberTable:");
	private static final Pattern LINENUMBER_OP_LINE = Pattern.compile("line\\s+(\\d+):\\s+(\\d+)");// line 33: 63
//	private static final String INVOKE_SPECIAL = "invokespecial";

	private static List<String> LOG;

	static {
		Properties p = new Properties();
		try {
			p.load(Files.newBufferedReader(Paths.get("javapparser.properties"), StandardCharsets.UTF_8));
			String debug = p.getProperty("log.debug.tags", "exception");
			LOG = Arrays.asList(debug.split(","));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws IOException {
		String rootPath = args[0];

//		long a = System.currentTimeMillis();
//		for(int i = 0; i < 500; i++) {
//			listFile(rootPath);
//		}
//		long b = System.currentTimeMillis();
//		System.out.println(((b-a)/1000d) + "秒");

		listFile(rootPath);
	}

	private static String trim(String a) { return a == null ?  "": a.trim(); }

	private static final void log(String tag, Object d) {
		if(LOG.contains(tag)) {
			System.out.println(d);
		}
	}


	public static void listFile(String rootPath) throws IOException {
		File p = new File(rootPath).getCanonicalFile();

		Files.walk(p.toPath()).filter(f -> f.toFile().isFile())
			.forEach(file -> {
				try {
//					log("D", file);
					parse(file.toFile());
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
	}

	public static ClassStruct parse(File file) throws FileNotFoundException, IOException {
		ClassStruct clazz = new ClassStruct();
		clazz.setClassName(file.getName());
		log("class", "* " + clazz.getClassName());

		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			MethodObject mObject = null;

			String line;
			Matcher methodLineMatcher;
			Matcher staticOpLineMatcher;
			Matcher stringLineMatcher;
			Matcher codeOpeLineMatcher;
			Matcher lineNumberOpLineMatcher;

			while((line = reader.readLine()) != null) {
				log("file-line", line);
				line = line.trim();

				//
				// Pattern Init.
				//
				methodLineMatcher = METHOD_LIKE_LINE.matcher(line);
				staticOpLineMatcher = STATIC_OP_LINE.matcher(line);
				stringLineMatcher = STRING_LINE.matcher(line);
				codeOpeLineMatcher = CODE_OPE_LINE.matcher(line);
				lineNumberOpLineMatcher = LINENUMBER_OP_LINE.matcher(line);
				if (staticOpLineMatcher.matches()) {
					staticOpLineMatcher.find(0);
					//
					// static {} line.
					//

					addClassMethod(clazz, mObject);

					mObject = new MethodObject();
					mObject.setMethodReturn("void");
					mObject.setMethodName(clazz.getClassName() + ".static");
					mObject.setMethodParameters("");

				} else if (methodLineMatcher.matches()) {
					//
					// Method line.
					//

					if(line.startsWith("#") || line.startsWith("descriptor:") || NONE_METHOD.matcher(line).matches()){
						continue;
					}
					methodLineMatcher.find(0);

					log("method", line);

					addClassMethod(clazz, mObject);

					mObject = new MethodObject();

					int ks = line.indexOf("(");
					String start = line.substring(0, ks);
					Matcher mstruct = METHOD_STRUCT.matcher(start);
					boolean isFind = mstruct.find(0);
					if(isFind) {
						String retValue = trim(mstruct.group(4));
						String methodName = trim(mstruct.group(5));
						if(methodName.isEmpty()) {
							// constructor
							methodName = retValue + ".\"<init>\"";
							retValue = "void";
						} else if(methodName.indexOf(".") < 0) {
							// not found is Local-Method
							methodName = clazz.getClassName() + "." + methodName;
						}

						mObject.setMethodReturn(retValue);
						mObject.setMethodName(methodName);

						String prms = line.substring(ks+1, line.indexOf(")"));
						mObject.setMethodParameters(trim(prms));

					} else {
						log("exception", clazz.getClassName() + " UnMatch Line: " + line);
					}

				} else if(stringLineMatcher.matches()) {
					stringLineMatcher.find(0);
					//
					// String Literal
					//

					String val = stringLineMatcher.group(3);
					String before = mObject.getInnerStringLiteral();
//					mObject.setInnerStringLiteral(StringEscapeUtils.unescapeJava(before != null ? before + " " + val : val));
					mObject.setInnerStringLiteral((before != null ? before + " " + val : val));

				} else if(codeOpeLineMatcher.matches()) {
					codeOpeLineMatcher.find(0);
					//
					// Code:  outer line.
					//

					String ll = codeOpeLineMatcher.group(1);
//					String ap = trim(codeOpeLineMatcher.group(2));
//					String op = codeOpeLineMatcher.group(3);
					String caller = trim(codeOpeLineMatcher.group(4));
//log("D", "    " + ll + " :: " + ap + " :: " + op + " :: " + caller );

					Matcher callerMatcher = CALLER_LINE.matcher(caller);
					if(callerMatcher.find()) {
						String callerMethod = trim(callerMatcher.group(1));
						String callerParameters = trim(callerMatcher.group(2));
						String callerReturn = trim(callerMatcher.group(3));

						callerMethod = callerMethod.replaceAll("/", ".");
						if(callerMethod.indexOf(".") < 0) {
							callerMethod = clazz.getClassName() + "." + callerMethod;
						}

						MethodCallerObject callerObject = new MethodCallerObject();
						callerObject.setIndex(Integer.parseInt(ll));
						callerObject.setMethodName(callerMethod.replaceAll("/", "."));
						callerObject.setMethodParameters(convertPlan(callerParameters));
						callerObject.setMethodReturn(convertPlan(callerReturn));

						mObject.getMethodCallerList().add(callerObject);

						log("method-caller", mObject);
					}

				} else if(lineNumberOpLineMatcher.matches()) {
					lineNumberOpLineMatcher.find(0);
					//
					// LineNumberTable: outer line.
					//

					String ll = lineNumberOpLineMatcher.group(1);
					String methodIndex = lineNumberOpLineMatcher.group(2);

					LineNumberTable obj = new LineNumberTable();
					obj.setLine(Integer.parseInt(ll));
					obj.setIndex(Integer.parseInt(methodIndex));

					mObject.getLineNumberTableList().add(obj);

					log("line-number", obj);
				}
			}

			// end loop
			addClassMethod(clazz, mObject);
		}

		List<Data> list = new ArrayList<>();
		for (MethodObject methodObj : clazz.getMethodList()) {
			Data d = new Data();
			d.setClassName(clazz.getClassName());
			d.setMethodStruct(methodObj.toString());
			d.setMethodCallerObject("-");
			d.setMethodCallerLineNo("-");
			d.setMethodLiteral(methodObj.getInnerStringLiteral());

			log("data", d);
			list.add(d);

			for (MethodCallerObject callerObj :methodObj.getMethodCallerList()) {
				d = new Data();
				d.setClassName(clazz.getClassName());
				d.setMethodStruct(methodObj.toString());
				d.setMethodCallerObject(callerObj.toString());
				d.setMethodCallerLineNo(String.valueOf(callerObj.getLine()));
				d.setMethodLiteral("");

				log("data", d);
				list.add(d);
			}
		}
		return clazz;
	}

	private static void addClassMethod(ClassStruct clazz, MethodObject mObject) {
		if(mObject != null) {
			// set line no
			List<MethodCallerObject> iList = mObject.getMethodCallerList();
			for(int i = 0, isize = iList.size(); i < isize; i++) {
				MethodCallerObject mca = iList.get(i);

				List<LineNumberTable> jList = mObject.getLineNumberTableList();
				for(int j = 0, jsize = jList.size(); j < jsize; j++) {
					LineNumberTable lt = jList.get(j);
					int index = lt.getIndex();
					if(index == mca.getIndex()) {
//						log("D", mca.toString() + "  index::" + lt.getLine());
						mca.setLine(lt.getLine());
						break;
					} else if(index > mca.getIndex()) {
//						log("D", mca.toString() + "  index::" + lt.getLine());
						if(j - 1 < 0) {
							mca.setLine(lt.getLine());
						} else {
							mca.setLine(jList.get(j-1).getLine());
						}
						break;
					}
				}
			}

			clazz.getMethodList().add(mObject);
		}
	}

	private static String convertPlan(String value) {
		StringBuilder b = new StringBuilder();
		for(int i = 0; i < value.length(); i++) {
			if(b.length() > 0) {
				b.append(", ");
			}
			switch(value.charAt(i)) {
			case 'V' :
				b.append("void");
				break;
			case 'C' :
				b.append("char");
				break;
			case 'Z' :
				b.append("boolean");
				break;
			case 'B' :
				b.append("byte");
				break;
			case 'S' :
				b.append("short");
				break;
			case 'I' :
				b.append("int");
				break;
			case 'J' :
				b.append("long");
				break;
			case 'D' :
				b.append("double");
				break;
			case 'L' :
				int last = value.indexOf(';', i);
				String obj = value.substring(i+1, last).replaceAll("/", ".");
				b.append(obj);
				i = last;
				break;
			}
		}
		return b.toString();
	}
}
