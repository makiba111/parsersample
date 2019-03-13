package com.github.makiba111.parsersample.dto;

public class Data {

	private String className;
	private String methodStruct;
	private String methodCallerObject;
	private String methodCallerLineNo;
	private String methodLiteral;

	public String getClassName() {
		return className;
	}
	public void setClassName(String className) {
		this.className = className;
	}
	public String getMethodStruct() {
		return methodStruct;
	}
	public void setMethodStruct(String methodStruct) {
		this.methodStruct = methodStruct;
	}
	public String getMethodCallerObject() {
		return methodCallerObject;
	}
	public void setMethodCallerObject(String methodCallerObject) {
		this.methodCallerObject = methodCallerObject;
	}
	public String getMethodCallerLineNo() {
		return methodCallerLineNo;
	}
	public void setMethodCallerLineNo(String methodCallerLineNo) {
		this.methodCallerLineNo = methodCallerLineNo;
	}

	public String getMethodLiteral() {
		return methodLiteral;
	}
	public void setMethodLiteral(String methodLiteral) {
		this.methodLiteral = methodLiteral;
	}

	public String toString() {
		return className + " : " + methodStruct + " : " + methodCallerObject + " : " + methodCallerLineNo + " @ " + methodLiteral;
	}
}
