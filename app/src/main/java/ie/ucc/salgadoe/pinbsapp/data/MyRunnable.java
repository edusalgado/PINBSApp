package ie.ucc.salgadoe.pinbsapp.data;

import android.os.Handler;
import android.os.SystemClock;

import java.io.IOException;

/**
 * Runnable with the capability to procrastinate
 */
public abstract class MyRunnable implements Runnable {
    protected long delay = 0;
    protected Handler mHandler = null;

    public abstract void run();

    protected abstract void doWork() throws IOException;

    public abstract void stop();
//    public abstract void unlock();

    /**
     * Set a delay that will be used when #run() is called if the class that extends implements it.
     *
     * @param ms time in miliseconds
     * @see SystemClock#sleep(long)
     */
    public void procrastinate(long ms) {
        delay = ms;
    }

    /**
     * Sets a handle in case of need to communicate something
     *
     * @param handler a handler
     */
    public void setHandler(Handler handler) {
        mHandler = handler;
    }
}