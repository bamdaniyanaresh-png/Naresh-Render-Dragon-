$input v_color0, v_fog, v_shadowData, v_lightmapUV, v_texcoord0, v_worldPos, v_position, v_viewVector

#include "bgfx_shader.sh"
#include "renderer/effects/player_shadow.h"
#include "renderer/effects/surface_style.h"

SAMPLER2D(s_MatTexture, 0);
SAMPLER2D(s_LightMapTexture, 1);
SAMPLER2D(s_SeasonsTexture, 2);

uniform vec4 CameraFacingDirection;
uniform vec4 SunDirection;

void main()
{
    vec4 material = texture2D(s_MatTexture, v_texcoord0);
    vec3 light = texture2D(s_LightMapTexture, v_lightmapUV).rgb;
    float alpha = material.a * v_color0.a;

#ifdef ALPHA_TEST
    if (alpha < 0.50)
        discard;
#endif

#if defined(DEPTH_ONLY) || defined(DEPTH_ONLY_OPAQUE)
    gl_FragColor = vec4(1.0);
    return;
#endif

#ifdef SEASONS
    vec3 seasonalTint = texture2D(s_SeasonsTexture, v_color0.xy).rgb;
    float seasonalAmount = clamp(v_color0.z, 0.0, 1.0);
    material.rgb *= mix(vec3(1.0), seasonalTint * 1.35, seasonalAmount);
#endif

    vec3 environmentLight = rdEnvironmentLight(light, SunDirection.xyz);
    vec3 color = material.rgb * v_color0.rgb * environmentLight;

#if defined(TRANSPARENT) && !defined(SEASONS) && !defined(RENDER_AS_BILLBOARDS)
    float waterMask = rdWaterVertexMask(v_color0);
    if (waterMask > 0.001) {
        float wave = rdWaterWave(v_worldPos, v_shadowData.x);
        float fresnel = rdWaterFresnel(v_viewVector);
        vec3 waterColor = color * RD_WATER_TINT;

        vec3 viewDirection = normalize(v_viewVector);
        vec3 reflectedView = reflect(-viewDirection, vec3(0.0, 1.0, 0.0));
        float sunLength = max(length(SunDirection.xyz), 0.0001);
        vec3 sunDirection = SunDirection.xyz / sunLength;
        float sunGlint = pow(
            max(dot(reflectedView, sunDirection), 0.0),
            40.0
        );
        vec3 horizonReflection = mix(
            vec3(0.08, 0.13, 0.19),
            v_fog.rgb,
            0.62
        );

        waterColor = mix(
            waterColor,
            horizonReflection,
            fresnel * RD_WATER_REFLECTION
        );
        waterColor += vec3(0.035, 0.075, 0.095)
            * RD_WATER_SHEEN
            * (0.35 + 0.65 * wave)
            * fresnel;
        waterColor += vec3(1.0, 0.88, 0.68)
            * sunGlint
            * RD_WATER_SPECULAR
            * (0.4 + 0.6 * wave);
        color = mix(color, waterColor, waterMask);
    }
#endif

#if RD_PLAYER_SHADOW_ENABLED && !defined(TRANSPARENT) && !defined(RENDER_AS_BILLBOARDS)
    float playerShadow = rdPlayerShadowCoverage(
        -v_viewVector,
        CameraFacingDirection.xyz,
        SunDirection.xyz,
        v_shadowData.x,
        v_shadowData.y
    );

    float brightBlockLight = smoothstep(0.52, 0.96, max(light.r, max(light.g, light.b)));
    float shadowWeight = clamp(
        playerShadow
            * RD_PLAYER_SHADOW_OPACITY
            * mix(1.0, 0.72, brightBlockLight),
        0.0,
        1.0
    );
    color = mix(color, color * RD_PLAYER_SHADOW_TINT, shadowWeight);
#endif

    float fogWeight = smoothstep(0.0, 1.0, clamp(v_fog.a, 0.0, 1.0));
    color = mix(
        color,
        v_fog.rgb,
        fogWeight
    );

    color = rdSoftColorGrade(color);
    gl_FragColor = vec4(color, alpha);
}
