#ifndef RENDERER_CAMERA_H
#define RENDERER_CAMERA_H

/*
 * RenderDragon built-in values are declared as vec4 in material metadata.
 * Use .xyz for their position/direction components.
 */
uniform vec4 CameraPosition;
uniform vec4 CameraFacingDirection;
uniform vec4 LastCameraPosition;
uniform vec4 LastCameraFacingDirection;

struct RendererCamera
{
    vec3 position;
    vec3 facingDirection;
    vec3 lastPosition;
    vec3 lastFacingDirection;
};

RendererCamera rendererGetCamera()
{
    RendererCamera camera;

    camera.position = CameraPosition.xyz;
    camera.facingDirection = normalize(CameraFacingDirection.xyz);
    camera.lastPosition = LastCameraPosition.xyz;
    camera.lastFacingDirection = normalize(LastCameraFacingDirection.xyz);

    return camera;
}

#endif
