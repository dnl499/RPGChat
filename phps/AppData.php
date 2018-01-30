<?php
	if ($_SERVER['REQUEST_METHOD'] == 'POST') {
		$login	 = clear($_POST['LOGIN']);
		$pass	 = $_POST['PASS'];
		$signup	 = $_POST['SIGN'];
		$iv	     = $_POST['IV'];
		if (isset($_POST['MAIL'])) {
			$mail = clear($_POST['MAIL']);
		} else {
			$mail = "";
		}
			
		if (empty($login) or empty($pass) or empty($signup)) {
			
			echo "Dados de login inválidos";
				
		} else {

			$server = 'localhost';
			$user = 'id2094952_appdata';
			$senha = 'AppData123';
			$conn = mysqli_connect($server, $user, $senha, "id2094952_appdata");
				
			$query = mysqli_query($conn, "SELECT user FROM usuarios WHERE user = '".$login."'");
			$result = $query->fetch_assoc();
				
			if (isset($result["user"])) {
				if ($signup == "N") {
					$query = mysqli_query($conn, "SELECT pass FROM usuarios WHERE user = '".$login."'");
					$result = $query->fetch_assoc();
					$hpass = base64_decode($result["pass"]);
					$pass = openssl_decrypt($pass, "aes-128-cbc", $hpass, OPENSSL_ZERO_PADDING, md5($iv, true));
						
					if (md5(trim($pass, hex2bin("100f0e0d0c0b0a090807060504030201")), true) == $hpass) {
						echo "lgsc";
					    $query = mysqli_query($conn, "UPDATE usuarios SET online = '".time()."' WHERE user = '".$login."'");
					} else {
						echo "lgus";
					}
				} else {
					echo "uexi";
				}
			} else if ($signup == "Y") {
				$pass = openssl_decrypt($pass, "aes-128-cbc", md5("SenhaCadastro", true), OPENSSL_ZERO_PADDING, md5($iv, true));
				$pass = base64_encode(md5(trim($pass, hex2bin("100f0e0d0c0b0a090807060504030201")), true));
				$query = mysqli_query($conn, "INSERT INTO usuarios(mail, user, pass, online) VALUES ('".$mail."', '".$login."', '".$pass."', '".time()."')");
					
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
