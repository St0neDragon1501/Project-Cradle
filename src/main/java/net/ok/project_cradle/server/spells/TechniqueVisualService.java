package net.ok.project_cradle.server.spells;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.ok.project_cradle.ProjectCradle;
import net.ok.project_cradle.capability.ModAttachments;
import net.ok.project_cradle.capability.madra.MadraType;
import net.ok.project_cradle.items.AdditionalTechniqueItem;
import net.ok.project_cradle.items.ElementalTechniqueItem;
import net.ok.project_cradle.items.PathTechniqueItem;
import net.ok.project_cradle.items.RulerTechniqueItem;
import net.ok.project_cradle.spells.TechniqueCastProfile;
import net.ok.project_cradle.spells.TechniqueScaling;

/** Original spell geometry drawn with sparse colored motes: sigils, expanding waves, and orbiting bands. */
@EventBusSubscriber(modid = ProjectCradle.MODID)
public final class TechniqueVisualService {
    private static final Map<ServerLevel, List<Wave>> WAVES = new WeakHashMap<>();

    private TechniqueVisualService() { }

    public static void complete(ServerPlayer player, ItemStack stack, TechniqueCastProfile profile) {
        MadraType.Element aspect = profile.aspect() == null
                ? player.getData(ModAttachments.MADRA_TYPE).getTypes().get(0) : profile.aspect();
        double radius = areaRadius(stack);
        if (radius <= 0.0) return;
        var waves = WAVES.computeIfAbsent(player.level(), ignored -> new ArrayList<>());
        waves.removeIf(wave -> wave.owner().equals(player.getUUID()));
        waves.add(new Wave(player.getUUID(), player.position().add(0, 0.12, 0),
                TechniqueScaling.range(player, radius), aspect, player.level().getGameTime()));
    }

    private static double areaRadius(ItemStack stack) {
        if (stack.getItem() instanceof ElementalTechniqueItem item) return switch (item.getTechnique()) {
            case FLAME_RING, FROST_NOVA -> 5.0;
            case REPULSION_WAVE -> 6.0;
            case STONE_AEGIS -> 1.0;
            // Persistent fields and aimed techniques draw at their own centers.
            default -> 0.0;
        };
        if (stack.getItem() instanceof RulerTechniqueItem) return 7.0;
        if (stack.getItem() instanceof PathTechniqueItem item) return switch (item.getTechnique()) {
            case VOID_DRAGONS_DANCE, WIND_PRESSURE -> 7.0;
            case VENOM_MIST -> 6.0;
            case OATHSIGN, DRAGON_DESCENDS -> 5.0;
            case ENDLESS_SWORD, FINAL_SWORD -> 4.0;
            default -> 1.0;
        };
        if (stack.getItem() instanceof AdditionalTechniqueItem item) return switch (item.getTechnique()) {
            case HEALING_PULSE -> 6.0;
            case COUNTERSPELL -> 2.0;
            default -> 0.0;
        };
        return 0.8;
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        List<Wave> waves = WAVES.get(level);
        if (waves == null) return;
        long now = level.getGameTime();
        waves.removeIf(wave -> now - wave.started() >= 16);
        if (now % 3 == 0) for (Wave wave : waves) {
            double progress = Math.min(1.0, (now - wave.started() + 1) / 14.0);
            double radius = Math.max(0.2, wave.radius() * progress);
            sigil(level, wave.center(), radius, wave.aspect(), now * 0.025, 32);
        }
        if (waves.isEmpty()) WAVES.remove(level);
    }

    public static void enforcer(ServerPlayer player, MadraType.Element aspect) {
        double phase = player.level().getGameTime() * 0.09;
        DustParticleOptions dust = new DustParticleOptions(color(aspect), 0.6F);
        for (int i = 0; i < 12; i++) {
            double angle = phase + i * Math.PI / 3.0;
            double height = 0.2 + i * 0.12;
            Vec3 point = player.position().add(Math.cos(angle) * 0.48, height, Math.sin(angle) * 0.48);
            player.level().sendParticles(dust, point.x, point.y, point.z, 1, 0, 0, 0, 0);
        }
    }

    public static void sigil(ServerLevel level, Vec3 center, double radius, MadraType.Element aspect,
            double phase, int samples) {
        DustParticleOptions dust = new DustParticleOptions(color(aspect), 0.7F);
        for (int i = 0; i < samples; i++) {
            double angle = phase + Math.PI * 2.0 * i / samples;
            double r = i % 4 == 0 ? radius * 0.76 : radius;
            Vec3 point = center.add(Math.cos(angle) * r, 0, Math.sin(angle) * r);
            level.sendParticles(dust, point.x, point.y, point.z, 1, 0, 0, 0, 0);
        }
    }

    public static int color(MadraType.Element aspect) {
        return switch (aspect) {
            case FIRE -> 0xFF713C;
            case WATER -> 0x55BFFF;
            case AIR -> 0xD7FFF7;
            case EARTH -> 0xAF9455;
            case PURE -> 0xA0E9FF;
            case LIFE -> 0x70E58B;
            case DEATH -> 0x5EB2A3;
            case LIGHT -> 0xFFE8A0;
            case SHADOW -> 0x8D77BB;
            case BLOOD -> 0xD63D54;
            case DREAM -> 0xD991F1;
            case DESTRUCTION -> 0xE64440;
            case FORCE -> 0x91D4DC;
            case SWORD -> 0xD8E6EF;
            case HUNGER -> 0xC6CAD6;
        };
    }

    private record Wave(java.util.UUID owner, Vec3 center, double radius, MadraType.Element aspect, long started) { }
}
