$input v_worldPos, v_underwaterRainTimeDay

#include <bgfx_shader.sh>
#include <renderer/main.h>

#define u_time rendererAnimationTime()

const float CLOUD_BASE_Y = 1.2;
const float CLOUD_THICKNESS = 0.45;

const vec3 SUN_DIRECTION =
    vec3(0.55, 0.45, 0.70);

const vec3 COLOR_SUNLIGHT =
    vec3(1.00, 0.94, 0.86);

const vec3 COLOR_SKY_ZENITH =
    vec3(0.12, 0.38, 0.88);

const vec3 COLOR_SKY_HORIZON =
    vec3(0.68, 0.82, 0.96);

const vec3 COLOR_AMBIENT =
    vec3(0.52, 0.60, 0.78);

const vec3 COLOR_SHADOW =
    vec3(0.24, 0.28, 0.42);


// ---------------------------------------------------------
// HASH
// ---------------------------------------------------------

float hash21(vec2 p)
{
    p = fract(p * vec2(234.34, 435.345));
    p += dot(p, p + 34.23);
    return fract(p.x * p.y);
}


// ---------------------------------------------------------
// NOISE
// ---------------------------------------------------------

float smoothNoise(vec2 p)
{
    vec2 i = floor(p);
    vec2 f = fract(p);

    vec2 u =
        f * f *
        (3.0 - 2.0 * f);

    return mix(
        mix(
            hash21(i),
            hash21(i + vec2(1.0, 0.0)),
            u.x
        ),
        mix(
            hash21(i + vec2(0.0, 1.0)),
            hash21(i + vec2(1.0, 1.0)),
            u.x
        ),
        u.y
    );
}


// ---------------------------------------------------------
// FBM
// ---------------------------------------------------------

float fastOrganicFBM(vec2 p)
{
    vec2 warp =
        vec2(
            smoothNoise(p * 1.5),
            smoothNoise(p * 1.5 + 4.3)
        ) * 0.3;

    p += warp;

    float total =
        0.65 * smoothNoise(p);

    p =
        mat2(
            0.8, 0.6,
           -0.6, 0.8
        ) * p * 2.1 +
        vec2(1.7, 9.2);

    total +=
        0.35 * smoothNoise(p);

    return total;
}


// ---------------------------------------------------------
// CLOUD DENSITY
// ---------------------------------------------------------

float sampleCloudDensity(vec3 p, float t)
{
    vec2 uv =
        p.xz * 0.12 +
        vec2(
            t * 0.025,
            t * 0.012
        );

    float n =
        fastOrganicFBM(uv);

    float h =
        clamp(
            (p.y - CLOUD_BASE_Y) /
            CLOUD_THICKNESS,
            0.0,
            1.0
        );

    float profile =
        smoothstep(0.0, 0.25, h) *
        (1.0 - smoothstep(0.70, 1.0, h));

    return
        smoothstep(0.38, 0.72, n) *
        profile;
}


// ---------------------------------------------------------
// CLOUD RAYMARCH
// ---------------------------------------------------------

vec4 marchClouds(
    vec3 rayDir,
    float t,
    bool fastMode
)
{
    if (rayDir.y <= 0.01)
        return vec4(0.0);

    float horizonFade =
        smoothstep(
            0.01,
            0.22,
            rayDir.y
        );

    const int MAX_STEPS = 14;

    int steps =
        fastMode ? 6 : 14;

    float stepSize =
        CLOUD_THICKNESS /
        float(steps);

    vec3 lightDir =
        normalize(SUN_DIRECTION);

    vec3 cloudColor =
        vec3(0.0);

    float transmittance =
        1.0;

    for (int i = 0; i < MAX_STEPS; i++)
    {
        if (i >= steps)
            break;

        float currH =
            CLOUD_BASE_Y +
            stepSize * float(i);

        float dist =
            currH /
            max(rayDir.y, 0.03);

        if (dist > 70.0)
            break;

        vec3 pos =
            rayDir * dist;

        float density =
            sampleCloudDensity(
                pos,
                t
            );

        if (density > 0.005)
        {
            float absorb =
                clamp(
                    density * 1.6,
                    0.0,
                    1.0
                );

            float height =
                float(i) /
                float(steps);

            float sunPhase =
                pow(
                    max(
                        dot(
                            rayDir,
                            lightDir
                        ),
                        0.0
                    ),
                    4.0
                );

            vec3 scatter =
                mix(
                    COLOR_SHADOW,
                    COLOR_AMBIENT,
                    height
                );

            scatter =
                mix(
                    scatter,
                    COLOR_SUNLIGHT,
                    height *
                    (0.5 + 0.5 * sunPhase)
                );

            float alpha =
                absorb *
                transmittance;

            cloudColor +=
                scatter * alpha;

            transmittance *=
                1.0 - absorb;

            if (transmittance < 0.03)
                break;
        }
    }

    return vec4(
        cloudColor,
        clamp(
            (1.0 - transmittance) *
            horizonFade,
            0.0,
            1.0
        )
    );
}


