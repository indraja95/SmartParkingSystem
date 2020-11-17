package com.firebase.client.core;

import com.firebase.client.CredentialStore;
import com.firebase.client.EventTarget;
import com.firebase.client.Firebase;
import com.firebase.client.Logger;
import com.firebase.client.Logger.Level;
import com.firebase.client.RunLoop;
import com.firebase.client.authentication.NoopCredentialStore;
import com.firebase.client.core.persistence.PersistenceManager;
import com.firebase.client.utilities.DefaultLogger;
import com.firebase.client.utilities.DefaultRunLoop;
import com.firebase.client.utilities.LogWrapper;
import com.firebase.tubesock.ThreadInitializer;
import com.firebase.tubesock.WebSocket;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.concurrent.ThreadFactory;

enum GaePlatform implements Platform {
    INSTANCE;
    
    static ThreadFactory threadFactoryInstance;
    static final ThreadInitializer threadInitializerInstance = null;

    static {
        threadInitializerInstance = new ThreadInitializer() {
            public void setName(Thread t, String name) {
            }

            public void setDaemon(Thread t, boolean isDaemon) {
            }

            public void setUncaughtExceptionHandler(Thread t, UncaughtExceptionHandler handler) {
                t.setUncaughtExceptionHandler(handler);
            }
        };
    }

    public Logger newLogger(Context ctx, Level level, List<String> components) {
        return new DefaultLogger(level, components);
    }

    private static ThreadFactory getGaeThreadFactory() {
        if (threadFactoryInstance == null) {
            try {
                Class c = Class.forName("com.google.appengine.api.ThreadManager");
                if (c != null) {
                    threadFactoryInstance = (ThreadFactory) c.getMethod("backgroundThreadFactory", new Class[0]).invoke(null, new Object[0]);
                }
            } catch (ClassNotFoundException e) {
                return null;
            } catch (InvocationTargetException e2) {
                throw new RuntimeException(e2);
            } catch (NoSuchMethodException e3) {
                throw new RuntimeException(e3);
            } catch (IllegalAccessException e4) {
                throw new RuntimeException(e4);
            }
        }
        return threadFactoryInstance;
    }

    public static boolean isActive() {
        return getGaeThreadFactory() != null;
    }

    public void initialize() {
        WebSocket.setThreadFactory(threadFactoryInstance, new ThreadInitializer() {
            public void setName(Thread thread, String s) {
                GaePlatform.threadInitializerInstance.setName(thread, s);
            }
        });
    }

    public EventTarget newEventTarget(Context ctx) {
        return new ThreadPoolEventTarget(getGaeThreadFactory(), threadInitializerInstance);
    }

    public RunLoop newRunLoop(Context context) {
        final LogWrapper logger = context.getLogger("RunLoop");
        return new DefaultRunLoop() {
            public void handleException(Throwable e) {
                logger.error("Uncaught exception in Firebase runloop (" + Firebase.getSdkVersion() + "). Please report to support@firebase.com", e);
            }

            /* access modifiers changed from: protected */
            public ThreadFactory getThreadFactory() {
                return GaePlatform.threadFactoryInstance;
            }

            /* access modifiers changed from: protected */
            public ThreadInitializer getThreadInitializer() {
                return GaePlatform.threadInitializerInstance;
            }
        };
    }

    public String getUserAgent(Context ctx) {
        return System.getProperty("java.specification.version", "Unknown") + "/" + "AppEngine";
    }

    public String getPlatformVersion() {
        return "gae-" + Firebase.getSdkVersion();
    }

    public PersistenceManager createPersistenceManager(Context ctx, String namespace) {
        return null;
    }

    public CredentialStore newCredentialStore(Context ctx) {
        return new NoopCredentialStore(ctx);
    }

    public void runBackgroundTask(final Context ctx, final Runnable r) {
        threadFactoryInstance.newThread(new Runnable() {
            public void run() {
                try {
                    r.run();
                } catch (OutOfMemoryError e) {
                    throw e;
                } catch (Throwable e2) {
                    ctx.getLogger("BackgroundTask").error("An unexpected error occurred. Please contact support@firebase.com. Details: ", e2);
                    throw new RuntimeException(e2);
                }
            }
        }).start();
    }
}
