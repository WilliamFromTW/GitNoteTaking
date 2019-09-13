package inmethod.gitnotetaking.view;

import android.view.View;
import android.widget.TextView;
import inmethod.gitnotetaking.R;

public class GitList {

    private String sRemoteUrl = null;
    private String sGitName = null;

    private GitList(){

    }
    public GitList(String sGitName,String sRemoteUrl){
        this.sGitName = sGitName;
        this.sRemoteUrl = sRemoteUrl;
    }

    public String getRemoteUrl(){
        return sRemoteUrl;
    }
    public void setsRemoteUrl(String sRemoteUrl){
        this.sRemoteUrl = sRemoteUrl;
    }

    public String getGitName(){
        return sGitName;
    }
    public void setGitName(String sGitName){
        this.sGitName = sGitName;
    }


    public static void mapDeviceInfoToLayout(Object[] layoutData, Object aGitListObject ){
        GitList aGitList = (GitList)aGitListObject;
        ((TextView)layoutData[0]).setText(aGitList.getGitName());
        ((TextView)layoutData[1]).setText( aGitList.getRemoteUrl());

    }

    public static Object[] getDeviceInfoFromLayoutId(View view){
        TextView[] aLayoutData = new TextView[2];
        aLayoutData[0] = (TextView) view.findViewById(R.id.git_name);
        aLayoutData[1] = (TextView) view.findViewById(R.id.git_remote_url);
        return aLayoutData;
    }
}