// ---------------------------------------------------------
// WATER
// ---------------------------------------------------------

vec3 getWaterNormal(
    vec2 uv,
    float t
)
{
    vec2 p = uv * 6.0;
    float t1 = t * 1.2;

    float waveX =
        cos(p.x * 2.0 + t1) * 2.0 -
        sin(p.x * 2.5 - t1 * 0.8) * 2.5;

    float waveY =
        cos(p.y * 1.5 + t1) * 1.5 +
        sin(p.y * 1.8 - t1 * 0.8) * 1.8;

    vec2 grad =
        vec2(waveX, waveY) * 0.02;

    return normalize(
        vec3(
            -grad.x,
            1.0,
            -grad.y
        )
    );
}


// ---------------------------------------------------------
// SKY
// ---------------------------------------------------------

vec3 evaluateSky(
    vec3 vdir,
    vec3 sunDir
)
{
    float zenith =
        clamp(vdir.y, 0.0, 1.0);

    float dayFactor =
        clamp(
            v_underwaterRainTimeDay.w,
            0.0,
            1.0
        );

    vec3 sky =
        mix(
            COLOR_SKY_HORIZON,
            COLOR_SKY_ZENITH,
            pow(zenith, 0.7)
        );

    sky *=
        mix(
            0.25,
            1.0,
            dayFactor
        );

    float sunDot =
        max(
            dot(vdir, sunDir),
            0.0
        );

    vec3 sun =
        COLOR_SUNLIGHT *
        pow(sunDot, 512.0) *
        1.2;

    vec3 halo =
        vec3(1.0, 0.8, 0.5) *
        pow(sunDot, 16.0) *
        0.25;

    sun *= dayFactor;
    halo *= mix(0.15, 1.0, dayFactor);

    return sky + sun + halo;
}


// ---------------------------------------------------------
// SCENE
// ---------------------------------------------------------

vec3 renderScene(
    vec3 rayDir,
    float t
)
{
    vec3 sunDir =
        normalize(SUN_DIRECTION);

    vec3 sky =
        evaluateSky(
            rayDir,
            sunDir
        );

    if (rayDir.y >= 0.0)
    {
        vec4 clouds =
            marchClouds(
                rayDir,
                t,
                false
            );

        return mix(
            sky,
            clouds.rgb,
            clouds.a
        );
    }

    vec2 oceanUV =
        rayDir.xz /
        max(-rayDir.y, 0.01);

    vec3 normal =
        getWaterNormal(
            oceanUV,
            t
        );

    vec3 reflectDir =
        reflect(
            rayDir,
            normal
        );

    reflectDir.y =
        max(
            reflectDir.y,
            0.02
        );

    vec3 reflection =
        evaluateSky(
            reflectDir,
            sunDir
        );

    vec4 reflectedClouds =
        marchClouds(
            reflectDir,
            t,
            true
        );

    reflection =
        mix(
            reflection,
            reflectedClouds.rgb,
            reflectedClouds.a
        );

    float fresnel =
        0.04 +
        0.96 *
        pow(
            1.0 -
            max(
                dot(
                    -rayDir,
                    normal
                ),
                0.0
            ),
            3.0
        );

    vec3 deepWater =
        vec3(0.04, 0.18, 0.40);

    return mix(
        deepWater,
        reflection,
        clamp(
            fresnel * 0.85 + 0.15,
            0.0,
            1.0
        )
    );
}


// ---------------------------------------------------------
// MAIN
// ---------------------------------------------------------

void main()
{
    /*
     * Real RenderDragon animation time:
     *
     * RendererViewPositionAndTime.w
     */
    float t =
        u_time;

    /*
     * Minecraft Sky geometry gives the direction.
     * No Shadertoy mouse/touch camera.
     */
    vec3 rayDir =
        normalize(v_worldPos);

    vec3 sceneColor =
        renderScene(
            rayDir,
            t
        );

    /*
     * Filmic tone mapping.
     */
    sceneColor =
        (
            sceneColor *
            (
                2.2 * sceneColor +
                0.1
            )
        )
        /
        (
            sceneColor *
            (
                2.2 * sceneColor +
                0.8
            )
            +
            0.06
        );

    gl_FragColor =
        vec4(
            clamp(
                sceneColor,
                0.0,
                1.0
            ),
            1.0
        );
}