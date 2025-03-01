package com.tyron.completion.java.provider;

import com.tyron.completion.java.CompileTask;
import com.tyron.completion.java.ParseTask;

import org.openjdk.source.tree.ArrayTypeTree;
import org.openjdk.source.tree.ClassTree;
import org.openjdk.source.tree.CompilationUnitTree;
import org.openjdk.source.tree.IdentifierTree;
import org.openjdk.source.tree.MemberSelectTree;
import org.openjdk.source.tree.MethodTree;
import org.openjdk.source.tree.ParameterizedTypeTree;
import org.openjdk.source.tree.PrimitiveTypeTree;
import org.openjdk.source.tree.Tree;
import org.openjdk.source.tree.VariableTree;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openjdk.javax.lang.model.element.Element;
import org.openjdk.javax.lang.model.element.ElementKind;
import org.openjdk.javax.lang.model.element.ExecutableElement;
import org.openjdk.javax.lang.model.element.TypeElement;
import org.openjdk.javax.lang.model.type.TypeMirror;
import org.openjdk.javax.lang.model.util.Types;


/**
 * Convenience class for common tasks with completions
 */
public class FindHelper {

    public static String[] erasedParameterTypes(CompileTask task, ExecutableElement method) {
        Types types = task.task.getTypes();
        String[] erasedParameterTypes = new String[method.getParameters().size()];
        for (int i = 0; i < erasedParameterTypes.length; i++) {
            TypeMirror p = method.getParameters().get(i).asType();
            erasedParameterTypes[i] = types.erasure(p).toString();
        }
        return erasedParameterTypes;
    }

    public static String[] erasedParameterTypes(ParseTask task, ExecutableElement method) {
        Types types = task.task.getTypes();
        String[] erasedParameterTypes = new String[method.getParameters().size()];
        for (int i = 0; i < erasedParameterTypes.length; i++) {
            TypeMirror p = method.getParameters().get(i).asType();
            erasedParameterTypes[i] = types.erasure(p).toString();
        }
        return erasedParameterTypes;
    }

    public static MethodTree findMethod(
            ParseTask task, String className, String methodName, String[] erasedParameterTypes) {
        ClassTree classTree = findType(task, className);
        for (Tree member : classTree.getMembers()) {
            if (member.getKind() != Tree.Kind.METHOD) continue;
            MethodTree method = (MethodTree) member;
            if (!method.getName().contentEquals(methodName)) continue;
            if (!isSameMethodType(method, erasedParameterTypes)) continue;
            return method;
        }
        return null;
    }

    public static VariableTree findField(ParseTask task, String className, String memberName) {
        ClassTree classTree = findType(task, className);
        for (Tree member : classTree.getMembers()) {
            if (member.getKind() != Tree.Kind.VARIABLE) continue;
            VariableTree variable = (VariableTree) member;
            if (!variable.getName().contentEquals(memberName)) continue;
            return variable;
        }
        throw new RuntimeException("no variable");
    }

    public static ClassTree findType(ParseTask task, String className) {
        return new FindTypeDeclarationNamed().scan(task.root, className);
    }

    public static ExecutableElement findMethod(
            CompileTask task, String className, String methodName, String[] erasedParameterTypes) {
        TypeElement type = task.task.getElements().getTypeElement(className);
        for (Element member : type.getEnclosedElements()) {
            if (member.getKind() != ElementKind.METHOD) continue;
            ExecutableElement method = (ExecutableElement) member;
            if (isSameMethod(task, method, className, methodName, erasedParameterTypes)) {
                return method;
            }
        }
        return null;
    }

    private static boolean isSameMethod(
            CompileTask task,
            ExecutableElement method,
            String className,
            String methodName,
            String[] erasedParameterTypes) {
        Types types = task.task.getTypes();
        TypeElement parent = (TypeElement) method.getEnclosingElement();
        if (!parent.getQualifiedName().contentEquals(className)) return false;
        if (!method.getSimpleName().contentEquals(methodName)) return false;
        if (method.getParameters().size() != erasedParameterTypes.length) return false;
        for (int i = 0; i < erasedParameterTypes.length; i++) {
            TypeMirror erasure = types.erasure(method.getParameters().get(i).asType());
            boolean same = erasure.toString().equals(erasedParameterTypes[i]);
            if (!same) return false;
        }
        return true;
    }

    private static boolean isSameMethodType(MethodTree candidate, String[] erasedParameterTypes) {
        if (candidate.getParameters().size() != erasedParameterTypes.length) {
            return false;
        }
        for (int i = 0; i < candidate.getParameters().size(); i++) {
            if (!typeMatches(candidate.getParameters().get(i).getType(), erasedParameterTypes[i])) {
                return false;
            }
        }
        return true;
    }

    private static boolean typeMatches(Tree candidate, String erasedType) {
        if (candidate instanceof ParameterizedTypeTree) {
            ParameterizedTypeTree parameterized = (ParameterizedTypeTree) candidate;
            return typeMatches(parameterized.getType(), erasedType);
        }
        if (candidate instanceof PrimitiveTypeTree) {
            return candidate.toString().equals(erasedType);
        }
        if (candidate instanceof IdentifierTree) {
            String simpleName = candidate.toString();
            return erasedType.endsWith(simpleName);
        }
        if (candidate instanceof MemberSelectTree) {
            return candidate.toString().equals(erasedType);
        }
        if (candidate instanceof ArrayTypeTree) {
            ArrayTypeTree array = (ArrayTypeTree) candidate;
            if (!erasedType.endsWith("[]")) return false;
            String erasedElement = erasedType.substring(0, erasedType.length() - "[]".length());
            return typeMatches(array.getType(), erasedElement);
        }
        return true;
    }

//    public static Location location(CompileTask task, TreePath path) {
//        return location(task, path, "");
//    }
//
//    public static Location location(CompileTask task, TreePath path, CharSequence name) {
//        var lines = path.getCompilationUnit().getLineMap();
//        var pos = Trees.instance(task.task).getSourcePositions();
//        var start = (int) pos.getStartPosition(path.getCompilationUnit(), path.getLeaf());
//        var end = (int) pos.getEndPosition(path.getCompilationUnit(), path.getLeaf());
//        if (name.length() > 0) {
//            start = FindHelper.findNameIn(path.getCompilationUnit(), name, start, end);
//            end = start + name.length();
//        }
//        var startLine = (int) lines.getLineNumber(start);
//        var startColumn = (int) lines.getColumnNumber(start);
//        var startPos = new Position(startLine - 1, startColumn - 1);
//        var endLine = (int) lines.getLineNumber(end);
//        var endColumn = (int) lines.getColumnNumber(end);
//        var endPos = new Position(endLine - 1, endColumn - 1);
//        var range = new Range(startPos, endPos);
//        URI uri = path.getCompilationUnit().getSourceFile().toUri();
//        return new Location(uri, range);
//    }

    public static int findNameIn(CompilationUnitTree root, CharSequence name, int start, int end) {
        CharSequence contents;
        try {
            contents = root.getSourceFile().getCharContent(true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Matcher matcher = Pattern.compile("\\b" + name + "\\b").matcher(contents);
        matcher.region(start, end);
        if (matcher.find()) {
            return matcher.start();
        }
        return -1;
    }
}