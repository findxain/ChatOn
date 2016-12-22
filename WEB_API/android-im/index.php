<?php
/****************************************
*		Server of Android IM Application
*
*		Author: ahmet oguz mermerkaya
* 		Email: ahmetmermerkaya@hotmail.com
*		Editor: Dominik Pirngruber
*		Email: d.pirngruber@gmail.com
* 		Date: Jun, 25, 2013   	
* 	
*		Supported actions: 
*			1.  authenticateUser
*			    if user is authentiated return friend list
* 		    
*			2.  signUpUser
* 		
*			3.  addNewFriend
* 		
* 			4.  responseOfFriendReqs
*
*			5.  testWebAPI
*************************************/


//TODO:  show error off

require_once("mysql.class.php");

$dbHost = "localhost";
$dbUsername = "root";
$dbPassword = "";
$dbName = "android-im";


$db = new MySQL($dbHost,$dbUsername,$dbPassword,$dbName);

// if operation is failed by unknown reason
define("FAILED", 0);

define("SUCCESSFUL", 1);
// when  signing up, if username is already taken, return this error
define("SIGN_UP_USERNAME_CRASHED", 2);  
// when add new friend request, if friend is not found, return this error 
define("ADD_NEW_USERNAME_NOT_FOUND", 2);

// TIME_INTERVAL_FOR_USER_STATUS: if last authentication time of user is older 
// than NOW - TIME_INTERVAL_FOR_USER_STATUS, then user is considered offline
define("TIME_INTERVAL_FOR_USER_STATUS", 60);

define("USER_APPROVED", 1);
define("USER_UNAPPROVED", 0);


$username = (isset($_REQUEST['username']) && count($_REQUEST['username']) > 0) 
							? $_REQUEST['username'] 
							: NULL;
$password = isset($_REQUEST['password']) ? md5($_REQUEST['password']) : NULL;
$port = isset($_REQUEST['port']) ? $_REQUEST['port'] : NULL;

$action = isset($_REQUEST['action']) ? $_REQUEST['action'] : NULL;
	
$videofilelocation="";

if ($action == "testWebAPI")
{
	if ($db->testconnection()){
	echo SUCCESSFUL;
	exit;
	}else{
	echo FAILED;
	exit;
	}
}


if ($username == NULL || $password == NULL)	 
{
	
	exit;
}

$out = NULL;

error_log($action."\r\n", 3, "error.log");

