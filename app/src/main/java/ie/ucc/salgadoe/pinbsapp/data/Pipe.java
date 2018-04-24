package ie.ucc.salgadoe.pinbsapp.data;

import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

/**
 * Defines a pipe to solve the mProducer-mConsumer problem.
 */

public class Pipe {
    private static final String TAG = Pipe.class.getName();
    private int mPipeBufferSize;
//    private PipedOutputStream mProducer;
//    private PipedInputStream mConsumer;

    private DataInputStream mDataConsumer;
    private DataOutputStream mDataProducer;


    /**
     * Defines a simple pipe with a buffered input and output
     *
     * @param elementSize the size in bits of the elements that will be contained
     * @param nElements   the number of elements that will be contained
     */
    public Pipe(int elementSize, int nElements) {
        try {
            mPipeBufferSize = nElements * elementSize / Byte.SIZE;    // in bytes
            PipedInputStream mConsumer = new PipedInputStream(mPipeBufferSize);
            PipedOutputStream mProducer = new PipedOutputStream(mConsumer);

            mDataConsumer = new DataInputStream(mConsumer);
            mDataProducer = new DataOutputStream(mProducer);
        } catch (IOException e) {
            Log.d(TAG, "Pipe: Could not be created");
        }
    }

    /**
     * Returns the PipedInputStream associated
     */
    public DataInputStream getSoure() {
        return mDataConsumer;
    }
//    public PipedInputStream getSoure() {
//        return mConsumer;
//    }

    /**
     * Returns the PipedOutputStream associated
     */
    public DataOutputStream getSink() {
        return mDataProducer;
    }

//    public PipedOutputStream getSink() {
//        return mProducer;
//    }
//
    public int getPipeBufferSize() {
        return mPipeBufferSize;
    }

    /**
     * Returns the space avaliable in the pipe. In percentage
     *
     * @return integer value of the percentage of avaliable space [0-100]
     */
    public int getCapacity() {
        try {
//            double value = ((mPipeBufferSize - mConsumer.available()) * 100.0 / (mPipeBufferSize));
            double value = ((mPipeBufferSize - mDataConsumer.available()) * 100.0 / (mPipeBufferSize));

            return (int) value;
        } catch (IOException expected) {
            /* If the UI ask for this and its not instantiated, reply that is empty. */
            return 100;
        }
    }

    /**
     * Returns the size in bytes of the data that can contain the pipe
     */
    public int getSize() {
        return mPipeBufferSize;
    }

    /**
     * Checks fullness of the pipe
     *
     * @return {@code true} if it is full, {@code false} otherwise
     */
    public boolean isFull() throws IOException {
//        return (mConsumer.available() == mPipeBufferSize);
        return (mDataConsumer.available() == mPipeBufferSize);
    }

    /**
     * Release the streams associated to it. Once released most of the methods will return an Exception.
     * <p>
     * It is better to handle that in each process. Closing the producer will notice with and EOF
     * to the consumer.
     */
    public void release() {
//        try {
//            mProducer.close();
//            mConsumer.close();
//        } catch (IOException e) {
//            Log.d(TAG, "release: A problem closing resources has occurred");
//            e.printStackTrace();
//        }
    }
}
