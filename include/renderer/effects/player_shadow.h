#ifndef RENDERER_EFFECTS_PLAYER_SHADOW_H
#define RENDERER_EFFECTS_PLAYER_SHADOW_H

/*
 * A lightweight, procedural ground silhouette for RenderChunk.
 *
 * This is a camera-anchored approximation: the RenderChunk interface has no
 * authoritative player transform. See docs/PLAYER_SHADOW.md before enabling
 * it in third-person or nonstandard camera modes.
 */
#ifndef RD_PLAYER_SHADOW_ENABLED
#define RD_PLAYER_SHADOW_ENABLED 1
#endif

#ifndef RD_PLAYER_SHADOW_EYE_TO_FEET
#define RD_PLAYER_SHADOW_EYE_TO_FEET 1.62
#endif

#ifndef RD_PLAYER_SHADOW_OPACITY
#define RD_PLAYER_SHADOW_OPACITY 0.48
#endif

#ifndef RD_PLAYER_SHADOW_EDGE
#define RD_PLAYER_SHADOW_EDGE 0.045
#endif

#ifndef RD_PLAYER_SHADOW_WALK_START
#define RD_PLAYER_SHADOW_WALK_START 0.004
#endif

#ifndef RD_PLAYER_SHADOW_WALK_FULL
#define RD_PLAYER_SHADOW_WALK_FULL 0.040
#endif

#ifndef RD_PLAYER_SHADOW_WALK_RATE
#define RD_PLAYER_SHADOW_WALK_RATE 7.2
#endif

#ifndef RD_PLAYER_SHADOW_LEG_SWAY
#define RD_PLAYER_SHADOW_LEG_SWAY 0.13
#endif

#ifndef RD_PLAYER_SHADOW_GROUND_FADE
#define RD_PLAYER_SHADOW_GROUND_FADE 0.34
#endif

#ifndef RD_PLAYER_SHADOW_TINT
#define RD_PLAYER_SHADOW_TINT vec3(0.24, 0.28, 0.34)
#endif

#if RD_PLAYER_SHADOW_ENABLED

float rdShadowRectangle(
    vec2 samplePosition,
    vec2 lowerBound,
    vec2 upperBound,
    float edgeWidth
)
{
    vec2 insideDistance = min(samplePosition - lowerBound, upperBound - samplePosition);
    float signedDistance = min(insideDistance.x, insideDistance.y);
    return smoothstep(-edgeWidth, edgeWidth, signedDistance);
}

float rdPlayerShadowCoverage(
    vec3 cameraToSurface,
    vec3 cameraFacing,
    vec3 sunDirection,
    float animationClock,
    float horizontalCameraTravel
)
{
    float sunDirectionLength = length(sunDirection);
    vec3 lightDirection = sunDirectionLength > 0.0001
        ? sunDirection / sunDirectionLength
        : vec3(0.0, 1.0, 0.0);
    float sunElevation = smoothstep(0.04, 0.24, lightDirection.y);

    /*
     * cameraToSurface comes from the project's existing view-vector varying,
     * so the shadow stays in the same space as the transformed RenderChunk
     * vertices without assuming that the renderer uses absolute positions.
     */
    vec3 surfaceFromFeet =
        cameraToSurface + vec3(0.0, RD_PLAYER_SHADOW_EYE_TO_FEET, 0.0);

    /*
     * Suppress the effect on walls and on terrain well above/below the
     * estimated foot plane; RenderChunk does not supply a face normal.
     */
    float groundPlaneWeight = 1.0 - smoothstep(
        0.06,
        RD_PLAYER_SHADOW_GROUND_FADE,
        abs(surfaceFromFeet.y)
    );

    vec2 facing = cameraFacing.xz;
    float facingLength = length(facing);
    facing = facingLength > 0.0001 ? facing / facingLength : vec2(0.0, 1.0);
    vec2 lateral = vec2(facing.y, -facing.x);

    vec2 horizontalLight = lightDirection.xz;
    float horizontalLightLength = length(horizontalLight);
    vec2 castDirection = horizontalLightLength > 0.0001
        ? -horizontalLight / horizontalLightLength
        : vec2(0.0, 1.0);

    float projectedLength = clamp(
        1.65 * horizontalLightLength / max(lightDirection.y, 0.20),
        0.28,
        2.15
    );
    float shadowScale = clamp(projectedLength / 1.45, 0.26, 1.35);
    vec2 groundPoint = surfaceFromFeet.xz;
    vec2 shadowPoint = vec2(
        dot(groundPoint, castDirection) / shadowScale,
        dot(groundPoint, lateral) / max(0.72, 0.62 + shadowScale * 0.40)
    );

    float walking = smoothstep(
        RD_PLAYER_SHADOW_WALK_START,
        RD_PLAYER_SHADOW_WALK_FULL,
        horizontalCameraTravel
    );
    float stepOffset = sin(animationClock * RD_PLAYER_SHADOW_WALK_RATE)
        * RD_PLAYER_SHADOW_LEG_SWAY * walking;

    float handOffset = -stepOffset * 0.62;
    float head = 1.0 - smoothstep(
        0.12,
        0.12 + RD_PLAYER_SHADOW_EDGE * 2.0,
        length(shadowPoint - vec2(1.20, 0.0))
    );
    float torso = rdShadowRectangle(
        shadowPoint,
        vec2(0.42, -0.16),
        vec2(1.08, 0.16),
        RD_PLAYER_SHADOW_EDGE
    );
    float leftArm = rdShadowRectangle(
        shadowPoint,
        vec2(0.50, 0.12 + handOffset),
        vec2(0.88, 0.34 + handOffset),
        RD_PLAYER_SHADOW_EDGE
    );
    float rightArm = rdShadowRectangle(
        shadowPoint,
        vec2(0.50, -0.34 - handOffset),
        vec2(0.88, -0.12 - handOffset),
        RD_PLAYER_SHADOW_EDGE
    );
    float leftLeg = rdShadowRectangle(
        shadowPoint,
        vec2(0.04, 0.025 + stepOffset),
        vec2(0.60, 0.14 + stepOffset),
        RD_PLAYER_SHADOW_EDGE
    );
    float rightLeg = rdShadowRectangle(
        shadowPoint,
        vec2(0.04, -0.14 - stepOffset),
        vec2(0.60, -0.025 - stepOffset),
        RD_PLAYER_SHADOW_EDGE
    );
    float silhouette = max(
        max(head, torso),
        max(max(leftArm, rightArm), max(leftLeg, rightLeg))
    );

    return silhouette * groundPlaneWeight * sunElevation;
}

#endif

#endif
