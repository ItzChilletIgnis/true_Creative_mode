package com.ItzChilletIgnis.horror.true_creative_mode;

import com.ItzChilletIgnis.horror.true_creative_mode.event.BlockBreakHandler;
import com.ItzChilletIgnis.horror.true_creative_mode.item.ModItems;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class True_creative_mode implements ModInitializer {
    public static final String MOD_ID = "true_creative_mode";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ModItems.registerModItems();
        BlockBreakHandler.register();
    }
}