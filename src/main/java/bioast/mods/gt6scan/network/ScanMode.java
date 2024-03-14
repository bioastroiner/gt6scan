package bioast.mods.gt6scan.network;

import gregapi.oredict.OreDictPrefix;

public enum ScanMode {
    NONE(null),
    //CUSTOM(null), /*TODO add custom mode*/
    LARGE(gregapi.data.OP.ore), SMALL(gregapi.data.OP.oreSmall), DENSE_AND_NORMAL(gregapi.data.OP.oreDense), BEDROCK(
        gregapi.data.OP.oreBedrock), FLUID_BEDROCK(gregapi.data.OP.bucket), ROCK(gregapi.data.OP.rockGt), FLUID(gregapi.data.OP.bucket);
    public final OreDictPrefix PREFIX;

    ScanMode(OreDictPrefix op) {
        PREFIX = op;
    }

    public String local() {
        return PREFIX.mNameLocal;
    }

    public boolean isTE() {
        switch (this) {
            case LARGE, ROCK, BEDROCK, SMALL, FLUID_BEDROCK -> {
                return true;
            }
            default -> {
                return false;
            }
        }
    }
}
