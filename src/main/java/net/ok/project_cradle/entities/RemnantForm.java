package net.ok.project_cradle.entities;

import java.util.Locale;

/** The broad shape retained by a spirit after its living body is gone. */
public enum RemnantForm {
    HUMANOID,
    BEAST,
    TREE;

    public String id() {
        return this.name().toLowerCase(Locale.ROOT);
    }

    public static RemnantForm byOrdinal(int ordinal) {
        return ordinal >= 0 && ordinal < values().length ? values()[ordinal] : HUMANOID;
    }

    public static RemnantForm byId(String id) {
        if (id != null) {
            for (RemnantForm form : values()) {
                if (form.id().equalsIgnoreCase(id)) return form;
            }
        }
        return HUMANOID;
    }
}
