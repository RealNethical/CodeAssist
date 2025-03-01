package com.tyron.completion.java;


import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.openjdk.javax.tools.JavaFileObject;

public interface CompilerProvider {

    boolean isReady();

    Set<String> imports();
    
    List<String> publicTopLevelTypes();

    List<String> packagePrivateTopLevelTypes(String packageName);

    Iterable<Path> search(String query);

    Optional<JavaFileObject> findAnywhere(String className);

    Path findTypeDeclaration(String className);

    Path[] findTypeReferences(String className);

    Path[] findMemberReferences(String className, String memberName);

    ParseTask parse(Path file);

    ParseTask parse(JavaFileObject file);

    CompilerContainer compile(Path... files);

    CompilerContainer compile(Collection<? extends JavaFileObject> sources);

    Path NOT_FOUND = Paths.get("");
}
