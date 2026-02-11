package com.juzi.gtoadd.data.items;

import com.juzi.gtoadd.common.item.BambooCopterItem;
import com.juzi.gtoadd.common.item.MaintenanceToolkitItem;
import com.juzi.gtoadd.data.misc.AddCreativeModTabs;

import com.tterrag.registrate.util.entry.ItemEntry;

import static com.juzi.gtoadd.common.registry.Registration.REGISTRATE;

public class AddItems {

    static {
        REGISTRATE.creativeModeTab(() -> AddCreativeModTabs.GTOAddItems);
    }

    public static ItemEntry<BambooCopterItem> BambooCopter = REGISTRATE.item("bamboo_copter", BambooCopterItem::new)
            .lang("竹蜻蜓")
            .register();
    public static ItemEntry<MaintenanceToolkitItem> MaintenanceToolkit = REGISTRATE.item("maintenance_toolkit", MaintenanceToolkitItem::new)
            .lang("维护工具箱")
            .register();

    public static void init() {}
}
