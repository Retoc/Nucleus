/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.quickstart;

import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import io.github.nucleuspowered.nucleus.quickstart.module.StandardModule;
import io.github.nucleuspowered.nucleus.services.INucleusServiceCollection;
import uk.co.drnaylor.quickstart.exceptions.QuickStartModuleLoaderException;
import uk.co.drnaylor.quickstart.holders.DiscoveryModuleHolder;
import uk.co.drnaylor.quickstart.loaders.ModuleConstructor;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class QuickStartModuleConstructor implements ModuleConstructor<StandardModule> {

    private final Key<Supplier<DiscoveryModuleHolder<? ,?>>> holderKey = Key.get(
            new TypeLiteral<Supplier<DiscoveryModuleHolder<? ,?>>>() {});
    private final Map<String, Map<String, List<String>>> moduleList;
    private final INucleusServiceCollection serviceCollection;

    public QuickStartModuleConstructor(Map<String, Map<String, List<String>>> m, INucleusServiceCollection serviceCollection) {
         this.moduleList = m;
         this.serviceCollection = serviceCollection;
    }

    @Override
    public StandardModule constructModule(Class<? extends StandardModule> moduleClass) throws QuickStartModuleLoaderException.Construction {
        return constructInternal(moduleClass);
    }

    public <T extends StandardModule> T constructInternal(Class<T> moduleClass) throws QuickStartModuleLoaderException.Construction {
        T m;
        try {
            try {
                Constructor<T> s = moduleClass.getDeclaredConstructor(Supplier.class, INucleusServiceCollection.class);
                m = s.newInstance(this.serviceCollection.injector().getInstance(this.holderKey), this.serviceCollection);
            } catch (Throwable e) {
                // then try injection
                m = this.serviceCollection.injector().getInstance(moduleClass);
            }
        } catch (Exception e) {
            throw new QuickStartModuleLoaderException.Construction(moduleClass, "Could not instantiate module!", e);
        }

        m.init(this.moduleList.get(moduleClass.getName()));
        return m;
    }
}
