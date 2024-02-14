package emu.grasscutter.game.entity.gadget.chest;

import emu.grasscutter.game.entity.gadget.content.GadgetChest;
import emu.grasscutter.game.player.Player;

public interface ChestInteractHandler {

    boolean isTwoStep();

    boolean onInteract(GadgetChest chest, Player player);
}
