package dev.touchlab.app.physics

import dev.touchlab.app.feature.dust.DustPhysics
import kotlin.math.hypot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DustPhysicsTest {

    private fun create(): DustPhysics {
        val dust = DustPhysics(1080f, 1800f)
        dust.reset()
        return dust
    }

    private fun DustPhysics.settle(seconds: Float) {
        repeat((seconds * 120f).toInt()) { step(1f / 120f) }
    }

    @Test
    fun particleCountNeverChanges() {
        val dust = create()
        assertEquals(120, dust.particleCount())

        dust.beginTouch(500f, 800f)
        dust.settle(2f)
        dust.moveTouch(700f, 1000f)
        dust.settle(2f)
        dust.endTouch()
        dust.settle(2f)
        dust.reset()
        dust.settle(1f)
        assertEquals(120, dust.particleCount())
    }

    @Test
    fun touchPullsParticlesCloser() {
        val dust = create()
        dust.beginTouch(dust.width * 0.5f, dust.height * 0.5f)
        val before = dust.averageDistanceToTouch()
        dust.settle(1.8f)
        val after = dust.averageDistanceToTouch()

        assertTrue("particles should gather: before=$before after=$after", after < before * 0.85f)
        assertTrue(after.isFinite())
    }

    @Test
    fun touchExactlyOnParticleDoesNotProduceNaN() {
        val dust = create()
        val target = dust.particle(17)
        dust.beginTouch(target.x, target.y)
        dust.settle(1.0f)
        assertTrue(dust.allPositionsFinite())
        for (i in 0 until DustPhysics.PARTICLE_COUNT) {
            val p = dust.particle(i)
            assertTrue(p.vx.isFinite())
            assertTrue(p.vy.isFinite())
        }
    }

    @Test
    fun longInteractionStaysInsideBoundsAndBoundedSpeed() {
        val dust = create()
        repeat(6_000) { i ->
            when (i % 400) {
                0 -> dust.beginTouch(120f + (i % 700), 200f + (i % 900))
                200 -> dust.moveTouch(900f - (i % 500), 1500f - (i % 600))
                399 -> dust.endTouch()
            }
            dust.step(1f / 120f)
        }
        assertTrue("all positions finite", dust.allPositionsFinite())
        assertTrue("all particles inside bounds", dust.allInsideBounds())
        assertTrue("speed limit", dust.maxSpeed() <= DustPhysics.MAX_SPEED + 0.5f)
    }

    @Test
    fun sameSeedAndInputsProduceSameField() {
        val first = create()
        val second = create()

        repeat(600) { i ->
            if (i == 0) {
                first.beginTouch(400f, 700f)
                second.beginTouch(400f, 700f)
            }
            if (i == 300) {
                first.moveTouch(800f, 1200f)
                second.moveTouch(800f, 1200f)
            }
            first.step(1f / 120f)
            second.step(1f / 120f)
        }

        for (i in 0 until DustPhysics.PARTICLE_COUNT) {
            val a = first.particle(i)
            val b = second.particle(i)
            assertEquals(a.x, b.x, 0.0001f)
            assertEquals(a.y, b.y, 0.0001f)
            assertEquals(a.vx, b.vx, 0.0001f)
            assertEquals(a.vy, b.vy, 0.0001f)
        }
    }

    @Test
    fun resetRestoresDeterministicInitialField() {
        val dust = create()
        val xs = FloatArray(DustPhysics.PARTICLE_COUNT) { dust.particle(it).x }
        val ys = FloatArray(DustPhysics.PARTICLE_COUNT) { dust.particle(it).y }
        val vxs = FloatArray(DustPhysics.PARTICLE_COUNT) { dust.particle(it).vx }
        val vys = FloatArray(DustPhysics.PARTICLE_COUNT) { dust.particle(it).vy }

        dust.beginTouch(500f, 500f)
        dust.settle(1f)
        dust.reset()

        for (i in 0 until DustPhysics.PARTICLE_COUNT) {
            assertEquals(xs[i], dust.particle(i).x, 0.0001f)
            assertEquals(ys[i], dust.particle(i).y, 0.0001f)
            assertEquals(vxs[i], dust.particle(i).vx, 0.0001f)
            assertEquals(vys[i], dust.particle(i).vy, 0.0001f)
        }
    }

    @Test
    fun zeroAndNegativeDtDoNotChangeField() {
        val dust = create()
        val x = dust.particle(0).x
        dust.step(0f)
        dust.step(-1f)
        dust.step(Float.NaN)
        assertEquals(x, dust.particle(0).x, 0.0001f)
    }

    @Test
    fun hugeDtIsClampedAndFinite() {
        val dust = create()
        dust.beginTouch(10f, 10f)
        repeat(100) { dust.step(10f) }
        assertTrue(dust.allPositionsFinite())
        assertTrue(dust.allInsideBounds())
        assertTrue(dust.maxSpeed() <= DustPhysics.MAX_SPEED + 0.5f)
    }

    @Test
    fun zeroSizeCanvasIsSafe() {
        val dust = DustPhysics(0f, 0f)
        dust.beginTouch(10f, 10f)
        dust.step(1f / 120f)
        dust.reset()
        dust.resize(-5f, 0f)
        assertFalse(dust.particle(0).x.isNaN())
        assertFalse(dust.particle(0).y.isNaN())
    }

    @Test
    fun releaseEventuallySlowsParticles() {
        val dust = create()
        dust.beginTouch(dust.width * 0.5f, dust.height * 0.5f)
        dust.settle(1.2f)
        dust.endTouch()
        dust.settle(6f)
        assertTrue("particles should settle, speed=${dust.maxSpeed()}", dust.maxSpeed() < 220f)
        assertTrue(dust.allPositionsFinite())
    }

    @Test
    fun speedAndDistanceRemainReasonable() {
        val dust = create()
        dust.beginTouch(dust.width * 0.5f, dust.height * 0.5f)
        dust.settle(2f)
        for (i in 0 until DustPhysics.PARTICLE_COUNT) {
            val p = dust.particle(i)
            assertTrue(hypot(p.vx, p.vy) <= DustPhysics.MAX_SPEED + 0.5f)
            assertTrue(p.x >= 0f && p.x <= dust.width)
            assertTrue(p.y >= 0f && p.y <= dust.height)
        }
    }
}
