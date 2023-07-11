package emu.grasscutter.server.packet.send;

import emu.grasscutter.game.dungeons.challenge.WorldChallenge;
import emu.grasscutter.net.packet.BasePacket;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.proto.DungeonChallengeFinishNotifyOuterClass.DungeonChallengeFinishNotify;
import emu.grasscutter.net.proto.ChallengeFinishTypeOuterClass.ChallengeFinishType;

public class PacketDungeonChallengeFinishNotify extends BasePacket {

	public PacketDungeonChallengeFinishNotify(WorldChallenge challenge) {
		super(PacketOpcodes.DungeonChallengeFinishNotify, true);

		DungeonChallengeFinishNotify proto = DungeonChallengeFinishNotify.newBuilder()
            .setChallengeIndex(challenge.getInfo().challengeIndex())
            .setIsSuccess(challenge.isSuccess())
            .setTimeCost(challenge.getFinishedTime() - challenge.getStartedAt())
            .setFinishType(challenge.isSuccess() ?
                ChallengeFinishType.CHALLENGE_FINISH_TYPE_SUCC :
                ChallengeFinishType.CHALLENGE_FINISH_TYPE_FAIL
            )
            .build();

		this.setData(proto);
	}
}
