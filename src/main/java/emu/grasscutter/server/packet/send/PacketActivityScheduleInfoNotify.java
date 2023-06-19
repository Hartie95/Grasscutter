package emu.grasscutter.server.packet.send;

import emu.grasscutter.game.activity.ActivityConfigItem;
import emu.grasscutter.net.packet.BaseTypedPackage;
import emu.grasscutter.utils.DateHelper;
import lombok.val;
import messages.activity.ActivityScheduleInfo;
import messages.activity.ActivityScheduleInfoNotify;

import java.util.Collection;

public class PacketActivityScheduleInfoNotify extends BaseTypedPackage<ActivityScheduleInfoNotify> {

	public PacketActivityScheduleInfoNotify(Collection<ActivityConfigItem> activityConfigItemList) {
		super(new ActivityScheduleInfoNotify());

        proto.setActivityScheduleList(activityConfigItemList.stream().map(item -> {
            val scheduleInfo = new ActivityScheduleInfo();

            scheduleInfo.setActivityId(item.getActivityId());
            scheduleInfo.setScheduleId(item.getScheduleId());
            scheduleInfo.setOpen(true);
            scheduleInfo.setBeginTime(DateHelper.getUnixTime(item.getBeginTime()));
            scheduleInfo.setEndTime(DateHelper.getUnixTime(item.getEndTime()));

            return scheduleInfo;
        }).toList());
	}
}
