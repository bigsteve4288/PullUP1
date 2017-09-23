<?php
// array for JSON response
$response = array();

// check for required fields
if (isset($_POST['eventtype']) && isset($_POST['eventname']) && isset($_POST['eventAddress']) && isset($_POST['eventstarttime'])  && isset($_POST['eventhost']) 
	&& isset($_POST['eventstatus']) && isset($_POST['eventimage'])) 
	{
 
    $eventtype = $_POST['eventtype'];
    $eventname = $_POST['eventname'];
    $eventAddress = $_POST['eventAddress'];
	$eventstarttime = $_POST['eventstarttime'];
    $eventhost = $_POST['eventhost'];
    $eventstatus = $_POST['eventstatus'];
    $eventimage = $_POST['eventimage'];

 
// include db connect class
require_once __DIR__ . '/db_connect.php';
 
// connecting to db
$db = new DB_CONNECT();

 // mysql inserting a new row
    $result = mysql_query("insert into pullup (`eventtype`, `eventname`, `eventAddress`, `eventstarttime`, `eventhost`, `eventstatus`, `eventimage`)  values('$eventtype','$eventname','$eventAddress','$eventstarttime','$eventhost','$eventstatus','$eventimage') ");
	
// check if row inserted or not
    if ($result) {
        // successfully inserted into database
        $response["success"] = 1;
        $response["message"] = "Product successfully created.";
 
        // echoing JSON response
        echo json_encode($response);
    } else {
        // failed to insert row
        $response["success"] = 0;
        $response["message"] = "Oops! An error occurred.";
 
        // echoing JSON response
        echo json_encode($response);
    }
} else {
    // required field is missing
    $response["success"] = 0;
    $response["message"] = "Required field(s) is missing";
 
    // echoing JSON response
    echo json_encode($response);
}
?>	

 
   