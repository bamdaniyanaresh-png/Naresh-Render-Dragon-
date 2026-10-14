# Material build coverage

The builder compiles a material only when both its shader-source directory and
a matching base material input are present.

At the time this source bundle was updated:

- 11 shader-source directories are present.
- 8 have matching base `.material.bin` inputs and are eligible to build.
- `CloudsForwardPBR`, `Extra`, and `ItemInHandTextured` have source folders but
  no matching base material input.
- The supplied base-material folder has 21 `.material.bin` files. The other 13
  base names do not currently have matching source folders, so they cannot be
  rebuilt from this source tree.

The base binaries are merge inputs, not newly compiled outputs. Adding a base
binary alone does not provide the missing vertex and fragment implementations.
`python -m tool list` reports current source/base matching.

## Verified outputs

- Android / ESSL 3.10: all 8 eligible materials compiled successfully to
  `build/android/`.
- iOS / Metal: the updated `RenderChunk` material compiled successfully to
  `build/ios/RenderChunk.material.bin`.
- The complete iOS material set is not yet verified: the separate `Sky`
  fragment fails Metal compilation because its varying is unresolved.
- Windows / merged: not built. The Linux `shaderc` binary used here does not
  include the HLSL compiler backend required for the Windows profile.

The supplied base materials use material format version 26, so this project
pins Lazurite 0.10.0. To reproduce the Android build, install the Python
requirements and a compatible BGFX `shaderc`, then run:

```bash
./build.sh mats -p android
```

To rebuild only the principal RenderChunk pass for Metal:

```bash
./build.sh mats -p ios -m RenderChunk
```

Outputs are accepted as successful only after the expected `.material.bin`
files are present. The remaining unsupported profiles require matching
compiler backends; missing source/base inputs require separate material
implementations or base material files.
