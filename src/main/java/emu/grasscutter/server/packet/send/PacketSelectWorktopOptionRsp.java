package emu.grasscutter.server.packet.send;

import emu.grasscutter.net.packet.BaseTypedPacket;
import org.anime_game_servers.multi_proto.gi.messages.gadget.SelectWorktopOptionRsp;

public class PacketSelectWorktopOptionRsp extends BaseTypedPacket<SelectWorktopOptionRsp> {

	public PacketSelectWorktopOptionRsp(int entityId, int optionId) {
        super(new SelectWorktopOptionRsp());
        proto.setGadgetEntityId(entityId);
        proto.setOptionId(optionId);
	}
}
