package net.ok.project_cradle.server.progression;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.ok.project_cradle.capability.ModAttachments;
import net.ok.project_cradle.capability.QuickDropInventory;
import net.ok.project_cradle.capability.SoulspaceInventory;
import net.ok.project_cradle.entities.RemnantEntity;
import net.ok.project_cradle.spells.TechniqueCatalog;

/** Successful practice and carried technique items determine the skills a spirit remembers. */
public final class TechniqueKnowledge {
    private static final String HISTORY = "project_cradle.known_techniques";
    private static final int LIMIT = 64;

    private TechniqueKnowledge() { }

    /** A fresh immutable snapshot: inventory changes after death cannot alter the spirit. */
    public static List<String> capture(LivingEntity source) {
        Set<String> known = new LinkedHashSet<>();
        add(known, TechniqueCatalog.basic());
        if (source instanceof RemnantEntity remnant) {
            for (String id : remnant.getKnownTechniques()) add(known, id);
        } else if (source.getData(ModAttachments.SACRED_BEAST_AGE).isSacred()) {
            for (String id : source.getData(ModAttachments.SACRED_BEAST_AGE).getTechniques()) add(known, id);
        } else if (source instanceof Player player) {
            CompoundTag history = player.getPersistentData().getCompoundOrEmpty(Player.PERSISTED_NBT_TAG)
                    .getCompoundOrEmpty(HISTORY);
            for (String id : history.keySet()) if (history.getBooleanOr(id, false)) add(known, id);
            // Existing item-use statistics recover practice from before explicit history was introduced.
            if (player instanceof ServerPlayer serverPlayer) for (String id : TechniqueCatalog.allIds()) {
                ItemStack technique = TechniqueCatalog.stack(id);
                if (!technique.isEmpty() && serverPlayer.getStats().getValue(Stats.ITEM_USED.get(technique.getItem())) > 0) add(known, id);
            }
            for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
                add(known, TechniqueCatalog.id(player.getInventory().getItem(slot)));
            }
            QuickDropInventory quick = player.getData(ModAttachments.QUICK_DROP_INVENTORY);
            for (int slot = 0; slot < QuickDropInventory.SLOT_COUNT; slot++) add(known, TechniqueCatalog.id(quick.getItem(slot)));
            SoulspaceInventory soulspace = player.getData(ModAttachments.SOULSPACE_INVENTORY);
            for (int slot = 0; slot < SoulspaceInventory.SLOT_COUNT; slot++) add(known, TechniqueCatalog.id(soulspace.getItem(slot)));
        }
        if (known.size() < 2) {
            add(known, TechniqueCatalog.themed(source.getData(ModAttachments.MADRA_TYPE).getTypes().get(0)));
        }
        return List.copyOf(known);
    }

    public static void recordSuccessfulCast(ServerPlayer player, ItemStack stack) {
        String cast = TechniqueCatalog.id(stack);
        if (cast.isEmpty()) return;
        CompoundTag persisted = player.getPersistentData().getCompoundOrEmpty(Player.PERSISTED_NBT_TAG);
        CompoundTag old = persisted.getCompoundOrEmpty(HISTORY);
        Set<String> valid = new LinkedHashSet<>();
        for (String id : old.keySet()) if (old.getBooleanOr(id, false)) add(valid, id);
        add(valid, cast);
        CompoundTag history = new CompoundTag();
        for (String id : valid) history.putBoolean(id, true);
        persisted.put(HISTORY, history);
        player.getPersistentData().put(Player.PERSISTED_NBT_TAG, persisted);
    }

    private static void add(Set<String> known, String id) {
        if (known.size() >= LIMIT) return;
        String normalized = TechniqueCatalog.normalize(id);
        if (!normalized.isEmpty()) known.add(normalized);
    }
}
