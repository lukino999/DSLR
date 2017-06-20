package com.lukino999.dslr06;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Luca on 19/06/2017.
 */

public class CameraFunctionsList {

    final public String AVAILABLE_VALUES = "availableValues";
    final public String VALUE = "value";
    final public String LABEL = "label";

    public ArrayList<HashMap> availableFuntions = new ArrayList<>();

    HashMap<String, String> focus = new HashMap<>();
    HashMap<String, String> flash= new HashMap<>();
    HashMap<String, String> iso = new HashMap<>();
    HashMap<String, String> sceneMode = new HashMap<>();
    HashMap<String, String> pictureSize= new HashMap<>();
    HashMap<String, String> effectValues= new HashMap<>();
    HashMap<String, String> selectableZoneAfValues= new HashMap<>();


    public CameraFunctionsList() {

        // focus
        focus.put(LABEL, "Focus");
        focus.put(VALUE, "focus-mode");
        focus.put(AVAILABLE_VALUES, "focus-mode-values");
        availableFuntions.add(focus);

        // flash
        flash.put(LABEL, "Flash");
        flash.put(VALUE, "flash-mode");
        flash.put(AVAILABLE_VALUES, "flash-mode-values");
        availableFuntions.add(flash);

        // ISO
        iso.put(LABEL, "ISO");
        iso.put(VALUE, "iso");
        iso.put(AVAILABLE_VALUES, "iso-values");
        availableFuntions.add(iso);

        // scene mode
        sceneMode.put(LABEL, "Scene mode");
        sceneMode.put(VALUE, "scene-mode");
        sceneMode.put(AVAILABLE_VALUES, "scene-mode-values");
        availableFuntions.add(sceneMode);

        // picture-size
        pictureSize.put(LABEL, "Picture size");
        pictureSize.put(VALUE, "picture-size");
        pictureSize.put(AVAILABLE_VALUES, "picture-size-values");
        availableFuntions.add(pictureSize);

        effectValues.put(LABEL, "Effects");
        effectValues.put(VALUE, "effect");
        effectValues.put(AVAILABLE_VALUES, "effect-values");
        availableFuntions.add(effectValues);

        selectableZoneAfValues.put(LABEL, "AutoFocus mode");
        selectableZoneAfValues.put(VALUE, "selectable-zone-af");
        selectableZoneAfValues.put(AVAILABLE_VALUES, "selectable-zone-af-values");
        availableFuntions.add(selectableZoneAfValues);




    }
}
