package com.example.net;

import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Project: ImageLoader.
 * Data: 2016/7/27.
 * Created by 8luerain.
 * Contact:<a href="mailto:8luerain@gmail.com">Contact_me_now</a>
 */
public class ScoketDispatcher {

    private static final ScoketDispatcher INSTANCE = new ScoketDispatcher();

    public static final int THEAD_POLL_CORE_SIZE = 1;

    private static LinkedBlockingQueue<Runnable> sLinkedBlockingQueue = new LinkedBlockingQueue();

    private static ThreadFactory sThreadFactory = new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            return new ClientActivity();
        }
    };

    private static ExecutorService sExecutorService = new ThreadPoolExecutor(
            THEAD_POLL_CORE_SIZE,
            Integer.MAX_VALUE,
            10L,
            TimeUnit.SECONDS,
            sLinkedBlockingQueue,
            sThreadFactory
    );

    private ScoketDispatcher() {
        //no instance
    }

    public static ScoketDispatcher getINSTANCE() {
        return INSTANCE;
    }

    public void dispatchSocket(Socket socket) {
        sLinkedBlockingQueue.add(new Runnable() {
            @Override
            public void run() {

            }
        });
    }
}

