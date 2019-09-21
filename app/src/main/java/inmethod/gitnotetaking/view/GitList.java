package inmethod.gitnotetaking.view;

import android.app.Application;
import android.graphics.Color;
import android.view.View;
import android.widget.TextView;
import inmethod.gitnotetaking.R;

public class GitList {

    private String sRemoteUrl = null;
    private String sGitName = null;
    private int iPushStatus = PUSH_SUCCESS;
    public static final int PUSH_SUCCESS = 0;
    public static final int PUSH_FAIL = -1;
    public static final int CLONING = -3;
    private GitList(){

    }
    public GitList(String sGitName,String sRemoteUrl,int iPushStatus){
        this.sGitName = sGitName;
        this.sRemoteUrl = sRemoteUrl;
        this.iPushStatus = iPushStatus;
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
    public int getPushStatus(){
        return iPushStatus;
    }
    public void setPushStatus(int iPushStatus){
        this.iPushStatus = iPushStatus;
    }


    public static void mapDeviceInfoToLayout(Object[] layoutData, Object aGitListObject ){
        GitList aGitList = (GitList)aGitListObject;
        TextView layout0 = ((TextView)layoutData[0]);

        if( aGitList.getRemoteUrl().indexOf("local")==-1) {
            if (aGitList.getPushStatus() == PUSH_FAIL) {
                layout0.setTextColor(Color.RED);
                layout0.setText(aGitList.getGitName()+"(long click to push)");
            }
            else  if (aGitList.getPushStatus() == CLONING) {
                layout0.setTextColor(Color.RED);
                layout0.setText(aGitList.getGitName()+" is cloning ...");
            }else
                layout0.setText(aGitList.getGitName());


        }
        ((TextView)layoutData[1]).setText( aGitList.getRemoteUrl());

    }

    public static Object[] getDeviceInfoFromLayoutId(View view){
        TextView[] aLayoutData = new TextView[2];
        aLayoutData[0] = (TextView) view.findViewById(R.id.git_name);
        aLayoutData[1] = (TextView) view.findViewById(R.id.git_remote_url);
        return aLayoutData;
    }
}
