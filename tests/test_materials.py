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
