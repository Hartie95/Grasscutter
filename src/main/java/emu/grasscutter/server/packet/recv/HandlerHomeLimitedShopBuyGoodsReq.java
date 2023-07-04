package emu.grasscutter.server.packet.recv;

import emu.grasscutter.net.packet.Opcodes;
import emu.grasscutter.net.packet.PacketHandler;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.proto.HomeLimitedShopBuyGoodsReqOuterClass.HomeLimitedShopBuyGoodsReq;
import emu.grasscutter.server.game.GameSession;
import emu.grasscutter.server.packet.send.PacketHomeLimitedShopBuyGoodsRsp;

@Opcodes(PacketOpcodes.HomeLimitedShopBuyGoodsReq)
public class HandlerHomeLimitedShopBuyGoodsReq extends PacketHandler {
	
	@Override
	public void handle(GameSession session, byte[] header, byte[] payload) throws Exception {
		HomeLimitedShopBuyGoodsReq req = HomeLimitedShopBuyGoodsReq.parseFrom(payload);
		session.send(new PacketHomeLimitedShopBuyGoodsRsp(session.getPlayer(), req.getGoods(), req.getBuyCount()));
	}

}
