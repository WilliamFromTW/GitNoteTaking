package inmethod.gitnotetaking.utility;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import androidx.preference.PreferenceManager;

import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.api.errors.WrongRepositoryStateException;
import org.eclipse.jgit.errors.LockFailedException;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import inmethod.gitnotetaking.MyApplication;
import inmethod.gitnotetaking.db.RemoteGit;
import inmethod.gitnotetaking.db.RemoteGitDAO;
import inmethod.jakarta.vcs.GitUtil;

public class MyGitUtility {

    public static final String TAG = "GitNoteTaking";
    public static final int GIT_STATUS_SUCCESS = 0;
    public static final int GIT_STATUS_PUSH_FAIL = -1;
    public static final int GIT_STATUS_CLONING = -3;
    public static final int GIT_STATUS_PULLING = -4;
    public static boolean bGitLock = false;


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
            aReturn = aGitUtil.getRemoteBranches(aRemoteGit.getUid(),aRemoteGit.getPwd());
            if (aGitUtil != null) aGitUtil.close();
        } catch (Exception ee) {
            ee.printStackTrace();
        }
        return aReturn;
    }

    public static void setGitLock(boolean bLock){
        bGitLock = bLock;
    }

    public static boolean isGitLock(){
        return bGitLock;
    }

    public static boolean push(Context context, String sRemoteUrl) {
        RemoteGitDAO aRemoteGitDAO = new RemoteGitDAO(context);
        RemoteGit aRemoteGit = aRemoteGitDAO.getByURL(sRemoteUrl);
        if (aRemoteGit == null) return false;
        if (!MyApplication.isNetworkConnected()){
            aRemoteGit.setStatus( MyGitUtility.GIT_STATUS_PUSH_FAIL);
            aRemoteGitDAO.update(aRemoteGit);
            setGitLock(false);
            aRemoteGitDAO.close();
            return false;
        }

        setGitLock(true);
        String sLocalDirectory = getLocalGitDirectory(context, sRemoteUrl);
        boolean bIsRemoteRepositoryExist = false;
        GitUtil aGitUtil=null;
        try {
            aGitUtil = new GitUtil(sRemoteUrl, sLocalDirectory);
            bIsRemoteRepositoryExist = aGitUtil.checkRemoteRepository(aRemoteGit.getUid(), aRemoteGit.getPwd());
            if (!bIsRemoteRepositoryExist) {
                Log.e(TAG, "check remote url failed");
                aRemoteGitDAO.update(aRemoteGit);
                setGitLock(false);
                if (aGitUtil != null) aGitUtil.close();
                aRemoteGitDAO.close();
                return false;
            }
            Log.d(TAG, "Remote repository exists ? " + bIsRemoteRepositoryExist);
            if (bIsRemoteRepositoryExist) {
                Log.d(TAG, "try to push \n");
                if (aGitUtil.push(sRemoteUrl, aRemoteGit.getUid(), aRemoteGit.getPwd())) {
                    Log.d(TAG, "push finished!");
                    aRemoteGit.setStatus(MyGitUtility.GIT_STATUS_SUCCESS);
                    aRemoteGitDAO.update(aRemoteGit);
                    setGitLock(false);
                    aRemoteGitDAO.close();
                    if (aGitUtil != null) aGitUtil.close();
                    return true;
                } else {
                    if (MyApplication.isNetworkConnected()) {
                        aRemoteGit.setStatus(MyGitUtility.GIT_STATUS_PUSH_FAIL);
                    }
                    aRemoteGitDAO.update(aRemoteGit);
                    Log.d(TAG, "push failed!");
                    setGitLock(false);
                    aRemoteGitDAO.close();
                    if (aGitUtil != null) aGitUtil.close();
                    return false;
                }
            }
            setGitLock(false);

            if( aRemoteGitDAO!=null)
            aRemoteGitDAO.close();
            if (aGitUtil != null) aGitUtil.close();
            return false;

        } catch (Exception e) {
            e.printStackTrace();
        }
        setGitLock(false);
        if (aGitUtil != null) aGitUtil.close();

        return false;
    }

    public static boolean commit(Context context, String sRemoteUrl, String sCommitMessages) {

        String sLocalDirectory = getLocalGitDirectory(context, sRemoteUrl);
        GitUtil aGitUtil=null;
        RemoteGitDAO aRemoteGitDAO = new RemoteGitDAO(context);
        RemoteGit aRemoteGit = aRemoteGitDAO.getByURL(sRemoteUrl);
        if (aRemoteGit == null) return false;
        if (!MyApplication.isNetworkConnected()){
            aRemoteGitDAO.update(aRemoteGit);
            setGitLock(false);
            aRemoteGitDAO.close();
            aRemoteGitDAO = new RemoteGitDAO(context);
            aRemoteGit = aRemoteGitDAO.getByURL(sRemoteUrl);
        }
        setGitLock(true);

        Log.d(TAG, "MyGitUtility.commit()");
        try {
            aGitUtil = new GitUtil(sRemoteUrl, sLocalDirectory);
            String sAuthorName = aRemoteGit.getAuthor_name();
            String sAuthorEmail = aRemoteGit.getAuthor_email();
            aRemoteGitDAO.close();
            try {
                if (aGitUtil.commit(sCommitMessages, sAuthorName, sAuthorEmail)) {
                    aRemoteGit.setStatus(MyGitUtility.GIT_STATUS_SUCCESS);
                    Log.d(TAG, "commit finished!");
                    setGitLock(false);
                    if (aGitUtil != null) aGitUtil.close();
                    return true;
                } else {
                    Log.d(TAG, "commit failed!");
                    setGitLock(false);
                    if (aGitUtil != null) aGitUtil.close();
                    return false;
                }
            }catch(LockFailedException ee){
                ee.printStackTrace();
                Log.d(TAG, "commit failed! Got LockFailedException = " + ee.getMessage());

                Log.d(TAG, "commit failed!");
                setGitLock(false);
                FileUtility.deleteLockFile(aGitUtil);
                if (aGitUtil.commit(sCommitMessages, sAuthorName, sAuthorEmail)) {
                    aRemoteGit.setStatus(MyGitUtility.GIT_STATUS_SUCCESS);
                    if (aGitUtil != null) aGitUtil.close();
                    return true;
                }
                if (aGitUtil != null) aGitUtil.close();
                return false;

            }catch(JGitInternalException aJGitInternalException){
                Log.d(TAG, "commit failed! Got aJGitInternalException = " + aJGitInternalException.getMessage());
                aJGitInternalException.printStackTrace();
                    Log.d(TAG, "commit failed!");
                    setGitLock(false);
                    FileUtility.deleteLockFile(aGitUtil);
                    if (aGitUtil.commit(sCommitMessages, sAuthorName, sAuthorEmail)) {
                        aRemoteGit.setStatus(MyGitUtility.GIT_STATUS_SUCCESS);
                        if (aGitUtil != null) aGitUtil.close();
                        return true;
                    }
                    if (aGitUtil != null) aGitUtil.close();
                    return false;
            }
        } catch (Exception e) {
            Log.d(TAG, "commit failed! Got Exception = " + e.getMessage());
            e.printStackTrace();
        }
        setGitLock(false);
        if (aGitUtil != null) aGitUtil.close();
        return false;
    }

    public static boolean deleteByRemoteUrl(Context context, String sRemoteUrl) {
        RemoteGitDAO aRemoteGitDAO = new RemoteGitDAO(context);
        boolean sReturn = aRemoteGitDAO.delete(sRemoteUrl);
        aRemoteGitDAO.close();
        return sReturn;
    }

    public static void backup(Context context,String sRemoteUrl,String sBackupDestLocation) throws Exception {
        String sLocalDirectory = getLocalGitDirectory(context, sRemoteUrl);
        GitUtil  aGitUtil = new GitUtil(sRemoteUrl, sLocalDirectory);
        aGitUtil.backup(sBackupDestLocation);
        aGitUtil.close();
    }

    public static ArrayList<RemoteGit> getRemoteGitList(Context context) {
        RemoteGitDAO aRemoteGitDAO = new RemoteGitDAO(context);
        ArrayList<RemoteGit> aList = aRemoteGitDAO.getAll();
        aRemoteGitDAO.close();
        return aList;
    }

    public static RemoteGit getRemoteGit(Context context,String sRemoteUrl) {
        RemoteGitDAO aRemoteGitDAO = new RemoteGitDAO(context);
        RemoteGit aReturn = aRemoteGitDAO.getByURL(sRemoteUrl);
        aRemoteGitDAO.close();
        return aReturn;
    }

    public static List<RevCommit> getLocalCommitIdListByFilePath(Context context, String sRemoteUrl,String sFilePath) {
        GitUtil aGitUtil;
        List<RevCommit> aList = null;
        try {
            String sLocalDirectory = getLocalGitDirectory(context, sRemoteUrl);
            aGitUtil = new GitUtil(sRemoteUrl, sLocalDirectory);
            aList = aGitUtil.getLocalCommitIdListByFilePath(sFilePath);
            if (aGitUtil != null) aGitUtil.close();
        } catch (Exception ee) {
            ee.printStackTrace();
        }
        return aList;
    }


    public static List<RevCommit> getLocalCommitLogList(Context context, String sRemoteUrl) {
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
        setGitLock(true);

        try {
            aGitUtil = new GitUtil(sRemoteUrl, sLocalDirectory);
                bReturn = aGitUtil.checkout(aRemoteGit.getBranch());
            aGitUtil.close();
        }catch (Exception ee){
            Log.d(TAG,ee.getLocalizedMessage());
        }
        setGitLock(false);
        return bReturn;
    }

    public static String getLocalBranchName(Context context,String sRemoteUrl){
        String sLocalDirectory = getLocalGitDirectory(context, sRemoteUrl);
        GitUtil aGitUtil;
        try {
            aGitUtil = new GitUtil(sRemoteUrl, sLocalDirectory);
            String sReturn = aGitUtil.getLocalDefaultBranch();
            aGitUtil.close();
            return sReturn;
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return null;
    }

    public static boolean pull(Context context, String sRemoteUrl) {
        RemoteGitDAO aRemoteGitDAO = new RemoteGitDAO(context);
        RemoteGit aRemoteGit = aRemoteGitDAO.getByURL(sRemoteUrl);
        if (aRemoteGit == null) return false;
        setGitLock(true);

        String sUserName = aRemoteGit.getUid();
        aRemoteGit.setStatus(GIT_STATUS_PULLING);
        aRemoteGitDAO.update(aRemoteGit);
        String sUserPassword = aRemoteGit.getPwd();
        String sLocalDirectory = getLocalGitDirectory(context, sRemoteUrl);
        boolean bIsRemoteRepositoryExist = false;
        GitUtil aGitUtil;
        try {
            aGitUtil = new GitUtil(sRemoteUrl, sLocalDirectory);
            aGitUtil.setContentMergeStrategyOURS();
            bIsRemoteRepositoryExist = aGitUtil.checkRemoteRepository(sUserName, sUserPassword);
            if (!bIsRemoteRepositoryExist) {
                aRemoteGitDAO.update(aRemoteGit);
                Log.e(TAG, "check remote url failed");
                setGitLock(false);
                if (aGitUtil != null) aGitUtil.close();
                return false;
            }
            Log.d(TAG, "Remote repository exists ? " + bIsRemoteRepositoryExist);
            if (bIsRemoteRepositoryExist) {
                Log.d(TAG, "try to pull remote repository , branch="+aRemoteGit.getRemoteName());
                try{
                  if (aGitUtil.pull(aRemoteGit.getRemoteName(), sUserName, sUserPassword)) {
                    Log.d(TAG, "pull finished!");
                    aRemoteGit.setStatus(GIT_STATUS_SUCCESS);
                    aRemoteGitDAO.update(aRemoteGit);
                    setGitLock(false);
                    if (aGitUtil != null) aGitUtil.close();
                    return true;
                  } else {
                      aGitUtil.reset(ResetCommand.ResetType.MIXED, PreferenceManager.getDefaultSharedPreferences(context).getString("GitRemoteName", "master"));
                      aRemoteGitDAO.update(aRemoteGit);
                      Log.d(TAG, "pull failed!");
                      setGitLock(false);
                      if (aGitUtil != null) aGitUtil.close();
                      return false;
                  }
                }catch(LockFailedException lockfail){
                    lockfail.printStackTrace();
                    aRemoteGitDAO.update(aRemoteGit);
                    Log.d(TAG, "pull failed!");
                    setGitLock(false);
                    FileUtility.deleteLockFile(aGitUtil);
                    if (aGitUtil != null) aGitUtil.close();
                    return false;
                }catch(JGitInternalException aJGitInternalException){
                        aRemoteGitDAO.update(aRemoteGit);
                        Log.d(TAG, "pull failed!");
                        setGitLock(false);
                        FileUtility.deleteLockFile(aGitUtil);
                        if (aGitUtil != null) aGitUtil.close();
                        return false;
                }catch(WrongRepositoryStateException asd){
                    asd.printStackTrace();
                    try {
                        FileUtility.deleteLockFile(aGitUtil);
                        aGitUtil.reset(ResetCommand.ResetType.HARD, "HEAD");
                    }catch (Exception resetEx){
                        Log.e(TAG,"reset exception");
                        resetEx.printStackTrace();
                    }
                    aRemoteGitDAO.update(aRemoteGit);
                    Log.d(TAG, "pull failed!");
                    setGitLock(false);
                    if (aGitUtil != null) aGitUtil.close();
                    return false;
                }
            }
            aRemoteGitDAO.update(aRemoteGit);
            setGitLock(false);
            if (aGitUtil != null) aGitUtil.close();
            if( aRemoteGit!=null )
            aRemoteGitDAO.close();
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

        setGitLock(true);

        boolean bIsRemoteRepositoryExist = false;
        GitUtil aGitUtil;
        try {
            aGitUtil = new GitUtil(sRemoteUrl, sLocalDirectory);
            bIsRemoteRepositoryExist = aGitUtil.checkRemoteRepository(sUserName, sUserPassword);
            if (!bIsRemoteRepositoryExist) {
                Log.e(TAG, "check remote url failed");
                setGitLock(false);
                if (aGitUtil != null) aGitUtil.close();
                return false;
            }
            Log.d(TAG, "Remote repository exists ? " + bIsRemoteRepositoryExist);
            if (bIsRemoteRepositoryExist) {
                Log.d(TAG, "try to clone remote repository if local repository is not exists \n");
                if (aGitUtil.clone(sUserName, sUserPassword,1)) {
                    Log.d(TAG, "clone finished!");
                    setGitLock(false);
                    if (aGitUtil != null) aGitUtil.close();
                    return true;
                } else {
                    Log.d(TAG, "clone failed!");
                    setGitLock(false);
                    if (aGitUtil != null) aGitUtil.close();
                    return false;
                }
            } else if (bIsRemoteRepositoryExist && aGitUtil.checkLocalRepository()) {
                Log.d(TAG, "pull branch = " + aGitUtil.getRemoteDefaultBranch() + " , status : "
                        + aGitUtil.pull(sRemoteName, sUserName, sUserPassword));
                setGitLock(false);
                if (aGitUtil != null) aGitUtil.close();
                return true;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
        }
        setGitLock(false);
        return false;
    }

    public static boolean createLocalGitRepository(Activity activity, String sLocalGitName) {
        try {
            Log.d(TAG,"Local git directory = "+getLocalGitDirectory(activity, "localhost://local" + File.separator + sLocalGitName + ".git"));
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
        return context.getExternalFilesDir("")+
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
