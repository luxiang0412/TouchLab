package dev.touchlab.app.physics

import dev.touchlab.app.feature.marbles.MarblesPhysics
import kotlin.math.abs
import kotlin.math.hypot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MarblesPhysicsTest {

    private fun create(gravity: Boolean = false): MarblesPhysics {
        val marbles = MarblesPhysics(1080f, 1800f)
        marbles.setGravity(gravity)
        marbles.reset()
        return marbles
    }

    private fun MarblesPhysics.parkAllExcept(vararg keep: Int) {
        val keepSet = keep.toSet()
        val spots = arrayOf(
            130f to (height - 130f),
            350f to (height - 130f),
            570f to (height - 130f),
            790f to (height - 130f),
        )
        var spot = 0
        for (i in 0 until MarblesPhysics.MARBLE_COUNT) {
            if (i in keepSet) continue
            val m = marble(i)
            val (x, y) = spots[spot % spots.size]
            m.x = x
            m.y = y
            m.vx = 0f
            m.vy = 0f
            spot++
        }
    }

    private fun MarblesPhysics.totalKineticEnergy(): Float {
        var energy = 0f
        for (i in 0 until MarblesPhysics.MARBLE_COUNT) {
            val m = marble(i)
            energy += 0.5f * (m.radius * m.radius) * (m.vx * m.vx + m.vy * m.vy)
        }
        return energy
    }

    @Test
    fun rightWallBounceReversesAndLosesSpeed() {
        val marbles = create()
        val m = marbles.marble(0)
        m.x = marbles.width - m.radius - 0.5f
        m.y = marbles.height * 0.15f
        m.vx = 800f
        m.vy = 0f

        marbles.step(1f / 120f)

        assertTrue("vx should reverse, was ${m.vx}", m.vx < 0f)
        assertTrue("speed must not increase", hypot(m.vx, m.vy) <= 800f + 0.01f)
    }

    @Test
    fun headOnCollisionDoesNotGainEnergy() {
        val marbles = create()
        marbles.parkAllExcept(0, 1)
        val a = marbles.marble(0)
        val b = marbles.marble(1)
        a.x = 380f
        a.y = 850f
        a.vx = 520f
        a.vy = 0f
        b.x = 380f + a.radius + b.radius + 4f
        b.y = 850f
        b.vx = -520f
        b.vy = 0f

        val before = marbles.totalKineticEnergy()
        repeat(360) { marbles.step(1f / 120f) }
        val after = marbles.totalKineticEnergy()

        assertTrue("energy before=$before after=$after", after <= before * 1.02f + 0.01f)
        assertTrue(marbles.allPositionsFinite())
    }

    @Test
    fun exactOverlapSeparatesWithoutNaN() {
        val marbles = create()
        marbles.parkAllExcept(0, 1)
        val a = marbles.marble(0)
        val b = marbles.marble(1)
        a.x = 500f
        a.y = 800f
        a.vx = 0f
        a.vy = 0f
        b.x = 500f
        b.y = 800f
        b.vx = 0f
        b.vy = 0f

        repeat(120) { marbles.step(1f / 120f) }

        assertTrue(marbles.allPositionsFinite())
        assertTrue(
            "overlap should resolve, separation=${marbles.minimumSeparation()}",
            marbles.minimumSeparation() > -2f,
        )
    }

    @Test
    fun fallingMarbleStopsMovingVertically() {
        val marbles = create(gravity = true)
        val yBefore = marbles.marble(0).y
        repeat(2400) { marbles.step(1f / 120f) }
        val m = marbles.marble(0)
        assertTrue("should have fallen", m.y >= yBefore)
        assertTrue(m.y <= marbles.height - m.radius + 0.5f)
        assertTrue("vertical speed should settle, vy=${m.vy} vx=${m.vx} y=${m.y}", abs(m.vy) < 120f)
    }

    @Test
    fun gravityToggleChangesSimulation() {
        val marbles = create(gravity = false)
        val startY = FloatArray(MarblesPhysics.MARBLE_COUNT) { marbles.marble(it).y }
        repeat(60) { marbles.step(1f / 120f) }
        for (i in 0 until MarblesPhysics.MARBLE_COUNT) {
            assertEquals(startY[i], marbles.marble(i).y, 0.01f)
        }

        marbles.setGravity(true)
        repeat(120) { marbles.step(1f / 120f) }
        val moved = (0 until MarblesPhysics.MARBLE_COUNT).count { i ->
            marbles.marble(i).y > startY[i] + 0.5f
        }
        assertTrue("gravity should move balls, moved=$moved", moved >= 4)
    }

    @Test
    fun dragOutsideBoundsIsClamped() {
        val marbles = create()
        val m = marbles.marble(0)
        assertTrue(marbles.beginDrag(m.x, m.y))
        marbles.dragTo(-500f, -500f, 0.016f)
        marbles.step(1f / 120f)

        assertTrue(m.x >= m.radius - 0.01f)
        assertTrue(m.y >= m.radius - 0.01f)
        marbles.endDrag()
        assertEquals(-1, marbles.draggedIndex)
    }

    @Test
    fun flickCreatesMotionThenDampingSlowsIt() {
        val marbles = create()
        val m = marbles.marble(1)
        assertTrue(marbles.beginDrag(m.x, m.y))
        marbles.dragTo(m.x + 40f, m.y - 10f, 0.016f)
        marbles.step(1f / 120f)
        marbles.dragTo(m.x + 180f, m.y - 80f, 0.016f)
        marbles.step(1f / 120f)
        marbles.endDrag()

        val initialSpeed = hypot(m.vx, m.vy)
        assertTrue("flick should throw the marble, speed=$initialSpeed", initialSpeed > 200f)

        repeat(960) { marbles.step(1f / 120f) }
        assertTrue("damping should slow it, speed=${hypot(m.vx, m.vy)}", hypot(m.vx, m.vy) < initialSpeed * 0.5f)
    }

    @Test
    fun resetRestoresPresetAndClearsVelocity() {
        val marbles = create()
        val xs = FloatArray(MarblesPhysics.MARBLE_COUNT) { marbles.marble(it).x }
        val ys = FloatArray(MarblesPhysics.MARBLE_COUNT) { marbles.marble(it).y }

        marbles.marble(0).x += 200f
        marbles.marble(0).vx = 900f
        marbles.setGravity(true)
        marbles.reset()

        for (i in 0 until MarblesPhysics.MARBLE_COUNT) {
            assertEquals(xs[i], marbles.marble(i).x, 0.01f)
            assertEquals(ys[i], marbles.marble(i).y, 0.01f)
            assertEquals(0f, marbles.marble(i).vx, 0.0001f)
            assertEquals(0f, marbles.marble(i).vy, 0.0001f)
        }
        assertEquals(-1, marbles.draggedIndex)
    }

    @Test
    fun longMixedRunStaysFiniteAndInside() {
        val marbles = create()
        repeat(6_000) { i ->
            when (i % 500) {
                0 -> {
                    val m = marbles.marble(i / 500 % MarblesPhysics.MARBLE_COUNT)
                    marbles.beginDrag(m.x, m.y)
                }
                120 -> marbles.dragTo(300f + (i % 500), 300f + (i % 900), 0.016f)
                260 -> marbles.dragTo(800f - (i % 400), 1400f - (i % 700), 0.016f)
                400 -> marbles.endDrag()
                450 -> marbles.setGravity(i % 1000 == 450)
            }
            marbles.step(1f / 120f)
        }

        assertTrue(marbles.allPositionsFinite())
        assertTrue(marbles.allInsideBounds())
        assertTrue(marbles.maxSpeed() <= MarblesPhysics.MAX_SPEED + 0.5f)
        assertTrue("separation should not be deeply negative", marbles.minimumSeparation() > -3f)
    }

    @Test
    fun hugeDtIsClampedAndFinite() {
        val marbles = create(gravity = true)
        repeat(100) { marbles.step(10f) }
        assertTrue(marbles.allPositionsFinite())
        assertTrue(marbles.allInsideBounds())
        assertTrue(marbles.maxSpeed() <= MarblesPhysics.MAX_SPEED + 0.5f)
    }

    @Test
    fun zeroSizeCanvasIsSafe() {
        val marbles = MarblesPhysics(0f, 0f)
        marbles.beginDrag(10f, 10f)
        marbles.step(1f / 120f)
        marbles.reset()
        marbles.resize(-1f, 0f)
        assertFalse(marbles.marble(0).x.isNaN())
        assertFalse(marbles.marble(0).y.isNaN())
    }

    @Test
    fun dragRequiresHittingAMarble() {
        val marbles = create()
        val m = marbles.marble(2)
        assertFalse(marbles.beginDrag(m.x + m.radius * 2.5f, m.y))
        assertEquals(-1, marbles.draggedIndex)
        assertTrue(marbles.beginDrag(m.x + m.radius * 0.2f, m.y))
        assertEquals(2, marbles.draggedIndex)
        marbles.cancelDrag()
        assertEquals(-1, marbles.draggedIndex)
    }
}
