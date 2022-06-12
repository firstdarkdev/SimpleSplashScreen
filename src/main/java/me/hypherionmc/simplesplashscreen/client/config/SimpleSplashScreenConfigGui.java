package me.hypherionmc.simplesplashscreen.client.config;

import me.hypherionmc.simplesplashscreen.SimpleSplashScreen;
import me.hypherionmc.simplesplashscreen.client.preview.ReloadPreviewScreen;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.serializer.ConfigSerializer;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.gui.entries.BooleanListEntry;
import me.shedaniel.clothconfig2.gui.entries.ColorEntry;
import me.shedaniel.clothconfig2.gui.entries.EnumListEntry;
import me.shedaniel.clothconfig2.gui.entries.StringListEntry;
import me.shedaniel.clothconfig2.impl.builders.SubCategoryBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class SimpleSplashScreenConfigGui {

    public static Screen getConfigScreen(Screen parent) {
        final ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.translatable("text.autoconfig.simplesplashscreen.title"));

        final SimpleSplashScreenConfig oldConfig = SimpleSplashScreen.CS_CONFIG;

        ConfigCategory configCategory = builder.getOrCreateCategory(Component.translatable(""));
        ConfigData configData = new ConfigData(builder.entryBuilder(), configCategory);
        configCategory.addEntry(new ButtonConfigEntry(Component.translatable("text.autoconfig.simplesplashscreen.button.preview"), button -> {
            SimpleSplashScreen.CS_CONFIG = configData.getConfig();
            LoadingOverlay.registerTextures(Minecraft.getInstance());
            Minecraft.getInstance().setOverlay(new ReloadPreviewScreen(500, () -> SimpleSplashScreen.CS_CONFIG = oldConfig));
        }));

        builder.setSavingRunnable(() -> {
            SimpleSplashScreenConfig config1 = configData.getConfig();
            JanksonConfigSerializer<SimpleSplashScreenConfig> janksonConfigSerializer = new JanksonConfigSerializer<SimpleSplashScreenConfig>(SimpleSplashScreenConfig.class.getAnnotation(Config.class), SimpleSplashScreenConfig.class);
            try {
                janksonConfigSerializer.serialize(config1);
            } catch (ConfigSerializer.SerializationException e) {
                e.printStackTrace();
            }
            SimpleSplashScreen.CS_CONFIG = config1;
        });

        return builder.build();
    }

    private static MutableComponent translationKey(String id) {
        return Component.translatable("text.autoconfig.simplesplashscreen.option." + id);
    }

    private static MutableComponent tooltipTranslationKey(String id) {
        return Component.translatable("text.autoconfig.simplesplashscreen.tooltip." + id);
    }

    public static class ConfigData {

        private final ConfigEntryBuilder builder;
        private final ConfigCategory configCategory;

        private final BooleanListEntry showProgressText, backgroundImage, customProgressBarBackground;
        private final EnumListEntry progressBarType, logoStyle, customProgressBarMode, bossBarType;
        private final ColorEntry backgroundColor, progressBarColor, progressFrameColor;
        private final StringListEntry BackgroundTexture, MojangLogo, Aspect1to1Logo, BossBarTexture, CustomBarTexture, CustomBarBackgroundTexture;

        public ConfigData(ConfigEntryBuilder builder, ConfigCategory category) {
            this.builder = builder;
            this.configCategory = category;

            showProgressText = createBooleanEntry("showProgressText", SimpleSplashScreen.CS_CONFIG.showProgressText, true);
            progressBarType = createEnumEntry("progressBarType", SimpleSplashScreenConfig.ProgressBarType.class, SimpleSplashScreen.CS_CONFIG.progressBarType, SimpleSplashScreenConfig.ProgressBarType.Vanilla);
            logoStyle = createEnumEntry("logoStyle", SimpleSplashScreenConfig.LogoStyle.class, SimpleSplashScreen.CS_CONFIG.logoStyle, SimpleSplashScreenConfig.LogoStyle.Mojang);
            backgroundImage = createBooleanEntry("backgroundImage", SimpleSplashScreen.CS_CONFIG.backgroundImage, false);
            backgroundColor = createColorEntry("backgroundColor", SimpleSplashScreen.CS_CONFIG.backgroundColor, 15675965);
            progressBarColor = createColorEntry("progressBarColor", SimpleSplashScreen.CS_CONFIG.progressBarColor, 16777215);
            progressFrameColor = createColorEntry("progressFrameColor", SimpleSplashScreen.CS_CONFIG.progressFrameColor, 16777215);
            customProgressBarMode = createEnumEntry("customProgressBarMode", SimpleSplashScreenConfig.ProgressBarMode.class, SimpleSplashScreen.CS_CONFIG.progressBarType, SimpleSplashScreenConfig.ProgressBarMode.Linear);
            customProgressBarBackground = createBooleanEntry("customProgressBarBackground", SimpleSplashScreen.CS_CONFIG.backgroundImage, false);
            bossBarType = createEnumEntry("bossBarType", SimpleSplashScreenConfig.BossBarType.class, SimpleSplashScreen.CS_CONFIG.bossBarType, SimpleSplashScreenConfig.BossBarType.NOTCHED_6);

            SubCategoryBuilder subCategoryBuilder = builder.startSubCategory(translationKey("textures"));
            BackgroundTexture = createListEntry(subCategoryBuilder, "textures.BackgroundTexture", SimpleSplashScreen.CS_CONFIG.textures.BackgroundTexture, "background.png");
            MojangLogo = createListEntry(subCategoryBuilder, "textures.MojangLogo", SimpleSplashScreen.CS_CONFIG.textures.MojangLogo, "mojangstudios.png");
            Aspect1to1Logo = createListEntry(subCategoryBuilder, "textures.Aspect1to1Logo", SimpleSplashScreen.CS_CONFIG.textures.Aspect1to1Logo, "mojank.png");
            BossBarTexture = createListEntry(subCategoryBuilder, "textures.BossBarTexture", SimpleSplashScreen.CS_CONFIG.textures.BossBarTexture, "textures/gui/bars.png");
            CustomBarTexture = createListEntry(subCategoryBuilder, "textures.CustomBarTexture", SimpleSplashScreen.CS_CONFIG.textures.CustomBarTexture, "progressbar.png");
            CustomBarBackgroundTexture = createListEntry(subCategoryBuilder, "textures.CustomBarBackgroundTexture", SimpleSplashScreen.CS_CONFIG.textures.CustomBarBackgroundTexture, "progressbar_background.png");
            configCategory.addEntry(subCategoryBuilder.build());

        }

        private BooleanListEntry createBooleanEntry(String id, boolean value, boolean defaultValue) {
            final BooleanListEntry entry = builder.startBooleanToggle(translationKey(id), value)
                    .setDefaultValue(defaultValue)
                    .setTooltip(tooltipTranslationKey(id)).build();
            configCategory.addEntry(entry);
            return entry;
        }

        private EnumListEntry createEnumEntry(String id, Class type, Enum value, Enum defaultValue) {
            final EnumListEntry entry = builder.startEnumSelector(translationKey(id), type, value)
                    .setDefaultValue(defaultValue)
                    .setTooltip(tooltipTranslationKey(id)).build();
            configCategory.addEntry(entry);
            return entry;
        }

        private ColorEntry createColorEntry(String id, int value, int defaultValue) {
            final ColorEntry entry = builder.startColorField(translationKey(id), value)
                    .setDefaultValue(defaultValue)
                    .setTooltip(tooltipTranslationKey(id)).build();
            configCategory.addEntry(entry);
            return entry;
        }

        private StringListEntry createListEntry(SubCategoryBuilder subCategoryBuilder, String id, String value, String defaultValue) {
            final StringListEntry entry = builder.startStrField(translationKey(id), value)
                    .setDefaultValue(defaultValue).build();
            subCategoryBuilder.add(entry);
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

            return new SimpleSplashScreenConfig(showProgressText.getValue(),
                    (SimpleSplashScreenConfig.ProgressBarType)progressBarType.getValue(),
                    (SimpleSplashScreenConfig.LogoStyle)logoStyle.getValue(),
                    backgroundImage.getValue(),
                    backgroundColor.getValue(),
                    progressBarColor.getValue(),
                    progressFrameColor.getValue(),
                    (SimpleSplashScreenConfig.ProgressBarMode)customProgressBarMode.getValue(),
                    customProgressBarBackground.getValue(),
                    (SimpleSplashScreenConfig.BossBarType)bossBarType.getValue(),
                    textures);
        }
    }
}
