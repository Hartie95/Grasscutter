package emu.grasscutter.game.player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import emu.grasscutter.data.GameData;
import emu.grasscutter.data.excels.BuffData;
import emu.grasscutter.game.ability.AbilityManager;
import emu.grasscutter.game.avatar.Avatar;
import emu.grasscutter.game.entity.GameEntity;
import emu.grasscutter.net.proto.ServerBuffChangeNotifyOuterClass.ServerBuffChangeNotify.ServerBuffChangeType;
import emu.grasscutter.net.proto.ServerBuffOuterClass.ServerBuff;
import emu.grasscutter.server.packet.send.PacketServerBuffChangeNotify;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import lombok.Getter;
import lombok.val;

public class PlayerBuffManager extends BasePlayerManager {
    private int nextBuffUid;

    private final List<PlayerBuff> pendingBuffs;
    private final Int2ObjectMap<PlayerBuff> buffs; // Server buffs

    public PlayerBuffManager(Player player) {
        super(player);
        this.buffs = new Int2ObjectOpenHashMap<>();
        this.pendingBuffs = new ArrayList<>();
    }

    /**
     * Gets a new uid for a server buff
     * @return New integer buff uid
     */
    private int getNextBuffUid() {
        return ++nextBuffUid;
    }

    private AbilityManager getAbilityManager() {
        return this.player.getAbilityManager();
    }

    /**
     * Returns true if the player has a buff with this group id
     * @param groupId Buff group id
     * @return True if a buff with this group id exists
     */
    public synchronized boolean hasBuff(int groupId) {
        return this.buffs.containsKey(groupId);
    }

    /**
     * Clears all player buffs
     */
    public synchronized void clearBuffs() {
        // Remove from player
        getPlayer().sendPacket(new PacketServerBuffChangeNotify(
            getPlayer(), ServerBuffChangeType.SERVER_BUFF_CHANGE_TYPE_DEL_SERVER_BUFF, this.buffs.values()));

        // Clear
        this.buffs.clear();
    }

    private void onAddAbility(GameEntity targetEntity, BuffData buffData) {
        Optional.ofNullable(targetEntity.getAbilities().get(buffData.getAbilityName()))
            .ifPresent(ability -> Optional.ofNullable(ability.getData()).map(abilityData -> abilityData.modifiers)
                .map(modifiersMap -> modifiersMap.get(buffData.getModifierName()))
                .map(abilityModifier -> abilityModifier.onAdded).stream().flatMap(Arrays::stream)
                .forEach(modifierAction -> getAbilityManager().executeAction(ability, modifierAction)));
    }

    /**
     * Adds a server buff to the player.
     * @param buffId Server buff id
     * @return True if a buff was added
     */
    public boolean addBuff(int buffId) {
        return addBuff(buffId, -1f);
    }

    /**
     * Adds a server buff to the player.
     * @param buffId Server buff id
     * @param duration Duration of the buff in seconds. Set to 0 for an infinite buff.
     * @return True if a buff was added
     */
    public synchronized boolean addBuff(int buffId, float duration) {
        return addBuff(buffId, duration, null);
    }

    /**
     * Adds a server buff to the player.
     * @param buffId Server buff id
     * @param duration Duration of the buff in seconds. Set to 0 for an infinite buff.
     * @param target Target avatar
     * @return True if a buff was added
     */
    public synchronized boolean addBuff(int buffId, float duration, Avatar target) {
        // Get buff excel data
        BuffData buffData = GameData.getBuffDataMap().get(buffId);
        if (buffData == null) return false;

        // Add buffs to target
        switch (buffData.getServerBuffType()) {
            case SERVER_BUFF_AVATAR -> {
                if (target == null) break;

                getAbilityManager().addAbilityToEntity(target.getAsEntity(), buffData.getAbilityName());
                onAddAbility(target.getAsEntity(), buffData);
            }
            case SERVER_BUFF_TEAM -> this.player.getTeamManager().getActiveTeam().forEach(entityAvatar -> {
                getAbilityManager().addAbilityToEntity(entityAvatar, buffData.getAbilityName());
                onAddAbility(entityAvatar, buffData);
            });
        }

        // Set duration
        if (duration < 0f) {
            duration = buffData.getTime();
        }

        // Don't add buff if duration is equal or less than 0
//        if (duration <= 0) {
//            return success;
//        }

        // Clear previous buff if it exists
        this.removeBuff(buffData.getGroupId());

        // Create and store buff
        PlayerBuff buff = new PlayerBuff(getNextBuffUid(), buffData, duration);
        this.buffs.put(buff.getGroupId(), buff);

        // Packet
        getPlayer().sendPacket(new PacketServerBuffChangeNotify(
            getPlayer(), ServerBuffChangeType.SERVER_BUFF_CHANGE_TYPE_ADD_SERVER_BUFF, buff));

        return true;
    }

    /**
     * Removes a buff by its group id
     * @param buffGroupId Server buff group id
     */
    public synchronized void removeBuff(int buffGroupId) {
        val buff = Optional.ofNullable(this.buffs.remove(buffGroupId));
        buff.ifPresent(playerBuff ->
            getPlayer().sendPacket(new PacketServerBuffChangeNotify(
                getPlayer(), ServerBuffChangeType.SERVER_BUFF_CHANGE_TYPE_DEL_SERVER_BUFF, playerBuff)));
//        return buff.isPresent();
    }

    public synchronized void onTick() {
        // Skip if no buffs
        if (this.buffs.isEmpty()) return;

        // Add to pending buffs to remove if buff has expired
        this.buffs.values().removeIf(buff -> {
            if (System.currentTimeMillis() <= buff.getEndTime())
                return false;
            this.pendingBuffs.add(buff);
            return true;
        });

        if (this.pendingBuffs.size() > 0) {
            // Send packet
            getPlayer().sendPacket(new PacketServerBuffChangeNotify(
                getPlayer(), ServerBuffChangeType.SERVER_BUFF_CHANGE_TYPE_DEL_SERVER_BUFF, this.pendingBuffs));
            this.pendingBuffs.clear();
        }
    }

    @Getter
    public static class PlayerBuff {
        private final int uid;
        private final BuffData buffData;
        private final long endTime;

        public PlayerBuff(int uid, BuffData buffData, float duration) {
            this.uid = uid;
            this.buffData = buffData;
            this.endTime = System.currentTimeMillis() + ((long) duration * 1000);
        }

        public int getGroupId() {
            return getBuffData().getGroupId();
        }

        public ServerBuff toProto() {
            return ServerBuff.newBuilder()
                .setServerBuffUid(this.getUid())
                .setServerBuffId(this.getBuffData().getId())
                .setServerBuffType(this.getBuffData().getServerBuffType().getValue())
                .setInstancedModifierId(1)
                .build();
        }
    }
}
