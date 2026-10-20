package dev.touchlab.app.physics

import dev.touchlab.app.feature.jelly.JellyPhysics
import kotlin.math.abs
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class JellyPhysicsTest {

    private fun create(): JellyPhysics {
        val jelly = JellyPhysics(1080f, 1800f, desiredRadiusPx = 280f)
        jelly.reset()
        return jelly
    }

    private fun JellyPhysics.settle(seconds: Float) {
        val steps = (seconds * 120f).toInt()
        repeat(steps) { step(1f / 120f) }
    }

    @Test
    fun pressIndentsNearbyOutlineMoreThanFarSide() {
        val jelly = create()
        val touchX = jelly.centerX + jelly.radius * 0.82f
        jelly.beginTouch(touchX, jelly.centerY)
        jelly.settle(0.30f)

        val near = jelly.indexForDirection(1f, 0f)
        val far = jelly.indexForDirection(-1f, 0f)
        assertTrue("near side should move inward", jelly.displacementAt(near) < -1f)
        assertTrue(
            "near indentation must exceed far side displacement",
            jelly.displacementAt(near) < jelly.displacementAt(far) - 2f,
        )
        assertFalse(jelly.displacementAt(near).isNaN())
    }

    @Test
    fun releaseSettlesWithinThreeSeconds() {
        val jelly = create()
        jelly.beginTouch(jelly.centerX + jelly.radius * 0.7f, jelly.centerY)
        jelly.settle(0.35f)
        jelly.moveTouch(jelly.centerX + jelly.radius * 0.5f, jelly.centerY + jelly.radius * 0.3f)
        jelly.settle(0.25f)
        jelly.endTouch()
        jelly.settle(3.0f)

        assertTrue("outline should settle, was ${jelly.maxAbsDisplacement()}", jelly.maxAbsDisplacement() < 1.5f)
        assertTrue("outline velocity should settle", jelly.maxAbsOutlineVelocity() < 30f)
        assertTrue("center should be inside canvas", jelly.centerX in 0f..1080f)
        assertTrue("center should be inside canvas", jelly.centerY in 0f..1800f)
    }

    @Test
    fun displacementAndVelocityStayBounded() {
        val jelly = create()
        jelly.beginTouch(jelly.centerX + jelly.radius, jelly.centerY)
        repeat(600) {
            jelly.moveTouch(
                jelly.centerX + jelly.radius * (it % 5 - 2) * 0.5f,
                jelly.centerY + jelly.radius * (it % 3 - 1) * 0.5f,
            )
            jelly.step(1f / 120f)
            val limit = jelly.radius * 0.381f
            for (i in 0 until JellyPhysics.POINT_COUNT) {
                assertTrue(abs(jelly.displacementAt(i)) <= limit)
                assertTrue(jelly.displacementAt(i).isFinite())
                assertTrue(jelly.velocityAt(i).isFinite())
            }
        }
    }

    @Test
    fun resetRestoresCenteredRestState() {
        val jelly = create()
        jelly.beginTouch(jelly.centerX + 100f, jelly.centerY)
        jelly.settle(0.3f)
        jelly.reset()

        assertFalse(jelly.touchActive)
        assertEquals(1080f * 0.5f, jelly.centerX, 0.01f)
        assertEquals(1800f * 0.5f, jelly.centerY, 0.01f)
        assertEquals(0f, jelly.maxAbsDisplacement(), 0.0001f)
        assertEquals(0f, jelly.centerVx, 0.0001f)
        assertEquals(0f, jelly.centerVy, 0.0001f)
    }

    @Test
    fun zeroAndNegativeDtDoNothing() {
        val jelly = create()
        jelly.beginTouch(jelly.centerX + 50f, jelly.centerY)
        jelly.step(0.2f)
        val snapshot = FloatArray(JellyPhysics.POINT_COUNT) { jelly.displacementAt(it) }
        val cx = jelly.centerX

        jelly.step(0f)
        jelly.step(-1f)
        jelly.step(Float.NaN)

        assertEquals(cx, jelly.centerX, 0.0001f)
        for (i in 0 until JellyPhysics.POINT_COUNT) {
            assertEquals(snapshot[i], jelly.displacementAt(i), 0.0001f)
        }
    }

    @Test
    fun hugeDtIsClampedAndStaysFinite() {
        val jelly = create()
        jelly.beginTouch(10f, 10f)
        repeat(100) { jelly.step(10f) }
        assertTrue(jelly.centerX.isFinite())
        assertTrue(jelly.centerY.isFinite())
        assertTrue(jelly.maxAbsDisplacement().isFinite())
        assertTrue(jelly.maxAbsOutlineVelocity().isFinite())
    }

    @Test
    fun zeroSizeCanvasIsSafe() {
        val jelly = JellyPhysics(0f, 0f, desiredRadiusPx = 200f)
        jelly.beginTouch(10f, 10f)
        jelly.step(1f / 120f)
        jelly.reset()
        jelly.resize(0f, 0f)
        assertTrue(jelly.centerX.isFinite())
        assertTrue(jelly.centerY.isFinite())
        assertTrue(jelly.radius >= 1f)
    }

    @Test
    fun longRunKeepsFiniteState() {
        val jelly = create()
        repeat(50_000) { i ->
            if (i % 600 == 0) {
                jelly.beginTouch(
                    jelly.centerX + jelly.radius * 0.8f,
                    jelly.centerY + jelly.radius * 0.2f,
                )
            }
            if (i % 600 == 300) jelly.endTouch()
            jelly.step(1f / 120f)
        }
        assertTrue(jelly.centerX.isFinite())
        assertTrue(jelly.centerY.isFinite())
        assertTrue(jelly.maxAbsDisplacement().isFinite())
        assertTrue(jelly.maxAbsDisplacement() < jelly.radius)
    }
}
