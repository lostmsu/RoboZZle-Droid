package com.team242.robozzle;

import CS2JNet.System.InvalidOperationException;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;
import com.team242.robozzle.service.OperationNotSupportedByClientException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by lost on 2/28/2016.
 */
public class LoginDialog extends Dialog {
	EditText login;
	EditText password;
	EditText email;
	CompoundButton registerCheck;
	SharedPreferences pref;
	View loginParameters;
	Button loginRegister;

	public LoginDialog(Context context) {
		super(context);
	}

	public LoginDialog(Context context, int themeResId) {
		super(context, themeResId);
	}

	protected LoginDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.login_dialog);
		setTitle(R.string.credential_settings);

		final View emailTitle = this.findViewById(R.id.emailTitle);
		loginRegister = (Button)this.findViewById(R.id.signIn);

		email = (EditText)this.findViewById(R.id.email);
		registerCheck = (CompoundButton)this.findViewById(R.id.registerCheck);
		registerCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton compoundButton, boolean newValue) {
				emailTitle.setEnabled(newValue);
				email.setEnabled(newValue);
				loginRegister.setText(newValue ? R.string.register : R.string.login);
			}
		});

		login = (EditText)this.findViewById(R.id.login);
		password = (EditText)this.findViewById(R.id.password);
		loginParameters = this.findViewById(R.id.loginParameters);

		loginRegister.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (login.getText().toString().length() == 0 || password.getText().toString().length() == 0)
					return;

				if (registerCheck.isChecked() && email.getText().toString().length() == 0)
					return;

				loginOrRegister();
			}
		});

		final View cancel = this.findViewById(R.id.cancel);
		cancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				LoginDialog.this.cancel();
			}
		});
	}

	@Override
	public void show() {
		if (pref == null)
			throw new InvalidOperationException();
		super.show();
	}

	private void loginOrRegister(){
		final RobozzleWebClient client = new RobozzleWebClient();
		final boolean register = registerCheck.isChecked();
		final String login = this.login.getText().toString();
		final String password = this.password.getText().toString();
		final String email = this.email.getText().toString();
		this.loginParameters.setEnabled(false);
		this.loginRegister.setEnabled(false);

		AsyncTask<Void, Void, Integer> connectionTask = new AsyncTask<Void, Void, Integer>() {
			volatile String textError = null;

			@Override
			protected void onPostExecute(Integer result) {
				super.onPostExecute(result);
				if (result != 0){
					Toast.makeText(LoginDialog.this.getContext(), result, Toast.LENGTH_LONG).show();
				} else if (textError != null){
					Toast.makeText(LoginDialog.this.getContext(), textError, Toast.LENGTH_LONG).show();
				} else {
					SharedPreferences.Editor editor = pref.edit();
					editor.putString(RoboZZleSettings.LOGIN, login);
					editor.putString(RoboZZleSettings.PASSWORD, password);
					editor.apply();
					LoginDialog.this.dismiss();
				}
				loginParameters.setEnabled(true);
				loginRegister.setEnabled(true);
			}

			@Override
			protected Integer doInBackground(Void... voids) {
				try {
					if (register)
						textError = client.Register(login, password, email);
					else {
						Collection<Integer> solved = new ArrayList<>();
						Collection<RobozzleWebClient.LevelVoteInfo> votes = new ArrayList<>();
						if (!client.LogIn(login, password, solved, votes))
							return R.string.loginInvalidCredentials;
					}
					return  0;
				} catch (IOException e){
					return R.string.robozzleComIOError;
				} catch (OperationNotSupportedByClientException e){
					return R.string.loginOperationNotSupportedByClient;
				}
			}
		};
		connectionTask.execute();
	}
}
