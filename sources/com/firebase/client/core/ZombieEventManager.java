package com.firebase.client.core;

import com.firebase.client.annotations.NotNull;
import com.firebase.client.core.view.QuerySpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class ZombieEventManager implements EventRegistrationZombieListener {
    static final /* synthetic */ boolean $assertionsDisabled = (!ZombieEventManager.class.desiredAssertionStatus());
    private static ZombieEventManager defaultInstance = new ZombieEventManager();
    final HashMap<EventRegistration, List<EventRegistration>> globalEventRegistrations = new HashMap<>();

    private ZombieEventManager() {
    }

    @NotNull
    public static ZombieEventManager getInstance() {
        return defaultInstance;
    }

    public void recordEventRegistration(EventRegistration registration) {
        synchronized (this.globalEventRegistrations) {
            List<EventRegistration> registrationList = (List) this.globalEventRegistrations.get(registration);
            if (registrationList == null) {
                registrationList = new ArrayList<>();
                this.globalEventRegistrations.put(registration, registrationList);
            }
            registrationList.add(registration);
            if (!registration.getQuerySpec().isDefault()) {
                EventRegistration defaultRegistration = registration.clone(QuerySpec.defaultQueryAtPath(registration.getQuerySpec().getPath()));
                List<EventRegistration> registrationList2 = (List) this.globalEventRegistrations.get(defaultRegistration);
                if (registrationList2 == null) {
                    registrationList2 = new ArrayList<>();
                    this.globalEventRegistrations.put(defaultRegistration, registrationList2);
                }
                registrationList2.add(registration);
            }
            registration.setIsUserInitiated(true);
            registration.setOnZombied(this);
        }
    }

    private void unRecordEventRegistration(EventRegistration zombiedRegistration) {
        synchronized (this.globalEventRegistrations) {
            boolean found = false;
            List<EventRegistration> registrationList = (List) this.globalEventRegistrations.get(zombiedRegistration);
            if (registrationList != null) {
                int i = 0;
                while (true) {
                    if (i >= registrationList.size()) {
                        break;
                    } else if (registrationList.get(i) == zombiedRegistration) {
                        found = true;
                        registrationList.remove(i);
                        break;
                    } else {
                        i++;
                    }
                }
                if (registrationList.isEmpty()) {
                    this.globalEventRegistrations.remove(zombiedRegistration);
                }
            }
            if (!$assertionsDisabled && !found && zombiedRegistration.isUserInitiated()) {
                throw new AssertionError();
            } else if (!zombiedRegistration.getQuerySpec().isDefault()) {
                EventRegistration defaultRegistration = zombiedRegistration.clone(QuerySpec.defaultQueryAtPath(zombiedRegistration.getQuerySpec().getPath()));
                List<EventRegistration> registrationList2 = (List) this.globalEventRegistrations.get(defaultRegistration);
                if (registrationList2 != null) {
                    int i2 = 0;
                    while (true) {
                        if (i2 >= registrationList2.size()) {
                            break;
                        } else if (registrationList2.get(i2) == zombiedRegistration) {
                            registrationList2.remove(i2);
                            break;
                        } else {
                            i2++;
                        }
                    }
                    if (registrationList2.isEmpty()) {
                        this.globalEventRegistrations.remove(defaultRegistration);
                    }
                }
            }
        }
    }

    public void zombifyForRemove(EventRegistration registration) {
        synchronized (this.globalEventRegistrations) {
            List<EventRegistration> registrationList = (List) this.globalEventRegistrations.get(registration);
            if (registrationList != null && !registrationList.isEmpty()) {
                if (registration.getQuerySpec().isDefault()) {
                    HashSet<QuerySpec> zombiedQueries = new HashSet<>();
                    for (int i = registrationList.size() - 1; i >= 0; i--) {
                        EventRegistration currentRegistration = (EventRegistration) registrationList.get(i);
                        if (!zombiedQueries.contains(currentRegistration.getQuerySpec())) {
                            zombiedQueries.add(currentRegistration.getQuerySpec());
                            currentRegistration.zombify();
                        }
                    }
                } else {
                    ((EventRegistration) registrationList.get(0)).zombify();
                }
            }
        }
    }

    public void onZombied(EventRegistration zombiedInstance) {
        unRecordEventRegistration(zombiedInstance);
    }
}
