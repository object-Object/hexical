package miyucomics.hexical.utils

import at.petrak.hexcasting.api.misc.FrozenColorizer
import at.petrak.hexcasting.api.spell.math.HexPattern
import at.petrak.hexcasting.client.rotate
import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.render.GameRenderer
import net.minecraft.client.render.Tessellator
import net.minecraft.client.render.VertexFormat
import net.minecraft.client.render.VertexFormats
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Matrix4f
import net.minecraft.util.math.Vec2f
import net.minecraft.util.math.Vec3d
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.ceil
import kotlin.math.max

object RenderUtils {
	const val CIRCLE_RESOLUTION: Int = 20

	fun getNormalizedStrokes(pattern: HexPattern): List<Vec2f> {
		val lines = pattern.toLines(1f, pattern.getCenter(1f).negate()).toMutableList()
		val scaling = max(
			lines.maxBy { vector -> vector.x }.x - lines.minBy { vector -> vector.x }.x,
			lines.maxBy { vector -> vector.y }.y - lines.minBy { vector -> vector.y }.y
		)
		for (i in lines.indices)
			lines[i] = Vec2f(lines[i].x, -lines[i].y).multiply(1 / scaling)
		return lines.toList()
	}

	// this rendering function draws some points similar to a sentinel: it will always be the same width regardless of distance
	fun sentinelLike(matrices: MatrixStack, points: List<Vec3d>, width: Float, colorizer: FrozenColorizer) {
		if (points.size <= 1) return
		val tessellator = Tessellator.getInstance()
		val buf = tessellator.buffer
		RenderSystem.setShader(GameRenderer::getRenderTypeLinesShader)
		RenderSystem.lineWidth(width)
		val matrix = matrices.peek().positionMatrix
		fun vertex(a: Vec3d, b: Vec3d) {
			val aColor = colorizer.getColor(0f, a)
			val bColor = colorizer.getColor(0f, b)
			val normal = a.subtract(b).normalize()
			buf.vertex(matrix, a.x.toFloat(), a.y.toFloat(), a.z.toFloat())
				.color(aColor)
				.normal(matrices.peek().normalMatrix, normal.x.toFloat(), normal.y.toFloat(), normal.z.toFloat())
				.next()
			buf.vertex(matrix, b.x.toFloat(), b.y.toFloat(), b.z.toFloat())
				.color(bColor)
				.normal(matrices.peek().normalMatrix, -normal.x.toFloat(), -normal.y.toFloat(), -normal.z.toFloat())
				.next()
		}
		buf.begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES)
		for (i in 1 until points.size) {
			val a = points[i - 1]
			val b = points[i]
			vertex(Vec3d(a.x, a.y, a.z), Vec3d(b.x, b.y, b.z))
		}
		tessellator.draw()
	}

	fun drawFigure(mat: Matrix4f, points: List<Vec2f>, width: Float, colorizer: FrozenColorizer, colorizerOffset: Vec3d) {
		val pointCount = points.size
		if (pointCount <= 1)
			return

		val tessellator = Tessellator.getInstance()
		val buf = tessellator.buffer
		val joinAngles = FloatArray(pointCount)
		for (i in 2 until pointCount) {
			val currentPoint = points[i - 1]
			val offsetFromLast = currentPoint.add(points[i - 2].negate())
			val offsetToNext = points[i].add(currentPoint.negate())
			joinAngles[i - 1] = atan2(offsetFromLast.x * offsetToNext.y - offsetFromLast.y * offsetToNext.x, offsetFromLast.x * offsetToNext.x + offsetFromLast.y * offsetToNext.y)
		}

		fun vertex(pos: Vec2f) = buf.vertex(mat, pos.x, pos.y, 0f)
			.color(colorizer.getColor(0f, Vec3d(pos.x.toDouble(), pos.y.toDouble(), 0.0).multiply(2.0).add(colorizerOffset)))
			.next()

		buf.begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR)
		for (i in 0 until pointCount - 1) {
			val currentPoint = points[i]
			val nextPoint = points[i + 1]

			val sideLength = nextPoint.add(currentPoint.negate()).normalize().multiply(width * 0.5f)
			val normal = Vec2f(-sideLength.y, sideLength.x)

			val currentDown = currentPoint.add(normal)
			val currentUp = currentPoint.add(normal.negate())
			val nextDown = nextPoint.add(normal)
			val nextUp = nextPoint.add(normal.negate())

			vertex(currentDown)
			vertex(currentUp)
			vertex(nextUp)

			vertex(currentDown)
			vertex(nextUp)
			vertex(nextDown)

			if (i > 0) {
				val angle = joinAngles[i]
				val joinSteps = ceil(abs(angle) / (2 * MathHelper.PI) * CIRCLE_RESOLUTION).toInt()
				if (joinSteps < 1)
					continue

				if (angle < 0) {
					var previous = currentPoint.add(normal)
					for (j in 1..joinSteps) {
						val fan = rotate(normal, -angle * (j.toFloat() / joinSteps))
						val fanShift = currentPoint.add(fan)

						vertex(currentPoint)
						vertex(fanShift)
						vertex(previous)
						previous = fanShift
					}
				} else if (angle > 0) {
					val reversedNormal = normal.negate()
					var previous = currentPoint.add(reversedNormal)
					for (j in 1..joinSteps) {
						val fan = rotate(reversedNormal, -angle * (j.toFloat() / joinSteps))
						val fanShift = currentPoint.add(fan)

						vertex(currentPoint)
						vertex(previous)
						vertex(fanShift)
						previous = fanShift
					}
				}
			}
		}
		tessellator.draw()

		fun drawCaps(currentPoint: Vec2f, previousPoint: Vec2f) {
			val sideLength = currentPoint.add(previousPoint.negate()).normalize().multiply(0.5f * width)
			val normal = Vec2f(-sideLength.y, sideLength.x)
			val joinSteps = CIRCLE_RESOLUTION / 2
			buf.begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR)
			vertex(currentPoint)
			for (j in joinSteps downTo 0)
				vertex(currentPoint.add(rotate(normal, -MathHelper.PI * (j.toFloat() / joinSteps))))
			tessellator.draw()
		}
		drawCaps(points[0], points[1])
		drawCaps(points[pointCount - 1], points[pointCount - 2])
	}
}