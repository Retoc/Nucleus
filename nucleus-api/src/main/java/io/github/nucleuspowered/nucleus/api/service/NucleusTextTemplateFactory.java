/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.api.service;

import io.github.nucleuspowered.nucleus.api.exceptions.NucleusException;
import io.github.nucleuspowered.nucleus.api.text.NucleusTextTemplate;

public interface NucleusTextTemplateFactory {

    /**
     * Creates a {@link NucleusTextTemplate} from a string, which could be either Json or Ampersand formatted.
     *
     * @param string The string to register.
     * @return The {@link NucleusTextTemplate} that can be parsed.
     * @throws NucleusException thrown if the string could not be parsed.
     */
    NucleusTextTemplate createFromString(String string) throws Throwable;

}
