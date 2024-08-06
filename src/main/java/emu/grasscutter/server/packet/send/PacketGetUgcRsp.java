package emu.grasscutter.server.packet.send;

import emu.grasscutter.net.packet.BaseTypedPacket;
import emu.grasscutter.net.proto.RetcodeOuterClass.Retcode;
import org.anime_game_servers.multi_proto.gi.messages.activity.user_generated_content.GetUgcReq;
import org.anime_game_servers.multi_proto.gi.messages.activity.user_generated_content.GetUgcRsp;
import org.anime_game_servers.multi_proto.gi.messages.activity.user_generated_content.music_game.UgcMusicBriefInfo;
import org.anime_game_servers.multi_proto.gi.messages.activity.user_generated_content.music_game.UgcMusicRecord;

public class PacketGetUgcRsp extends BaseTypedPacket<GetUgcRsp> {

    public PacketGetUgcRsp(UgcMusicBriefInfo briefInfo, UgcMusicRecord musicRecord, GetUgcReq req) {
        super(new GetUgcRsp());
        proto.setUgcGuid(briefInfo.getUgcGuid());
        proto.setUgcType(req.getUgcType());
        proto.setUgcRecordUsage(req.getUgcRecordUsage());
        proto.setRecord(new GetUgcRsp.Record.MusicRecord(musicRecord));
        proto.setBrief(new GetUgcRsp.Brief.MusicBriefInfo(briefInfo));
    }
    public PacketGetUgcRsp(Retcode errorCode, GetUgcReq req) {
        super(new GetUgcRsp());
        proto.setUgcGuid(req.getUgcGuid());
        proto.setUgcType(req.getUgcType());
        proto.setUgcRecordUsage(req.getUgcRecordUsage());
        proto.setRetcode(errorCode.getNumber());
    }
}
