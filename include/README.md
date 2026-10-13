Put shared shader headers here, for example a BGFX `bgfx_shader.sh` file that
your shader source is licensed to use.

The `renderer/` headers in this project provide small typed helpers for
RenderDragon time, camera, and environment uniforms. Include them after the
BGFX shader header:

```glsl
#include "bgfx_shader.sh"
#include <renderer/main.h>

RendererCamera camera = rendererGetCamera();
RendererEnvironment environment = rendererGetEnvironment();
float animationTime = rendererAnimationTime();
```

The builder supplies default `uniform_overrides` metadata when a material does
not have its own `material.json`. A material with custom metadata remains
responsible for mapping every engine uniform it uses. These helpers cannot add
runtime values that the target game or loader does not supply.
