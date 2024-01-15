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

public class AuditVideoResampler {

	private static final boolean ENABLE_MAIN_METHOD = true;
	private static final Logger LOGGER = LogManager.getLogger(AuditVideoResampler.class.getName());

	private static final String FIX_DRIVE = "E:\\";
	private static final String FIX_STUFF_FOLDER = "FixStuff";

	private static final String SOURCE_FOLDER_1 = "E:\\Pictures";
	private static final String REPLACE_1 = "E:";

	private static final String SOURCE_FOLDER_2 = "F:\\20240112_Backup\\E\\Pictures";
	private static final String REPLACE_2 = "F:\\20240112_Backup\\E";

	private static final String FVR_ORIGINAL_FILES_NAME_FIX = FIX_DRIVE + FIX_STUFF_FOLDER + "\\FVR_Audit_Fixed.txt";
	private static final String FVR_BACKUP_FILES_NAME_FIX = FIX_DRIVE + FIX_STUFF_FOLDER + "\\FVR_Audit_Backup.txt";

	private static StringBuilder sourceFolderOneBuffer = new StringBuilder();
	private static StringBuilder sourceFolderTwoBuffer = new StringBuilder();

	public static void main(String[] args) throws IOException {

		if (ENABLE_MAIN_METHOD) {
			
			createFileFolder(FIX_DRIVE + FIX_STUFF_FOLDER);
			
			startVisit(SOURCE_FOLDER_1, sourceFolderOneBuffer);
			startVisit(SOURCE_FOLDER_2, sourceFolderTwoBuffer);

			XSaLTFileSystemUtils.writeStringBuilderToFile(sourceFolderOneBuffer, FVR_ORIGINAL_FILES_NAME_FIX);
			XSaLTFileSystemUtils.writeStringBuilderToFile(sourceFolderTwoBuffer, FVR_BACKUP_FILES_NAME_FIX);
		}

	}

	public static void startVisit(String filePath, StringBuilder stringBuilder) {
		visitFiles(Paths.get(filePath), stringBuilder);
	}

	private static void visitFiles(Path path, StringBuilder stringBuilder) {
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
			for (Path entry : stream) {
				if (entry.toFile().isDirectory()) {
					visitFiles(entry, stringBuilder);
				} else {
					visitFileCode(entry.toString(), stringBuilder);
				}
			}
		} catch (IOException e) {
			LOGGER.error(e.toString(), e);
		}
	}

	public static void visitFileCode(String filePath, StringBuilder stringBuilder) {
		stringBuilder.append(filePath.replace(REPLACE_1, "").replace(REPLACE_2, "") + "\n");
		System.out.println(filePath);
	}
	
	public static void createFileFolder(String filePath) {
		File oDirectory = new File(filePath);
		if (!oDirectory.exists()) {
			oDirectory.mkdirs();
		}
	}

}
