package com.codef.videoresampler;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.codef.xsalt.utils.XSaLTFileSystemUtils;

public class RenameVideoResampler {

	private static final String FIX_DRIVE = "E:\\";
	private static final String FIX_STUFF_FOLDER = "FixStuff";

	private static final boolean ENABLE_MAIN_METHOD = true;
	private static final Logger LOGGER = LogManager.getLogger(RenameVideoResampler.class.getName());

	private static final String SOURCE_FOLDER = "E:\\Pictures";

	private static StringBuilder sourceFolderOneBuffer = new StringBuilder();

	private static final String FVR_RENAME_FILES_NAME_FIX = FIX_DRIVE + FIX_STUFF_FOLDER + "\\FVR_Rename_Normal.ps1";

	public static void main(String[] args) throws IOException {

		if (ENABLE_MAIN_METHOD) {

			createFileFolder(FIX_DRIVE + FIX_STUFF_FOLDER);

			startVisit(SOURCE_FOLDER);
			XSaLTFileSystemUtils.writeStringBuilderToFile(sourceFolderOneBuffer, FVR_RENAME_FILES_NAME_FIX);
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
		if (filePath.toLowerCase().endsWith(".mp4") && filePath.toLowerCase().contains("_fixed")) {
			String newFileNameOnly = filePath.substring(filePath.lastIndexOf("\\") + 1, filePath.length())
					.replace("_fixed", "");
			System.out.println(newFileNameOnly);
			String powerShellCopy = "Rename-Item -Path \"" + filePath + "\" -NewName \"" + newFileNameOnly + "\"\n";
			sourceFolderOneBuffer.append(powerShellCopy);
		}
	}

	public static void createFileFolder(String filePath) {
		File oDirectory = new File(filePath);
		if (!oDirectory.exists()) {
			oDirectory.mkdirs();
		}
	}

}