switch($action) 
{
	
	
	case "uploadvideo":
	
		//echo "UploadVideo";
	if($userId = authenticateUser($db, $username, $password))
	{	
		if($_SERVER['REQUEST_METHOD']=='POST')
		{
		$file_name = $_FILES['myFile']['name'];
		$file_size = $_FILES['myFile']['size'];
		$file_type = $_FILES['myFile']['type'];
		$temp_name = $_FILES['myFile']['tmp_name'];
 
		$location = "video/";
		move_uploaded_file($temp_name, $location.$file_name);
		echo $temp_name."      ".$file_name."         ".$location.$file_name;
		if (isset($_REQUEST['to']))
		{
			 $tousername = $_REQUEST['to'];	
			 $message = $_REQUEST['message'];
			$type=$_REQUEST['type'];
			$filename="";
			if(strcmp($type,"video")==0)
			{
				
				$filename=$location.$file_name;
			}
				
			 $sqlto = "select Id from  users where username = '".$tousername."' limit 1";
			 
			 
		
					if ($resultto = $db->query($sqlto))			
					{
						while ($rowto = $db->fetchObject($resultto))
						{
							$uto = $rowto->Id;
						}
						$sql22 = "INSERT INTO `messages` (`fromuid`, `touid`, `sentdt`, `messagetext`,`type`,`file`) VALUES ('".$userId."', '".$uto."', '".DATE("Y-m-d H:i")."', '".$message."','".$type."','".$filename."');";						
						 					
			 					error_log("$sql22", 3 , "error_log");
							if ($db->query($sql22))	
							{
							 		$out = SUCCESSFUL;
							}				
							else {
									$out = FAILED;
							}				 		
						$resultto = NULL;
					}	
			 				 	 	
		$sqlto = NULL;
		}
		$out=SUCCESSFUL;
		}
		else{
			//echo "<br/>upload Fail";
		$out=FAILED;
		}
	}else
	{
		$out=FAILED;
	}
	break;
	
	case "uploadProfilePic":
		
		if ($userId = authenticateUser($db, $username, $password)) 
		{
			HandleImg($_REQUEST['pic'],$username,"userpic");
			$sql="update users SET pic='userpic/".$username.".png' where username='".$username."';";
			if($result=$db->query($sql))
			{
				$out=SUCCESSFUL;
				
				
			}
			else
			{
				$out=FAILED;
			}
			
		}
		break;
	
	case "getuserprofile":
	//echo "get user profile<br/>";
	
	
	if ($userId = authenticateUser($db, $username, $password)) 
		{
			$sql ="select * from users where username='".$username."';";
			if($result=$db->query($sql))
			{
				$stuff =array();
				while ($row = $db->fetchObject($result))
					{
						$stuff['id']=$row->Id;
						$stuff['username']=$row->username;
						$stuff['email']=$row->email;
						$stuff['ip']=$row->IP;
						$stuff['pic']=base64_encode_image($row->pic,'png');
					}
				
				
				
				echo json_encode($stuff);
			}else
				$out=FAILED;
			
		}
	
	break;
	
	case "authenticateUserJson":
		if ($userId = authenticateUser($db, $username, $password)) 
		{
			
			
			$sql = "select u.Id, u.username, (NOW()-u.authenticationTime) as authenticateTimeDifference, u.IP, 
										f.providerId, f.requestId, f.status, u.port 
							from friends f
							left join users u on 
										u.Id = if ( f.providerId = ".$userId.", f.requestId, f.providerId ) 
							where (f.providerId = ".$userId." and f.status=".USER_APPROVED.")  or 
										 f.requestId = ".$userId."  GROUP BY u.Id";
										 
			//$sqlmessage = "SELECT * FROM `messages` WHERE `touid` = ".$userId." AND `read` = 0 LIMIT 0, 30 ";
			
			$sqlmessage = "SELECT m.id, m.fromuid, m.touid, m.sentdt, m.read, m.readdt, m.messagetext,m.type,m.file, u.username from messages m \n"
    . "left join users u on u.Id = m.fromuid WHERE `touid` = ".$userId." AND `read` = 0 LIMIT 0, 30 ";
			
	
			if ($result = $db->query($sql))			
			{
			//	echo "Started\n";
				//$count=0;
				$stuff=array();
				$stuff['userKey']=$userId;
					$FriendsStatusArray=array();
					while($row = $db->fetchObject($result))
					{
						//
						$tempArray=array();
						$status = "offline";
						if (((int)$row->status) == USER_UNAPPROVED)
						{
							$status = "unApproved";
						}
						else if (((int)$row->authenticateTimeDifference) < TIME_INTERVAL_FOR_USER_STATUS)
						{
							$status = "online";
							 
						}
						$tempArray['username']=$row->username;
						$tempArray['status']=$status;
						$tempArray['IP']=$row->IP;
						$tempArray['userKey']=$row->Id;
						$tempArray['port']=$row->port;//Port is giving error my be because of Im not testing from mobile so thats why?  :(
						//echo "port ".$row->port;
						//echo json_encode($tempArray);
						$FriendsStatusArray[]=$tempArray;
						//echo "FriendsArray".json_encode($FriendsStatusArray);
						
						//$out .= "<friend  username = '".$row->username."'  status='".$status."' IP='".$row->IP."' userKey = '".$row->Id."'  port='".$row->port."'/>";
						
						
					}
					$stuff['friends']=$FriendsStatusArray;
					
					$stuffMessage=array();
					
						if ($resultmessage = $db->query($sqlmessage))							
							{
								$temp=array();
							while ($rowmessage = $db->fetchObject($resultmessage))
								{
								//$out .= "<message  from='".$rowmessage->username."'  sendt='".$rowmessage->sentdt."' text='".$rowmessage->messagetext."' />";
								$sqlendmsg = "UPDATE `messages` SET `read` = 1, `readdt` = '".DATE("Y-m-d H:i")."' WHERE `messages`.`id` = ".$rowmessage->id.";";
								$db->query($sqlendmsg);
								$temp['from']=$rowmessage->username;
								$temp['sendt']=$rowmessage->sentdt;
								$temp['text']=$rowmessage->messagetext;
								$msgType=$rowmessage->type;
								$temp['type']=$msgType;
								if(strcmp($msgType,"pic")==0)
								{
									$temp['file']=base64_encode_image($rowmessage->file,'png');
								}
								else if(strcmp($msgType,"video")==0)
								{
									$temp['file']=$rowmessage->file;
								}
								else{$temp['file']="";}
								$stuffMessage[]=$temp;
								}
								$stuff['message']=$stuffMessage;
								
							}
					
			echo json_encode($stuff);
			
		}else
			
			{
				//$out=FAILED;
				
			}
	
		}
	break;
	
	case "authenticateUser":
		
		
		if ($userId = authenticateUser($db, $username, $password)) 
		{					
			
			// providerId and requestId is Id of  a friend pair,
			// providerId is the Id of making first friend request
			// requestId is the Id of the friend approved the friend request made by providerId
			
			// fetching friends, 
			// left join expression is a bit different, 
			//		it is required to fetch the friend, not the users itself
			
			$sql = "select u.Id, u.username, (NOW()-u.authenticationTime) as authenticateTimeDifference, u.IP, 
										f.providerId, f.requestId, f.status, u.port 
							from friends f
							left join users u on 
										u.Id = if ( f.providerId = ".$userId.", f.requestId, f.providerId ) 
							where (f.providerId = ".$userId." and f.status=".USER_APPROVED.")  or 
										 f.requestId = ".$userId." ";
										 
			//$sqlmessage = "SELECT * FROM `messages` WHERE `touid` = ".$userId." AND `read` = 0 LIMIT 0, 30 ";
			
			$sqlmessage = "SELECT m.id, m.fromuid, m.touid, m.sentdt, m.read, m.readdt, m.messagetext, u.username from messages m \n"
    . "left join users u on u.Id = m.fromuid WHERE `touid` = ".$userId." AND `read` = 0 LIMIT 0, 30 ";
			
	
			if ($result = $db->query($sql))			
			{
					$out .= "<data>"; 
					$out .= "<user userKey='".$userId."' />";
					while ($row = $db->fetchObject($result))
					{
						$status = "offline";
						if (((int)$row->status) == USER_UNAPPROVED)
						{
							$status = "unApproved";
						}
						else if (((int)$row->authenticateTimeDifference) < TIME_INTERVAL_FOR_USER_STATUS)
						{
							$status = "online";
							 
						}
						$out .= "<friend  username = '".$row->username."'  status='".$status."' IP='".$row->IP."' userKey = '".$row->Id."'  port='".$row->port."'/>";
												
												// to increase security, we need to change userKey periodically and pay more attention
												// receiving message and sending message 
						
					}
						if ($resultmessage = $db->query($sqlmessage))			
							{
							while ($rowmessage = $db->fetchObject($resultmessage))
								{
								$out .= "<message  from='".$rowmessage->username."'  sendt='".$rowmessage->sentdt."' text='".$rowmessage->messagetext."' />";
								$sqlendmsg = "UPDATE `messages` SET `read` = 1, `readdt` = '".DATE("Y-m-d H:i")."' WHERE `messages`.`id` = ".$rowmessage->id.";";
								$db->query($sqlendmsg);
								}
							}
					$out .= "</data>";
			}
			else
			{
				$out = FAILED;
			}			
		}
		else
		{
				// exit application if not authenticated user
				$out = "AuthenticationFAiled";
		}
		
	break;
	
	case "signUpUser":
		if (isset($_REQUEST['email']))
		{
			 $email = $_REQUEST['email'];		
			 	
			 $sql = "select Id from  users 
			 				where username = '".$username."' limit 1";
			 
		
			 				
			 if ($result = $db->query($sql))
			 {
			 		if ($db->numRows($result) == 0) 
			 		{
			 				$sql = "insert into users(username, password, email)
			 					values ('".$username."', '".$password."', '".$email."') ";		 					
						 					
			 					error_log("$sql", 3 , "error_log");
							if ($db->query($sql))	
							{
							 		$out = SUCCESSFUL;
							}				
							else {
									$out = FAILED;
							}				 			
			 		}
			 		else
			 		{
			 			$out = SIGN_UP_USERNAME_CRASHED;
			 		}
			 }				 	 	
		}
		else
		{
			$out = FAILED;
		}	
	break;
	
	case "sendMessage":
		
	if ($userId = authenticateUser($db, $username, $password)) 
		{	
		if (isset($_REQUEST['to']))
		{
			 $tousername = $_REQUEST['to'];	
			 $message = $_REQUEST['message'];
			$type=$_REQUEST['type'];
			$filename="";
			if(strcmp($type,"pic")==0)
			{
				$pic=$_REQUEST['file'];
				 $filename= uniqid(rand(), true);
				HandleImg($pic,$filename,"messagepic");		
				
			}if(strcmp($type,"video")==0)
			{
				$filename=$videofilelocation;
			}
			//echo $tousername;
				
			 $sqlto = "select Id from  users where username = '".$tousername."' limit 1;";
			 
			 //echo $sqlto;
			 
			 
		
					if ($resultto = $db->query($sqlto))			
					{
						
						while ($rowto = $db->fetchObject($resultto))
						{
							$uto = $rowto->Id;
						}
						$sql22 = "INSERT INTO `messages` (`fromuid`, `touid`, `sentdt`, `messagetext`,`type`,`file`) VALUES ('".$userId."', '".$uto."', '".DATE("Y-m-d H:i")."', '".$message."','".$type."','messagepic/".$filename.".png');";						
						 					
											//echo $sql22;
			 					
							if ($db->query($sql22))	
							{
							 		$out = SUCCESSFUL;
							}				
							else {
									$out = "Query Failed in sendMessage ".$sql22;
									//echo error_log($sql22, 3 , "error_log");
							}				 		
						$resultto = NULL;
					}else{
						//$out = "FAiled";
					}	
			 				 	 	
		$sqlto = NULL;
		}else{
			$out = "to Error";
		}
		}
		else
		{
			//$out = FAILED;
			$out = "authenticationFailed";
		}	
	break;
	
	case "addNewFriend":
		$userId = authenticateUser($db, $username, $password);
		if ($userId != NULL)
		{
			
			if (isset($_REQUEST['friendUserName']))			
			{				
				 $friendUserName = $_REQUEST['friendUserName'];
				 
				 $sql = "select Id from users 
				 				 where username='".$friendUserName."' 
				 				 limit 1";
				 if ($result = $db->query($sql))
				 {
				 		if ($row = $db->fetchObject($result))
				 		{
				 			 $requestId = $row->Id;
				 			 
				 			 if ($row->Id != $userId)
				 			 {
				 			 		 $sql = "insert into friends(providerId, requestId, status)
				 				  		 values(".$userId.", ".$requestId.", ".USER_UNAPPROVED.")";
							 
									 if ($db->query($sql))
									 {
									 		$out = SUCCESSFUL;
									 }
									 else
									 {
									 		$out = FAILED;
									 }
							}
							else
							{
								$out = FAILED;  // user add itself as a friend
							} 		 				 				  		 
				 		}
				 		else
				 		{
				 			$out = FAILED;			 			
				 		}
				 }				 				 
				 else
				 {
				 		$out = FAILED;
				 }				
			}
			else
			{
					$out = FAILED;
			} 			
		}
		else
		{
			$out = FAILED;
		}	
	break;
	
	case "responseOfFriendReqs":
		$userId = authenticateUser($db, $username, $password);
		if ($userId != NULL)
		{
			$sqlApprove = NULL;
			$sqlDiscard = NULL;
			if (isset($_REQUEST['approvedFriends']))
			{
				  $friendNames = split(",", $_REQUEST['approvedFriends']);
				  $friendCount = count($friendNames);
				  $friendNamesQueryPart = NULL;
				  for ($i = 0; $i < $friendCount; $i++)
				  {
				  	if (strlen($friendNames[$i]) > 0)
				  	{
				  		if ($i > 0 )
				  		{
				  			$friendNamesQueryPart .= ",";
				  		}
				  		
				  		$friendNamesQueryPart .= "'".$friendNames[$i]."'";
				  		
				  	}			  	
				  	
				  }
				  if ($friendNamesQueryPart != NULL)
				  {
				  	$sqlApprove = "update friends set status = ".USER_APPROVED."
				  					where requestId = ".$userId." and 
				  								providerId in (select Id from users where username in (".$friendNamesQueryPart."));
				  				";		
				  }
				  				  
			}
			if (isset($_REQUEST['discardedFriends']))
			{
					$friendNames = split(",", $_REQUEST['discardedFriends']);
				  $friendCount = count($friendNames);
				  $friendNamesQueryPart = NULL;
				  for ($i = 0; $i < $friendCount; $i++)
				  {
				  	if (strlen($friendNames[$i]) > 0)
				  	{
				  		if ($i > 0 )
				  		{
				  			$friendNamesQueryPart .= ",";
				  		}
				  		
				  		$friendNamesQueryPart .= "'".$friendNames[$i]."'";
				  		
				  	}				  	
				  }
				  if ($friendNamesQueryPart != NULL)
				  {
				  	$sqlDiscard = "delete from friends 
				  						where requestId = ".$userId." and 
				  									providerId in (select Id from users where username in (".$friendNamesQueryPart."));
				  							";
				  }						
			}
			if (  ($sqlApprove != NULL ? $db->query($sqlApprove) : true) &&
						($sqlDiscard != NULL ? $db->query($sqlDiscard) : true) 
			   )
			{
				$out = SUCCESSFUL;
			}
			else
			{
				$out = FAILED;
			}		
		}
		else
		{
			$out = FAILED;
		}
	break;
	
	default:
		$out = "Default Break";		
		break;	
}

