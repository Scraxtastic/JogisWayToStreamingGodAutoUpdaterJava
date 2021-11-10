import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ProjectUpdater {
    private final String latestDownloadUrlFormat = "https://github.com/%s/%s/releases/latest/download/JogisWayToStreamingGod_Data.zip";
    private final String latestVersionUrlFormat = "https://github.com/%s/%s/releases/latest/";

    private String latestDownloadUrl;
    private String latestVersionUrl ;

    private String _username;
    private String _repo;

    private String regex = "<a href=\"\\/login\\?return_to=(.*)\"";
    private Pattern pattern = Pattern.compile(regex);

    public ProjectUpdater(String username, String repo) {
        _username = username;
        _repo = repo;
        latestDownloadUrl = String.format(latestDownloadUrlFormat, _username, _repo);
        latestVersionUrl = String.format(latestVersionUrlFormat, _username, _repo);
    }

    public void update(String latestVersionFileName, String updateZipName,
                       String updateFolderName, String gameFolderName, String oldGameFolderName) {
        Path filePath = Path.of(latestVersionFileName);
        boolean isLatestVersion = true;
        try {
            String latestVersionWebsite = getUrlContent((latestVersionUrl));
            final Matcher matcher = pattern.matcher(latestVersionWebsite);
            String latestVersionLinkDecoded = "";
            if (matcher.find()) {
                String latestVersionLink = matcher.group(1).split("\"")[0];
                latestVersionLinkDecoded = java.net.URLDecoder.decode(latestVersionLink, StandardCharsets.UTF_8.name());
                if (!Files.exists(filePath) || !Files.readString(filePath).equals(latestVersionLinkDecoded)) {
                    isLatestVersion = false;
                }
            }
            if (!isLatestVersion) {
                downloadFile(latestDownloadUrl, updateZipName);
                System.out.println("Download completed.");
                System.out.println("Unzipping folder...");
                deleteDirectory(new File(updateFolderName));
                unzipTo(updateZipName, updateFolderName);
                System.out.println("Unzipped folder to: " + new File(updateFolderName).getAbsolutePath());
                deleteDirectory(new File(oldGameFolderName));
                File gameFolder = new File(gameFolderName);
                if (gameFolder.exists())
                    Files.move(gameFolder.toPath(), new File(oldGameFolderName).toPath(), StandardCopyOption.REPLACE_EXISTING);
                deleteDirectory(new File(gameFolderName));
                File updateFolder = new File(updateFolderName);
                if (updateFolder.exists())
                    Files.move(updateFolder.toPath(), new File(gameFolderName).toPath(), StandardCopyOption.REPLACE_EXISTING);
                System.out.println("Moved folder to " + updateFolder.getAbsolutePath());
                Files.writeString(filePath, latestVersionLinkDecoded);
                System.out.println("Updated successfully.");
            } else {
                System.out.println("Already latest version.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private String getUrlContent(String latestVersionUrl) {

        StringBuilder result = new StringBuilder();
        try {
            URL url = new URL(latestVersionUrl);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
            for (String line; (line = reader.readLine()) != null; ) {
                result.append(line);
            }
            con.disconnect();
        } catch (MalformedURLException | ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result.toString();
    }

    private boolean downloadFile(String urlLink, String filename) {
        try (BufferedInputStream in = new BufferedInputStream(new URL(urlLink).openStream());
             FileOutputStream fileOutputStream = new FileOutputStream(filename)) {
            URL url = new URL(urlLink);
            URLConnection connection = url.openConnection();
            long contentLength = connection.getContentLength();
            int bufferLength = 2048;
            byte dataBuffer[] = new byte[bufferLength];
            int bytesRead;
            long contentRead = 0;
            while ((bytesRead = in.read(dataBuffer, 0, bufferLength)) != -1) {
                fileOutputStream.write(dataBuffer, 0, bytesRead);
                contentRead += bytesRead;
//                System.out.println(String.format("%d / %d (%f %%)",contentRead, contentLength, (((double)contentRead)/(double) contentLength)*100 ));
                System.out.println(String.format("%d / %d (%d %%)", contentRead / 1000, contentLength / 1000, (contentRead * 100) / contentLength));

            }
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    public void unzipTo(String zipFilePath, String destinationFilePath) {
        final byte[] buffer = new byte[1024];
        try {
            final ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFilePath));

            ZipEntry zipEntry;
            // zipEntry = zis.getNextEntry();
            while ((zipEntry = zis.getNextEntry()) != null) {
                final File newFile = newFile(new File(destinationFilePath), zipEntry);
                writeZipBuffer(buffer, zis, zipEntry, newFile);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeZipBuffer(byte[] buffer, ZipInputStream zis, ZipEntry zipEntry, File newFile) throws IOException {
        if (zipEntry.isDirectory()) {
            if (!newFile.isDirectory() && !newFile.mkdirs()) {
                throw new IOException("Failed to create directory " + newFile);
            }
        } else {
            File parent = newFile.getParentFile();
            if (!parent.isDirectory() && !parent.mkdirs()) {
                throw new IOException("Failed to create directory " + parent);
            }

            final FileOutputStream fos = new FileOutputStream(newFile);
            int len;
            while ((len = zis.read(buffer)) > 0) {
                fos.write(buffer, 0, len);
            }
            fos.close();
        }
    }

    private File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }

    public boolean deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        return directoryToBeDeleted.delete();
    }
}
