import java.io.IOException;

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
            runtime.exec("cmd /c start " + gamePath);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
