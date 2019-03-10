package com.github.makiba111.parsersample;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.resolution.SymbolResolver;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.utils.ParserCollectionStrategy;
import com.github.javaparser.utils.ProjectRoot;

public class JavaParserLiteralSearch {
	private static final String JAVAP_PATH = "C:/Program Files/Java/jdk1.8.0_102/bin/javap.exe";

	public static void main(String[] args) throws IOException {
		String rootPath = "../PluginSample/src";
		ParserCollectionStrategy strategy = new ParserCollectionStrategy();
		ProjectRoot projectRoot = strategy.collect(new File(rootPath).toPath());

		projectRoot.getSourceRoots().stream().forEach(p -> {
			try {
				p.tryToParse().forEach((cu) -> {
//System.out.println(cu);s
					cu.getResult().get().findAll(ClassOrInterfaceDeclaration.class).stream()
				    //.filter(f -> f.isPublic() && !f.isStatic())
				    .forEach(f -> {
//				    	f.resolve().;
				    	System.out.println(f.getNameAsString());
				    });

				});
			} catch (IOException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			}
		});
//		Optional<ClassOrInterfaceDeclaration> classA = compilationUnit.getClassByName("A");
//		compilationUnit.findAll(FieldDeclaration.class).stream()
//        .filter(f -> f.isPublic() && !f.isStatic())
//        .forEach(f -> System.out.println("Check field at line " +
//            f.getRange().map(r -> r.begin.line).orElse(-1)));
	}

}
