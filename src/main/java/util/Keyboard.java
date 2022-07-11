package util;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class Keyboard {
    private static final Map<Integer, Boolean> pressedKeys = new HashMap<>();
    private static final Map<Integer, Boolean> keyValues = new HashMap<>();

    private static final Map<Integer, Consumer<KeyEvent>> typedActions = new HashMap<>();
    private static final DualMap<Integer, Integer, Consumer<KeyEvent>> typedActionsMods = new DualMap<>();

    static {
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(event -> {
            synchronized (Keyboard.class) {
                if (event.getID() == KeyEvent.KEY_PRESSED) pressedKeys.put(event.getKeyCode(), true);
                else if (event.getID() == KeyEvent.KEY_RELEASED) pressedKeys.put(event.getKeyCode(), false);

                for (Integer key : typedActions.keySet()) {
                    if (getKeyDown(key)) {
                        typedActions.get(key).accept(event);
                    }
                }

                for (int i = 0; i < typedActionsMods.size(); i++) {
                    int key1 = typedActionsMods.getKey1(i);
                    int key2 = typedActionsMods.getKey2(i);
                    if (getKeyPressed(key1) && getKeyDown(key2)) {
                        typedActionsMods.get(key1, key2).accept(event);
                    }
                }

                return false;
            }
        });
    }

    public static boolean getKeyPressed(int keyCode) {
        return pressedKeys.getOrDefault(keyCode, false);
    }

    public static boolean getKeyDown(int keyCode) {
        // if the stored value is false (not pressed) AND the current value is true (pressed), then we started pressing
        boolean value = !keyValues.getOrDefault(keyCode, false) && pressedKeys.getOrDefault(keyCode, false);
        keyValues.put(keyCode, pressedKeys.getOrDefault(keyCode, false));

        return value;
    }

    public static boolean getKeyUp(int keyCode) {
        // if the stored value is true (pressed) AND the current value is false (not pressed), then we stopped pressing
        boolean value = keyValues.getOrDefault(keyCode, false) && !pressedKeys.getOrDefault(keyCode, false);
        keyValues.put(keyCode, pressedKeys.getOrDefault(keyCode, false));

        return value;
    }

    public static void registerTypedAction(Integer keyCode, Consumer<KeyEvent> event) {
        typedActions.put(keyCode, event);
    }

    public static void registerTypedAction(Integer keyCode, Integer modifier, Consumer<KeyEvent> event) {
        typedActionsMods.put(keyCode, modifier, event);
    }
}

class DualMap<K1, K2, V> {
    private final Map<K1, V> map1 = new HashMap<>();
    private final Map<K2, V> map2 = new HashMap<>();

    public DualMap() {

    }

    public void put(K1 key1, K2 key2, V val) {
        map1.put(key1, val);
        map2.put(key2, val);
    }

    public void remove(K1 key1, K2 key2) {
        map1.remove(key1);
        map2.remove(key2);
    }

    public boolean containsKey(K1 key1, K2 key2) {
        return map1.containsKey(key1) && map2.containsKey(key2);
    }

    public boolean containsEitherKey(K1 key1, K2 key2) {
        return map1.containsKey(key1) || map2.containsKey(key2);
    }

    public V get(K1 key1, K2 key2) {
        if (containsKey(key1, key2)) {
            return map1.get(key1);
        }

        throw new NullPointerException();
    }

    public V getOrDefault(K1 key1, K2 key2, V def) {
        if (containsKey(key1, key2)) {
            return map1.getOrDefault(key1, def);
        }

        return def;
    }

    public K1 getKey1(int index) {
        return map1.keySet().stream().toList().get(index);
    }

    public K2 getKey2(int index) {
        return map2.keySet().stream().toList().get(index);
    }

    public int size() {
        return map1.size();
    }

    public Map<K1, V> getMap1() {
        return map1;
    }

    public Map<K2, V> getMap2() {
        return map2;
    }
}