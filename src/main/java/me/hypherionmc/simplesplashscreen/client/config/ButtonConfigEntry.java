package me.hypherionmc.simplesplashscreen.client.config;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.ITextComponent;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class ButtonConfigEntry extends AbstractConfigListEntry<Void> {

    private final Button button;

    public ButtonConfigEntry(ITextComponent fieldName, Button.IPressable onPress) {
        super(fieldName, false);
        final int width = Minecraft.getInstance().font.width(fieldName) + 24;
        button = new Button(0, 0, width, 20, fieldName, onPress);
    }

    @Override
    public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isHovered, float delta) {
        button.x = x + (entryWidth - button.getWidth()) / 2;
        button.y = y + (entryHeight - 20) / 2;
        button.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public Void getValue() { return null; }

    @Override
    public Optional<Void> getDefaultValue() { return Optional.empty(); }

    @Override
    public void save() {}

    @Override
    public List<? extends IGuiEventListener> children() {
        return Collections.singletonList(button);
    }
}
