import tempfile
import unittest
from pathlib import Path
from unittest.mock import patch

from tool import materials
from tool.interface import inspect_material


class MaterialInputTests(unittest.TestCase):
    def test_material_json_suffix_is_matched_correctly(self):
        with tempfile.TemporaryDirectory() as temp:
            base_dir = Path(temp) / "base"
            base_dir.mkdir()
            (base_dir / "Sky.material.json").write_text("{}", encoding="utf-8")
            (base_dir / "RenderChunk.material.bin").write_bytes(b"placeholder")

            with patch.object(materials, "BASE_MATERIALS", base_dir):
                self.assertEqual(
                    materials.available_base_materials(),
                    {"Sky", "RenderChunk"},
                )

    def test_build_selection_skips_only_missing_base_inputs(self):
        with tempfile.TemporaryDirectory() as temp:
            base_dir = Path(temp) / "base"
            base_dir.mkdir()
            (base_dir / "Sky.material.bin").write_bytes(b"placeholder")

            with patch.object(materials, "BASE_MATERIALS", base_dir):
                buildable, missing = materials.select_buildable_materials(
                    ["Sky", "CloudsForwardPBR"]
                )

            self.assertEqual(buildable, ["Sky"])
            self.assertEqual(missing, ["CloudsForwardPBR"])

    def test_source_materials_are_not_hidden_by_missing_base_files(self):
        with tempfile.TemporaryDirectory() as temp:
            source_dir = Path(temp) / "materials"
            source_dir.mkdir()
            (source_dir / "CloudsForwardPBR").mkdir()
            (source_dir / "RenderChunk").mkdir()

            with patch.object(materials, "SOURCE_ROOT", source_dir):
                self.assertEqual(
                    materials.material_names(),
                    ["CloudsForwardPBR", "RenderChunk"],
                )

    def test_renderchunk_position_inputs_are_linked_across_stages(self):
        info = inspect_material("RenderChunk")
        varying_names = {item["name"] for item in info["varyings"]}

        self.assertIn("v_position", varying_names)
        self.assertIn("v_viewVector", varying_names)
        self.assertIn("v_position", info["vertex_outputs"])
        self.assertIn("v_position", info["fragment_inputs"])
        self.assertIn("v_viewVector", info["vertex_outputs"])
        self.assertIn("v_viewVector", info["fragment_inputs"])

    def test_renderchunk_shadow_is_project_local_and_configurable(self):
        root = Path(__file__).resolve().parents[1]
        header = root / "include" / "renderer" / "effects" / "player_shadow.h"
        vertex = root / "src" / "materials" / "RenderChunk" / "vertex.sc"
        fragment = root / "src" / "materials" / "RenderChunk" / "fragment.sc"

        header_text = header.read_text(encoding="utf-8")
        vertex_text = vertex.read_text(encoding="utf-8")
        fragment_text = fragment.read_text(encoding="utf-8")

        self.assertIn("rdPlayerShadowCoverage", header_text)
        self.assertIn("rdPlayerShadowCoverage", fragment_text)
        self.assertIn("RD_PLAYER_SHADOW_ENABLED", header_text)
        self.assertIn("RD_PLAYER_SHADOW_TINT", header_text)
        self.assertNotIn("NL_", header_text + vertex_text + fragment_text)
        self.assertIn("v_shadowData", vertex_text)
        self.assertIn("CameraPosition.xyz - LastCameraPosition.xyz", vertex_text)
        self.assertIn("-v_viewVector", fragment_text)

    def test_renderchunk_has_independent_surface_passes(self):
        root = Path(__file__).resolve().parents[1]
        folder = root / "src" / "materials" / "RenderChunk"
        vertex = (folder / "vertex.sc").read_text(encoding="utf-8")
        fragment = (folder / "fragment.sc").read_text(encoding="utf-8")
        config = (folder / "config.json").read_text(encoding="utf-8")
        style = (
            root / "include" / "renderer" / "effects" / "surface_style.h"
        ).read_text(encoding="utf-8")

        self.assertIn("mtxFromCols", vertex)
        self.assertIn("RENDER_AS_BILLBOARDS", vertex)
        self.assertIn("ALPHA_TEST", fragment)
        self.assertIn("DEPTH_ONLY", fragment)
        self.assertIn("s_SeasonsTexture", fragment)
        self.assertIn("rdEnvironmentLight", fragment)
        self.assertIn("rdWaterWave", fragment)
        self.assertIn("RD_WATER_REFLECTION", fragment)
        self.assertIn("rdSoftColorGrade", fragment)
        self.assertIn("RD_STYLE_DAY_TINT", style)
        for engine_variant in ("Instancing", "RenderAsBillboards", "Seasons", "DepthOnly"):
            self.assertIn(engine_variant, config)

    def test_material_specific_uniform_overrides_are_preserved(self):
        info = inspect_material("EndSky")
        self.assertEqual(
            info["uniform_overrides"],
            {"SkyColor": "BUILTIN_MinecraftFogColor"},
        )

    def test_interface_rejects_parent_directory_traversal(self):
        with self.assertRaises(ValueError):
            inspect_material("../tool")


if __name__ == "__main__":
    unittest.main()
