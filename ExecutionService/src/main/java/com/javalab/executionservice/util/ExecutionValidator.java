package com.javalab.executionservice.util;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
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

    public ValidationResult validate(String code)
    {
        try
        {
            JavaParser javaParser = new JavaParser();
            CompilationUnit unit = javaParser.parse(code)
                    .getResult()
                    .orElseThrow(RuntimeException::new);
            List<String> errors = new ArrayList<>();

            for (ImportDeclaration importDeclaration : unit.getImports())
            {
                if (!isAllowedImport(importDeclaration.getNameAsString()))
                    errors.add("Forbidden import at line %d: %s"
                            .formatted(
                                    importDeclaration.getBegin().get().line,
                                    importDeclaration.getNameAsString()
                            )
                    );
            }

            unit.findAll(MethodCallExpr.class)
                    .forEach(methodCall -> {
                        ResolvedMethodDeclaration declaration = methodCall.resolve();
                        String fullMethodName = declaration.getQualifiedName();
                        if (isForbiddenMethod(fullMethodName))
                            errors.add("Forbidden method at line %d: %s"
                                    .formatted(methodCall.getRange()
                                                    .map(r -> r.begin.line)
                                                    .orElse(-1),
                                            fullMethodName
                                    ));
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