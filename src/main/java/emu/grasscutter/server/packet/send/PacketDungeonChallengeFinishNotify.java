package emu.grasscutter.server.packet.send;

import emu.grasscutter.game.dungeons.challenge.WorldChallenge;
import emu.grasscutter.net.packet.BasePacket;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.proto.DungeonChallengeFinishNotifyOuterClass.DungeonChallengeFinishNotify;
import emu.grasscutter.net.proto.Unk2700FHOKHHBGPEG.Unk2700_FHOKHHBGPEG;

public class PacketDungeonChallengeFinishNotify extends BasePacket {

	public PacketDungeonChallengeFinishNotify(WorldChallenge challenge, int challengeRecordType) {
		super(PacketOpcodes.DungeonChallengeFinishNotify, true);

		DungeonChallengeFinishNotify proto = DungeonChallengeFinishNotify.newBuilder()
				.setChallengeIndex(challenge.getChallengeIndex())
				.setIsSuccess(challenge.isSuccess())
				.setTimeCost(challenge.getFinishedTime())
				.setChallengeRecordType(challengeRecordType)
				// its like some challenge state i guess
				.setUnk2700ONCDLPDHFAB(challenge.isSuccess() ? 
					Unk2700_FHOKHHBGPEG.Unk2700_FHOKHHBGPEG_SUCC :
					Unk2700_FHOKHHBGPEG.Unk2700_FHOKHHBGPEG_FAIL) // TODO, hopefully someone godly can replace it with readable name
				.build();

		this.setData(proto);
	}
}
