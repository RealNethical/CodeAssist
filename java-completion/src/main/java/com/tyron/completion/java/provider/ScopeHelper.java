package com.tyron.completion.java.provider;

import com.tyron.completion.java.CompileTask;

import org.openjdk.javax.lang.model.element.Element;
import org.openjdk.javax.lang.model.element.Modifier;
import org.openjdk.javax.lang.model.element.TypeElement;
import org.openjdk.javax.lang.model.type.DeclaredType;
import org.openjdk.javax.lang.model.util.Elements;
import org.openjdk.source.tree.Scope;
import org.openjdk.source.util.Trees;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class ScopeHelper {
    // TODO is this still necessary? Test speed. We could get rid of the extra static-imports step.
    public static List<Scope> fastScopes(Scope start) {
        List<Scope> scopes = new ArrayList<>();
        for (Scope s = start; s != null; s = s.getEnclosingScope()) {
            scopes.add(s);
        }
        // Scopes may be contained in an enclosing scope.
        // The outermost scope contains those elements available via "star import" declarations;
        // the scope within that contains the top level elements of the compilation unit, including any named
        // imports.
        // https://parent.docs.oracle.com/en/java/javase/11/docs/api/jdk.compiler/org.openjdk.source/tree/Scope.html
        return scopes.subList(0, scopes.size() - 2);
    }

    public static List<Element> scopeMembers(CompileTask task, Scope inner, Predicate<CharSequence> filter) {
        Trees trees = Trees.instance(task.task);
        Elements elements = task.task.getElements();
        boolean isStatic = false;
        List<Element> list = new ArrayList<>();
        for (Scope scope : fastScopes(inner)) {
            if (scope.getEnclosingMethod() != null) {
                isStatic = isStatic || scope.getEnclosingMethod().getModifiers().contains(Modifier.STATIC);
            }
            for (Element member : scope.getLocalElements()) {
                if (!filter.test(member.getSimpleName())) continue;
                if ("<init>".contentEquals(member.getSimpleName())) continue;
                if ("<clinit>".contentEquals(member.getSimpleName())) continue;
                if (isStatic && member.getSimpleName().contentEquals("this")) continue;
                if (isStatic && member.getSimpleName().contentEquals("super")) continue;
                list.add(member);
            }
            if (scope.getEnclosingClass() != null) {
                TypeElement typeElement = scope.getEnclosingClass();
                DeclaredType typeType = (DeclaredType) typeElement.asType();
                for (Element member : elements.getAllMembers(typeElement)) {
                    if (!filter.test(member.getSimpleName())) continue;
                    if (!trees.isAccessible(scope, member, typeType)) continue;
                    if (isStatic && !member.getModifiers().contains(Modifier.STATIC)) continue;
                    list.add(member);
                }
                isStatic = isStatic || typeElement.getModifiers().contains(Modifier.STATIC);
            }
        }
        return list;
    }


}
