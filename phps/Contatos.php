<?php
	if ($_SERVER['REQUEST_METHOD'] == 'POST') {
		$login	 = clear($_POST['LOGIN']);
		$pass	 = $_POST['PASS'];
		$iv	 	 = $_POST['IV'];
			
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
					echo "lcon";
					echo "\n"."Adcionar novo";
					$query = mysqli_query($conn, "SELECT contato FROM contatos WHERE user = '".$login."'");
					while ($result = $query->fetch_assoc()) {
						echo "\n".$result['contato'];
					}
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
