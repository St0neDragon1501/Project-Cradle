package net.ok.project_cradle.server.event;

import com.mojang.brigadier.arguments.StringArgumentType;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.entity.EntitySpawnReason;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.ok.project_cradle.ProjectCradle;
import net.ok.project_cradle.capability.core.advancement.AdvancementLevel;
import net.ok.project_cradle.capability.madra.MadraType;
import net.ok.project_cradle.entities.ModEntities;
import net.ok.project_cradle.entities.RemnantEntity;
import net.ok.project_cradle.entities.RemnantForm;

/** Operator tools for placing a spirit with a specific origin, advancement, and madra. */
@EventBusSubscriber(modid = ProjectCradle.MODID)
public final class RemnantCommands {
    private RemnantCommands() { }

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        var secondary = Commands.argument("secondary", StringArgumentType.word())
                .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                        Arrays.stream(MadraType.Element.values()).map(MadraType.Element::id), builder))
                .executes(context -> spawn(context.getSource(), StringArgumentType.getString(context, "aspect"),
                        StringArgumentType.getString(context, "rank"), StringArgumentType.getString(context, "form"),
                        StringArgumentType.getString(context, "secondary")));
        var form = Commands.argument("form", StringArgumentType.word())
                .suggests((context, builder) -> SharedSuggestionProvider.suggest(List.of("humanoid", "beast", "tree"), builder))
                .executes(context -> spawn(context.getSource(), StringArgumentType.getString(context, "aspect"),
                        StringArgumentType.getString(context, "rank"), StringArgumentType.getString(context, "form"), null))
                .then(secondary);
        var rank = Commands.argument("rank", StringArgumentType.word())
                .suggests((context, builder) -> SharedSuggestionProvider.suggest(Arrays.stream(AdvancementLevel.values())
                        .filter(level -> level.order() >= AdvancementLevel.IRON.order()).map(AdvancementLevel::id), builder))
                .executes(context -> spawn(context.getSource(), StringArgumentType.getString(context, "aspect"),
                        StringArgumentType.getString(context, "rank"), "humanoid", null))
                .then(form);
        var aspect = Commands.argument("aspect", StringArgumentType.word())
                .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                        Arrays.stream(MadraType.Element.values()).map(MadraType.Element::id), builder))
                .executes(context -> spawn(context.getSource(), StringArgumentType.getString(context, "aspect"),
                        "iron", "humanoid", null))
                .then(rank);
        event.getDispatcher().register(Commands.literal("cradle")
                .then(Commands.literal("remnant")
                        .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                        .then(Commands.literal("spawn").then(aspect))));
    }

    private static int spawn(CommandSourceStack source, String aspectId, String rankId, String formId, String secondaryId) {
        MadraType.Element aspect = MadraType.Element.byId(aspectId);
        MadraType.Element secondary = secondaryId == null ? null : MadraType.Element.byId(secondaryId);
        AdvancementLevel rank = AdvancementLevel.fromId(rankId);
        RemnantForm form;
        try {
            form = RemnantForm.valueOf(formId.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            source.sendFailure(Component.translatable("commands.project_cradle.invalid_remnant_form"));
            return 0;
        }
        if (aspect == null || secondaryId != null && secondary == null) {
            source.sendFailure(Component.translatable("commands.project_cradle.invalid_madra_type"));
            return 0;
        }
        if (rank.order() < AdvancementLevel.IRON.order()) {
            source.sendFailure(Component.translatable("commands.project_cradle.invalid_remnant_rank"));
            return 0;
        }
        RemnantEntity remnant = ModEntities.REMNANT.get().create(source.getLevel(), EntitySpawnReason.COMMAND);
        if (remnant == null) return 0;
        remnant.setSpirit(rank, secondary == null ? List.of(aspect) : List.of(aspect, secondary), form);
        var position = source.getPosition().add(0.0, 1.0, 0.0);
        remnant.snapTo(position.x, position.y, position.z, 0.0F, 0.0F);
        remnant.setPersistenceRequired();
        if (!source.getLevel().addFreshEntity(remnant)) return 0;
        remnant.spawnAnim();
        source.sendSuccess(() -> Component.translatable("commands.project_cradle.remnant_spawned",
                Component.translatable("advancement.project_cradle." + rank.id()),
                Component.translatable(aspect.translationKey())), true);
        return 1;
    }
}
