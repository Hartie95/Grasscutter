package emu.grasscutter.server.packet.send;

import emu.grasscutter.net.packet.BasePacket;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.proto.ItemParamOuterClass.ItemParam;
import emu.grasscutter.net.proto.TakeCompoundOutputRspOuterClass.TakeCompoundOutputRsp;

public class PacketTakeCompoundOutputRsp extends BasePacket {

    public PacketTakeCompoundOutputRsp(Iterable<ItemParam> itemList, int retcode) {
        super(PacketOpcodes.TakeCompoundOutputRsp);
        var builder = TakeCompoundOutputRsp.newBuilder()
            .addAllItemList(itemList)
            .setRetcode(retcode);
        setData(builder.build());
    }
}
