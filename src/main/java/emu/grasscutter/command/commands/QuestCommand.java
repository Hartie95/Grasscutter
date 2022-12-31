package emu.grasscutter.command.commands;

import emu.grasscutter.command.Command;
import emu.grasscutter.command.CommandHandler;
import emu.grasscutter.game.player.Player;
import emu.grasscutter.game.quest.GameQuest;

import java.util.List;

import static emu.grasscutter.utils.Language.translate;

@Command(label = "quest",
         aliases = {"q"},
         usage = {"(add|finish) [<questId>]", "enable"},
         permission = "player.quest",
         permissionTargeted = "player.quest.others")
public final class QuestCommand implements CommandHandler {

    @Override
    public void execute(Player sender, Player targetPlayer, List<String> args) {
        if (args.size() == 0) {
            sendUsageMessage(sender);
            return;
        }
        String cmd = args.remove(0).toLowerCase();

        int questId = 0;
        if (args.size() == 1) {
            try {
                questId = Integer.parseInt(args.get(0));
            }
            catch (Exception e) {
                CommandHandler.sendMessage(sender, translate(sender, "commands.quest.invalid_id"));
                return;
            }
        }

        switch (cmd) {
            case "enable" -> {
                targetPlayer.getQuestManager().enableQuests(true);
                CommandHandler.sendMessage(sender, translate(sender, "commands.quest.enabled"));
            }
            case "add" -> {
                GameQuest quest = targetPlayer.getQuestManager().addQuest(questId, true);

                if (quest != null) {
                    CommandHandler.sendMessage(sender, translate(sender, "commands.quest.added", questId));
                    return;
                }
                CommandHandler.sendMessage(sender, translate(sender, "commands.quest.not_found"));
            }
            case "finish" -> {
                GameQuest quest = targetPlayer.getQuestManager().getQuestById(questId);

                if (quest == null) {
                    CommandHandler.sendMessage(sender, translate(sender, "commands.quest.not_found"));
                    return;
                }

                quest.finish();

                CommandHandler.sendMessage(sender, translate(sender, "commands.quest.finished", questId));
            }
            default -> {
                sendUsageMessage(sender);
            }
        }
    }
}
