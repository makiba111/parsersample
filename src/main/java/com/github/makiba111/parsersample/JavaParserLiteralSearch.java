package com.github.makiba111.parsersample;

import java.io.File;
import java.io.IOException;

import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import com.github.javaparser.utils.ParserCollectionStrategy;
import com.github.javaparser.utils.ProjectRoot;
import com.github.javaparser.utils.StringEscapeUtils;

public class JavaParserLiteralSearch {
	public static void main(String[] args) throws IOException {
		String rootPath = args[0];;

		ParserConfiguration config = new ParserConfiguration();
		CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver();
		combinedTypeSolver.add(new JavaParserTypeSolver(rootPath));
		combinedTypeSolver.add(new ReflectionTypeSolver());

		config.setSymbolResolver(new JavaSymbolSolver(combinedTypeSolver));//new JavaSymbolSolver(new JavaParserTypeSolver(rootPath)));
		ParserCollectionStrategy strategy = new ParserCollectionStrategy(config);
		ProjectRoot projectRoot = strategy.collect(new File(rootPath).toPath());

		projectRoot.getSourceRoots().stream().forEach(p -> {
			try {
				p.tryToParse().forEach((cu) -> {
					cu.getResult().get().findAll(ClassOrInterfaceDeclaration.class).stream()
				    //.filter(f -> f.isPublic() && !f.isStatic())
				    .forEach(f -> {
//				    	f.resolve().;
				    	f.findAll(MethodDeclaration.class).stream()
				    	.forEach(method -> {

				    		method.findAll(StringLiteralExpr.class).stream()
				    		.forEach(str -> {
				    			System.out.println("String::" + StringEscapeUtils.unescapeJava(str.toString()));
				    		});

				    		System.out.println(method.resolve().getQualifiedName());

//				    		System.out.println(method.getType().getElementType());
				    		System.out.println(method.getParameters());
				    		for (Parameter param : method.getParameters()) {
				    			System.out.println("  param:" + param.resolve().getType().describe());

				    		}
				    	});

//				    	System.out.println(f.getNameAsString());
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
