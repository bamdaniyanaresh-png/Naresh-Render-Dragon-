#ifndef RENDERER_ENVIRONMENT_H
#define RENDERER_ENVIRONMENT_H

/*
 * Built-in material uniforms use vec4 declarations. Components are unpacked
 * below to keep call sites concise while matching the RenderDragon ABI.
 */
uniform vec4 Day;
uniform vec4 TimeOfDay;
uniform vec4 SunDirection;
uniform vec4 FogColor;
uniform vec4 CloudColor;

struct RendererEnvironment
{
    float timeOfDay;
    float day;

    float dayFactor;
    float nightFactor;
    float sunsetFactor;

    vec3 sunDirection;
    vec3 moonDirection;

    vec3 horizonColor;
    vec3 zenithColor;

    vec3 fogColor;
    vec3 cloudColor;
};

RendererEnvironment rendererGetEnvironment()
{
    RendererEnvironment env;

    env.timeOfDay = clamp(TimeOfDay.x, 0.0, 1.0);
    env.day = Day.x;

    env.sunDirection = normalize(SunDirection.xyz);

    env.dayFactor = clamp(
        env.sunDirection.y * 0.5 + 0.5,
        0.0,
        1.0
    );

    env.nightFactor = 1.0 - env.dayFactor;

    /*
     * Stronger around sunrise/sunset.
     */
    env.sunsetFactor =
        1.0 - abs(env.sunDirection.y);

    env.horizonColor = FogColor.rgb;
    env.zenithColor = FogColor.rgb;

    env.fogColor = FogColor.rgb;
    env.cloudColor = CloudColor.rgb;

    env.moonDirection = -env.sunDirection;

    return env;
}

#endif
