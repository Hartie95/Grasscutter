package emu.grasscutter.game.talk;

import emu.grasscutter.Grasscutter;
import emu.grasscutter.data.excels.TalkConfigData;
import emu.grasscutter.data.excels.TalkConfigData.TalkExecParam;
import emu.grasscutter.game.player.Player;
import emu.grasscutter.server.game.BaseGameSystem;
import emu.grasscutter.server.game.GameServer;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.reflections.Reflections;

@SuppressWarnings("unchecked")
public class TalkSystem extends BaseGameSystem {
    private final Int2ObjectMap<TalkExecHandler> execHandlers;

    public TalkSystem(GameServer server) {
        super(server);

        this.execHandlers = new Int2ObjectOpenHashMap<>();

        this.registerHandlers();
    }

    public void registerHandlers() {
        this.registerHandlers(this.execHandlers, "emu.grasscutter.game.talk.exec", TalkExecHandler.class);
    }

    public <T> void registerHandlers(Int2ObjectMap<T> map, String packageName, Class<T> clazz) {
        Reflections reflections = new Reflections(packageName);
        var handlerClasses = reflections.getSubTypesOf(clazz);

        for (var obj : handlerClasses) {
            this.registerPacketHandler(map, obj);
        }
    }

    public <T> void registerPacketHandler(Int2ObjectMap<T> map, Class<? extends T> handlerClass) {
        try {
            int value = 0;
            if (handlerClass.isAnnotationPresent(TalkValueExec.class)) {
                TalkValueExec opcode = handlerClass.getAnnotation(TalkValueExec.class);
                value = opcode.value().getValue();
            } else {
                return;
            }

            if (value <= 0) return;

            map.put(value, handlerClass.getDeclaredConstructor().newInstance());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

	public void triggerExec(Player player, TalkConfigData talkData, TalkExecParam execParam) {
		TalkExecHandler handler = execHandlers.get(execParam.getType().getValue());

        if (handler == null) {
            Grasscutter.getLogger().debug("Could not trigger talk exec {} at {}", execParam.getType().getValue(), talkData.getId());
            return;
        }

		handler.execute(player, talkData, execParam);
	}
}
