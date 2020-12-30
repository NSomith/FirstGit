package com.example.myfirebase

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.example.myfirebase.UserDao.UserDao
import com.example.myfirebase.models.Users
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_signin.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class SigninActivity : AppCompatActivity() {

    private val RC_SIGN_IN: Int = 12
    private lateinit var googleSignInClient:GoogleSignInClient
    lateinit var auth:FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signin)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this,gso)
        auth = Firebase.auth

        signbtn.setOnClickListener {
            signIn()
        }
    }

    override fun onStart() {
        super.onStart()
        val currentuser = auth.currentUser
        updateUI(currentuser)
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handlesigninResult(task)
        }
    }

    private fun handlesigninResult(task: Task<GoogleSignInAccount>?) {
        try {
            // Google Sign In was successful, authenticate with Firebase
            val account = task?.getResult(ApiException::class.java)!!
            firebaseAuthWithGoogle(account.idToken!!)
        } catch (e: ApiException) {
            // Google Sign In failed, update UI appropriately
            Log.e("fire","caught erroe${e.toString()}")
            // ...
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken,null)
        progressbar.visibility = View.VISIBLE
        signbtn.visibility = View.GONE
        GlobalScope.launch (Dispatchers.IO){
            val auth =  auth.signInWithCredential(credential).await()
            val firebase = auth.user
            withContext(Dispatchers.Main){
                updateUI(firebase)
            }
        }
    }

    private fun updateUI(firebase: FirebaseUser?) {
        if(firebase!=null){
            val users = Users(firebase.uid,firebase.displayName,firebase.photoUrl.toString())
            val userdao = UserDao()
            userdao.addusers(users)

            startActivity(Intent(this,MainActivity::class.java))
            finish()
        }else{
            progressbar.visibility = View.GONE
            signbtn.visibility = View.VISIBLE
        }

    }
}