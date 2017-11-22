package com.chaosthedude.souls.items;

import java.util.List;

import com.chaosthedude.souls.Souls;
import com.chaosthedude.souls.client.SoulsSounds;
import com.chaosthedude.souls.entity.EntitySoul;
import com.chaosthedude.souls.util.ItemUtils;
import com.chaosthedude.souls.util.PlayerUtils;
import com.chaosthedude.souls.util.StringUtils;
import com.chaosthedude.souls.util.Strings;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class ItemSoulIdentifier extends Item {

	public static final String NAME = "soul_identifier";

	public ItemSoulIdentifier() {
		setUnlocalizedName(Souls.MODID + "." + NAME);
		setMaxStackSize(1);
		setCreativeTab(CreativeTabs.TOOLS);
	}

	@Override
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		if (StringUtils.holdShiftForInfo(tooltip)) {
			ItemUtils.addItemDesc(tooltip, Strings.SOUL_IDENTIFIER);
		}
	}

	public static void displaySoulInfo(EntityPlayer player, EntitySoul soul, ItemSoulIdentifier soulIdentifier) {
		if (soul == null || player == null || player.isSneaking()) {
			return;
		}

		if (soulIdentifier != null) {
			PlayerUtils.playSoundAtPlayer(player, SoulsSounds.IDENTIFIER);
		}

		player.sendMessage(parseSoulInfo(soul));
	}

	protected static TextComponentTranslation parseSoulInfo(EntitySoul soul) {
		final TextComponentTranslation soulInfo = new TextComponentTranslation(Strings.SOUL_INFO, soul.getPlayerName(),
				StringUtils.parseDate(soul.getDateCreatedMillis()), soul.getNumItemsHeld());
		soulInfo.setStyle(new Style().setItalic(true).setColor(TextFormatting.GRAY));
		return soulInfo;
	}

}
