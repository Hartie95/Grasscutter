package emu.grasscutter.server.packet.send;

import emu.grasscutter.net.packet.BasePacket;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.proto.DungeonInterruptChallengeReqOuterClass.DungeonInterruptChallengeReq;
import emu.grasscutter.net.proto.DungeonInterruptChallengeRspOuterClass.DungeonInterruptChallengeRsp;

public class PacketDungeonInterruptChallengeRsp extends BasePacket {
    public PacketDungeonInterruptChallengeRsp(boolean result, DungeonInterruptChallengeReq req) {
        super(PacketOpcodes.DungeonInterruptChallengeRsp);
        this.setData(DungeonInterruptChallengeRsp.newBuilder()
            .setChallengeId(req.getChallengeId())
            .setChallengeIndex(req.getChallengeIndex())
            .setGroupId(req.getGroupId())
            .setRetcode(result ? 0 : 1));
    }
}
