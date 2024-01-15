package com.codef.videoresampler;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jcodec.api.FrameGrab;
import org.jcodec.common.model.Picture;

import com.codef.xsalt.utils.XSaLTFileSystemUtils;

public class FixVideoResampler {

	// https://www.wearethefirehouse.com/aspect-ratio-cheat-sheet

	private static final boolean ENABLE_MAIN_METHOD = true;

	private static final Logger LOGGER = LogManager.getLogger(FixVideoResampler.class.getName());

	private static final String FFMPEG_BIN_COMMAND = ".\\";
	private static final String FFMPEG_BIN_FOLDER = "C:\\Program Files\\FFMPEG1";

	private static final String SCAN_FOLDER = "E:\\Pictures\\EventsFix";
	private static final String FIX_DRIVE = "E:\\";
	private static final String FIX_STUFF_FOLDER = "FixStuff";

	// NO TOUCHEE!

	private static final String FVR_DIMENSIONS_FILE_NAME = FIX_DRIVE + FIX_STUFF_FOLDER + "\\FVR_Dimensions.tab";

	private static final String FVR_FILES_NORMAL_NAME = FIX_DRIVE + FIX_STUFF_FOLDER + "\\FVR_Files_Normal.tab";
	private static final String FVR_FILES_SMALL_NAME = FIX_DRIVE + FIX_STUFF_FOLDER + "\\FVR_Files_Small.tab";
	private static final String FVR_FILES_PROBLEMS_NAME = FIX_DRIVE + FIX_STUFF_FOLDER + "\\FVR_Files_Problems.tab";

	private static final String FVR_FILES_NORMAL_COPY_NAME = FIX_DRIVE + FIX_STUFF_FOLDER + "\\FVR_Copy_Normal.ps1";
	private static final String FVR_FILES_PROBLEMS_COPY_NAME = FIX_DRIVE + FIX_STUFF_FOLDER + "\\FVR_Copy_Problems.ps1";

	private static final String FVR_PROBLEM_FILES_NAME_FIX = FIX_DRIVE + FIX_STUFF_FOLDER + "\\FVR_Fix_Problems.ps1";
	private static final String FVR_NORMAL_FILES_NAME_FIX = FIX_DRIVE + FIX_STUFF_FOLDER + "\\FVR_Fix_Normal.ps1";

	private static StringBuilder normalFileBuffer = new StringBuilder();
	private static StringBuilder normalFileCopyBuffer = new StringBuilder();
	private static StringBuilder normalFileFixBuffer = new StringBuilder();

	private static StringBuilder tooSmallBuffer = new StringBuilder();

	private static StringBuilder problemFileBuffer = new StringBuilder();
	private static StringBuilder problemFileCopyBuffer = new StringBuilder();
	private static StringBuilder problemFileFixBuffer = new StringBuilder();

	private static TreeSet<String> dimensionsFound = new TreeSet<String>();

	private static boolean MAKE_COPY_OF_FILES = true;

	public static void main(String[] args) throws IOException {

		if (ENABLE_MAIN_METHOD) {

			problemFileFixBuffer.append("cd \"" + FFMPEG_BIN_FOLDER + "\"" + "\n");
			normalFileFixBuffer.append("cd \"" + FFMPEG_BIN_FOLDER + "\"" + "\n");

			createFileFolder(FIX_DRIVE + FIX_STUFF_FOLDER);
			startVisit(SCAN_FOLDER);

			XSaLTFileSystemUtils.writeStringBuilderToFile(normalFileBuffer, FVR_FILES_NORMAL_NAME);
			XSaLTFileSystemUtils.writeStringBuilderToFile(tooSmallBuffer, FVR_FILES_SMALL_NAME);
			XSaLTFileSystemUtils.writeStringBuilderToFile(problemFileBuffer, FVR_FILES_PROBLEMS_NAME);

			if (MAKE_COPY_OF_FILES) {
				XSaLTFileSystemUtils.writeStringBuilderToFile(normalFileCopyBuffer, FVR_FILES_NORMAL_COPY_NAME);
				XSaLTFileSystemUtils.writeStringBuilderToFile(problemFileCopyBuffer, FVR_FILES_PROBLEMS_COPY_NAME);
			}

			XSaLTFileSystemUtils.writeStringBuilderToFile(problemFileFixBuffer, FVR_PROBLEM_FILES_NAME_FIX);
			XSaLTFileSystemUtils.writeStringBuilderToFile(normalFileFixBuffer, FVR_NORMAL_FILES_NAME_FIX);

			writeSet(dimensionsFound, FVR_DIMENSIONS_FILE_NAME);
		}

	}

