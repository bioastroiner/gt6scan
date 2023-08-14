package bioast.mods.gt6m;

import java.io.File;

import com.sun.org.apache.xpath.internal.operations.Bool;
import net.minecraftforge.common.config.Configuration;

public enum Config {

    PENALTY_NO("QOL",
            "removePotionsPenaltyOnBreak",
            true,
            "Removes that Nasty Effects whenever you break a GT tool so no more Slowness and Ftigue. Gt tools are Bad enough!"),
    SCRAP_NO("QOL",
            "removeScrapOnBreak",
            true,
            "Do not return Scrap whenever a tool breaks");
    enum EType{
        Boolean,Integer,Float
    }
    Config.EType type;
    String cg;String key;String comment;

    public boolean getBool() {
        return bool;
    }

    boolean bool;

    public int getInteger() {
        return integer;
    }

    int integer;

    public float getFloat() {
        return flo;
    }

    float flo;
    public static Configuration configuration;


    Config(String cg, String key){
        this.cg = cg;
        this.key = key;
    }

    Config(String cg, String key,Boolean boolIn,String cmt){
        this(cg,key);
        bool=boolIn;
        type = EType.Boolean;
        comment=cmt;
    }

    Config(String cg, String key,int IntIn){
        this(cg,key);
        integer=IntIn;
        type = EType.Integer;
    }

    public static void synchronizeConfiguration(File configFile) {
        configuration = new Configuration(configFile);
        for (Config cfg : Config.values()) {

            switch(cfg.type){
                case Boolean:
                    configuration.get(cfg.cg,cfg.key,cfg.bool);
                    break;
                case Integer:
                    configuration.get(cfg.cg,cfg.key,cfg.integer);
                    break;
                case Float:
                    configuration.get(cfg.cg,cfg.key,cfg.flo);
                    break;
            }
        }
        if (configuration.hasChanged()) {
            configuration.save();
        }
    }
}
