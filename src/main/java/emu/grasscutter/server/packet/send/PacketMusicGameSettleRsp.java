package emu.grasscutter.server.packet.send;

import emu.grasscutter.net.packet.BasePacket;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.proto.MusicGameSettleReqOuterClass.MusicGameSettleReq;
import emu.grasscutter.net.proto.MusicGameSettleRspOuterClass.MusicGameSettleRsp;
import emu.grasscutter.net.proto.RetcodeOuterClass.Retcode;

public class PacketMusicGameSettleRsp extends BasePacket {

    public PacketMusicGameSettleRsp(int musicBasicId, long musicShareId, boolean isNewRecord) {
        super(PacketOpcodes.MusicGameSettleRsp);

        var proto = MusicGameSettleRsp.newBuilder();

        proto.setMusicBasicId(musicBasicId)
            .setUgcGuid(musicShareId)
            .setIsNewRecord(isNewRecord);

        this.setData(proto);
    }

    public PacketMusicGameSettleRsp(Retcode errorCode, MusicGameSettleReq req) {
        super(PacketOpcodes.MusicGameSettleRsp);

        var proto = MusicGameSettleRsp.newBuilder()
            .setRetcode(errorCode.getNumber())
            .setMusicBasicId(req.getMusicBasicId())
            .setUgcGuid(req.getUgcGuid());


        this.setData(proto);
    }
}
