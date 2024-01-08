package bioast.mods.gt6mapper.network;

import bioast.mods.gt6mapper.MapperMod;
import bioast.mods.gt6mapper.item.ItemProspectMap;
import bioast.mods.gt6mapper.world.ProspectMapData;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent;
import cpw.mods.fml.common.network.internal.FMLProxyPacket;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.S34PacketMaps;

import java.io.IOException;

@ChannelHandler.Sharable
public class MapPacketHandler {

	/**
	 * Make a Packet250CustomPayload that wraps around a MapData packet, and sends on a specific channel
	 */
	public static Packet makeProspectingMapHandler(String mapChannel, short mapID, byte[] datas) {

		S34PacketMaps mapPacket = new S34PacketMaps(mapID, datas);
		PacketBuffer payload = new PacketBuffer(Unpooled.buffer());

		try {
			mapPacket.writePacketData(payload);
		} catch (IOException e) {
			e.printStackTrace();
		}

		FMLProxyPacket pkt = new FMLProxyPacket(payload, mapChannel);

		return pkt;
	}

	/**
	 * Extract a Packet131MapData packet from a Packet250CustomPayload.  This is a little silly, huh?
	 */
	public S34PacketMaps readMapPacket(ByteBuf byteBuf) {

		S34PacketMaps mapPacket = new S34PacketMaps();
		try {
			mapPacket.readPacketData(new PacketBuffer(byteBuf));
		} catch (IOException e) {
			e.printStackTrace();
		}

		return mapPacket;
	}

	/**
	 * Packet!
	 */
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void incomingPacket(FMLNetworkEvent.ClientCustomPacketEvent event) {
//		System.out.println("Incoming packet detected!");

		if (event.packet.channel().equals(ItemProspectMap.STR_ID)) {
			//System.out.println("Incoming maze map packet detected!");

			S34PacketMaps mapPacket = readMapPacket(event.packet.payload());
			ProspectMapData data = ItemProspectMap.getMPMapData(mapPacket.func_149188_c(), MapperMod.proxy.getClientWorld());
			data.updateMPMapData(mapPacket.func_149187_d());
			// FIXME: NullPointerException
			Minecraft.getMinecraft().entityRenderer.getMapItemRenderer().func_148246_a(data);
		}
	}
}
