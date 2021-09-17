/*
 * reserved comment block
 * DO NOT REMOVE OR ALTER!
 */
/*
 * Copyright 2001-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * $Id: StringLengthCall.java,v 1.2.4.1 2005/09/05 09:08:46 pvedula Exp $
 */

package com.sun.apache.xalan.internal.xsltc.compiler;

import java.util.Vector;

import com.sun.apache.bcel.internal.generic.ConstantPoolGen;
import com.sun.apache.xalan.internal.xsltc.compiler.util.ClassGenerator;
import com.sun.apache.xalan.internal.xsltc.compiler.util.MethodGenerator;
import com.sun.apache.xalan.internal.xsltc.compiler.util.Type;
import com.sun.apache.bcel.internal.generic.INVOKEVIRTUAL;
import com.sun.apache.bcel.internal.generic.InstructionList;

/**
 * @author Jacek Ambroziak
 * @author Santiago Pericas-Geertsen
 */
final class StringLengthCall extends FunctionCall {
    public StringLengthCall(QName fname, Vector arguments) {
        super(fname, arguments);
    }

    public void translate(ClassGenerator classGen, MethodGenerator methodGen) {
        final ConstantPoolGen cpg = classGen.getConstantPool();
        final InstructionList il = methodGen.getInstructionList();
        if (argumentCount() > 0) {
            argument().translate(classGen, methodGen);
        }
        else {
            il.append(methodGen.loadContextNode());
            Type.Node.translateTo(classGen, methodGen, Type.String);
        }
        il.append(new INVOKEVIRTUAL(cpg.addMethodref(STRING_CLASS,
                                                     "length", "()I")));
    }
}
