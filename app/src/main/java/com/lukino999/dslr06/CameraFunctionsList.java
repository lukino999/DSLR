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
    HashMap<String, String> effect = new HashMap<>();
    HashMap<String, String> selectableZoneAf = new HashMap<>();
    HashMap<String, String> autoExposureLock = new HashMap<>();
    HashMap<String, String> autoWhitebalanceLock = new HashMap<>();
    HashMap<String, String> denoise= new HashMap<>();
    HashMap<String, String> whitebalance = new HashMap<>();
    HashMap<String, String> antibanding = new HashMap<>();
    HashMap<String, String> pictureFormat = new HashMap<>();
    HashMap<String, String> saturation = new HashMap<>();
    HashMap<String, String> exposureCompensation = new HashMap<>();
    HashMap<String, String> aeBracketHDR = new HashMap<>();
    HashMap<String, String> sharpness = new HashMap<>();





    public CameraFunctionsList() {

        // focus
        focus.put(LABEL, "Focus");
        focus.put(VALUE, "focus-mode");
        focus.put(AVAILABLE_VALUES, "-values");
        availableFuntions.add(focus);

        // auto-exposure-lock
        autoExposureLock.put(LABEL, "Auto Exposure Lock");
        autoExposureLock.put(VALUE, "auto-exposure-lock");
        autoExposureLock.put(AVAILABLE_VALUES, "-supported");
        availableFuntions.add(autoExposureLock);

        // auto-whitebalance-lock
        autoWhitebalanceLock.put(LABEL, "Auto WhiteBalance Lock");
        autoWhitebalanceLock.put(VALUE, "auto-whitebalance-lock");
        autoWhitebalanceLock.put(AVAILABLE_VALUES, "-supported");
        availableFuntions.add(autoWhitebalanceLock);

        // flash
        flash.put(LABEL, "Flash");
        flash.put(VALUE, "flash-mode");
        flash.put(AVAILABLE_VALUES, "-values");
        availableFuntions.add(flash);

        // whitebalance
        whitebalance.put(LABEL, "Whitebalance");
        whitebalance.put(VALUE, "whitebalance");
        whitebalance.put(AVAILABLE_VALUES, "-values");
        availableFuntions.add(whitebalance);

        // ISO
        iso.put(LABEL, "ISO");
        iso.put(VALUE, "iso");
        iso.put(AVAILABLE_VALUES, "-values");
        availableFuntions.add(iso);

        // scene mode
        sceneMode.put(LABEL, "Scene mode");
        sceneMode.put(VALUE, "scene-mode");
        sceneMode.put(AVAILABLE_VALUES, "-values");
        availableFuntions.add(sceneMode);

        // effect
        effect.put(LABEL, "Effects");
        effect.put(VALUE, "effect");
        effect.put(AVAILABLE_VALUES, "-values");
        availableFuntions.add(effect);

        // denoise
        denoise.put(LABEL, "DeNoise");
        denoise.put(VALUE, "denoise");
        denoise.put(AVAILABLE_VALUES, "-values");
        availableFuntions.add(denoise);

        // picture-size
        pictureSize.put(LABEL, "Picture size");
        pictureSize.put(VALUE, "picture-size");
        pictureSize.put(AVAILABLE_VALUES, "-values");
        availableFuntions.add(pictureSize);


        // selectable-zone-af
        selectableZoneAf.put(LABEL, "AutoFocus mode");
        selectableZoneAf.put(VALUE, "selectable-zone-af");
        selectableZoneAf.put(AVAILABLE_VALUES, "-values");
        availableFuntions.add(selectableZoneAf);

        // antibanding
        antibanding.put(LABEL, "Antibanding");
        antibanding.put(VALUE, "antibanding");
        antibanding.put(AVAILABLE_VALUES, "-values");
        availableFuntions.add(antibanding);


        // picture-format
        pictureFormat.put(LABEL, "Picture format");
        pictureFormat.put(VALUE, "picture-format");
        pictureFormat.put(AVAILABLE_VALUES, "-values");
        //availableFuntions.add(pictureFormat); // doesnt work


        // saturation
        saturation.put(LABEL, "Saturation");
        saturation.put(VALUE, "saturation");
        saturation.put(AVAILABLE_VALUES, "max-");
        availableFuntions.add(saturation);


        // exposure-compensation
        exposureCompensation.put(LABEL, "Exposure");
        exposureCompensation.put(VALUE, "exposure-compensation");
        exposureCompensation.put(AVAILABLE_VALUES, "max-");
        availableFuntions.add(exposureCompensation);


        // ae-bracket-hdr
        aeBracketHDR.put(LABEL, "AE-Bracket HDR");
        aeBracketHDR.put(VALUE, "ae-bracket-hdr");
        aeBracketHDR.put(AVAILABLE_VALUES, "-values");
        availableFuntions.add(aeBracketHDR);


        // sharpness
        sharpness.put(LABEL, "Sharpness");
        sharpness.put(VALUE, "sharpness");
        sharpness.put(AVAILABLE_VALUES, "max-");
        availableFuntions.add(sharpness);


    }
}
