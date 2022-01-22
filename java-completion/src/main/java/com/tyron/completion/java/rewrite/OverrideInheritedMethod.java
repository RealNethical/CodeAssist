package com.tyron.completion.java.rewrite;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.google.common.base.Strings;
import com.tyron.completion.java.CompilerContainer;
import com.tyron.completion.java.CompilerProvider;
import com.tyron.completion.java.FindTypeDeclarationAt;
import com.tyron.completion.java.ParseTask;
import com.tyron.completion.java.util.ActionUtil;
import com.tyron.completion.java.util.JavaParserUtil;
import com.tyron.completion.model.Position;
import com.tyron.completion.model.Range;
import com.tyron.completion.model.TextEdit;
import com.tyron.completion.java.provider.FindHelper;

import org.openjdk.javax.lang.model.element.ExecutableElement;
import org.openjdk.javax.lang.model.element.TypeElement;
import org.openjdk.javax.lang.model.type.DeclaredType;
import org.openjdk.javax.lang.model.type.ExecutableType;
import org.openjdk.javax.lang.model.util.Types;
import org.openjdk.javax.tools.JavaFileObject;
import org.openjdk.source.tree.ClassTree;
import org.openjdk.source.tree.ImportTree;
import org.openjdk.source.tree.MethodTree;
import org.openjdk.source.tree.Tree;
import org.openjdk.source.util.SourcePositions;
import org.openjdk.source.util.TreePath;
import org.openjdk.source.util.Trees;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class OverrideInheritedMethod implements JavaRewrite {

    final String superClassName, methodName;
    final String[] erasedParameterTypes;
    final Path file;
    final int insertPosition;

    public OverrideInheritedMethod(String superClassName, String methodName,
                                   String[] erasedParameterTypes, Path file, int insertPosition) {
        this.superClassName = superClassName;
        this.methodName = methodName;
        this.erasedParameterTypes = erasedParameterTypes;
        this.file = file;
        this.insertPosition = insertPosition;
    }

    @Override
    public Map<Path, TextEdit[]> rewrite(CompilerProvider compiler) {

        List<TextEdit> edits = new ArrayList<>();
        Position insertPoint = insertNearCursor(compiler);

        CompilerContainer container = compiler.compile(file);
        return container.get(task -> {
            Types types = task.task.getTypes();
            Trees trees = Trees.instance(task.task);
            ExecutableElement superMethod = FindHelper.findMethod(task, superClassName,
                    methodName, erasedParameterTypes);
            if (superMethod == null) {
                return null;
            }

            ClassTree thisTree = new FindTypeDeclarationAt(task.task).scan(task.root(),
                    (long) insertPosition);
            TreePath thisPath = trees.getPath(task.root(), thisTree);
            TypeElement thisClass = (TypeElement) trees.getElement(thisPath);
            ExecutableType parameterizedType =
                    (ExecutableType) types.asMemberOf((DeclaredType) thisClass.asType(),
                            superMethod);
            int indent = EditHelper.indent(task.task, task.root(), thisTree);
            if (indent == 1) {
                indent = 4;
            }
            indent += 4;

            Set<String> importedClasses = new HashSet<>();
            Set<String> typesToImport = ActionUtil.getTypesToImport(parameterizedType);
            task.root().getImports().stream().map(ImportTree::getQualifiedIdentifier).map(Object::toString).forEach(importedClasses::add);

            Optional<JavaFileObject> sourceFile = compiler.findAnywhere(superClassName);
            MethodDeclaration methodDeclaration;
            if (sourceFile.isPresent()) {
                ParseTask parse = compiler.parse(sourceFile.get());
                MethodTree source = FindHelper.findMethod(parse, superClassName, methodName,
                        erasedParameterTypes);
                if (source == null) {
                    methodDeclaration = EditHelper.printMethod(superMethod, parameterizedType,
                            superMethod);
                } else {
                    methodDeclaration = EditHelper.printMethod(superMethod, parameterizedType,
                            source);
                }
            } else {
                methodDeclaration = EditHelper.printMethod(superMethod, parameterizedType,
                        superMethod);
            }
            int tabCount = indent / 4;

            String tabs = Strings.repeat("\t", tabCount);
            String text = JavaParserUtil.prettyPrint(methodDeclaration, className -> false);
            text = tabs + text.replace("\n", "\n" + tabs) + "\n\n";

            edits.add(new TextEdit(new Range(insertPoint, insertPoint), text));

            for (String s : typesToImport) {
                if (!ActionUtil.hasImport(task.root(), s)) {
                    JavaRewrite addImport = new AddImport(file.toFile(), s);
                    Map<Path, TextEdit[]> rewrite = addImport.rewrite(compiler);
                    TextEdit[] textEdits = rewrite.get(file);
                    if (textEdits != null) {
                        Collections.addAll(edits, textEdits);
                    }
                }
            }
            return Collections.singletonMap(file, edits.toArray(new TextEdit[0]));
        });
    }

    private Position insertNearCursor(CompilerProvider compiler) {
        ParseTask task = compiler.parse(file);
        ClassTree parent = new FindTypeDeclarationAt(task.task).scan(task.root,
                (long) insertPosition);
        Position next = nextMember(task, parent);
        if (next != Position.NONE) {
            return next;
        }
        return EditHelper.insertAtEndOfClass(task.task, task.root, parent);
    }

    private Position nextMember(ParseTask task, ClassTree parent) {
        SourcePositions pos = Trees.instance(task.task).getSourcePositions();
        if (parent != null) {
            for (Tree member : parent.getMembers()) {
                long start = pos.getStartPosition(task.root, member);
                if (start > insertPosition) {
                    int line = (int) task.root.getLineMap().getLineNumber(start);
                    return new Position(line - 1, 0);
                }
            }
        }
        return Position.NONE;
    }
}
