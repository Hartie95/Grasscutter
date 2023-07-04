package emu.grasscutter.server.packet.send;

import emu.grasscutter.game.home.FurnitureMakeSlotItem;
import emu.grasscutter.net.proto.RetcodeOuterClass.Retcode;
import emu.grasscutter.net.packet.BasePacket;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.proto.FurnitureMakeCancelRspOuterClass.FurnitureMakeCancelRsp;
import emu.grasscutter.net.proto.FurnitureMakeSlotOuterClass.FurnitureMakeSlot;
import emu.grasscutter.net.proto.FurnitureMakeDataOuterClass.FurnitureMakeData;

import java.util.List;

public class PacketFurnitureMakeCancelRsp extends BasePacket {

	public PacketFurnitureMakeCancelRsp(Retcode result, int makeId, List<FurnitureMakeSlotItem> slotItem) {
		super(PacketOpcodes.FurnitureMakeCancelRsp);

		FurnitureMakeCancelRsp.Builder proto = FurnitureMakeCancelRsp.newBuilder();

		proto.setRetcode(result.getNumber()).setMakeId(makeId);

		if (slotItem != null) {
			FurnitureMakeSlot.Builder slotProto = FurnitureMakeSlot.newBuilder();

			slotItem.forEach(x -> {
				slotProto.addFurnitureMakeDataList(FurnitureMakeData.newBuilder()
					.setIndex(x.getIndex())
					.setDurTime(x.getDurTime())
					.setBeginTime(x.getBeginTime())
					.setAvatarId(x.getAvatarId())
					.setMakeId(x.getMakeId())
					.build());				
			});
			proto.setFurnitureMakeSlot(slotProto.build());
		}

		this.setData(proto.build());
	}
}
