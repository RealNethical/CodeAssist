package com.tyron.actions;

import android.app.Activity;
import android.content.Context;

import androidx.fragment.app.Fragment;

import com.tyron.builder.project.Project;
import com.tyron.editor.Editor;

import org.jetbrains.kotlin.com.intellij.openapi.util.Key;
import org.openjdk.javax.tools.Diagnostic;

import java.io.File;

public class CommonDataKeys {

    /**
     * The current file opened in the editor
     */
    public static final Key<File> FILE = Key.create("file");

    public static final Key<Activity> ACTIVITY = Key.create("activity");
    /**
     * The current accessible context
     */
    public static final Key<Context> CONTEXT = Key.create("context");

    /**
     * The current fragment this action is invoked on
     */
    public static final Key<Fragment> FRAGMENT = Key.create("fragment");

    public static final Key<Diagnostic<?>> DIAGNOSTIC = Key.create("diagnostic");

    /**
     * The current opened project
     */
    public static final Key<Project> PROJECT = Key.create("project");

    public static final Key<Editor> EDITOR = Key.create("editor");
}
