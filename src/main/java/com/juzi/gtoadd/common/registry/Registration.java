package com.juzi.gtoadd.common.registry;

import com.juzi.gtoadd.GTOAddition;

import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;

import net.minecraft.world.item.CreativeModeTab;

import com.tterrag.registrate.util.entry.RegistryEntry;

public class Registration {

    public static GTRegistrate REGISTRATE = GTRegistrate.create(GTOAddition.MODID);

    static {
        REGISTRATE.creativeModeTab((RegistryEntry<CreativeModeTab>) null);
    }
}
