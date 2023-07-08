package emu.grasscutter.scripts.serializer;

import com.esotericsoftware.reflectasm.ConstructorAccess;
import com.esotericsoftware.reflectasm.MethodAccess;
import emu.grasscutter.Grasscutter;
import emu.grasscutter.scripts.ScriptUtils;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import lombok.val;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LuaSerializer implements Serializer {

    private final static Map<Class<?>, MethodAccess> methodAccessCache = new ConcurrentHashMap<>();
    private final static Map<Class<?>, ConstructorAccess<?>> constructorCache = new ConcurrentHashMap<>();
    private final static Map<Class<?>, Map<String, FieldMeta>> fieldMetaCache = new ConcurrentHashMap<>();

    @Override
    public <T> List<T> toList(Class<T> type, Object obj) {
        return serializeList(type, (LuaTable) obj);
    }

    @Override
    public <T> T toObject(Class<T> type, Object obj) {
        return serialize(type, null, (LuaTable) obj);
    }

    @Override
    public <T> Map<String, T> toMap(Class<T> type, Object obj) {
        return serializeMap(type, (LuaTable) obj);
    }

    private <T> Map<String,T> serializeMap(Class<T> type, LuaTable table) {
        Map<String,T> map = new HashMap<>();

        if (table == null) return map;

        try {
            Stream.of(table.keys()).forEach(k -> {
                try {
                    LuaValue keyValue = table.get(k);
                    T object = switch (keyValue.type()) {
                        case LuaValue.TTABLE -> serialize(type, null, keyValue.checktable());
                        case LuaValue.TSTRING -> type.cast(keyValue.tojstring());
                        case LuaValue.TNUMBER -> keyValue.isint() ? type.cast(keyValue.toint()) : type.cast(keyValue.tofloat());  // terrible...
                        case LuaValue.TBOOLEAN -> type.cast(keyValue.toboolean());
                        default -> type.cast(keyValue);
                    };

                    if (object != null) map.put(String.valueOf(k),object);
                } catch (Exception ignored) {}
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        return map;
    }

    public <T> List<T> serializeList(Class<T> type, LuaTable table) {
        return (table == null) ? List.of() : Stream.of(table.keys()).map(table::get)
            .map(keyValue -> {
                try {
                    return switch (keyValue.type()) {
                        case LuaValue.TTABLE -> serialize(type, null, keyValue.checktable());
                        case LuaValue.TSTRING -> type.cast(keyValue.tojstring());
                        case LuaValue.TNUMBER ->
                            keyValue.isint() ? type.cast(keyValue.toint()) : type.cast(keyValue.tofloat());  // terrible...
                        case LuaValue.TBOOLEAN -> type.cast(keyValue.toboolean());
                        default -> type.cast(keyValue);
                    };
                } catch (Exception ignored){}
                return null;
            })
            .filter(Objects::nonNull)
            .toList();
    }

    private Class<?> getListType(Class<?> type, @Nullable Field field){
        return field == null ? type.getTypeParameters()[0].getClass() :
            !(field.getGenericType() instanceof ParameterizedType fieldType) ? null :
            (Class<?>) fieldType.getActualTypeArguments()[0];
    }

    public <T> T serialize(Class<T> type, @Nullable Field field, LuaTable table) {
        if (type == List.class) {
            try {
                return type.cast(serializeList(getListType(type, field), table));
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        T object = null;
        try {
            if (!methodAccessCache.containsKey(type)) cacheType(type);

            MethodAccess methodAccess = methodAccessCache.get(type);
            val fieldMetaMap = fieldMetaCache.get(type);

            object = type.cast(constructorCache.get(type).newInstance());

            if (table == null) return object;

            for (LuaValue k : table.keys()) {
                try {
                    String keyName = k.checkjstring();
                    if (!fieldMetaMap.containsKey(keyName)) continue;

                    FieldMeta fieldMeta = fieldMetaMap.get(keyName);
                    LuaValue keyValue = table.get(k);

                    methodAccess.invoke(object, fieldMeta.index,
                        switch (fieldMeta.getType().getSimpleName()) {
                            case "float" -> keyValue.tofloat();
                            case "int" -> keyValue.toint();
                            case "boolean" -> keyValue.toboolean();
                            case "String" -> keyValue.tojstring();
                            default -> serialize(fieldMeta.getType(), fieldMeta.getField(), keyValue.checktable());
                        });
                } catch (Exception ignored) {}
            }
        } catch (Exception e) {
            Grasscutter.getLogger().info(ScriptUtils.toMap(table).toString());
//            e.printStackTrace();
        }

        return object;
    }

    public <T> Map<String, FieldMeta> cacheType(Class<T> type) {
        if (fieldMetaCache.containsKey(type)) return fieldMetaCache.get(type);

        constructorCache.putIfAbsent(type, ConstructorAccess.get(type));
        MethodAccess methodAccess = Optional.ofNullable(methodAccessCache.get(type)).orElse(MethodAccess.get(type));
        methodAccessCache.putIfAbsent(type, methodAccess);

        val fieldMetaMap = new HashMap<String, FieldMeta>();
        val methodNameSet = Stream.of(methodAccess.getMethodNames()).collect(Collectors.toSet());

        Arrays.stream(type.getDeclaredFields())
            .filter(field -> methodNameSet.contains(getSetterName(field.getName())))
            .forEach(field -> {
                String setter = getSetterName(field.getName());
                fieldMetaMap.put(field.getName(),
                    new FieldMeta(field.getName(), setter, methodAccess.getIndex(setter), field.getType(), field));
            });

        Arrays.stream(type.getFields())
            .filter(field -> !fieldMetaMap.containsKey(field.getName()))
            .filter(field -> methodNameSet.contains(getSetterName(field.getName())))
            .forEach(field -> {
                String setter = getSetterName(field.getName());
                fieldMetaMap.put(field.getName(),
                    new FieldMeta(field.getName(), setter, methodAccess.getIndex(setter), field.getType(), field));
            });

        fieldMetaCache.put(type, fieldMetaMap);
        return fieldMetaMap;
    }

    public String getSetterName(String fieldName) {
        return (fieldName == null || fieldName.length() == 0) ? null :
            fieldName.length() == 1 ? "set" + fieldName.toUpperCase() :
            "set" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
    }

    @Data
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    static class FieldMeta{
        String name;
        String setter;
        int index;
        Class<?> type;
        @Nullable Field field;
    }
}
