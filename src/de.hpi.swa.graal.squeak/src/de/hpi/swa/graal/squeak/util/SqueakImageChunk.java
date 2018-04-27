package de.hpi.swa.graal.squeak.util;

import de.hpi.swa.graal.squeak.SqueakImageContext;

public final class SqueakImageChunk extends AbstractImageChunk {

    public SqueakImageChunk(final SqueakImageReader reader, final SqueakImageContext image, final long size, final int format, final int classid, final int hash, final int pos) {
        super(reader, image, size, format, classid, hash, pos);
    }
}