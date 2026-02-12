package com.juzi.gtoadd;

import com.juzi.gtoadd.addon.GTOAddAddon;
import com.juzi.gtoadd.common.CommonProxy;

import com.gregtechceu.gtceu.api.addon.AddonFinder;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

@Mod(GTOAddition.MODID)
public class GTOAddition {

    public static final String MODID = "gtoadd";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }

    public GTOAddition() {
        AddonFinder.add(new GTOAddAddon());
        DistExecutor.safeRunForDist(() -> ClientProxy::new, () -> CommonProxy::new);
    }
}
