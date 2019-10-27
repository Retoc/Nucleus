/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.services;

import com.google.inject.ImplementedBy;
import io.github.nucleuspowered.nucleus.api.service.NucleusTextTemplateFactory;
import io.github.nucleuspowered.nucleus.services.impl.texttemplatefactory.NucleusTextTemplateFactoryImpl;
import io.github.nucleuspowered.nucleus.services.impl.texttemplatefactory.NucleusTextTemplateImpl;
import org.spongepowered.api.text.Text;

@ImplementedBy(NucleusTextTemplateFactoryImpl.class)
public interface INucleusTextTemplateFactory extends NucleusTextTemplateFactory {

    NucleusTextTemplateImpl createFromAmpersandString(String string);

    NucleusTextTemplateImpl createFromAmpersandString(String string, Text prefix, Text suffix);
}
