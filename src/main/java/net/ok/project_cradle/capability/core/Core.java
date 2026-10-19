package net.ok.project_cradle.capability.core;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
import net.ok.project_cradle.capability.core.advancement.AdvancementLevel;

/** Persistent progression. Personal revelation text stays on the server when state is synchronized. */
public class Core implements ValueIOSerializable {
    private static final Codec<List<String>> ICONS_CODEC = Codec.STRING.listOf();
    private static final StreamCodec<ByteBuf, List<String>> ICONS_STREAM_CODEC =
            ByteBufCodecs.stringUtf8(32).apply(ByteBufCodecs.list());
    private static final double MAX_SOULFIRE = 1_000_000_000.0;
    private static final double SOULFIRE_PRECISION = 1_000_000.0;
    private static final double SOULFIRE_EPSILON = 0.5 / SOULFIRE_PRECISION;

    public static final StreamCodec<ByteBuf, Core> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public Core decode(ByteBuf buffer) {
            Core core = new Core();
            int ordinal = buffer.readInt();
            AdvancementLevel[] ranks = AdvancementLevel.values();
            core.advancement = ordinal >= 0 && ordinal < ranks.length ? ranks[ordinal] : AdvancementLevel.FOUNDATION;
            int flags = buffer.readInt();
            core.herald = (flags & 1) != 0;
            core.sage = (flags & 2) != 0;
            core.monarch = (flags & 4) != 0;
            core.tutorialReceived = (flags & 8) != 0;
            core.soulspace = (flags & 16) != 0;
            for (LordRevelation revelation : LordRevelation.values()) {
                if ((flags & (32 << revelation.ordinal())) != 0) {
                    core.revelations.put(revelation, "remembered");
                }
            }
            core.goldsign = ByteBufCodecs.STRING_UTF8.decode(buffer);
            core.goldsignStyle = GoldsignStyle.fromId(ByteBufCodecs.STRING_UTF8.decode(buffer));
            core.resolvedGoldsignStyle = GoldsignStyle.fromId(ByteBufCodecs.STRING_UTF8.decode(buffer));
            core.readIcons(ICONS_STREAM_CODEC.decode(buffer));
            core.pendingIcon = ByteBufCodecs.STRING_UTF8.decode(buffer);
            core.ironBodyType = IronBodyType.fromId(ByteBufCodecs.STRING_UTF8.decode(buffer));
            core.goldRoute = GoldRoute.fromId(ByteBufCodecs.STRING_UTF8.decode(buffer));
            core.soulfire = normalizeSoulfire(buffer.readDouble());
            core.setSelectedTechnique(buffer.readInt());
            core.ironPreparation = Math.max(0, buffer.readInt());
            core.jadeCycling = Math.max(0, buffer.readInt());
            core.goldCycling = Math.max(0, buffer.readInt());
            core.remnantMastery = Math.max(0, buffer.readInt());
            core.willpower = Math.max(0, buffer.readInt());
            return core;
        }

