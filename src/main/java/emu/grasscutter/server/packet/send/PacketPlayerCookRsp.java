package emu.grasscutter.server.packet.send;

import emu.grasscutter.game.inventory.GameItem;
import emu.grasscutter.net.packet.BaseTypedPacket;
import emu.grasscutter.net.proto.RetcodeOuterClass.Retcode;
import lombok.val;
import org.anime_game_servers.multi_proto.gi.messages.cooking.CookRecipeData;
import org.anime_game_servers.multi_proto.gi.messages.cooking.PlayerCookRsp;

import java.util.List;

public class PacketPlayerCookRsp extends BaseTypedPacket<PlayerCookRsp> {
    public PacketPlayerCookRsp(Retcode retcode) {
        super(new PlayerCookRsp());
        proto.setRetcode(retcode.getNumber());
    }

    public PacketPlayerCookRsp(List<GameItem> output, int quality, int count, int recipeId, int proficiency) {
        super(new PlayerCookRsp());
        val cookRecipeData = new CookRecipeData();
        cookRecipeData.setRecipeId(recipeId);
        cookRecipeData.setProficiency(proficiency);
        proto.setRecipeData(cookRecipeData);
        proto.setQteQuality(quality);
        proto.setCookCount(count);
        proto.setItemList(output.stream().map(GameItem::toItemParam).toList());
    }
}
