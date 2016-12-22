package at.vcity.androidim.interfaces;

import android.graphics.Bitmap;

import java.io.UnsupportedEncodingException;

import at.vcity.androidim.types.Profiler;


public interface IAppManager {

	public Profiler getUserprofile();
	public String getUsername();
	public String sendMessage(String username,String tousername, String message,String messageType,String PicPath) throws UnsupportedEncodingException;
	public String authenticateUser(String usernameText, String passwordText) throws UnsupportedEncodingException; 
	public void messageReceived(String username, String message, String messagetype, String file);
//	public void setUserKey(String value);
	public boolean isNetworkConnected();
	public boolean isUserAuthenticated();
	public String getLastRawFriendList();
	public void exit();
	public String signUpUser(String usernameText, String passwordText, String email);
	public String addNewFriendRequest(String friendUsername);
	public String sendFriendsReqsResponse(String approvedFriendNames,
			String discardedFriendNames);

	public String UploadPicOfUser(Bitmap bitmap);

	public String sendVideoMessage(String  username, String  tousername, String message,String messageType,String picPath,String Vediopath);



}
