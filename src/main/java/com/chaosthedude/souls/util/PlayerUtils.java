package com.chaosthedude.souls.util;

import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;

public class PlayerUtils {

	public static String getPlayerNameFromUUID(World world, UUID id) {
		final EntityPlayer player = world.getPlayerEntityByUUID(id);
		String name = "";
		if (player != null) {
			name = player.getName();
		}

		return name;
	}

	public static void playSoundAtPlayer(EntityPlayer player, SoundEvent sound) {
		player.world.playSound(null, player.posX, player.posY, player.posZ, sound, SoundCategory.AMBIENT, 1.0F,
				player.world.rand.nextFloat() * 0.1F + 0.9F);
	}

}
