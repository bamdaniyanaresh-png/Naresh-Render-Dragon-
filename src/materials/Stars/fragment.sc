$input v_color0, v_texcoord0, v_worldPos

#include "bgfx_shader.sh"

uniform vec4 StarsColor;

void main()
{
    vec3 color =
        max(v_color0.rgb, vec3(0.0)) *
        max(StarsColor.rgb, vec3(0.25));

    gl_FragColor = vec4(color, v_color0.a);
}
