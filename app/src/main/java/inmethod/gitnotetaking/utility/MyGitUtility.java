package inmethod.gitnotetaking.utility;

import android.app.Activity;
import android.os.Environment;
import android.util.Log;

import androidx.preference.PreferenceManager;

import java.io.File;
import java.util.ArrayList;

import inmethod.gitnotetaking.db.RemoteGit;
import inmethod.gitnotetaking.db.RemoteGitDAO;
import inmethod.gitnotetaking.view.GitList;
import inmethod.jakarta.vcs.GitUtil;

public class MyGitUtility {

    public static final String TAG = "GitNoteTaking";

    public static boolean deleteLocalGitRepository(Activity activity,String sRemoteUrl) {
        String sLocalDirectory = getLocalGitDirectory(activity,sRemoteUrl);
        //   Log.d(TAG, "check local repository, status = " + checkLocalGitRepository(sRemoteUrl));
        if (checkLocalGitRepository(activity,sRemoteUrl)) {

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

    public static boolean push(Activity activity, String sRemoteUrl) {
        RemoteGitDAO aRemoteGitDAO = new RemoteGitDAO(activity);
        RemoteGit aRemoteGit = aRemoteGitDAO.getByURL(sRemoteUrl);
        if (aRemoteGit == null) return false;
        String sLocalDirectory = getLocalGitDirectory(activity,sRemoteUrl);

        boolean bIsRemoteRepositoryExist = false;
        GitUtil aGitUtil;
        try {
            aGitUtil = new GitUtil(sRemoteUrl, sLocalDirectory);
            bIsRemoteRepositoryExist = aGitUtil.checkRemoteRepository(aRemoteGit.getUid(), aRemoteGit.getPwd());
            if (!bIsRemoteRepositoryExist) {
                Log.e(TAG, "check remote url failed");
                aRemoteGit.setPush_status(GitList.PUSH_FAIL);
                aRemoteGitDAO.update(aRemoteGit);
                return false;
            }
            Log.d(TAG,"Remote repository exists ? " + bIsRemoteRepositoryExist);
            if (bIsRemoteRepositoryExist) {
                Log.d(TAG,"try to push \n");
                if (aGitUtil.push(aRemoteGit.getRemoteName(), aRemoteGit.getUid(), aRemoteGit.getPwd())) {
                    Log.d(TAG,"push finished!");
                    aRemoteGit.setPush_status(GitList.PUSH_SUCCESS);
                    aRemoteGitDAO.update(aRemoteGit);
                    aRemoteGitDAO.close();
                    return true;
                } else {
                    aRemoteGit.setPush_status(GitList.PUSH_FAIL);
                    aRemoteGitDAO.update(aRemoteGit);
                    Log.d(TAG,"push failed!");
                    aRemoteGitDAO.close();
                    return false;
                }
            }
            return false;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean commit(Activity activity, String sRemoteUrl, String sCommitMessages) {

        String sLocalDirectory = getLocalGitDirectory(activity,sRemoteUrl);
        GitUtil aGitUtil;
        RemoteGitDAO aRemoteGitDAO = new RemoteGitDAO(activity);
        RemoteGit aRemoteGit = aRemoteGitDAO.getByURL(sRemoteUrl);
        aRemoteGitDAO.close();
        try {
            aGitUtil = new GitUtil(sRemoteUrl, sLocalDirectory);

            String sAuthorName = aRemoteGit.getAuthor_name();
            String sAuthorEmail = aRemoteGit.getAuthor_email();
            if (aGitUtil.commit(sCommitMessages, sAuthorName, sAuthorEmail)) {
                aRemoteGit.setPush_status(GitList.PUSH_FAIL);
                Log.d(TAG,"commit finished!");
                return true;
            } else {
                Log.d(TAG,"commit failed!");
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean deleteByRemoteUrl(Activity activity,String sRemoteUrl){
        RemoteGitDAO aRemoteGitDAO = new RemoteGitDAO(activity);
        boolean sReturn = aRemoteGitDAO.delete(sRemoteUrl);
        aRemoteGitDAO.close();
        return sReturn;

    }

    public static ArrayList<RemoteGit> getRemoteGitList(Activity activity){
        RemoteGitDAO aRemoteGitDAO = new RemoteGitDAO(activity);
        ArrayList<RemoteGit> aList =  aRemoteGitDAO.getAll();
        aRemoteGitDAO.close();
        return aList;

    }
    public static boolean pull(Activity activity,String sRemoteUrl) {
        RemoteGitDAO aRemoteGitDAO = new RemoteGitDAO(activity);
        RemoteGit aRemoteGit = aRemoteGitDAO.getByURL(sRemoteUrl);
        if (aRemoteGit == null) return false;
        String sUserName = aRemoteGit.getUid();
        String sUserPassword = aRemoteGit.getPwd();
        aRemoteGitDAO.close();
        String sLocalDirectory = getLocalGitDirectory(activity,sRemoteUrl);
        boolean bIsRemoteRepositoryExist = false;
        GitUtil aGitUtil;
        try {
            aGitUtil = new GitUtil(sRemoteUrl, sLocalDirectory);
            bIsRemoteRepositoryExist = aGitUtil.checkRemoteRepository(sUserName, sUserPassword);
            if (!bIsRemoteRepositoryExist) {
                Log.e(TAG, "check remote url failed");
                return false;
            }
            Log.d(TAG,"Remote repository exists ? " + bIsRemoteRepositoryExist);
            if (bIsRemoteRepositoryExist) {
                Log.d(TAG,"try to update remote repository if local repository is not exists \n");
                if (aGitUtil.pull(sUserName, sUserPassword)) {
                    Log.d(TAG,"pull finished!");
                    return true;
                } else {
                    Log.d(TAG,"pull failed!");
                    return false;
                }
            }
            return false;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    public static boolean cloneGit(Activity activity,String sRemoteUrl,String sUserName,String sUserPassword) {
        String sLocalDirectory = getLocalGitDirectory(activity,sRemoteUrl);

        if (checkLocalGitRepository(activity,sRemoteUrl)) {
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
            Log.d(TAG,"Remote repository exists ? " + bIsRemoteRepositoryExist);
            if (bIsRemoteRepositoryExist) {
                Log.d(TAG,"try to clone remote repository if local repository is not exists \n");
                if (aGitUtil.clone(sUserName, sUserPassword)) {
                    Log.d(TAG,"clone finished!");
                    return true;
                } else {
                    Log.d(TAG,"clone failed!");
                    return false;
                }
            } else if (bIsRemoteRepositoryExist && aGitUtil.checkLocalRepository()) {
                Log.d(TAG,"pull branch = " + aGitUtil.getDefaultBranch() + " , status : "
                        + aGitUtil.pull(sUserName, sUserPassword));
                return true;
            }
            return false;
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
            sReturn = sRemoteUrl.substring(lastPath + 2);
        }
        if (sReturn.length() > 4)
            sReturn = sReturn.substring(0, sReturn.length() - 4);
        else return "";
        return sReturn;
    }


    public static String getLocalGitDirectory(Activity activity,String sRemoteUrl) {
        return Environment.getExternalStorageDirectory() +
                File.separator +  PreferenceManager.getDefaultSharedPreferences(activity).getString("GitLocalDirName", "gitnotetaking")  + File.separator + getLocalGitDir(sRemoteUrl);
    }

    public static boolean checkLocalGitRepository(Activity activity,String sRemoteUrl) {
        String sLocalDirectory = getLocalGitDirectory(activity,sRemoteUrl);
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
