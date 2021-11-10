import javax.naming.spi.DirectoryManager;
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

public class Launcher {
    public static void main(String[] args) {
        String latestVersionFileName = "./latestUpdate.sav";
        String updateZipName = "./update.zip";
        String updateFolderName = "./update";
        String gameFolderName = "./game";
        String oldGameFolderName = "./game_old";
        ProjectUpdater updater = new ProjectUpdater("Scraxtastic", "JogisWayToStreamingGod");
        updater.update(latestVersionFileName, updateZipName, updateFolderName, gameFolderName, oldGameFolderName);
        String gamePath = gameFolderName + "/JogisWayToStreamingGod.exe";
        Runtime runtime = Runtime.getRuntime();
        try {
            Process process = runtime.exec("cmd /c start " + gamePath);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
