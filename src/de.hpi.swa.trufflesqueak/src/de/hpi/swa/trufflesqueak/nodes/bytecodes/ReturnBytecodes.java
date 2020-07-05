/*
 * Copyright (c) 2017-2020 Software Architecture Group, Hasso Plattner Institute
 *
 * Licensed under the MIT License.
 */
package de.hpi.swa.trufflesqueak.nodes.bytecodes;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;

import de.hpi.swa.trufflesqueak.exceptions.Returns.NonLocalReturn;
import de.hpi.swa.trufflesqueak.exceptions.SqueakExceptions.SqueakException;
import de.hpi.swa.trufflesqueak.image.SqueakImageContext;
import de.hpi.swa.trufflesqueak.model.CompiledCodeObject;
import de.hpi.swa.trufflesqueak.model.ContextObject;
import de.hpi.swa.trufflesqueak.model.NilObject;
import de.hpi.swa.trufflesqueak.nodes.bytecodes.ReturnBytecodesFactory.BlockReturnTopNodeGen;
import de.hpi.swa.trufflesqueak.nodes.bytecodes.ReturnBytecodesFactory.ReturnConstantFromBlockNodeGen;
import de.hpi.swa.trufflesqueak.nodes.bytecodes.ReturnBytecodesFactory.ReturnConstantFromMethodNodeGen;
import de.hpi.swa.trufflesqueak.nodes.bytecodes.ReturnBytecodesFactory.ReturnReceiverFromBlockNodeGen;
import de.hpi.swa.trufflesqueak.nodes.bytecodes.ReturnBytecodesFactory.ReturnReceiverFromMethodNodeGen;
import de.hpi.swa.trufflesqueak.nodes.bytecodes.ReturnBytecodesFactory.ReturnTopFromBlockNodeGen;
import de.hpi.swa.trufflesqueak.nodes.bytecodes.ReturnBytecodesFactory.ReturnTopFromMethodNodeGen;
import de.hpi.swa.trufflesqueak.nodes.context.frame.FrameStackPopNode;
import de.hpi.swa.trufflesqueak.nodes.context.frame.GetOrCreateContextNode;
import de.hpi.swa.trufflesqueak.nodes.process.GetActiveProcessNode;
import de.hpi.swa.trufflesqueak.util.FrameAccess;

public final class ReturnBytecodes {

    public static BlockReturnTopNode createBlockReturnNode(final CompiledCodeObject code, final int index) {
        return BlockReturnTopNodeGen.create(code, index);
    }

    public static AbstractReturnNode createReturnConstantNode(final CompiledCodeObject code, final int index, final Object coonstant) {
        if (code.isCompiledBlock()) {
            return ReturnConstantFromBlockNodeGen.create(code, index, coonstant);
        } else {
            return ReturnConstantFromMethodNodeGen.create(code, index, coonstant);
        }
    }

    public static AbstractReturnNode createReturnReceiverNode(final CompiledCodeObject code, final int index) {
        if (code.isCompiledBlock()) {
            return ReturnReceiverFromBlockNodeGen.create(code, index);
        } else {
            return ReturnReceiverFromMethodNodeGen.create(code, index);
        }
    }

    public static AbstractReturnNode createReturnTopNode(final CompiledCodeObject code, final int index) {
        if (code.isCompiledBlock()) {
            return ReturnTopFromBlockNodeGen.create(code, index);
        } else {
            return ReturnTopFromMethodNodeGen.create(code, index);
        }
    }

    public abstract static class AbstractReturnNode extends AbstractBytecodeNode {
        protected AbstractReturnNode(final CompiledCodeObject code, final int index) {
            super(code, index);
        }

        protected final boolean hasModifiedSender(final VirtualFrame frame) {
            final ContextObject context = getContext(frame);
            return context != null && context.hasModifiedSender();
        }

        @Override
        public final void executeVoid(final VirtualFrame frame) {
            throw SqueakException.create("executeReturn() should be called instead");
        }

        public final Object executeReturn(final VirtualFrame frame) {
            return executeReturnSpecialized(frame);
        }

        protected abstract Object executeReturnSpecialized(VirtualFrame frame);

        @SuppressWarnings("unused")
        protected Object getReturnValue(final VirtualFrame frame) {
            throw SqueakException.create("Needs to be overriden");
        }
    }

    protected abstract static class BlockReturnTopNode extends AbstractReturnNode {
        @Child private FrameStackPopNode popNode = FrameStackPopNode.create();

        protected BlockReturnTopNode(final CompiledCodeObject code, final int index) {
            super(code, index);
            assert code.isCompiledBlock() : "blockReturn can only occure in CompiledBlockObject";
        }

        @Specialization(guards = {"!hasModifiedSender(frame)"})
        protected final Object doLocalReturn(final VirtualFrame frame) {
            return getReturnValue(frame);
        }

        @Specialization(guards = {"hasModifiedSender(frame)"})
        protected final Object doNonLocalReturnClosure(final VirtualFrame frame,
                        @Cached final GetActiveProcessNode getActiveProcessNode) {
            // Target is sender of closure's home context.
            final ContextObject homeContext = FrameAccess.getClosure(frame).getHomeContext();
            assert homeContext.getProcess() != null;
            final boolean homeContextNotOnTheStack = homeContext.getProcess() != getActiveProcessNode.execute();
            final Object caller = homeContext.getFrameSender();
            if (caller == NilObject.SINGLETON || homeContextNotOnTheStack) {
                CompilerDirectives.transferToInterpreter();
                final ContextObject contextObject = GetOrCreateContextNode.getOrCreateFromActiveProcessUncached(frame);
                lookupContext().cannotReturn.executeAsSymbolSlow(frame, contextObject, getReturnValue(frame));
                throw SqueakException.create("Should not reach");
            }
            throw new NonLocalReturn(getReturnValue(frame), caller);
        }

