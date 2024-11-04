package emu.grasscutter.command.commands;

import emu.grasscutter.command.Command;
import emu.grasscutter.command.CommandHandler;
import emu.grasscutter.game.avatar.Avatar;
import emu.grasscutter.game.player.Player;
import emu.grasscutter.server.packet.send.*;
import lombok.val;

import java.util.List;

@Command(
    label = "setConst",
    aliases = {"setconstellation"},
    usage = {"<constellation level> [all]"},
    permission = "player.setconstellation",
    permissionTargeted = "player.setconstellation.others")
public final class SetConstCommand implements CommandHandler {
    @Override
    public void execute(Player sender, Player targetPlayer, List<String> args) {
        if (args.size() < 1) {
            sendUsageMessage(sender);
            return;
        }
        try {
            int constLevel = Integer.parseInt(args.get(0));
            // Check if level is out of range
            if (constLevel < -1 || constLevel > 6) {
                CommandHandler.sendTranslatedMessage(sender, "commands.setConst.range_error");
                return;
            }
            // If it's either empty or anything else other than "all" just do normal setConstellation
            if (args.size() == 1) {
                val entity = targetPlayer.getTeamManager().getCurrentAvatarEntity();
                if (entity == null) return;
                val avatar = entity.getAvatar();
                this.setConstellation(targetPlayer, avatar, constLevel);
                CommandHandler.sendTranslatedMessage(sender, "commands.setConst.success", avatar.getAvatarData().getBaseName(), constLevel);
                return;
            }
            // Check if there's an additional argument which is "all", if it does then go setAllConstellation
            if (args.size() > 1 && args.get(1).equalsIgnoreCase("all")) {
                this.setAllConstellation(targetPlayer, constLevel);
                CommandHandler.sendTranslatedMessage(sender, "commands.setConst.successall", constLevel);
            }
            else sendUsageMessage(sender);
        } catch (NumberFormatException ignored) {
            CommandHandler.sendTranslatedMessage(sender, "commands.setConst.level_error");
        }
    }

    private void setConstellation(Player player, Avatar avatar, int constLevel) {
        val currentConstLevel = avatar.getCoreProudSkillLevel();
        avatar.forceConstellationLevel(constLevel);

        // force player to reload scene when necessary
        if (constLevel < currentConstLevel) {
            this.reloadScene(player);
        }

        // ensure that all changes are visible to the player
        avatar.recalcConstellations();
        avatar.recalcStats(true);
        avatar.save();
    }

    private void setAllConstellation(Player player, int constLevel) {
        player.getAvatars().forEach(avatar -> {
                avatar.forceConstellationLevel(constLevel);
                avatar.recalcConstellations();
                avatar.recalcStats(true);
                avatar.save();
        });
        // Just reload scene once, shorter than having to check for each constLevel < currentConstLevel
        this.reloadScene(player);
    }

    private void reloadScene(Player player) {
        val world = player.getWorld();
        val scene = player.getScene();
        val pos = player.getPosition();
        world.transferPlayerToScene(player, 1, pos, null);
        world.transferPlayerToScene(player, scene.getId(), pos, null);
        scene.broadcastPacket(new PacketSceneEntityAppearNotify(player));
    }
}
