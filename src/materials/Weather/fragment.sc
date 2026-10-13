$input v_color0, v_fog, v_occlusionHeight, v_occlusionUV, v_texcoord0, v_worldPos

#include "bgfx_shader.sh"

SAMPLER2D(s_WeatherTexture, 0);
SAMPLER2D(s_LightingTexture, 1);
SAMPLER2D(s_OcclusionTexture, 2);

void main()
{
    vec4 weather =
        texture2D(
            s_WeatherTexture,
            v_texcoord0
        );

    vec3 lighting =
        texture2D(
            s_LightingTexture,
            v_texcoord0
        ).rgb;

    float occlusion =
        texture2D(
            s_OcclusionTexture,
            v_occlusionUV
        ).r;

    vec3 color =
        weather.rgb *
        max(lighting, vec3(0.1)) *
        max(v_color0.rgb, vec3(0.0));

    color *= mix(
        1.0,
        occlusion,
        0.25
    );

    color = mix(
        color,
        v_fog.rgb,
        clamp(v_fog.a, 0.0, 1.0)
    );

    gl_FragColor = vec4(
        color,
        weather.a * v_color0.a
    );
}
