package com.juzi.gtoadd.common;

import com.juzi.gtoadd.common.registry.Registration;
import com.juzi.gtoadd.config.ConfigHolder;
import com.juzi.gtoadd.data.items.AddItems;
import com.juzi.gtoadd.data.misc.AddCreativeModTabs;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public class CommonProxy {

    public CommonProxy() {
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        eventBus.register(this);
        ConfigHolder.init();
        init();
    }

    public static void init() {
        AddCreativeModTabs.init();
        AddItems.init();
        Registration.REGISTRATE.registerRegistrate();
    }
}
