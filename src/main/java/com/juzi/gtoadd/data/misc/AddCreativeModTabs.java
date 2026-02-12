package com.juzi.gtoadd.data.misc;

import com.juzi.gtoadd.GTOAddition;

import com.gregtechceu.gtceu.common.data.GTCreativeModeTabs;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Items;

import com.tterrag.registrate.util.entry.RegistryEntry;

import static com.juzi.gtoadd.common.registry.Registration.REGISTRATE;
import static com.juzi.gtoadd.data.items.AddItems.BambooCopter;

public class AddCreativeModTabs {

    public static RegistryEntry<CreativeModeTab> GTOAddItems;
    public static RegistryEntry<CreativeModeTab> GTOAddMachines;

    static {
        GTOAddItems = REGISTRATE.defaultCreativeTab("items", builder -> builder
                .withTabsBefore(CreativeModeTabs.COMBAT)
                .icon(BambooCopter.asItem()::getDefaultInstance)
                .displayItems(new GTCreativeModeTabs.RegistrateDisplayItemsGenerator("items", REGISTRATE))
                .title(REGISTRATE.addLang("itemGroup", GTOAddition.id("items"), "GTOAdd|Items"))).register();
        GTOAddMachines = REGISTRATE.defaultCreativeTab("machines", builder -> builder
                .withTabsBefore(CreativeModeTabs.COMBAT)
                .icon(Items.COMMAND_BLOCK::getDefaultInstance)
                .displayItems(new GTCreativeModeTabs.RegistrateDisplayItemsGenerator("machines", REGISTRATE))
                .title(REGISTRATE.addLang("itemGroup", GTOAddition.id("machines"), "GTOAdd|Machines"))).register();
    }

    public static void init() {}
}
