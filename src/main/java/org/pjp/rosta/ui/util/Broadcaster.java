package org.pjp.rosta.ui.util;

import java.util.LinkedList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import com.vaadin.flow.shared.Registration;

public final class Broadcaster {
    private static final Executor EXECUTOR = Executors.newSingleThreadExecutor();

    private static final LinkedList<Consumer<RostaMessage>> LISTENERS = new LinkedList<>();

    public static synchronized Registration register(Consumer<RostaMessage> listener) {
        LISTENERS.add(listener);

        return () -> {
            synchronized (Broadcaster.class) {
                LISTENERS.remove(listener);
            }
        };
    }

    public static synchronized void broadcast(RostaMessage message) {
        for (Consumer<RostaMessage> listener : LISTENERS) {
            EXECUTOR.execute(() -> listener.accept(message));
        }
    }

    private Broadcaster() {
        // prevent instantiation
    }
}