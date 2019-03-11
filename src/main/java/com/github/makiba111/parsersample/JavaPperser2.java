package com.github.makiba111.parsersample;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.makiba111.parsersample.dto.ClassStruct;
import com.github.makiba111.parsersample.dto.Data;
import com.github.makiba111.parsersample.dto.LineNumberTable;
import com.github.makiba111.parsersample.dto.MethodCallerObject;
import com.github.makiba111.parsersample.dto.MethodObject;

public class JavaPperser {
//	private static final Pattern COMPILE_LINE = Pattern.compile("Compiled from \\\".+\\.java\"");
	private static final Pattern CLASS_LINE = Pattern.compile("(public\\s*)?(final\\s*)?class\\s+(.+)\\s*(extends.*)?");
	private static final Pattern METHOD_LIKE_LINE1 = Pattern.compile("^(public|protected|private)?\\s*(static)?\\s*(final)?\\s*(.+)\\((.+)?\\);");
	private static final Pattern METHOD_LIKE_LINE2 = Pattern.compile("^(public|protected|private)?\\s*(static)?\\s*(final)?\\s*(\\S+)[(](.*)[)];");
	private static final Pattern METHOD_LIKE_LINE3 = Pattern.compile("^(public|protected|private)?\\s*(static)?\\s*(final)?\\s*(\\S+)\\s+(\\S+)[(](.*)[)];");
//	private static final Pattern METHOD_LIKE_LINE = Pattern.compile("(public|protected|private)?\\s*(static)?\\s*(final)?\\s*(.*)\\s?(.+)[(](.+)?[)];");
//	private static final Pattern METHOD_LIKE_LINE = Pattern.compile("(?!#\\d+\\s+)(public|protected|private)?\\s*(static)?\\s*(final)?\\s*(.*);$");
//	private static final Pattern CODE_LINE = Pattern.compile("Code:");
	private static final Pattern CODE_OPE_LINE = Pattern.compile("(\\d+): (invoke.+) (#[0-9]+)\\s+(.+)");
	private static final Pattern CALLER_LINE = Pattern.compile("//\\s*Method\\s+(.+):\\((.+)?\\)(.+)?");
	private static final Pattern STRING_LINE = Pattern.compile("(\\d+):\\s*ldc\\s*(#\\d+)\\s*//\\s*String\\s(.*)");
//	private static final Pattern LINENUMBER_LINE = Pattern.compile("LineNumberTable:");
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
					System.out.println(file);
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

//				if (COMPILE_LINE.matcher(line).matches()) {
////					System.out.println(true);
//				} else
				if (CLASS_LINE.matcher(line).matches()) {
					Matcher m = CLASS_LINE.matcher(line);
					m.find();
					String className = m.group(3);
					clazz.setClassName(className);

//System.out.println("* " + className);
				} else if (METHOD_LIKE_LINE2.matcher(line).matches() ||  METHOD_LIKE_LINE3.matcher(line).matches()) {

					Matcher m2 = METHOD_LIKE_LINE2.matcher(line);
					Matcher m3 = METHOD_LIKE_LINE3.matcher(line);
//					boolean isBoth = false;
					boolean a = m2.find();
					boolean b = m3.find();
					boolean isBoth = a & b;
//					System.out.println(a + " " + b);

					if(mObject != null) {
						// set line no
						for(MethodCallerObject mca : mObject.getMethodCallerList()) {
							for(LineNumberTable lt : mObject.getLineNumberTableList()) {
								int index = lt.getIndex();
								if(index >= mca.getIndex()) {
//									System.out.println(mca.toString() + "  index::" + lt.getLine());
									mca.setLine(lt.getLine());
									break;
								}
							}
						}

						clazz.getMethodList().add(mObject);
					}

					mObject = new MethodObject();

					if(a && !b) {
						String retV = "";
						String mname = trim(m2.group(4));
						String mp = trim(m2.group(5));

						System.out.println("M2 -"+ retV + "- " + "'" + mname+"'  " + mp);

						mObject.setMethodReturn(retV);
						mObject.setMethodName(mname);
						mObject.setMethodParameters(mp);
					} else {
						String retV = trim(m3.group(4));
						String mname = trim(m3.group(5));
						String mp = trim(m3.group(6));

						System.out.println("M3 -"+ retV + "- " + "'" + mname+"'  " + mp);

						mObject.setMethodReturn(retV);
						mObject.setMethodName(mname);
						mObject.setMethodParameters(mp);
					}

//System.out.println("++++:" + mObject);
				} else if(STRING_LINE.matcher(line).matches()) {
					Matcher m = STRING_LINE.matcher(line);
					m.find();

					String val = m.group(3);
					String before = mObject.getInnerStringLiteral();
					mObject.setInnerStringLiteral(before != null ? before + " " + val : val);
//System.out.println("        ****=" + mObject);
//System.out.println("        ****String=" + mObject.getInnerStringLiteral());

				} else if(CODE_OPE_LINE.matcher(line).matches()) {
					Matcher m = CODE_OPE_LINE.matcher(line);
					m.find();

					String ll = m.group(1);
					String ap = trim(m.group(2));
					String op = m.group(3);
					String caller = trim(m.group(4));
//System.out.println("    " + ll + " :: " + ap + " :: " + op + " :: " + caller );

					Matcher callerMatcher = CALLER_LINE.matcher(caller);
					//if(!ap.equals(INVOKE_SPECIAL) && callerMatcher.find()) {
					if(callerMatcher.find()) {
						String callerMethod = trim(callerMatcher.group(1));
						String callerParameters = trim(callerMatcher.group(2));
						String callerReturn = trim(callerMatcher.group(3));

//						System.out.println(callerMethod);
//						System.out.println(callerParameters);
//						System.out.println(callerReturn);

						MethodCallerObject callerObject = new MethodCallerObject();
						callerObject.setIndex(Integer.parseInt(ll));
						callerObject.setMethodName(callerMethod.replaceAll("/", "."));
						callerObject.setMethodParameters(convertPlan(callerParameters));
						callerObject.setMethodReturn(convertPlan(callerReturn));

						mObject.getMethodCallerList().add(callerObject);

//System.out.println("    @@@@ " + callerObject);
					}


				} else if(LINENUMBER_OP_LINE.matcher(line).matches()) {
					Matcher m = LINENUMBER_OP_LINE.matcher(line);
					m.find();

					String ll = m.group(1);
					String methodIndex = m.group(2);
//System.out.println("        " + ll + " " + methodIndex);

					LineNumberTable obj = new LineNumberTable();
					obj.setLine(Integer.parseInt(ll));
					obj.setIndex(Integer.parseInt(methodIndex));

					mObject.getLineNumberTableList().add(obj);
				}
			}

			if(mObject != null) {
				// set line no
				for(MethodCallerObject mca : mObject.getMethodCallerList()) {
					for(LineNumberTable lt : mObject.getLineNumberTableList()) {
						int index = lt.getIndex();
						if(index >= mca.getIndex()) {
//							System.out.println(mca.toString() + "  index::" + lt.getLine());
							mca.setLine(lt.getLine());
							break;
						}
					}
				}
				clazz.getMethodList().add(mObject);
			}
		} finally {
			System.out.println("");
		}


		List<Data> list = new ArrayList<>();
		for (MethodObject methodObj : clazz.getMethodList()) {
			for (MethodCallerObject callerObj :methodObj.getMethodCallerList()) {
				Data d = new Data();
				d.setClassName(clazz.getClassName());
				d.setMethodStruct(methodObj.toString());
				d.setMethodCallerObject(callerObj.toString());
				d.setMethodCallerLineNo(String.valueOf(callerObj.getLine()));
				d.setMethodLiteral(methodObj.getInnerStringLiteral());

				System.out.println(d);
				list.add(d);
			}
		}


		return clazz;
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

	private static String trim(String a) { return a == null ?  "": a.trim(); }

}
