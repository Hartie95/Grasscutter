package emu.grasscutter.server.packet.send;

import emu.grasscutter.game.entity.create_config.CreateGadgetEntityConfig;
import emu.grasscutter.game.player.Player;
import emu.grasscutter.game.entity.EntityVehicle;
import emu.grasscutter.game.entity.GameEntity;

import emu.grasscutter.net.packet.BaseTypedPacket;

import emu.grasscutter.utils.Position;
import org.anime_game_servers.multi_proto.gi.messages.gadget.CreateVehicleRsp;
import org.anime_game_servers.multi_proto.gi.messages.gadget.VehicleInteractType;
import org.anime_game_servers.multi_proto.gi.messages.general.vehicle.VehicleMember;
import lombok.val;

import java.util.List;

public class PacketCreateVehicleRsp extends BaseTypedPacket<CreateVehicleRsp> {

    public PacketCreateVehicleRsp(Player player, int vehicleId, int pointId, Position pos, Position rot) {
        super(new CreateVehicleRsp());

        // Eject vehicle members and Kill previous vehicles if there are any
        List<GameEntity<?>> previousVehicles = player.getScene().getEntities().values().stream()
                .filter(entity -> entity instanceof EntityVehicle
                        && ((EntityVehicle) entity).getGadgetId() == vehicleId
                        && ((EntityVehicle) entity).getOwner().equals(player))
                .toList();

        previousVehicles.stream().forEach(entity -> {
            List<VehicleMember> vehicleMembers = ((EntityVehicle) entity).getVehicleMembers().stream().toList();

            vehicleMembers.stream().forEach(vehicleMember -> {
                player.getScene().broadcastPacket(new PacketVehicleInteractRsp(((EntityVehicle) entity), vehicleMember, VehicleInteractType.VEHICLE_INTERACT_OUT));
            });

            player.getScene().killEntity(entity, 0);
        });

        val config = new CreateGadgetEntityConfig(vehicleId)
            .setBornPos(pos)
            .setBornRot(rot);

        EntityVehicle vehicle = new EntityVehicle(player.getScene(), player, config);
        player.getScene().addEntity(vehicle);

        proto.setVehicleId(vehicleId);
        proto.setEntityId(vehicle.getId());
    }
}
