package com.gtoaddthings;

import com.gtoaddthings.item.BambooCopterItem;
import com.gtoaddthings.item.MaintenanceToolkitItem;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

@Mod(GTOAddThings.MODID)
public class GTOAddThings
{
    public static final String MODID = "gtoaddthings";
    public static final Logger LOGGER = LogUtils.getLogger();

    // 物品注册
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    // 创造标签页注册
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    // 竹蜻蜓 - 头部饰品
    public static final RegistryObject<Item> BAMBOO_COPTER = ITEMS.register("bamboo_copter", BambooCopterItem::new);
    
    // 维护工具箱 - 用于修复维护仓
    public static final RegistryObject<Item> MAINTENANCE_TOOLKIT = ITEMS.register("maintenance_toolkit", MaintenanceToolkitItem::new);

    // 创造标签页 - 使用竹蜻蜓作为图标
    public static final RegistryObject<CreativeModeTab> GTOADDTHINGS_TAB = CREATIVE_MODE_TABS.register("gtoaddthings_tab", () -> CreativeModeTab.builder()
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> BAMBOO_COPTER.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(BAMBOO_COPTER.get());
                output.accept(MAINTENANCE_TOOLKIT.get());
            }).build());

    public GTOAddThings(FMLJavaModLoadingContext context)
    {
        IEventBus modEventBus = context.getModEventBus();

        modEventBus.addListener(this::commonSetup);

        ITEMS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(this);

        context.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
        LOGGER.info("GTO AddThings 模组已加载");
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
        LOGGER.info("GTO AddThings 服务器启动");
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
            LOGGER.info("GTO AddThings 客户端设置完成");
        }
    }
}
