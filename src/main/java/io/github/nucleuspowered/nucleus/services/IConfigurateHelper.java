package io.github.nucleuspowered.nucleus.services;

import com.google.inject.ImplementedBy;
import io.github.nucleuspowered.nucleus.services.impl.configurate.ConfigurateHelper;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializerCollection;

@ImplementedBy(ConfigurateHelper.class)
public interface IConfigurateHelper {

    ConfigurationOptions setOptions(ConfigurationOptions options);

    TypeSerializerCollection getNucleusTypeSerialiserCollection();
}
