package com.tyron.completion.java.rewrite;

import org.openjdk.source.tree.Tree;
import org.openjdk.source.util.SourcePositions;
import org.openjdk.source.util.Trees;

import com.tyron.completion.java.CompilerProvider;
import com.tyron.completion.model.Position;
import com.tyron.completion.java.ParseTask;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.tyron.completion.model.Range;
import com.tyron.completion.model.TextEdit;

import org.openjdk.source.tree.ImportTree;

import java.io.File;

public class AddImport implements JavaRewrite {
    
    private final String className;
    private final File currentFile;
    private final boolean isStatic;

    public AddImport(File currentFile, String className) {
        this(currentFile, className, false);
    }
    
    public AddImport(File currentFile, String className, boolean isStatic) {
        this.className = className;
        this.currentFile = currentFile;
        this.isStatic = isStatic;
    }

    @Override
    public Map<Path, TextEdit[]> rewrite(CompilerProvider compiler) {
        ParseTask task = compiler.parse(currentFile.toPath());
        Position point = insertPosition(task);
        String text = "import " + className + ";\n";
        TextEdit[] edits = { new TextEdit(new Range(point, point), text)};
        return Collections.singletonMap(currentFile.toPath(), edits);
    }

    public Map<File, TextEdit> getText(ParseTask task) {
        Position point = insertPosition(task);

        String text = "import " + className + ";\n";
        if (point.line == 1) {
            text = "\nimport " + className + ";\n";
        }
        TextEdit edit = new TextEdit(new Range(point, point), text);
        return Collections.singletonMap(currentFile, edit);
    }
    
    private Position insertPosition(ParseTask task) {
        List<? extends ImportTree> imports = task.root.getImports();
        for (ImportTree i : imports) {
            String next = i.getQualifiedIdentifier().toString();
            if (className.compareTo(next) < 0) {
                return insertBefore(task, i);
            }
        }
        if (!imports.isEmpty()) {
            ImportTree last = imports.get(imports.size() - 1);
            return insertAfter(task, last);
        }
        if (task.root.getPackageName() != null) {
            return insertAfter(task, task.root.getPackageName());
        }
        return new Position(0, 0);
    }

    private Position insertBefore(ParseTask task, Tree i) {
        SourcePositions pos = Trees.instance(task.task).getSourcePositions();
        long offset = pos.getStartPosition(task.root, i);
        int line = (int) task.root.getLineMap().getLineNumber(offset);
        return new Position(line - 1, 0);
    }

    private Position insertAfter(ParseTask task, Tree i) {
        SourcePositions pos = Trees.instance(task.task).getSourcePositions();
        long offset = pos.getStartPosition(task.root, i);
        int line = (int) task.root.getLineMap().getLineNumber(offset);
        return new Position(line, 0);
    }
}
