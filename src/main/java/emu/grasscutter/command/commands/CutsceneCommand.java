package emu.grasscutter.command.commands;

import emu.grasscutter.command.Command;
import emu.grasscutter.command.CommandHandler;
import emu.grasscutter.game.entity.EntityAvatar;
import emu.grasscutter.game.player.Player;
import emu.grasscutter.game.props.EntityType;
import emu.grasscutter.game.world.Scene;
import emu.grasscutter.game.world.SceneGroupInstance;
import emu.grasscutter.scripts.data.SceneGroup;
import emu.grasscutter.server.packet.send.PacketCutsceneBeginNotify;
import emu.grasscutter.server.packet.send.PacketNpcTalkRsp;
import emu.grasscutter.utils.Position;
import lombok.Setter;
import lombok.val;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;

import static emu.grasscutter.command.CommandHelpers.*;
import static emu.grasscutter.utils.Language.translate;

@Command(label = "cutscene",
         aliases = {"c"},
         usage = {
            "[<cutsceneId>]"
         },
         permission = "player.group",
         permissionTargeted = "player.group.others")
public final class CutsceneCommand implements CommandHandler {

    @Override
    public void execute(Player sender, Player targetPlayer, List<String> args) {
        if (args.isEmpty()) {
            sendUsageMessage(sender);
            return;
        }
        val cutSceneId = Integer.parseInt(args.get(0));
        targetPlayer.sendPacket(new PacketCutsceneBeginNotify(cutSceneId));
    }
}
