$input v_color0, v_texcoord0, v_worldPos

#include "bgfx_shader.sh"

uniform vec4 SkyColor;

void main()
{
    vec3 dir = normalize(v_worldPos);

    float horizon = pow(
        1.0 - abs(dir.y),
        1.7
    );

    vec3 deepColor = vec3(0.008, 0.003, 0.025);
    vec3 purpleColor = vec3(0.20, 0.035, 0.42);
    vec3 violetColor = vec3(0.48, 0.08, 0.78);

    float gradient = clamp(
        0.5 + dir.y * 0.5,
        0.0,
        1.0
    );

    vec3 color = mix(
        purpleColor,
        deepColor,
        gradient
    );

    color += violetColor * horizon * 0.18;
    color *= max(SkyColor.rgb, vec3(0.25));

    color *= max(v_color0.rgb, vec3(0.0));

    gl_FragColor = vec4(color, 1.0);
}
