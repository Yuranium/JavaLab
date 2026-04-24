package com.javalab.executionservice.util;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import com.javalab.executionservice.config.ExecutionConfig;
import com.javalab.executionservice.models.dto.ValidationResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ExecutionValidator
{
    private final ExecutionConfig config;

    private static final JavaParser PARSER;

    static
    {
        ReflectionTypeSolver typeSolver = new ReflectionTypeSolver();
        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(typeSolver);

        ParserConfiguration parserConfig = new ParserConfiguration()
                .setSymbolResolver(symbolSolver);

        PARSER = new JavaParser(parserConfig);
    }

    public ValidationResult validate(String code)
    {
        try
        {
            CompilationUnit unit = PARSER.parse(code)
                    .getResult()
                    .orElseThrow(RuntimeException::new);
            List<String> errors = new ArrayList<>();

            unit.getImports().stream()
                    .filter(e -> !isAllowedImport(e.getNameAsString()))
                    .forEach(importDeclaration ->
                            importDeclaration.getBegin()
                                    .ifPresent(position ->
                                            errors.add("Forbidden import at line %d: %s"
                                            .formatted(
                                                    position.line,
                                                    importDeclaration.getNameAsString()
                                            )))
                    );

            unit.findAll(MethodCallExpr.class)
                    .forEach(methodCall -> {
                        try
                        {
                            ResolvedMethodDeclaration declaration = methodCall.resolve();
                            String fullMethodName = declaration.getQualifiedName();

                            if (isForbiddenMethod(fullMethodName))
                                errors.add("Forbidden method at line %d: %s"
                                        .formatted(
                                                methodCall.getRange().map(r -> r.begin.line).orElse(-1),
                                                fullMethodName
                                        )
                                );
                        } catch (RuntimeException e) {}
                    });

            return new ValidationResult(!errors.isEmpty(), errors);
        } catch (Exception e)
        {
            return new ValidationResult(true, Collections.singletonList(e.getMessage()));
        }
    }

    private boolean isAllowedImport(String importName)
    {
        String basePackage = importName.substring(0, importName.lastIndexOf('.'));
        return config.getValidation().getAllowedPackages().contains(basePackage);
    }

    private boolean isForbiddenMethod(String methodName)
    {
        return config.getValidation().getForbiddenMethods().contains(methodName);
    }
}