package inmethod.gitnotetaking.utility;

import android.app.Activity;
import android.content.Context;
import android.os.Environment;
import android.util.Log;

import androidx.preference.PreferenceManager;

import org.eclipse.jgit.revwalk.RevCommit;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import inmethod.gitnotetaking.db.RemoteGit;
import inmethod.gitnotetaking.db.RemoteGitDAO;
import inmethod.gitnotetaking.view.GitList;
import inmethod.jakarta.vcs.GitUtil;

public class MyGitUtility {

    public static final String TAG = "GitNoteTaking";

    public static boolean deleteLocalGitRepository(Context context, String sRemoteUrl) {
        String sLocalDirectory = getLocalGitDirectory(context, sRemoteUrl);
        //   Log.d(TAG, "check local repository, status = " + checkLocalGitRepository(sRemoteUrl));
        if (checkLocalGitRepository(context, sRemoteUrl)) {

            GitUtil aGitUtil;
            try {
                aGitUtil = new GitUtil(sRemoteUrl, sLocalDirectory);
                aGitUtil.removeLocalGitRepository();
                if (aGitUtil != null) aGitUtil.close();
                return true;
            } catch (Exception ee) {
                ee.printStackTrace();
            }
        }
        return false;
    }

    public static List<String> fetchGitBranches(Context context, String sRemoteUrl) {
        GitUtil aGitUtil;
        RemoteGitDAO aRemoteGitDAO = new RemoteGitDAO(context);
        RemoteGit aRemoteGit = aRemoteGitDAO.getByURL(sRemoteUrl);
        aRemoteGitDAO.close();
        if (aRemoteGit == null) return null;
        List<String> aReturn = null;
        try {
            aGitUtil = new GitUtil(sRemoteUrl, null);
            aReturn = aGitUtil.fetchGitBranches(aRemoteGit.getUid(),aRemoteGit.getPwd());
            if (aGitUtil != null) aGitUtil.close();
        } catch (Exception ee) {
            ee.printStackTrace();
        }
        return aReturn;
    }


    public static boolean push(Context context, String sRemoteUrl) {
        RemoteGitDAO aRemoteGitDAO = new RemoteGitDAO(context);
        RemoteGit aRemoteGit = aRemoteGitDAO.getByURL(sRemoteUrl);
        if (aRemoteGit == null) return false;
        String sLocalDirectory = getLocalGitDirectory(context, sRemoteUrl);

        boolean bIsRemoteRepositoryExist = false;
        GitUtil aGitUtil=null;
        try {
            aGitUtil = new GitUtil(sRemoteUrl, sLocalDirectory);
            bIsRemoteRepositoryExist = aGitUtil.checkRemoteRepository(aRemoteGit.getUid(), aRemoteGit.getPwd());
            if (!bIsRemoteRepositoryExist) {
                Log.e(TAG, "check remote url failed");
                aRemoteGit.setStatus(GitList.PUSH_FAIL);
                aRemoteGitDAO.update(aRemoteGit);
                if (aGitUtil != null) aGitUtil.close();
                return false;
            }
            Log.d(TAG, "Remote repository exists ? " + bIsRemoteRepositoryExist);
            if (bIsRemoteRepositoryExist) {
                Log.d(TAG, "try to push \n");
                if (aGitUtil.push(sRemoteUrl, aRemoteGit.getUid(), aRemoteGit.getPwd())) {
                    Log.d(TAG, "push finished!");
                    aRemoteGit.setStatus(GitList.PUSH_SUCCESS);
                    aRemoteGitDAO.update(aRemoteGit);
                    aRemoteGitDAO.close();
                    if (aGitUtil != null) aGitUtil.close();
                    return true;
                } else {
                    aRemoteGit.setStatus(GitList.PUSH_FAIL);
                    aRemoteGitDAO.update(aRemoteGit);
                    Log.d(TAG, "push failed!");
                    aRemoteGitDAO.close();
                    if (aGitUtil != null) aGitUtil.close();
                    return false;
                }
            }
            aRemoteGitDAO.close();
            if (aGitUtil != null) aGitUtil.close();
            return false;

        } catch (Exception e) {
            e.printStackTrace();
        }
        if (aGitUtil != null) aGitUtil.close();
        return false;
    }

