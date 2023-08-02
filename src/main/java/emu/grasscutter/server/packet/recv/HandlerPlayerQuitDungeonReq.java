package emu.grasscutter.server.packet.recv;

import emu.grasscutter.net.packet.BasePacket;
import emu.grasscutter.net.packet.Opcodes;
import emu.grasscutter.net.packet.PacketHandler;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.proto.PlayerQuitDungeonReqOuterClass.PlayerQuitDungeonReq;
import emu.grasscutter.server.game.GameSession;

@Opcodes(PacketOpcodes.PlayerQuitDungeonReq)
public class HandlerPlayerQuitDungeonReq extends PacketHandler {

    @Override
    public void handle(GameSession session, byte[] header, byte[] payload) throws Exception {
        PlayerQuitDungeonReq req = PlayerQuitDungeonReq.parseFrom(payload);
        session.getPlayer().getServer().getDungeonSystem().exitDungeon(session.getPlayer(), req.getIsQuitImmediately());
        session.getPlayer().sendPacket(new BasePacket(PacketOpcodes.PlayerQuitDungeonRsp));
    }

}
