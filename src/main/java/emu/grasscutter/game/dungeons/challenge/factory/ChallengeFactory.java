package emu.grasscutter.game.dungeons.challenge.factory;

import emu.grasscutter.data.GameData;
import emu.grasscutter.data.excels.DungeonChallengeConfigData;
import emu.grasscutter.game.dungeons.challenge.WorldChallenge;
import emu.grasscutter.game.world.Scene;
import emu.grasscutter.scripts.data.SceneGroup;
import lombok.Getter;
import org.reflections.Reflections;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static emu.grasscutter.game.dungeons.challenge.enums.ChallengeType.CHALLENGE_NONE;

public class ChallengeFactory {
    @Getter private static final List<ChallengeFactoryHandler> challengeFactoryHandlers = new ArrayList<>();

    static {
        // Use reflection to scan and find the classes that implement the interface
        Reflections reflections = new Reflections("emu.grasscutter.game.dungeons.challenge.factory");

        // Instantiate objects of those classes dynamically
        reflections.getSubTypesOf(ChallengeFactoryHandler.class).forEach(clazz -> {
            try {
                getChallengeFactoryHandlers().add(clazz.getDeclaredConstructor().newInstance());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    // indices: [currentChallengeIndex, currentChallengeId, fatherChallengeIndex]
    public static WorldChallenge getChallenge(List<Integer> indices, List<Integer> params, Scene scene, SceneGroup group){
        return getChallengeFactoryHandlers().stream()
            .filter(handler -> handler.isThisType(
                Optional.ofNullable(GameData.getDungeonChallengeConfigDataMap().get(indices.get(1).intValue()))
                    .map(DungeonChallengeConfigData::getChallengeType)
                    .orElse(CHALLENGE_NONE))
            ).map(handler -> handler.build(indices, params, scene, group))
            .findFirst().orElse(null);
    }
}
