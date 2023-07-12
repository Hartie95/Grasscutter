package emu.grasscutter.command.commands;

import emu.grasscutter.command.Command;
import emu.grasscutter.command.CommandHandler;
import emu.grasscutter.game.entity.EntityGadget;
import emu.grasscutter.game.player.Player;
import emu.grasscutter.game.world.Scene;
import lombok.Getter;
import lombok.Setter;
import lombok.val;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;

import static emu.grasscutter.command.CommandHelpers.*;

@Command(label = "gadget",
    aliases = {"gt"},
    usage = {
        "(state) [grp<groupId>] [cfg<configId>] [st<gadgetState>]"
    },
    permission = "player.gadget",
    permissionTargeted = "player.gadget.others")
public class GadgetCommand implements CommandHandler {
    private static class GadgetParameters {
        @Setter @Getter public int groupId;
        @Setter @Getter public int configId = 0;
        @Setter @Getter public int state = 0;
        @Setter @Getter public int sceneId = 0;
        @Setter @Getter public Scene scene = null;
    }

    @Getter
    private static final Map<Pattern, BiConsumer<GadgetParameters, Integer>> intCommandHandlers = Map.ofEntries(
        Map.entry(groupRegex, GadgetParameters::setGroupId),
        Map.entry(configRegex, GadgetParameters::setConfigId),
        Map.entry(stateRegex, GadgetParameters::setState),
        Map.entry(sceneRegex, GadgetParameters::setSceneId)
    );

    @Override
    public void execute(Player sender, Player targetPlayer, List<String> args) {
        GadgetParameters param = new GadgetParameters();
        parseIntParameters(args, param, getIntCommandHandlers());

        if (args.isEmpty()) {
            sendUsageMessage(sender);
            return;
        }
        String cmd = args.remove(0).toLowerCase();
        param.setScene(param.getSceneId() == 0 ? targetPlayer.getScene() : targetPlayer.getWorld().getSceneById(param.getSceneId()));

        if(param.getScene() == null) {
            CommandHandler.sendMessage(sender, "unknown/unloaded scene "+ param.getSceneId());
            return;
        }

        switch (cmd) {
            case "state" -> {
                val entity = param.getScene().getEntityByConfigId(param.getConfigId(), param.getGroupId());
                if(entity instanceof EntityGadget gadget){
                    gadget.updateState(param.getState());
                    CommandHandler.sendMessage(sender, "Gadget State changed.");
                    return;
                }
                CommandHandler.sendMessage(sender, "No gadget found with such properties.");
            }
            case "else" -> {}
            default -> sendUsageMessage(sender);
        }
    }
}
