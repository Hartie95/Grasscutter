package emu.grasscutter.server.packet.send;

import emu.grasscutter.game.inventory.GameItem;
import emu.grasscutter.net.packet.BaseTypedPacket;
import emu.grasscutter.net.proto.RetcodeOuterClass;
import org.anime_game_servers.multi_proto.gi.messages.item.UseItemRsp;

public class PacketUseItemRsp extends BaseTypedPacket<UseItemRsp> {
	public PacketUseItemRsp(long targetGuid, GameItem useItem) {
        super(new UseItemRsp());
        proto.setTargetGuid(targetGuid);
        proto.setItemId(useItem.getItemId());
        proto.setGuid(useItem.getGuid());
	}

	public PacketUseItemRsp() {
        super(new UseItemRsp());
        proto.setRetCode(RetcodeOuterClass.Retcode.RET_SVR_ERROR_VALUE);
	}
}