echo $out;



///////////////////////////////////////////////////////////////
function authenticateUser($db, $username, $password)
{
	
	$sql22 = "select * from users 
					where username = '".$username."' and password = '".$password."' 
					limit 1";
	
	$out = NULL;
	if ($result22 = $db->query($sql22))
	{
		if ($row22 = $db->fetchObject($result22))
		{
				$out = $row22->Id;
				
				$sql22 = "update users set authenticationTime = NOW(), 
																 IP = '".$_SERVER["REMOTE_ADDR"]."' ,
																 port = 15145 
								where Id = ".$row22->Id."
								limit 1";
				
				$db->query($sql22);				
								
								
		}		
	}
	
	return $out;
}

function authenticateUserJson($db, $username, $password)
{
	
	$sql22 = "select * from users 
					where username = '".$username."' and password = '".$password."' 
					limit 1";
	
	$out = NULL;
	if ($result22 = $db->query($sql22))
	{
		if ($row22 = $db->fetchObject($result22))
		{
				$out = $row22->Id;
				$sql22 = "update users set authenticationTime = NOW(), 
																 IP = '".$_SERVER["REMOTE_ADDR"]."' ,
																 port = 15145 
								where Id = ".$row22->Id."
								limit 1";
				$db->query($sql22);					
		}		
	}
	
	return $out;
}
function HandleImg($img,$username,$location)
{
	$img = str_replace('data:image/png;base64,', '', $img);
	$img = str_replace(' ', '+', $img);
	$data = base64_decode($img);
	file_put_contents($location.'/'.$username.'.png', $data);

}
function base64_encode_image ($filename=string,$filetype=string) {
    if ($filename) {
        $imgbinary = fread(fopen($filename, "r"), filesize($filename));
        //return 'data:image/' . $filetype . ';base64,' . base64_encode($imgbinary);
		return base64_encode($imgbinary);
    }
}
?>