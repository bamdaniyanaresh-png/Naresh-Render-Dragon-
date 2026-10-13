$input v_color0, v_ditheringAndMaskTinting, v_fog, v_lightmapUV, v_texcoord0, v_worldPos, v_position, v_viewVector

#include "bgfx_shader.sh"

SAMPLER2D(s_MatTexture, 0);
SAMPLER2D(s_LightMapTexture, 1);

void main()
{
    vec4 material = texture2D(s_MatTexture, v_texcoord0);
    vec3 light = texture2D(s_LightMapTexture, v_lightmapUV).rgb;

    vec3 color =
        material.rgb *
        v_color0.rgb *
        max(light, vec3(0.05));

    color = mix(
        color,
        v_fog.rgb,
        clamp(v_fog.a, 0.0, 1.0)
    );

    gl_FragColor = vec4(
        color,
        material.a * v_color0.a
    );
}
