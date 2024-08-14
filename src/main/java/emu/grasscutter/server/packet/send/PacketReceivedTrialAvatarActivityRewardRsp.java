package emu.grasscutter.server.packet.send;

import emu.grasscutter.net.packet.BaseTypedPacket;
import emu.grasscutter.net.proto.RetcodeOuterClass.Retcode;
import org.anime_game_servers.multi_proto.gi.messages.activity.trial.ReceivedTrialAvatarActivityRewardRsp;

public class PacketReceivedTrialAvatarActivityRewardRsp extends BaseTypedPacket<ReceivedTrialAvatarActivityRewardRsp> {
	public PacketReceivedTrialAvatarActivityRewardRsp(int activityId, int trialAvatarId, boolean success) {
		this(activityId, trialAvatarId, success ? Retcode.RET_SUCC_VALUE : Retcode.RET_FAIL_VALUE);
	}

	public PacketReceivedTrialAvatarActivityRewardRsp(int activityId, int trialAvatarId, int retcodeVal) {
        super(new ReceivedTrialAvatarActivityRewardRsp());
        proto.setActivityId(activityId);
        proto.setTrialAvatarIndexId(trialAvatarId);
        proto.setRetcode(retcodeVal);
	}
}
