package emu.grasscutter.server.packet.send;

import emu.grasscutter.net.packet.BaseTypedPacket;
import org.anime_game_servers.multi_proto.gi.messages.ability.ClientAbilitiesInitFinishCombineNotify;
import org.anime_game_servers.multi_proto.gi.messages.ability.EntityAbilityInvokeEntry;

import java.util.List;

public class PacketClientAbilitiesInitFinishCombineNotify extends BaseTypedPacket<ClientAbilitiesInitFinishCombineNotify> {

	public PacketClientAbilitiesInitFinishCombineNotify(List<EntityAbilityInvokeEntry> entries) {
        super(new ClientAbilitiesInitFinishCombineNotify(), true);
        proto.setEntityInvokeList(entries);
	}
}
