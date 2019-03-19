package com.github.makiba111.parsersample.dto;

public class MethodCallerObject {

	public MethodCallerObject(){
	}

	private int line;
	private int index;
	private String methodName;
	private String methodParameters;
	private String methodReturn;
	private int duplicateMark;

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
	public int getDuplicateMark() {
		return duplicateMark;
	}
	public void setDuplicateMark(int duplicateMark) {
		this.duplicateMark = duplicateMark;
	}

	public String toString() {
		return methodReturn + " " + methodName + "(" + methodParameters + ")";
	}
}
