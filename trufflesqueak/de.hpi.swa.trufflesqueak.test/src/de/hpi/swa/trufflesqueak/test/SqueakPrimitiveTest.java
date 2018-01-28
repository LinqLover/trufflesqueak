package de.hpi.swa.trufflesqueak.test;

import java.math.BigInteger;

import org.junit.Test;

import de.hpi.swa.trufflesqueak.model.BaseSqueakObject;
import de.hpi.swa.trufflesqueak.model.ListObject;

public class SqueakPrimitiveTest extends AbstractSqueakTestCase {
    @Test
    public void testPrimEquivalent() {
        BaseSqueakObject rcvr = image.specialObjectsArray;
        assertTrue((boolean) runBinaryPrimitive(110, rcvr, rcvr));
        assertFalse((boolean) runBinaryPrimitive(110, rcvr, image.nil));
    }

    @Test
    public void testPrimReplaceFromTo() {
        BaseSqueakObject rcvr = new ListObject(image, image.arrayClass,
                        new Object[]{image.nil, image.sqFalse, image.sqTrue, image.characterClass, image.metaclass,
                                        image.schedulerAssociation, image.smallIntegerClass, image.smalltalk,
                                        image.specialObjectsArray});
        assertSame(image.nil, rcvr.at0(0));
        for (int i = 1; i < 8; i++) {
            assertNotSame(image.nil, rcvr.at0(i));
        }
        Object result = runQuinaryPrimitive(105, rcvr, 1, 6, new ListObject(image, image.nilClass, 10), 1);
        assertSame(result, rcvr);
        for (int i = 0; i < 6; i++) {
            assertSame(image.nil, rcvr.at0(i));
        }
        for (int i = 7; i < 8; i++) {
            assertNotSame(image.nil, rcvr.at0(i));
        }
    }

    @Test
    public void testAdd() {
        Object[][] testValues = new Object[][]{
                        {Integer.MAX_VALUE, Integer.MAX_VALUE, 2 * (long) Integer.MAX_VALUE},
                        {Long.MAX_VALUE, Long.MAX_VALUE, BigInteger.valueOf(Long.MAX_VALUE).multiply(BigInteger.valueOf(2))},
                        {BigInteger.valueOf(Long.MAX_VALUE), Long.MIN_VALUE, BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.valueOf(Long.MIN_VALUE)).intValue()}};
        for (int i = 0; i < testValues.length; i++) {
            Object[] values = testValues[i];
            assertEquals(runBinaryPrimitive(1, values[0], values[1]), values[2]);
        }
    }

    @Test
    public void testSub() {
        Object[][] testValues = new Object[][]{
                        {Integer.MAX_VALUE, Integer.MAX_VALUE, 0},
                        {Long.MAX_VALUE, Long.MAX_VALUE, 0},
                        {Long.MAX_VALUE, Long.MAX_VALUE - 1, 1},
                        {Long.MAX_VALUE, Long.MAX_VALUE - Integer.MAX_VALUE, Integer.MAX_VALUE},
                        {Long.MIN_VALUE, 1, BigInteger.valueOf(Long.MIN_VALUE).subtract(BigInteger.ONE)},
                        {BigInteger.valueOf(Long.MAX_VALUE), BigInteger.valueOf(Long.MAX_VALUE - Integer.MAX_VALUE), Integer.MAX_VALUE},
                        {BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.ONE), 1, Long.MAX_VALUE}};
        for (int i = 0; i < testValues.length; i++) {
            Object[] values = testValues[i];
            assertEquals(runBinaryPrimitive(2, values[0], values[1]), values[2]);
        }
    }
}
