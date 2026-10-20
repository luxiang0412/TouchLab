package dev.touchlab.app.feature.marbles

import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.hypot
import kotlin.math.min
import kotlin.math.sin

/**
 * Two-dimensional marble tray. Pure Kotlin, pixel coordinates.
 *
 * Motion uses semi-implicit Euler with clamped dt and speed. Wall and ball
 * collisions use restitution < 1 so the system loses energy instead of gaining
 * it. Ball-ball handling separates position and velocity so overlap resolution
 * cannot inject extra energy. Only one ball can be dragged at a time.
 */
class MarblesPhysics(
    widthPx: Float = 0f,
    heightPx: Float = 0f,
) {
    companion object {
        const val MARBLE_COUNT = 6
        const val MAX_STEP_SECONDS = 1f / 30f
        const val MAX_SPEED = 2600f
        const val MAX_DRAG_SPEED = 2600f
        const val GRAVITY = 1500f

        private const val WALL_RESTITUTION = 0.82f
        private const val BALL_RESTITUTION = 0.90f
        private const val WALL_FRICTION = 0.992f
        private const val AIR_DAMPING = 0.34f
        private const val POSITION_CORRECTION = 0.8f
        private const val DRAG_HIT_SLOP = 1.28f
        private const val DRAG_VELOCITY_SMOOTHING = 0.65f
        private const val DRAG_VELOCITY_DECAY = 8f

        private val RADIUS_FRACTIONS = floatArrayOf(0.078f, 0.064f, 0.088f, 0.056f, 0.070f, 0.050f)
        private val PRESET_X = floatArrayOf(0.28f, 0.62f, 0.47f, 0.24f, 0.74f, 0.50f)
        private val PRESET_Y = floatArrayOf(0.30f, 0.26f, 0.55f, 0.72f, 0.68f, 0.84f)
    }

    class Marble(
        var x: Float = 0f,
        var y: Float = 0f,
        var vx: Float = 0f,
        var vy: Float = 0f,
        var radius: Float = 1f,
        val colorIndex: Int = 0,
    )

    var width: Float = 0f
        private set
    var height: Float = 0f
        private set

    var gravityEnabled: Boolean = true
        private set

    var draggedIndex: Int = -1
        private set

    private val marbles = ArrayList<Marble>(MARBLE_COUNT)
    private var touchX = 0f
    private var touchY = 0f
    private var grabOffsetX = 0f
    private var grabOffsetY = 0f
    private var lastPointerX = 0f
    private var lastPointerY = 0f
    private var dragVelocityX = 0f
    private var dragVelocityY = 0f
    private var pendingImpactStrength = 0f

    init {
        repeat(MARBLE_COUNT) { index ->
            marbles.add(Marble(colorIndex = index))
        }
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

        if (width <= 0f || height <= 0f) {
            draggedIndex = -1
            return
        }

        val base = min(width, height)
        for (i in 0 until MARBLE_COUNT) {
            marbles[i].radius = (RADIUS_FRACTIONS[i] * base).coerceAtLeast(8f)
        }

        if (hadSize) {
            for (m in marbles) {
                m.x = (m.x / oldWidth) * width
                m.y = (m.y / oldHeight) * height
                constrainInside(m)
            }
            draggedIndex = -1
        } else {
            reset()
        }
    }

    fun reset() {
        draggedIndex = -1
        dragVelocityX = 0f
        dragVelocityY = 0f
        pendingImpactStrength = 0f
        if (width <= 0f || height <= 0f) return
        val base = min(width, height)
        for (i in 0 until MARBLE_COUNT) {
            val m = marbles[i]
            m.radius = (RADIUS_FRACTIONS[i] * base).coerceAtLeast(8f)
            m.x = (PRESET_X[i] * width).coerceIn(m.radius, (width - m.radius).coerceAtLeast(m.radius))
            m.y = (PRESET_Y[i] * height).coerceIn(m.radius, (height - m.radius).coerceAtLeast(m.radius))
            m.vx = 0f
            m.vy = 0f
        }
    }

    fun setGravity(enabled: Boolean) {
        gravityEnabled = enabled
    }

    fun beginDrag(x: Float, y: Float): Boolean {
        if (width <= 0f || height <= 0f) return false
        var bestIndex = -1
        var bestDistance = Float.MAX_VALUE
        for (i in 0 until MARBLE_COUNT) {
            val m = marbles[i]
            val dx = x - m.x
            val dy = y - m.y
            val distance = hypot(dx, dy)
            if (distance <= m.radius * DRAG_HIT_SLOP && distance < bestDistance) {
                bestDistance = distance
                bestIndex = i
            }
        }
        if (bestIndex < 0) return false
        draggedIndex = bestIndex
        val m = marbles[bestIndex]
        touchX = sanitize(x, m.x)
        touchY = sanitize(y, m.y)
        grabOffsetX = m.x - touchX
        grabOffsetY = m.y - touchY
        lastPointerX = touchX
        lastPointerY = touchY
        dragVelocityX = 0f
        dragVelocityY = 0f
        return true
    }

    /**
     * Updates the drag target. [secondsSinceLastMove] comes from pointer event
     * timestamps, so a real flick speed can be measured without guessing.
     */
    fun dragTo(x: Float, y: Float, secondsSinceLastMove: Float = 0f) {
        if (draggedIndex < 0) return
        val safeX = sanitize(x, touchX)
        val safeY = sanitize(y, touchY)
        touchX = safeX
        touchY = safeY

        if (secondsSinceLastMove > 0.0005f && secondsSinceLastMove < 0.5f) {
            val instantX = ((safeX - lastPointerX) / secondsSinceLastMove)
                .coerceIn(-MAX_DRAG_SPEED, MAX_DRAG_SPEED)
            val instantY = ((safeY - lastPointerY) / secondsSinceLastMove)
                .coerceIn(-MAX_DRAG_SPEED, MAX_DRAG_SPEED)
            dragVelocityX = dragVelocityX * (1f - DRAG_VELOCITY_SMOOTHING) +
                instantX * DRAG_VELOCITY_SMOOTHING
            dragVelocityY = dragVelocityY * (1f - DRAG_VELOCITY_SMOOTHING) +
                instantY * DRAG_VELOCITY_SMOOTHING
            dragVelocityX = dragVelocityX.coerceIn(-MAX_DRAG_SPEED, MAX_DRAG_SPEED)
            dragVelocityY = dragVelocityY.coerceIn(-MAX_DRAG_SPEED, MAX_DRAG_SPEED)
        }
        lastPointerX = safeX
        lastPointerY = safeY
    }

    fun endDrag() {
        if (draggedIndex < 0) return
        val m = marbles[draggedIndex]
        m.vx = dragVelocityX.coerceIn(-MAX_DRAG_SPEED, MAX_DRAG_SPEED)
        m.vy = dragVelocityY.coerceIn(-MAX_DRAG_SPEED, MAX_DRAG_SPEED)
        draggedIndex = -1
    }

    fun cancelDrag() {
        if (draggedIndex < 0) return
        marbles[draggedIndex].vx = 0f
        marbles[draggedIndex].vy = 0f
        draggedIndex = -1
    }

    fun lastDragSpeed(): Float = hypot(dragVelocityX, dragVelocityY)

    /** Strongest collision speed observed since the last call, then cleared. */
    fun consumeImpact(): Float {
        val strength = pendingImpactStrength
        pendingImpactStrength = 0f
        return strength
    }

    fun marble(index: Int): Marble = marbles[index]

    fun step(dt: Float) {
        if (!dt.isFinite() || dt <= 0f) return
        if (width <= 0f || height <= 0f) {
            draggedIndex = -1
            return
        }
        val time = dt.coerceAtMost(MAX_STEP_SECONDS)

        if (draggedIndex >= 0) {
            val m = marbles[draggedIndex]
            val targetX = (touchX + grabOffsetX).coerceIn(m.radius, (width - m.radius).coerceAtLeast(m.radius))
            val targetY = (touchY + grabOffsetY).coerceIn(m.radius, (height - m.radius).coerceAtLeast(m.radius))
            m.x = targetX
            m.y = targetY
            m.vx = dragVelocityX
            m.vy = dragVelocityY
            val decay = exp(-DRAG_VELOCITY_DECAY * time)
            dragVelocityX *= decay
            dragVelocityY *= decay
        }

        for (i in 0 until MARBLE_COUNT) {
            if (i == draggedIndex) continue
            val m = marbles[i]
            if (gravityEnabled) {
                m.vy += GRAVITY * time
            }
            val decay = exp(-AIR_DAMPING * time)
            m.vx *= decay
            m.vy *= decay
            clampSpeed(m)
            m.x += m.vx * time
            m.y += m.vy * time
            collideWithWalls(m, dragged = false)
        }

        // Two passes help dense piles separate without visibly sinking.
        repeat(2) {
            for (i in 0 until MARBLE_COUNT) {
                for (j in i + 1 until MARBLE_COUNT) {
                    collideMarbles(marbles[i], marbles[j], i, j)
                }
            }
        }

        for (i in 0 until MARBLE_COUNT) {
            collideWithWalls(marbles[i], dragged = i == draggedIndex)
            clampSpeed(marbles[i])
            val m = marbles[i]
            if (!m.x.isFinite() || !m.y.isFinite() || !m.vx.isFinite() || !m.vy.isFinite()) {
                reset()
                return
            }
        }
    }

    fun maxSpeed(): Float = marbles.maxOfOrNull { hypot(it.vx, it.vy) } ?: 0f

    fun allPositionsFinite(): Boolean = marbles.all { it.x.isFinite() && it.y.isFinite() }

    fun allInsideBounds(): Boolean = marbles.all {
        it.x >= it.radius - 0.5f && it.x <= width - it.radius + 0.5f &&
            it.y >= it.radius - 0.5f && it.y <= height - it.radius + 0.5f
    }

    fun minimumSeparation(): Float {
        var minSeparation = Float.MAX_VALUE
        for (i in 0 until MARBLE_COUNT) {
            for (j in i + 1 until MARBLE_COUNT) {
                val a = marbles[i]
                val b = marbles[j]
                minSeparation = min(minSeparation, hypot(b.x - a.x, b.y - a.y) - a.radius - b.radius)
            }
        }
        return minSeparation
    }

    private fun collideMarbles(a: Marble, b: Marble, indexA: Int, indexB: Int) {
        val dx = b.x - a.x
        val dy = b.y - a.y
        val minDistance = a.radius + b.radius
        var distance = hypot(dx, dy)
        if (distance >= minDistance) return

        val nx: Float
        val ny: Float
        if (distance < 0.0001f) {
            // Deterministic normal for exact overlap, never 0/0.
            val angle = (indexA * 31 + indexB * 17 + 1) * 0.7f
            nx = cos(angle)
            ny = sin(angle)
            distance = 0f
        } else {
            nx = dx / distance
            ny = dy / distance
        }

        val draggedA = indexA == draggedIndex
        val draggedB = indexB == draggedIndex
        if (draggedA && draggedB) return

        val invMassA = if (draggedA) 0f else 1f / (a.radius * a.radius)
        val invMassB = if (draggedB) 0f else 1f / (b.radius * b.radius)
        val totalInvMass = invMassA + invMassB
        if (totalInvMass <= 0f) return

        val overlap = (minDistance - distance).coerceAtLeast(0f)
        val correction = overlap * POSITION_CORRECTION / totalInvMass
        if (invMassA > 0f) {
            a.x -= nx * correction * invMassA
            a.y -= ny * correction * invMassA
        }
        if (invMassB > 0f) {
            b.x += nx * correction * invMassB
            b.y += ny * correction * invMassB
        }

        val relativeVx = b.vx - a.vx
        val relativeVy = b.vy - a.vy
        val velocityAlongNormal = relativeVx * nx + relativeVy * ny
        if (velocityAlongNormal < 0f) {
            registerImpact(-velocityAlongNormal)
            val impulse = -(1f + BALL_RESTITUTION) * velocityAlongNormal / totalInvMass
            if (invMassA > 0f) {
                a.vx -= impulse * invMassA * nx
                a.vy -= impulse * invMassA * ny
            }
            if (invMassB > 0f) {
                b.vx += impulse * invMassB * nx
                b.vy += impulse * invMassB * ny
            }
        }
    }

    private fun collideWithWalls(m: Marble, dragged: Boolean) {
        val minX = m.radius
        val maxX = (width - m.radius).coerceAtLeast(minX)
        val minY = m.radius
        val maxY = (height - m.radius).coerceAtLeast(minY)

        if (m.x < minX) {
            m.x = minX
            if (m.vx < 0f) {
                registerImpact(-m.vx)
                m.vx = if (dragged) 0f else -m.vx * WALL_RESTITUTION
            }
            m.vy *= WALL_FRICTION
        } else if (m.x > maxX) {
            m.x = maxX
            if (m.vx > 0f) {
                registerImpact(m.vx)
                m.vx = if (dragged) 0f else -m.vx * WALL_RESTITUTION
            }
            m.vy *= WALL_FRICTION
        }
        if (m.y < minY) {
            m.y = minY
            if (m.vy < 0f) {
                registerImpact(-m.vy)
                m.vy = if (dragged) 0f else -m.vy * WALL_RESTITUTION
            }
            m.vx *= WALL_FRICTION
        } else if (m.y > maxY) {
            m.y = maxY
            if (m.vy > 0f) {
                registerImpact(m.vy)
                m.vy = if (dragged) 0f else -m.vy * WALL_RESTITUTION
            }
            m.vx *= WALL_FRICTION
        }
    }

    private fun constrainInside(m: Marble) {
        if (width <= 0f || height <= 0f) return
        m.x = m.x.coerceIn(m.radius, (width - m.radius).coerceAtLeast(m.radius))
        m.y = m.y.coerceIn(m.radius, (height - m.radius).coerceAtLeast(m.radius))
    }

    private fun registerImpact(strength: Float) {
        if (!strength.isFinite() || strength < 120f) return
        if (strength > pendingImpactStrength) pendingImpactStrength = strength
    }

    private fun clampSpeed(m: Marble) {
        val speed = hypot(m.vx, m.vy)
        if (speed > MAX_SPEED) {
            val factor = MAX_SPEED / speed
            m.vx *= factor
            m.vy *= factor
        }
    }

    private fun sanitize(value: Float, fallback: Float): Float = if (value.isFinite()) value else fallback
}
