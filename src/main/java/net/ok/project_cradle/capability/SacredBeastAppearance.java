package net.ok.project_cradle.capability;

import java.util.List;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.ok.project_cradle.capability.madra.MadraType.Element;

/** A small public snapshot; gameplay history and techniques remain server-only. */
public record SacredBeastAppearance(boolean sacred, int primary, int secondary, int variant) {
    public static final SacredBeastAppearance NONE = new SacredBeastAppearance(false, Element.PURE.ordinal(), -1, 0);
    public static final MapCodec<SacredBeastAppearance> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("sacred", false).forGetter(SacredBeastAppearance::sacred),
            Codec.INT.optionalFieldOf("primary", Element.PURE.ordinal()).forGetter(SacredBeastAppearance::primary),
            Codec.INT.optionalFieldOf("secondary", -1).forGetter(SacredBeastAppearance::secondary),
            Codec.INT.optionalFieldOf("variant", 0).forGetter(SacredBeastAppearance::variant)
    ).apply(instance, SacredBeastAppearance::new));
    public static final StreamCodec<ByteBuf, SacredBeastAppearance> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, SacredBeastAppearance::sacred,
            ByteBufCodecs.VAR_INT, SacredBeastAppearance::primary,
            ByteBufCodecs.VAR_INT, SacredBeastAppearance::secondary,
            ByteBufCodecs.VAR_INT, SacredBeastAppearance::variant,
            SacredBeastAppearance::new
    );

    public SacredBeastAppearance {
        if (primary < 0 || primary >= Element.values().length) primary = Element.PURE.ordinal();
        if (secondary < 0 || secondary >= Element.values().length || secondary == primary) secondary = -1;
        variant = Math.floorMod(variant, 4);
        if (!sacred) {
            primary = Element.PURE.ordinal();
            secondary = -1;
            variant = 0;
        }
    }

    public static SacredBeastAppearance of(List<Element> aspects, int variant) {
        Element primary = aspects == null || aspects.isEmpty() || aspects.get(0) == null ? Element.PURE : aspects.get(0);
        Element secondary = aspects != null && aspects.size() > 1 ? aspects.get(1) : null;
        return new SacredBeastAppearance(true, primary.ordinal(), secondary == null ? -1 : secondary.ordinal(), variant);
    }

    public Element primaryElement() { return Element.byOrdinal(this.primary); }

    public boolean hasSecondaryAspect() { return this.secondary >= 0; }

    public Element secondaryElement() { return this.hasSecondaryAspect() ? Element.byOrdinal(this.secondary) : this.primaryElement(); }
}
