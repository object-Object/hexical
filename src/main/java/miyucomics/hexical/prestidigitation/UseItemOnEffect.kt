package miyucomics.hexical.prestidigitation

import miyucomics.hexical.interfaces.PrestidigitationEffect
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.passive.AnimalEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUsageContext
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d

class UseItemOnEffect(val stack: ItemStack) : PrestidigitationEffect {
	override fun effectBlock(caster: ServerPlayerEntity, position: BlockPos) {
		val originalStack = caster.getStackInHand(Hand.OFF_HAND)
		caster.setStackInHand(Hand.OFF_HAND, stack)
		caster.world.getBlockState(position).onUse(caster.world, caster, Hand.OFF_HAND, BlockHitResult(Vec3d.of(position), Direction.DOWN, position, false))
		stack.item.useOnBlock(ItemUsageContext(caster, Hand.OFF_HAND, BlockHitResult(Vec3d.ofCenter(position), Direction.UP, position, true)))
		caster.setStackInHand(Hand.OFF_HAND, originalStack)
	}

	override fun effectEntity(caster: ServerPlayerEntity, entity: Entity) {
		val originalStack = caster.getStackInHand(Hand.OFF_HAND)
		caster.setStackInHand(Hand.OFF_HAND, stack)
		entity.interact(caster, Hand.OFF_HAND)
		if (entity is LivingEntity)
			stack.item.useOnEntity(stack, caster, entity, Hand.OFF_HAND)
		if (entity is AnimalEntity)
			entity.interactMob(caster, Hand.OFF_HAND)
		caster.setStackInHand(Hand.OFF_HAND, originalStack)
	}
}