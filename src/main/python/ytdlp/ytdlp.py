from pydub import AudioSegment # type: ignore
from pytubefix import YouTube # type: ignore

from typing import Optional, Tuple

import sys
import os

import logging

class YouTubeDownloader:
    def __init__(self) -> None:
        """Initialize the YouTube downloader with logging setup"""
        self.logger = self._setup_logger()
    
    def _setup_logger(self) -> logging.Logger:
        """Configure the logging system"""
        logging.basicConfig(
            level=logging.INFO,
            format='%(asctime)s - %(levelname)s - %(message)s'
        )
        return logging.getLogger(__name__)
    
    def _ensure_directory_exists(self, path: str) -> None:
        """Ensure the target directory exists"""
        dir_path = os.path.dirname(path) if os.path.dirname(path) else '.'
        os.makedirs(dir_path, exist_ok=True)
        self.logger.debug(f"Directory verified/created: {dir_path}")
    
    def _parse_filename(self, full_path: str) -> Tuple[str, str]:
        """Split full path into directory and filename components"""
        dir_path = os.path.dirname(full_path) if os.path.dirname(full_path) else '.'
        file_name = os.path.basename(full_path)
        return (dir_path, file_name)
    
    def _download_audio(self, yt: YouTube, output_path: str, file_name: str) -> str:
        """
        Download and convert audio to MP3 with specific settings
        
        Args:
            yt: YouTube object
            output_path: Output directory path
            file_name: Output filename with .mp3 extension
            
        Returns:
            Path to the downloaded file
            
        Raises:
            Exception: If any error occurs during download/conversion
        """
        try:
            self.logger.info("Starting audio download...")
            
            # Create temp directory if it doesn't exist
            temp_dir = "temp_downloads"
            os.makedirs(temp_dir, exist_ok=True)
            
            # Download audio (best available quality)
            audio_stream = yt.streams.get_audio_only()
            temp_file = os.path.join(temp_dir, "temp_audio.m4a")
            audio_stream.download(output_path=temp_dir, filename="temp_audio.m4a") # type: ignore
            
            # Convert to MP3 with specific settings
            self.logger.info("Converting to MP3 (32000Hz, 180kbps)...")
            sound: AudioSegment = AudioSegment.from_file(temp_file)
            sound = sound.set_frame_rate(32000)
            
            output_file = os.path.join(output_path, file_name)
            sound.export(output_file, format="mp3", bitrate="180k")
            
            # Remove temp file
            os.remove(temp_file)
            self.logger.info(f"Audio successfully converted: {output_file}")
            return output_file
            
        except Exception as e:
            self.logger.error(f"Error processing audio: {e}")
            raise
    
    def _download_video(self, yt: YouTube, output_path: str, file_name: str) -> str:
        """
        Download video in MP4 format (highest resolution)
        
        Args:
            yt: YouTube object
            output_path: Output directory path
            file_name: Output filename with .mp4 extension
            
        Returns:
            Path to the downloaded file
            
        Raises:
            Exception: If any error occurs during download
        """
        try:
            self.logger.info("Starting video download...")
            stream = yt.streams.get_highest_resolution()
            output_file = stream.download(output_path=output_path, filename=file_name) # type: ignore
            self.logger.info(f"Video successfully downloaded: {output_file}")
            return output_file # type: ignore
        except Exception as e:
            self.logger.error(f"Error downloading video: {e}")
            raise
    
    def download(
        self,
        url: str,
        output_full_path: str,
        format_type: str
    ) -> Optional[str]:
        """
        Download YouTube content in specified format
        
        Args:
            url: YouTube video URL
            output_full_path: Full output path including filename and extension
            format_type: Output format (mp3 or mp4)
            
        Returns:
            Full path to downloaded file or None if error occurs
        """
        try:
            self.logger.info(f"Starting download from: {url}")
            
            # Validate and prepare paths
            self._ensure_directory_exists(output_full_path)
            output_path, file_name = self._parse_filename(output_full_path)
            
            # Create YouTube object
            yt: YouTube = YouTube(url, "WEB")
            self.logger.info(f"Video title: {yt.title}")
            
            # Process based on format using match case (Python 3.10+)
            match format_type.lower():
                case "mp3":
                    return self._download_audio(yt, output_path, file_name)
                case "mp4":
                    return self._download_video(yt, output_path, file_name)
                case _:
                    raise ValueError(f"Unsupported format: {format_type}")
                    
        except Exception as e:
            self.logger.error(f"Download error: {e}")
            return None

def viewVideoTitle(url: str) -> str:
    """
    Return the title of a YouTube video given its URL.

    Args:
        url: URL of the YouTube video

    Returns:
        Title of the YouTube video
    """
    
    yt: YouTube = YouTube(url)
    
    return yt.title

def verifyArgs(args) -> bool:
    
    if args[0] == "--print-title" and len(args) == 2:
        return True
    
    if args[0] == "--debug" and len(args) == 4 and (args[3] == "mp3" or args[3] == "mp4"):
        return True
    
    if len(args) == 3 and (args[2] == "mp3" or args[2] == "mp4"):
        return True
    
    return False

def main() -> None:
    """
    Command-line interface to the YouTube Downloader.
    
    Downloads a YouTube video or converts to audio (MP3) given a URL and optional output path.
    
    Supported formats:
        - MP3 (audio only)
        - MP4 (video)
    
    If the --print-title flag is specified, the video title and info will be printed and no download
    will occur.
    
    If the --debug flag is specified, detailed logging will be enabled.
    """
    
    totalArgs = sys.argv[1:]
    
    
    downloader: YouTubeDownloader = YouTubeDownloader()
    
    if not verifyArgs(totalArgs):
        raise ValueError("Invalid arguments")
    
    if totalArgs[0] == "--debug":
        downloader.logger.setLevel(logging.DEBUG)
        downloader.logger.debug("Debug mode enabled")
    
    if totalArgs[0] == "--print-title":
        print(viewVideoTitle(totalArgs[1]))
        return
    
    result: Optional[str] = downloader.download(
        url=totalArgs[0] if len(totalArgs) == 3 else totalArgs[1],
        output_full_path=totalArgs[1] if len(totalArgs) == 3 else totalArgs[2],
        format_type=totalArgs[-1]
    )
    
    if not result:
        print("Download failed. Check logs for details.")
        sys.exit(1)
    
    print(f"Successfully downloaded: {result}")
    sys.exit(0)


if __name__ == "__main__":
    main()