package emu.grasscutter.server.packet.recv;

import emu.grasscutter.net.packet.Opcodes;
import emu.grasscutter.net.packet.PacketHandler;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.proto.GetDailyDungeonEntryInfoReqOuterClass.GetDailyDungeonEntryInfoReq;
import emu.grasscutter.server.game.GameSession;
import emu.grasscutter.server.packet.send.PacketGetDailyDungeonEntryInfoRsp;

@Opcodes(PacketOpcodes.GetDailyDungeonEntryInfoReq)
public class HandlerGetDailyDungeonEntryInfoReq extends PacketHandler {

    @Override
    public void handle(GameSession session, byte[] header, byte[] payload) throws Exception {
        GetDailyDungeonEntryInfoReq req = GetDailyDungeonEntryInfoReq.parseFrom(payload);

        session.send(new PacketGetDailyDungeonEntryInfoRsp(session.getPlayer(), req.getSceneId()));
    }
}
