package de.hpi.swa.graal.squeak.nodes.plugins;

import java.util.List;

import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.NodeFactory;
import com.oracle.truffle.api.dsl.Specialization;

import de.hpi.swa.graal.squeak.model.BaseSqueakObject;
import de.hpi.swa.graal.squeak.model.CompiledMethodObject;
import de.hpi.swa.graal.squeak.model.NativeObject;
import de.hpi.swa.graal.squeak.nodes.primitives.AbstractPrimitiveFactoryHolder;
import de.hpi.swa.graal.squeak.nodes.primitives.AbstractPrimitiveNode;
import de.hpi.swa.graal.squeak.nodes.primitives.SqueakPrimitive;

public class HostWindowPlugin extends AbstractPrimitiveFactoryHolder {

    @GenerateNodeFactory
    @SqueakPrimitive(name = "primitiveWindowClose", numArguments = 2)
    protected abstract static class PrimHostWindowCloseNode extends AbstractPrimitiveNode {
        protected PrimHostWindowCloseNode(final CompiledMethodObject code) {
            super(code);
        }

        @Specialization(guards = {"id == 1"})
        protected final Object doSize(final BaseSqueakObject receiver, @SuppressWarnings("unused") final long id) {
            code.image.display.close();
            return receiver;
        }
    }

    @GenerateNodeFactory
    @SqueakPrimitive(name = "primitiveHostWindowPosition", numArguments = 2)
    protected abstract static class PrimHostWindowPositionNode extends AbstractPrimitiveNode {
        protected PrimHostWindowPositionNode(final CompiledMethodObject code) {
            super(code);
        }

        @SuppressWarnings("unused")
        @Specialization(guards = {"id == 1"})
        protected final Object doSize(final BaseSqueakObject receiver, final long id) {
            return code.image.newPoint(0L, 0L);
        }
    }

    @GenerateNodeFactory
    @SqueakPrimitive(name = "primitiveHostWindowSizeSet", numArguments = 4)
    protected abstract static class PrimHostWindowSizeSetNode extends AbstractPrimitiveNode {
        protected PrimHostWindowSizeSetNode(final CompiledMethodObject code) {
            super(code);
        }

        @Specialization(guards = {"id == 1"})
        protected final Object doSize(final BaseSqueakObject receiver, @SuppressWarnings("unused") final long id, final long width, final long height) {
            code.image.display.resizeTo((int) width, (int) height);
            return receiver;
        }
    }

    @GenerateNodeFactory
    @SqueakPrimitive(name = "primitiveHostWindowTitle", numArguments = 3)
    protected abstract static class PrimHostWindowTitleNode extends AbstractPrimitiveNode {
        protected PrimHostWindowTitleNode(final CompiledMethodObject code) {
            super(code);
        }

        @Specialization(guards = {"id == 1"})
        protected final Object doTitle(final BaseSqueakObject receiver, @SuppressWarnings("unused") final long id, final NativeObject title) {
            code.image.display.setWindowTitle(title.toString());
            return receiver;
        }
    }

    @Override
    public List<? extends NodeFactory<? extends AbstractPrimitiveNode>> getFactories() {
        return HostWindowPluginFactory.getFactories();
    }
}