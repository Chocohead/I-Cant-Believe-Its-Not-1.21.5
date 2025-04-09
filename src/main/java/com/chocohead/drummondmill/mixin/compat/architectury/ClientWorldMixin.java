package com.chocohead.drummondmill.mixin.compat.architectury;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.aprilfools.WorldEffect;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.client.world.ClientWorld.Properties;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;

import com.chocohead.drummondmill.InterceptingMixin;
import com.chocohead.drummondmill.PlacatingSurrogate;
import com.chocohead.drummondmill.Shim;

@Mixin(ClientWorld.class)
@InterceptingMixin(value = "dev/architectury/mixin/fabric/client/MixinClientLevel", from = "architectury")
abstract class ClientWorldMixin {
	@PlacatingSurrogate
    private void construct(ClientPlayNetworkHandler networkHandler, Properties properties, RegistryKey<World> key, RegistryEntry<DimensionType> type, int loadDistance,
    		int simulationDistance, WorldRenderer renderer, boolean debugWorld, long seed, int seaLevel, List<WorldEffect> list, List<WorldEffect> list2, CallbackInfo call) {
		construct(networkHandler, properties, key, type, loadDistance, simulationDistance, renderer, debugWorld, seed, seaLevel, call);
    }

	@Shim
	public native void construct(ClientPlayNetworkHandler networkHandler, Properties properties, RegistryKey<World> key, RegistryEntry<DimensionType> type,
			int loadDistance, int simulationDistance, WorldRenderer renderer, boolean debugWorld, long seed, int seaLevel, CallbackInfo call);
}