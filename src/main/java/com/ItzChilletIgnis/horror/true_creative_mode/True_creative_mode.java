package com.ItzChilletIgnis.horror.true_creative_mode;

import com.ItzChilletIgnis.horror.true_creative_mode.command.DebugCommand;
import com.ItzChilletIgnis.horror.true_creative_mode.event.AnimalKillHandler;
import com.ItzChilletIgnis.horror.true_creative_mode.event.ArsonHandler;
import com.ItzChilletIgnis.horror.true_creative_mode.event.BlockBreakHandler;
import com.ItzChilletIgnis.horror.true_creative_mode.event.ClearedSkyHandler;
import com.ItzChilletIgnis.horror.true_creative_mode.event.SpawningHandler;
import com.ItzChilletIgnis.horror.true_creative_mode.item.ModItems;
import com.ItzChilletIgnis.horror.true_creative_mode.network.SyncAshfallStatePayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class True_creative_mode implements ModInitializer {
    public static final String MOD_ID = "true_creative_mode";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final Identifier PACKET_SYNC_ASHFALL_STATE = Identifier.of(MOD_ID, "sync_ashfall_state");

    @Override
    public void onInitialize() {
        ModItems.registerModItems();
        BlockBreakHandler.register();
        AnimalKillHandler.register();
        SpawningHandler.register();
        ArsonHandler.register();
        ClearedSkyHandler.register();
        DebugCommand.register();
        
        // 注册 Payload
        PayloadTypeRegistry.playS2C().register(SyncAshfallStatePayload.ID, SyncAshfallStatePayload.CODEC);
    }
}