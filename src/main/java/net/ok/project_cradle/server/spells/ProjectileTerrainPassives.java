package net.ok.project_cradle.server.spells;

import java.util.ArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.util.BlockSnapshot;
import net.neoforged.neoforge.event.EventHooks;
import net.ok.project_cradle.capability.madra.MadraPassives;
import net.ok.project_cradle.capability.madra.MadraType.Element;
import net.ok.project_cradle.entities.BasicStrikerProjectile;

/** Player-owned projectile impacts use normal build permissions and cancellable block events. */
public final class ProjectileTerrainPassives {
    private ProjectileTerrainPassives() { }

    public static void onHit(BasicStrikerProjectile projectile, BlockHitResult hit) {
        if (!(projectile.level() instanceof ServerLevel level)
                || !(projectile.getOwner() instanceof ServerPlayer owner)
                || !owner.isAlive() || owner.level() != level || owner.isSpectator()) return;
        BlockPos pos = hit.getBlockPos();
        if (!mayChange(level, owner, pos)) return;

        boolean destroyed = false;
        if (MadraPassives.has(owner, Element.DESTRUCTION)) {
            var state = level.getBlockState(pos);
            if (!state.isAir() && !state.liquid() && state.getDestroySpeed(level, pos) >= 0.0F
                    && state.canEntityDestroy(level, pos, owner)
                    && !CommonHooks.fireBlockBreak(level, owner.gameMode.getGameModeForPlayer(), owner, pos, state).isCanceled()) {
                destroyed = level.destroyBlock(pos, true, owner);
            }
        }

        if (MadraPassives.has(owner, Element.FIRE)) {
            BlockPos firePos = destroyed ? pos : pos.relative(hit.getDirection());
            if (!tryIgnite(level, owner, firePos, hit) && destroyed) {
                tryIgnite(level, owner, pos.relative(hit.getDirection()), hit);
            }
        }
    }

    private static boolean mayChange(ServerLevel level, ServerPlayer player, BlockPos pos) {
        return player.getAbilities().mayBuild && level.hasChunkAt(pos)
                && level.getWorldBorder().isWithinBounds(pos) && !level.isOutsideBuildHeight(pos)
                && level.mayInteract(player, pos) && player.mayInteract(level, pos);
    }

    private static boolean tryIgnite(ServerLevel level, ServerPlayer owner, BlockPos pos, BlockHitResult hit) {
        if (!mayChange(level, owner, pos) || !level.getBlockState(pos).isAir()
                || !BaseFireBlock.canBePlacedAt(level, pos, hit.getDirection())) return false;
        var fire = BaseFireBlock.getState(level, pos);
        if (!fire.canSurvive(level, pos)) return false;
        // Defer onPlace/neighbor updates until protection has approved the placement,
        // including fire's ability to create a portal rather than just one fire block.
        if (level.captureBlockSnapshots) return false;
        int start = level.capturedBlockSnapshots.size();
        boolean placed;
        level.captureBlockSnapshots = true;
        try {
            placed = level.setBlock(pos, fire, Block.UPDATE_ALL);
        } finally {
            level.captureBlockSnapshots = false;
        }
        var snapshots = new ArrayList<>(level.capturedBlockSnapshots.subList(start, level.capturedBlockSnapshots.size()));
        level.capturedBlockSnapshots.subList(start, level.capturedBlockSnapshots.size()).clear();
        if (!placed || snapshots.isEmpty()) return false;
        if (EventHooks.onBlockPlace(owner, snapshots.getFirst(), hit.getDirection())) {
            boolean restoring = level.restoringBlockSnapshots;
            level.restoringBlockSnapshots = true;
            try {
                for (BlockSnapshot previous : snapshots.reversed()) previous.restore(Block.UPDATE_ALL);
            } finally {
                level.restoringBlockSnapshots = restoring;
            }
            return false;
        }
        for (BlockSnapshot previous : snapshots) {
            var current = level.getBlockState(previous.getPos());
            current.onPlace(level, previous.getPos(), previous.getState(), false);
            level.markAndNotifyBlock(previous.getPos(), level.getChunkAt(previous.getPos()),
                    previous.getState(), current, previous.getFlags(), 512);
        }
        return true;
    }
}
