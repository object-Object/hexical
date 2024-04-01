package miyucomics.hexical.mixin;

import at.petrak.hexcasting.api.misc.MediaConstants;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import miyucomics.hexical.Hexical;
import miyucomics.hexical.registry.HexicalItems;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.WanderingTraderEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOfferList;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Objects;

@Mixin(WanderingTraderEntity.class)
public abstract class WanderingTraderEntityMixin extends MerchantEntity {
	@Shadow
	protected native void fillRecipes();

	@Shadow
	protected native void afterUsing(TradeOffer offer);

	public WanderingTraderEntityMixin(EntityType<? extends MerchantEntity> entityType, World world) {
		super(entityType, world);
	}

	@Inject(method = "fillRecipes", at = @At("RETURN"))
	public void addNewTrades(CallbackInfo info) {
		TradeOfferList tradeOfferList = getOffers();
		if (tradeOfferList == null)
			return;
		if (random.nextFloat() < 0.1f) {
			ItemStack trade = new ItemStack(HexicalItems.INSTANCE.getLAMP_ITEM());
			Objects.requireNonNull(IXplatAbstractions.INSTANCE.findHexHolder(trade)).writeHex(new ArrayList<>(), MediaConstants.DUST_UNIT * 320);
			Objects.requireNonNull(IXplatAbstractions.INSTANCE.findMediaHolder(trade)).withdrawMedia((int) (Hexical.RANDOM.nextFloat() * 160f) * MediaConstants.DUST_UNIT, false);
			tradeOfferList.add(new TradeOffer(new ItemStack(Items.EMERALD, 32), trade, 1, 1, 1));
		}
	}
}