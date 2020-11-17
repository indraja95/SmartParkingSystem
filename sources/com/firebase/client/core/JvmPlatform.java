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
import java.util.List;
import java.util.concurrent.Executors;

enum JvmPlatform implements Platform {
    INSTANCE;

    public Logger newLogger(Context ctx, Level level, List<String> components) {
        return new DefaultLogger(level, components);
    }

    public EventTarget newEventTarget(Context ctx) {
        return new ThreadPoolEventTarget(Executors.defaultThreadFactory(), ThreadInitializer.defaultInstance);
    }

    public RunLoop newRunLoop(Context context) {
        final LogWrapper logger = context.getLogger("RunLoop");
        return new DefaultRunLoop() {
            public void handleException(Throwable e) {
                logger.error("Uncaught exception in Firebase runloop (" + Firebase.getSdkVersion() + "). Please report to support@firebase.com", e);
            }
        };
    }

    public String getUserAgent(Context ctx) {
        return System.getProperty("java.specification.version", "Unknown") + "/" + System.getProperty("java.vm.name", "Unknown JVM");
    }

    public String getPlatformVersion() {
        return "jvm-" + Firebase.getSdkVersion();
    }

    public PersistenceManager createPersistenceManager(Context ctx, String namespace) {
        return null;
    }

    public CredentialStore newCredentialStore(Context ctx) {
        return new NoopCredentialStore(ctx);
    }

    public void runBackgroundTask(final Context ctx, final Runnable r) {
        new Thread() {
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
        }.start();
    }
}
