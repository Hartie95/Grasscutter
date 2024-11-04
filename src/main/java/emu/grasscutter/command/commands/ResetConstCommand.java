package emu.grasscutter.command.commands;

import emu.grasscutter.command.Command;
import emu.grasscutter.command.CommandHandler;
import emu.grasscutter.game.avatar.Avatar;
import emu.grasscutter.game.player.Player;
import lombok.val;

import java.util.List;

import static emu.grasscutter.utils.Language.translate;

@Command(
    label = "resetConst",
    aliases = {"resetconstellation"},
    usage = "[all]",
    permission = "player.resetconstellation",
    permissionTargeted = "player.resetconstellation.others")
public final class ResetConstCommand implements CommandHandler {

    @Override
    public void execute(Player sender, Player targetPlayer, List<String> args) {
        if (args.size() > 0 && args.get(0).equalsIgnoreCase("all")) {
            targetPlayer.getAvatars().forEach(this::resetConstellation);
            CommandHandler.sendMessage(sender, translate(sender, "commands.resetConst.reset_all"));
        } else {
            val entity = targetPlayer.getTeamManager().getCurrentAvatarEntity();
            if (entity == null) {
                return;
            }

            val avatar = entity.getAvatar();
            this.resetConstellation(avatar);

            CommandHandler.sendMessage(sender, translate(sender, "commands.resetConst.success", avatar.getAvatarData().getBaseName()));
        }
    }

    private void resetConstellation(Avatar avatar) {
        avatar.forceConstellationLevel(-1);
    }
}
