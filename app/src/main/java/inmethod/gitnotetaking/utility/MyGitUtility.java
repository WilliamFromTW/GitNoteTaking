package inmethod.gitnotetaking.utility;

import android.os.Environment;
import android.util.Log;

import java.io.File;

import inmethod.jakarta.vcs.GitUtil;

public class MyGitUtility {

    public static final String TAG = "GitNoteTaking";

    public static boolean deleteLocalGitRepository(String sRemoteUrl) {
        String sLocalDirectory = getLocalGitDirectory(sRemoteUrl);
     //   Log.d(TAG, "check local repository, status = " + checkLocalGitRepository(sRemoteUrl));
        if (checkLocalGitRepository(sRemoteUrl)) {

            GitUtil aGitUtil;
            try {
                aGitUtil = new GitUtil(sRemoteUrl, sLocalDirectory);
                aGitUtil.removeLocalGitRepository();
                return true;
            } catch (Exception ee) {
                ee.printStackTrace();
            }
        }
        return false;
    }

    public static boolean cloneGit(String sRemoteUrl, String sUserName, String sUserPassword) {

        String sLocalDirectory = getLocalGitDirectory(sRemoteUrl);

        if (checkLocalGitRepository(sRemoteUrl)) {
            return false;
        }
        boolean bIsRemoteRepositoryExist = false;
        GitUtil aGitUtil;
        try {
            aGitUtil = new GitUtil(sRemoteUrl, sLocalDirectory);
            bIsRemoteRepositoryExist = aGitUtil.checkRemoteRepository(sUserName, sUserPassword);
            if (!bIsRemoteRepositoryExist) {
                Log.e(TAG, "check remote url failed");
                return false;
            }
            System.out.println("Remote repository exists ? " + bIsRemoteRepositoryExist);
            if (bIsRemoteRepositoryExist) {
                System.out.println("try to clone remote repository if local repository is not exists \n");
                if (aGitUtil.clone(sUserName, sUserPassword)) {
                    System.out.println("clone finished!");
                    return true;
                } else {
                    System.out.println("clone failed!");
                    return false;
                }
            } else if (bIsRemoteRepositoryExist && aGitUtil.checkLocalRepository()) {
                System.out.println("pull branch = " + aGitUtil.getDefaultBranch() + " , status : "
                        + aGitUtil.update(sUserName, sUserPassword));
                return true;
            }
            return false;
/*
            System.out.println("Default branch : " + aGitUtil.getDefaultBranch());
            if (aGitUtil.checkLocalRepository()) {
                List<Ref> aAllBranches = aGitUtil.getBranches();
                if (aAllBranches != null) {
                    System.out.println("\nList All Local Branch Name\n--------------------------------");
                    for (Ref aBranch : aAllBranches) {
                        System.out.println("branch : " + aBranch.getName());
                    }
                    System.out.println("");
                }
                System.out.println("Switch local branch to master: " + aGitUtil.checkout("master"));
                List<Ref> aAllTags = aGitUtil.getLocalTags();
                if (aAllTags != null) {
                    System.out.println("\nList All Local Tags Name\n--------------------------------");
                    for (Ref aTag : aAllTags) {
                        System.out.println("Tag : " + aTag.getName() + "(" + aGitUtil.getTagDate(aTag, "yyyy-MM-dd HH:mm:ss") + " created!)");
                        System.out.println("Commit messages\n==\n" + aGitUtil.getCommitMessageByTagName(aTag) + "\n");
                    }
                    System.out.println("");
                }
                aGitUtil.close();
            }

 */
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    private static String getLocalGitDir(String sRemoteUrl) {
        String sReturn = "";
        if (sRemoteUrl.indexOf(".git") == -1) return sReturn;
        int lastPath = sRemoteUrl.lastIndexOf("//");
        if (lastPath != -1) {
            sReturn = sRemoteUrl.substring(lastPath + 1);
        }
        if (sReturn.length() > 4)
            sReturn = sReturn.substring(0, sReturn.length() - 4);
        else return "";
        return sReturn;
    }


    public static String getLocalGitDirectory(String sRemoteUrl){
       return  Environment.getExternalStorageDirectory() +
                File.separator + "gitnotetaking" + File.separator + getLocalGitDir(sRemoteUrl);
    }

    public static boolean checkLocalGitRepository(String sRemoteUrl) {
        String sLocalDirectory = getLocalGitDirectory(sRemoteUrl);
        Log.d(TAG, "default local directory = " + sLocalDirectory);
        boolean bIsLocalRepositoryExist = false;
        try {
            bIsLocalRepositoryExist = GitUtil.checkLocalRepository(sLocalDirectory + "/.git");
            Log.d(TAG, "bIsLocalRepositoryExist=" + bIsLocalRepositoryExist);
            if (bIsLocalRepositoryExist) return true;
        } catch (Exception ee) {
            ee.printStackTrace();
        }
        return false;
    }
}
