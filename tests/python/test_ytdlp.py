import os
import sys
import unittest

ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), "../../src/main/python"))
sys.path.insert(0, ROOT)

from ytdlp.ytdlp import verifyArgs  # type: ignore


class TestYtdlp(unittest.TestCase):

    def test_verify_args_print_title(self):
        self.assertTrue(verifyArgs(["--print-title", "https://www.youtube.com/watch?v=ABCDEFGHIJK"]))

    def test_verify_args_debug_mode(self):
        self.assertTrue(verifyArgs(["--debug", "https://www.youtube.com/watch?v=ABCDEFGHIJK", "output.mp3", "mp3"]))

    def test_verify_args_basic_download(self):
        self.assertTrue(verifyArgs(["https://www.youtube.com/watch?v=ABCDEFGHIJK", "output.mp4", "mp4"]))

    def test_verify_args_invalid(self):
        self.assertFalse(verifyArgs(["https://www.youtube.com/watch?v=ABCDEFGHIJK"]))


if __name__ == "__main__":
    unittest.main()
