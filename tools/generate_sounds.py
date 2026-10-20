#!/usr/bin/env python3
"""Generate TouchLab's original short sound effects.

The project deliberately avoids third-party audio. Every WAV is synthesized
here with the Python standard library (wave/math/struct/random) and committed
under app/src/main/res/raw/.

Run from the repository root:
    python tools/generate_sounds.py
"""

from __future__ import annotations

import math
import os
import random
import struct
import wave

SAMPLE_RATE = 22050
OUT_DIR = os.path.join("app", "src", "main", "res", "raw")


def envelope(t: float, duration: float, attack: float, release: float, curve: float = 2.0) -> float:
    if t < attack:
        return t / max(attack, 1e-5)
    if t > duration - release:
        return max(0.0, (duration - t) / max(release, 1e-5)) ** 1.2
    return (1.0 - (t - attack) / max(duration - attack, 1e-5)) ** curve


def sine(freq: float, t: float, phase: float = 0.0) -> float:
    return math.sin(2.0 * math.pi * freq * t + phase)


def write_wav(name: str, samples: list[float]) -> None:
    os.makedirs(OUT_DIR, exist_ok=True)
    peak = max(1e-6, max(abs(s) for s in samples))
    gain = 0.62 / peak
    frames = bytearray()
    for sample in samples:
        value = int(max(-1.0, min(1.0, sample * gain)) * 32767)
        frames += struct.pack("<h", value)
    path = os.path.join(OUT_DIR, name)
    with wave.open(path, "wb") as handle:
        handle.setnchannels(1)
        handle.setsampwidth(2)
        handle.setframerate(SAMPLE_RATE)
        handle.writeframes(bytes(frames))
    print(f"wrote {path} ({len(samples) / SAMPLE_RATE:.3f}s, {len(frames)} bytes)")


def jelly_press() -> None:
    duration = 0.16
    count = int(SAMPLE_RATE * duration)
    samples = []
    for i in range(count):
        t = i / SAMPLE_RATE
        env = envelope(t, duration, 0.004, 0.10, curve=2.6)
        freq = 190.0 - 65.0 * (t / duration)
        tone = sine(freq, t) * 0.72 + sine(freq * 2.01, t) * 0.14
        samples.append(tone * env)
    write_wav("jelly_press.wav", samples)


def jelly_release() -> None:
    duration = 0.34
    count = int(SAMPLE_RATE * duration)
    samples = []
    for i in range(count):
        t = i / SAMPLE_RATE
        x = t / duration
        env = envelope(t, duration, 0.006, 0.16, curve=2.1)
        freq = 210.0 + 110.0 * math.sin(math.pi * min(1.0, x * 1.15)) - 30.0 * x
        tone = sine(freq, t) * 0.66 + sine(freq * 1.5, t, 0.6) * 0.18
        samples.append(tone * env)
    write_wav("jelly_release.wav", samples)


def dust_touch() -> None:
    duration = 0.30
    count = int(SAMPLE_RATE * duration)
    random.seed(7)
    noise_state = 0.0
    samples = []
    for i in range(count):
        t = i / SAMPLE_RATE
        env = envelope(t, duration, 0.035, 0.14, curve=1.8)
        noise_state = noise_state * 0.93 + (random.random() * 2.0 - 1.0) * 0.07
        air = noise_state * 0.35
        chord = (
            sine(523.25, t) * 0.26
            + sine(659.25, t, 0.4) * 0.20
            + sine(783.99, t, 1.1) * 0.16
        )
        samples.append((chord + air) * env)
    write_wav("dust_touch.wav", samples)


def marble_hit() -> None:
    duration = 0.11
    count = int(SAMPLE_RATE * duration)
    samples = []
    for i in range(count):
        t = i / SAMPLE_RATE
        env = envelope(t, duration, 0.002, 0.07, curve=3.0)
        tone = (
            sine(880.0 - 160.0 * (t / duration), t) * 0.55
            + sine(1420.0, t, 0.3) * 0.22
            + sine(2320.0, t, 0.9) * 0.08
        )
        samples.append(tone * env)
    write_wav("marble_hit.wav", samples)


def ui_tap() -> None:
    duration = 0.075
    count = int(SAMPLE_RATE * duration)
    samples = []
    for i in range(count):
        t = i / SAMPLE_RATE
        env = envelope(t, duration, 0.002, 0.05, curve=2.8)
        tone = sine(640.0, t) * 0.60 + sine(960.0, t, 0.8) * 0.18
        samples.append(tone * env)
    write_wav("ui_tap.wav", samples)


def main() -> None:
    jelly_press()
    jelly_release()
    dust_touch()
    marble_hit()
    ui_tap()


if __name__ == "__main__":
    main()
