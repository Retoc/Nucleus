/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.tests;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.config.CommandsConfig;
import io.github.nucleuspowered.nucleus.dataservices.KitDataService;
import io.github.nucleuspowered.nucleus.dataservices.NameBanService;
import io.github.nucleuspowered.nucleus.internal.interfaces.Reloadable;
import io.github.nucleuspowered.nucleus.internal.text.TextParsingUtils;
import io.github.nucleuspowered.nucleus.modules.core.config.WarmupConfig;
import io.github.nucleuspowered.nucleus.quickstart.NucleusConfigAdapter;
import io.github.nucleuspowered.nucleus.quickstart.module.StandardModule;
import io.github.nucleuspowered.nucleus.services.IPermissionService;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.FormattingCodeTextSerializer;
import org.spongepowered.api.text.serializer.SafeTextSerializer;
import org.spongepowered.api.text.serializer.TextSerializers;
import uk.co.drnaylor.quickstart.holders.DiscoveryModuleHolder;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Sponge.class)
public abstract class TestBase {

    private static boolean complete = false;

    private static void setFinalStatic(Field field) throws Exception {
        field.setAccessible(true);

        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
    }

    private static void setFinalStaticPlain(Field field) throws Exception {
        setFinalStatic(field);
        SafeTextSerializer sts = Mockito.mock(SafeTextSerializer.class);
        Mockito.when(sts.serialize(Mockito.any())).thenReturn("key");
        Mockito.when(sts.deserialize(Mockito.any())).thenReturn(Text.of("key"));
        field.set(null, sts);
    }

    private static void setFinalStaticFormatters(Field field) throws Exception {
        setFinalStatic(field);
        FormattingCodeTextSerializer sts = Mockito.mock(FormattingCodeTextSerializer.class);
        Mockito.when(sts.serialize(Mockito.any())).thenReturn("key");
        Mockito.when(sts.deserialize(Mockito.any())).thenReturn(Text.of("key"));
        Mockito.when(sts.stripCodes(Mockito.anyString())).thenReturn("test");
        Mockito.when(sts.replaceCodes(Mockito.anyString(), Mockito.anyChar())).thenReturn("test");
        field.set(null, sts);
    }

    @BeforeClass
    public static void testSetup() throws Exception {
        if (complete) {
            return;
        }

        complete = true;
        try {
            Method m = Nucleus.class.getDeclaredMethod("setNucleus", Nucleus.class);
            m.setAccessible(true);
            m.invoke(null, new NucleusTest());
        } catch (IllegalStateException e) {
            // Nope
        }

        setFinalStaticPlain(TextSerializers.class.getField("PLAIN"));
        setFinalStaticFormatters(TextSerializers.class.getField("FORMATTING_CODE"));
        setFinalStaticFormatters(TextSerializers.class.getField("LEGACY_FORMATTING_CODE"));
    }

    public static void setupSpongeMock() {
        Cause mockCause = Cause.of(EventContext.empty(), "test");
        CauseStackManager csm = Mockito.mock(CauseStackManager.class);
        Mockito.when(csm.getCurrentCause()).thenReturn(mockCause);
        PowerMockito.mockStatic(Sponge.class);
        PowerMockito.when(Sponge.getCauseStackManager()).thenReturn(csm);
    }

    private static class NucleusTest extends Nucleus {

        @Override
        public void addX(List<Text> messages, int spacing) {
            // NOOP
        }

        @Override
        public void saveData() {

        }

        @Override
        public Logger getLogger() {
            return LoggerFactory.getLogger("test");
        }

        @Override public Path getConfigDirPath() {
            return null;
        }

        @Override public Path getDataPath() {
            return null;
        }

        @Override
        public boolean reload() {
            return true;
        }

        @Override public boolean reloadMessages() {
            return true;
        }

        @Override public WarmupConfig getWarmupConfig() {
            return null;
        }

        @Override
        public DiscoveryModuleHolder<StandardModule, StandardModule> getModuleHolder() {
            return null;
        }

        @Override public <T extends NucleusConfigAdapter<?>> Optional<T> getConfigAdapter(String id, Class<T> configAdapterClass) {
            return Optional.empty();
        }

        @Override public Optional<Instant> getGameStartedTime() {
            return Optional.empty();
        }

        public TextParsingUtils getTextParsingUtils() {
            return null;
        }

        @Override public void registerReloadable(Reloadable reloadable) {

        }

        @Override public KitDataService getKitDataService() {
            return null;
        }

        @Override public NameBanService getNameBanService() {
            return null;
        }

        @Override public CommandsConfig getCommandsConfig() {
            return null;
        }

        @Override public IPermissionService getPermissionResolver() {
            return null;
        }

        @Override public boolean isServer() {
            return true;
        }

        @Override public void addStartupMessage(Text message) {
            // NOOP
        }

        @Override public boolean isPrintingSavesAndLoads() {
            return false;
        }

    }
}
