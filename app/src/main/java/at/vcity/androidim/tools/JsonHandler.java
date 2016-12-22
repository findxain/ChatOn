package at.vcity.androidim.tools;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Vector;

import at.vcity.androidim.interfaces.IUpdateData;
import at.vcity.androidim.types.FriendInfo;
import at.vcity.androidim.types.MessageInfo;
import at.vcity.androidim.types.STATUS;

/**
 * Created by Lap Mania on 3/5/2016.
 */
public class JsonHandler {

    private final String Tag="Json Handler";
    String userKey;
    String JsonString;
    private Vector<FriendInfo> mFriends = new Vector<FriendInfo>();
    private Vector<FriendInfo> mOnlineFriends = new Vector<FriendInfo>();
    private Vector<FriendInfo> mUnapprovedFriends = new Vector<FriendInfo>();

    private Vector<MessageInfo> mUnreadMessages = new Vector<MessageInfo>();

    private IUpdateData updater;




    public JsonHandler(String JsonString,IUpdateData updater)
    {
        this.JsonString=JsonString;

        this.updater = updater;
        Log.d(Tag,"Constructor");

    }

    public void Parseit()
    {
        try {


            Log.d(Tag, "ParseIt");
            this.mFriends.clear();
            this.mOnlineFriends.clear();
            this.mUnreadMessages.clear();

            JSONObject jsonObject=new JSONObject(JsonString);
            userKey=jsonObject.getString(FriendInfo.USER_KEY);

            Log.d(Tag,userKey);

            try {
                ParseFriends(jsonObject.getJSONArray("friends"));
            }catch (Exception e)
            {
                e.printStackTrace();
            }
            try {


                ParseMessage(jsonObject.getJSONArray(FriendInfo.MESSAGE));
            }catch (Exception e)
            {
                e.printStackTrace();
            }

            SendIt();










        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    private void SendIt() {

        Log.d(Tag+" SendIt", "sendIt");
        FriendInfo[] friends = new FriendInfo[mFriends.size() + mOnlineFriends.size()];
        MessageInfo[] messages = new MessageInfo[mUnreadMessages.size()];

        int onlineFriendCount = mOnlineFriends.size();
        for (int i = 0; i < onlineFriendCount; i++)
        {
            friends[i] = mOnlineFriends.get(i);
        }


        int offlineFriendCount = mFriends.size();
        for (int i = 0; i < offlineFriendCount; i++)
        {
            friends[i + onlineFriendCount] = mFriends.get(i);
        }

        int unApprovedFriendCount = mUnapprovedFriends.size();
        FriendInfo[] unApprovedFriends = new FriendInfo[unApprovedFriendCount];

        for (int i = 0; i < unApprovedFriends.length; i++) {
            unApprovedFriends[i] = mUnapprovedFriends.get(i);
        }

        int unreadMessagecount = mUnreadMessages.size();
        Log.i("MessageLOG", "mUnreadMessages="+unreadMessagecount );
        for (int i = 0; i < unreadMessagecount; i++)
        {
            messages[i] = mUnreadMessages.get(i);
            Log.i("MessageLOG", "i="+i );
        }

        this.updater.updateData(messages, friends, unApprovedFriends, userKey);



    }

    private void ParseMessage(JSONArray jsonaaa) {
        Log.d(Tag+" ParseMessage","ParseMessage");

        for(int i=0;i<jsonaaa.length();i++)
        {

            try {


            JSONObject jsonObject=jsonaaa.getJSONObject(i);
            MessageInfo message = new MessageInfo();
            message.userid = jsonObject.getString(MessageInfo.USERID);
            message.sendt = jsonObject.getString(MessageInfo.SENDT);
            message.messagetext =jsonObject.getString(MessageInfo.MESSAGETEXT);
            message.messagetype=jsonObject.getString(MessageInfo.MESSAGETYPE);
            message.file=jsonObject.getString(MessageInfo.FILE);

            Log.i("MessageLOG", message.userid + message.sendt + message.messagetext);
            mUnreadMessages.add(message);

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }


    }


    private void ParseFriends(JSONArray friends) {
        Log.d(Tag+" ParseFriends","ParseFriends");

        for(int i=0;i<friends.length();i++) {
            try {



                JSONObject jsonObject=friends.getJSONObject(i);
                Log.d(Tag+" ParseFriends",jsonObject.toString());
                FriendInfo friend = new FriendInfo();
                friend.userName = jsonObject.getString(FriendInfo.USERNAME);
                Log.d(Tag+" ParseFriends",friend.userName);
                String status=jsonObject.getString(FriendInfo.STATUS);
                Log.d(Tag+" ParseFriends",status);
                friend.ip=jsonObject.getString(FriendInfo.IP);
                Log.d(Tag+" ParseFriends",friend.ip);
                friend.port=jsonObject.getString(FriendInfo.PORT);
               Log.d(Tag+" ParseFriends",friend.port);
                friend.userKey=jsonObject.getString(FriendInfo.USER_KEY);
                Log.d(Tag+" ParseFriends",friend.userKey);


                if (status != null && status.equals("online"))
                {
                    friend.status = STATUS.ONLINE;
                    mOnlineFriends.add(friend);
                }
                else if (status.equals("unApproved"))
                {
                    friend.status = STATUS.UNAPPROVED;
                    mUnapprovedFriends.add(friend);
                }
                else
                {
                    friend.status = STATUS.OFFLINE;
                    mFriends.add(friend);
                }






            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

    }


}
