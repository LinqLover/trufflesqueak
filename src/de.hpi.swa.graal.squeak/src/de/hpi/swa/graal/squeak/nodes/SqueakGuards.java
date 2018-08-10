package de.hpi.swa.graal.squeak.nodes;

import de.hpi.swa.graal.squeak.model.AbstractSqueakObject;
import de.hpi.swa.graal.squeak.model.EmptyObject;
import de.hpi.swa.graal.squeak.model.FloatObject;
import de.hpi.swa.graal.squeak.model.LargeIntegerObject;
import de.hpi.swa.graal.squeak.model.NativeObject;
import de.hpi.swa.graal.squeak.model.NotProvided;
import de.hpi.swa.graal.squeak.model.PointersObject;

public final class SqueakGuards {
    public static boolean isAbstractSqueakObject(final Object object) {
        return object instanceof AbstractSqueakObject;
    }

    public static boolean isEmptyObject(final AbstractSqueakObject object) {
        return object instanceof EmptyObject;
    }

    public static boolean isFloatObject(final AbstractSqueakObject object) {
        return object instanceof FloatObject;
    }

    public static boolean isLargeIntegerObject(final AbstractSqueakObject object) {
        return object instanceof LargeIntegerObject;
    }

    public static boolean isNativeObject(final AbstractSqueakObject object) {
        return object instanceof NativeObject;
    }

    public static boolean isNotProvided(final Object obj) {
        return NotProvided.isInstance(obj);
    }

    public static boolean isPointersObject(final Object obj) {
        return obj instanceof PointersObject;
    }

    public static boolean isSmallInteger32bit(final long value) {
        return LargeIntegerObject.SMALLINTEGER32_MIN <= value && value <= LargeIntegerObject.SMALLINTEGER32_MAX;
    }

    public static boolean isSmallInteger64bit(final long value) {
        return LargeIntegerObject.SMALLINTEGER64_MIN <= value && value <= LargeIntegerObject.SMALLINTEGER64_MAX;
    }

    private SqueakGuards() {
    }
}