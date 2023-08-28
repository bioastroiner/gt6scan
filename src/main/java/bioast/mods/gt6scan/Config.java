package bioast.mods.gt6scan;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import java.io.File;

public enum Config {

    ;

    public static Configuration configuration;
    final String CATEGORY;
    final String KEY;
    final String COMMENT;
    Config.EType type;
    boolean defaultBoolean;
    int defaultInteger;
    float defaultFloat;
    private Property property;
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
                    cfg.property = configuration.get(cfg.CATEGORY, cfg.KEY, cfg.defaultBoolean, cfg.COMMENT);
                    break;
                case Integer:
                    cfg.property = configuration.get(cfg.CATEGORY, cfg.KEY, cfg.defaultInteger, cfg.COMMENT);
                    break;
                case Float:
                    cfg.property = configuration.get(cfg.CATEGORY, cfg.KEY, cfg.defaultFloat, cfg.COMMENT);
                    break;
            }
        }
        if (configuration.hasChanged()) {
            configuration.save();
        }
    }

    public boolean getBoolean() {
        return getProperty().getBoolean();
    }

    public Property getProperty() {
        return property;
    }

    enum EType {
        Boolean,
        Integer,
        Float
    }
}
