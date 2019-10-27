/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.util;

import io.github.nucleuspowered.nucleus.Nucleus;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.event.cause.EventContextKey;

public class CauseStackHelper {

    private CauseStackHelper() {}

    public static <T, X extends Throwable> T createFrameWithCausesWithReturn(ThrownFunction<Cause, T, X> function, Object... causeStack) throws X {
        try (CauseStackManager.StackFrame csf = createFrameWithCauses(causeStack)) {
            return function.apply(Sponge.getCauseStackManager().getCurrentCause());
        }
    }

    public static <X extends Throwable> void createFrameWithCausesWithConsumer(ThrownConsumer<Cause, X> supplier, Object... causeStack) throws X {
        try (CauseStackManager.StackFrame csf = createFrameWithCauses(causeStack)) {
            supplier.accept(Sponge.getCauseStackManager().getCurrentCause());
        }
    }

    public static CauseStackManager.StackFrame createFrameWithCauses(Object... causeStack) {
        // Get a new CauseStack
        return createFrameWithCauses(EventContext.empty(), causeStack);
    }

    @SuppressWarnings("unchecked")
    public static CauseStackManager.StackFrame createFrameWithCauses(EventContext eventContext, Object... causeStack) {
        // Get a new CauseStack
        CauseStackManager.StackFrame csf = Sponge.getCauseStackManager().pushCauseFrame();
        eventContext.asMap().forEach((context, object) -> Sponge.getCauseStackManager().addContext((EventContextKey)context, object));

        if (causeStack.length > 0) {
            for (int i = causeStack.length - 1; i >= 0; i--) {
                Sponge.getCauseStackManager().pushCause(causeStack[i]);
            }
        }

        return csf;
    }

    public static Cause createCause(Object... causeStack) {
        return createCause(EventContext.empty(), causeStack);
    }

    public static Cause createCause(EventContext eventContext, Object... causeStack) {
        if (Sponge.getServer().isMainThread()) {
            try (CauseStackManager.StackFrame frame = createFrameWithCauses(eventContext, causeStack)) {
                return Sponge.getCauseStackManager().getCurrentCause();
            }
        }

        Cause.Builder cb = Cause.builder();
        if (causeStack.length > 0) {
            for (Object cause : causeStack) {
                cb.append(cause);
            }
        } else {
            cb.append(Nucleus.getNucleus());
        }

        return cb.build(eventContext);
    }

}
