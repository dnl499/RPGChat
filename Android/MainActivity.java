package com.example.daniel.directchat;

/*
 * Before anything, have in mind that I have an initial focus on only supporting portuguese-br language, maybe in the future that may change.
 * Though I'll leave the comments in english as most people interested in the code will know that language. If you need explaining on any
 * piece of this code, feel free to ask. ^^
 */

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.CountDownTimer;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Base64;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

	protected static 	ArrayList<ArrayList<TextView>> 	tvs = new ArrayList<>(); 		  //A list with a list of messages received
	protected static 	ArrayList<Boolean>				      isRoom = new ArrayList<>();	  //Stores wheter a chat is a room or not
	protected static 	ArrayList<ArrayList<TextView>>	roomMsgs = new ArrayList<>();	//Stores the rooms' messages
	protected static 	ArrayList<Button> 				      btns = new ArrayList<>(); 		//A list with the tab buttons
	protected static 	ArrayList<String> 				      oppened = new ArrayList<>();	//A list with the name of the oppened tabs
	protected static 	int 							              atual = 0;						        //The current tab number
	protected static 	ArrayList<Integer> 				      nMsgs = new ArrayList<>();		//The number of messages in each tab
	private           EditText 						            edTxt;							          //The edit text for message sending
	private   static 	TextView 						            lgerr;							          //The login error reporter
	protected static 	LinearLayout 					          ln;
	protected static 	LinearLayout 					          ln1;							            //ln is the message's layout, ln1 is the tab buttons' layout
	protected static	ImageView						            opt, tools, part, swi, fav;		//The chat controler buttons (Are imageviews, though :p )
	private 			    Button 							            btn;                       		//It's the send button
	private 			    ImageView 						          btnp;							            //It's the add button (It's a imageview, though :p )
	private 			    String 							            nome = "";            				//The name for the actual destiny

	private static 		AlertDialog 					          dg; 	                   		  //General purpose AlertDialog

	private 			    Random 							            rd = new Random();				    //For generating random IVs
	private 			    URL 							              serverurl;						        //The URL for the actual operations
	private 			    LinearLayout 					          logln;   						          //The login/singup form Layout
	protected static 	EditText 						            login, pass, mail;				    //The login/signup form EditText
	private 			    String 							            riv;							            //The random IV for encrypt the password and messages
	private 			    boolean 						            sh = false;						        //This boolean defines if either the passowrd is showing or not on signin
	private 			    String 							            logged = "";					        //The user's login (Needed authentication on each operation)
	private 			    String 							            upass = "";						        //The user's password (Also needed for each operation)

	@SuppressLint("SetTextI18n")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);

		// Initializing the UI variables
		ln = (LinearLayout) findViewById(R.id.linearLayout);
		ln1 = (LinearLayout) findViewById(R.id.linearLayout1);
		edTxt = (EditText) findViewById(R.id.editText);
		lgerr = (TextView) findViewById(R.id.lgerr);
		btnp = (ImageView) findViewById(R.id.btn);
		btn = (Button) findViewById(R.id.btn1);

		// Gives the initial random IV for the authentication's encrypting
		byte[] biv = new byte[12];
		rd.nextBytes(biv);
		riv = Base64.encodeToString(biv, Base64.DEFAULT);
	}

	//Sends the login and password to the php server and waits for the confirmation, then moves to the chat screen
	public void login(View v) {
		logln = new LinearLayout(this);
		login = new EditText(this);
		pass = new EditText(this);

		logln.setOrientation(LinearLayout.VERTICAL);
		login.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
		pass.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
		pass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
		logln.addView(login);
		logln.addView(pass);
		pass.setOnLongClickListener(null);

		login.setHint("Seu login");
		pass.setHint("Sua senha");

		dg = new AlertDialog.Builder(this)
				.setTitle("Digite seu login e senha:")
				.setView(logln)
				.setNegativeButton("Cancel", null)
				.setPositiveButton("Log in", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						try {
							serverurl = new URL("http://chaos-workbench.net16.net/AppData.php");
						} catch (MalformedURLException e) {
							mensagem("Erro: URL malformado", e.getMessage());
						}

						byte[] biv = new byte[12];
						rd.nextBytes(biv);
						riv = Base64.encodeToString(biv, Base64.DEFAULT);

						String out = encrypt(pass.getText().toString(), pass.getText().toString(), riv);
						logged = login.getText().toString();
						upass = pass.getText().toString();

						final OkHttpClient client = new OkHttpClient();
						RequestBody body = new FormBody.Builder()
								.add("LOGIN", login.getText().toString())
								.add("PASS", out)
								.add("SIGN", "N")
								.add("IV", riv)
								.build();
						Request request = new Request.Builder()
								.url(serverurl)
								.post(body)
								.addHeader("content-type", "application/json; charset=utf-8")
								.build();

						client.newCall(request).enqueue(new Callback() {
							@Override
							public void onFailure(@NonNull Call call, @NonNull final IOException e) {
								runOnUiThread(new Runnable() {
									@Override
									public void run() {
										lgerr.setText("Erro de conexão: talvez o servidor esteja offline ou você esteja desconectado");
									}
								});
							}

							@SuppressWarnings("ConstantConditions")
							@Override
							public void onResponse(@NonNull Call call, @NonNull final Response response) throws IOException {
								final String res = response.body().string();
								runOnUiThread(new Runnable() {
									@Override
									public void run() {
										if (res.contains("lgsc")) {
											runOnUiThread(new Runnable() {
												@Override
												public void run() {
													setContentView(R.layout.activity_main);
													ln1 = (LinearLayout) findViewById(R.id.linearLayout1);
													ln = (LinearLayout) findViewById(R.id.linearLayout);
													edTxt = (EditText) findViewById(R.id.editText);
													btnp = (ImageView) findViewById(R.id.btn);
													btn = (Button) findViewById(R.id.btn1);
													opt = (ImageView) findViewById(R.id.options);
													tools = (ImageView) findViewById(R.id.tools);
													part = (ImageView) findViewById(R.id.participants);
													swi = (ImageView) findViewById(R.id.alternate);
													fav = (ImageView) findViewById(R.id.favorite);
													MainActivity.tools.setEnabled(false);
													MainActivity.part.setEnabled(false);
													MainActivity.swi.setEnabled(false);
													MainActivity.fav.setEnabled(false);

													atual = 0;
													nome = "Echo";
													nMsgs.add(0);
													oppened.add("Echo");
													btns.add(new Button(getApplication()));
													tvs.add(new ArrayList<TextView>());
													btns.get(atual).setText(nome);
													ln.removeAllViews();
													ln1.addView(btns.get(atual), atual+1);
													btns.get(atual).setContentDescription(String.valueOf(atual));   //Identificador do botão
													btns.get(atual).setOnClickListener(new ButtonClickHandler());
													btns.get(atual).setEnabled(false);
													roomMsgs.add(new ArrayList<TextView>());
													isRoom.add(false);
                                                    tools.setVisibility(View.INVISIBLE);
                                                    part.setVisibility(View.INVISIBLE);
                                                    swi.setVisibility(View.INVISIBLE);
                                                    fav.setVisibility(View.INVISIBLE);
													messageObserver().start();
												}
											});
										} else if (res.contains("lgus")) {
											lgerr.setText("Senha inválida");
										} else if (res.contains("uuser")) {
											lgerr.setText("Login inválido");
										} else {
											lgerr.setText(res);
										}
									}
								});
							}
						});
					}
				}).show();
	}

	public void cadastrar(View v) {
		logln = new LinearLayout(this);
		login = new EditText(this);
		pass = new EditText(this);
		mail = new EditText(this);

		logln.setOrientation(LinearLayout.VERTICAL);
		login.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
		mail.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
		pass.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
		pass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
		logln.addView(login);
		logln.addView(pass);
		logln.addView(mail);

		login.setHint("Seu login");
		pass.setHint("Sua senha (Pressione para mostra-la)");
		mail.setHint("Email para recuperação de senha");

		pass.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View view) {
				if (!sh) pass.setInputType(InputType.TYPE_CLASS_TEXT);
				else pass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
				sh = !sh;
				return true;
			}
		});

		dg = new AlertDialog.Builder(this)
				.setTitle("Cadastre seu login e senha (Email opcional)")
				.setView(logln)
				.setNegativeButton("Cancel", null)
				.setPositiveButton("Cadastrar", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						try {
							serverurl = new URL("http://chaos-workbench.net16.net/AppData.php");
						} catch (MalformedURLException e) {
							mensagem("Erro: URL malformado", e.getMessage());
						}

						byte[] biv = new byte[12];
						rd.nextBytes(biv);
						riv = Base64.encodeToString(biv, Base64.DEFAULT);

						String out = encrypt(pass.getText().toString(), "SenhaCadastro", riv);

						OkHttpClient client = new OkHttpClient();
						RequestBody body = new FormBody.Builder()
								.add("LOGIN", login.getText().toString())
								.add("PASS", out)
								.add("SIGN", "Y")
								.add("MAIL", mail.getText().toString())
								.add("IV", riv)
								.build();
						Request request = new Request.Builder()
								.url(serverurl)
								.post(body)
								.addHeader("content-type", "application/json; charset=utf-8")
								.build();

						client.newCall(request).enqueue(new Callback() {
							@Override
							public void onFailure(@NonNull Call call, @NonNull IOException e) {
								runOnUiThread(new Runnable() {
									@Override
									public void run() {
										lgerr.setText("Erro de conexão: talvez o servidor esteja offline ou você esteja desconectado");
									}
								});

							}

							@SuppressWarnings("ConstantConditions")
							@Override
							public void onResponse(@NonNull Call call, @NonNull final Response response) throws IOException {
								final String res = response.body().string();
								runOnUiThread(new Runnable() {
									@Override
									public void run() {
										if (res.contains("susc")) {
											lgerr.setText("Cadastro concluido");
										} else if (res.contains("uexi")) {
											lgerr.setText("Este usuário já existe");
										} else {
											lgerr.setText(lgerr.getText()+res);
										}
									}
								});
							}
						});
					}
				}).show();
	}

	public void logout(MenuItem i) {
		setContentView(R.layout.login);
		ln.removeAllViews();
		ln1.removeAllViews();
		btns.clear();
		tvs.clear();
		isRoom.clear();
		roomMsgs.clear();
		oppened.clear();
		nMsgs.clear();
		lgerr.setText("");
		logged = "";
		upass = "";
		messageObserver().cancel();
	}

	//Called by the send button, it sends the edTxt as a message and prints it
	public void echo(View v) {
		tvs.get(atual).add(new TextView(getApplication()));
		tvs.get(atual).get(nMsgs.get(atual)).setText("Eu: " + edTxt.getText());							//Adds an textview with the message you sent
		tvs.get(atual).get(nMsgs.get(atual)).setTextColor(0xFFFFFFFF);
		tvs.get(atual).get(nMsgs.get(atual)).setBackgroundColor(0xFF0000FF);
		ln.addView(tvs.get(atual).get(nMsgs.get(atual)));                       						//Shows the textview with the message
		nMsgs.set(atual, nMsgs.get(atual)+1);															//Increments the number of messages
		if (atual > 0) messageSend(edTxt.getText().toString(), btns.get(atual).getText().toString());	//Unless it's the echo tab, sends to server
		edTxt.setText("");																				//Resests the edtTxt
	}

	//When a message is received, it'll call this method, that will put it on the screen (Debug only)
	private void messageReceiver(String sender, String msg) {
		tvs.get(atual).add(new TextView(getApplication()));
		tvs.get(atual).get(nMsgs.get(atual)).setText(sender + ": " + msg);
		tvs.get(atual).get(nMsgs.get(atual)).setTextColor(0xFFFFFFFF);
		tvs.get(atual).get(nMsgs.get(atual)).setBackgroundColor(0xFFFF0000);
		ln.addView(tvs.get(atual).get(nMsgs.get(atual)));
		nMsgs.set(atual, nMsgs.get(atual)+1);
		edTxt.setText("");
	}

	//Create and set the settings for new tabs
	private void createTab(String name) {
		btns.get(atual).setEnabled(true);
		atual = btns.size();
		nMsgs.add(0);
		btns.add(new Button(this));
		tvs.add(new ArrayList<TextView>());
		btns.get(atual).setText(name);
		ln.removeAllViews();
		ln1.addView(btns.get(atual), atual+1);
		btns.get(atual).setContentDescription(String.valueOf(atual));   //Button number identifier (I didn't found a better way of tagging them)
		btns.get(atual).setOnClickListener(new ButtonClickHandler());
		btns.get(atual).setOnLongClickListener(new ButtonClickHandler());
		btns.get(atual).setEnabled(false);
		roomMsgs.add(new ArrayList<TextView>());
	}

	//Shows a dialog asking whether the user want to start a private chat, or enter in a room
	public void nCon(View v) {
		int x = btns.size();
		if (x >= 100) {
			dg = new AlertDialog.Builder(this).setTitle("Muitas abas!")
					.setMessage("Você está tentando matar seu smartphone? o.o")
					.setNegativeButton("Não!", null)
					.setPositiveButton("Sim! :v", null)
					.show();
			return;
		}
		dg = new AlertDialog.Builder(this)
				.setTitle("Escolha uma das opções:")
				.setNegativeButton("Cancelar", null)
				.setSingleChoiceItems(new String[]{"Contatos", "Salas ativas", "Salas preferidas", "Minhas salas"}, -1, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						dg.dismiss();
						switch (i) {
							case 0:
								showContacts();
								break;
							case 1:
								showRooms(false, false);
								break;
							case 2:
								showRooms(true, false);
								break;
							case 3:
								showRooms(false, true);
								break;
						}
					}
				})
				.show();
	}

	private void showContacts() {
		try {
			serverurl = new URL("http://chaos-workbench.net16.net/Contatos.php");
		} catch (MalformedURLException e) {
			mensagem("Erro: URL malformado", e.getMessage());
		}

		byte[] biv = new byte[12];
		rd.nextBytes(biv);
		riv = Base64.encodeToString(biv, Base64.DEFAULT);

		String out = encrypt(upass, upass, riv);

		OkHttpClient client = new OkHttpClient();
		RequestBody body = new FormBody.Builder()
				.add("LOGIN", logged)
				.add("PASS", out)
				.add("IV", riv)
				.build();
		Request request = new Request.Builder()
				.url(serverurl)
				.post(body)
				.addHeader("content-type", "application/json; charset=utf-8")
				.build();

		client.newCall(request).enqueue(new Callback() {
			@Override
			public void onFailure(@NonNull Call call, @NonNull IOException e) {
				Looper.prepare();
				mensagem("Erro de conexão: ", "Talvez você tenha sido desconectado ou o servidor esteja offline.");

			}

			@SuppressWarnings("ConstantConditions")
			@Override
			public void onResponse(@NonNull Call call, @NonNull final Response response) throws IOException {
				final String res = response.body().string();
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (res.contains("uuser")) {
							messageReceiver("App", "Como você logou? :o");
						} else if (res.contains("lgus")) {
							messageReceiver("App", "Como você logou? :o");
						} else if (res.contains("lcon")) {
							dg = new AlertDialog.Builder(lgerr.getContext())
									.setTitle("Com quem deseja se conectar?")
									.setSingleChoiceItems(res.substring(5).split("\n"), -1, openContact(res.substring(5).split("\n")))
									.show();
						}else {
							messageReceiver("App", res);
						}
					}
				});
			}
		});
	}

	private DialogInterface.OnClickListener openContact(final String[] contact) {
		return new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int i) {
				dg.dismiss();
				nome = contact[i];
				if (i == 0) {
					searchContact(null);
				} else if (!oppened.contains(nome)) {
					oppened.add(nome);
					isRoom.add(false);
					roomMsgs.add(new ArrayList<TextView>());
					createTab(nome);
				} else {
					mensagem("Ops", "Parece que você já está conversando com "+ nome + "!");
				}
			}
		};
	}

	public void searchContact(MenuItem item) {
		final EditText contato = new EditText(this);
		dg = new AlertDialog.Builder(this)
				.setTitle("Procurar contato")
				.setPositiveButton("Procurar", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						try {
							serverurl = new URL("http://chaos-workbench.net16.net/SContato.php");
						} catch (MalformedURLException e) {
							mensagem("Erro: URL malformado", e.getMessage());
						}

						byte[] biv = new byte[12];
						rd.nextBytes(biv);
						riv = Base64.encodeToString(biv, Base64.DEFAULT);

						String out = encrypt(upass, upass, riv);

						OkHttpClient client = new OkHttpClient();
						RequestBody body = new FormBody.Builder()
								.add("LOGIN", logged)
								.add("PASS", out)
								.add("CONTATO", contato.getText().toString())
								.add("IV", riv)
								.build();
						Request request = new Request.Builder()
								.url(serverurl)
								.post(body)
								.addHeader("content-type", "application/json; charset=utf-8")
								.build();

						client.newCall(request).enqueue(new Callback() {
							@Override
							public void onFailure(@NonNull Call call, @NonNull IOException e) {
								Looper.prepare();
								mensagem("Erro de conexão: ", "Talvez você tenha sido desconectado ou o servidor esteja offline.");

							}

							@SuppressWarnings("ConstantConditions")
							@Override
							public void onResponse(@NonNull Call call, @NonNull final Response response) throws IOException {
								final String res = response.body().string();
								runOnUiThread(new Runnable() {
									@Override
									public void run() {
										if (res.contains("uuser")) {
											messageReceiver("App", "Como você logou? :o");
										} else if (res.contains("lgus")) {
											messageReceiver("App", "Como você logou? :o");
										} else if (res.contains("ctad")) {
											mensagem(null, "Contato adicionado");
										} else if (res.contains("ctnf")) {
											mensagem("Contato não encontrado", "Talvez essa pessoa não esteja cadastrada, ou o nome esteja errado");
										} else {
											messageReceiver("App", res);
										}
									}
								});
							}
						});
					}
				})
				.setNegativeButton("Cancelar", null)
				.setView(contato)
				.show();
	}

	private void showRooms(boolean fav, boolean adm) {
		String favorito = "f";
		String admin = "f";
		if (fav) favorito = "s";
		if (adm) admin = "s";
		try {
			serverurl = new URL("http://chaos-workbench.net16.net/Rooms.php");
		} catch (MalformedURLException e) {
			mensagem("Erro: URL malformado", e.getMessage());
		}

		byte[] biv = new byte[12];
		rd.nextBytes(biv);
		riv = Base64.encodeToString(biv, Base64.DEFAULT);

		String out = encrypt(upass, upass, riv);

		OkHttpClient client = new OkHttpClient();
		RequestBody body = new FormBody.Builder()
				.add("LOGIN", logged)
				.add("PASS", out)
				.add("IV", riv)
				.add("FAVORITO", favorito)
				.add("ADMIN", admin)
				.build();
		Request request = new Request.Builder()
				.url(serverurl)
				.post(body)
				.addHeader("content-type", "application/json; charset=utf-8")
				.build();

		client.newCall(request).enqueue(new Callback() {
			@Override
			public void onFailure(@NonNull Call call, @NonNull IOException e) {
				Looper.prepare();
				mensagem("Erro de conexão: ", "Talvez você tenha sido desconectado ou o servidor esteja offline.");

			}

			@SuppressWarnings("ConstantConditions")
			@Override
			public void onResponse(@NonNull Call call, @NonNull final Response response) throws IOException {
				final String res = response.body().string();
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (res.contains("uuser")) {
							messageReceiver("App", "Como você logou? :o");
						} else if (res.contains("lgus")) {
							messageReceiver("App", "Como você logou? :o");
						} else if (res.contains("lcon")) {
							dg = new AlertDialog.Builder(lgerr.getContext())
									.setTitle("Qual sala desejas entrar?")
									.setSingleChoiceItems(res.substring(5).split("\n"), -1, openRoom(res.substring(5).split("\n")))
									.show();
						}else {
							messageReceiver("App", res);
						}
					}
				});
			}
		});
	}

	private DialogInterface.OnClickListener openRoom(final String[] room) {
		return new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int i) {
				dg.dismiss();
				nome = room[i];
                if (i == 0) {
                    createRoom();
                } else if (!oppened.contains(nome)) {
					oppened.add(nome);
					isRoom.add(true);
					roomMsgs.add(new ArrayList<TextView>());
					createTab(nome);
					try {
						serverurl = new URL("http://chaos-workbench.net16.net/RoomEnter.php");
					} catch (MalformedURLException e) {
						mensagem("Erro: URL malformado", e.getMessage());
					}

					byte[] biv = new byte[12];
					rd.nextBytes(biv);
					riv = Base64.encodeToString(biv, Base64.DEFAULT);

					String out = encrypt(upass, upass, riv);

					OkHttpClient client = new OkHttpClient();
					RequestBody body = new FormBody.Builder()
							.add("LOGIN", logged)
							.add("PASS", out)
							.add("IV", riv)
							.add("SALA", nome)
							.build();
					Request request = new Request.Builder()
							.url(serverurl)
							.post(body)
							.addHeader("content-type", "application/json; charset=utf-8")
							.build();

					client.newCall(request).enqueue(new Callback() {
						@Override
						public void onFailure(@NonNull Call call, @NonNull IOException e) {
							Looper.prepare();
							mensagem("Erro de conexão: ", "Talvez você tenha sido desconectado ou o servidor esteja offline.");

						}

						@SuppressWarnings("ConstantConditions")
						@Override
						public void onResponse(@NonNull Call call, @NonNull final Response response) throws IOException {
							final String res = response.body().string();
							runOnUiThread(new Runnable() {
								@Override
								public void run() {
									if (res.contains("uuser")) {
										messageReceiver("App", "Como você logou? :o");
									} else if (res.contains("lgus")) {
										messageReceiver("App", "Como você logou? :o");
									} else {
										messageReceiver("App", res);
									}
								}
							});
						}
					});
				} else {
					mensagem("Ops", "Parece que você já está participando da sala "+ nome + "!");
				}
			}
		};
	}

	private void createRoom() {
        final EditText sala = new EditText(this);
        dg = new AlertDialog.Builder(this)
                .setTitle("Qual nome terá a sala?")
                .setView(sala)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Criar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        try {
                            serverurl = new URL("http://chaos-workbench.net16.net/RoomCreate.php");
                        } catch (MalformedURLException e) {
                            mensagem("Erro: URL malformado", e.getMessage());
                        }

                        byte[] biv = new byte[12];
                        rd.nextBytes(biv);
                        riv = Base64.encodeToString(biv, Base64.DEFAULT);

                        String out = encrypt(upass, upass, riv);

                        OkHttpClient client = new OkHttpClient();
                        RequestBody body = new FormBody.Builder()
                                .add("LOGIN", logged)
                                .add("PASS", out)
                                .add("IV", riv)
                                .add("SALA", sala.getText().toString())
                                .build();
                        Request request = new Request.Builder()
                                .url(serverurl)
                                .post(body)
                                .addHeader("content-type", "application/json; charset=utf-8")
                                .build();

                        client.newCall(request).enqueue(new Callback() {
                            @Override
                            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                Looper.prepare();
                                mensagem("Erro de conexão: ", "Talvez você tenha sido desconectado ou o servidor esteja offline.");

                            }

                            @SuppressWarnings("ConstantConditions")
                            @Override
                            public void onResponse(@NonNull Call call, @NonNull final Response response) throws IOException {
                                final String res = response.body().string();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (res.contains("uuser")) {
                                            messageReceiver("App", "Como você logou? :o");
                                        } else if (res.contains("lgus")) {
                                            messageReceiver("App", "Como você logou? :o");
                                        } else if (res.contains("rmex")) {
                                            mensagem("Esse sala já existe!", "");
                                        } else if (res.contains("crsc")) {
                                            mensagem("Sala criada com sucesso", "");
											createTab(sala.getText().toString());
											isRoom.add(true);
											roomMsgs.add(new ArrayList<TextView>());
                                        }else {
                                            messageReceiver("App", "Q?"+res);
                                        }
                                    }
                                });
                            }
                        });
                    }
                })
                .show();
    }

	//Keep checking in the server for new messages every 10s
	private CountDownTimer messageObserver () {
		return new CountDownTimer(60000, 10000) {
			@Override
			public void onTick(long l) {
				URL murl;
				try {
					murl = new URL("http://chaos-workbench.net16.net/Messages.php");
				} catch (MalformedURLException e) {
					mensagem("Erro: URL malformado", e.getMessage());
					return;
				}

				byte[] biv = new byte[12];
				rd.nextBytes(biv);
				riv = Base64.encodeToString(biv, Base64.DEFAULT);

				String out = encrypt(upass, upass, riv);

				OkHttpClient client = new OkHttpClient();
				RequestBody body = new FormBody.Builder()
						.add("LOGIN", logged)
						.add("PASS", out)
						.add("IV", riv)
						.build();
				Request request = new Request.Builder()
						.url(murl)
						.post(body)
						.addHeader("content-type", "application/json; charset=utf-8")
						.build();

				client.newCall(request).enqueue(new Callback() {
					@Override
					public void onFailure(@NonNull Call call, @NonNull IOException e) {
						Looper.prepare();
						mensagem("Erro de conexão: ", "Talvez você tenha sido desconectado ou o servidor esteja offline.");

					}

					@SuppressWarnings("ConstantConditions")
					@Override
					public void onResponse(@NonNull Call call, @NonNull final Response response) throws IOException {
						final String res = response.body().string();
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								if (res.contains("uuser")) {
									messageReceiver("App", "Como você logou? :o");
								} else if (res.contains("lgus")) {
									messageReceiver("App", "Como você logou? :o");
								} else if (res.contains("lcon")) {
									String[] msgs = res.split("\n");
									if (msgs.length > 1 ) {
										for (int i = 1; i < msgs.length; i++) {
											String sender = msgs[i].split("\t")[0];
											String txt = msgs[i].split("\t")[1];
											for (int j = 0; j < btns.size(); j++) {
												if (btns.get(j).getText().toString().equals(sender.substring(1))) {
													if (btns.get(j).isEnabled()) btns.get(j).setBackgroundColor(0xFFFFD700);
													tvs.get(j).add(new TextView(getApplicationContext()));
													tvs.get(j).get(nMsgs.get(j)).setText(txt);
													tvs.get(j).get(nMsgs.get(j)).setTextColor(0xFFFFFFFF);
													tvs.get(j).get(nMsgs.get(j)).setBackgroundColor(0xFFFF0000);
													if (atual == j) ln.addView(tvs.get(j).get(nMsgs.get(j)));
													nMsgs.set(j, nMsgs.get(j)+1);
													break;
												}
												if (j == (btns.size()-1)) {
													int x = btns.size();
													btns.add(new Button(getApplicationContext()));
													tvs.add(new ArrayList<TextView>());
													if (x >= nMsgs.size()) nMsgs.add(0);
													btns.get(x).setText(sender.substring(1));
													ln1.addView(btns.get(x), x+1);
													btns.get(x).setContentDescription(String.valueOf(x));
													btns.get(x).setOnClickListener(new ButtonClickHandler());
													btns.get(x).setOnLongClickListener(new ButtonClickHandler());
													btns.get(x).setBackgroundColor(0xFFFFD700);
													tvs.get(x).add(new TextView(getApplicationContext()));
													tvs.get(x).get(nMsgs.get(x)).setText(txt);
													tvs.get(x).get(nMsgs.get(x)).setTextColor(0xFFFFFFFF);
													tvs.get(x).get(nMsgs.get(x)).setBackgroundColor(0xFFFF0000);
													nMsgs.set(x, nMsgs.get(x)+1);
													if (sender.substring(0,1) == "<") {
														isRoom.add(true);
														roomMsgs.add(new ArrayList<TextView>());
													} else {
														isRoom.add(false);
														roomMsgs.add(new ArrayList<TextView>());
													}
													break;
												}
											}
										}
									}
								} else if (res.contains("indt")) {
									messageReceiver("App", "Como você logou? :o");
								} else if (res.contains("emps")) {
									messageReceiver("App", "Como você logou? :o");
								} else {
									messageReceiver("App", res);
								}
							}
						});
					}
				});
			}

			@Override
			public void onFinish() {messageObserver().start();}
		};
	}

	private void messageSend (String msg, String destiny) {
		URL murl;
		try {
			if (isRoom.get(atual)) murl = new URL("http://chaos-workbench.net16.net/RoomMessage.php");
            else murl = new URL("http://chaos-workbench.net16.net/MSender.php");
		} catch (MalformedURLException e) {
			mensagem("Erro: URL malformado", e.getMessage());
			return;
		}

		byte[] biv = new byte[12];
		rd.nextBytes(biv);
		riv = Base64.encodeToString(biv, Base64.DEFAULT);

		String out = encrypt(upass, upass, riv);

		OkHttpClient client = new OkHttpClient();
		RequestBody body = new FormBody.Builder()
				.add("LOGIN", logged)
				.add("PASS", out)
				.add("IV", riv)
				.add("MESSAGE", msg)
				.add("DESTINY", destiny)
				.build();
		Request request = new Request.Builder()
				.url(murl)
				.post(body)
				.addHeader("content-type", "application/json; charset=utf-8")
				.build();

		client.newCall(request).enqueue(new Callback() {
			@Override
			public void onFailure(@NonNull Call call, @NonNull IOException e) {
				Looper.prepare();
				mensagem("Erro de conexão: ", "Talvez você tenha sido desconectado ou o servidor esteja offline.");

			}

			@Override
			public void onResponse(@NonNull Call call, @NonNull final Response response) throws IOException {
				final String res = response.body().string();
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (res.contains("uuser")) {
							messageReceiver("App", "Como você logou? :o");
						} else if (res.contains("lgus")) {
							messageReceiver("App", "Como você logou? :o");
						} else if (!res.contains("lcon")) {
							messageReceiver("App", res);
						}
					}
				});
			}
		});
	}

	//General purpose dialog
	static void mensagem(String title, String msg) {
		dg = new AlertDialog.Builder(ln.getContext())
				.setTitle(title)
				.setMessage(msg)
				.show();
	}

	//Actually, in this method, the key and the iv are hashed in MD5 so they can fit as 16 bits
	static String encrypt(String s, String key, String iv) {
		String out = "";
		Cipher c;
		try {
			MessageDigest ds = MessageDigest.getInstance("MD5");
			ds.update(iv.getBytes("UTF-8"));
			byte[] biv = ds.digest();
			ds.update(key.getBytes("UTF-8"));
			byte[] bkey = ds.digest();
			IvParameterSpec Iv = new IvParameterSpec(biv);
			SecretKeySpec kkey = new SecretKeySpec(bkey, "AES");
			c = Cipher.getInstance("AES/CBC/PKCS7Padding");
			c.init(Cipher.ENCRYPT_MODE, kkey, Iv);
			out = new String(Base64.encode(c.doFinal(s.getBytes()), Base64.DEFAULT));
		} catch (NoSuchAlgorithmException e) {
			mensagem("Erro: Algoritmo inexistente", e.getMessage());
		} catch (NoSuchPaddingException e) {
			mensagem("Erro: Padding inexistente", e.getMessage());
		} catch (InvalidKeyException e) {
			mensagem("Erro: Chave inválida", e.getMessage());
		} catch (BadPaddingException e) {
			mensagem("Erro: Mal preenchimento", e.getMessage());
		} catch (IllegalBlockSizeException e) {
			mensagem("Erro: Tamanho de bloco incompativel", e.getMessage());
		} catch (InvalidAlgorithmParameterException e) {
			mensagem("Erro: Parametro de algoritmo inválido", e.getMessage());
		} catch (UnsupportedEncodingException e) {
			mensagem("Erro: UTF-8 não suportado", e.getMessage());
		}
		return out;
	}

	@Override
	public boolean onKeyDown(int keycode, KeyEvent e) {

		if (keycode == KeyEvent.KEYCODE_ENTER) {
			echo(null);
			return true;
		} else if (keycode == KeyEvent.KEYCODE_MENU) {
			showMenu(null);
			return true;
		}

		return super.onKeyDown(keycode, e);
	}

	//Show the context menu on menu key is pressed or when the menu icon is pressed
	public void showMenu(View v) {
		if (v == null) {
			if (lgerr.isShown()) {
				v = lgerr;
				PopupMenu menu = new PopupMenu(this, v);
				menu.getMenuInflater().inflate(R.menu.menu2, menu.getMenu());
				menu.show();
			} else {
				v = ln1;
				PopupMenu menu = new PopupMenu(this, v);
				menu.getMenuInflater().inflate(R.menu.menu, menu.getMenu());
				menu.show();
			}
		} else {
			PopupMenu menu = new PopupMenu(this, v);
			menu.getMenuInflater().inflate(R.menu.menu, menu.getMenu());
			menu.show();
		}
	}
}
