/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.services.impl.configurate;

import com.flowpowered.math.vector.Vector3d;
import com.google.common.reflect.TypeToken;
import io.github.nucleuspowered.neutrino.objectmapper.NeutrinoObjectMapperFactory;
import io.github.nucleuspowered.neutrino.typeserialisers.ByteArrayTypeSerialiser;
import io.github.nucleuspowered.neutrino.typeserialisers.IntArrayTypeSerialiser;
import io.github.nucleuspowered.neutrino.typeserialisers.PatternTypeSerialiser;
import io.github.nucleuspowered.neutrino.typeserialisers.SetTypeSerialiser;
import io.github.nucleuspowered.neutrino.typeserialisers.ShortArrayTypeSerialiser;
import io.github.nucleuspowered.nucleus.configurate.typeserialisers.InstantTypeSerialiser;
import io.github.nucleuspowered.nucleus.configurate.typeserialisers.MailMessageSerialiser;
import io.github.nucleuspowered.nucleus.configurate.typeserialisers.NamedLocationSerialiser;
import io.github.nucleuspowered.nucleus.configurate.typeserialisers.NucleusItemStackSnapshotSerialiser;
import io.github.nucleuspowered.nucleus.configurate.typeserialisers.NucleusTextTemplateTypeSerialiser;
import io.github.nucleuspowered.nucleus.configurate.typeserialisers.Vector3dTypeSerialiser;
import io.github.nucleuspowered.nucleus.configurate.typeserialisers.WarpCategorySerialiser;
import io.github.nucleuspowered.nucleus.configurate.typeserialisers.WarpSerialiser;
import io.github.nucleuspowered.nucleus.configurate.wrappers.NucleusItemStackSnapshot;
import io.github.nucleuspowered.nucleus.internal.TypeTokens;
import io.github.nucleuspowered.nucleus.services.IConfigurateHelper;
import io.github.nucleuspowered.nucleus.services.IMessageProviderService;
import io.github.nucleuspowered.nucleus.services.impl.storage.DataObjectTranslator;
import io.github.nucleuspowered.nucleus.services.impl.storage.dataobjects.configurate.AbstractConfigurateBackedDataObject;
import io.github.nucleuspowered.nucleus.services.impl.texttemplatefactory.NucleusTextTemplateImpl;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializerCollection;

import java.time.Instant;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ConfigurateHelper implements IConfigurateHelper {

    private final IMessageProviderService messageProvider;

    private static final TypeToken<AbstractConfigurateBackedDataObject> ABSTRACT_DATA_OBJECT_TYPE_TOKEN = TypeToken.of(
            AbstractConfigurateBackedDataObject.class);

    private boolean collectionCompleted = false;
    private final TypeSerializerCollection typeSerializerCollection = ConfigurationOptions.defaults().getSerializers().newChild();
    private final NeutrinoObjectMapperFactory objectMapperFactory;
    private final Pattern commentPattern = Pattern.compile("^(loc:)?(?<key>([a-zA-Z0-9_-]+\\.?)+)$");

    @Inject
    public ConfigurateHelper(IMessageProviderService messageProvider) {
        this.messageProvider = messageProvider;
        this.objectMapperFactory = NeutrinoObjectMapperFactory.builder()
                .setCommentProcessor(setting -> {
                    String comment = setting.comment();
                    if (comment.contains(".") && !comment.contains(" ")) {
                        Matcher matcher = this.commentPattern.matcher(comment);

                        if (matcher.matches()) {
                            return this.messageProvider.getMessageString(matcher.group("key"));
                        }
                    }

                    return comment;
                }).build(true);
    }

    /**
     * Set NucleusPlugin specific options on the {@link ConfigurationOptions}
     *
     * @param options The {@link ConfigurationOptions} to alter.
     * @return The {@link ConfigurationOptions}, for easier inline use of this function.
     */
    @Override public ConfigurationOptions setOptions(ConfigurationOptions options) {
        TypeSerializerCollection tsc = getNucleusTypeSerialiserCollection();

        // Allows us to use localised comments and @ProcessSetting annotations
        return options.setSerializers(tsc).setObjectMapperFactory(objectMapperFactory);
    }

    @Override public TypeSerializerCollection getNucleusTypeSerialiserCollection() {
        if (this.collectionCompleted) {
            return this.typeSerializerCollection;
        }

        this.collectionCompleted = true;

        // Custom type serialisers for Nucleus
        typeSerializerCollection.registerType(TypeToken.of(Vector3d.class), new Vector3dTypeSerialiser());
        typeSerializerCollection.registerType(TypeToken.of(NucleusItemStackSnapshot.class), new NucleusItemStackSnapshotSerialiser());
        typeSerializerCollection.registerType(TypeToken.of(Pattern.class), new PatternTypeSerialiser());
        typeSerializerCollection.registerType(TypeToken.of(NucleusTextTemplateImpl.class), new NucleusTextTemplateTypeSerialiser());
        typeSerializerCollection.registerPredicate(
                typeToken -> Set.class.isAssignableFrom(typeToken.getRawType()),
                new SetTypeSerialiser()
        );

        typeSerializerCollection.registerType(new TypeToken<byte[]>(){}, new ByteArrayTypeSerialiser());
        typeSerializerCollection.registerType(new TypeToken<short[]>(){}, new ShortArrayTypeSerialiser());
        typeSerializerCollection.registerType(new TypeToken<int[]>(){}, new IntArrayTypeSerialiser());
        typeSerializerCollection.registerType(TypeToken.of(Instant.class), new InstantTypeSerialiser());

        typeSerializerCollection.registerPredicate(x -> x.isSubtypeOf(ABSTRACT_DATA_OBJECT_TYPE_TOKEN), DataObjectTranslator.INSTANCE);
        typeSerializerCollection.registerType(TypeTokens.WARP, WarpSerialiser.INSTANCE);
        typeSerializerCollection.registerType(TypeTokens.WARP_CATEGORY, new WarpCategorySerialiser());
        typeSerializerCollection.registerType(TypeTokens.NAMEDLOCATION, new NamedLocationSerialiser());
        typeSerializerCollection.registerType(TypeTokens.MAIL_MESSAGE, new MailMessageSerialiser());

        return typeSerializerCollection;
    }
}
