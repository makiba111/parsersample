package com.github.makiba111.parsersample.dto;

import java.util.ArrayList;
import java.util.List;

public class ClassStruct {

	public ClassStruct() {
		this.methodList = new ArrayList<>();
	}

	private String className;
	private List<MethodObject> methodList;

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public List<MethodObject> getMethodList() {
		return methodList;
	}

	public void setMethodList(List<MethodObject> methodList) {
		this.methodList = methodList;
	}

}
