package emu.grasscutter.server.packet.recv;

import emu.grasscutter.game.quest.enums.QuestContent;
import emu.grasscutter.net.packet.Opcodes;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.packet.PacketHandler;
import emu.grasscutter.server.game.GameSession;
import emu.grasscutter.server.packet.send.PacketPostEnterSceneRsp;

import lombok.val;

@Opcodes(PacketOpcodes.PostEnterSceneReq)
public class HandlerPostEnterSceneReq extends PacketHandler {

    @Override
    public void handle(GameSession session, byte[] header, byte[] payload) throws Exception {
        val sceneId = session.getPlayer().getSceneId();
        val questManager = session.getPlayer().getQuestManager();
        switch (session.getPlayer().getScene().getSceneType()){
            case SCENE_ROOM -> questManager.queueEvent(QuestContent.QUEST_CONTENT_ENTER_ROOM, sceneId,0);
            case SCENE_WORLD -> {
                questManager.queueEvent(QuestContent.QUEST_CONTENT_ENTER_MY_WORLD, sceneId);
                questManager.queueEvent(QuestContent.QUEST_CONTENT_ENTER_MY_WORLD_SCENE, sceneId);
            }
            case SCENE_DUNGEON -> {
                val dungeonManager = session.getPlayer().getScene().getDungeonManager();
                if (dungeonManager != null) dungeonManager.startDungeon();
            }
        }


        session.send(new PacketPostEnterSceneRsp(session.getPlayer()));
    }

}
