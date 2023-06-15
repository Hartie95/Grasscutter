package emu.grasscutter.server.packet.recv;

import emu.grasscutter.net.packet.Opcodes;
import emu.grasscutter.net.packet.PacketHandler;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.proto.SceneAudioNotifyOuterClass.SceneAudioNotify;
import emu.grasscutter.server.game.GameSession;
import emu.grasscutter.server.packet.send.PacketSceneAudioNotify;
import lombok.val;

@Opcodes(PacketOpcodes.SceneAudioNotify)
public class HandlerSceneAudioNotify extends PacketHandler {

	@Override
	public void handle(GameSession session, byte[] header, byte[] payload) throws Exception {
        val notify = SceneAudioNotify.parseFrom(payload);

        session.getPlayer().getScene().broadcastPacket(new PacketSceneAudioNotify(notify));
	}

}
