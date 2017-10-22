package com.chaosthedude.souls.items;

import java.util.List;

import com.chaosthedude.souls.Souls;
import com.chaosthedude.souls.SoulsItems;
import com.chaosthedude.souls.client.SoulsSounds;
import com.chaosthedude.souls.config.ConfigHandler;
import com.chaosthedude.souls.entity.EntitySoul;
import com.chaosthedude.souls.util.ItemUtils;
import com.chaosthedude.souls.util.PlayerUtils;
import com.chaosthedude.souls.util.StringUtils;
import com.chaosthedude.souls.util.Strings;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class ItemPickpocketGauntlet extends Item {

	public String name;

	private int successRate;
	private int maxCharges;

	public ItemPickpocketGauntlet(int maxCharges, double successRate, String name) {
		this.name = name;

		setUnlocalizedName(Souls.MODID + "." + name);
		setMaxStackSize(1);
		setNoRepair();
		setMaxCharges(maxCharges);
		setSuccessRate(successRate);
		setCreativeTab(CreativeTabs.TOOLS);
	}

	@Override
	public EnumRarity getRarity(ItemStack stack) {
		if (stack.getItem() == SoulsItems.CREATIVE_PICKPOCKET_GAUNTLET) {
			return EnumRarity.EPIC;
		}

		return EnumRarity.RARE;
	}

	@Override
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		tooltip.add(TextFormatting.GRAY.toString() + StringUtils.localize(Strings.CHARGES) + ": " + getRarity(stack).rarityColor.toString() + getChargesAsString(stack));
		if (StringUtils.holdShiftForInfo(tooltip)) {
			if (stack.getItem() == SoulsItems.PICKPOCKET_GAUNTLET) {
				ItemUtils.addItemDesc(tooltip, Strings.PICKPOCKET_GAUNTLET, MathHelper.floor(ConfigHandler.pickpocketSuccessRate) + "%");
			} else if (stack.getItem() == SoulsItems.CREATIVE_PICKPOCKET_GAUNTLET) {
				ItemUtils.addItemDesc(tooltip, Strings.CREATIVE_PICKPOCKET_GAUNTLET, "100%");
			}
		}
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		return recharge(player, player.getHeldItem(hand));
	}

	public void pickpocket(EntityPlayer player, ItemStack stack, EntitySoul soul) {
		if (player == null || player.isSneaking() || isOnCooldown(player) || soul == null || soul.items.isEmpty() || !(stack.getItem() instanceof ItemPickpocketGauntlet) || !soul.canInteract(player)) {
			return;
		}

		final ItemPickpocketGauntlet pickpocketGauntlet = (ItemPickpocketGauntlet) stack.getItem();
		if (!pickpocketGauntlet.hasCharges(stack)) {
			return;
		}

		int totalItems = 0;
		for (int i = 0; i < soul.items.size(); i++) {
			if (!soul.items.get(i).isEmpty()) {
				totalItems++;
			}
		}

		int itemCount = 0;
		int itemToGet = player.world.rand.nextInt(totalItems * successRate);
		boolean success = false;
		if (itemToGet < soul.items.size()) {
			for (int i = 0; i < soul.items.size(); i++) {
				ItemStack soulStack = soul.items.get(i);
				if (!soulStack.isEmpty() && itemToGet == itemCount) {
					success = true;
					player.inventory.addItemStackToInventory(soulStack);
					soul.removeItemInSlot(i);
					PlayerUtils.playSoundAtPlayer(player, SoulsSounds.PICKPOCKET);
					break;
				}

				itemCount++;
			}
		}

		if (!success && ConfigHandler.soulsAggro && ConfigHandler.pickpocketGauntletAggros && !player.capabilities.isCreativeMode) {
			soul.setAttackTarget(player);
		}

		pickpocketGauntlet.useCharge(player, stack);
		player.getCooldownTracker().setCooldown(this, 20);
	}

	public EnumActionResult recharge(EntityPlayer player, ItemStack stack) {
		if (player == null || !player.isSneaking() || player.capabilities.isCreativeMode || stack.isEmpty() || stack.getItem() != SoulsItems.PICKPOCKET_GAUNTLET || getCharges(stack) == 16) {
			return EnumActionResult.PASS;
		}

		for (int i = 0; i < player.inventory.mainInventory.size(); i++) {
			if (getCharges(stack) == maxCharges) {
				return EnumActionResult.SUCCESS;
			}

			ItemStack invStack = player.inventory.mainInventory.get(i);
			if (!invStack.isEmpty() && invStack.getItem() == Items.ENDER_PEARL) {
				if (getEmptyCharges(stack) < invStack.getCount()) {
					player.inventory.decrStackSize(i, getEmptyCharges(stack));
					addCharges(getEmptyCharges(stack), stack);
				} else {
					addCharges(invStack.getCount(), stack);
					player.inventory.setInventorySlotContents(i, ItemStack.EMPTY);
				}
			}
		}

		return EnumActionResult.SUCCESS;
	}

	public void addCharges(int amount, ItemStack stack) {
		if (getCharges(stack) < maxCharges) {
			setDamage(stack, stack.getItemDamage() - amount);
		}
	}

	public void useCharge(EntityPlayer player, ItemStack stack) {
		if (stack.getItem() == SoulsItems.PICKPOCKET_GAUNTLET && hasCharges(stack) && !player.capabilities.isCreativeMode) {
			setDamage(stack, stack.getItemDamage() + 1);
		}
	}

	public int getCharges(ItemStack stack) {
		return maxCharges - stack.getItemDamage();
	}
	public String getChargesAsString(ItemStack stack) {
		String charges = "Infnite";
		if (getCharges(stack) < 9999) {
			charges = String.valueOf(getCharges(stack));
		}
		
		return charges;
	}

	public int getEmptyCharges(ItemStack stack) {
		return maxCharges - getCharges(stack);
	}

	public boolean hasCharges(ItemStack stack) {
		return getCharges(stack) > 0;
	}

	public boolean isOnCooldown(EntityPlayer player) {
		return player.getCooldownTracker().getCooldown(this, 0.0F) > 0.0F;
	}

	public ItemPickpocketGauntlet setMaxCharges(int amount) {
		maxCharges = amount;
		setMaxDamage(amount);
		return this;
	}

	public ItemPickpocketGauntlet setSuccessRate(double rate) {
		successRate = MathHelper.floor(100 / rate);
		return this;
	}

}