        @Override
        public void encode(ByteBuf buffer, Core core) {
            buffer.writeInt(core.advancement.ordinal());
            int flags = (core.herald ? 1 : 0) | (core.sage ? 2 : 0) | (core.monarch ? 4 : 0)
                    | (core.tutorialReceived ? 8 : 0) | (core.soulspace ? 16 : 0);
            for (LordRevelation revelation : core.revelations.keySet()) {
                flags |= 32 << revelation.ordinal();
            }
            buffer.writeInt(flags);
            ByteBufCodecs.STRING_UTF8.encode(buffer, core.goldsign);
            ByteBufCodecs.STRING_UTF8.encode(buffer, core.goldsignStyle.id());
            ByteBufCodecs.STRING_UTF8.encode(buffer, core.resolvedGoldsignStyle.id());
            ICONS_STREAM_CODEC.encode(buffer, core.getIcons());
            ByteBufCodecs.STRING_UTF8.encode(buffer, core.pendingIcon);
            ByteBufCodecs.STRING_UTF8.encode(buffer, core.ironBodyType.id());
            ByteBufCodecs.STRING_UTF8.encode(buffer, core.goldRoute.id());
            buffer.writeDouble(core.soulfire);
            buffer.writeInt(core.selectedTechnique);
            buffer.writeInt(core.ironPreparation);
            buffer.writeInt(core.jadeCycling);
            buffer.writeInt(core.goldCycling);
            buffer.writeInt(core.remnantMastery);
            buffer.writeInt(core.willpower);
        }
    };

    private AdvancementLevel advancement = AdvancementLevel.FOUNDATION;
    private IronBodyType ironBodyType = IronBodyType.NONE;
    private GoldRoute goldRoute = GoldRoute.NONE;
    private boolean herald;
    private boolean sage;
    private boolean monarch;
    private boolean tutorialReceived;
    private boolean soulspace;
    private String goldsign = "";
    private GoldsignStyle goldsignStyle = GoldsignStyle.AUTO;
    private GoldsignStyle resolvedGoldsignStyle = GoldsignStyle.AUTO;
    private final LinkedHashSet<String> icons = new LinkedHashSet<>();
    private String pendingIcon = "strength";
    private final EnumMap<LordRevelation, String> revelations = new EnumMap<>(LordRevelation.class);
    private double soulfire;
    private int selectedTechnique;
    private int ironPreparation;
    private int jadeCycling;
    private int goldCycling;
    private int remnantMastery;
    private int willpower;
    private String bondedPartner = "";
    private String ownRemnantId = "";

    public Core() {
    }

    public AdvancementLevel getAdvancement() { return this.advancement; }
    public String getALevel() { return this.advancement.id(); }
    public void setALevel(String level) { setAdvancement(AdvancementLevel.fromId(level)); }
    public void setAdvancement(AdvancementLevel rank) {
        if (rank != null) {
            this.advancement = rank;
            updateMonarch();
        }
    }
    public IronBodyType getIronBodyType() { return this.ironBodyType; }
    public void setIronBodyType(IronBodyType type) { this.ironBodyType = type == null ? IronBodyType.NONE : type; }
    public boolean hasIronBody() { return this.ironBodyType != IronBodyType.NONE; }
    public void setIronBody(boolean value) { this.ironBodyType = value ? IronBodyType.PERFECT : IronBodyType.NONE; }
    public GoldRoute getGoldRoute() { return this.goldRoute; }
    public void setGoldRoute(GoldRoute route) { this.goldRoute = route == null ? GoldRoute.NONE : route; }
    public boolean hasHerald() { return this.herald; }
    public void setHerald(boolean herald) { this.herald = herald; updateMonarch(); }
    public boolean hasSage() { return this.sage; }
    public void setSage(boolean sage) { this.sage = sage; updateMonarch(); }
    public boolean isMonarch() { return this.monarch || this.advancement == AdvancementLevel.MONARCH; }
    public void setMonarch(boolean monarch) {
        this.monarch = monarch;
        if (monarch) this.advancement = AdvancementLevel.MONARCH;
    }
    public boolean hasReceivedTutorial() { return this.tutorialReceived; }
    public void setTutorialReceived(boolean value) { this.tutorialReceived = value; }
    public String getGoldsign() { return this.goldsign; }
    public void setGoldsign(String goldsign) { this.goldsign = goldsign == null ? "" : goldsign; }
    public GoldsignStyle getGoldsignStyle() { return this.goldsignStyle; }
    public void setGoldsignStyle(GoldsignStyle style) { this.goldsignStyle = style == null ? GoldsignStyle.AUTO : style; }
    public GoldsignStyle getResolvedGoldsignStyle() { return this.resolvedGoldsignStyle; }
    public void setResolvedGoldsignStyle(GoldsignStyle style) { this.resolvedGoldsignStyle = style == null ? GoldsignStyle.AUTO : style; }
    public String getIcon() { return this.icons.stream().findFirst().orElse(""); }
    public List<String> getIcons() { return List.copyOf(this.icons); }
    public void setIcon(String icon) { addIcon(icon); }
    public boolean addIcon(String icon) {
        String concept = normalizeIcon(icon);
        return !concept.isBlank() && this.icons.add(concept);
    }
    public String getPendingIcon() { return this.pendingIcon; }
    public void setPendingIcon(String icon) {
        String concept = normalizeIcon(icon);
        if (!concept.isBlank()) this.pendingIcon = concept;
    }
    public static String normalizeIcon(String icon) {
        if (icon == null) return "";
        String normalized = icon.trim().toLowerCase(Locale.ROOT).replace(' ', '_').replace('-', '_');
        return normalized.matches("[a-z][a-z0-9_]{0,31}") ? normalized : "";
    }
    public boolean hasSoulspace() { return this.soulspace; }
    public void setSoulspace(boolean value) { this.soulspace = value; }
    public boolean hasRevelation(LordRevelation revelation) { return this.revelations.containsKey(revelation); }
    public void recordRevelation(LordRevelation revelation, String statement) { this.revelations.put(revelation, statement); }
    public Map<LordRevelation, String> getRevelations() { return Map.copyOf(this.revelations); }
    public double getSoulfire() { return this.soulfire; }
    public void addSoulfire(double amount) {
        if (Double.isFinite(amount) && amount > 0.0) {
            this.soulfire = normalizeSoulfire(Math.min(MAX_SOULFIRE, this.soulfire + amount));
        }
    }
    public boolean hasSoulfire(double amount) {
        return Double.isFinite(amount) && amount >= 0.0 && this.soulfire + SOULFIRE_EPSILON >= amount;
    }
    public boolean trySpendSoulfire(double amount) {
        if (!hasSoulfire(amount)) return false;
        this.soulfire = normalizeSoulfire(this.soulfire - amount);
        return true;
    }
    private static double normalizeSoulfire(double value) {
        if (!Double.isFinite(value)) return 0.0;
        // Decimal weaving increments must meet the same threshold shown by the HUD.
        return Math.round(Math.max(0.0, Math.min(MAX_SOULFIRE, value)) * SOULFIRE_PRECISION) / SOULFIRE_PRECISION;
    }
    public int getSelectedTechnique() { return this.selectedTechnique; }
    public void setSelectedTechnique(int index) { this.selectedTechnique = Math.max(0, Math.min(3, index)); }
    public int getIronPreparation() { return this.ironPreparation; }
    public int getJadeCycling() { return this.jadeCycling; }
    public boolean hasJadeSpiral() { return this.jadeCycling >= ProgressionRules.JADE_SPIRAL_CYCLES; }
    public int getGoldCycling() { return this.goldCycling; }
    public int getRemnantMastery() { return this.remnantMastery; }
    public boolean hasMasteredRemnant() { return this.goldRoute != GoldRoute.REMNANT || this.remnantMastery >= ProgressionRules.REMNANT_MASTERY_CYCLES; }
    public int getWillpower() { return this.willpower; }
    public String getBondedPartner() { return this.bondedPartner; }
    public void setBondedPartner(String id) { this.bondedPartner = id == null ? "" : id; }
    public String getOwnRemnantId() { return this.ownRemnantId; }
    public void setOwnRemnantId(String id) { this.ownRemnantId = id == null ? "" : id; }

    public void trainCycling() {
        this.willpower = Math.min(1_000_000, this.willpower + 1);
        if (this.advancement == AdvancementLevel.COPPER) this.ironPreparation = Math.min(ProgressionRules.IRON_PREPARATION_CYCLES, this.ironPreparation + 1);
        if (this.advancement == AdvancementLevel.IRON) this.jadeCycling = Math.min(ProgressionRules.JADE_SPIRAL_CYCLES, this.jadeCycling + 1);
        if (this.advancement == AdvancementLevel.JADE) this.goldCycling = Math.min(ProgressionRules.NATURAL_GOLD_CYCLES, this.goldCycling + 1);
        if (this.goldRoute == GoldRoute.REMNANT && this.advancement.order() >= AdvancementLevel.LOWGOLD.order()) {
            this.remnantMastery = Math.min(ProgressionRules.REMNANT_MASTERY_CYCLES, this.remnantMastery + 1);
        }
    }

    public void copyFromCore(Core source) {
        this.advancement = source.advancement;
        this.ironBodyType = source.ironBodyType;
        this.goldRoute = source.goldRoute;
        this.herald = source.herald;
        this.sage = source.sage;
        this.monarch = source.monarch;
        this.tutorialReceived = source.tutorialReceived;
        this.soulspace = source.soulspace;
        this.goldsign = source.goldsign;
        this.goldsignStyle = source.goldsignStyle;
        this.resolvedGoldsignStyle = source.resolvedGoldsignStyle;
        this.icons.clear();
        this.icons.addAll(source.icons);
        this.pendingIcon = source.pendingIcon;
        this.revelations.clear();
        this.revelations.putAll(source.revelations);
        this.soulfire = normalizeSoulfire(source.soulfire);
        this.selectedTechnique = source.selectedTechnique;
        this.ironPreparation = source.ironPreparation;
        this.jadeCycling = source.jadeCycling;
        this.goldCycling = source.goldCycling;
        this.remnantMastery = source.remnantMastery;
        this.willpower = source.willpower;
        this.bondedPartner = source.bondedPartner;
        this.ownRemnantId = source.ownRemnantId;
    }

    private void updateMonarch() {
        if (ProgressionRules.canBecomeMonarch(this.advancement, this.sage, this.herald)) setMonarch(true);
    }
    private void readIcons(Iterable<String> encoded) {
        this.icons.clear();
        for (String icon : encoded) addIcon(icon);
    }

    @Override
    public void serialize(ValueOutput output) {
        output.putInt("progressionVersion", 3);
        output.putString("advancement", this.advancement.id());
        output.putBoolean("ironBody", hasIronBody());
        output.putString("ironBodyType", this.ironBodyType.id());
        output.putString("goldRoute", this.goldRoute.id());
        output.putBoolean("herald", this.herald);
        output.putBoolean("sage", this.sage);
        output.putBoolean("monarch", this.monarch);
        output.putBoolean("tutorialReceived", this.tutorialReceived);
        output.putBoolean("soulspace", this.soulspace);
        output.putString("goldsign", this.goldsign);
        output.putString("goldsignStyle", this.goldsignStyle.id());
        output.putString("resolvedGoldsignStyle", this.resolvedGoldsignStyle.id());
        output.store("icons", ICONS_CODEC, getIcons());
        output.putString("icon", getIcon());
        output.putString("pendingIcon", this.pendingIcon);
        for (LordRevelation revelation : LordRevelation.values()) output.putString("revelation_" + revelation.id(), this.revelations.getOrDefault(revelation, ""));
        output.putDouble("soulfire", this.soulfire);
        output.putInt("selectedTechnique", this.selectedTechnique);
        output.putInt("ironPreparation", this.ironPreparation);
        output.putInt("jadeCycling", this.jadeCycling);
        output.putInt("goldCycling", this.goldCycling);
        output.putInt("remnantMastery", this.remnantMastery);
        output.putInt("willpower", this.willpower);
        output.putString("bondedPartner", this.bondedPartner);
        output.putString("ownRemnantId", this.ownRemnantId);
    }

    @Override
    public void deserialize(ValueInput input) {
        String oldRank = input.read("aLevel", Codec.STRING).orElse(AdvancementLevel.FOUNDATION.id());
        this.advancement = AdvancementLevel.fromId(input.read("advancement", Codec.STRING).orElse(oldRank));
        if (this.advancement == AdvancementLevel.UNFOUNDED) this.advancement = AdvancementLevel.FOUNDATION;
        boolean legacy = input.getIntOr("progressionVersion", 0) < 2;
        boolean reachedIron = this.advancement.order() >= AdvancementLevel.IRON.order();
        boolean reachedJade = this.advancement.order() >= AdvancementLevel.JADE.order();
        boolean reachedGold = this.advancement.order() >= AdvancementLevel.LOWGOLD.order();
        boolean reachedLord = this.advancement.order() >= AdvancementLevel.UNDERLORD.order();
        this.ironBodyType = IronBodyType.fromId(input.read("ironBodyType", Codec.STRING).orElse(
                legacy && (input.getBooleanOr("ironBody", false) || reachedIron) ? "perfect" : "none"));
        this.herald = input.getBooleanOr("herald", false);
        this.sage = input.getBooleanOr("sage", false);
        this.monarch = input.getBooleanOr("monarch", this.advancement == AdvancementLevel.MONARCH);
        this.tutorialReceived = input.getBooleanOr("tutorialReceived", false);
        this.goldsign = input.read("goldsign", Codec.STRING).orElse("");
        this.goldsignStyle = GoldsignStyle.fromId(input.read("goldsignStyle", Codec.STRING).orElse("auto"));
        this.resolvedGoldsignStyle = GoldsignStyle.fromId(input.read("resolvedGoldsignStyle", Codec.STRING)
                .orElseGet(() -> GoldsignStyle.resolve(this, null).id()));
        this.goldRoute = GoldRoute.fromId(input.read("goldRoute", Codec.STRING).orElse(
                reachedGold ? (this.goldsign.isBlank() ? "natural" : "remnant") : "none"));
        // Accept the former comma-separated field and the original single-Icon field.
        readIcons(input.read("icons", Codec.either(ICONS_CODEC, Codec.STRING))
                .map(encoded -> encoded.map(list -> list, csv -> List.of(csv.split(","))))
                .orElseGet(() -> List.of(input.read("icon", Codec.STRING).orElse(""))));
        this.sage |= !this.icons.isEmpty();
        this.pendingIcon = input.read("pendingIcon", Codec.STRING).orElse("strength");
        this.soulspace = input.getBooleanOr("soulspace", legacy && reachedLord);
        this.revelations.clear();
        for (LordRevelation revelation : LordRevelation.values()) {
            String statement = input.read("revelation_" + revelation.id(), Codec.STRING).orElse(
                    legacy && this.advancement.order() >= revelation.target().order() ? "legacy achievement" : "");
            if (!statement.isBlank()) this.revelations.put(revelation, statement);
        }
        this.soulfire = normalizeSoulfire(input.read("soulfire", Codec.DOUBLE).orElse(0.0));
        setSelectedTechnique(input.getIntOr("selectedTechnique", 0));
        this.ironPreparation = Math.max(0, input.getIntOr("ironPreparation", legacy && reachedIron ? ProgressionRules.IRON_PREPARATION_CYCLES : 0));
        this.jadeCycling = Math.max(0, input.getIntOr("jadeCycling", legacy && reachedJade ? ProgressionRules.JADE_SPIRAL_CYCLES : 0));
        this.goldCycling = Math.max(0, input.getIntOr("goldCycling", legacy && reachedGold ? ProgressionRules.NATURAL_GOLD_CYCLES : 0));
        this.remnantMastery = Math.max(0, input.getIntOr("remnantMastery", legacy && reachedLord ? ProgressionRules.REMNANT_MASTERY_CYCLES : 0));
        this.willpower = Math.max(0, input.getIntOr("willpower", legacy && (this.sage || this.herald) ? ProgressionRules.HERALD_WILLPOWER : 0));
        this.bondedPartner = input.read("bondedPartner", Codec.STRING).orElse("");
        this.ownRemnantId = input.read("ownRemnantId", Codec.STRING).orElse("");
        updateMonarch();
    }
}
