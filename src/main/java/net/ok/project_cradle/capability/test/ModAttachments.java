package net.ok.project_cradle.capability.test;

import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

import net.ok.project_cradle.ProjectCradle;

public class ModAttachments {

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(
                    NeoForgeRegistries.ATTACHMENT_TYPES,
                    ProjectCradle.MODID
            );

    public static final Supplier<AttachmentType<Shaper>> SHAPER =
            ATTACHMENT_TYPES.register(
                    "shaper",
                    () -> AttachmentType.serializable(Shaper::new).build()
            );
}