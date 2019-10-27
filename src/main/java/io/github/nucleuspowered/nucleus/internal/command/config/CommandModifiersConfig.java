/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal.command.config;

import ninja.leaping.configurate.ConfigurationNode;

public class CommandModifiersConfig {

    private double cost = -1;
    private int warmup = -1;
    private int cooldown = -1;

    public double getCost() {
        return this.cost;
    }

    public int getWarmup() {
        return this.warmup;
    }

    public int getCooldown() {
        return this.cooldown;
    }

    public void setCost(double cost) {
        this.cost = Math.max(0, cost);
    }

    public void setWarmup(int warmup) {
        this.warmup = Math.max(0, warmup);
    }

    public void setCooldown(int cooldown) {
        this.cooldown = Math.max(0, cooldown);
    }

}
