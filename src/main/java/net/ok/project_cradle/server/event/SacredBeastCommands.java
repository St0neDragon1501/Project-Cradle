package net.ok.project_cradle.server.event;

import java.util.Collection;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.ok.project_cradle.ProjectCradle;
import net.ok.project_cradle.server.world.SacredBeastService;

/** Operator tools for awakening a selected vanilla creature without waiting for a rare spawn. */
@EventBusSubscriber(modid = ProjectCradle.MODID)
public final class SacredBeastCommands {
    private SacredBeastCommands() { }

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("cradle")
                .then(Commands.literal("sacred_beast")
                        .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                        .then(Commands.argument("targets", EntityArgument.entities())
                                .executes(context -> awaken(context.getSource(), EntityArgument.getEntities(context, "targets"))))));
    }

    private static int awaken(CommandSourceStack source, Collection<? extends Entity> targets) {
        int awakened = 0;
        for (Entity target : targets) {
            if (target instanceof Mob mob && SacredBeastService.canBecomeSacred(mob) && SacredBeastService.awaken(mob)) awakened++;
        }
        int count = awakened;
        source.sendSuccess(() -> Component.translatable("commands.project_cradle.sacred_beasts_awakened", count), true);
        return count;
    }
}
