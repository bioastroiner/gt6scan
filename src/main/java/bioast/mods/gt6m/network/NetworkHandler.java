//package bioast.mods.gt6m.network;
//
//import com.google.common.io.ByteArrayDataInput;
//import com.google.common.io.ByteStreams;
//import cpw.mods.fml.common.FMLCommonHandler;
//import cpw.mods.fml.common.network.FMLEmbeddedChannel;
//import cpw.mods.fml.common.network.FMLOutboundHandler;
//import cpw.mods.fml.common.network.NetworkRegistry;
//import cpw.mods.fml.common.network.internal.FMLProxyPacket;
//import cpw.mods.fml.relauncher.Side;
//import gregapi.network.INetworkHandler;
//import gregapi.network.IPacket;
//import io.netty.buffer.ByteBuf;
//import io.netty.buffer.ByteBufInputStream;
//import io.netty.buffer.Unpooled;
//import io.netty.channel.ChannelHandler;
//import io.netty.channel.ChannelHandlerContext;
//import io.netty.channel.SimpleChannelInboundHandler;
//import io.netty.handler.codec.MessageToMessageCodec;
//import net.minecraft.client.Minecraft;
//import net.minecraft.entity.player.EntityPlayerMP;
//import net.minecraft.util.ChunkCoordinates;
//import net.minecraft.world.World;
//
//import java.util.ArrayList;
//import java.util.EnumMap;
//import java.util.List;
//import java.util.UUID;
//
//@ChannelHandler.Sharable
//public class NetworkHandler extends MessageToMessageCodec<FMLProxyPacket, IPacket> implements INetworkHandler {
//    private final EnumMap<Side, FMLEmbeddedChannel> mChannel;
//    private final List<IPacket> packets;
//    public NetworkHandler(){
//        mChannel = NetworkRegistry.INSTANCE.newChannel(aChannelName, this, FMLCommonHandler.instance().getSide()==Side.CLIENT?new gregapi.network.NetworkHandler.HandlerClient(this):new gregapi.network.NetworkHandler.HandlerServer(this));
//        packets = new ArrayList<>();
//    }
//
//    @Override
//    protected void encode(ChannelHandlerContext ctx, IPacket msg, List<Object> out) throws Exception {
//        out.add(new FMLProxyPacket(Unpooled.buffer().writeByte(msg.getPacketID()).writeBytes(msg.encode().toByteArray()), ctx.channel().attr(NetworkRegistry.FML_CHANNEL).get()));
//        out.add(new FMLProxyPacket(msg.payload(), ctx.channel().attr(NetworkRegistry.FML_CHANNEL).get()));
//
//        packets.add(msg);
//    }
//
//    @Override
//    protected void decode(ChannelHandlerContext ctx, FMLProxyPacket msg, List<Object> out) throws Exception {
////        ByteArrayDataInput aData = ByteStreams.newDataInput(msg.payload().array());
////        out.add(packets.get(aData))
//        ByteBuf payload = msg.payload();
//        payload.readByte(); // Sub Channel - Ignore
//        out.add(PacketScanner.decode(new ByteBufInputStream(payload)));
//    }
//
//    @Override
//    public void sendToServer(IPacket aPacket) {
//        FMLEmbeddedChannel tChannel = getChannel(Side.SERVER);
//        tChannel.attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.PLAYER);
//        tChannel.attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(aPlayer);
//        tChannel.writeAndFlush(aPacket);
//    }
//
//    @Override
//    public void sendToPlayer(IPacket aPacket, EntityPlayerMP aPlayer) {
//
//    }
//
//    @Override
//    public void sendToAllAround(IPacket aPacket, NetworkRegistry.TargetPoint aPosition) {
//
//    }
//
//    @Override
//    public void sendToAllPlayersInRange(IPacket aPacket, World aWorld, int aX, int aZ) {
//
//    }
//
//    @Override
//    public void sendToAllPlayersInRange(IPacket aPacket, World aWorld, ChunkCoordinates aCoords) {
//
//    }
//
//    @Override
//    public void sendToPlayerIfInRange(IPacket aPacket, UUID aPlayer, World aWorld, int aX, int aZ) {
//
//    }
//
//    @Override
//    public void sendToPlayerIfInRange(IPacket aPacket, UUID aPlayer, World aWorld, ChunkCoordinates aCoords) {
//
//    }
//
//    @Override
//    public void sendToAllPlayersInRangeExcept(IPacket aPacket, UUID aPlayer, World aWorld, int aX, int aZ) {
//
//    }
//
//    @Override
//    public void sendToAllPlayersInRangeExcept(IPacket aPacket, UUID aPlayer, World aWorld, ChunkCoordinates aCoords) {
//
//    }
//
//    @Override
//    public FMLEmbeddedChannel getChannel(Side aSide) {
//        return mChannel.get(aSide);
//    }
//
//    @Sharable
//    static final class HandlerClient extends SimpleChannelInboundHandler<IPacket> {
//        public final INetworkHandler mNetworkHandler;
//
//        public HandlerClient(INetworkHandler aNetworkHandler) {
//            mNetworkHandler = aNetworkHandler;
//        }
//
//        @Override
//        protected void channelRead0(ChannelHandlerContext ctx, IPacket aPacket) throws Exception {
//            aPacket.process(Minecraft.getMinecraft().thePlayer == null ? null : Minecraft.getMinecraft().thePlayer.worldObj, mNetworkHandler);
////          DEB.println(aPacket.getClass().getName());
////          if (aPacket instanceof PacketCoordinates) DEB.println(" X: " + ((PacketCoordinates)aPacket).mX + " - Y: " + ((PacketCoordinates)aPacket).mY + " - Z: " + ((PacketCoordinates)aPacket).mZ);
//        }
//    }
//
//    @Sharable
//    static final class HandlerServer extends SimpleChannelInboundHandler<IPacket> {
//        public final INetworkHandler mNetworkHandler;
//
//        public HandlerServer(INetworkHandler aNetworkHandler) {
//            mNetworkHandler = aNetworkHandler;
//        }
//
//        @Override
//        protected void channelRead0(ChannelHandlerContext ctx, IPacket aPacket) throws Exception {
//            aPacket.process(null, mNetworkHandler);
//        }
//    }
//}
