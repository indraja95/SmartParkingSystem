package com.firebase.client.android;

import android.content.Context;
import android.os.Build.VERSION;
import android.os.Handler;
import android.util.Log;
import com.firebase.client.CredentialStore;
import com.firebase.client.EventTarget;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseException;
import com.firebase.client.Logger;
import com.firebase.client.Logger.Level;
import com.firebase.client.RunLoop;
import com.firebase.client.core.Platform;
import com.firebase.client.core.persistence.DefaultPersistenceManager;
import com.firebase.client.core.persistence.LRUCachePolicy;
import com.firebase.client.core.persistence.PersistenceManager;
import com.firebase.client.utilities.DefaultRunLoop;
import com.firebase.client.utilities.LogWrapper;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AndroidPlatform implements Platform {
    /* access modifiers changed from: private */
    public final Context applicationContext;
    private final Set<String> createdPersistenceCaches = new HashSet();

    public AndroidPlatform(Context context) {
        this.applicationContext = context.getApplicationContext();
    }

    public EventTarget newEventTarget(com.firebase.client.core.Context context) {
        return new AndroidEventTarget();
    }

    public RunLoop newRunLoop(com.firebase.client.core.Context ctx) {
        final LogWrapper logger = ctx.getLogger("RunLoop");
        return new DefaultRunLoop() {
            public void handleException(final Throwable e) {
                final String message = "Uncaught exception in Firebase runloop (" + Firebase.getSdkVersion() + "). Please report to support@firebase.com";
                logger.error(message, e);
                new Handler(AndroidPlatform.this.applicationContext.getMainLooper()).post(new Runnable() {
                    public void run() {
                        throw new RuntimeException(message, e);
                    }
                });
            }
        };
    }

    public Logger newLogger(com.firebase.client.core.Context context, Level component, List<String> enabledComponents) {
        return new AndroidLogger(component, enabledComponents);
    }

    public String getUserAgent(com.firebase.client.core.Context context) {
        return VERSION.SDK_INT + "/Android";
    }

    public void runBackgroundTask(com.firebase.client.core.Context context, final Runnable r) {
        new Thread() {
            public void run() {
                try {
                    r.run();
                } catch (OutOfMemoryError e) {
                    throw e;
                } catch (Throwable e2) {
                    Log.e("Firebase", "An unexpected error occurred. Please contact support@firebase.com. Details: ", e2);
                    throw new RuntimeException(e2);
                }
            }
        }.start();
    }

    public String getPlatformVersion() {
        return "android-" + Firebase.getSdkVersion();
    }

    public PersistenceManager createPersistenceManager(com.firebase.client.core.Context firebaseContext, String firebaseId) {
        String sessionId = firebaseContext.getSessionPersistenceKey();
        String cacheId = firebaseId + "_" + sessionId;
        if (this.createdPersistenceCaches.contains(cacheId)) {
            throw new FirebaseException("SessionPersistenceKey '" + sessionId + "' has already been used.");
        }
        this.createdPersistenceCaches.add(cacheId);
        return new DefaultPersistenceManager(firebaseContext, new SqlPersistenceStorageEngine(this.applicationContext, firebaseContext, cacheId), new LRUCachePolicy(firebaseContext.getPersistenceCacheSizeBytes()));
    }

    public CredentialStore newCredentialStore(com.firebase.client.core.Context context) {
        return new AndroidCredentialStore(this.applicationContext);
    }
}
