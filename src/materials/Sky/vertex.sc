$input a_position
$output v_worldPos, v_underwaterRainTimeDay

#include <bgfx_shader.sh>
#include <renderer/main.h>

uniform mat4 u_modelViewProj;
uniform vec4 FogColor;
uniform vec4 FogAndDistanceControl;
uniform vec4 ViewPositionAndTime;

void main()
{
    gl_Position = mul(
        u_modelViewProj,
        vec4(a_position, 1.0)
    );

    v_worldPos = a_position;

    v_underwaterRainTimeDay.x =
        float(detectUnderwater(
            FogColor.rgb,
            FogAndDistanceControl.xy
        ));

    v_underwaterRainTimeDay.y =
        detectRain(
            FogAndDistanceControl.xyz
        );

    // RenderDragon animation clock
    v_underwaterRainTimeDay.z =
        ViewPositionAndTime.w;

    v_underwaterRainTimeDay.w =
        detectDayFactor(
            FogColor.rgb
        );
}