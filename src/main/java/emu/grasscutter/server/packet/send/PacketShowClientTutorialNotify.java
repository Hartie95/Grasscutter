package emu.grasscutter.server.packet.send;

import emu.grasscutter.net.packet.BasePacket;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.proto.ShowClientTutorialNotifyOuterClass.ShowClientTutorialNotify;

public class PacketShowClientTutorialNotify extends BasePacket{
    public PacketShowClientTutorialNotify(int tutorialId) {
        super(PacketOpcodes.ShowClientTutorialNotify);

        this.setData(ShowClientTutorialNotify.newBuilder()
            .setTutorialId(tutorialId)
            .build());
    }
}
