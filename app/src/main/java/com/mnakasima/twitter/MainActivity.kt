package com.mnakasima.twitter

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    var listTweet = arrayListOf<Ticket>()
    var adapter:MyTweetAdapter?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        listTweet.add(Ticket("0","him","url","add"))
        listTweet.add(Ticket("1","him","url","maruko"))

        adapter = MyTweetAdapter(this,listTweet)
        lvTweet.adapter = adapter
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
                return myView
            }else{
                //Load tweet ticket
                var myView = layoutInflater.inflate(R.layout.tweets_ticket, null)
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
}
