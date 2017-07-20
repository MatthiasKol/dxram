package de.hhu.bsinfo.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.Buffer;
import java.nio.ByteBuffer;

/**
 * Helper class for advanced usage of ByteBuffers
 *
 * @author Stefan Nothaas, stefan.nothaas@hhu.de, 11.07.2017
 */
public final class ByteBufferHelper {
    private static final Field ms_byteBufferAddress;
    private static final Field ms_byteBufferCapacity;
    private static final Constructor<?> ms_directByteBufferConstructor;

    static {
        try {
            Class<?> clazz = Class.forName("java.nio.DirectByteBuffer");
            ms_directByteBufferConstructor = clazz.getDeclaredConstructor(Long.TYPE, Integer.TYPE);
            ms_directByteBufferConstructor.setAccessible(true);
        } catch (final ClassNotFoundException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        try {
            ms_byteBufferAddress = Buffer.class.getDeclaredField("address");
            ms_byteBufferAddress.setAccessible(true);

            ms_byteBufferCapacity = Buffer.class.getDeclaredField("capacity");
            ms_byteBufferCapacity.setAccessible(true);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Utils class
     */
    private ByteBufferHelper() {

    }

    /**
     * Get the memory address of the array allocated on the heap outside of the jvm for a DirectByteBuffer
     *
     * @param p_buffer
     *         Buffer to get the address of (must be a DirectByteBuffer!)
     * @return Memory address pointing to the data stored on the heap outside of the jvm
     */
    public static long getDirectAddress(final ByteBuffer p_buffer) {
        try {
            return ms_byteBufferAddress.getLong(p_buffer);
        } catch (final IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Wrap a (unsafe) pointer
     *
     * @param p_addr
     *         Pointer address
     * @param p_size
     *         Size of the memory region
     * @return Wrapped addr as ByteBuffer
     */
    public static ByteBuffer wrap(final long p_addr, final int p_size) {
        try {
            return (ByteBuffer) ms_directByteBufferConstructor.newInstance(p_addr, p_size);
        } catch (final InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}