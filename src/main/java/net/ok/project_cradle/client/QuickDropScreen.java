package net.ok.project_cradle.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.ok.project_cradle.menus.QuickDropMenu;

public class QuickDropScreen extends AbstractContainerScreen<QuickDropMenu> {
    public QuickDropScreen(QuickDropMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 176, 156);
        this.inventoryLabelY = 56;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);

        graphics.fill(
                this.leftPos,
                this.topPos,
                this.leftPos + this.imageWidth,
                this.topPos + this.imageHeight,
                0xE0101010
        );
        graphics.outline(this.leftPos, this.topPos, this.imageWidth, this.imageHeight, 0xFF777777);

        for (Slot slot : this.menu.slots) {
            int x = this.leftPos + slot.x;
            int y = this.topPos + slot.y;
            graphics.fill(x - 1, y - 1, x + 17, y + 17, 0xFFB0B0B0);
            graphics.fill(x, y, x + 16, y + 16, 0xFF373737);
        }
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractLabels(graphics, mouseX, mouseY);

        for (int slot = 0; slot < QuickDropMenu.QUICK_DROP_SLOT_COUNT; slot++) {
            graphics.centeredText(this.font, Integer.toString(slot + 1), 51 + slot * 18, 17, 0xFFFFFFFF);
        }
    }
}
