package net.ok.project_cradle.client;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.ok.project_cradle.capability.ModAttachments;
import net.ok.project_cradle.capability.core.Core;
import net.ok.project_cradle.menus.TechniqueWheelMenu;
import net.ok.project_cradle.networking.packet.SelectTechniqueC2S;

/** Radial selector for the techniques assigned to quick slots one through four. */
public final class TechniqueWheelScreen extends AbstractContainerScreen<TechniqueWheelMenu> {
    public TechniqueWheelScreen(TechniqueWheelMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 200, 170);
        this.titleLabelX = (this.imageWidth - this.font.width(title)) / 2;
        this.titleLabelY = 5;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        graphics.fill(this.leftPos, this.topPos, this.leftPos + this.imageWidth,
                this.topPos + this.imageHeight, 0xE0101820);
        graphics.outline(this.leftPos, this.topPos, this.imageWidth, this.imageHeight, 0xFF8B9BA8);

        for (Slot slot : this.menu.slots) {
            int x = this.leftPos + slot.x;
            int y = this.topPos + slot.y;
            graphics.fill(x - 2, y - 2, x + 18, y + 18, 0xFF90A4AE);
            graphics.fill(x, y, x + 16, y + 16, 0xFF202A30);
        }
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractLabels(graphics, mouseX, mouseY);
        Core core = Minecraft.getInstance().player.getData(ModAttachments.CORE);
        int selected = core.getSelectedTechnique();
        int unlocked = Math.min(TechniqueWheelMenu.TECHNIQUE_SLOT_COUNT,
                core.getAdvancement().techniqueSlots());

        for (int index = 0; index < TechniqueWheelMenu.TECHNIQUE_SLOT_COUNT; index++) {
            Slot slot = this.menu.slots.get(index);
            int color = index == selected ? 0xFFFFD54F : index < unlocked ? 0xFFFFFFFF : 0xFF777777;
            graphics.centeredText(this.font, Integer.toString(index + 1), slot.x + 8, slot.y - 11, color);
        }

        graphics.centeredText(this.font,
                Component.translatable("screen.project_cradle.technique_wheel.instructions"),
                this.imageWidth / 2, this.imageHeight - 12, 0xFFD7E3EA);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 0) {
            for (int index = 0; index < this.menu.slots.size(); index++) {
                Slot slot = this.menu.slots.get(index);
                int x = this.leftPos + slot.x;
                int y = this.topPos + slot.y;
                if (event.x() >= x - 3 && event.x() < x + 19
                        && event.y() >= y - 3 && event.y() < y + 19) {
                    selectTechnique(index);
                    return true;
                }
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        int keyCode = event.key();
        if (keyCode >= GLFW.GLFW_KEY_1 && keyCode <= GLFW.GLFW_KEY_4) {
            selectTechnique(keyCode - GLFW.GLFW_KEY_1);
            return true;
        }
        return super.keyPressed(event);
    }

    private void selectTechnique(int slot) {
        ClientPacketDistributor.sendToServer(new SelectTechniqueC2S(slot));
        this.onClose();
    }
}
