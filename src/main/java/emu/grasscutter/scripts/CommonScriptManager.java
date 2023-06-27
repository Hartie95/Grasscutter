package emu.grasscutter.scripts;

import emu.grasscutter.Grasscutter;
import emu.grasscutter.scripts.data.common.BaseCommonScript;
import emu.grasscutter.scripts.data.common.CommonScriptType;
import lombok.val;
import org.reflections.Reflections;

import javax.script.Bindings;
import javax.script.CompiledScript;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static emu.grasscutter.utils.FileUtils.getScriptPath;

public class CommonScriptManager {
    /**
     * Holds all Common scripts [Key: lua file name without extension, should also be the java class name, value: class instance]
     * */
    private static final Map<String, BaseCommonScript> COMMON_SCRIPTS = new ConcurrentHashMap<>();
    /**
     * Holds all Common scripts [Key: lua file name for mapping, value: class type]
     * */
    private static final Map<String, Class<? extends BaseCommonScript>> COMMON_SCRIPT_FILE_NAME_TO_CLASS = new ConcurrentHashMap<>();
    private static final String SCRIPT_TYPE = "Common";

    /**
     * Loads when server starts
     * */
    public static void load(){
        cacheCommonScripts();
    }

    /**
     * Cache all common scripts to hashMap
     * */
    private static void cacheCommonScripts(){
        // Use reflection to scan and find the classes that extends the BaseCommonScripts class
        Reflections reflections = new Reflections("emu.grasscutter.scripts.data.common");

        // Make a map of scripts' file name to its corresponding class
        reflections.getSubTypesOf(BaseCommonScript.class).forEach(clazz -> {
            try {
                // build a map for file loader to use so that it can build class instance correspondingly
                COMMON_SCRIPT_FILE_NAME_TO_CLASS.put(clazz.getAnnotation(CommonScriptType.class).name(), clazz);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        try {
            Files.newDirectoryStream(getScriptPath(SCRIPT_TYPE + "/"), "*.lua")
                .forEach(path -> {
                String fileName = path.getFileName().toString();
                val commonScript = COMMON_SCRIPT_FILE_NAME_TO_CLASS.getOrDefault(fileName, null);

                if(!fileName.endsWith(".lua") || commonScript == null) return;

                String controllerName = commonScript.getSimpleName();
                CompiledScript cs = ScriptLoader.getScript(SCRIPT_TYPE + "/" + fileName);
                Bindings bindings = ScriptLoader.getEngine().createBindings();
                if (cs == null) return;

                try{
                    cs.eval(bindings);
                    COMMON_SCRIPTS.put(controllerName, commonScript.getDeclaredConstructor(
                        CompiledScript.class, Bindings.class).newInstance(cs, bindings));
                } catch (Throwable e){
                    Grasscutter.getLogger().error("Error while loading common script: {}", fileName);
                }
            });

            Grasscutter.getLogger().info("Loaded {} common scripts", COMMON_SCRIPTS.size());
        } catch (IOException e) {
            Grasscutter.getLogger().error("Error loading common scripts luas");
        }
    }

    public static BaseCommonScript getCommonScripts(String name) {
        return COMMON_SCRIPTS.get(name);
    }
}
