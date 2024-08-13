package emu.grasscutter.server.packet.send;

import emu.grasscutter.net.packet.BaseTypedPacket;
import emu.grasscutter.net.proto.RetcodeOuterClass;
import org.anime_game_servers.multi_proto.gi.messages.general.item.ItemParam;
import org.anime_game_servers.multi_proto.gi.messages.item.combine.CombineReq;
import org.anime_game_servers.multi_proto.gi.messages.item.combine.CombineRsp;

import java.util.List;

public class PacketCombineRsp extends BaseTypedPacket<CombineRsp> {

    public PacketCombineRsp() {
        this(RetcodeOuterClass.Retcode.RET_SVR_ERROR_VALUE);
    }

    public PacketCombineRsp(int retcode) {
        super(new CombineRsp());
        proto.setRetCode(retcode);
    }

    public PacketCombineRsp(CombineReq combineReq,
                            List<ItemParam> costItemList,
                            List<ItemParam> resultItemList,
                            List<ItemParam> totalRandomItemList,
                            List<ItemParam> totalReturnItemList,
                            List<ItemParam> totalExtraItemList) {

        super(new CombineRsp());
        proto.setRetCode(RetcodeOuterClass.Retcode.RET_SUCC_VALUE);
        proto.setCombineId(combineReq.getCombineId());
        proto.setCombineCount(combineReq.getCombineCount());
        proto.setAvatarGuid(combineReq.getAvatarGuid());
        proto.setCostItemList(costItemList);
        proto.setResultItemList(resultItemList);
        proto.setTotalRandomItemList(totalRandomItemList);
        proto.setTotalReturnItemList(totalReturnItemList);
        proto.setTotalExtraItemList(totalExtraItemList);
    }
}
