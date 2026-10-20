package dev.touchlab.app.feature.jelly

import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.hypot
import kotlin.math.min
import kotlin.math.sin

/**
 * Pure Kotlin jelly model.
 *
 * The outline is sampled at [POINT_COUNT] angles. Each sample owns a radial
 * displacement and velocity driven by a damped spring. A pressed finger pulls a
 * gaussian-weighted section of the outline inward; releasing the finger lets
 * the springs overshoot and settle. The center independently spring-follows the
 * drag target, so the body lags slightly behind the finger.
 *
 * Coordinates are pixels in the local canvas space. No Android types are used.
 */
class JellyPhysics(
    widthPx: Float = 0f,
    heightPx: Float = 0f,
    private val desiredRadiusPx: Float = 180f,
) {
    companion object {
        const val POINT_COUNT = 24
        const val MAX_STEP_SECONDS = 1f / 30f

        private const val CENTER_STIFFNESS = 210f
        private const val CENTER_DAMPING = 21f
        private const val CENTER_RELEASE_DAMPING = 12f
        private const val CENTER_MAX_SPEED = 4200f

        private const val OUTLINE_STIFFNESS = 150f
        private const val OUTLINE_DAMPING = 6.5f
        private const val OUTLINE_MAX_SPEED = 3200f
        private const val PRESS_DEPTH_RATIO = 0.16f
        private const val PRESS_SIGMA = 0.72f
        private const val VOLUME_BULGE = 0.22f
        private const val MAX_DISPLACEMENT_RATIO = 0.38f
        private const val CENTER_MARGIN_RATIO = 0.96f

        private val TWO_PI = (Math.PI * 2.0).toFloat()
        private val PI = Math.PI.toFloat()
    }

    var width: Float = 0f
        private set
    var height: Float = 0f
        private set

    var centerX: Float = 0f
        private set
    var centerY: Float = 0f
        private set
    var centerVx: Float = 0f
        private set
    var centerVy: Float = 0f
        private set

    var radius: Float = 1f
        private set

    var touchActive: Boolean = false
        private set
    var touchX: Float = 0f
        private set
    var touchY: Float = 0f
        private set

    private var grabOffsetX = 0f
    private var grabOffsetY = 0f
    private val displacement = FloatArray(POINT_COUNT)
    private val velocity = FloatArray(POINT_COUNT)

    init {
        resize(widthPx, heightPx)
    }

    fun resize(widthPx: Float, heightPx: Float) {
        val safeWidth = if (widthPx.isFinite() && widthPx > 0f) widthPx else 0f
        val safeHeight = if (heightPx.isFinite() && heightPx > 0f) heightPx else 0f
        val hadSize = width > 0f && height > 0f
        val oldWidth = width
        val oldHeight = height
        width = safeWidth
        height = safeHeight

        radius = if (safeWidth <= 0f || safeHeight <= 0f) {
            1f
        } else {
            min(desiredRadiusPx.coerceAtLeast(24f), min(safeWidth, safeHeight) * 0.34f)
                .coerceAtLeast(12f)
        }

        if (!hadSize) {
            centerX = safeWidth * 0.5f
            centerY = safeHeight * 0.5f
            centerVx = 0f
            centerVy = 0f
        } else if (safeWidth > 0f && safeHeight > 0f) {
            centerX = (centerX / oldWidth) * safeWidth
            centerY = (centerY / oldHeight) * safeHeight
            constrainCenter(keepVelocity = false)
        } else {
            touchActive = false
            displacement.fill(0f)
            velocity.fill(0f)
            centerVx = 0f
            centerVy = 0f
        }
    }

    fun reset() {
        touchActive = false
        centerX = width * 0.5f
        centerY = height * 0.5f
        centerVx = 0f
        centerVy = 0f
        displacement.fill(0f)
        velocity.fill(0f)
        grabOffsetX = 0f
        grabOffsetY = 0f
    }

    fun beginTouch(x: Float, y: Float) {
        if (width <= 0f || height <= 0f) return
        touchActive = true
        touchX = sanitize(x, centerX)
        touchY = sanitize(y, centerY)
        grabOffsetX = centerX - touchX
        grabOffsetY = centerY - touchY
    }

    fun moveTouch(x: Float, y: Float) {
        if (!touchActive) return
        touchX = sanitize(x, touchX)
        touchY = sanitize(y, touchY)
    }

    fun endTouch() {
        touchActive = false
    }

    fun cancelTouch() = endTouch()

    fun step(dt: Float) {
        if (!dt.isFinite() || dt <= 0f) return
        if (width <= 0f || height <= 0f) return
        val time = dt.coerceAtMost(MAX_STEP_SECONDS)

        if (touchActive) {
            val targetX = touchX + grabOffsetX
            val targetY = touchY + grabOffsetY
            centerVx += ((targetX - centerX) * CENTER_STIFFNESS - centerVx * CENTER_DAMPING) * time
            centerVy += ((targetY - centerY) * CENTER_STIFFNESS - centerVy * CENTER_DAMPING) * time
        } else {
            val decay = exp(-CENTER_RELEASE_DAMPING * time)
            centerVx *= decay
            centerVy *= decay
        }

        centerVx = centerVx.coerceIn(-CENTER_MAX_SPEED, CENTER_MAX_SPEED)
        centerVy = centerVy.coerceIn(-CENTER_MAX_SPEED, CENTER_MAX_SPEED)
        centerX += centerVx * time
        centerY += centerVy * time
        constrainCenter(keepVelocity = true)

        val touchAngle: Float
        val touchDistance: Float
        if (touchActive) {
            val dx = touchX - centerX
            val dy = touchY - centerY
            touchAngle = atan2(dy, dx)
            touchDistance = hypot(dx, dy)
        } else {
            touchAngle = 0f
            touchDistance = 0f
        }

        val depth = radius * PRESS_DEPTH_RATIO
        val uniformPress = touchActive && touchDistance < radius * 0.30f
        val sigmaSquared = PRESS_SIGMA * PRESS_SIGMA

        for (i in 0 until POINT_COUNT) {
            val target = if (!touchActive) {
                0f
            } else if (uniformPress) {
                -depth * 0.62f
            } else {
                val angle = i * TWO_PI / POINT_COUNT
                val delta = angularDistance(angle, touchAngle)
                val influence = exp(-(delta * delta) / (2f * sigmaSquared))
                -depth * influence + depth * VOLUME_BULGE * (1f - influence)
            }

            var v = velocity[i]
            v += (-OUTLINE_STIFFNESS * (displacement[i] - target) - OUTLINE_DAMPING * v) * time
            v = v.coerceIn(-OUTLINE_MAX_SPEED, OUTLINE_MAX_SPEED)
            var d = displacement[i] + v * time
            val limit = radius * MAX_DISPLACEMENT_RATIO
            if (d > limit) {
                d = limit
                if (v > 0f) v *= -0.15f
            } else if (d < -limit) {
                d = -limit
                if (v < 0f) v *= -0.15f
            }
            displacement[i] = d
            velocity[i] = v
        }

        if (centerX.isNaN() || centerY.isNaN() || !centerX.isFinite() || !centerY.isFinite()) {
            reset()
        }
    }

    fun displacementAt(index: Int): Float = displacement[index]

    fun velocityAt(index: Int): Float = velocity[index]

    fun maxAbsDisplacement(): Float {
        var max = 0f
        for (i in 0 until POINT_COUNT) {
            max = maxOf(max, abs(displacement[i]))
        }
        return max
    }

    fun maxAbsOutlineVelocity(): Float {
        var max = 0f
        for (i in 0 until POINT_COUNT) {
            max = maxOf(max, abs(velocity[i]))
        }
        return max
    }

    fun outlineX(index: Int): Float {
        val angle = index * TWO_PI / POINT_COUNT
        return centerX + (radius + displacement[index]) * cos(angle)
    }

    fun outlineY(index: Int): Float {
        val angle = index * TWO_PI / POINT_COUNT
        return centerY + (radius + displacement[index]) * sin(angle)
    }

    fun copyOutline(outX: FloatArray, outY: FloatArray) {
        for (i in 0 until POINT_COUNT) {
            val angle = i * TWO_PI / POINT_COUNT
            val r = radius + displacement[i]
            outX[i] = centerX + r * cos(angle)
            outY[i] = centerY + r * sin(angle)
        }
    }

    /** Index of the outline sample closest to the given screen angle. */
    fun indexForDirection(dx: Float, dy: Float): Int {
        val angle = atan2(dy, dx)
        val normalized = if (angle < 0f) angle + TWO_PI else angle
        return ((normalized / TWO_PI) * POINT_COUNT).toInt() % POINT_COUNT
    }

    private fun constrainCenter(keepVelocity: Boolean) {
        if (width <= 0f || height <= 0f) return
        val marginX = min(radius * CENTER_MARGIN_RATIO, width * 0.5f)
        val marginY = min(radius * CENTER_MARGIN_RATIO, height * 0.5f)
        val minX = marginX
        val maxX = (width - marginX).coerceAtLeast(minX)
        val minY = marginY
        val maxY = (height - marginY).coerceAtLeast(minY)

        if (centerX < minX) {
            centerX = minX
            if (keepVelocity && centerVx < 0f) centerVx *= -0.25f
        } else if (centerX > maxX) {
            centerX = maxX
            if (keepVelocity && centerVx > 0f) centerVx *= -0.25f
        }
        if (centerY < minY) {
            centerY = minY
            if (keepVelocity && centerVy < 0f) centerVy *= -0.25f
        } else if (centerY > maxY) {
            centerY = maxY
            if (keepVelocity && centerVy > 0f) centerVy *= -0.25f
        }
    }

    private fun sanitize(value: Float, fallback: Float): Float {
        return if (value.isFinite()) value else fallback
    }

    private fun angularDistance(a: Float, b: Float): Float {
        var delta = a - b
        while (delta > PI) delta -= TWO_PI
        while (delta < -PI) delta += TWO_PI
        return delta
    }
}
