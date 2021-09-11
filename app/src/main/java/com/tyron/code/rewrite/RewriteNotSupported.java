package com.tyron.code.rewrite;

import com.tyron.code.completion.CompilerProvider;
import com.tyron.code.model.TextEdit;

import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;

public class RewriteNotSupported implements Rewrite {
    @Override
    public Map<Path, TextEdit[]> rewrite(CompilerProvider compiler) {
        return Collections.emptyMap();
    }
}