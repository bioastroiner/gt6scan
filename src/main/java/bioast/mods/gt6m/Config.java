package bioast.mods.gt6m;

import java.io.File;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

public enum Config {

    PENALTY_NO("QOL", "removePotionsPenaltyOnBreak", true,
        "Removes that Nasty Effects whenever you break a GT tool so no more Slowness and Ftigue. Gt tools are Bad enough!"),
    SCRAP_NO("QOL", "removeScrapOnBreak", true, "Do not return Scrap whenever a tool breaks"),
    ELECTRIC_DURIBILITY_NO("QOL", "removeElectricToolsDuribility", true,
        "Electric tools no longer use any duribility just Electricity."),
    ENABLE_ANIME_GIRLS("???", "Enable Anime Girls", false, "They Might become real... take your meds");

    enum EType {
        Boolean,
        Integer,
        Float
    }

    Config.EType type;
    final String CATEGORY;
    final String KEY;
    final String COMMENT;

    public boolean getBoolean() {
        return property.getBoolean();
    }

    boolean defaultBoolean;
    int defaultInteger;
    float defaultFloat;
    public static Configuration configuration;
    public static Property property;

    Config(String CATEGORY, String KEY) {
        this.CATEGORY = CATEGORY;
        this.KEY = KEY;
        this.COMMENT = "";
    }

    Config(String CATEGORY, String KEY, String COMMENT) {
        this.CATEGORY = CATEGORY;
        this.KEY = KEY;
        this.COMMENT = COMMENT;
    }

    Config(String CATEGORY, String KEY, Boolean boolIn, String cmt) {
        this(CATEGORY, KEY, cmt);
        defaultBoolean = boolIn;
        type = EType.Boolean;
    }

    Config(String CATEGORY, String KEY, int IntIn) {
        this(CATEGORY, KEY);
        defaultInteger = IntIn;
        type = EType.Integer;
    }

    public static void synchronizeConfiguration(File configFile) {
        configuration = new Configuration(configFile);
        for (Config cfg : Config.values()) {

            switch (cfg.type) {
                case Boolean:
                    property = configuration.get(cfg.CATEGORY, cfg.KEY, cfg.defaultBoolean, cfg.COMMENT);
                    break;
                case Integer:
                    property = configuration.get(cfg.CATEGORY, cfg.KEY, cfg.defaultInteger, cfg.COMMENT);
                    break;
                case Float:
                    property = configuration.get(cfg.CATEGORY, cfg.KEY, cfg.defaultFloat, cfg.COMMENT);
                    break;
            }
        }
        if (configuration.hasChanged()) {
            configuration.save();
        }
    }
}
