package bioast.mods.gt6scan.network;

import bioast.mods.gt6scan.network.scanmessage.ScanRequest;
import bioast.mods.gt6scan.proxy.CommonProxy;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.Arrays;
import java.util.List;

public class ScanCommand extends CommandBase {
    @Override
    public String getCommandName() {
        return "scan";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return null;
    }

    @Override
    /*
    /scan {ScanMode} {Size}
    /scan {Size}
     */
    public void processCommand(ICommandSender sender, String[] args) {
        if (sender instanceof AbstractClientPlayer) {
            ScanMode mode = ScanMode.LARGE;
            int x = ((AbstractClientPlayer) sender).serverPosX;
            int z = ((AbstractClientPlayer) sender).serverPosZ;
            int size = 9;
            if (args.length < 1) {
            } else if (args.length > 1) {
                if (NumberUtils.isNumber(args[0])) {
                    size = Integer.parseInt(args[1]);
                } else {
                    var res = Arrays.stream(ScanMode.values())
                        .map(ScanMode::toString)
                        .filter(s -> args[0].equalsIgnoreCase(s))
                        .map(ScanMode::valueOf)
                        .findFirst();
                    if (res.isPresent()) {
                        mode = res.get();
                    }
                    if (args.length > 2) {
                        if (NumberUtils.isNumber(args[1])) {
                            size = Integer.parseInt(args[1]);
                        }
                    }
                }
            }
            if (size > 9) {
                sender.addChatMessage(new ChatComponentText("Size Capped to 9, from %d".formatted(size)));
                size = 9;
            }
            sender.addChatMessage(new ChatComponentText("Beginning Scanning Mode:%s, at X:%d Z:%d with size:{%d}".formatted(
                mode,
                x,
                z,
                size)));
            CommonProxy.simpleNetworkWrapper.sendToServer(new ScanRequest(mode, x, z, size));
        }
    }

    @Override
    public List addTabCompletionOptions(ICommandSender sender, String[] args) {
        if (args.length > 1) {
            return getListOfStringsMatchingLastWord(args, Arrays.toString(ScanMode.values()));
        }
        return super.addTabCompletionOptions(sender, args);
    }
}
