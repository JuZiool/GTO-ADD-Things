package com.juzi.gtoadd.data.machines;

import com.juzi.gtoadd.common.registry.Registration;
import com.juzi.gtoadd.data.misc.AddCreativeModTabs;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.TieredEnergyMachine;
import com.gregtechceu.gtceu.api.registry.registrate.MachineBuilder;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;

import java.util.Locale;
import java.util.function.BiFunction;

import static com.juzi.gtoadd.common.registry.Registration.REGISTRATE;

public class AddMachines {

    static {
        REGISTRATE.creativeModeTab(() -> AddCreativeModTabs.GTOAddMachines);
    }

    public static MachineDefinition[] TEST_MACHINES;

    static {
        TEST_MACHINES = registerTieredMachines("test_machine", TieredEnergyMachine::new, (i, mb) -> mb
                .langValue("测试注册用机器")
                .tier(i)
                .recipeType(GTRecipeTypes.FORMING_PRESS_RECIPES)
                .register(),
                GTValues.LV, GTValues.MV, GTValues.HV);
    }

    public static void init() {}

    public static MachineDefinition[] registerTieredMachines(String name, BiFunction<MetaMachineBlockEntity, Integer, MetaMachine> factory, BiFunction<Integer, MachineBuilder<MachineDefinition>, MachineDefinition> builder, int... tiers) {
        MachineDefinition[] definitions = new MachineDefinition[GTValues.TIER_COUNT];

        for (int tier : tiers) {
            MachineBuilder<MachineDefinition> register = Registration.REGISTRATE.machine(GTValues.VN[tier].toLowerCase(Locale.ROOT) + "_" + name, (holder) -> (MetaMachine) factory.apply(holder, tier)).tier(tier);
            definitions[tier] = (MachineDefinition) builder.apply(tier, register);
        }

        return definitions;
    }
}
