package com.github.makiba111.parsersample.dto;

public class MethodCallerObject {

	public MethodCallerObject(){
	}

	private int line;
	private int index;
	private String methodName;
	private String methodParameters;
	private String methodReturn;
	private int duplidateMark;

	public int getLine() {
		return line;
	}
	public void setLine(int line) {
		this.line = line;
	}
	public int getIndex() {
		return index;
	}
	public void setIndex(int index) {
		this.index = index;
	}
	public String getMethodName() {
		return methodName;
	}
	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}
	public String getMethodParameters() {
		return methodParameters;
	}
	public void setMethodParameters(String methodParameters) {
		this.methodParameters = methodParameters;
	}
	public String getMethodReturn() {
		return methodReturn;
	}
	public void setMethodReturn(String methodReturn) {
		this.methodReturn = methodReturn;
	}
	public int getDuplidateMark() {
		return duplidateMark;
	}
	public void setDuplidateMark(int duplidateMark) {
		this.duplidateMark = duplidateMark;
	}

	public String toString() {
		return methodReturn + " " + methodName + "(" + methodParameters + ")";
	}
}
