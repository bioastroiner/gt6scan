package bioast.mods.gt6scan.network.scanmessage;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

/*
 * This MessageHandler does nothing; it is only used because the dedicated server must register at least one message
 *   handler in order for Forge to know what ID to use for this message.  See more explanation in StartupCommon.
 */
public class HandlerDummy implements IMessageHandler<ScanResponse, IMessage> {
    @Override
    public IMessage onMessage(ScanResponse message, MessageContext ctx) {
        System.err.println("ScanResponceToClient received on wrong side:" + ctx.side);
        return null;
    }
}
