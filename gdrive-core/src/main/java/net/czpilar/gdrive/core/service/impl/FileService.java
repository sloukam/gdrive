package net.czpilar.gdrive.core.service.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.ParentReference;
import net.czpilar.gdrive.core.exception.FileHandleException;
import net.czpilar.gdrive.core.listener.FileUploadProgressListener;
import net.czpilar.gdrive.core.service.IDirectoryService;
import net.czpilar.gdrive.core.service.IFileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Service with methods for handling files in Google Drive.
 *
 * @author David Pilar (david@czpilar.net)
 */
public class FileService extends AbstractService implements IFileService {

	private static final Logger LOG = LoggerFactory.getLogger(FileService.class);

	private IDirectoryService directoryService;

	@Autowired
	public void setDirectoryService(IDirectoryService directoryService) {
		this.directoryService = directoryService;
	}

	protected IDirectoryService getDirectoryService() {
		return directoryService;
	}

	protected String getUploadDir(String uploadDirname) {
		if (uploadDirname == null) {
			uploadDirname = getGDriveCredential().getUploadDir();
		}
		return uploadDirname;
	}

	@Override
	public File uploadFile(String filename, String pathname) {
		File parentDir = getDirectoryService().findOrCreateDirectory(getUploadDir(pathname));
		return uploadFile(filename, parentDir);
	}

	@Override
	public File uploadFile(String filename, File parentDir) {
		LOG.info("Uploading file {}", filename);
		try {
			Path path = Paths.get(filename);

			File file = new File();
			file.setTitle(path.getFileName().toString());
			file.setMimeType(Files.probeContentType(path));
			if (parentDir != null) {
				file.setParents(Arrays.asList(new ParentReference().setId(parentDir.getId())));
			}

			Drive.Files.Insert insert = getDrive().files().insert(file, new FileContent(file.getMimeType(), path.toFile()));
			insert.getMediaHttpUploader().setDirectUploadEnabled(false);
			insert.getMediaHttpUploader().setProgressListener(new FileUploadProgressListener(filename));
			file = insert.execute();
			LOG.info("Finished uploading file {} - remote file ID is {}", filename, file.getId());
			return file;
		} catch (IOException e) {
			LOG.error("Unable to upload file {}.", filename);
			throw new FileHandleException("Unable to upload file.", e);
		}
	}

	@Override
	public File uploadFile(String filename) {
		return uploadFile(filename, (File) null);
	}

	@Override
	public List<File> uploadFiles(List<String> filenames) {
		return uploadFiles(filenames, (File) null);
	}

	@Override
	public List<File> uploadFiles(List<String> filenames, String pathname) {
		File parentDir = getDirectoryService().findOrCreateDirectory(getUploadDir(pathname));
		return uploadFiles(filenames, parentDir);
	}

	@Override
	public List<File> uploadFiles(List<String> filenames, File parentDir) {
		List<File> files = new ArrayList<File>();
		if (filenames != null) {
			for (String filename : filenames) {
				try {
					files.add(uploadFile(filename, parentDir));
				} catch (FileHandleException e) {
					LOG.error("Error during uploading file.", e);
				}
			}
		}
		return files;
	}
}
