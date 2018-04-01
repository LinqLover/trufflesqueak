package de.hpi.swa.trufflesqueak.nodes;

import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.frame.MaterializedFrame;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.RootNode;

import de.hpi.swa.trufflesqueak.SqueakImageContext;
import de.hpi.swa.trufflesqueak.SqueakLanguage;
import de.hpi.swa.trufflesqueak.exceptions.ProcessSwitch;
import de.hpi.swa.trufflesqueak.exceptions.Returns.NonLocalReturn;
import de.hpi.swa.trufflesqueak.exceptions.Returns.NonVirtualReturn;
import de.hpi.swa.trufflesqueak.exceptions.Returns.TopLevelReturn;
import de.hpi.swa.trufflesqueak.exceptions.SqueakException;
import de.hpi.swa.trufflesqueak.exceptions.SqueakQuit;
import de.hpi.swa.trufflesqueak.model.BaseSqueakObject;
import de.hpi.swa.trufflesqueak.model.BlockClosureObject;
import de.hpi.swa.trufflesqueak.model.CompiledCodeObject;
import de.hpi.swa.trufflesqueak.model.ContextObject;
import de.hpi.swa.trufflesqueak.nodes.context.frame.FrameSlotWriteNode;
import de.hpi.swa.trufflesqueak.util.FrameAccess;

public class ExecuteTopLevelContextNode extends RootNode {
    @CompilationFinal private final SqueakImageContext image;
    @CompilationFinal private final ContextObject initialContext;
    @CompilationFinal private final EnterCodeNode enterActiveCodeNode;
    @Child private FrameSlotWriteNode instructionPointerWriteNode;
    @Child private FrameSlotWriteNode contextWriteNode;

    public static ExecuteTopLevelContextNode create(SqueakLanguage language, ContextObject context) {
        return new ExecuteTopLevelContextNode(language, context, context.getCodeObject());
    }

    private ExecuteTopLevelContextNode(SqueakLanguage language, ContextObject context, CompiledCodeObject code) {
        super(language, code.getFrameDescriptor());
        this.image = code.image;
        this.initialContext = context;
        enterActiveCodeNode = EnterCodeNode.create(language, code);
        instructionPointerWriteNode = FrameSlotWriteNode.create(code.instructionPointerSlot);
        contextWriteNode = FrameSlotWriteNode.create(code.thisContextOrMarkerSlot);
    }

    @Override
    public Object execute(VirtualFrame frame) {
        try {
            executeLoop();
        } catch (TopLevelReturn e) {
            return e.getReturnValue();
        } catch (SqueakQuit e) {
            System.out.println(e.toString());
            System.exit(e.getExitCode());
        } finally {
            image.display.close();
        }
        throw new SqueakException("Top level context did not return");
    }

    @Child ExecuteContextNode executeContextNode;

    public void executeLoop() {
        ContextObject activeContext = initialContext;
        while (true) {
            BaseSqueakObject sender = activeContext.getSender();
            try {
                CompiledCodeObject code = activeContext.getCodeObject();
                code.invalidateCanBeVirtualizedAssumption();
                Object[] frameArgs = activeContext.getReceiverAndArguments();
                BlockClosureObject closure = activeContext.getClosure();
                MaterializedFrame frame = Truffle.getRuntime().createMaterializedFrame(FrameAccess.newWith(code, sender, closure, frameArgs), code.getFrameDescriptor());
                contextWriteNode.executeWrite(frame, activeContext);
                // FIXME: do not create node here
                executeContextNode = insert(ExecuteContextNode.create(code));
                // doIt: activeContext.printSqStackTrace();
                Object result = executeContextNode.executeNonVirtualized(frame, activeContext);
                activeContext = unwindContextChain(sender, sender, result);
                image.traceVerbose("Local Return on top-level, new context is " + activeContext);
            } catch (ProcessSwitch ps) {
                image.trace("Switching from " + activeContext + " to " + ps.getNewContext());
                activeContext = ps.getNewContext();
            } catch (NonLocalReturn nlr) {
                BaseSqueakObject target = nlr.hasArrivedAtTargetContext() ? sender : nlr.getTargetContext();
                activeContext = unwindContextChain(sender, target, nlr.getReturnValue());
                image.traceVerbose("Non Local Return on top-level, new context is " + activeContext);
            } catch (NonVirtualReturn nvr) {
                activeContext = unwindContextChain(nvr.getCurrentContext(), nvr.getTargetContext(), nvr.getReturnValue());
                image.traceVerbose("Non Virtual Return on top-level, new context is " + activeContext);
            }
        }
    }

    private ContextObject unwindContextChain(BaseSqueakObject startContext, BaseSqueakObject targetContext, Object returnValue) {
        if (startContext == image.nil) {
            throw new TopLevelReturn(returnValue);
        }
        if (!(targetContext instanceof ContextObject)) {
            throw new SqueakException("targetContext is not a ContextObject: " + targetContext.toString());
        }
        ContextObject context = (ContextObject) startContext;
        while (context != targetContext) {
            BaseSqueakObject sender = context.getSender();
            if (sender == image.nil) {
                throw new SqueakException("Unable to unwind context chain");
            }
            context.terminate();
            context = (ContextObject) sender;
        }
        context.push(returnValue);
        return context;
    }
}
