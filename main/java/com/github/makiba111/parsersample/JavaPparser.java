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

import com.github.makiba111.parsersample.dto.ClassStruct;
import com.github.makiba111.parsersample.dto.Data;
import com.github.makiba111.parsersample.dto.LineNumberTable;
import com.github.makiba111.parsersample.dto.MethodCallerObject;
import com.github.makiba111.parsersample.dto.MethodObject;

public class JavaPparser {
	private static final Pattern STATIC_OP_LINE = Pattern.compile("^static [{][}];");
	private static final Pattern METHOD_LIKE_LINE = Pattern.compile("(.+[(]).*([)]).*;$");
	private static final Pattern NONE_METHOD = Pattern.compile("^\\d+[:].*");
	private static final Pattern CODE_OPE_LINE = Pattern.compile("^(\\d+)[:]\\s+(invoke\\S+)\\s+(#[0-9]+)\\s+(.+)");
	private static final Pattern CALLER_LINE = Pattern.compile("^(.+):[(](.+)?[)](.*)");
	private static final Pattern STRING_LINE = Pattern.compile("^(\\d+)[:]\\s*ldc\\s*(#\\d+)\\s*//\\s*String\\s(.*)");
	private static final Pattern LINENUMBER_OP_LINE = Pattern.compile("^line\\s+(\\d+):\\s+(\\d+)");

	private static final List<String> LOG;

	static {
		Properties p = new Properties();
		List<String> logList = null;
		try {
			p.load(Files.newBufferedReader(Paths.get("javapparser.properties"), StandardCharsets.UTF_8));
			String debug = p.getProperty("log.debug.tags", "exception");
			logList = Arrays.asList(debug.split(","));
		} catch (IOException e) {
			e.printStackTrace();
		}
		LOG = logList;
	}

	public static void main(String[] args) throws IOException {
		String rootPath = args[0];

		// long a = System.currentTimeMillis();
		// for(int i = 0; i < 500; i++) {
		// listFile(rootPath);
		// }
		// long b = System.currentTimeMillis();
		// System.out.println(((b-a)/1000d) + "秒");

		parseAllByRootDir(rootPath);
	}

	private static final String trim(String a) {
		return a == null ? "" : a.trim();
	}

	private static final void log(String tag, Object d) {
		if (LOG.contains(tag)) {
			System.out.println(d);
		}
	}

	public static List<Data> parseAllByRootDir(String rootPath) throws IOException {
		File p = new File(rootPath).getCanonicalFile();

		List<Data> list = new ArrayList<>();

		Files.walk(p.toPath()).filter(f -> f.toFile().isFile()).forEach(file -> {
			try {
				// log("D", file);
				list.addAll(parse(file.toFile()));
			} catch (IOException e) {
				e.printStackTrace();
			}
		});

		return list;
	}

	public static List<Data> parse(File file) throws FileNotFoundException, IOException {
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

			while ((line = reader.readLine()) != null) {
				log("file-line", line);
				line = line.trim();

				if (line.startsWith("descriptor:") && mObject != null) {
					//
					// method descriptor.
					//

					int last = line.indexOf(")");
					if (last >= 0) {
						String signature = line.substring(line.indexOf("(") + 1, last).trim();
						String prms = convertDescriptor(signature);
						String retValue = convertDescriptor(line.substring(line.indexOf(")") + 1).trim());

						mObject.setMethodReturn(retValue);
						mObject.setMethodParameters(trim(prms));
					} else {
						String signature = line.substring(line.indexOf(":") + 1).trim();
						mObject.setMethodReturn(convertDescriptor(signature));
					}
				}

				if (line.startsWith("#")
						|| line.startsWith("Classfile")
						|| line.startsWith("descriptor:")
						|| line.startsWith("Signature:")
						|| line.startsWith("flags:")
						|| line.startsWith("Code:")
						|| line.startsWith("LocalVariableTypeTable:")
						|| line.startsWith("stack")
						|| line.startsWith("StackMapTable:")) {
					continue;
				}

				//
				// Pattern Init.
				//
				staticOpLineMatcher = STATIC_OP_LINE.matcher(line);
				methodLineMatcher = METHOD_LIKE_LINE.matcher(line);
				stringLineMatcher = STRING_LINE.matcher(line);
				codeOpeLineMatcher = CODE_OPE_LINE.matcher(line);
				lineNumberOpLineMatcher = LINENUMBER_OP_LINE.matcher(line);
				if (staticOpLineMatcher.matches()) {
					//
					// static {} line.
					//

					addClassMethod(clazz, mObject);

					mObject = new MethodObject();
					mObject.setMethodReturn("void");
					mObject.setMethodName(clazz.getClassName() + ".static");
					mObject.setMethodParameters("");

				} else if (stringLineMatcher.matches()) {
					stringLineMatcher.find(0);
					//
					// String Literal
					//

					String val = stringLineMatcher.group(3);
					String before = mObject.getInnerStringLiteral();
					// mObject.setInnerStringLiteral(StringEscapeUtils.unescapeJava(before
					// != null ? before + " " + val : val));
					mObject.setInnerStringLiteral((before != null ? before + " " + val : val));

				} else if (codeOpeLineMatcher.matches()) {
					codeOpeLineMatcher.find(0);
					//
					// Code: outer line.
					//

					String ll = codeOpeLineMatcher.group(1);
					// String ap = trim(codeOpeLineMatcher.group(2));
					// String op = codeOpeLineMatcher.group(3);
					// String caller = trim(codeOpeLineMatcher.group(4));
					// log("data", " " + ll + " :: " + ap + " :: " + op + " :: "
					// + caller );

					String caller = line
							.substring(line.indexOf("Method") < 0 ? 0 : line.indexOf("Method") + "Method".length())
							.trim();
					Matcher callerMatcher = CALLER_LINE.matcher(caller);
					if (callerMatcher.find()) {
						String callerMethod = trim(callerMatcher.group(1));
						String callerParameters = trim(callerMatcher.group(2));
						String callerReturn = trim(callerMatcher.group(3));

						callerMethod = callerMethod.replaceAll("/", ".");
						if (callerMethod.indexOf(".") < 0) {
							callerMethod = clazz.getClassName() + "." + callerMethod;
						}

						MethodCallerObject callerObject = new MethodCallerObject();
						callerObject.setIndex(Integer.parseInt(ll));
						callerObject.setMethodName(callerMethod.replaceAll("/", "."));
						callerObject.setMethodParameters(convertDescriptor(callerParameters));
						callerObject.setMethodReturn(convertDescriptor(callerReturn));

						// same caller counter
						int callerCount = 0;
						for (MethodCallerObject obj : mObject.getMethodCallerList()) {
							if (obj.toString().equals(callerObject.toString())) {
								callerCount++;
							}
						}
						callerObject.setDuplicateMark(callerCount);

						mObject.getMethodCallerList().add(callerObject);

						log("method-caller", mObject);
					}

				} else if (lineNumberOpLineMatcher.matches()) {
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

				} else if (methodLineMatcher.matches()) {
					//
					// Method line.
					//

					if (NONE_METHOD.matcher(line).matches()) {
						continue;
					}
					methodLineMatcher.find(0);

					log("method", line);

					addClassMethod(clazz, mObject);

					mObject = new MethodObject();

					int ks = line.indexOf("(");
					String start = line.substring(0, ks);
					int mlast = start.lastIndexOf(" ");
					mlast = (mlast < 0) ? 0 : mlast;
					String methodName = start.substring(mlast).trim();
					if (methodName.indexOf(".") < 0) {
						// not found is Local-Method
						methodName = clazz.getClassName() + "." + methodName;
					}
					mObject.setMethodName(methodName);
				}
			}

			// end loop
			addClassMethod(clazz, mObject);
		}

