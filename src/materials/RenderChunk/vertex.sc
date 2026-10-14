$input a_color0, a_texcoord0, a_texcoord1, a_position
#ifdef INSTANCING
  $input i_data0, i_data1, i_data2, i_data3
#endif
$output v_color0, v_fog, v_shadowData, v_lightmapUV, v_texcoord0, v_worldPos, v_position, v_viewVector

#include "bgfx_shader.sh"
#include "renderer/effects/surface_style.h"

uniform vec4 ViewPositionAndTime;
uniform vec4 CameraPosition;
uniform vec4 LastCameraPosition;
uniform vec4 RenderChunkFogAlpha;
uniform vec4 FogColor;
uniform vec4 FogAndDistanceControl;
uniform vec4 MeshContext;

float rdSurfaceWind(vec3 position, float clock)
{
    float phase = dot(floor(position.xz), vec2(0.37, 0.53));
    float low = sin(clock * 1.7 + phase);
    float high = sin(clock * 2.3 + phase * 1.31);
    return low * 0.68 + high * 0.32;
}

void main()
{
    mat4 model = u_model[0];
#ifdef INSTANCING
    model = mtxFromCols(i_data0, i_data1, i_data2, i_data3);
#endif

    vec4 world = mul(model, vec4(a_position, 1.0));
    vec4 vertexColor = a_color0;
    float clock = ViewPositionAndTime.w;

#ifdef RENDER_AS_BILLBOARDS
    /*
     * Bedrock billboard vertices encode their corner offsets in color X/Z.
     * Rebuild a camera-facing basis from the project camera vector.
     */
    vec3 cameraVector = ViewPositionAndTime.xyz - world.xyz;
    float cameraLength = max(length(cameraVector), 0.0001);
    vec3 facing = cameraVector / cameraLength;
    vec3 billboardRight = cross(vec3(0.0, 1.0, 0.0), facing);
    float rightLength = length(billboardRight);
    billboardRight = rightLength > 0.0001
        ? billboardRight / rightLength
        : vec3(1.0, 0.0, 0.0);
    vec3 billboardUp = normalize(cross(facing, billboardRight));
    vec2 corner = a_color0.xz - vec2(0.5);
    world.xyz += billboardRight * corner.x + billboardUp * corner.y;
    vertexColor = vec4(1.0);
#endif

    vec3 cameraTravel = CameraPosition.xyz - LastCameraPosition.xyz;
    v_shadowData = vec2(clock, length(cameraTravel.xz));

#if defined(TRANSPARENT) && !defined(RENDER_AS_BILLBOARDS) && !defined(SEASONS)
    float waterMask = rdWaterVertexMask(a_color0);
    world.y += rdSurfaceWind(world.xyz, clock) * 0.012 * waterMask;
#endif

#if defined(ALPHA_TEST) && !defined(RENDER_AS_BILLBOARDS) && !defined(SEASONS)
    float leafLike = smoothstep(0.035, 0.24, abs(a_color0.r - a_color0.g));
    float upperVertex = smoothstep(0.08, 0.94, fract(a_position.y));
    float windAmount = rdSurfaceWind(world.xyz, clock);
    world.x += windAmount * 0.022 * leafLike * upperVertex;
    world.z += windAmount * 0.017 * leafLike * upperVertex;
#endif

    gl_Position = mul(u_viewProj, world);

    v_color0 = vertexColor;

    v_texcoord0 = a_texcoord0;

    vec2 lm = floor(a_texcoord1 * 15.0 + 0.5);
    v_lightmapUV = (lm + vec2(0.5)) / 16.0;

    v_worldPos = world.xyz;
    // Public position inputs for effects that need per-fragment geometry data.
    // v_position intentionally aliases the exact space used by the fog calculation.
    v_position = world.xyz;
    v_viewVector = ViewPositionAndTime.xyz - world.xyz;

    float distanceToCamera =
        length(world.xyz - ViewPositionAndTime.xyz);

    float fogStart = FogAndDistanceControl.x;
    float fogEnd = max(FogAndDistanceControl.y, fogStart + 0.0001);

    float fog = clamp(
        (distanceToCamera - fogStart) /
        (fogEnd - fogStart),
        0.0,
        1.0
    );

    fog = clamp(
        fog + RenderChunkFogAlpha.x,
        0.0,
        1.0
    );

    v_fog = vec4(FogColor.rgb, fog);
}
