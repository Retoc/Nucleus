/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.services;

import com.google.inject.ImplementedBy;
import io.github.nucleuspowered.nucleus.api.service.NucleusMessageTokenService;
import io.github.nucleuspowered.nucleus.services.impl.messagetoken.NucleusTokenServiceImpl;
import io.github.nucleuspowered.nucleus.services.impl.messagetoken.Tokens;

@ImplementedBy(NucleusTokenServiceImpl.class)
public interface IMessageTokenService extends NucleusMessageTokenService {

    Tokens getNucleusTokenParser();

    String performReplacements(String string);
}