        @Override
        protected final Object getReturnValue(final VirtualFrame frame) {
            return popNode.execute(frame);
        }

        @Override
        public final String toString() {
            CompilerAsserts.neverPartOfCompilation();
            return "blockReturn";
        }
    }

    protected interface ReturnFromMethod {
        abstract Object getReturnValue(final VirtualFrame frame);

        @Specialization(guards = {"!hasModifiedSender(frame)"})
        default Object doLocalReturn(final VirtualFrame frame) {
            return getReturnValue(frame);
        }

        @Specialization(guards = {"hasModifiedSender(frame)"})
        default Object doNonLocalReturn(final VirtualFrame frame) {
            assert FrameAccess.getSender(frame) instanceof ContextObject : "Sender must be a materialized ContextObject";
            throw new NonLocalReturn(getReturnValue(frame), FrameAccess.getSender(frame));
        }
    }

    protected interface ReturnFromBlock {
        abstract Object getReturnValue(final VirtualFrame frame);

        abstract SqueakImageContext lookupContext();

        @Specialization
        default Object doClosureReturn(final VirtualFrame frame,
                        @Cached final GetActiveProcessNode getActiveProcessNode) {
            // Target is sender of closure's home context.
            final ContextObject homeContext = FrameAccess.getClosure(frame).getHomeContext();
            assert homeContext.getProcess() != null;
            final Object caller = homeContext.getFrameSender();
            final boolean homeContextNotOnTheStack = homeContext.getProcess() != getActiveProcessNode.execute();
            if (caller == NilObject.SINGLETON || homeContextNotOnTheStack) {
                CompilerDirectives.transferToInterpreter();
                final ContextObject contextObject = GetOrCreateContextNode.getOrCreateFromActiveProcessUncached(frame);
                lookupContext().cannotReturn.executeAsSymbolSlow(frame, contextObject, getReturnValue(frame));
                throw SqueakException.create("Should not reach");
            }
            throw new NonLocalReturn(getReturnValue(frame), caller);
        }
    }

    public abstract static class AbstractReturnConstantNode extends AbstractReturnNode {
        public final Object constant;

        protected AbstractReturnConstantNode(final CompiledCodeObject code, final int index, final Object obj) {
            super(code, index);
            constant = obj;
        }

        @Override
        public final Object getReturnValue(final VirtualFrame frame) {
            return constant;
        }

        @Override
        public final String toString() {
            CompilerAsserts.neverPartOfCompilation();
            return "return: " + constant.toString();
        }
    }

    protected abstract static class ReturnConstantFromMethodNode extends AbstractReturnConstantNode implements ReturnFromMethod {
        protected ReturnConstantFromMethodNode(final CompiledCodeObject code, final int index, final Object obj) {
            super(code, index, obj);
        }
    }

    protected abstract static class ReturnConstantFromBlockNode extends AbstractReturnConstantNode implements ReturnFromBlock {
        protected ReturnConstantFromBlockNode(final CompiledCodeObject code, final int index, final Object obj) {
            super(code, index, obj);
        }
    }

    protected abstract static class AbstractReturnReceiverNode extends AbstractReturnNode {
        protected AbstractReturnReceiverNode(final CompiledCodeObject code, final int index) {
            super(code, index);
        }

        @Override
        public final Object getReturnValue(final VirtualFrame frame) {
            return FrameAccess.getReceiver(frame);
        }

        @Override
        public final String toString() {
            CompilerAsserts.neverPartOfCompilation();
            return "returnSelf";
        }
    }

    protected abstract static class ReturnReceiverFromMethodNode extends AbstractReturnReceiverNode implements ReturnFromMethod {
        protected ReturnReceiverFromMethodNode(final CompiledCodeObject code, final int index) {
            super(code, index);
        }
    }

    protected abstract static class ReturnReceiverFromBlockNode extends AbstractReturnReceiverNode implements ReturnFromBlock {
        protected ReturnReceiverFromBlockNode(final CompiledCodeObject code, final int index) {
            super(code, index);
        }
    }

    protected abstract static class AbstractReturnTopNode extends AbstractReturnNode {
        @Child private FrameStackPopNode popNode = FrameStackPopNode.create();

        protected AbstractReturnTopNode(final CompiledCodeObject code, final int index) {
            super(code, index);
        }

        @Override
        public final Object getReturnValue(final VirtualFrame frame) {
            return popNode.execute(frame);
        }

        @Override
        public String toString() {
            CompilerAsserts.neverPartOfCompilation();
            return "returnTop";
        }
    }

    protected abstract static class ReturnTopFromMethodNode extends AbstractReturnTopNode implements ReturnFromMethod {
        protected ReturnTopFromMethodNode(final CompiledCodeObject code, final int index) {
            super(code, index);
        }
    }

    protected abstract static class ReturnTopFromBlockNode extends AbstractReturnTopNode implements ReturnFromBlock {
        protected ReturnTopFromBlockNode(final CompiledCodeObject code, final int index) {
            super(code, index);
        }
    }
}
