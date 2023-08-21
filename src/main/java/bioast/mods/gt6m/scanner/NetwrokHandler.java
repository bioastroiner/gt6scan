package bioast.mods.gt6m.scanner;

import bioast.mods.gt6m.scanner.utils.VALs;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.FMLEmbeddedChannel;
import cpw.mods.fml.common.network.FMLOutboundHandler;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.internal.FMLProxyPacket;
import cpw.mods.fml.relauncher.Side;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.MessageToMessageCodec;
import net.minecraft.entity.player.EntityPlayerMP;

import java.util.EnumMap;
import java.util.List;

@ChannelHandler.Sharable
public class NetwrokHandler extends MessageToMessageCodec<FMLProxyPacket, PacketScanner> {
    static public NetwrokHandler INSTANCE;
    public final EnumMap<Side, FMLEmbeddedChannel> mChannel;

    public NetwrokHandler(){
        INSTANCE=this;
        this.mChannel = NetworkRegistry.INSTANCE.newChannel(VALs.SCANNER_NET_ID, this, new HandlerShared());
    }
    @Override
    protected void encode(ChannelHandlerContext ctx, PacketScanner msg, List<Object> out) throws Exception {
        ByteBuf buf = Unpooled.buffer();
        buf.writeByte(msg.getPacketID());
        msg.encode(new ByteBufOutputStream(buf));
        out.add(new FMLProxyPacket(buf, ctx.channel().attr(NetworkRegistry.FML_CHANNEL).get()));
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, FMLProxyPacket msg, List<Object> out) throws Exception {
        ByteBuf payload = msg.payload();
        payload.readByte(); // Sub Channel - Ignore
        out.add(PacketScanner.decode(new ByteBufInputStream(payload)));
    }

    public void sendToClient(PacketScanner aPacket, EntityPlayerMP aPlayer) {
        this.mChannel.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGET)
                .set(FMLOutboundHandler.OutboundTarget.PLAYER);
        this.mChannel.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(aPlayer);
        this.mChannel.get(Side.SERVER).writeAndFlush(aPacket);
        sendToServer(aPacket);
    }

    public void sendToServer(PacketScanner aPacket) {
        this.mChannel.get(Side.CLIENT).attr(FMLOutboundHandler.FML_MESSAGETARGET)
                .set(FMLOutboundHandler.OutboundTarget.TOSERVER);
        this.mChannel.get(Side.CLIENT).writeAndFlush(aPacket);
    }

    @ChannelHandler.Sharable
    static final class HandlerShared extends SimpleChannelInboundHandler<PacketScanner> {

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, PacketScanner aPacket) throws Exception {
            aPacket.process();
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            super.channelInactive(ctx);
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            ((PacketScanner)msg).process();
            super.channelRead(ctx, msg);
        }
    }
}