		List<Data> list = new ArrayList<>();
		Data d = null;
		for (MethodObject methodObj : clazz.getMethodList()) {
			d = new Data();
			d.setClassName(clazz.getClassName());
			d.setMethodStruct(methodObj.toString());
			d.setMethodCallerObject("-");
			d.setMethodCallerLineNo("-");
			d.setMethodLiteral(methodObj.getInnerStringLiteral());
			d.setDuplicateMark("0");
			log("data", d);
			list.add(d);

			for (MethodCallerObject callerObj : methodObj.getMethodCallerList()) {
				d = new Data();
				d.setClassName(clazz.getClassName());
				d.setMethodStruct(methodObj.toString());
				d.setMethodCallerObject(callerObj.toString());
				d.setMethodCallerLineNo(String.valueOf(callerObj.getLine()));
				d.setMethodLiteral(methodObj.getInnerStringLiteral());
				d.setDuplicateMark(String.valueOf(callerObj.getDuplicateMark()));

				log("data", d);
				list.add(d);
			}
		}
		return list;
	}

	private static void addClassMethod(ClassStruct clazz, MethodObject mObject) {
		if (mObject != null) {
			// set line no
			List<MethodCallerObject> iList = mObject.getMethodCallerList();
			for (int i = 0, isize = iList.size(); i < isize; i++) {
				MethodCallerObject mca = iList.get(i);

				List<LineNumberTable> jList = mObject.getLineNumberTableList();
				for (int j = 0, jsize = jList.size(); j < jsize; j++) {
					LineNumberTable lt = jList.get(j);
					int index = lt.getIndex();
					if (index == mca.getIndex()) {
						// log("D", mca.toString() + " index::" + lt.getLine());
						mca.setLine(lt.getLine());
						break;
					} else if (index > mca.getIndex()) {
						// log("D", mca.toString() + " index::" + lt.getLine());
						if (j - 1 < 0) {
							mca.setLine(lt.getLine());
						} else {
							mca.setLine(jList.get(j - 1).getLine());
						}
						break;
					}
				}
			}

			// mObject.setLine(mObject.getLineNumberTableList().get(0).getLine());

			clazz.getMethodList().add(mObject);
		}
	}

	private static String convertDescriptor(String value) {
		// System.out.print(":::::: " + value);
		StringBuilder b = new StringBuilder();

		int arrayCounter = 0;
		for (int i = 0; i < value.length(); i++) {
			if (b.length() > 0 && arrayCounter == 0) {
				b.append(", ");
			}
			switch (value.charAt(i)) {
			case 'V':
				b.append("void");
				break;
			case 'C':
				b.append("char");
				break;
			case 'Z':
				b.append("boolean");
				break;
			case 'B':
				b.append("byte");
				break;
			case 'S':
				b.append("short");
				break;
			case 'I':
				b.append("int");
				break;
			case 'J':
				b.append("long");
				break;
			case 'F':
				b.append("float");
				break;
			case 'D':
				b.append("double");
				break;
			case '[':
				arrayCounter++;
				continue;
			case 'L':
				int last = value.indexOf(';', i);
				String obj = value.substring(i + 1, last).replaceAll("/", ".");
				b.append(obj);
				i = last;
				break;
			}

			for (int z = 0; z < arrayCounter; z++) {
				b.append("[]");
			}
			arrayCounter = 0;
		}
		// System.out.println(" ||| " +b);
		return b.toString();
	}
}
