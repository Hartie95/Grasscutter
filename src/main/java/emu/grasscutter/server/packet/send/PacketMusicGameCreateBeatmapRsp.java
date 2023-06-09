package emu.grasscutter.server.packet.send;

import emu.grasscutter.net.packet.BasePacket;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.proto.RetcodeOuterClass;
import emu.grasscutter.net.proto.SaveUgcRspOuterClass.SaveUgcRsp;
import emu.grasscutter.net.proto.UgcTypeOuterClass.UgcType;
import lombok.val;

public class PacketMusicGameCreateBeatmapRsp extends BasePacket {

	public PacketMusicGameCreateBeatmapRsp(long musicShareId, UgcType ugcType) {
		super(PacketOpcodes.SaveUgcRsp);

        val proto = SaveUgcRsp.newBuilder()
            .setUgcGuid(musicShareId)
            .setUgcType(ugcType);;

        this.setData(proto);
	}

	public PacketMusicGameCreateBeatmapRsp(RetcodeOuterClass.Retcode retCode, UgcType ugcType) {
		super(PacketOpcodes.SaveUgcRsp);

        val proto = SaveUgcRsp.newBuilder()
            .setRetcode(retCode.getNumber())
            .setUgcType(ugcType);

        this.setData(proto);
	}
}
