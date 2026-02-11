package com.gtoaddthings.item;

import com.gtoaddthings.GTOAddThings;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MaintenanceToolkitItem extends Item {

    private static final String TOOLS_TAG = "MaintenanceTools";
    private static final int REPAIR_DAMAGE = 1;

    public static final Map<String, String> TOOL_NAMES = new HashMap<>();
    static {
        TOOL_NAMES.put("wrench", "扳手");
        TOOL_NAMES.put("screwdriver", "螺丝刀");
        TOOL_NAMES.put("soft_hammer", "软锤");
        TOOL_NAMES.put("hard_hammer", "硬锤");
        TOOL_NAMES.put("crowbar", "撬棍");
    }

    public MaintenanceToolkitItem() {
        super(new Item.Properties().stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        
        if (!level.isClientSide) {
            player.sendSystemMessage(Component.literal("工具装载功能开发中...")
                .withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)));
        }

        return InteractionResultHolder.success(stack);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();

        if (level.isClientSide || player == null) {
            return InteractionResult.SUCCESS;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity == null) {
            return InteractionResult.PASS;
        }

        if (isMaintenanceHatch(blockEntity)) {
            return tryRepairMaintenanceHatch((ServerPlayer) player, stack, blockEntity);
        }

        return InteractionResult.PASS;
    }

    private boolean isMaintenanceHatch(BlockEntity blockEntity) {
        String className = blockEntity.getClass().getName();
        return className.toLowerCase().contains("maintenance") ||
               className.toLowerCase().contains("maintenhatch");
    }

    private InteractionResult tryRepairMaintenanceHatch(ServerPlayer player, ItemStack toolkit, BlockEntity blockEntity) {
        Map<String, Boolean> problems = getMaintenanceProblems(blockEntity);
        
        if (problems == null || problems.isEmpty()) {
            player.sendSystemMessage(Component.literal("维护仓状态良好，不需要修复")
                .withStyle(Style.EMPTY.withColor(ChatFormatting.GREEN)));
            return InteractionResult.PASS;
        }

        boolean repaired = false;
        StringBuilder consumedTools = new StringBuilder();
        
        for (Map.Entry<String, Boolean> entry : problems.entrySet()) {
            if (entry.getValue()) {
                String toolType = entry.getKey();
                if (hasTool(toolkit, toolType)) {
                    if (consumeToolDurability(toolkit, toolType)) {
                        fixMaintenanceProblem(blockEntity, toolType);
                        if (consumedTools.length() > 0) {
                            consumedTools.append(", ");
                        }
                        consumedTools.append(TOOL_NAMES.getOrDefault(toolType, toolType));
                        repaired = true;
                    }
                }
            }
        }

        if (repaired) {
            player.sendSystemMessage(Component.literal("维护仓修复成功！消耗了: " + consumedTools)
                .withStyle(Style.EMPTY.withColor(ChatFormatting.GREEN)));
            return InteractionResult.SUCCESS;
        } else {
            player.sendSystemMessage(Component.literal("工具箱中缺少必要的工具或工具耐久不足")
                .withStyle(Style.EMPTY.withColor(ChatFormatting.RED)));
            return InteractionResult.FAIL;
        }
    }

    private Map<String, Boolean> getMaintenanceProblems(BlockEntity blockEntity) {
        Map<String, Boolean> problems = new HashMap<>();
        
        try {
            Class<?> clazz = blockEntity.getClass();
            java.lang.reflect.Field problemsField = null;
            
            try {
                problemsField = clazz.getDeclaredField("maintenanceProblems");
            } catch (NoSuchFieldException e) {
                try {
                    problemsField = clazz.getDeclaredField("problems");
                } catch (NoSuchFieldException e2) {
                    // 尝试其他可能的字段名
                    java.lang.reflect.Field[] fields = clazz.getDeclaredFields();
                    for (java.lang.reflect.Field field : fields) {
                        if (field.getName().toLowerCase().contains("maintenance") || 
                            field.getName().toLowerCase().contains("problem")) {
                            problemsField = field;
                            break;
                        }
                    }
                }
            }

            if (problemsField != null) {
                problemsField.setAccessible(true);
                Object value = problemsField.get(blockEntity);
                
                byte problemsValue = 0;
                if (value instanceof Byte) {
                    problemsValue = (Byte) value;
                } else if (value instanceof Integer) {
                    problemsValue = ((Integer) value).byteValue();
                }
                
                // GTCEu维护问题位定义
                problems.put("screwdriver", (problemsValue & 0x01) != 0);
                problems.put("wrench", (problemsValue & 0x02) != 0);
                problems.put("crowbar", (problemsValue & 0x04) != 0);
                problems.put("hard_hammer", (problemsValue & 0x08) != 0);
                problems.put("soft_hammer", (problemsValue & 0x10) != 0);
            }
        } catch (Exception e) {
            GTOAddThings.LOGGER.error("获取维护问题时出错: " + e.getMessage());
        }
        
        return problems;
    }

    private void fixMaintenanceProblem(BlockEntity blockEntity, String toolType) {
        try {
            Class<?> clazz = blockEntity.getClass();
            
            // 尝试调用修复方法
            java.lang.reflect.Method[] methods = clazz.getMethods();
            for (java.lang.reflect.Method method : methods) {
                String methodName = method.getName().toLowerCase();
                if ((methodName.contains("fix") || methodName.contains("repair")) && 
                    methodName.contains("maintenance")) {
                    try {
                        if (method.getParameterCount() == 0) {
                            method.invoke(blockEntity);
                            return;
                        }
                    } catch (Exception e) {
                        // 继续尝试
                    }
                }
            }

            // 直接修改字段
            java.lang.reflect.Field problemsField = null;
            try {
                problemsField = clazz.getDeclaredField("maintenanceProblems");
            } catch (NoSuchFieldException e) {
                java.lang.reflect.Field[] fields = clazz.getDeclaredFields();
                for (java.lang.reflect.Field field : fields) {
                    if (field.getName().toLowerCase().contains("maintenance") || 
                        field.getName().toLowerCase().contains("problem")) {
                        problemsField = field;
                        break;
                    }
                }
            }

            if (problemsField != null) {
                problemsField.setAccessible(true);
                Object value = problemsField.get(blockEntity);
                byte currentProblems = 0;
                if (value instanceof Byte) {
                    currentProblems = (Byte) value;
                } else if (value instanceof Integer) {
                    currentProblems = ((Integer) value).byteValue();
                }
                
                byte mask = switch (toolType) {
                    case "screwdriver" -> ~0x01;
                    case "wrench" -> ~0x02;
                    case "crowbar" -> ~0x04;
                    case "hard_hammer" -> ~0x08;
                    case "soft_hammer" -> ~0x10;
                    default -> (byte) 0xFF;
                };
                
                byte newValue = (byte) (currentProblems & mask);
                if (value instanceof Byte) {
                    problemsField.setByte(blockEntity, newValue);
                } else {
                    problemsField.setInt(blockEntity, newValue);
                }
            }
            
        } catch (Exception e) {
            GTOAddThings.LOGGER.error("修复维护问题时出错: " + e.getMessage());
        }
    }

    private boolean hasTool(ItemStack toolkit, String toolType) {
        CompoundTag tag = toolkit.getOrCreateTag();
        if (!tag.contains(TOOLS_TAG)) {
            return false;
        }

        ListTag toolsList = tag.getList(TOOLS_TAG, 10);
        for (int i = 0; i < toolsList.size(); i++) {
            CompoundTag toolTag = toolsList.getCompound(i);
            if (toolTag.getString("ToolType").equals(toolType)) {
                return true;
            }
        }
        return false;
    }

    private boolean consumeToolDurability(ItemStack toolkit, String toolType) {
        CompoundTag tag = toolkit.getOrCreateTag();
        if (!tag.contains(TOOLS_TAG)) {
            return false;
        }

        ListTag toolsList = tag.getList(TOOLS_TAG, 10);
        for (int i = 0; i < toolsList.size(); i++) {
            CompoundTag toolTag = toolsList.getCompound(i);
            if (toolTag.getString("ToolType").equals(toolType)) {
                ItemStack toolStack = ItemStack.of(toolTag.getCompound("ToolItem"));
                
                if (toolStack.isDamageableItem()) {
                    int maxDamage = toolStack.getMaxDamage();
                    int currentDamage = toolStack.getDamageValue();
                    
                    if (maxDamage - currentDamage <= REPAIR_DAMAGE) {
                        return false;
                    }
                    
                    toolStack.setDamageValue(currentDamage + REPAIR_DAMAGE);
                    toolTag.put("ToolItem", toolStack.save(new CompoundTag()));
                    toolsList.set(i, toolTag);
                    tag.put(TOOLS_TAG, toolsList);
                    return true;
                }
                return true;
            }
        }
        return false;
    }

    public static boolean addTool(ItemStack toolkit, ItemStack tool, String toolType) {
        if (!toolkit.is(GTOAddThings.MAINTENANCE_TOOLKIT.get())) {
            return false;
        }

        CompoundTag tag = toolkit.getOrCreateTag();
        ListTag toolsList;

        if (tag.contains(TOOLS_TAG)) {
            toolsList = tag.getList(TOOLS_TAG, 10);
            // 检查是否已有该类型工具
            for (int i = 0; i < toolsList.size(); i++) {
                CompoundTag existingTool = toolsList.getCompound(i);
                if (existingTool.getString("ToolType").equals(toolType)) {
                    return false;
                }
            }
        } else {
            toolsList = new ListTag();
        }

        CompoundTag toolTag = new CompoundTag();
        toolTag.putString("ToolType", toolType);
        toolTag.put("ToolItem", tool.save(new CompoundTag()));
        toolsList.add(toolTag);

        tag.put(TOOLS_TAG, toolsList);
        return true;
    }

    public static Map<String, ItemStack> getTools(ItemStack toolkit) {
        Map<String, ItemStack> tools = new HashMap<>();
        
        if (!toolkit.is(GTOAddThings.MAINTENANCE_TOOLKIT.get())) {
            return tools;
        }

        CompoundTag tag = toolkit.getTag();
        if (tag == null || !tag.contains(TOOLS_TAG)) {
            return tools;
        }

        ListTag toolsList = tag.getList(TOOLS_TAG, 10);
        for (int i = 0; i < toolsList.size(); i++) {
            CompoundTag toolTag = toolsList.getCompound(i);
            String toolType = toolTag.getString("ToolType");
            ItemStack toolStack = ItemStack.of(toolTag.getCompound("ToolItem"));
            tools.put(toolType, toolStack);
        }
        
        return tools;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        tooltipComponents.add(Component.empty());
        tooltipComponents.add(Component.literal("功能:")
            .withStyle(Style.EMPTY.withColor(ChatFormatting.GOLD).withBold(true)));
        tooltipComponents.add(Component.literal("- ")
            .withStyle(Style.EMPTY.withColor(ChatFormatting.GRAY))
            .append(Component.literal("右键打开工具箱界面（开发中）")
                .withStyle(Style.EMPTY.withColor(ChatFormatting.AQUA))));
        tooltipComponents.add(Component.literal("- ")
            .withStyle(Style.EMPTY.withColor(ChatFormatting.GRAY))
            .append(Component.literal("对维护仓右键一键修复")
                .withStyle(Style.EMPTY.withColor(ChatFormatting.AQUA))));

        tooltipComponents.add(Component.empty());
        tooltipComponents.add(Component.literal("已装载工具:")
            .withStyle(Style.EMPTY.withColor(ChatFormatting.GOLD).withBold(true)));

        Map<String, ItemStack> tools = getTools(stack);
        if (tools.isEmpty()) {
            tooltipComponents.add(Component.literal("- 空")
                .withStyle(Style.EMPTY.withColor(ChatFormatting.DARK_GRAY)));
        } else {
            for (Map.Entry<String, ItemStack> entry : tools.entrySet()) {
                String toolName = TOOL_NAMES.getOrDefault(entry.getKey(), entry.getKey());
                ItemStack toolStack = entry.getValue();
                int remaining = toolStack.getMaxDamage() - toolStack.getDamageValue();
                int max = toolStack.getMaxDamage();
                
                tooltipComponents.add(Component.literal("- " + toolName + ": ")
                    .withStyle(Style.EMPTY.withColor(ChatFormatting.GRAY))
                    .append(Component.literal(remaining + "/" + max)
                        .withStyle(Style.EMPTY.withColor(remaining > max * 0.5 ? ChatFormatting.GREEN :
                            remaining > max * 0.2 ? ChatFormatting.YELLOW : ChatFormatting.RED))));
            }
        }

        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
    }
}
