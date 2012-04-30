package eu.piotrbuda.android.appswatch;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class WelcomeActivity extends Activity {
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }

    public void obtainTokenAM(View view) {
        new AuthTokenFetcher(this, this).execute(false);
    }

    public void invalidateTokenAM(View view) {
        new AuthTokenFetcher(this, this).execute(true);
    }

    private void displayAuthToken(String authToken) {
        ((TextView) findViewById(R.id.textView)).setText(authToken);
    }

    private class AuthTokenFetcher extends AsyncTask<Boolean, Void, String> {
        private Activity activity;
        private Context context;

        public AuthTokenFetcher(Activity activity, Context context) {
            this.activity = activity;
            this.context = context;
        }

        @Override
        protected String doInBackground(Boolean... booleans) {
            return updateToken(booleans[0]);
        }

        @Override
        protected void onPostExecute(String s) {
            displayAuthToken(s);
        }

        private String updateToken(boolean invalidateToken) {
            String authToken = "null";
            try {
                AccountManager am = AccountManager.get(context);
                Account[] accounts = am.getAccountsByType("com.google");
                AccountManagerFuture<Bundle> accountManagerFuture;
                if (context == null) {//this is used when calling from an interval thread
                    accountManagerFuture = am.getAuthToken(accounts[0], "android", null, false, null, null);
                } else {
                    accountManagerFuture = am.getAuthToken(accounts[0], "android", null, activity, null, null);
                }
                Bundle authTokenBundle = accountManagerFuture.getResult();
                authToken = authTokenBundle.getString(AccountManager.KEY_AUTHTOKEN).toString();
                if (invalidateToken) {
                    am.invalidateAuthToken("com.google", authToken);
                    authToken = updateToken(false);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return authToken;
        }
    }
}
