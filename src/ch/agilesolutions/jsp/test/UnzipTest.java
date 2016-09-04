package ch.agilesolutions.jsp.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.IOUtils;

/**
 * http://www.baeldung.com/java-compress-and-uncompress
 * 
 * http://stackoverflow.com/questions/9324933/what-is-a-good-java-library-to-zip-unzip-files
 * 
 * @author
 *
 */
public class UnzipTest {

	public static void main(String[] args) throws IOException {

		java.util.zip.ZipFile zipFile = new ZipFile(
				"C:\\Development\\Workspaces\\JSP\\rob\\lib\\commons-io-2.5-bin.zip");
		try {
			Enumeration<? extends ZipEntry> entries = zipFile.entries();
			while (entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();
				File entryDestination = new File("C:\\Development\\Workspaces\\JSP\\rob\\tmp", entry.getName());
				if (entry.isDirectory()) {
					entryDestination.mkdirs();
				} else {
					entryDestination.getParentFile().mkdirs();
					InputStream in = zipFile.getInputStream(entry);
					OutputStream out = new FileOutputStream(entryDestination);
					IOUtils.copy(in, out);
					IOUtils.closeQuietly(in);
					out.close();
				}
			}
		} finally {
			zipFile.close();
		}
	}
}