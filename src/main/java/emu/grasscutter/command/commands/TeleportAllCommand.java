package emu.grasscutter.command.commands;

import emu.grasscutter.command.Command;
import emu.grasscutter.command.CommandHandler;
import emu.grasscutter.game.player.Player;
import emu.grasscutter.server.event.player.PlayerTeleportEvent.TeleportType;

import java.util.List;

import static emu.grasscutter.utils.Language.translate;

@Command(label = "teleportAll", aliases = {"tpall"}, permission = "player.tpall", permissionTargeted = "player.tpall.others")
public final class TeleportAllCommand implements CommandHandler {

    @Override
    public void execute(Player sender, Player targetPlayer, List<String> args) {
        if (!targetPlayer.getWorld().isMultiplayer()) {
            CommandHandler.sendMessage(sender, translate(sender, "commands.teleportAll.error"));
            return;
        }

        targetPlayer.getWorld().getPlayers().stream().filter(player -> !player.equals(targetPlayer)).forEach(player ->
            player.getWorld().transferPlayerToScene(
                player, targetPlayer.getSceneId(), TeleportType.COMMAND, targetPlayer.getPosition(), targetPlayer.getRotation()));

        CommandHandler.sendMessage(sender, translate(sender, "commands.teleportAll.success"));
    }
}
