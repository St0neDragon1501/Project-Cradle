package net.ok.project_cradle.server.progression;

import java.util.List;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.ok.project_cradle.capability.ModAttachments;
import net.ok.project_cradle.capability.core.Core;
import net.ok.project_cradle.capability.core.GoldRoute;
import net.ok.project_cradle.capability.core.advancement.AdvancementLevel;
import net.ok.project_cradle.capability.madra.MadraType;

/** Forms a mutual spiritual contract with a stronger sacred beast or wild spirit. */
public final class SpiritBondService {
    public static final String WILD_SPIRIT_TAG = "project_cradle.wild_spirit";

    private SpiritBondService() {
    }

    public static InteractionResult contract(ServerPlayer player, LivingEntity partner) {
        Core core = player.getData(ModAttachments.CORE);
        if (core.getAdvancement() != AdvancementLevel.JADE || !core.hasJadeSpiral()) {
            return fail(player, "contract_requires_jade");
        }
        if (!player.isAlive() || player.isSpectator() || partner == player || partner instanceof Player
                || !partner.isAlive() || partner.level() != player.level()
                || !player.isWithinEntityInteractionRange(partner, 3.0)) {
            return fail(player, "contract_invalid_partner");
        }

        boolean sacredBeast = partner.getData(ModAttachments.SACRED_BEAST_AGE).isSacred();
        if (!sacredBeast && !partner.entityTags().contains(WILD_SPIRIT_TAG)) {
            return fail(player, "contract_invalid_partner");
        }
        Core partnerCore = partner.getData(ModAttachments.CORE);
        // Sacred beasts from older saves predate the entity rank field used for contracts.
        boolean legacySacredBeast = sacredBeast
                && partnerCore.getAdvancement().order() < AdvancementLevel.LOWGOLD.order();
        if (!legacySacredBeast
                && partnerCore.getAdvancement().order() < AdvancementLevel.LOWGOLD.order()) {
            return fail(player, "contract_partner_too_weak");
        }
        if (!core.getBondedPartner().isBlank() || !partnerCore.getBondedPartner().isBlank()) {
            return fail(player, "contract_already_bound");
        }

        MadraType playerType = player.getData(ModAttachments.MADRA_TYPE);
        MadraType partnerType = partner.getData(ModAttachments.MADRA_TYPE);
        List<MadraType.Element> previousTypes = List.copyOf(playerType.getTypes());
        boolean pureCore = previousTypes.size() == 1 && previousTypes.get(0) == MadraType.Element.PURE;
        if (!pureCore && previousTypes.stream().noneMatch(partnerType.getTypes()::contains)) {
            return fail(player, "contract_incompatible_madra");
        }

        GoldRoute previousRoute = core.getGoldRoute();
        String previousGoldsign = core.getGoldsign();
        core.setBondedPartner(partner.getUUID().toString());
        partnerCore.setBondedPartner(player.getUUID().toString());
        core.setGoldRoute(GoldRoute.CONTRACT);
        core.setGoldsign(partnerType.getTypes().get(0).id());
        if (pureCore) {
            playerType.setTypes(partnerType.getTypes());
        }

        if (!ProgressionService.advance(player, AdvancementLevel.LOWGOLD)) {
            core.setBondedPartner("");
            partnerCore.setBondedPartner("");
            core.setGoldRoute(previousRoute);
            core.setGoldsign(previousGoldsign);
            playerType.setTypes(previousTypes);
            return fail(player, "contract_failed");
        }
        if (legacySacredBeast) {
            partnerCore.setAdvancement(AdvancementLevel.LOWGOLD);
        }
        GoldsignAppearanceService.refresh(player);
        player.syncData(ModAttachments.CORE);
        player.syncData(ModAttachments.MADRA_TYPE);
        partner.syncData(ModAttachments.CORE);
        player.level().sendParticles(ParticleTypes.END_ROD,
                partner.getX(), partner.getY() + partner.getBbHeight() * 0.5, partner.getZ(),
                8, 0.35, 0.3, 0.35, 0.02);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 0.7F, 1.0F);
        player.sendSystemMessage(Component.translatable("message.project_cradle.contract_formed", partner.getName()));
        return InteractionResult.SUCCESS;
    }

    private static InteractionResult fail(ServerPlayer player, String key) {
        player.sendSystemMessage(Component.translatable("message.project_cradle." + key));
        return InteractionResult.FAIL;
    }
}
