$input v_clipPosition, v_color0, v_fog, v_light, v_texcoord0, v_worldPos

#include "bgfx_shader.sh"

SAMPLER2D(s_MatTexture, 0);

void main()
{
    vec4 tex = texture2D(s_MatTexture, v_texcoord0);

    vec3 light = max(v_light.rgb, vec3(0.15));
    vec3 color = tex.rgb * v_color0.rgb * light;

    color = mix(color, v_fog.rgb, v_fog.a);

    gl_FragColor = vec4(color, tex.a * v_color0.a);
}
