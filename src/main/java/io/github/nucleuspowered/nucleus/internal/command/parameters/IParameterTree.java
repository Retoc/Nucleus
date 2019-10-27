/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal.command.parameters;

import io.github.nucleuspowered.nucleus.internal.command.ICommandContext;

import java.util.function.Predicate;

public interface IParameterTree<B extends IParameterTree<B>> {

    B flag(IParameterKey<Boolean> key);

    B permissionFlag(IParameterKey<Boolean> key, String permission);

    <T> Flag<B, T> valueFlag();

    <T> Element<B, T> then(Class<T> parameterType);

    IParameterTree.Branch<B, ?> branch();

    interface Branch<P, B extends IParameterTree.Branch<P, B>> {

        B or();

        <T> Element<B, T> then(Class<T> parameterType);

        Branch<B, ?> branch();

        P endBranch();
    }

    interface Element<P, T> {

        <S> Element<P, S> then(Class<S> parameterType);

        Element<P, T> setKey(IParameterKey<T> key);

        Element<P, T> setRequirement(Predicate<ICommandContext<?>> requirement);

        Element<P, T> setOptional(boolean optional);

        Element<P, T> using(IParameterType<? extends T> parameterType);

        IParameterTree.Branch<P, ?> branch();

        P endElement();

    }

    interface Flag<P extends IParameterTree<?>, T> {

        Flag<P, T> setKey(IParameterKey<T> key);

        Flag<P, T> setRequirement(Predicate<ICommandContext<?>> requirement);

        <T> Flag<P, T> value(IParameterType<? extends T> parameterType);

        P endFlag();

    }

}

