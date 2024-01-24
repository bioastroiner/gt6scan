package bioast.mods.gt6scan.network;

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
	public void processCommand(ICommandSender sender, String[] args) {
		if (sender instanceof AbstractClientPlayer) {
			ScanMode mode = ScanMode.LARGE;
			int x = ((AbstractClientPlayer) sender).serverPosX;
			int z = ((AbstractClientPlayer) sender).serverPosZ;
			if (args.length < 1) {
			} else if (args.length > 1) {
				if (NumberUtils.isNumber(args[0])) {
					mode = ScanMode.values()[Integer.parseInt(args[0]) % ScanMode.values().length];
				}
//                if(args.length > 2){
//                    if(args.length > 3){
//                          TO BE
//                    }
//                }
			}
			CommonProxy.simpleNetworkWrapper.sendToServer(new ScanRequestToServer(mode, x, z));
			sender.addChatMessage(new ChatComponentText("doing stuff bec your a player client"));
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
