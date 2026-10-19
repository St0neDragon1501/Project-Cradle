package net.ok.project_cradle.server.event;

import java.util.List;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.ok.project_cradle.ProjectCradle;
import net.ok.project_cradle.capability.ModAttachments;
import net.ok.project_cradle.capability.core.Core;
import net.ok.project_cradle.capability.core.GoldsignStyle;
import net.ok.project_cradle.capability.core.ProgressionHints;
import net.ok.project_cradle.menus.SoulspaceMenu;
import net.ok.project_cradle.server.progression.ProgressionService;
import net.ok.project_cradle.server.progression.GoldsignAppearanceService;
import net.ok.project_cradle.server.progression.GoldPassiveService;
import net.minecraft.world.SimpleMenuProvider;
import net.ok.project_cradle.capability.madra.MadraType;
import net.ok.project_cradle.capability.madra.MaxMadra;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;

/** Player progression commands and operator-only madra configuration. */
@EventBusSubscriber(modid = ProjectCradle.MODID)
public final class MadraCommands {
    private static final double MAX_COMMAND_MADRA = 1_000_000_000.0;

    private MadraCommands() {
    }

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        registerPlayerCommands(event);
        event.getDispatcher().register(Commands.literal("cradle")
                .then(Commands.literal("madra")
                        .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                        .then(Commands.literal("set")
                                .then(Commands.argument("target", EntityArgument.player())
                                        .then(Commands.argument("current", DoubleArgumentType.doubleArg(0.0, MAX_COMMAND_MADRA))
                                                .executes(context -> setCurrentMadra(
                                                        context.getSource(),
                                                        EntityArgument.getPlayer(context, "target"),
                                                        DoubleArgumentType.getDouble(context, "current")))
                                                .then(Commands.argument("maximum", DoubleArgumentType.doubleArg(0.0, MAX_COMMAND_MADRA))
                                                        .executes(context -> setMadraAndMaximum(
                                                                context.getSource(),
                                                                EntityArgument.getPlayer(context, "target"),
                                                                DoubleArgumentType.getDouble(context, "current"),
                                                                DoubleArgumentType.getDouble(context, "maximum")))))))
                        .then(Commands.literal("type")
                                .then(Commands.argument("target", EntityArgument.player())
                                        .then(Commands.argument("primary", StringArgumentType.word())
                                                .executes(context -> setMadraType(
                                                        context.getSource(),
                                                        EntityArgument.getPlayer(context, "target"),
                                                        StringArgumentType.getString(context, "primary"),
                                                        null))
                                                .then(Commands.argument("secondary", StringArgumentType.word())
                                                        .executes(context -> setMadraType(
                                                                context.getSource(),
                                                                EntityArgument.getPlayer(context, "target"),
                                                                StringArgumentType.getString(context, "primary"),
                                                                StringArgumentType.getString(context, "secondary")))))))));
    }

    private static void registerPlayerCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("cradle")
                .then(Commands.literal("revelation")
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            player.sendSystemMessage(ProgressionHints.next(player.getData(ModAttachments.CORE)));
                            return 1;
                        })
                        .then(Commands.argument("statement", StringArgumentType.greedyString())
                                .executes(context -> ProgressionService.recordRevelation(context.getSource().getPlayerOrException(), StringArgumentType.getString(context, "statement")) ? 1 : 0)))
                .then(Commands.literal("soulspace").executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    if (!ProgressionService.openSoulspace(player)) return 0;
                    player.openMenu(new SimpleMenuProvider((id, inventory, owner) -> new SoulspaceMenu(id, inventory), Component.translatable("container.project_cradle.soulspace")));
                    return 1;
                }))
                .then(Commands.literal("icon")
                        .then(Commands.argument("concept", StringArgumentType.greedyString()).executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            String concept = Core.normalizeIcon(StringArgumentType.getString(context, "concept"));
                            if (concept.isBlank()) {
                                player.sendSystemMessage(Component.translatable("message.project_cradle.invalid_icon"));
                                return 0;
                            }
                            player.getData(ModAttachments.CORE).setPendingIcon(concept);
                            player.syncData(ModAttachments.CORE);
                            player.sendSystemMessage(Component.translatable("message.project_cradle.icon_selected", ProgressionHints.iconName(concept)));
                            return 1;
                        })))
                .then(Commands.literal("passives").executes(context -> {
                    GoldPassiveService.describe(context.getSource().getPlayerOrException());
                    return 1;
                }))
                .then(Commands.literal("goldsign")
                        .executes(context -> showGoldsign(context.getSource().getPlayerOrException()))
                        .then(Commands.argument("style", StringArgumentType.word())
                                .suggests((context, builder) -> net.minecraft.commands.SharedSuggestionProvider.suggest(GoldsignStyle.commandIds(), builder))
                                .executes(context -> selectGoldsign(context.getSource().getPlayerOrException(), StringArgumentType.getString(context, "style")))))
                .then(Commands.literal("icons").executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    player.sendSystemMessage(Component.translatable("message.project_cradle.icons_known", player.getData(ModAttachments.CORE).getIcons().stream().map(icon -> ProgressionHints.iconName(icon).getString()).collect(java.util.stream.Collectors.joining(", "))));
                    return 1;
                })));
    }

    private static int showGoldsign(ServerPlayer player) {
        Core core = player.getData(ModAttachments.CORE);
        if (core.getGoldsign().isBlank()) {
            player.sendSystemMessage(Component.translatable("message.project_cradle.goldsign_required"));
            return 0;
        }
        GoldsignAppearanceService.refresh(player);
        player.sendSystemMessage(Component.translatable("message.project_cradle.goldsign_current",
                Component.translatable(core.getGoldsignStyle().translationKey()),
                Component.translatable(core.getResolvedGoldsignStyle().translationKey())));
        return 1;
    }

    private static int selectGoldsign(ServerPlayer player, String id) {
        GoldsignStyle style = GoldsignStyle.byId(id);
        if (style == null) {
            player.sendSystemMessage(Component.translatable("commands.project_cradle.invalid_goldsign_style"));
            return 0;
        }
        if (!GoldsignAppearanceService.select(player, style)) {
            player.sendSystemMessage(Component.translatable("message.project_cradle.goldsign_required"));
            return 0;
        }
        Core core = player.getData(ModAttachments.CORE);
        player.sendSystemMessage(Component.translatable("message.project_cradle.goldsign_selected",
                Component.translatable(style.translationKey()),
                Component.translatable(core.getResolvedGoldsignStyle().translationKey())));
        return 1;
    }

    private static int setCurrentMadra(net.minecraft.commands.CommandSourceStack source,
            ServerPlayer target, double current) {
        MaxMadra maximum = target.getData(ModAttachments.MAXMADRA);
        if (!Double.isFinite(current) || current < 0.0) {
            source.sendFailure(Component.translatable("commands.project_cradle.invalid_madra_amount"));
            return 0;
        }
        if (current > maximum.getLevel()) {
            source.sendFailure(Component.translatable("commands.project_cradle.madra_exceeds_max", maximum.getLevel()));
            return 0;
        }
        target.getData(ModAttachments.MADRA).setALevel(current);
        target.syncData(ModAttachments.MADRA);
        source.sendSuccess(() -> Component.translatable("commands.project_cradle.madra_set",
                target.getName(), current, maximum.getLevel()), true);
        return 1;
    }

    private static int setMadraAndMaximum(net.minecraft.commands.CommandSourceStack source,
            ServerPlayer target, double current, double max) {
        if (!Double.isFinite(current) || !Double.isFinite(max) || current < 0.0 || max < 0.0) {
            source.sendFailure(Component.translatable("commands.project_cradle.invalid_madra_amount"));
            return 0;
        }
        if (current > max) {
            source.sendFailure(Component.translatable("commands.project_cradle.madra_exceeds_max", max));
            return 0;
        }
        target.getData(ModAttachments.MAXMADRA).setALevel(max);
        target.getData(ModAttachments.MADRA).setALevel(current);
        target.syncData(ModAttachments.MAXMADRA);
        target.syncData(ModAttachments.MADRA);
        source.sendSuccess(() -> Component.translatable("commands.project_cradle.madra_set",
                target.getName(), current, max), true);
        return 1;
    }

    private static int setMadraType(net.minecraft.commands.CommandSourceStack source,
            ServerPlayer target, String primaryId, String secondaryId) {
        MadraType.Element primary = MadraType.Element.byId(primaryId);
        MadraType.Element secondary = secondaryId == null ? null : MadraType.Element.byId(secondaryId);
        if (primary == null || (secondaryId != null && secondary == null)) {
            source.sendFailure(Component.translatable("commands.project_cradle.invalid_madra_type"));
            return 0;
        }
        if (secondary == primary) {
            source.sendFailure(Component.translatable("commands.project_cradle.duplicate_madra_type"));
            return 0;
        }

        MadraType madraType = target.getData(ModAttachments.MADRA_TYPE);
        boolean updated = secondary == null
                ? madraType.setTypes(List.of(primary))
                : madraType.setTypes(List.of(primary, secondary));
        if (!updated) {
            source.sendFailure(Component.translatable("commands.project_cradle.invalid_madra_type"));
            return 0;
        }
        target.syncData(ModAttachments.MADRA_TYPE);
        GoldsignAppearanceService.refresh(target);
        String translatedTypes = madraType.getTypes().stream()
                .map(type -> Component.translatable(type.translationKey()).getString())
                .collect(java.util.stream.Collectors.joining(", "));
        source.sendSuccess(() -> Component.translatable("commands.project_cradle.madra_type_set",
                target.getName(), translatedTypes), true);
        return 1;
    }
}
