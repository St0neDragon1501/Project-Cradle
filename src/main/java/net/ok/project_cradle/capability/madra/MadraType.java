package net.ok.project_cradle.capability.madra;

import java.util.List;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;

public class MadraType implements ValueIOSerializable {
    public static final StreamCodec<ByteBuf, MadraType> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            type -> type.first.ordinal(),
            ByteBufCodecs.VAR_INT,
            type -> type.second == null ? -1 : type.second.ordinal(),
            MadraType::new
    );

    private Element first = Element.FIRE;
    private Element second;

    public MadraType() {
    }

    private MadraType(int firstOrdinal, int secondOrdinal) {
        this.first = Element.fromOrdinal(firstOrdinal, Element.FIRE);
        Element decodedSecond = Element.fromOrdinal(secondOrdinal, null);
        this.second = decodedSecond == this.first ? null : decodedSecond;
    }

    public List<Element> getTypes() {
        return this.second == null ? List.of(this.first) : List.of(this.first, this.second);
    }

    public boolean addType(Element element) {
        if (element == null || this.second != null || this.first == element) {
            return false;
        }

        this.second = element;
        return true;
    }

    public boolean removeType(Element element) {
        if (element == null) {
            return false;
        }
        if (this.first == element) {
            if (this.second == null) {
                return false;
            }
            this.first = this.second;
            this.second = null;
            return true;
        }
        if (this.second == element) {
            this.second = null;
            return true;
        }
        return false;
    }

    public boolean setTypes(List<Element> types) {
        if (types == null || types.isEmpty() || types.size() > 2
                || types.get(0) == null
                || (types.size() == 2 && (types.get(1) == null || types.get(0) == types.get(1)))) {
            return false;
        }

        this.first = types.get(0);
        this.second = types.size() == 2 ? types.get(1) : null;
        return true;
    }

    public Element cycleType(boolean secondarySlot) {
        if (secondarySlot && this.second == null) {
            this.second = nextElement(this.first, null);
            return this.second;
        }

        Element current = secondarySlot ? this.second : this.first;
        Element other = secondarySlot ? this.first : this.second;
        Element next = nextElement(current, other);
        if (secondarySlot) {
            this.second = next;
        } else {
            this.first = next;
        }
        return next;
    }

    public void copyFrom(MadraType source) {
        this.first = source.first;
        this.second = source.second;
    }

    private static Element nextElement(Element current, Element excluded) {
        Element next = current;
        do {
            next = Element.values()[(next.ordinal() + 1) % Element.values().length];
        } while (next == excluded);
        return next;
    }

    @Override
    public void serialize(ValueOutput output) {
        output.putString("first", this.first.id);
        if (this.second != null) {
            output.putString("second", this.second.id);
        }
    }

    @Override
    public void deserialize(ValueInput input) {
        this.first = Element.fromId(input.read("first", Codec.STRING).orElse(Element.FIRE.id), Element.FIRE);
        Element decodedSecond = Element.fromId(input.read("second", Codec.STRING).orElse(""), null);
        this.second = decodedSecond == this.first ? null : decodedSecond;
    }

    public enum Element {
        FIRE("fire"),
        WATER("water"),
        AIR("air"),
        EARTH("earth");

        private final String id;

        Element(String id) {
            this.id = id;
        }

        public String translationKey() {
            return "madra_type.project_cradle." + this.id;
        }

        public static Element byOrdinal(int ordinal) {
            return fromOrdinal(ordinal, FIRE);
        }

        private static Element fromId(String id, Element fallback) {
            for (Element element : values()) {
                if (element.id.equalsIgnoreCase(id)) {
                    return element;
                }
            }
            return fallback;
        }

        private static Element fromOrdinal(int ordinal, Element fallback) {
            return ordinal >= 0 && ordinal < values().length ? values()[ordinal] : fallback;
        }
    }
}
