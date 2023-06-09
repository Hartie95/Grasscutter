package emu.grasscutter.command.commands;

import emu.grasscutter.command.Command;
import emu.grasscutter.command.CommandHandler;
import emu.grasscutter.game.player.Player;
import emu.grasscutter.game.world.Scene;
import emu.grasscutter.game.world.SceneGroupInstance;
import emu.grasscutter.scripts.data.SceneGroup;
import emu.grasscutter.utils.Position;
import lombok.Setter;
import lombok.val;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;

import static emu.grasscutter.command.CommandHelpers.*;
import static emu.grasscutter.utils.Language.translate;

@Command(label = "group",
         aliases = {"g"},
         usage = {
            "(refresh) [grp<groupId>] [suite<suiteId>]",
            "(register/unregister) [grp<groupId>] [suite<suiteId>] [scene<sceneId>]",
            "(list) [scene<sceneId>]"
         },
         permission = "player.group",
         permissionTargeted = "player.group.others")
public final class GroupCommand implements CommandHandler {

    private static class GroupParameters {
        @Setter public int groupId;
        @Setter public int suiteId = 0;
        @Setter public int sceneId = 0;
        public Scene scene = null;
    }
    private static final Map<Pattern, BiConsumer<GroupParameters, Integer>> intCommandHandlers = Map.ofEntries(
        Map.entry(groupRegex, GroupParameters::setGroupId),
        Map.entry(sceneRegex, GroupParameters::setSceneId),
        Map.entry(suiteRegex, GroupParameters::setSuiteId)
    );

    @Override
    public void execute(Player sender, Player targetPlayer, List<String> args) {
        GroupParameters param = new GroupParameters();
        parseIntParameters(args, param, intCommandHandlers);

        if (args.isEmpty()) {
            sendUsageMessage(sender);
            return;
        }
        String cmd = args.remove(0).toLowerCase();
        param.scene = param.sceneId == 0 ? targetPlayer.getScene() : targetPlayer.getWorld().getSceneById(param.sceneId);

        if(param.scene == null) {
            CommandHandler.sendMessage(sender, "unknown/unloaded scene "+ param.sceneId);
            return;
        }
        switch (cmd) {
            case "refresh" -> {
                SceneGroupInstance groupInstance = param.scene.getScriptManager().getGroupInstanceById(param.groupId);
                if(groupInstance == null) {
                    CommandHandler.sendMessage(sender, translate(sender, "commands.group.group_not_found", param.groupId));
                    return;
                }

                if(args.size() >= 2) {
                    param.scene.getScriptManager().refreshGroup(groupInstance, param.suiteId, false);
                } else {
                    param.scene.getScriptManager().refreshGroup(groupInstance);
                }

                CommandHandler.sendMessage(sender, translate(sender, "commands.group.refreshed", param.groupId));
            }
            case "register" -> {
                var loadedSuite = param.scene.loadDynamicGroup(param.groupId);
                if(param.suiteId!=0 && param.suiteId!=loadedSuite){
                    if(param.scene.getScriptManager().refreshGroupSuite(param.groupId, param.suiteId)){
                        loadedSuite = param.suiteId;
                    }
                }
                CommandHandler.sendMessage(sender, "registered group "+param.groupId+" with suite "+ loadedSuite);
            }
            case "unregister" -> {
                val result = param.scene.unregisterDynamicGroup(param.groupId);
                CommandHandler.sendMessage(sender, "unloaded group:"+param.groupId+" "+ result);
            }
            case "list" -> {
                val builder = new StringBuilder();
                for(SceneGroupInstance group : param.scene.getScriptManager().getCachedGroupInstances().values()){
                    builder.append("g")
                        .append(group.getGroupId())
                        .append(" s")
                        .append(group.getActiveSuiteId())
                        .append(", ");
                }
                builder.delete(builder.length()-3, builder.length()-1);
                CommandHandler.sendMessage(sender, "loaded groups:\n"+ builder);
            }
            default -> {
                sendUsageMessage(sender);
            }
        }
    }
}
