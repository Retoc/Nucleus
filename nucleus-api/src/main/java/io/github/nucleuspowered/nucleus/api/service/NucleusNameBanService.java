/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.api.service;

import io.github.nucleuspowered.nucleus.api.exceptions.NameBanException;
import io.github.nucleuspowered.nucleus.api.exceptions.NucleusException;
import org.spongepowered.api.event.cause.Cause;

import java.util.Optional;

/**
 * Created by Daniel on 26/02/2017.
 */
public interface NucleusNameBanService {

    boolean addName(String name, String reason, Cause cause) throws NameBanException;

    Optional<String> getReasonForBan(String name);

    boolean removeName(String name, Cause cause) throws NameBanException;
}
