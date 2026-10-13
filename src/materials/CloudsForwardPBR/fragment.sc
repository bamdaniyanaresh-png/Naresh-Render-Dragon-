// CloudsForwardPBR fragment shader entry point.
// Replace this file with your BGFX/RenderDragon fragment.sc.
//
// This project intentionally does not bundle proprietary Minecraft
// material data or another creator's shader implementation.

$input v_texcoord0

#include "bgfx_shader.sh"

void main()
{
    gl_FragColor = vec4(1.0, 1.0, 1.0, 1.0);
}
