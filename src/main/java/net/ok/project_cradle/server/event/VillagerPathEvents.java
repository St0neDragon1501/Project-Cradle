package net.ok.project_cradle.server.event;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.ok.project_cradle.ProjectCradle;
import net.ok.project_cradle.capability.ModAttachments;
import net.ok.project_cradle.capability.core.advancement.AdvancementLevel;
import net.ok.project_cradle.capability.madra.MadraType;
import net.ok.project_cradle.capability.madra.MaxMadra;

/** Gives naturally created villagers an aspect and an experience-based rank. */
@EventBusSubscriber(modid = ProjectCradle.MODID)
public final class VillagerPathEvents {
    private VillagerPathEvents() {
    }

    @SubscribeEvent
    public static void onVillagerJoin(EntityJoinLevelEvent event) {
        if (!(event.getEntity() instanceof Villager villager) || !(event.getLevel() instanceof ServerLevel)
                || event.loadedFromDisk()) {
            return;
        }

        MadraType type = villager.getData(ModAttachments.MADRA_TYPE);
        type.setTypes(java.util.List.of(aspectFor(villager)));

        int level = villager.getVillagerData().level();
        AdvancementLevel advancement = switch (level) {
            case 2 -> AdvancementLevel.IRON;
            case 3 -> AdvancementLevel.JADE;
            case 4 -> AdvancementLevel.LOWGOLD;
            case 5 -> AdvancementLevel.TRUEGOLD;
            default -> AdvancementLevel.COPPER;
        };
        villager.getData(ModAttachments.CORE).setAdvancement(advancement);
        MaxMadra maxMadra = villager.getData(ModAttachments.MAXMADRA);
        maxMadra.setALevel(800.0 + 1_200.0 * level);
    }

    private static MadraType.Element aspectFor(Villager villager) {
        var profession = villager.getVillagerData().profession();
        if (profession.is(VillagerProfession.BUTCHER)
                || profession.is(VillagerProfession.TOOLSMITH)
                || profession.is(VillagerProfession.WEAPONSMITH)) {
            return MadraType.Element.FIRE;
        }
        if (profession.is(VillagerProfession.FISHERMAN)) {
            return MadraType.Element.WATER;
        }
        if (profession.is(VillagerProfession.CARTOGRAPHER)
                || profession.is(VillagerProfession.FLETCHER)
                || profession.is(VillagerProfession.SHEPHERD)) {
            return MadraType.Element.AIR;
        }
        if (profession.is(VillagerProfession.CLERIC) || profession.is(VillagerProfession.LIBRARIAN)) {
            return MadraType.Element.PURE;
        }
        if (profession.is(VillagerProfession.ARMORER)
                || profession.is(VillagerProfession.FARMER)
                || profession.is(VillagerProfession.LEATHERWORKER)
                || profession.is(VillagerProfession.MASON)) {
            return MadraType.Element.EARTH;
        }
        return MadraType.Element.PURE;
    }
}
