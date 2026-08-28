package net.ok.project_cradle.items;



import net.minecraft.world.item.component.BlocksAttacks;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.entity.EquipmentSlot;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.ok.project_cradle.ProjectCradle;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;


public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ProjectCradle.MODID);

    public static final DeferredItem<Item> HOPE = ITEMS.registerItem("hope",
            properties -> new HopeItem(properties.stacksTo(1)));
  
    
    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}