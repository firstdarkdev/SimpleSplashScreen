package me.hypherionmc.simplesplashscreen.client.config;

import com.mojang.blaze3d.vertex.PoseStack;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;

import java.util.Collections;

import java.util.List;
import java.util.Optional;

public class ButtonConfigEntry extends AbstractConfigListEntry<Void> {

    private final Button button;

    public ButtonConfigEntry(Component fieldName, Button.OnPress onPress) {
        super(fieldName, false);
        final int width = Minecraft.getInstance().font.width(fieldName) + 24;
        button = Button.builder(fieldName, onPress).width(width).pos(0, 0).build();
    }

    @Override
    public void render(PoseStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isHovered, float delta) {
        button.setX(x + (entryWidth - button.getWidth()) / 2);
        button.setY(y + (entryHeight - 20) / 2);
        button.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public Void getValue() { return null; }

    @Override
    public Optional<Void> getDefaultValue() { return Optional.empty(); }

    @Override
    public void save() {}

    @Override
    public List<? extends GuiEventListener> children() {
        return Collections.singletonList(button);
    }

    @Override
    public List<? extends NarratableEntry> narratables() {
        return Collections.singletonList(button);
    }
}
