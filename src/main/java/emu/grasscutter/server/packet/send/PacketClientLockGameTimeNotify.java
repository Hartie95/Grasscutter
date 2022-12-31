package emu.grasscutter.server.packet.send;

import emu.grasscutter.net.packet.BasePacket;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.proto.ClientLockGameTimeNotifyOuterClass.ClientLockGameTimeNotify;

public class PacketClientLockGameTimeNotify extends BasePacket {
	
	public PacketClientLockGameTimeNotify(int isLockInt) {
		super(PacketOpcodes.ClientLockGameTimeNotify);
		this.setData(ClientLockGameTimeNotify.newBuilder()
			.setIsLock(isLockInt == 1)
			.build());
	}
}
