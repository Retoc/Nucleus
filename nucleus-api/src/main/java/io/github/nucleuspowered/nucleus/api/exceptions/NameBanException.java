/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.api.exceptions;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.text.Text;

public class NameBanException extends CommandException {

    private final Reasons reason;

    public NameBanException(Text message, Reasons reasons) {
        super(message);
        this.reason = reasons;
    }

    public enum Reasons {
        DISALLOWED_NAME
    }
}
