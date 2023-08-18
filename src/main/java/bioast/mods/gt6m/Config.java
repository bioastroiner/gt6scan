package bioast.mods.gt6m;

import java.io.File;

import net.minecraftforge.common.config.Configuration;

public enum Config {

    PENALTY_NO("QOL", "removePotionsPenaltyOnBreak", true,
        "Removes that Nasty Effects whenever you break a GT tool so no more Slowness and Ftigue. Gt tools are Bad enough!"),
    SCRAP_NO("QOL", "removeScrapOnBreak", true, "Do not return Scrap whenever a tool breaks"),
    ELECTRIC_DURIBILITY_NO("QOL", "removeElectricToolsDuribility", true,
        "Electric tools no longer use any duribility just Electricity.");

    enum EType {
        Boolean,
        Integer,
        Float
    }

    Config.EType type;
    final String CATEGORY;
    final String KEY;
    final String COMMENT;

    public boolean getBool() {
        return bool;
    }

    boolean bool;

    public int getInteger() {
        return integer;
    }

    int integer;

    public float getFloat() {
        return floati;
    }

    float floati;
    public static Configuration configuration;

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
        bool = boolIn;
        type = EType.Boolean;
    }

    Config(String CATEGORY, String KEY, int IntIn) {
        this(CATEGORY, KEY);
        integer = IntIn;
        type = EType.Integer;
    }

    public static void synchronizeConfiguration(File configFile) {
        configuration = new Configuration(configFile);
        for (Config cfg : Config.values()) {

            switch (cfg.type) {
                case Boolean:
                    configuration.get(cfg.CATEGORY, cfg.KEY, cfg.bool, cfg.COMMENT);
                    break;
                case Integer:
                    configuration.get(cfg.CATEGORY, cfg.KEY, cfg.integer, cfg.COMMENT);
                    break;
                case Float:
                    configuration.get(cfg.CATEGORY, cfg.KEY, cfg.floati, cfg.COMMENT);
                    break;
            }
        }
        if (configuration.hasChanged()) {
            configuration.save();
        }
    }
}
