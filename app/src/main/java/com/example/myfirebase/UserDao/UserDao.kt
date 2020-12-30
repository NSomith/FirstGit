package com.example.myfirebase.UserDao

import com.example.myfirebase.models.Users
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class UserDao {

    val db = FirebaseFirestore.getInstance()
    val userCollection = db.collection("users")

    fun addusers(user:Users?){
        user?.let {
            GlobalScope.launch (Dispatchers.IO){
                userCollection.document(user.id).set(it)
            }
        }
    }
}