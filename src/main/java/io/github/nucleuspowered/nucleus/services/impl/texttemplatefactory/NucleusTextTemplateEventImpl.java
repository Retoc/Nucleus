/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.services.impl.texttemplatefactory;

import com.google.common.collect.ImmutableSet;
import io.github.nucleuspowered.nucleus.api.events.NucleusTextTemplateEvent;
import io.github.nucleuspowered.nucleus.api.text.NucleusTextTemplate;
import io.github.nucleuspowered.nucleus.services.INucleusTextTemplateFactory;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.impl.AbstractEvent;

import java.util.Collection;

class NucleusTextTemplateEventImpl extends AbstractEvent implements NucleusTextTemplateEvent {

    private boolean cancelled = false;
    private final NucleusTextTemplate originalMessage;
    private NucleusTextTemplate message;
    private final INucleusTextTemplateFactory textTemplateFactory;
    private final ImmutableSet<CommandSource> originalMembers;
    private ImmutableSet<CommandSource> members;
    private final Cause cause;

    NucleusTextTemplateEventImpl(
            NucleusTextTemplate originalMessage,
            Collection<CommandSource> originalMembers,
            INucleusTextTemplateFactory textTemplateFactory,
            Cause cause) {
        this.originalMessage = originalMessage;
        this.message = originalMessage;
        this.originalMembers = ImmutableSet.copyOf(originalMembers);
        this.members = this.originalMembers;
        this.cause = cause;
        this.textTemplateFactory = textTemplateFactory;
    }

    @Override
    public NucleusTextTemplate getMessage() {
        return this.message;
    }

    @Override
    public NucleusTextTemplate getOriginalMessage() {
        return this.originalMessage;
    }

    @Override
    public void setMessage(NucleusTextTemplate message) {
        this.message = message;
    }

    @Override public void setMessage(String message) {
        try {
            this.textTemplateFactory.createFromString(message);
        } catch (Throwable throwable) {
            throw new IllegalArgumentException("Could not create text", throwable);
        }
    }

    @Override public Collection<CommandSource> getOriginalRecipients() {
        return this.originalMembers;
    }

    @Override
    public Collection<CommandSource> getRecipients() {
        return this.members;
    }

    @Override
    public void setRecipients(Collection<? extends CommandSource> recipients) {
        this.members = ImmutableSet.copyOf(recipients);
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @Override
    @NonNull
    public Cause getCause() {
        return this.cause;
    }

    static class Broadcast extends NucleusTextTemplateEventImpl implements NucleusTextTemplateEvent.Broadcast {

        Broadcast(NucleusTextTemplate originalMessage,
                Collection<CommandSource> originalMembers,
                INucleusTextTemplateFactory factory,
                Cause cause) {
            super(originalMessage, originalMembers, factory, cause);
        }
    }
}
