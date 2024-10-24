package emu.grasscutter.server.packet.send;

import emu.grasscutter.game.entity.EntityBaseGadget;
import emu.grasscutter.net.packet.BaseTypedPacket;
import org.anime_game_servers.multi_proto.gi.messages.gadget.GadgetStateNotify;

public class PacketGadgetStateNotify extends BaseTypedPacket<GadgetStateNotify> {

	public PacketGadgetStateNotify(EntityBaseGadget gadget, int newState) {
        super(new GadgetStateNotify());
        proto.setGadgetEntityId(gadget.getId());
        proto.setGadgetState(newState);
        proto.setEnableInteract(gadget.isInteractEnabled());
	}
}
