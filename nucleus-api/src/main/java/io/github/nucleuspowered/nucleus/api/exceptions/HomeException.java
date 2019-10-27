/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.api.exceptions;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.text.Text;

/**
 * Thrown when a home related action fails.
 */
public class HomeException extends CommandException {

    private final Reasons reason;

    public HomeException(Text message, Reasons reasons) {
        super(message);
        this.reason = reasons;
    }

    public HomeException(Text message, Throwable cause, Reasons reasons) {
        super(message, cause);
        this.reason = reasons;
    }

    public Reasons getReason() {
        return this.reason;
    }

    public enum Reasons {

        /**
         * The home point does not exists
         */
        DOES_NOT_EXIST,

        /**
         * The home name is invalid
         */
        INVALID_NAME,

        /**
         * The home exists, but the location is not valid. Usually
         * due to a world not being loaded, or having been removed.
         */
        INVALID_LOCATION,

        /**
         * The maximum number of permitted homes has already
         * been allocated.
         */
        LIMIT_REACHED,

        /**
         * A plugin cancelled the event.
         */
        PLUGIN_CANCELLED,

        /**
         * An unknown error occurred.
         */
        UNKNOWN;
    }

}
