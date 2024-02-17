package emu.grasscutter.server.packet.send;

import emu.grasscutter.game.player.Player;
import emu.grasscutter.net.packet.BasePacket;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.proto.QuestListNotifyOuterClass.QuestListNotify;
import org.anime_game_servers.core.gi.enums.QuestState;

public class PacketQuestListNotify extends BasePacket {

    public PacketQuestListNotify(Player player) {
        super(PacketOpcodes.QuestListNotify, true);

        QuestListNotify.Builder proto = QuestListNotify.newBuilder();

        player.getQuestManager().forEachQuest(quest -> {
            if (quest.getState() != QuestState.QUEST_STATE_UNSTARTED) {
                proto.addQuestList(quest.toProto());
            }
        });

        this.setData(proto);
    }
}
