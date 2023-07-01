package emu.grasscutter.scripts;

import emu.grasscutter.Grasscutter;
import emu.grasscutter.scripts.data.SceneGroup;
import emu.grasscutter.scripts.data.common.BaseCommonScript;
import emu.grasscutter.scripts.data.common.CommonScriptType;
import lombok.val;
import org.reflections.Reflections;

import javax.script.Bindings;
import javax.script.CompiledScript;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static emu.grasscutter.utils.FileUtils.getScriptPath;

public class CommonScriptManager {
    /**
     * Holds all Common scripts,
     * [Key: lua file name without extension (including subfolder name if any) i.e. : V2_0/BlossomGroup,
     * value: class instance]
     * */
    private static final Map<String, BaseCommonScript> COMMON_SCRIPTS = new ConcurrentHashMap<>();
    /**
     * Holds all Common scripts [Key: lua file name for mapping, value: class type]
     * */
    private static final Map<String, Class<? extends BaseCommonScript>> COMMON_SCRIPT_FILE_NAME_TO_CLASS = new ConcurrentHashMap<>();
    private static final String SCRIPT_TOP_FOLDER = "Common/";

    /**
     * Loads when server starts
     * */
    public static void load(){
        cacheCommonScripts();
    }

    /**
     * Process common lua scripts
     * */
    private static void processFile(String subdirNFileName) {
        val commonScript = COMMON_SCRIPT_FILE_NAME_TO_CLASS.get(subdirNFileName);
        if(!subdirNFileName.endsWith(".lua") || commonScript == null) return;

        CompiledScript cs = ScriptLoader.getScript(SCRIPT_TOP_FOLDER + "/" + subdirNFileName);
        Bindings bindings = ScriptLoader.getEngine().createBindings();
        if (cs == null) return;

        try{
            cs.eval(bindings);
            val newInstance = commonScript.getDeclaredConstructor(
                CompiledScript.class, Bindings.class).newInstance(cs, bindings);
            COMMON_SCRIPTS.put(subdirNFileName.replace(".lua", ""), newInstance);
        } catch (Throwable e){
            Grasscutter.getLogger().error("Error while loading common script: {}", subdirNFileName);
        }
    }

    /**
     * Recursively process common scripts folder and sub folders
     * */
    private static void processFolder(String folderPath, String subfolderPath) throws IOException{
        File folder = new File(folderPath);
        if (!folder.isDirectory()) return;

        Files.newDirectoryStream(folder.toPath()).forEach(subPath -> {
            try {
                String newSubfolderPath = subfolderPath + subPath.getFileName();
                if (Files.isDirectory(subPath)) {
                    processFolder(subPath.toString(), newSubfolderPath + "/"); // Recursively process sub folders
                } else {
                    processFile(newSubfolderPath); // Process individual file
                }
            } catch (Exception ignored) {

            }
        });
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

        try{ // loads all files
            processFolder(getScriptPath(SCRIPT_TOP_FOLDER).toString(), "");
            Grasscutter.getLogger().info("Loaded {} common scripts", COMMON_SCRIPTS.size());
        } catch (Exception ignored) {
            Grasscutter.getLogger().error("Error loading common scripts luas");
        }
    }

    public static BaseCommonScript getCommonScripts(String name) {
        return COMMON_SCRIPTS.get(name);
    }

    /**
     * Adds all triggers and bindings of common scripts to the group
     * */
    public static void rebuildTriggersAndBindings(String name, SceneGroup group){
        if (name == null || name.isBlank()) return;

        BaseCommonScript targetScript = getCommonScripts(name);
        if (targetScript == null) return;

        targetScript.rebuildTriggersAndBindings(group);
    }
}
