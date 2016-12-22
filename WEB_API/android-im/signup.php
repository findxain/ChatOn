<?php

$servername = "localhost";
$username = "root";
$pass = "";
$dbname = "android_im";
// Create connection
$conn = mysqli_connect($servername, $username, $pass, $dbname);
// Check connection
if (!$conn) {
    die("Connection failed: " . mysqli_connect_error());
}
$stuff=array();
$sql ="select * from users";

$result=mysqli_query($conn,$sql);
if($result)
{
	while($row=mysqli_fetch_assoc($result))
	{
	
	echo $row;
	
	foreach($row as $key=>$value)
	{
		;
	}
	}
}





?>