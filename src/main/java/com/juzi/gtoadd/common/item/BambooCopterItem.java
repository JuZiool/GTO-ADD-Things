package com.juzi.gtoadd.common.item;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BambooCopterItem extends Item implements ICurioItem {

    // 10分钟 = 12000 ticks (20 ticks/秒 * 60秒 * 10分钟)
    private static final int DURABILITY_CONSUME_INTERVAL_NORMAL = 12000;
    // 3分钟 = 3600 ticks (20 ticks/秒 * 60秒 * 3分钟)
    private static final int DURABILITY_CONSUME_INTERVAL_LOW = 3600;
    // 低耐久阈值（剩余3点或以下）
    private static final int LOW_DURABILITY_THRESHOLD = 3;
    private static final String FLIGHT_TIME_TAG = "FlightTime";

    // 存储每个玩家的飞行时间
    private static final Map<UUID, Integer> playerFlightTime = new HashMap<>();

    public BambooCopterItem(Properties properties) {
        super(properties.stacksTo(1).durability(63)); // 63点耐久
        // 注册事件监听
        MinecraftForge.EVENT_BUS.register(this);
    }

    /**
     * 允许从使用键直接装备
     */
    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        return true;
    }

    /**
     * 装备时给予飞行能力
     */
    @Override
    public void onEquip(SlotContext slotContext, ItemStack prevStack, ItemStack stack) {
        if (slotContext.entity() instanceof ServerPlayer player) {
            player.getAbilities().mayfly = true;
            player.onUpdateAbilities();

            // 初始化飞行时间
            if (!stack.hasTag()) {
                stack.setTag(new CompoundTag());
            }
            if (!stack.getTag().contains(FLIGHT_TIME_TAG)) {
                stack.getTag().putInt(FLIGHT_TIME_TAG, 0);
            }
        }
    }

    /**
     * 卸下时移除飞行能力
     */
    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
        if (slotContext.entity() instanceof ServerPlayer player) {
            // 只有在没有装备其他竹蜻蜓时才移除飞行能力
            if (!hasBambooCopterEquipped(player)) {
                player.getAbilities().mayfly = false;
                player.getAbilities().flying = false;
                player.onUpdateAbilities();
            }

            // 清除玩家飞行时间记录
            playerFlightTime.remove(player.getUUID());
        }
    }

    /**
     * 检查玩家是否装备了竹蜻蜓
     */
    private boolean hasBambooCopterEquipped(ServerPlayer player) {
        // 通过Curios API检查头部槽位
        return GetHeadCurios(player);
    }

    /**
     * 每tick检查飞行时间并消耗耐久
     */
    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer player)) return;

        // 玩家正在飞行且装备了竹蜻蜓
        if (player.getAbilities().flying && isWearingBambooCopter(player)) {
            UUID playerId = player.getUUID();
            int currentTime = playerFlightTime.getOrDefault(playerId, 0) + 1;
            playerFlightTime.put(playerId, currentTime);

            // 获取当前消耗间隔（根据剩余耐久决定）
            int consumeInterval = getConsumeInterval(player);

            // 达到消耗间隔时消耗耐久
            if (currentTime >= consumeInterval) {
                consumeDurability(player);
                playerFlightTime.put(playerId, 0);
            }
        }
    }

    /**
     * 获取当前耐久消耗间隔
     * 正常情况：10分钟 = 12000 ticks
     * 低耐久（<=3点）：3分钟 = 3600 ticks
     */
    private int getConsumeInterval(ServerPlayer player) {
        return top.theillusivec4.curios.api.CuriosApi.getCuriosInventory(player)
                .map(handler -> {
                    var stack = handler.getStacksHandler("head").map(h -> h.getStacks().getStackInSlot(0)).orElse(ItemStack.EMPTY);
                    if (!stack.isEmpty() && stack.getItem() instanceof BambooCopterItem) {
                        int remainingDurability = stack.getMaxDamage() - stack.getDamageValue();
                        // 剩余耐久<=3点时，消耗加快
                        if (remainingDurability <= LOW_DURABILITY_THRESHOLD) {
                            return DURABILITY_CONSUME_INTERVAL_LOW;
                        }
                    }
                    return DURABILITY_CONSUME_INTERVAL_NORMAL;
                })
                .orElse(DURABILITY_CONSUME_INTERVAL_NORMAL);
    }

    /**
     * 检查玩家是否装备了竹蜻蜓
     */
    private boolean isWearingBambooCopter(ServerPlayer player) {
        // 通过Curios API检查头部槽位
        return GetHeadCurios(player);
    }

    private boolean GetHeadCurios(ServerPlayer player) {
        return top.theillusivec4.curios.api.CuriosApi.getCuriosInventory(player)
                .map(handler -> {
                    var stack = handler.getStacksHandler("head").map(h -> h.getStacks().getStackInSlot(0)).orElse(ItemStack.EMPTY);
                    return !stack.isEmpty() && stack.getItem() instanceof BambooCopterItem;
                })
                .orElse(false);
    }

    /**
     * 消耗竹蜻蜓耐久
     */
    private void consumeDurability(ServerPlayer player) {
        top.theillusivec4.curios.api.CuriosApi.getCuriosInventory(player)
                .ifPresent(handler -> {
                    handler.getStacksHandler("head").ifPresent(stacksHandler -> {
                        ItemStack stack = stacksHandler.getStacks().getStackInSlot(0);
                        if (!stack.isEmpty() && stack.getItem() instanceof BambooCopterItem) {
                            // 考虑耐久附魔
                            int unbreakingLevel = stack.getEnchantmentLevel(Enchantments.UNBREAKING);

                            // 耐久附魔：每级增加100%不消耗耐久的几率
                            // 耐久I: 50%不消耗, 耐久II: 66%不消耗, 耐久III: 75%不消耗
                            boolean shouldConsume = true;
                            if (unbreakingLevel > 0) {
                                double chance = 1.0 / (unbreakingLevel + 1);
                                shouldConsume = player.getRandom().nextDouble() < chance;
                            }

                            if (shouldConsume) {
                                stack.setDamageValue(stack.getDamageValue() + 1);

                                // 耐久耗尽时破坏物品
                                if (stack.getDamageValue() >= stack.getMaxDamage()) {
                                    stack.shrink(1);
                                    player.getAbilities().mayfly = false;
                                    player.getAbilities().flying = false;
                                    player.onUpdateAbilities();
                                }
                            }
                        }
                    });
                });
    }

    /**
     * 支持经验修补附魔
     */
    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }

    /**
     * 获取最大附魔等级
     */
    @Override
    public int getEnchantmentValue() {
        return 15; // 与铁工具相同
    }

    /**
     * 支持耐久和经验修补附魔
     */
    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return enchantment == Enchantments.UNBREAKING ||
                enchantment == Enchantments.MENDING ||
                super.canApplyAtEnchantingTable(stack, enchantment);
    }

    /**
     * 添加物品Tooltip
     */
    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        // 空行
        tooltipComponents.add(Component.empty());

        // 功能描述标题
        tooltipComponents.add(Component.literal("装备效果:")
                .withStyle(Style.EMPTY.withColor(ChatFormatting.GOLD).withBold(true)));

        // 飞行能力
        tooltipComponents.add(Component.literal("- ")
                .withStyle(Style.EMPTY.withColor(ChatFormatting.GRAY))
                .append(Component.literal("✦ 赋予创造模式飞行能力")
                        .withStyle(Style.EMPTY.withColor(ChatFormatting.AQUA))));

        // 装备位置
        tooltipComponents.add(Component.literal("- ")
                .withStyle(Style.EMPTY.withColor(ChatFormatting.GRAY))
                .append(Component.literal("MAN！")
                        .withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW).withBold(true))));

        // 空行
        tooltipComponents.add(Component.empty());

        // 耐久信息标题
        tooltipComponents.add(Component.literal("耐久信息:")
                .withStyle(Style.EMPTY.withColor(ChatFormatting.GOLD).withBold(true)));

        // 耐久消耗
        tooltipComponents.add(Component.literal("- ")
                .withStyle(Style.EMPTY.withColor(ChatFormatting.GRAY))
                .append(Component.literal("每10分钟飞行消耗1点耐久")
                        .withStyle(Style.EMPTY.withColor(ChatFormatting.WHITE))));

        // 低耐久警告
        tooltipComponents.add(Component.literal("- ")
                .withStyle(Style.EMPTY.withColor(ChatFormatting.GRAY))
                .append(Component.literal("耐久≤3时: 每3分钟消耗1点")
                        .withStyle(Style.EMPTY.withColor(ChatFormatting.RED))));

        // 总耐久
        int remainingDurability = stack.getMaxDamage() - stack.getDamageValue();
        tooltipComponents.add(Component.literal("- ")
                .withStyle(Style.EMPTY.withColor(ChatFormatting.GRAY))
                .append(Component.literal("剩余耐久: " + remainingDurability + "/" + stack.getMaxDamage())
                        .withStyle(Style.EMPTY.withColor(remainingDurability > 20 ? ChatFormatting.GREEN :
                                remainingDurability > 10 ? ChatFormatting.YELLOW : ChatFormatting.RED))));

        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
    }
}
