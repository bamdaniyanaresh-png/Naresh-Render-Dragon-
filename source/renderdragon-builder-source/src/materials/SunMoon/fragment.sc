$input v_color0, v_texcoord0, v_worldPos

#include "bgfx_shader.sh"

SAMPLER2D(s_SunMoonTexture, 0);

uniform vec4 SunMoonColor;

void main()
{
    vec4 tex = texture2D(
        s_SunMoonTexture,
        v_texcoord0
    );

    vec3 color =
        tex.rgb *
        max(SunMoonColor.rgb, vec3(0.25)) *
        max(v_color0.rgb, vec3(0.0));

    gl_FragColor = vec4(
        color,
        tex.a * v_color0.a
    );
}
