package miyucomics.hexical.entities

import at.petrak.hexcasting.api.spell.math.HexPattern
import at.petrak.hexcasting.api.utils.putCompound
import at.petrak.hexcasting.xplat.IXplatAbstractions
import miyucomics.hexical.networking.SpawnLivingScrollPacket
import miyucomics.hexical.registry.HexicalEntities
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.entity.data.DataTracker
import net.minecraft.entity.data.TrackedData
import net.minecraft.entity.data.TrackedDataHandlerRegistry
import net.minecraft.entity.decoration.AbstractDecorationEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtList
import net.minecraft.network.Packet
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket
import net.minecraft.sound.SoundEvents
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.GameRules
import net.minecraft.world.World

class LivingScrollEntity(entityType: EntityType<LivingScrollEntity?>?, world: World?) : AbstractDecorationEntity(entityType, world) {
	companion object {
		private val renderDataTracker: TrackedData<NbtCompound> = DataTracker.registerData(SpeckEntity::class.java, TrackedDataHandlerRegistry.NBT_COMPOUND)
	}

	var patterns: MutableList<HexPattern> = mutableListOf()
	var stack: ItemStack = ItemStack.EMPTY
	var size: Int = 1

	constructor(world: World, position: BlockPos, dir: Direction, size: Int, stack: ItemStack, patterns: MutableList<HexPattern>) : this(HexicalEntities.LIVING_SCROLL_ENTITY, world) {
		this.attachmentPos = position
		this.patterns = patterns
		this.setFacing(dir)
		this.size = size
		this.stack = stack
		updateAttachmentPosition()
		updateRender()
	}

	override fun initDataTracker() {
		super.initDataTracker()
		this.dataTracker.startTracking(renderDataTracker, NbtCompound())
	}

	override fun tick() {
		if ((world.time % 20).toInt() == 0 && patterns.isNotEmpty())
			updateRender()
		super.tick()
	}

	private fun updateRender() {
		this.dataTracker.set(renderDataTracker, patterns[((world.time / 20).toInt() % patterns.size)].serializeToNBT())
	}

	fun readSpawnData(pos: BlockPos, dir: Direction, size: Int) {
		this.size = size
		this.setFacing(dir)
		this.setPos(pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble())
		this.updateAttachmentPosition()
	}

	override fun readCustomDataFromNbt(nbt: NbtCompound?) {
		setFacing(Direction.values()[nbt!!.getByte("direction").toInt()])
		this.stack = ItemStack.fromNbt(nbt.getCompound("stack"))
		this.size = nbt.getInt("size")
		updateAttachmentPosition()
		val data = nbt.get("patterns") as NbtList
		this.patterns = mutableListOf()
		for (pattern in data)
			this.patterns.add(HexPattern.fromNBT(pattern as NbtCompound))
		updateRender()
		super.readCustomDataFromNbt(nbt)
	}

	override fun writeCustomDataToNbt(nbt: NbtCompound?) {
		nbt!!.putByte("direction", facing.ordinal.toByte())
		nbt.putInt("size", this.size)
		val data = NbtList()
		for (pattern in patterns)
			data.add(pattern.serializeToNBT())
		nbt.put("patterns", data)
		val compound = NbtCompound()
		stack.writeNbt(nbt)
		nbt.putCompound("stack", compound)
		super.writeCustomDataToNbt(nbt)
	}

	override fun onPlace() = playSound(SoundEvents.ENTITY_PAINTING_PLACE, 1.0F, 1.0F)
	override fun onBreak(entity: Entity?) {
		this.playSound(SoundEvents.ENTITY_PAINTING_BREAK, 1.0f, 1.0f)
		if (this.world.gameRules.getBoolean(GameRules.DO_ENTITY_DROPS)) {
			if (entity is PlayerEntity && entity.abilities.creativeMode)
				return
			this.dropStack(this.stack)
		}
	}

	fun getRender(): HexPattern {
		return HexPattern.fromNBT(dataTracker.get(renderDataTracker))
	}

	override fun getWidthPixels() = 16 * this.size
	override fun getHeightPixels() = 16 * this.size
	override fun createSpawnPacket(): Packet<*> = IXplatAbstractions.INSTANCE.toVanillaClientboundPacket(SpawnLivingScrollPacket(EntitySpawnS2CPacket(this), this.blockPos, this.facing, this.size))
}