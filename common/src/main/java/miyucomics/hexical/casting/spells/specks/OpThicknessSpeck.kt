package miyucomics.hexical.casting.spells.specks

import at.petrak.hexcasting.api.spell.*
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.mishaps.MishapBadEntity
import miyucomics.hexical.entities.SpeckEntity

class OpThicknessSpeck : SpellAction {
	override val argc = 2
	override fun execute(args: List<Iota>, ctx: CastingContext): Triple<RenderedSpell, Int, List<ParticleSpray>> {
		val entity = args.getEntity(0, argc)
		ctx.assertEntityInRange(entity)
		val thickness = args.getPositiveDoubleUnder(1, 5.0, argc)
		if (entity !is SpeckEntity)
			throw MishapBadEntity.of(entity, "speck")
		return Triple(Spell(entity, thickness), 0, listOf())
	}

	private data class Spell(val speck: SpeckEntity, val thickness: Double) : RenderedSpell {
		override fun cast(ctx: CastingContext) = speck.setThickness(thickness.toFloat())
	}
}