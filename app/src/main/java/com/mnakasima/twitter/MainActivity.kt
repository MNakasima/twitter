package com.mnakasima.twitter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.add_ticket.*
import kotlinx.android.synthetic.main.add_ticket.view.*
import kotlinx.android.synthetic.main.tweets_ticket.view.*
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {

    var listTweet = arrayListOf<Ticket>()
    var adapter:MyTweetAdapter?=null
    var myEmail:String?=null
    var userUID:String?=null

    private var database = FirebaseDatabase.getInstance()
    private var myRef = database.reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var bundle:Bundle? = intent.extras
        myEmail = bundle!!.getString("email")
        userUID = bundle!!.getString("uid")

        listTweet.add(Ticket("0","him","url","add"))

        adapter = MyTweetAdapter(this,listTweet)
        lvTweet.adapter = adapter

        LoadPost()
    }

    inner class MyTweetAdapter:BaseAdapter{

        var listTweetAdapter = arrayListOf<Ticket>()
        var context:Context?=null

        constructor(context:Context, listTweetAdapter: ArrayList<Ticket>):super(){
            this.listTweetAdapter=listTweetAdapter
            this.context = context
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

            var myTweet = listTweetAdapter[position]

            if (myTweet.personUID.equals("add")){
                //Load add ticket
                var myView = layoutInflater.inflate(R.layout.add_ticket, null)

                myView.iv_attach.setOnClickListener(View.OnClickListener {

                    loadImage()

                })

                myView.iv_post.setOnClickListener( View.OnClickListener {

                    myRef.child("posts").push().setValue(
                        PostInfo( userUID!!, myView.etPost.text.toString() , downloadURL!!))
                    myView.etPost.setText("")

                })
                return myView

            }else{
                var myView=layoutInflater.inflate(R.layout.tweets_ticket,null)
                myView.txt_tweet.text = myTweet.text

                //myView.tweet_picture.setImageURI(mytweet.tweetImageURL)
                Picasso.get().load(myTweet.image).into(myView.tweet_picture)

                myRef.child("Users").child(myTweet.personUID!!)
                    .addValueEventListener(object :ValueEventListener{

                        override fun onDataChange(dataSnapshot: DataSnapshot) {

                            try {

                                var td= dataSnapshot!!.value as HashMap<String,Any>

                                for(key in td.keys){

                                    var userInfo= td[key] as String
                                    if(key.equals("ProfileImage")){
                                        Picasso.get().load(userInfo).into(myView.picture_path)
                                    }else{
                                        myView.txtUserName.text = userInfo
                                    }



                                }

                            }catch (ex:Exception){}


                        }

                        override fun onCancelled(p0: DatabaseError) {

                        }
                    })

                return myView
            }

        }

        override fun getItem(position: Int): Any {
            return listTweetAdapter[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
    }

        override fun getCount(): Int {
            return listTweetAdapter.size
        }

    }

    val PICK_IMAGE_CODE = 123
    fun loadImage(){

        var intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

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
            uploadImage(BitmapFactory.decodeFile(picturePath))
        }
    }


    var downloadURL:String?=null
    fun uploadImage(bitmap:Bitmap){

        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.getReferenceFromUrl("gs://twitter-6f828.appspot.com")

        val df = SimpleDateFormat("ddMMyyHHmmss")
        val dataobj = Date()

        val imagePath = splitSting(myEmail!!) +"."+df.format(dataobj)+ ".jpg"
        val imageRef = storageRef.child("ImagesPost/"+imagePath)

        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        val uploadTask= imageRef.putBytes(data)

        uploadTask.addOnFailureListener{

            Toast.makeText(applicationContext,"fail to upload image", Toast.LENGTH_LONG).show()

        }.addOnSuccessListener {

                taskSnapshot ->
            downloadURL = taskSnapshot.metadata!!.reference!!.downloadUrl.toString()

        }
    }

    fun splitSting(email:String):String{
        val split =  email.split("@")
        return split[0]
    }

    fun LoadPost(){

        myRef.child("posts")
            .addValueEventListener(object :ValueEventListener{

                override fun onDataChange(dataSnapshot: DataSnapshot) {

                    try {

                        listTweet.clear()
                        listTweet.add(Ticket("0","him","url","add"))
                        listTweet.add(Ticket("0","him","url","ads"))
                        var td= dataSnapshot!!.value as HashMap<String,Any>

                        for(key in td.keys){

                            var post= td[key] as HashMap<String,Any>

                            listTweet.add(Ticket(key,

                                post["text"] as String,
                                post["postImage"] as String
                                ,post["userUID"] as String))


                        }


                        adapter!!.notifyDataSetChanged()
                    }catch (ex:Exception){}


                }

                override fun onCancelled(p0: DatabaseError) {

                }
            })
    }

}
