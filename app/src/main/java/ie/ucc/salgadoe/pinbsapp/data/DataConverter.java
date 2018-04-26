package ie.ucc.salgadoe.pinbsapp.data;

import android.util.Log;

import java.nio.ByteBuffer;

/**
 * Implements methods to convert data from one type to another
 */

public class DataConverter {
    private static final String TAG = "DataConverter";

    /**
     * Converts a double to a byte array of 8 elements
     *
     * @param value double
     * @return 8 bytes representing the given double
     */
    public static byte[] DoubleToByteArray(double value) {
//        ByteBuffer buffer = ByteBuffer.allocate(8);
//        buffer.putDouble(value);
//        return buffer.array();
        return ByteBuffer.wrap(new byte[8]).putDouble(value).array();
    }

    /**
     * Converts an array of 8 elements to its double representation
     *
     * @param array 8 bytes
     * @return double
     */
    public static double ByteArrayToDouble(byte[] array) {
//        if (array.length != 8) {
//            throw new IllegalArgumentException("The size of the array must be 8");
//        }
        return ByteBuffer.wrap(array).getDouble();
    }

    /**
     * Converts a value of [-1, 1] to a short [-32767, 32767] and returns its value in byte array.
     * Uses: Double -> AudioFormat.ENCODING_PCM_16BIT
     *
     * @param value normalized double
     * @return 2 bytes representing a Short
     * @deprecated Replaced by {@link #DoubleToShort(Double)}
     */
    @Deprecated
    public static byte[] DoubleTo16Bits(double value) {
        /* [-32768, 32767] --> this will shrink the integer to use only 16 bits (15+sign) */
        double d = value * 32767;
        Double d2 = Math.floor(d);
        Short sho = d2.shortValue();
//        ByteBuffer bb = ByteBuffer.allocate(2);
//        bb.order(ByteOrder.nativeOrder()); //native is little
//        bb.putShort(sho);
        byte[] byteArray = new byte[2];
        byteArray[0] = (byte) (sho & 0x00FF);
        byteArray[1] = (byte) ((sho >>> 8) & 0x00FF);
        Log.d(TAG, "DoubleTo16Bits: tone >s " + String.format("%.06f", value) + " >scale " + String.format("%.06f", d) + " >floor " + d2 + " >int " + sho + " >byte " + byteArray[0] + " " + byteArray[1]);
        Log.d(TAG, "DoubleTo16Bits: >bits " + ByteToBits(byteArray[1]) + ByteToBits(byteArray[0]));
        return byteArray;
    }

    /**
     * Converts a value of [-1, 1] to a integer [-127, 127] and returns its value in byte array
     * Uses: Double -> AudioFormat.ENCODING_PCM_8BIT
     *
     * @param value normalized double
     * @return 1 byte representing an Integer
     * @deprecated Replaced by {@link #DoubleToByte(Double)}
     */
    @Deprecated
    public static byte[] DoubleTo8Bits(double value) {
        /* [0,254] --> this will shrink the integer to use only 8 bits (7+sign) */
        double d = value * 127 + 127;
        Double d2 = Math.floor(d);
        Integer integ = d2.intValue();
        /* Get only one byte from integer (4 bytes). */
        byte[] byteArray = new byte[1];
        byteArray[0] = (byte) (integ & 0x00FF);
//        Log.d(TAG, "DoubleTo8Bits: tone >s " + String.format("%.06f", value) + " >scale " + String.format("%.06f", d) + " >floor " + d2 + " >int " + integ + " >byte " + byteArray[0]);// + " " + byteArray[1]);
//        Log.d(TAG, "DoubleTo8Bits: >bits " + ByteToBits(byteArray[0]));
        return byteArray;
    }

    /**
     * Convert a double to a short (64 --> 16 bits). The input must be inside [1,1] to
     * not overflow the output. This method does not check the input for being less computational
     * costly. The output will be in the range of [-32767, 32767]. Little endian.
     *
     * @param value double
     * @return 1 short representing the double
     */
    public static short[] DoubleToShort(Double value) {
        value *= Short.MAX_VALUE;
        short[] byteArray = new short[1];
        byteArray[0] = value.shortValue();
//        Log.d(TAG, "DoubleToShort: tone >s " + String.format("%.06f", value) + " >byte " + byteArray[0]);
//        Log.d(TAG, "DoubleToShort: >bits " + ShortToBits(byteArray[0]));
        return byteArray;
    }

    /**
     * Convert a double to a short (64 --> 8 bits). The input must be inside [-1,1] to
     * not overflow the output. This method does not check the input for being less computational
     * costly. The output will be in the range of [0, 254]. Little endian.
     *
     * @param value double
     * @return 1 byte representing the double
     */
    public static byte[] DoubleToByte(Double value) {
        value = value * Byte.MAX_VALUE + Byte.MAX_VALUE;
        byte[] byteArray = new byte[1];
        byteArray[0] = value.byteValue();
//        Log.d(TAG, "DoubleToByte: tone >s " + String.format("%.06f", value) + " >byte " + byteArray[0]);
//        Log.d(TAG, "DoubleToByte: >bits " + ByteToBits(byteArray[0]));
        return byteArray;
    }

    /**
     * Convert a double to a float (64 --> 32 bits). The input must be inside [-1,1] to
     * not overflow the output. This method does not check the input for being less computational
     * costly. The output will be in the range of [-1,1]. Little endian.
     *
     * @param value value
     **/
    public static float[] DoubleToFloat(Double value) {
        float[] byteArray = new float[1];
        byteArray[0] = value.floatValue();
//        Log.d(TAG, "DoubleToByte: tone >s " + String.format("%.06f", value) + " >byte " + byteArray[0]);
        return byteArray;
    }

    /**
     * Returns the binary string representing the input value
     *
     * @param octet value
     * @return string representation
     */
    public static String ByteToBits(byte octet) {
        String bits = "";
        for (int i = Byte.SIZE - 1; i >= 0; i--) {
            bits += String.valueOf(octet >>> i & 0x1);
            if (i % 4 == 0) bits += ".";
        }
        return bits;
    }

    /**
     * Returns the binary string representing the input value
     *
     * @param octet value
     * @return string representation
     */
    public static String ShortToBits(short octet) {
        String bits = "";
        for (int i = Short.SIZE - 1; i >= 0; i--) {
            bits += String.valueOf(octet >>> i & 0x1);
            if (i % 4 == 0) bits += ".";
        }
        return bits;
    }
}
