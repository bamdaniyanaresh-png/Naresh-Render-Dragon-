// CloudsForwardPBR vertex shader entry point.
// Replace this file with your BGFX/RenderDragon vertex.sc.

$input a_position
$output v_texcoord0

#include "bgfx_shader.sh"

void main()
{
    gl_Position = vec4(a_position, 1.0);
    v_texcoord0 = vec2(0.0, 0.0);
}
