package de.hpi.swa.trufflesqueak.nodes.primitives.impl;

import com.oracle.truffle.api.dsl.Specialization;

import de.hpi.swa.trufflesqueak.exceptions.PrimitiveFailed;
import de.hpi.swa.trufflesqueak.exceptions.UnwrappingError;
import de.hpi.swa.trufflesqueak.model.BaseSqueakObject;
import de.hpi.swa.trufflesqueak.model.CompiledMethodObject;
import de.hpi.swa.trufflesqueak.model.NativeObject;
import de.hpi.swa.trufflesqueak.nodes.primitives.PrimitiveQuinaryOperation;

public class PrimReplaceFromToNode extends PrimitiveQuinaryOperation {
    public PrimReplaceFromToNode(CompiledMethodObject cm) {
        super(cm);
    }

    @Specialization
    Object replace(NativeObject rcvr, int start, int stop, NativeObject repl, int replStart) {
        int repOff = replStart - start;
        for (int i = start - 1; i < stop; i++) {
            rcvr.setNativeAt0(i, repl.getNativeAt0(repOff + i));
        }
        return rcvr;
    }

    @Specialization
    Object replace(BaseSqueakObject rcvr, int start, int stop, BaseSqueakObject repl, int replStart) {
        int repOff = replStart - start;
        for (int i = start - 1; i < stop; i++) {
            try {
                rcvr.atput0(i, repl.at0(repOff + i));
            } catch (UnwrappingError e) {
                throw new PrimitiveFailed();
            }
        }
        return rcvr;
    }
}
