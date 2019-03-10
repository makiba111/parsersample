package com.github.makiba111.parsersample.dto;

import java.util.ArrayList;
import java.util.List;

public class MethodObject {

	public MethodObject(){
		this.setMethodCallerList(new ArrayList<>());
		this.setLineNumberTableList(new ArrayList<>());
	}

	private int line;
	private String methodName;
	private String methodParameters;
	private String methodReturn;
	private List<MethodCallerObject> methodCallerList;
	private List<LineNumberTable> lineNumberTableList;


	public int getLine() {
		return line;
	}
	public void setLine(int line) {
		this.line = line;
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
	public List<MethodCallerObject> getMethodCallerList() {
		return methodCallerList;
	}
	public void setMethodCallerList(List<MethodCallerObject> methodCallerList) {
		this.methodCallerList = methodCallerList;
	}

	public List<LineNumberTable> getLineNumberTableList() {
		return lineNumberTableList;
	}
	public void setLineNumberTableList(List<LineNumberTable> lineNumberTableList) {
		this.lineNumberTableList = lineNumberTableList;
	}

	public String toString() {
		return methodReturn + " " + methodName + "(" + methodParameters + ")";
	}

}