	public static void startVisit(String filePath) {
		visitFiles(Paths.get(filePath));
	}

	private static void visitFiles(Path path) {
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
			for (Path entry : stream) {
				if (entry.toFile().isDirectory()) {
					visitFiles(entry);
				} else {
					visitFileCode(entry.toString());
				}
			}
		} catch (IOException e) {
			LOGGER.error(e.toString(), e);
		}
	}

	public static void visitFileCode(String filePath) {
		if (filePath.toLowerCase().endsWith(".mp4") && !filePath.toLowerCase().contains("_fixed")) {

			try {
				File videoFile = new File(filePath);
				Picture frame = FrameGrab.getFrameFromFile(videoFile, 0);

				int width = frame.getWidth();
				int height = frame.getHeight();
				String dimensions = "w" + width + " x h" + height;
				String orientation = width > height ? "land" : "port";

				if (width > 720 || height > 720) {
					handleFixFile(filePath, dimensions, orientation);
				} else {
					// too small
					handleTooSmallFile(filePath, dimensions, orientation);
				}

			} catch (Exception e) {
				// bad file
				handleBadFile(filePath, e);
			}
		}
	}

	public static void handleFixFile(String filePath, String dimensions, String orientation) {

		String line = filePath.substring(filePath.length() - 3).toLowerCase() + "\t" + filePath + "\t" + dimensions
				+ "\t" + orientation + "\n";
		System.out.println(line.substring(0, line.length() - 1));

		dimensionsFound.add(dimensions + "\t" + orientation);
		normalFileBuffer.append(line);

		handleFolderForFile(filePath, normalFileCopyBuffer);
		handleFfMpegCommand(filePath, dimensions, orientation, normalFileFixBuffer);

	}

	public static void handleTooSmallFile(String filePath, String dimensions, String orientation) {

		String line = filePath.substring(filePath.length() - 3).toLowerCase() + "\t" + filePath + "\t" + dimensions
				+ "\t" + orientation + "\n";
		System.out.println(line.substring(0, line.length() - 1));
		tooSmallBuffer.append(line);

	}

	public static void handleBadFile(String filePath, Exception e) {

		String line = filePath.substring(filePath.length() - 3).toLowerCase() + "\t" + filePath + "\t" + e.toString()
				+ "\n";
		System.out.println(line.substring(0, line.length() - 1));
		problemFileBuffer.append(line);

		handleFolderForFile(filePath, problemFileCopyBuffer);
		handleFfMpegCommand(filePath, "N/A", "land", problemFileFixBuffer);

	}

	public static void handleFfMpegCommand(String filePath, String dimensions, String orientation, StringBuilder sb) {

		String originalFilePath = filePath;

		if (MAKE_COPY_OF_FILES) {
			filePath = filePath.replace(FIX_DRIVE, FIX_DRIVE + FIX_STUFF_FOLDER + "\\");
			filePath = filePath.substring(0, filePath.lastIndexOf(".")) + "_orig"
					+ filePath.substring(filePath.lastIndexOf("."));
			originalFilePath = originalFilePath.replace(FIX_DRIVE, FIX_DRIVE + FIX_STUFF_FOLDER + "\\");
		} else {
			originalFilePath = originalFilePath.substring(0, originalFilePath.lastIndexOf(".")) + "_fixed"
					+ originalFilePath.substring(originalFilePath.lastIndexOf("."));
		}

		ArrayList<String> commandList = new ArrayList<String>();
		commandList.add(String.format("%sffmpeg.exe -i \"%s\"", FFMPEG_BIN_COMMAND, filePath));

		if (dimensions.equals("N/A")) {
			commandList.add(String.format("-vf scale=%s:%s", "1280", "720"));
		} else {

			switch (dimensions) {
			case "land":
				commandList.add(String.format("-vf scale=%s:%s", "1280", "720"));
				break;
			case "port":
				commandList.add(String.format("-vf scale=%s:%s", "720", "1280"));
				break;
			case "w1088 x h1920":
				commandList.add(String.format("-vf scale=%s:%s", "720", "1280"));
				break;
			case "w1280 x h720":
				commandList.add(String.format("-vf scale=%s:%s", "1280", "720"));
				break;
			case "w1392 x h784":
				commandList.add(String.format("-vf scale=%s:%s", "1280", "720"));
				break;
			case "w1440 x h1088":
				commandList.add(String.format("-vf scale=%s:%s", "1280", "952"));
				break;
			case "w1504 x h1504":
				commandList.add(String.format("-vf scale=%s:%s", "1280", "1280"));
				break;
			case "w1920 x h1088":
				commandList.add(String.format("-vf scale=%s:%s", "1280", "720"));
				break;
			case "w2800 x h1584":
				commandList.add(String.format("-vf scale=%s:%s", "1280", "720"));
				break;
			case "w2880 x h2880":
				commandList.add(String.format("-vf scale=%s:%s", "1280", "1280"));
				break;
			case "w3840 x h2160":
				commandList.add(String.format("-vf scale=%s:%s", "1280", "720"));
				break;
			case "w624 x h1232":
				commandList.add(String.format("-vf scale=%s:%s", "624", "1232"));
				break;
			case "w720 x h1280":
				commandList.add(String.format("-vf scale=%s:%s", "720", "1280"));
				break;
			case "w800 x h800":
				commandList.add(String.format("-vf scale=%s:%s", "720", "720"));
				break;
			case "w848 x h480":
				commandList.add(String.format("-vf scale=%s:%s", "1280", "720"));
				break;
			default:
				commandList.add(String.format("-vf scale=%s:%s", "848", "480"));
			}

		}

		commandList.add(String.format("\"%s\"", originalFilePath));
		String lineTwo = String.join(" ", commandList);
		sb.append(lineTwo + "\n");
	}

	public static void handleFolderForFile(String filePath, StringBuilder buffer) {
		String[] copyToFolder = createFileFolderFromFilePath(filePath);
		String newFilePath = copyToFolder[0] + "\\" + copyToFolder[1];
		String powerShellCopy = "Copy-Item -Path \"" + filePath + "\" -Destination \"" + newFilePath + "\"\n";
		buffer.append(powerShellCopy);
	}

	public static String[] createFileFolderFromFilePath(String filePath) {
		String finalFolderPath = FIX_DRIVE + FIX_STUFF_FOLDER;
		String[] pathElements = filePath.split("\\\\");
		for (int i = 1; i < pathElements.length - 1; i++) {
			finalFolderPath = finalFolderPath + "\\" + pathElements[i];
			if (MAKE_COPY_OF_FILES) {
				createFileFolder(finalFolderPath);
			}
		}

		String finalFileName = pathElements[pathElements.length - 1];
		if (MAKE_COPY_OF_FILES) {
			finalFileName = finalFileName.substring(0, finalFileName.lastIndexOf(".")) + "_orig"
					+ finalFileName.substring(finalFileName.lastIndexOf("."));
		}

		return new String[] { finalFolderPath, finalFileName };

	}

	public static void createFileFolder(String filePath) {
		File oDirectory = new File(filePath);
		if (!oDirectory.exists()) {
			oDirectory.mkdirs();
		}
	}

	public static void writeSet(Set<String> stringSet, String filePath) {

		try (PrintWriter writer = new PrintWriter(new File(filePath))) {
			for (String element : stringSet) {
				writer.println(element);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
