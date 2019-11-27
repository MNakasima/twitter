package com.mnakasima.twitter

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.activity_login.*
import com.google.firebase.auth.FirebaseAuth
import androidx.core.app.ComponentActivity
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*


class Login : AppCompatActivity() {

    private var mAuth: FirebaseAuth? = null

    private var database = FirebaseDatabase.getInstance()
    private var myRef = database.reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        mAuth = FirebaseAuth.getInstance()

        ivProfile.setOnClickListener( View.OnClickListener {
            checkPermission()
        })
    }

    fun loginToFirebase(email:String, password:String){
        mAuth!!.createUserWithEmailAndPassword(email,password)
            .addOnCompleteListener(this){
                task ->

                if (task.isSuccessful){
                    Toast.makeText(applicationContext,"Success Login", Toast.LENGTH_LONG).show()

                    saveImageFirebase()

                }else{
                    Toast.makeText(applicationContext,"Login Failed", Toast.LENGTH_LONG).show()
                }

            }
    }

    fun saveImageFirebase(){

        var currentUser = mAuth!!.currentUser
        val email:String = currentUser!!.email.toString()

        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.getReferenceFromUrl("gs://twitter-6f828.appspot.com")

        val df = SimpleDateFormat("ddMMyyHHmmss")
        val dataobj = Date()

        val imagePath = splitSting(email) +"."+df.format(dataobj)+ ".jpg"
        val imageRef = storageRef.child("Images/"+imagePath)

        ivProfile.isDrawingCacheEnabled=true
        ivProfile.buildDrawingCache()

        val drawable = ivProfile.drawable as BitmapDrawable
        val bitmap = drawable.bitmap

        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()
        
        val uploadTask= imageRef.putBytes(data)

        uploadTask.addOnFailureListener{

            Toast.makeText(applicationContext,"fail to upload image", Toast.LENGTH_LONG).show()

        }.addOnSuccessListener {

            taskSnapshot ->
            val downloadURL = taskSnapshot.metadata!!.reference!!.downloadUrl.toString()

            myRef.child("Users").child(currentUser.uid).child("email").setValue(currentUser.email)
            myRef.child("Users").child(currentUser.uid).child("profileImage").setValue(currentUser.email)
            loadTweets()
        }
    }

    override fun onStart() {
        super.onStart()
        loadTweets()
    }

    fun splitSting(email:String):String{
        val split =  email.split("@")
        return split[0]
    }

    fun loadTweets(){
        var currentUser = mAuth!!.currentUser

        if(currentUser != null){
            var intent = Intent (this, MainActivity::class.java)
            intent.putExtra("email",currentUser.email)
            intent.putExtra("uid",currentUser.uid)
            startActivity(intent)
        }

    }

    fun buLoginEvent(view: View){

        loginToFirebase(etEmail.text.toString(), etPassword.text.toString())

    }

    val PICK_IMAGE_CODE = 123
    fun loadImage(){

        var intent = Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

        startActivityForResult(intent, PICK_IMAGE_CODE)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == PICK_IMAGE_CODE && data != null && resultCode == Activity.RESULT_OK){
            val selectedImage = data.data
            val filePathColum = arrayOf(MediaStore.Images.Media.DATA)
            val cursor = contentResolver.query(selectedImage!!, filePathColum, null , null ,null)
            cursor!!.moveToFirst()
            val columnIndex = cursor.getColumnIndex(filePathColum[0])
            val picturePath = cursor.getString(columnIndex)
            cursor.close()
            ivProfile.setImageBitmap(BitmapFactory.decodeFile(picturePath))
        }
    }


    val PERMISSION_CODE = 321
    fun checkPermission(){

        if(Build.VERSION.SDK_INT >= 23){
            if(ActivityCompat.checkSelfPermission(this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSION_CODE)
                return
            }
        }
        loadImage()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        when (requestCode){
            PERMISSION_CODE -> {
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    loadImage()
                }else{
                    Toast.makeText(this, "Cannot access your images", Toast.LENGTH_LONG).show()
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

}
