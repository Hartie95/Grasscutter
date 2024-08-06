package emu.grasscutter.server.packet.send;

import emu.grasscutter.net.packet.BaseTypedPacket;
import org.anime_game_servers.multi_proto.gi.messages.battle_pass.BuyBattlePassLevelRsp;

public class PacketBuyBattlePassLevelRsp extends BaseTypedPacket<BuyBattlePassLevelRsp> {

	public PacketBuyBattlePassLevelRsp(int buyLevel) {
        super(new BuyBattlePassLevelRsp());
        proto.setBuyLevel(buyLevel);
	}
}
