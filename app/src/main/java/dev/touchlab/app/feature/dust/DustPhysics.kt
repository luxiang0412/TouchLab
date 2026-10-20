package dev.touchlab.app.feature.dust

import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.hypot
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * A fixed-size, deterministic particle field. Coordinates are pixels.
 *
 * With no touch, particles drift on a slow sine field. While touched, particles
 * are pulled toward the finger with a softened inverse-square force, offset by
 * a tangential swirl and a soft core repulsion so they form a halo instead of
 * collapsing onto one pixel. Velocities and accelerations are clamped and the
 * particle count never changes.
 */
class DustPhysics(
    widthPx: Float = 0f,
    heightPx: Float = 0f,
) {
    companion object {
        const val PARTICLE_COUNT = 120
        const val DEFAULT_SEED = 42L
        const val MAX_STEP_SECONDS = 1f / 30f
        const val MAX_SPEED = 1500f

        private const val ATTRACTION = 45_000_000f
        private const val SOFTENING = 3_600f
        private const val MAX_PULL = 4_200f
        private const val MAX_ACCELERATION = 5_200f
        private const val SWIRL_ACCELERATION = 300f
        private const val SWIRL_RADIUS_SQUARED = 340f * 340f
        private const val TOUCH_DAMPING = 1.35f
        private const val IDLE_DAMPING = 0.32f
        private const val IDLE_ACCELERATION = 9f
        private const val CORE_RADIUS = 26f
        private const val CORE_REPULSION = 46f
        private const val WALL_RESTITUTION = 0.72f
        private const val SECONDS_TO_WRAP = 3600f

        private val TWO_PI = (Math.PI * 2.0).toFloat()
    }

    class Particle(
        var x: Float = 0f,
        var y: Float = 0f,
        var vx: Float = 0f,
        var vy: Float = 0f,
        var radius: Float = 1.5f,
        var phase: Float = 0f,
        var brightness: Float = 1f,
    )

    var width: Float = 0f
        private set
    var height: Float = 0f
        private set

    var touchActive: Boolean = false
        private set
    var touchX: Float = 0f
        private set
    var touchY: Float = 0f
        private set

    var simulatedSeconds: Float = 0f
        private set

    private val field = ArrayList<Particle>(PARTICLE_COUNT)
    private var seed = DEFAULT_SEED

    init {
        repeat(PARTICLE_COUNT) { field.add(Particle()) }
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

        if (width > 0f && height > 0f) {
            if (hadSize) {
                for (p in field) {
                    p.x = (p.x / oldWidth) * width
                    p.y = (p.y / oldHeight) * height
                    constrainIntoBounds(p)
                }
            } else {
                reset(seed)
            }
        }
    }

    fun reset(seed: Long = DEFAULT_SEED) {
        this.seed = seed
        simulatedSeconds = 0f
        touchActive = false
        if (width <= 0f || height <= 0f) return

        val random = Lcg(seed)
        val cx = width * 0.5f
        val cy = height * 0.5f
        val cloudRadius = min(width, height) * 0.36f
        for (p in field) {
            val angle = random.nextFloat() * TWO_PI
            val distance = sqrt(random.nextFloat()) * cloudRadius
            p.x = cx + cos(angle) * distance
            p.y = cy + sin(angle) * distance * 0.92f
            p.vx = (random.nextFloat() - 0.5f) * 26f
            p.vy = (random.nextFloat() - 0.5f) * 26f
            p.radius = 1.0f + random.nextFloat() * 2.2f
            p.phase = random.nextFloat() * TWO_PI
            p.brightness = 0.35f + random.nextFloat() * 0.65f
            constrainIntoBounds(p)
        }
    }

    fun beginTouch(x: Float, y: Float) {
        if (width <= 0f || height <= 0f) return
        touchActive = true
        touchX = sanitize(x, width * 0.5f)
        touchY = sanitize(y, height * 0.5f)
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

    fun particle(index: Int): Particle = field[index]

    fun particleCount(): Int = field.size

    fun step(dt: Float, motionScale: Float = 1f) {
        if (!dt.isFinite() || dt <= 0f) return
        if (width <= 0f || height <= 0f) return
        val time = dt.coerceAtMost(MAX_STEP_SECONDS)
        val scale = motionScale.coerceIn(0f, 1f)
        simulatedSeconds += time
        if (simulatedSeconds > SECONDS_TO_WRAP) simulatedSeconds = 0f

        val idle = IDLE_ACCELERATION * scale
        val touch = touchActive

        for (p in field) {
            var ax = sin(simulatedSeconds * 0.55f + p.phase) * idle
            var ay = cos(simulatedSeconds * 0.47f + p.phase * 1.31f) * idle

            if (touch) {
                val dx = touchX - p.x
                val dy = touchY - p.y
                val distanceSquared = dx * dx + dy * dy
                val safeDistance = sqrt(distanceSquared + 1e-4f)
                val invDistance = 1f / safeDistance
                val dirX = dx * invDistance
                val dirY = dy * invDistance

                val pull = min(MAX_PULL, ATTRACTION / (distanceSquared + SOFTENING))
                ax += dirX * pull
                ay += dirY * pull

                val swirlWeight = exp(-distanceSquared / (2f * SWIRL_RADIUS_SQUARED))
                ax += -dirY * SWIRL_ACCELERATION * swirlWeight
                ay += dirX * SWIRL_ACCELERATION * swirlWeight

                if (safeDistance < CORE_RADIUS) {
                    val push = (CORE_RADIUS - safeDistance) * CORE_REPULSION
                    ax -= dirX * push
                    ay -= dirY * push
                }
            }

            val accelMagnitude = hypot(ax, ay)
            if (accelMagnitude > MAX_ACCELERATION) {
                val factor = MAX_ACCELERATION / accelMagnitude
                ax *= factor
                ay *= factor
            }

            val damping = if (touch) TOUCH_DAMPING else IDLE_DAMPING
            val decay = exp(-damping * time)
            p.vx = (p.vx + ax * time) * decay
            p.vy = (p.vy + ay * time) * decay

            val speed = hypot(p.vx, p.vy)
            if (speed > MAX_SPEED) {
                val factor = MAX_SPEED / speed
                p.vx *= factor
                p.vy *= factor
            }

            p.x += p.vx * time
            p.y += p.vy * time
            bounceInside(p)

            if (!p.x.isFinite() || !p.y.isFinite() || !p.vx.isFinite() || !p.vy.isFinite()) {
                p.x = width * 0.5f
                p.y = height * 0.5f
                p.vx = 0f
                p.vy = 0f
            }
        }
    }

    fun averageDistanceToTouch(): Float {
        if (field.isEmpty()) return 0f
        var sum = 0f
        for (p in field) {
            sum += hypot(touchX - p.x, touchY - p.y)
        }
        return sum / field.size
    }

    fun maxSpeed(): Float {
        var max = 0f
        for (p in field) {
            max = maxOf(max, hypot(p.vx, p.vy))
        }
        return max
    }

    fun allPositionsFinite(): Boolean = field.all { it.x.isFinite() && it.y.isFinite() }

    fun allInsideBounds(): Boolean = field.all {
        it.x >= -0.01f && it.x <= width + 0.01f && it.y >= -0.01f && it.y <= height + 0.01f
    }

    private fun bounceInside(p: Particle) {
        val minX = p.radius
        val maxX = (width - p.radius).coerceAtLeast(minX)
        val minY = p.radius
        val maxY = (height - p.radius).coerceAtLeast(minY)
        if (p.x < minX) {
            p.x = minX
            p.vx = abs(p.vx) * WALL_RESTITUTION
        } else if (p.x > maxX) {
            p.x = maxX
            p.vx = -abs(p.vx) * WALL_RESTITUTION
        }
        if (p.y < minY) {
            p.y = minY
            p.vy = abs(p.vy) * WALL_RESTITUTION
        } else if (p.y > maxY) {
            p.y = maxY
            p.vy = -abs(p.vy) * WALL_RESTITUTION
        }
    }

    private fun constrainIntoBounds(p: Particle) {
        if (width <= 0f || height <= 0f) return
        p.x = p.x.coerceIn(p.radius, (width - p.radius).coerceAtLeast(p.radius))
        p.y = p.y.coerceIn(p.radius, (height - p.radius).coerceAtLeast(p.radius))
    }

    private fun sanitize(value: Float, fallback: Float): Float = if (value.isFinite()) value else fallback

    private class Lcg(seed: Long) {
        private var state: Long = if (seed == 0L) 1L else seed

        fun nextFloat(): Float {
            state = state * 6364136223846793005L + 1442695040888963407L
            val bits = ((state ushr 40).toInt() and 0xFFFFFF)
            return bits / 16_777_216f
        }
    }
}
