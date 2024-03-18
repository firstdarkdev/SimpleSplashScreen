package com.hypherionmc.simplesplashscreen.client.config;

import com.hypherionmc.simplesplashscreen.SimpleSplashScreenCommon;
import com.hypherionmc.simplesplashscreen.client.preview.ReloadPreviewScreen;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.serializer.ConfigSerializer;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.gui.entries.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

/**
 * @author HypherionSA
 * Config Screen for configurating the mod
 */
public class SimpleSplashScreenConfigGui {

    public static Screen getConfigScreen(Screen parent) {
        final ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.translatable("text.autoconfig.simplesplashscreen.title"));

        ConfigData configData = new ConfigData(builder);

        builder.setSavingRunnable(() -> {
            SimpleSplashScreenConfig config1 = configData.getConfig();
            JanksonConfigSerializer<SimpleSplashScreenConfig> janksonConfigSerializer = new JanksonConfigSerializer<>(SimpleSplashScreenConfig.class.getAnnotation(Config.class), SimpleSplashScreenConfig.class);
            try {
                janksonConfigSerializer.serialize(config1);
            } catch (ConfigSerializer.SerializationException e) {
                e.printStackTrace();
            }
            SimpleSplashScreenCommon.CS_CONFIG = config1;
        });

        return builder.build();
    }

    public static MutableComponent translationKey(String id) {
        return Component.translatable("text.autoconfig.simplesplashscreen.option." + id);
    }

    public static MutableComponent tooltipTranslationKey(String id) {
        return Component.translatable("text.autoconfig.simplesplashscreen.tooltip." + id);
    }

    public static class ConfigData {

        private final ConfigEntryBuilder builder;

        private final BooleanListEntry backgroundImage, customProgressBarBackground;
        private final EnumListEntry<?> progressBarType, logoStyle, customProgressBarMode, bossBarType, bossBarColor;
        private final ColorEntry backgroundColor, progressBarColor, progressFrameColor;
        private final StringListEntry BackgroundTexture, MojangLogo, Aspect1to1Logo, BossBarTexture, CustomBarTexture, CustomBarBackgroundTexture;

        public ConfigData(ConfigBuilder builder) {
            this.builder = builder.entryBuilder();
            SimpleSplashScreenConfig oldConfig = SimpleSplashScreenCommon.CS_CONFIG;

            // Logos
            ConfigCategory logo = builder.getOrCreateCategory(SimpleSplashScreenConfigGui.translationKey("logo"));
            logoStyle = createEnumEntry("logoStyle", SimpleSplashScreenConfig.LogoStyle.class, SimpleSplashScreenCommon.CS_CONFIG.logoStyle, SimpleSplashScreenConfig.LogoStyle.Mojang, logo);
            MojangLogo = createListEntry("textures.MojangLogo", SimpleSplashScreenCommon.CS_CONFIG.textures.MojangLogo, "mojangstudios.png", logo);
            Aspect1to1Logo = createListEntry("textures.Aspect1to1Logo", SimpleSplashScreenCommon.CS_CONFIG.textures.Aspect1to1Logo, "mojank.png", logo);

            // Progress Bar
            ConfigCategory progressBar = builder.getOrCreateCategory(SimpleSplashScreenConfigGui.translationKey("progress"));
            progressBarType = createEnumEntry("progressBarType", SimpleSplashScreenConfig.ProgressBarType.class, SimpleSplashScreenCommon.CS_CONFIG.progressBarType, SimpleSplashScreenConfig.ProgressBarType.Vanilla, progressBar);
            progressBarColor = createColorEntry("progressBarColor", SimpleSplashScreenCommon.CS_CONFIG.progressBarColor, 16777215, progressBar);
            progressFrameColor = createColorEntry("progressFrameColor", SimpleSplashScreenCommon.CS_CONFIG.progressFrameColor, 16777215, progressBar);

            // Custom Progress Bar
            createEmptyEntry(progressBar);
            customProgressBarMode = createEnumEntry("customProgressBarMode", SimpleSplashScreenConfig.ProgressBarMode.class, SimpleSplashScreenCommon.CS_CONFIG.progressBarType, SimpleSplashScreenConfig.ProgressBarMode.Linear, progressBar);
            customProgressBarBackground = createBooleanEntry("customProgressBarBackground", SimpleSplashScreenCommon.CS_CONFIG.backgroundImage, false, progressBar);
            CustomBarTexture = createListEntry("textures.CustomBarTexture", SimpleSplashScreenCommon.CS_CONFIG.textures.CustomBarTexture, "progressbar.png", progressBar);
            CustomBarBackgroundTexture = createListEntry("textures.CustomBarBackgroundTexture", SimpleSplashScreenCommon.CS_CONFIG.textures.CustomBarBackgroundTexture, "progressbar_background.png", progressBar);

            // Boss Bar
            createEmptyEntry(progressBar);
            bossBarColor = createEnumEntry("bossBarColor", SimpleSplashScreenConfig.BossBarColor.class, SimpleSplashScreenCommon.CS_CONFIG.bossBarColor, SimpleSplashScreenConfig.BossBarColor.MAGENTA, progressBar);
            bossBarType = createEnumEntry("bossBarType", SimpleSplashScreenConfig.BossBarType.class, SimpleSplashScreenCommon.CS_CONFIG.bossBarType, SimpleSplashScreenConfig.BossBarType.NOTCHED_6, progressBar);
            BossBarTexture = createListEntry("textures.BossBarTexture", SimpleSplashScreenCommon.CS_CONFIG.textures.BossBarTexture, "textures/gui/bars.png", progressBar);

            // Background
            ConfigCategory bg = builder.getOrCreateCategory(SimpleSplashScreenConfigGui.translationKey("background"));
            backgroundImage = createBooleanEntry("backgroundImage", SimpleSplashScreenCommon.CS_CONFIG.backgroundImage, false, bg);
            backgroundColor = createColorEntry("backgroundColor", SimpleSplashScreenCommon.CS_CONFIG.backgroundColor, 15675965, bg);
            BackgroundTexture = createListEntry("textures.BackgroundTexture", SimpleSplashScreenCommon.CS_CONFIG.textures.BackgroundTexture, "background.png", bg);

            // Buttons
            logo.addEntry(new ButtonConfigEntry(Component.translatable("text.autoconfig.simplesplashscreen.button.preview"), button -> showPreviewScreen(oldConfig)));
            progressBar.addEntry(new ButtonConfigEntry(Component.translatable("text.autoconfig.simplesplashscreen.button.preview"), button -> showPreviewScreen(oldConfig)));
            bg.addEntry(new ButtonConfigEntry(Component.translatable("text.autoconfig.simplesplashscreen.button.preview"), button -> showPreviewScreen(oldConfig)));
        }

        private void showPreviewScreen(SimpleSplashScreenConfig oldConfig) {
            SimpleSplashScreenCommon.CS_CONFIG = getConfig();
            LoadingOverlay.registerTextures(Minecraft.getInstance());
            Minecraft.getInstance().setOverlay(new ReloadPreviewScreen(500, () -> SimpleSplashScreenCommon.CS_CONFIG = oldConfig));
        }

        private BooleanListEntry createBooleanEntry(String id, boolean value, boolean defaultValue, ConfigCategory configCategory) {
            final BooleanListEntry entry = builder.startBooleanToggle(translationKey(id), value)
                    .setDefaultValue(defaultValue)
                    .setTooltip(tooltipTranslationKey(id)).build();
            configCategory.addEntry(entry);
            return entry;
        }

        private EnumListEntry<?> createEnumEntry(String id, Class type, Enum<?> value, Enum<?> defaultValue, ConfigCategory configCategory) {
            final EnumListEntry<?> entry = builder.startEnumSelector(translationKey(id), type, value)
                    .setDefaultValue(defaultValue)
                    .setTooltip(tooltipTranslationKey(id)).build();
            configCategory.addEntry(entry);
            return entry;
        }

        private ColorEntry createColorEntry(String id, int value, int defaultValue, ConfigCategory configCategory) {
            final ColorEntry entry = builder.startColorField(translationKey(id), value)
                    .setDefaultValue(defaultValue)
                    .setTooltip(tooltipTranslationKey(id)).build();
            configCategory.addEntry(entry);
            return entry;
        }

        private void createEmptyEntry(ConfigCategory configCategory) {
            configCategory.addEntry(new EmptyEntry(20));
        }

        private StringListEntry createListEntry(String id, String value, String defaultValue, ConfigCategory configCategory) {
            final StringListEntry entry = builder.startStrField(translationKey(id), value)
                    .setDefaultValue(defaultValue).build();
            configCategory.addEntry(entry);
            return entry;
        }

        public SimpleSplashScreenConfig getConfig() {

            SimpleSplashScreenConfig.Textures textures = new SimpleSplashScreenConfig.Textures();
            textures.BackgroundTexture = BackgroundTexture.getValue();
            textures.MojangLogo = MojangLogo.getValue();
            textures.Aspect1to1Logo = Aspect1to1Logo.getValue();
            textures.BossBarTexture = BossBarTexture.getValue();
            textures.CustomBarTexture = CustomBarTexture.getValue();
            textures.CustomBarBackgroundTexture = CustomBarBackgroundTexture.getValue();

            return new SimpleSplashScreenConfig((SimpleSplashScreenConfig.ProgressBarType)progressBarType.getValue(),
                    (SimpleSplashScreenConfig.LogoStyle)logoStyle.getValue(),
                    backgroundImage.getValue(),
                    backgroundColor.getValue(),
                    progressBarColor.getValue(),
                    progressFrameColor.getValue(),
                    (SimpleSplashScreenConfig.ProgressBarMode)customProgressBarMode.getValue(),
                    customProgressBarBackground.getValue(),
                    (SimpleSplashScreenConfig.BossBarColor)bossBarColor.getValue(),
                    (SimpleSplashScreenConfig.BossBarType)bossBarType.getValue(),
                    textures);
        }
    }
}
