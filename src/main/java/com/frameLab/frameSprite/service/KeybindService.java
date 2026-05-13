package com.frameLab.frameSprite.service;


import com.frameLab.frameSprite.utils.Actions;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

import java.util.EnumMap;

public class KeybindService {

    private static KeybindService instance;
    private final EnumMap<Actions,KeyCombination> defaultBinds = new EnumMap<>(Actions.class);

    private KeybindService(){
        defaultBinds.put(Actions.SAVE, new KeyCodeCombination(KeyCode.S,KeyCombination.CONTROL_DOWN));
        defaultBinds.put(Actions.EXPORT, new KeyCodeCombination(KeyCode.E,KeyCombination.CONTROL_DOWN));
        defaultBinds.put(Actions.TOOL_ERASER, new KeyCodeCombination(KeyCode.E));
        defaultBinds.put(Actions.UNDO, new KeyCodeCombination(KeyCode.Z,KeyCombination.CONTROL_DOWN));
        defaultBinds.put(Actions.REDO, new KeyCodeCombination(KeyCode.Z,KeyCombination.CONTROL_DOWN,KeyCombination.SHIFT_DOWN));
        defaultBinds.put(Actions.TOOL_BRUSH, new KeyCodeCombination(KeyCode.B));
        defaultBinds.put(Actions.EMPTY_LAYER, new KeyCodeCombination(KeyCode.L,KeyCombination.CONTROL_DOWN));

    }

    public static KeybindService getInstance()  {
        if (instance == null) {
            instance = new KeybindService();
        }
        return instance;
    }
}
