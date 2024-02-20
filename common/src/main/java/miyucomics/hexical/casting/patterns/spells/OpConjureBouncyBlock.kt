package miyucomics.hexical.casting.patterns.spells

import at.petrak.hexcasting.api.misc.MediaConstants
import at.petrak.hexcasting.api.spell.ParticleSpray
import at.petrak.hexcasting.api.spell.RenderedSpell
import at.petrak.hexcasting.api.spell.SpellAction
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.getBlockPos
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.mishaps.MishapBadBlock
import at.petrak.hexcasting.common.blocks.BlockConjured
import at.petrak.hexcasting.xplat.IXplatAbstractions
import miyucomics.hexical.blocks.ConjuredBouncyBlock
import miyucomics.hexical.registry.HexicalBlocks
import net.minecraft.item.ItemPlacementContext
import net.minecraft.item.ItemStack
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World

class OpConjureBouncyBlock : SpellAction {
	override val argc = 1

	override fun execute(args: List<Iota>, ctx: CastingContext): Triple<RenderedSpell, Int, List<ParticleSpray>>? {
		val pos = args.getBlockPos(0, argc)
		ctx.assertVecInRange(pos)
		return Triple(Spell(pos), MediaConstants.DUST_UNIT, listOf(ParticleSpray.cloud(Vec3d.ofCenter(pos), 1.0)))
	}

	private data class Spell(val pos: BlockPos) : RenderedSpell {
		override fun cast(ctx: CastingContext) {
			if (!ctx.canEditBlockAt(pos))
				return
			ctx.world.setBlockState(pos, HexicalBlocks.CONJURED_BOUNCY_BLOCK.defaultState, 5)
			ConjuredBouncyBlock.setColor(ctx.world, pos, IXplatAbstractions.INSTANCE.getColorizer(ctx.caster))
		}
	}
}