package com.example.chess

import android.R.attr.value
import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.net.UrlQuerySanitizer
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import io.grpc.example.chess.Table
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


class MainActivity : AppCompatActivity() {
    private lateinit var url:Uri
    private lateinit var chessRCP:ChessRCP
    private lateinit var tables: List<Table>
    private lateinit var blackPlayerName:TextView
    private lateinit var whitePlayerName:TextView
    private lateinit var isBlack:CheckBox
    private lateinit var isWhite:CheckBox
    private lateinit var serverURL:EditText
    private var currentTablePosition = 0
    private var currentTableId = 0

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //setContentView(Game(this))
        val btStart = findViewById<Button>(R.id.btStart)
        isBlack = findViewById(R.id.cbBlack)
        isWhite = findViewById(R.id.cbWhite)
        isWhite.isEnabled = false
        isBlack.isEnabled = false
        serverURL = findViewById(R.id.etServerURL)
        val playerName = findViewById<EditText>(R.id.etPlayerName)
        blackPlayerName = findViewById(R.id.tvBlackPlayerName)
        whitePlayerName = findViewById(R.id.tvWhitePlayerName)
        btStart.setOnClickListener {
            runBlocking {
                var table = io.grpc.example.chess.table {
                    id = tables[currentTablePosition].id
                    blackPlayer = blackPlayerName.text.toString()
                    whitePlayer = whitePlayerName.text.toString()
                }
                tables = chessRCP.setTable(table)!!
            }

            if(blackPlayerName.text.isNotEmpty() && whitePlayerName.text.isNotEmpty()) {
                launchGame()
            }
            btStart.text = "Waiting for other player"
            btStart.isEnabled = false
            CoroutineScope(Dispatchers.IO).launch{
                while(true) {
                    tables = chessRCP.getTables()!!
                    if (tables != null) {
                        var found = false
                        for (table in tables) {
                            if (table.id == currentTableId) {
                                found = true
                                break
                            }
                        }
                        Log.i("MYTAG","found = $found  currentTablePosition")
                        if(!found)
                        {
                            launchGame()
                            break
                        }
                    }
                    else
                        break
                    delay(1000)
                }
            }
        }

        isBlack.setOnClickListener {
            if(isBlack.isChecked) {
                blackPlayerName.text = playerName.text.toString()
                if(isWhite.isEnabled && whitePlayerName.text.isNotEmpty()) {
                    whitePlayerName.text = ""
                    isWhite.isChecked = false
                }
            }
            else
                blackPlayerName.text = ""
        }

        isWhite.setOnClickListener {
            if(isWhite.isChecked) {
                whitePlayerName.text = playerName.text.toString()
                if (isBlack.isEnabled && blackPlayerName.text.isNotEmpty()) {
                    blackPlayerName.text = ""
                    isBlack.isChecked = false
                }
            }
            else
                blackPlayerName.text = ""
        }

        url = Uri.parse(serverURL.text.toString())
        //url = Uri.parse("http://150.136.175.102:50051/")
       // url = Uri.parse("http://192.168.29.218:50051/")

        //chessRCP = ChessRCP(url)
        chessRCP = ChessRCP.getInstance(url)

        val btGetTables = findViewById<Button>(R.id.btGetTables)

        btGetTables.setOnClickListener {
            runBlocking {
                tables = chessRCP.getTables()!!
                if(tables!=null){
                    var array:Array<String> = arrayOf()
                    for(table in tables)
                    {
                        array += table.id.toString()
                    }
                    displayTablesInSpinner(array)
                }
            }
        }

    }
    private fun launchGame() {
        val myIntent = Intent(this@MainActivity, GameActivity::class.java)
        myIntent.putExtra("isBlack", isBlack.isChecked) //Optional parameters
        myIntent.putExtra("blackPlayerName", tables[currentTableId].blackPlayer.toString()) //Optional parameters
        myIntent.putExtra("whitePlayerName", tables[currentTableId].whitePlayer.toString()) //Optional parameters
        myIntent.putExtra("tableId", currentTableId)
        this@MainActivity.startActivity(myIntent)
    }

    private fun displayTablesInSpinner(array:Array<String>) {
        val mSpinner =  findViewById<Spinner>(R.id.spTables)
        var adapter:ArrayAdapter<String> = ArrayAdapter(this,R.layout.spinner_item,array)
        adapter.setDropDownViewResource(R.layout.drop_item)
        mSpinner.prompt="Please select your table."
        mSpinner.adapter=adapter
        mSpinner.setSelection(0)
        mSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            )
            {
                Log.i("MYTAG", "${array[position]}")
                currentTablePosition = position
                currentTableId = tables[position].id
                isWhite.isEnabled = tables[position].whitePlayer.isEmpty()
                isWhite.isChecked = false
                isBlack.isChecked = false
                whitePlayerName.text = tables[position].whitePlayer
                isBlack.isEnabled = tables[position].blackPlayer.isEmpty()
                blackPlayerName.text = tables[position].blackPlayer
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        }
    }
}