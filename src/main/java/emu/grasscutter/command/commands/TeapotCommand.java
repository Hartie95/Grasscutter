package emu.grasscutter.command.commands;

import emu.grasscutter.command.Command;
import emu.grasscutter.command.CommandHandler;
import emu.grasscutter.data.GameData;
import emu.grasscutter.data.excels.ItemData;
import emu.grasscutter.game.player.Player;
import emu.grasscutter.server.packet.send.PacketHomeBasicInfoNotify;
import emu.grasscutter.server.packet.send.PacketPlayerHomeCompInfoNotify;

import java.util.List;

import static emu.grasscutter.utils.Language.translate;

@Command(label = "teapot",
        usage = {"setLevel <level>", 
            "unlockModule <1 - 4>", 
            "lockModule <1 - 4>",
            "giveAllFurniture <count>",
            "refreshLimitedShop"},
        permission = "player.teapot",
        permissionTargeted = "player.teapot.others")
public final class TeapotCommand implements CommandHandler {

    @Override
    public void execute(Player sender, Player targetPlayer, List<String> args) {
        if (args.size() == 0) {
            sendUsageMessage(sender);
            return;
        }

        String cmd = args.remove(0).toLowerCase();

        int param = 0;
        if (args.size() == 1) {
            try {
                param = Integer.parseInt(args.get(0));
            }
            catch (Exception e) {
                CommandHandler.sendMessage(sender, translate(sender, "commands.teapot.invalid_param"));
                return;
            }
        }

        switch (cmd) {
            case "setlevel" -> {
                if (param < 1 || param > 10) {
                    CommandHandler.sendMessage(sender, translate(sender, "commands.teapot.level_range_error"));
                    return;
                }
                targetPlayer.getHome().setLevel(param);
                targetPlayer.getHome().save();
                targetPlayer.sendPacket(new PacketHomeBasicInfoNotify(targetPlayer, false));
                CommandHandler.sendMessage(sender, translate(sender, "commands.teapot.level_success", param));
            }
            case "unlockmodule" -> {
                if (param > 4) {
                    CommandHandler.sendMessage(sender, translate(sender, "commands.teapot.module_sumeru_error"));
                    return;
                }

                if (param < 1) {
                    CommandHandler.sendMessage(sender, translate(sender, "commands.teapot.module_range_error"));
                    return;
                }

                if (targetPlayer.getRealmList().contains(param)) {
                    CommandHandler.sendMessage(sender, translate(sender, "commands.teapot.unlock_module_contain_error"));
                    return;
                }
                targetPlayer.getRealmList().add(param);
                targetPlayer.save();
                targetPlayer.sendPacket(new PacketPlayerHomeCompInfoNotify(targetPlayer));
                CommandHandler.sendMessage(sender, translate(sender, "commands.teapot.unlock_module_success", param));
            }
            case "lockmodule" -> {
                if (param > 4) {
                    CommandHandler.sendMessage(sender, translate(sender, "commands.teapot.module_sumeru_error"));
                    return;
                }

                if (param < 1) {
                    CommandHandler.sendMessage(sender, translate(sender, "commands.teapot.module_range_error"));
                    return;
                }

                if (param == targetPlayer.getCurrentRealmId()) {
                    CommandHandler.sendMessage(sender, translate(sender, "commands.teapot.lock_module_in_scene_error"));
                    return;
                }

                if (!targetPlayer.getRealmList().contains(param)) {
                    CommandHandler.sendMessage(sender, translate(sender, "commands.teapot.lock_module_contain_error"));
                    return;
                }
                targetPlayer.getHome().getRealmBlockMap().remove(param);
                targetPlayer.getHome().save();
                targetPlayer.getRealmList().remove(param);
                targetPlayer.save();

                targetPlayer.sendPacket(new PacketPlayerHomeCompInfoNotify(targetPlayer));
                CommandHandler.sendMessage(sender, translate(sender, "commands.teapot.lock_module_success", param));
            }
            case "giveallfurniture" -> {
                for (ItemData item : GameData.getItemDataMap().values().stream().toList()) {
                    if (item.getFurnType() == null) continue;

                    targetPlayer.getInventory().addItem(item.getId(), param);
                }
                CommandHandler.sendMessage(sender, translate(sender, "commands.teapot.give_furniture_success"));
            }
            case "refreshlimitedshop" -> {
                targetPlayer.getHome().refreshLimitedShop();
                targetPlayer.getHome().save();
                CommandHandler.sendMessage(sender, translate(sender, "commands.teapot.refresh_limited_shop_success"));
            }
            default -> {
                sendUsageMessage(sender);
            }
        }
    }
}
