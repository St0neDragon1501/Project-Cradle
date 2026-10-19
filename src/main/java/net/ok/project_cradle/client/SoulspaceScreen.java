package net.ok.project_cradle.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.ok.project_cradle.menus.SoulspaceMenu;

/** Presentation for the item stored in the player's soulspace. */
public final class SoulspaceScreen extends AbstractContainerScreen<SoulspaceMenu> {
    public SoulspaceScreen(SoulspaceMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 176, 156);
        this.titleLabelX = (this.imageWidth - this.font.width(title)) / 2;
        this.inventoryLabelY = 56;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        graphics.fill(this.leftPos, this.topPos, this.leftPos + this.imageWidth,
                this.topPos + this.imageHeight, 0xEC101925);
        graphics.outline(this.leftPos, this.topPos, this.imageWidth, this.imageHeight, 0xFF9DB8D1);

        for (Slot slot : this.menu.slots) {
            int x = this.leftPos + slot.x;
            int y = this.topPos + slot.y;
            boolean soulspaceSlot = slot.index == 0;
            graphics.fill(x - 1, y - 1, x + 17, y + 17,
                    soulspaceSlot ? 0xFFE7C870 : 0xFF8A9BAA);
            graphics.fill(x, y, x + 16, y + 16,
                    soulspaceSlot ? 0xFF293552 : 0xFF29313A);
        }
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractLabels(graphics, mouseX, mouseY);
        graphics.centeredText(this.font,
                Component.translatable("screen.project_cradle.soulspace.storage"),
                this.imageWidth / 2, 17, 0xFFD9E8FA);
    }
}
