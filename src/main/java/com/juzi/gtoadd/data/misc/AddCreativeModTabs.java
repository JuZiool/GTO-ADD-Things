package com.juzi.gtoadd.data.misc;

import com.juzi.gtoadd.GTOAddition;

import com.gregtechceu.gtceu.common.data.GTCreativeModeTabs;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;

import com.tterrag.registrate.util.entry.RegistryEntry;

import static com.juzi.gtoadd.common.registry.Registration.REGISTRATE;
import static com.juzi.gtoadd.data.items.AddItems.BambooCopter;

public class AddCreativeModTabs {

    public static RegistryEntry<CreativeModeTab> GTOAddItems;

    static {
        GTOAddItems = REGISTRATE.defaultCreativeTab("items", builder -> builder
                .withTabsBefore(CreativeModeTabs.COMBAT)
                .icon(BambooCopter.asItem()::getDefaultInstance)
                .displayItems(new GTCreativeModeTabs.RegistrateDisplayItemsGenerator("items", REGISTRATE))
                .title(REGISTRATE.addLang("itemGroup", GTOAddition.id("items"), "GTOAdd|Items"))).register();
    }

    public static void init() {}
}
