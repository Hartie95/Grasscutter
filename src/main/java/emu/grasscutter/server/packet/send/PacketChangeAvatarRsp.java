package emu.grasscutter.server.packet.send;

import emu.grasscutter.net.packet.BasePacket;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.proto.ChangeAvatarRspOuterClass.ChangeAvatarRsp;

public class PacketChangeAvatarRsp extends BasePacket {

	public PacketChangeAvatarRsp(int ret, long guid) {
		super(PacketOpcodes.ChangeAvatarRsp);

		ChangeAvatarRsp p = ChangeAvatarRsp.newBuilder()
				.setRetcode(ret)
				.setCurGuid(guid)
				.build();

		this.setData(p);
	}
}
