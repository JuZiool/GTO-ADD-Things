package com.juzi.gtoadd.addon;

import com.juzi.gtoadd.GTOAddition;
import com.juzi.gtoadd.common.registry.Registration;
import com.juzi.gtoadd.data.machines.AddMachines;

import com.gregtechceu.gtceu.api.addon.IGTAddon;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;

public class GTOAddAddon implements IGTAddon {

    @Override
    public GTRegistrate getRegistrate() {
        return Registration.REGISTRATE;
    }

    @Override
    public String addonModId() {
        return GTOAddition.MODID;
    }

    @Override
    public void registerMachiness() {
        AddMachines.init();
    }
}
