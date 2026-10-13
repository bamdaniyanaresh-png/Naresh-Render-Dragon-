#ifndef RENDERER_MATH_H
#define RENDERER_MATH_H

float rendererSaturate(float x)
{
    return clamp(x, 0.0, 1.0);
}

vec2 rendererSaturate(vec2 x)
{
    return clamp(x, vec2(0.0), vec2(1.0));
}

vec3 rendererSaturate(vec3 x)
{
    return clamp(x, vec3(0.0), vec3(1.0));
}

float rendererRemap(float value, float inMin, float inMax,
                    float outMin, float outMax)
{
    float range = inMax - inMin;

    if (abs(range) < 0.000001)
        return outMin;

    return mix(
        outMin,
        outMax,
        (value - inMin) / range
    );
}

#endif
