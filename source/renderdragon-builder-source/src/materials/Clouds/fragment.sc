$input v_color0, v_texcoord0, v_worldPos

#include "bgfx_shader.sh"

void main()
{
    vec3 color = max(v_color0.rgb, vec3(0.0));
    float alpha = clamp(v_color0.a, 0.0, 1.0);

    gl_FragColor = vec4(color, alpha);
}
