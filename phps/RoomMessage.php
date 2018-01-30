<?php
	if ($_SERVER['REQUEST_METHOD'] == 'POST') {
		$login	    = clear($_POST['LOGIN']);
		$pass	    = $_POST['PASS'];
		$iv	 	    = $_POST['IV'];
		$msg	    = $_POST['MESSAGE'];
		$destiny	= clear($_POST['DESTINY']);
			
		if (empty($login)) {
			
			echo "Dados de login vazios";
				
		} else if (empty($pass)) {
			
			echo "Senha vazia";
			
		} else {

			$server = 'localhost';
			$user = 'id2094952_appdata';
			$senha = 'AppData123';
			$conn = mysqli_connect($server, $user, $senha, "id2094952_appdata");
				
			$query = mysqli_query($conn, "SELECT user FROM usuarios WHERE user = '".$login."'");
			$result = $query->fetch_assoc();
				
			if (isset($result["user"])) {
				$query = mysqli_query($conn, "SELECT pass FROM usuarios WHERE user = '".$login."'");
				$result = $query->fetch_assoc();
				$hpass = base64_decode($result["pass"]);
				$pass = openssl_decrypt($pass, "aes-128-cbc", $hpass, OPENSSL_ZERO_PADDING, md5($iv, true));

				if (md5(trim($pass, hex2bin("100f0e0d0c0b0a090807060504030201")), true) == $hpass) {
				    $query = mysqli_query($conn, "SELECT participantes FROM salas WHERE nome = '".$destiny."'");
				    $result = $query->fetch_assoc();
				    if (isset($result['participantes'])) {
					    $tempus = date("Y-m-d H:i:s", time());
				        $partarray = explode(";", $result['participantes']);
				        for ($c = 0; isset($partarray[$c]); $c++) {
				            if (empty($partarray[$c])) {
				                
				            } else if ($partarray[$c] != $login) {
					            $query = mysqli_query($conn, "INSERT INTO messages(sender, destiny, message, quando) VALUES ('"."<".$destiny."', '".$partarray[$c]."', '".$login.": ".$msg."', '".$tempus."')");
				            }
				        }
				    }
					echo "lcon";
				} else {
					echo "lgus";
				}
			} else {
				echo "uuser";
			}
		}
	}
	
	function clear ($msg) {
	    trim($msg, "\\=<>\"'/&");
	    return $msg;
	}
?>