    public static boolean commit(Context context, String sRemoteUrl, String sCommitMessages) {

        String sLocalDirectory = getLocalGitDirectory(context, sRemoteUrl);
        GitUtil aGitUtil=null;
        RemoteGitDAO aRemoteGitDAO = new RemoteGitDAO(context);
        RemoteGit aRemoteGit = aRemoteGitDAO.getByURL(sRemoteUrl);
        try {
            aGitUtil = new GitUtil(sRemoteUrl, sLocalDirectory);

            String sAuthorName = aRemoteGit.getAuthor_name();
            String sAuthorEmail = aRemoteGit.getAuthor_email();
            aRemoteGitDAO.close();
            if (aGitUtil.commit(sCommitMessages, sAuthorName, sAuthorEmail)) {
                aRemoteGit.setStatus(GitList.PUSH_SUCCESS);
                Log.d(TAG, "commit finished!");
                if (aGitUtil != null) aGitUtil.close();
                return true;
            } else {
                aRemoteGit.setStatus(GitList.PUSH_FAIL);
                Log.d(TAG, "commit failed!");
                if (aGitUtil != null) aGitUtil.close();
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (aGitUtil != null) aGitUtil.close();
        return false;
    }

    public static boolean deleteByRemoteUrl(Context context, String sRemoteUrl) {
        RemoteGitDAO aRemoteGitDAO = new RemoteGitDAO(context);
        boolean sReturn = aRemoteGitDAO.delete(sRemoteUrl);
        aRemoteGitDAO.close();
        return sReturn;
    }

    public static ArrayList<RemoteGit> getRemoteGitList(Context context) {
        RemoteGitDAO aRemoteGitDAO = new RemoteGitDAO(context);
        ArrayList<RemoteGit> aList = aRemoteGitDAO.getAll();
        aRemoteGitDAO.close();
        return aList;

    }

    public static List<RevCommit> getLocalCommiLogtList(Context context, String sRemoteUrl) {
        GitUtil aGitUtil;
        List<RevCommit> aList = null;
        try {
            String sLocalDirectory = getLocalGitDirectory(context, sRemoteUrl);
            aGitUtil = new GitUtil(sRemoteUrl, sLocalDirectory);
            aList = aGitUtil.getLocalCommitIdList();
            if (aGitUtil != null) aGitUtil.close();
        } catch (Exception ee) {
            ee.printStackTrace();
        }
        return aList;
    }
    public static boolean checkout(Context context,String sRemoteUrl){
        RemoteGitDAO aRemoteGitDAO = new RemoteGitDAO(context);
        RemoteGit aRemoteGit = aRemoteGitDAO.getByURL(sRemoteUrl);
        if (aRemoteGit == null) return false;
        String sUserName = aRemoteGit.getUid();
        String sUserPassword = aRemoteGit.getPwd();
        aRemoteGitDAO.close();
        String sLocalDirectory = getLocalGitDirectory(context, sRemoteUrl);
        GitUtil aGitUtil;
        boolean bReturn = false;
        try {
            aGitUtil = new GitUtil(sRemoteUrl, sLocalDirectory);
                bReturn = aGitUtil.checkout(aRemoteGit.getBranch());
            aGitUtil.close();
        }catch (Exception ee){
            Log.d(TAG,ee.getLocalizedMessage());
        }
        return bReturn;
    }

    public static String getLocalBranchName(Context context,String sRemoteUrl){
        String sLocalDirectory = getLocalGitDirectory(context, sRemoteUrl);
        GitUtil aGitUtil;
        try {
            aGitUtil = new GitUtil(sRemoteUrl, sLocalDirectory);
            return aGitUtil.getLocalDefaultBranch();
        }catch(Exception ex){
            ex.printStackTrace();

        }
        return null;
    }

    public static boolean pull(Context context, String sRemoteUrl) {
        RemoteGitDAO aRemoteGitDAO = new RemoteGitDAO(context);
        RemoteGit aRemoteGit = aRemoteGitDAO.getByURL(sRemoteUrl);
        if (aRemoteGit == null) return false;
        String sUserName = aRemoteGit.getUid();
        String sUserPassword = aRemoteGit.getPwd();
        aRemoteGitDAO.close();
        String sLocalDirectory = getLocalGitDirectory(context, sRemoteUrl);
        boolean bIsRemoteRepositoryExist = false;
        GitUtil aGitUtil;
        try {
            aGitUtil = new GitUtil(sRemoteUrl, sLocalDirectory);
            bIsRemoteRepositoryExist = aGitUtil.checkRemoteRepository(sUserName, sUserPassword);
            if (!bIsRemoteRepositoryExist) {
                Log.e(TAG, "check remote url failed");
                if (aGitUtil != null) aGitUtil.close();
                return false;
            }
            Log.d(TAG, "Remote repository exists ? " + bIsRemoteRepositoryExist);
            if (bIsRemoteRepositoryExist) {
                Log.d(TAG, "try to update remote repository if local repository is not exists , branch="+aRemoteGit.getRemoteName());
                if (aGitUtil.pull(aRemoteGit.getRemoteName(), sUserName, sUserPassword)) {
                    Log.d(TAG, "pull finished!");
                    if (aGitUtil != null) aGitUtil.close();
                    return true;
                } else {
                    Log.d(TAG, "pull failed!");
                    if (aGitUtil != null) aGitUtil.close();
                    return false;
                }
            }
            if (aGitUtil != null) aGitUtil.close();
            return false;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    public static boolean cloneGit(Context context, String sRemoteUrl, String sRemoteName, String sUserName, String sUserPassword) {
        String sLocalDirectory = getLocalGitDirectory(context, sRemoteUrl);

        if (checkLocalGitRepository(context, sRemoteUrl)) {
            return false;
        }
        boolean bIsRemoteRepositoryExist = false;
        GitUtil aGitUtil;
        try {
            aGitUtil = new GitUtil(sRemoteUrl, sLocalDirectory);
            bIsRemoteRepositoryExist = aGitUtil.checkRemoteRepository(sUserName, sUserPassword);
            if (!bIsRemoteRepositoryExist) {
                Log.e(TAG, "check remote url failed");
                if (aGitUtil != null) aGitUtil.close();
                return false;
            }
            Log.d(TAG, "Remote repository exists ? " + bIsRemoteRepositoryExist);
            if (bIsRemoteRepositoryExist) {
                Log.d(TAG, "try to clone remote repository if local repository is not exists \n");
                if (aGitUtil.clone(sUserName, sUserPassword)) {
                    Log.d(TAG, "clone finished!");
                    if (aGitUtil != null) aGitUtil.close();
                    return true;
                } else {
                    Log.d(TAG, "clone failed!");
                    if (aGitUtil != null) aGitUtil.close();
                    return false;
                }
            } else if (bIsRemoteRepositoryExist && aGitUtil.checkLocalRepository()) {
                Log.d(TAG, "pull branch = " + aGitUtil.getRemoteDefaultBranch() + " , status : "
                        + aGitUtil.pull(sRemoteName, sUserName, sUserPassword));
                if (aGitUtil != null) aGitUtil.close();
                return true;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean createLocalGitRepository(Activity activity, String sLocalGitName) {
        try {
            GitUtil aGitUtil = new GitUtil("localhost://local" + File.separator + sLocalGitName + ".git", getLocalGitDirectory(activity, "localhost://local" + File.separator + sLocalGitName + ".git"));
            boolean bReturn = aGitUtil.createLocalRepository();
            if (aGitUtil != null) aGitUtil.close();
            return bReturn;
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


    public static String getLocalGitDirectory(Context context, String sRemoteUrl) {
        return Environment.getExternalStorageDirectory() +
                File.separator + PreferenceManager.getDefaultSharedPreferences(context).getString("GitLocalDirName", "gitnotetaking") + File.separator + getLocalGitDir(sRemoteUrl);
    }

    public static boolean checkLocalGitRepository(Context context, String sRemoteUrl) {
        String sLocalDirectory = getLocalGitDirectory(context, sRemoteUrl);
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
