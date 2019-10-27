/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.services;

import com.google.inject.ImplementedBy;
import io.github.nucleuspowered.nucleus.api.service.NucleusWarmupManagerService;
import io.github.nucleuspowered.nucleus.services.impl.warmup.WarmupService;

@ImplementedBy(WarmupService.class)
public interface IWarmupService extends NucleusWarmupManagerService { }
