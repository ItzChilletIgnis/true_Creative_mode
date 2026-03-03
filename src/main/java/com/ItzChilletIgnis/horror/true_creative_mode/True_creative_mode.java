package com.ItzChilletIgnis.horror.true_creative_mode;

import com.ItzChilletIgnis.horror.true_creative_mode.command.DebugCommand;
import com.ItzChilletIgnis.horror.true_creative_mode.event.AnimalKillHandler;
import com.ItzChilletIgnis.horror.true_creative_mode.event.ArsonHandler;
import com.ItzChilletIgnis.horror.true_creative_mode.event.BlockBreakHandler;
import com.ItzChilletIgnis.horror.true_creative_mode.event.SpawningHandler;
import com.ItzChilletIgnis.horror.true_creative_mode.item.ModItems;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class True_creative_mode implements ModInitializer {
    public static final String MOD_ID = "true_creative_mode";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final Identifier PACKET_SYNC_ASHFALL_STATE = new Identifier(MOD_ID, "sync_ashfall_state");

    @Override
    public void onInitialize() {
        ModItems.registerModItems();
        BlockBreakHandler.register();
        AnimalKillHandler.register();
        SpawningHandler.register();
        ArsonHandler.register();
        DebugCommand.register();
    }
}