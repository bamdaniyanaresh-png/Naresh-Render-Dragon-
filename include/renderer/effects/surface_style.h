#ifndef RENDERER_EFFECTS_SURFACE_STYLE_H
#define RENDERER_EFFECTS_SURFACE_STYLE_H

#ifndef RD_STYLE_DAY_TINT
#define RD_STYLE_DAY_TINT vec3(1.04, 1.01, 0.95)
#endif

#ifndef RD_STYLE_NIGHT_TINT
#define RD_STYLE_NIGHT_TINT vec3(0.80, 0.87, 1.08)
#endif

#ifndef RD_STYLE_SUNSET_TINT
#define RD_STYLE_SUNSET_TINT vec3(1.13, 0.84, 0.66)
#endif

#ifndef RD_STYLE_AMBIENT_FLOOR
#define RD_STYLE_AMBIENT_FLOOR 0.045
#endif

#ifndef RD_STYLE_SATURATION
#define RD_STYLE_SATURATION 1.045
#endif

#ifndef RD_STYLE_HIGHLIGHT_ROLLOFF
#define RD_STYLE_HIGHLIGHT_ROLLOFF 0.10
#endif

#ifndef RD_WATER_TINT
#define RD_WATER_TINT vec3(0.56, 0.79, 0.91)
#endif

#ifndef RD_WATER_WAVE_RATE
#define RD_WATER_WAVE_RATE 1.35
#endif

#ifndef RD_WATER_SHEEN
#define RD_WATER_SHEEN 0.12
#endif

#ifndef RD_WATER_REFLECTION
#define RD_WATER_REFLECTION 0.24
#endif

#ifndef RD_WATER_SPECULAR
#define RD_WATER_SPECULAR 0.18
#endif

vec3 rdEnvironmentLight(vec3 sampledLight, vec3 sunDirection)
{
    float sunY = clamp(sunDirection.y, -1.0, 1.0);
    float daylight = smoothstep(-0.16, 0.48, sunY);
    float horizonBand = 1.0 - smoothstep(0.035, 0.42, abs(sunY));

    vec3 tint = mix(RD_STYLE_NIGHT_TINT, RD_STYLE_DAY_TINT, daylight);
    tint = mix(tint, RD_STYLE_SUNSET_TINT, horizonBand * 0.18);

    /*
     * Keep the supplied lightmap as the primary lighting source. This layer
     * only adds a restrained environmental color response and minimum fill.
     */
    vec3 light = max(sampledLight, vec3(RD_STYLE_AMBIENT_FLOOR));
    return light * tint;
}

vec3 rdSoftColorGrade(vec3 color)
{
    color = max(color, vec3(0.0));

    float luminance = dot(color, vec3(0.2126, 0.7152, 0.0722));
    color = mix(vec3(luminance), color, RD_STYLE_SATURATION);

    /*
     * A small rational shoulder limits hot highlights without flattening
     * the midtones of the texture/lightmap result.
     */
    return color / (vec3(1.0) + color * RD_STYLE_HIGHLIGHT_ROLLOFF);
}

float rdWaterVertexMask(vec4 vertexColor)
{
    float blueSurface = smoothstep(0.30, 0.62, vertexColor.b);
    float translucentSurface = 1.0 - smoothstep(0.76, 0.97, vertexColor.a);
    return blueSurface * translucentSurface;
}

float rdWaterWave(vec3 position, float clock)
{
    float broad = sin(dot(position.xz, vec2(0.58, 0.37)) + clock * RD_WATER_WAVE_RATE);
    float cross = sin(dot(position.xz, vec2(-0.24, 0.71)) - clock * RD_WATER_WAVE_RATE * 0.73);
    return (broad * 0.62 + cross * 0.38);
}

float rdWaterFresnel(vec3 surfaceToCamera)
{
    float lengthToCamera = max(length(surfaceToCamera), 0.0001);
    float viewElevation = clamp(abs(surfaceToCamera.y / lengthToCamera), 0.0, 1.0);
    float grazing = 1.0 - viewElevation;
    float grazing2 = grazing * grazing;
    return grazing2 * grazing2 * grazing;
}

#endif
