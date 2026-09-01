package net.ok.project_cradle.capability;

import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

import net.ok.project_cradle.ProjectCradle;
import net.ok.project_cradle.capability.core.Core;
import net.ok.project_cradle.capability.madra.*;

public class ModAttachments {

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(
                    NeoForgeRegistries.ATTACHMENT_TYPES,
                    ProjectCradle.MODID
            );

    
    public static final Supplier<AttachmentType<Core>> CORE =
            ATTACHMENT_TYPES.register(
                    "core",
                    () -> AttachmentType.serializable(Core::new).build()
            );
    
    
    public static final Supplier<AttachmentType<Madra>> MADRA =
            ATTACHMENT_TYPES.register(
                    "madra",
                    () -> AttachmentType.serializable(Madra::new).build()
            );
    
    
    public static final Supplier<AttachmentType<MaxMadra>> MAXMADRA =
            ATTACHMENT_TYPES.register(
                    "max_madra",
                    () -> AttachmentType.serializable(MaxMadra::new).build()
            );
    
    
    
}