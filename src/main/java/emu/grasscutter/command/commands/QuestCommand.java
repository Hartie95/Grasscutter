package emu.grasscutter.command.commands;

import emu.grasscutter.command.Command;
import emu.grasscutter.command.CommandHandler;
import emu.grasscutter.game.player.Player;
import emu.grasscutter.game.quest.GameQuest;
import emu.grasscutter.game.world.Scene;
import emu.grasscutter.utils.Position;
import lombok.Setter;
import lombok.val;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;

import static emu.grasscutter.command.CommandHelpers.*;
import static emu.grasscutter.utils.Language.translate;

@Command(label = "quest",
         aliases = {"q"},
         usage = {"(add|finish|status) [<questId> ...] [m<mainquestId> ...]", "enable", "active"},
         permission = "player.quest",
         permissionTargeted = "player.quest.others")
public final class QuestCommand implements CommandHandler {

    private static class QuestParameters {
        @Setter public Set<Integer> subIds = new HashSet<>();
        @Setter public Set<Integer> mainIds = new HashSet<>();
        public void addSubId(Integer id) {
            subIds.add(id);
        }
        public void addMainId(Integer id) {
            mainIds.add(id);
        }
    }
    private static final Map<Pattern, BiConsumer<QuestParameters, Integer>> intCommandHandlers = Map.ofEntries(
        Map.entry(mainQuestRegex, QuestParameters::addMainId),
        Map.entry(subQuestRegex, QuestParameters::addSubId)
    );
    @Override
    public void execute(Player sender, Player targetPlayer, List<String> args) {
        if (args.size() == 0) {
            sendUsageMessage(sender);
            return;
        }
        QuestParameters questParameters = new QuestParameters();
        parseIntParameters(args, questParameters, intCommandHandlers);

        String cmd = args.remove(0).toLowerCase();

        while(!args.isEmpty()){
            try {
                questParameters.addSubId(Integer.parseInt(args.remove(0)));
            }
            catch (Exception e) {
                CommandHandler.sendMessage(sender, translate(sender, "commands.quest.invalid_id"));
            }
        }

        if (questParameters.mainIds.isEmpty() && questParameters.subIds.isEmpty() && !cmd.equals("enable")) {
            CommandHandler.sendMessage(sender, translate(sender, "commands.quest.invalid_id"));
            return;
        }

        switch (cmd) {
            case "enable" -> {
                targetPlayer.getQuestManager().enableQuests();
                CommandHandler.sendMessage(sender, translate(sender, "commands.quest.enabled"));
            }
            case "add" -> {
                List<Integer> failedSubQuests = new ArrayList<>();
                List<Integer> addedSubQuests = new ArrayList<>();

                List<Integer> failedMainQuests = new ArrayList<>();
                List<Integer> addMainQuests = new ArrayList<>();

                questParameters.subIds.forEach(id -> {
                    if(addSubQuest(targetPlayer, id)){
                        addedSubQuests.add(id);
                    } else {
                        failedSubQuests.add(id);
                    }
                });
                questParameters.mainIds.forEach(id -> {
                    if(addMainQuest(targetPlayer, id)){
                        addMainQuests.add(id);
                    }else {
                        failedMainQuests.add(id);
                    }
                });
                sendResultMessage(sender, "commands.quest.added", "commands.quest.not_found",
                    failedSubQuests, addedSubQuests, failedMainQuests, addMainQuests);

            }
            case "finish" -> {
                List<Integer> failedSubQuests = new ArrayList<>();
                List<Integer> finishedSubQuests = new ArrayList<>();

                List<Integer> failedMainQuests = new ArrayList<>();
                List<Integer> finishedMainQuests = new ArrayList<>();

                questParameters.subIds.forEach(id -> {
                    if(finishSubQuest(targetPlayer, id)){
                        finishedSubQuests.add(id);
                    } else {
                        failedSubQuests.add(id);
                    }
                });
                questParameters.mainIds.forEach(id -> {
                    if(finishMainQuest(targetPlayer, id)){
                        finishedMainQuests.add(id);
                    }else {
                        failedMainQuests.add(id);
                    }
                });
                sendResultMessage(sender, "commands.quest.finished", "commands.quest.not_found",
                    failedSubQuests, finishedSubQuests, failedMainQuests, finishedMainQuests);
            }

            case "status" -> {
                val  resultString = new StringBuilder();

                questParameters.subIds.forEach(id -> {
                    resultString.append(getSubQuestStatus(targetPlayer, id));
                });
                questParameters.mainIds.forEach(id -> {
                    resultString.append(getMainQuestStatus(targetPlayer, id));
                });
                CommandHandler.sendMessage(sender, resultString.toString());
            }
            default -> {
                sendUsageMessage(sender);
            }
        }
    }
    private void sendResultMessage(Player sender, String finishedKey,String failedKey, List<Integer> failedSubQuests,List<Integer> finishedSubQuests,
                                   List<Integer> failedMainQuests, List<Integer> finishedMainQuests){
        if(!finishedMainQuests.isEmpty() || !finishedSubQuests.isEmpty()){
            val questsString = finishedSubQuests.stream().map(String::valueOf).reduce((a, b) -> a + ", " + b).orElse("")+
                finishedMainQuests.stream().map(String::valueOf).reduce((a, b) -> "m"+a + ", " + b).orElse("");
            CommandHandler.sendMessage(sender, translate(sender, finishedKey, questsString));
        }
        if(!failedSubQuests.isEmpty() || !failedMainQuests.isEmpty()){
            val questsString = finishedSubQuests.stream().map(String::valueOf).reduce((a, b) -> a + ", " + b).orElse("")+
                finishedMainQuests.stream().map(String::valueOf).reduce((a, b) -> "m"+a + ", " + b).orElse("");
            CommandHandler.sendMessage(sender, translate(sender, failedKey, questsString));
        }
    }

    private boolean addSubQuest(Player targetPlayer, int questId){
        GameQuest quest = targetPlayer.getQuestManager().addQuest(questId);
        return quest != null;
    }
    private boolean addMainQuest(Player targetPlayer, int mainQuestId){
        return targetPlayer.getQuestManager().startMainQuest(mainQuestId);
    }
    private boolean finishSubQuest(Player targetPlayer, int questId){
        GameQuest quest = targetPlayer.getQuestManager().addQuest(questId);
        return quest != null;
    }
    private boolean finishMainQuest(Player targetPlayer, int mainQuestId){
        return targetPlayer.getQuestManager().startMainQuest(mainQuestId);
    }

    private String getMainQuestStatus(Player targetPlayer, int mainQuestId){
        val mainQuest = targetPlayer.getQuestManager().getMainQuestById(mainQuestId);
        if(mainQuest==null){
            return "m"+mainQuestId+" status: Not found\n\n";
        }
        val mainStatus = "m"+mainQuestId+" status: "+mainQuest.getState().toString();
        val subStatus = mainQuest.getChildQuests().values().stream().map(subQuest -> subQuest.getSubQuestId()+" status: "+subQuest.getState().toString()).reduce((a, b) -> a + ", " + b).orElse("");
        return mainStatus+"\n"+subStatus+"\n";
    }
    private String getSubQuestStatus(Player targetPlayer, int subQuestId){
        val quest = targetPlayer.getQuestManager().getQuestById(subQuestId);
        if(quest==null){
            return subQuestId+" status: Not found\n\n";
        }
        return subQuestId+" status: "+quest.getState().toString()+"\n\n";

    }
}
