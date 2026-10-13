#ifndef RENDERER_TIME_H
#define RENDERER_TIME_H

/*
 * Common RenderDragon time interface.
 *
 * The shader reads these values from RenderDragon material metadata.
 * Minecraft RenderDragon supplies the actual values through
 * material uniform metadata.
 */

uniform vec4 RendererTimeOfDay;
uniform vec4 RendererElapsedFrameTime;
uniform vec4 RendererViewPositionAndTime;

/*
 * Minecraft day/night position.
 * 0.0 .. 1.0
 */
float rendererTimeOfDay()
{
    return clamp(RendererTimeOfDay.x, 0.0, 1.0);
}

/*
 * Time passed since the previous frame.
 */
float rendererFrameTime()
{
    return max(RendererElapsedFrameTime.x, 0.0);
}

/*
 * Continuous renderer animation clock.
 *
 * ViewPositionAndTime.w is supplied by RenderDragon and is suitable
 * for shader-side animation that must continue from frame to frame.
 */
float rendererAnimationTime()
{
    return RendererViewPositionAndTime.w;
}

/*
 * Convenient repeating 0..1 animation phase.
 */
float rendererAnimationPhase(float speed)
{
    return fract(rendererAnimationTime() * speed);
}

#endif
