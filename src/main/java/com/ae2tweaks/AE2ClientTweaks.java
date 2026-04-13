package com.ae2tweaks;

import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod("ae2clienttweaks")
public class AE2ClientTweaks {
    public static final String MOD_ID = "ae2clienttweaks";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public AE2ClientTweaks() {
        LOGGER.info("AE2 Client Tweaks loaded");
    }
}
