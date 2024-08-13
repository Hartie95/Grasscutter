package emu.grasscutter.server.packet.send;

import emu.grasscutter.net.packet.BaseTypedPacket;
import emu.grasscutter.net.proto.RetcodeOuterClass;
import org.anime_game_servers.multi_proto.gi.messages.activity.trial.EnterTrialAvatarActivityDungeonRsp;

public class PacketEnterTrialAvatarActivityDungeonRsp extends BaseTypedPacket<EnterTrialAvatarActivityDungeonRsp> {
	public PacketEnterTrialAvatarActivityDungeonRsp(int activityId, int trialAvatarIndexId, boolean success) {
		this(activityId, trialAvatarIndexId, success ? RetcodeOuterClass.Retcode.RET_SUCC_VALUE : RetcodeOuterClass.Retcode.RET_FAIL_VALUE);
	}

	public PacketEnterTrialAvatarActivityDungeonRsp(int activityId, int trialAvatarIndexId, int retcodeVal) {
        super(new EnterTrialAvatarActivityDungeonRsp());
        proto.setActivityId(activityId);
        proto.setTrialAvatarIndexId(trialAvatarIndexId);
        proto.setRetcode(retcodeVal);
	}
}
